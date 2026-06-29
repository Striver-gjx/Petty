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
@DisplayName("WithdrawalController - 集成测试")
class WithdrawalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String sitterToken;

    @BeforeAll
    void login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "13900001111", "role", "SITTER"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());
        sitterToken = data.get("data").get("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("GET /withdrawals 列出提现记录(初始为空)")
    void listWithdrawals_empty() throws Exception {
        mockMvc.perform(get("/api/v1/withdrawals")
                        .header("Authorization", "Bearer " + sitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @Order(2)
    @DisplayName("POST /withdrawals 余额不足被拒绝")
    void requestWithdrawal_insufficientBalance() throws Exception {
        Map<String, Object> body = Map.of(
                "amount", 99999.00,
                "bankAccount", "6222021234567890123"
        );

        mockMvc.perform(post("/api/v1/withdrawals")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("余额不足")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /withdrawals 无效token请求余额不足")
    void requestWithdrawal_withBadToken() throws Exception {
        Map<String, Object> body = Map.of(
                "amount", 100.00,
                "bankAccount", "6222021234567890123"
        );

        mockMvc.perform(post("/api/v1/withdrawals")
                        .header("Authorization", "Bearer invalid_token_here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
