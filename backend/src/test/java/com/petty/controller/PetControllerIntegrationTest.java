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
@DisplayName("PetController - 集成测试")
class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;

    @BeforeAll
    void login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "13800001111", "role", "OWNER"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());
        ownerToken = data.get("data").get("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("GET /pets 列出当前用户的宠物")
    void listOwnPets() throws Exception {
        mockMvc.perform(get("/api/v1/pets")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /pets?ownerId=1 OWNER角色忽略ownerId参数返回自己的")
    void listByOwner() throws Exception {
        mockMvc.perform(get("/api/v1/pets?ownerId=1")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("小橘"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /pets/{id} 获取指定宠物")
    void getPet() throws Exception {
        mockMvc.perform(get("/api/v1/pets/1")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("小橘"))
                .andExpect(jsonPath("$.data.species").value("CAT"))
                .andExpect(jsonPath("$.data.breed").value("橘猫"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /pets 创建宠物")
    void createPet() throws Exception {
        Map<String, Object> body = Map.of(
                "ownerId", 1,
                "name", "球球",
                "species", "DOG",
                "breed", "泰迪",
                "gender", "FEMALE",
                "weight", 3.5
        );

        mockMvc.perform(post("/api/v1/pets")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("球球"));
    }

    @Test
    @Order(5)
    @DisplayName("PUT /pets/{id} 更新宠物")
    void updatePet() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "球球plus",
                "weight", 4.0
        );

        mockMvc.perform(put("/api/v1/pets/4")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /pets/{id} 删除宠物")
    void deletePet() throws Exception {
        mockMvc.perform(delete("/api/v1/pets/4")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/v1/pets/4")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
