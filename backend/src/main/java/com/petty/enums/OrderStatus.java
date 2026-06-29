package com.petty.enums;

import java.util.*;

public enum OrderStatus {
    PENDING_MATCH("待匹配"),
    PENDING_ACCEPT("待接单"),
    ACCEPTED("已接单"),
    SITTER_EN_ROUTE("前往中"),
    IN_SERVICE("服务中"),
    SERVICE_COMPLETED("服务完成"),
    OWNER_CONFIRMED("已确认"),
    CANCELLED("已取消"),
    DISPUTED("争议中"),
    REFUNDED("已退款");

    private final String label;
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(PENDING_MATCH, Set.of(PENDING_ACCEPT, CANCELLED));
        TRANSITIONS.put(PENDING_ACCEPT, Set.of(ACCEPTED, PENDING_MATCH, CANCELLED));
        TRANSITIONS.put(ACCEPTED, Set.of(SITTER_EN_ROUTE, CANCELLED));
        TRANSITIONS.put(SITTER_EN_ROUTE, Set.of(IN_SERVICE, CANCELLED));
        TRANSITIONS.put(IN_SERVICE, Set.of(SERVICE_COMPLETED, DISPUTED));
        TRANSITIONS.put(SERVICE_COMPLETED, Set.of(OWNER_CONFIRMED, DISPUTED));
        TRANSITIONS.put(OWNER_CONFIRMED, Set.of());
        TRANSITIONS.put(CANCELLED, Set.of(REFUNDED));
        TRANSITIONS.put(DISPUTED, Set.of(OWNER_CONFIRMED, CANCELLED, REFUNDED));
        TRANSITIONS.put(REFUNDED, Set.of());
    }

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitionTo(OrderStatus target) {
        Set<OrderStatus> allowed = TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    public boolean isCancellable() {
        return this == PENDING_MATCH || this == PENDING_ACCEPT || this == ACCEPTED || this == SITTER_EN_ROUTE;
    }
}
