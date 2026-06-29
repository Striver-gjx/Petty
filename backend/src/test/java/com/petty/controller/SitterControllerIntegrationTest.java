package com.petty.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("SitterController - 集成测试")
class SitterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String sitterToken;

    @BeforeAll
    void login() throws Exception {
        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "13800000000", "role", "ADMIN"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode adminData = objectMapper.readTree(adminResult.getResponse().getContentAsString());
        adminToken = adminData.get("data").get("token").asText();

        MvcResult sitterResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "13900001111", "role", "SITTER"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sitterData = objectMapper.readTree(sitterResult.getResponse().getContentAsString());
        sitterToken = sitterData.get("data").get("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("GET /sitters 列出所有喂养师")
    void listSitters() throws Exception {
        mockMvc.perform(get("/api/v1/sitters")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @Order(2)
    @DisplayName("GET /sitters?status=ACTIVE 按状态筛选")
    void listByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/sitters?status=ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /sitters/{id} 获取指定喂养师")
    void getSitter() throws Exception {
        mockMvc.perform(get("/api/v1/sitters/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("王大勇"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /sitters/apply 申请入驻（无需ADMIN角色）")
    void applyOnboard() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "新喂养师",
                "phone", "13900009999",
                "idCard", "110101199001011234",
                "bio", "新人入驻测试",
                "experienceYears", 1,
                "serviceArea", "北京市丰台区"
        );

        mockMvc.perform(post("/api/v1/sitters/apply")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /sitters/{id}/approve 审核通过（需ADMIN角色）")
    void approveSitter() throws Exception {
        mockMvc.perform(post("/api/v1/sitters/3/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/sitters/3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /sitters/{id}/approve 非ADMIN被拒绝")
    void approveWithoutAdmin_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/sitters/3/approve")
                        .header("Authorization", "Bearer " + sitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message", containsString("权限不足")));
    }

    @Test
    @Order(7)
    @DisplayName("POST /sitters ADMIN创建喂养师")
    void createSitter() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "ADMIN创建",
                "phone", "13900008888",
                "bio", "管理员直接创建",
                "experienceYears", 2,
                "serviceArea", "北京市通州区",
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/v1/sitters")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("ADMIN创建"));
    }

    @Test
    @Order(8)
    @DisplayName("PUT /sitters/{id} ADMIN更新喂养师")
    void updateSitter() throws Exception {
        Map<String, Object> body = Map.of(
                "bio", "更新后的简介"
        );

        mockMvc.perform(put("/api/v1/sitters/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
