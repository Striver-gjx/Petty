package com.petty.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderStatus - 状态机测试")
class OrderStatusTest {

    @Test
    @DisplayName("PENDING_MATCH 可以流转到 PENDING_ACCEPT 和 CANCELLED")
    void pendingMatch_validTransitions() {
        assertThat(OrderStatus.PENDING_MATCH.canTransitionTo(OrderStatus.PENDING_ACCEPT)).isTrue();
        assertThat(OrderStatus.PENDING_MATCH.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING_MATCH.canTransitionTo(OrderStatus.IN_SERVICE)).isFalse();
    }

    @Test
    @DisplayName("PENDING_ACCEPT 可以流转到 ACCEPTED, PENDING_MATCH, CANCELLED")
    void pendingAccept_validTransitions() {
        assertThat(OrderStatus.PENDING_ACCEPT.canTransitionTo(OrderStatus.ACCEPTED)).isTrue();
        assertThat(OrderStatus.PENDING_ACCEPT.canTransitionTo(OrderStatus.PENDING_MATCH)).isTrue();
        assertThat(OrderStatus.PENDING_ACCEPT.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING_ACCEPT.canTransitionTo(OrderStatus.IN_SERVICE)).isFalse();
    }

    @Test
    @DisplayName("IN_SERVICE 只能流转到 SERVICE_COMPLETED 和 DISPUTED")
    void inService_validTransitions() {
        assertThat(OrderStatus.IN_SERVICE.canTransitionTo(OrderStatus.SERVICE_COMPLETED)).isTrue();
        assertThat(OrderStatus.IN_SERVICE.canTransitionTo(OrderStatus.DISPUTED)).isTrue();
        assertThat(OrderStatus.IN_SERVICE.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    @DisplayName("OWNER_CONFIRMED 是终态，不可再流转")
    void ownerConfirmed_isTerminal() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.OWNER_CONFIRMED.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    @DisplayName("REFUNDED 是终态，不可再流转")
    void refunded_isTerminal() {
        for (OrderStatus target : OrderStatus.values()) {
            assertThat(OrderStatus.REFUNDED.canTransitionTo(target)).isFalse();
        }
    }

    @Test
    @DisplayName("isCancellable 只在 PENDING_MATCH/PENDING_ACCEPT/ACCEPTED/SITTER_EN_ROUTE 返回 true")
    void isCancellable() {
        assertThat(OrderStatus.PENDING_MATCH.isCancellable()).isTrue();
        assertThat(OrderStatus.PENDING_ACCEPT.isCancellable()).isTrue();
        assertThat(OrderStatus.ACCEPTED.isCancellable()).isTrue();
        assertThat(OrderStatus.SITTER_EN_ROUTE.isCancellable()).isTrue();
        assertThat(OrderStatus.IN_SERVICE.isCancellable()).isFalse();
        assertThat(OrderStatus.SERVICE_COMPLETED.isCancellable()).isFalse();
        assertThat(OrderStatus.OWNER_CONFIRMED.isCancellable()).isFalse();
        assertThat(OrderStatus.CANCELLED.isCancellable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("每个状态都有非空 label")
    void everyStatus_hasLabel(OrderStatus status) {
        assertThat(status.getLabel()).isNotBlank();
    }
}
