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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("OrderController - 完整流程集成测试")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String sitterToken;

    @BeforeAll
    void login() throws Exception {
        MvcResult ownerResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("phone", "13800001111", "role", "OWNER"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode ownerData = objectMapper.readTree(ownerResult.getResponse().getContentAsString());
        ownerToken = ownerData.get("data").get("token").asText();

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
    @DisplayName("GET /service-types 返回 6 种服务")
    void listServiceTypes() throws Exception {
        mockMvc.perform(get("/api/v1/service-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(6)));
    }

    @Test
    @Order(2)
    @DisplayName("POST /orders 创建订单并自动匹配")
    void createOrder() throws Exception {
        Map<String, Object> body = Map.of(
                "serviceTypeId", 1,
                "petIds", List.of(1),
                "scheduledDate", "2026-08-01",
                "scheduledStartTime", "10:00",
                "scheduledEndTime", "10:30",
                "serviceAddress", "北京市朝阳区建国路88号",
                "latitude", 39.9087,
                "longitude", 116.4716
        );

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(49.0))
                .andExpect(jsonPath("$.data.status").value("PENDING_ACCEPT"))
                .andExpect(jsonPath("$.data.sitterName").isNotEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("POST /payments/pay 支付预授权")
    void initiatePayment() throws Exception {
        Map<String, Object> body = Map.of(
                "orderId", 1,
                "paymentMethod", "WECHAT"
        );

        mockMvc.perform(post("/api/v1/payments/pay")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.prepayId").isNotEmpty())
                .andExpect(jsonPath("$.data.timeStamp").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("POST /orders/{id}/accept 喂养师接单")
    void acceptOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/accept")
                        .header("Authorization", "Bearer " + sitterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    @DisplayName("POST /orders/{id}/check-in GPS打卡")
    void checkIn() throws Exception {
        Map<String, Object> body = Map.of(
                "latitude", 39.9087,
                "longitude", 116.4716,
                "photoUrl", "https://oss.petty.com/checkin.jpg"
        );

        mockMvc.perform(post("/api/v1/orders/1/check-in")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("POST /orders/{id}/logs 上传服务记录")
    void addServiceLog() throws Exception {
        Map<String, Object> body = Map.of(
                "logType", "FEEDING",
                "description", "已喂食猫粮50g",
                "photoUrls", List.of("https://oss.petty.com/feed.jpg"),
                "petStatus", "食欲正常",
                "latitude", 39.9087,
                "longitude", 116.4716
        );

        mockMvc.perform(post("/api/v1/orders/1/logs")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    @DisplayName("POST /orders/{id}/check-out 服务完成打卡")
    void checkOut() throws Exception {
        Map<String, Object> body = Map.of(
                "latitude", 39.9088,
                "longitude", 116.4717,
                "photoUrl", "https://oss.petty.com/checkout.jpg",
                "serviceReport", "服务完成，猫咪状态良好"
        );

        mockMvc.perform(post("/api/v1/orders/1/check-out")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(8)
    @DisplayName("POST /orders/{id}/confirm 主人确认")
    void confirmOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/confirm")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @DisplayName("GET /orders/{id} 查看已确认订单详情")
    void getOrderDetail() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OWNER_CONFIRMED"))
                .andExpect(jsonPath("$.data.pets", hasSize(1)))
                .andExpect(jsonPath("$.data.serviceLogs", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @Order(10)
    @DisplayName("POST /reviews 提交评价")
    void createReview() throws Exception {
        Map<String, Object> body = Map.of(
                "orderId", 1,
                "rating", 5.0,
                "content", "非常满意",
                "tags", List.of("准时", "专业"),
                "isAnonymous", false
        );

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(11)
    @DisplayName("GET /reviews/sitter/{id} 查看喂养师评价")
    void listSitterReviews() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/sitter/1")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].rating").value(5.0));
    }

    @Test
    @Order(12)
    @DisplayName("GPS 超距打卡返回错误")
    void checkIn_outOfRange_returnError() throws Exception {
        Map<String, Object> createBody = Map.of(
                "serviceTypeId", 1,
                "petIds", List.of(1),
                "scheduledDate", "2026-08-02",
                "scheduledStartTime", "14:00",
                "scheduledEndTime", "14:30",
                "serviceAddress", "北京市海淀区",
                "latitude", 39.9087,
                "longitude", 116.4716
        );

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders/2/accept")
                        .header("Authorization", "Bearer " + sitterToken))
                .andExpect(status().isOk());

        Map<String, Object> farCheckIn = Map.of(
                "latitude", 40.5000,
                "longitude", 117.5000,
                "photoUrl", "https://example.com/far.jpg"
        );

        mockMvc.perform(post("/api/v1/orders/2/check-in")
                        .header("Authorization", "Bearer " + sitterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(farCheckIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("GPS距离")));
    }
}
