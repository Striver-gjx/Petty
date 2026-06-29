package com.petty.service.boundary;

import com.petty.entity.Sitter;
import com.petty.service.matching.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MatchingEngine - 边界条件测试")
class MatchingEngineBoundaryTest {

    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine();
    }

    @Test
    @DisplayName("空候选列表 - 返回空结果")
    void rank_emptyCandidates_returnsEmpty() {
        List<MatchingEngine.ScoredSitter> result = engine.rank(
                Collections.emptyList(),
                new BigDecimal("39.9"),
                new BigDecimal("116.4"));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("单个候选 - 直接返回该喂养师")
    void rank_singleCandidate_returnsIt() {
        Sitter sitter = createSitter(1L, "39.9", "116.4", "5.0", 5.0, 100.0, 10);
        List<MatchingEngine.ScoredSitter> result = engine.rank(
                List.of(sitter),
                new BigDecimal("39.9"),
                new BigDecimal("116.4"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).sitter().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("距离过滤 - 超出半径的被排除")
    void filterByDistance_farAway_excluded() {
        Sitter near = createSitter(1L, "39.9087", "116.4716", "5.0", 5.0, 100.0, 10);
        near.setServiceRadiusKm(new BigDecimal("3.0"));

        Sitter far = createSitter(2L, "40.5", "117.5", "5.0", 5.0, 100.0, 10);
        far.setServiceRadiusKm(new BigDecimal("5.0"));

        List<Sitter> result = engine.filterByDistance(
                List.of(near, far),
                new BigDecimal("39.9087"),
                new BigDecimal("116.4716"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("距离过滤 - 所有候选超出半径返回空")
    void filterByDistance_allFar_empty() {
        Sitter far1 = createSitter(1L, "40.5", "117.5", "5.0", 5.0, 100.0, 10);
        far1.setServiceRadiusKm(new BigDecimal("1.0"));
        Sitter far2 = createSitter(2L, "41.0", "118.0", "5.0", 5.0, 100.0, 10);
        far2.setServiceRadiusKm(new BigDecimal("2.0"));

        List<Sitter> result = engine.filterByDistance(
                List.of(far1, far2),
                new BigDecimal("39.9"),
                new BigDecimal("116.4"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("物种过滤 - 不接受该物种的被排除")
    void filterBySpecies_notAccepted_excluded() {
        Sitter catOnly = createSitter(1L, "39.9", "116.4", "5.0", 5.0, 100.0, 10);
        catOnly.setAcceptedSpecies("[\"CAT\"]");

        Sitter dogOnly = createSitter(2L, "39.9", "116.4", "5.0", 5.0, 100.0, 10);
        dogOnly.setAcceptedSpecies("[\"DOG\"]");

        List<Sitter> result = engine.filterBySpecies(List.of(catOnly, dogOnly), "CAT");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("物种过滤 - acceptedSpecies 为 null 默认接受所有")
    void filterBySpecies_nullAccepted_includesAll() {
        Sitter sitter = createSitter(1L, "39.9", "116.4", "5.0", 5.0, 100.0, 10);
        sitter.setAcceptedSpecies(null);

        List<Sitter> result = engine.filterBySpecies(List.of(sitter), "REPTILE");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("排名 - 评分更高的排名靠前")
    void rank_higherRating_ranksFirst() {
        Sitter highRating = createSitter(1L, "39.91", "116.48", "5.0", 4.95, 100.0, 5);
        Sitter lowRating = createSitter(2L, "39.91", "116.48", "5.0", 3.0, 50.0, 30);

        List<MatchingEngine.ScoredSitter> ranked = engine.rank(
                List.of(lowRating, highRating),
                new BigDecimal("39.91"),
                new BigDecimal("116.48"));

        assertThat(ranked.get(0).sitter().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("排名 - null 坐标的喂养师被距离过滤排除")
    void filterByDistance_nullCoords_excluded() {
        Sitter nullCoords = createSitter(1L, null, null, "5.0", 5.0, 100.0, 10);
        nullCoords.setServiceRadiusKm(new BigDecimal("10.0"));

        List<Sitter> result = engine.filterByDistance(
                List.of(nullCoords),
                new BigDecimal("39.9"),
                new BigDecimal("116.4"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("排名 - responseTimeMin 为 null 使用默认值")
    void rank_nullResponseTime_usesDefault() {
        Sitter s1 = createSitter(1L, "39.9", "116.4", "5.0", 4.5, 90.0, null);
        Sitter s2 = createSitter(2L, "39.9", "116.4", "5.0", 4.8, 95.0, 15);

        List<MatchingEngine.ScoredSitter> ranked = engine.rank(
                List.of(s1, s2),
                new BigDecimal("39.9"),
                new BigDecimal("116.4"));

        assertThat(ranked).hasSize(2);
    }

    private Sitter createSitter(Long id, String lat, String lng, String radius,
                                double rating, double completion, Integer responseTime) {
        Sitter s = new Sitter();
        s.setId(id);
        s.setName("Sitter-" + id);
        s.setStatus("ACTIVE");
        s.setHomeLatitude(lat != null ? new BigDecimal(lat) : null);
        s.setHomeLongitude(lng != null ? new BigDecimal(lng) : null);
        s.setServiceRadiusKm(new BigDecimal(radius));
        s.setRating(BigDecimal.valueOf(rating));
        s.setCompletionRate(BigDecimal.valueOf(completion));
        s.setResponseTimeMin(responseTime);
        return s;
    }
}
