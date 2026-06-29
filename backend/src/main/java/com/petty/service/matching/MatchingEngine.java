package com.petty.service.matching;

import com.petty.entity.Sitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static final double DISTANCE_WEIGHT = 0.30;
    private static final double RATING_WEIGHT = 0.30;
    private static final double COMPLETION_WEIGHT = 0.20;
    private static final double RESPONSE_WEIGHT = 0.20;

    /**
     * 从候选喂养师列表中选出最优匹配
     * @param candidates 候选喂养师（已过滤可用性和物种）
     * @param serviceLat 服务地址纬度
     * @param serviceLng 服务地址经度
     * @return 排序后的喂养师列表（分数从高到低）
     */
    public List<ScoredSitter> rank(List<Sitter> candidates, BigDecimal serviceLat, BigDecimal serviceLng) {
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredSitter> scored = new ArrayList<>();
        double maxDistance = 0;
        double maxResponseTime = 0;

        List<Double> distances = new ArrayList<>();
        for (Sitter s : candidates) {
            double dist = haversineKm(
                    serviceLat.doubleValue(), serviceLng.doubleValue(),
                    s.getHomeLatitude() != null ? s.getHomeLatitude().doubleValue() : 0,
                    s.getHomeLongitude() != null ? s.getHomeLongitude().doubleValue() : 0
            );
            distances.add(dist);
            maxDistance = Math.max(maxDistance, dist);
            maxResponseTime = Math.max(maxResponseTime, s.getResponseTimeMin() != null ? s.getResponseTimeMin() : 0);
        }

        for (int i = 0; i < candidates.size(); i++) {
            Sitter s = candidates.get(i);
            double dist = distances.get(i);

            double distScore = maxDistance > 0 ? 1.0 - (dist / maxDistance) : 1.0;
            double ratingScore = s.getRating() != null ? s.getRating().doubleValue() / 5.0 : 0.5;
            double completionScore = s.getCompletionRate() != null ? s.getCompletionRate().doubleValue() / 100.0 : 0.5;
            double responseScore = maxResponseTime > 0 && s.getResponseTimeMin() != null
                    ? 1.0 - (s.getResponseTimeMin().doubleValue() / maxResponseTime)
                    : 0.5;

            double totalScore = distScore * DISTANCE_WEIGHT
                    + ratingScore * RATING_WEIGHT
                    + completionScore * COMPLETION_WEIGHT
                    + responseScore * RESPONSE_WEIGHT;

            scored.add(new ScoredSitter(s, totalScore, dist));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredSitter::score).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 过滤服务半径内的喂养师
     */
    public List<Sitter> filterByDistance(List<Sitter> sitters, BigDecimal lat, BigDecimal lng) {
        return sitters.stream().filter(s -> {
            if (s.getHomeLatitude() == null || s.getHomeLongitude() == null) return false;
            double dist = haversineKm(lat.doubleValue(), lng.doubleValue(),
                    s.getHomeLatitude().doubleValue(), s.getHomeLongitude().doubleValue());
            double radius = s.getServiceRadiusKm() != null ? s.getServiceRadiusKm().doubleValue() : 5.0;
            return dist <= radius;
        }).collect(Collectors.toList());
    }

    /**
     * 过滤接受指定物种的喂养师
     */
    public List<Sitter> filterBySpecies(List<Sitter> sitters, String species) {
        return sitters.stream().filter(s -> {
            String accepted = s.getAcceptedSpecies();
            if (accepted == null || accepted.isEmpty()) return true;
            return accepted.contains(species);
        }).collect(Collectors.toList());
    }

    /**
     * 过滤在指定日期和时间段有空闲排班的喂养师
     * 如果喂养师没有排班记录，默认认为全天可用
     */
    public List<Sitter> filterBySchedule(List<Sitter> sitters, List<com.petty.entity.SitterSchedule> schedules,
                                          java.time.LocalDate date, java.time.LocalTime startTime) {
        if (schedules == null || schedules.isEmpty()) return sitters;

        Map<Long, List<com.petty.entity.SitterSchedule>> byId = schedules.stream()
                .collect(Collectors.groupingBy(com.petty.entity.SitterSchedule::getSitterId));

        return sitters.stream().filter(s -> {
            List<com.petty.entity.SitterSchedule> slots = byId.get(s.getId());
            if (slots == null || slots.isEmpty()) return true;
            return slots.stream().anyMatch(slot ->
                    "AVAILABLE".equals(slot.getStatus()) &&
                    slot.getBookedOrders() < slot.getMaxOrders() &&
                    !startTime.isBefore(slot.getTimeSlotStart()) &&
                    !startTime.isAfter(slot.getTimeSlotEnd()));
        }).collect(Collectors.toList());
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public record ScoredSitter(Sitter sitter, double score, double distanceKm) {}
}
