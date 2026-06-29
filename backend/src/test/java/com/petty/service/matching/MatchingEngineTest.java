package com.petty.service.matching;

import com.petty.entity.Sitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MatchingEngine - 匹配算法测试")
class MatchingEngineTest {

    private MatchingEngine engine;
    private final BigDecimal SERVICE_LAT = new BigDecimal("39.9087");
    private final BigDecimal SERVICE_LNG = new BigDecimal("116.4716");

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine();
    }

    private Sitter createSitter(Long id, String name, double lat, double lng, double radiusKm,
                                 double rating, int totalOrders, double completionRate, int responseMin, String species) {
        Sitter s = new Sitter();
        s.setId(id);
        s.setName(name);
        s.setHomeLatitude(BigDecimal.valueOf(lat));
        s.setHomeLongitude(BigDecimal.valueOf(lng));
        s.setServiceRadiusKm(BigDecimal.valueOf(radiusKm));
        s.setRating(BigDecimal.valueOf(rating));
        s.setTotalOrders(totalOrders);
        s.setCompletionRate(BigDecimal.valueOf(completionRate));
        s.setResponseTimeMin(responseMin);
        s.setAcceptedSpecies(species);
        s.setStatus("ACTIVE");
        return s;
    }

    @Nested
    @DisplayName("filterByDistance")
    class FilterByDistanceTest {

        @Test
        @DisplayName("服务半径内的喂养师通过")
        void withinRadius_included() {
            Sitter nearby = createSitter(1L, "近距离", 39.909, 116.472, 5.0, 4.5, 50, 95.0, 10, "CAT,DOG");
            List<Sitter> result = engine.filterByDistance(List.of(nearby), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("超出服务半径的喂养师被过滤")
        void outsideRadius_excluded() {
            Sitter farAway = createSitter(2L, "远距离", 40.5, 117.0, 3.0, 4.8, 100, 98.0, 5, "CAT");
            List<Sitter> result = engine.filterByDistance(List.of(farAway), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("坐标为 null 的喂养师被过滤")
        void nullCoordinates_excluded() {
            Sitter noCoords = new Sitter();
            noCoords.setId(3L);
            noCoords.setServiceRadiusKm(BigDecimal.TEN);
            List<Sitter> result = engine.filterByDistance(List.of(noCoords), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("默认半径 5km 适用于未设置 serviceRadiusKm 的喂养师")
        void defaultRadius_5km() {
            Sitter nearby = createSitter(4L, "默认半径", 39.910, 116.473, 0, 4.0, 20, 90.0, 15, "DOG");
            nearby.setServiceRadiusKm(null);
            List<Sitter> result = engine.filterByDistance(List.of(nearby), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("filterBySpecies")
    class FilterBySpeciesTest {

        @Test
        @DisplayName("接受 CAT 的喂养师匹配猫订单")
        void acceptsCat_matchesCat() {
            Sitter catSitter = createSitter(1L, "猫师", 39.909, 116.472, 5.0, 4.5, 50, 95.0, 10, "CAT,DOG");
            List<Sitter> result = engine.filterBySpecies(List.of(catSitter), "CAT");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("只接受 DOG 的喂养师不匹配猫订单")
        void acceptsDogOnly_rejectsCat() {
            Sitter dogOnly = createSitter(2L, "狗师", 39.909, 116.472, 5.0, 4.8, 30, 92.0, 8, "DOG");
            List<Sitter> result = engine.filterBySpecies(List.of(dogOnly), "CAT");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("未设置物种限制的喂养师接受所有物种")
        void noSpeciesSet_acceptsAll() {
            Sitter any = createSitter(3L, "通用师", 39.909, 116.472, 5.0, 4.0, 10, 88.0, 20, null);
            List<Sitter> result = engine.filterBySpecies(List.of(any), "REPTILE");
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("rank")
    class RankTest {

        @Test
        @DisplayName("空列表返回空结果")
        void emptyCandidates_returnsEmpty() {
            List<MatchingEngine.ScoredSitter> result = engine.rank(Collections.emptyList(), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("距离近+评分高的喂养师排名靠前")
        void closerAndHigherRating_ranksHigher() {
            Sitter close = createSitter(1L, "近+高分", 39.909, 116.472, 5.0, 4.9, 80, 98.0, 5, "CAT");
            Sitter far = createSitter(2L, "远+低分", 39.95, 116.50, 10.0, 3.5, 10, 70.0, 30, "CAT");

            List<MatchingEngine.ScoredSitter> result = engine.rank(Arrays.asList(close, far), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).sitter().getName()).isEqualTo("近+高分");
            assertThat(result.get(0).score()).isGreaterThan(result.get(1).score());
        }

        @Test
        @DisplayName("单个候选人也能正常排名")
        void singleCandidate_ranksNormally() {
            Sitter only = createSitter(1L, "唯一", 39.909, 116.472, 5.0, 4.5, 50, 95.0, 10, "CAT");
            List<MatchingEngine.ScoredSitter> result = engine.rank(List.of(only), SERVICE_LAT, SERVICE_LNG);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).score()).isGreaterThan(0);
        }

        @Test
        @DisplayName("分数范围在 0 到 1 之间")
        void scores_between0And1() {
            Sitter s1 = createSitter(1L, "A", 39.909, 116.472, 5.0, 5.0, 100, 100.0, 1, "CAT");
            Sitter s2 = createSitter(2L, "B", 39.95, 116.50, 10.0, 1.0, 5, 50.0, 60, "CAT");

            List<MatchingEngine.ScoredSitter> result = engine.rank(Arrays.asList(s1, s2), SERVICE_LAT, SERVICE_LNG);
            for (MatchingEngine.ScoredSitter scored : result) {
                assertThat(scored.score()).isBetween(0.0, 1.0);
            }
        }
    }
}
