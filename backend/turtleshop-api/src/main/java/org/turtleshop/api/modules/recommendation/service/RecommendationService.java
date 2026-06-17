package org.turtleshop.api.modules.recommendation.service;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.recommendation.dto.RecommendedProduct;
import org.turtleshop.api.modules.recommendation.repository.RecommendationRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public RecommendationService(RecommendationRepository recommendationRepository,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.recommendationRepository = recommendationRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<RecommendedProduct> getHydratedRecommendations(String customerId, int limit) {
        String currentSeason = determineCurrentSeason();
        List<Integer> productIds = recommendationRepository.getSeasonalCollaborativeRecommendations(customerId, currentSeason, limit);
        return hydrateProducts(productIds);
    }

    public List<RecommendedProduct> getTopProductsSoldThisMonth(int limit) {
        String currentMonthId = LocalDate.now().format(MONTH_FORMATTER);
        List<Integer> productIds = recommendationRepository.getTopProductsByCurrentMonth(currentMonthId, limit);
        return hydrateProducts(productIds);
    }

    public List<RecommendedProduct> getTopProductsSoldThisSeason(int limit) {
        String currentSeason = determineCurrentSeason();
        List<Integer> productIds = recommendationRepository.getTopProductsByCurrentSeason(currentSeason, limit);
        return hydrateProducts(productIds);
    }

    public List<RecommendedProduct> getFrequentlyBoughtTogether(int productId, int limit) {
        List<Integer> productIds = recommendationRepository.getFrequentlyBoughtTogether(productId, limit);
        return hydrateProducts(productIds);
    }

    private List<RecommendedProduct> hydrateProducts(List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = """
            SELECT product_id, product_name, base_price AS price, NULL AS image_url
            FROM PRODUCT
            WHERE product_id IN (:productIds)
            """;

        List<RecommendedProduct> hydratedProducts = namedParameterJdbcTemplate.query(
            sql,
            Map.of("productIds", productIds),
            (rs, rowNum) -> new RecommendedProduct(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getBigDecimal("price"),
                rs.getString("image_url")
            )
        );

        Map<Integer, RecommendedProduct> productLookup = hydratedProducts.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(RecommendedProduct::productId, Function.identity()));

        return productIds.stream()
            .map(productLookup::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private String determineCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        return switch (month) {
            case 12, 1, 2 -> "Winter";
            case 3, 4, 5 -> "Spring";
            case 6, 7, 8 -> "Summer";
            default -> "Autumn";
        };
    }
}