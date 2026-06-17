package org.turtleshop.api.modules.recommendation.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.recommendation.repository.BackfillGraphRepository;

import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BackfillService {

    private final JdbcTemplate jdbcTemplate;
    private final BackfillGraphRepository backfillGraphRepository;
    private final MongoTemplate mongoTemplate;

    private static final int BATCH_SIZE = 500;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public void executeOrderDataBackfill() {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, " +
                     "oi.product_id, oi.quantity, p.product_name " +
                     "FROM ORDERS o " +
                     "JOIN ORDER_ITEM oi ON o.order_id = oi.order_id " +
                     "JOIN PRODUCT p ON oi.product_id = p.product_id";

        jdbcTemplate.query(sql, (ResultSet rs) -> {
            List<Map<String, Object>> batch = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("order_id", rs.getInt("order_id"));
                row.put("customer_id", rs.getString("customer_id"));
                row.put("product_id", rs.getInt("product_id"));
                row.put("product_name", rs.getString("product_name"));
                row.put("quantity", rs.getInt("quantity"));

                Timestamp orderDate = rs.getTimestamp("order_date");
                String monthId = orderDate != null
                        ? orderDate.toLocalDateTime().format(MONTH_FORMATTER)
                        : LocalDate.now().format(MONTH_FORMATTER);
                row.put("month_id", monthId);

                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {
                    backfillGraphRepository.batchInsertOrders(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                backfillGraphRepository.batchInsertOrders(batch);
            }

            return null;
        });
    }

    public void executeReviewDataBackfill() {
        Query query = new Query();
        query.cursorBatchSize(BATCH_SIZE);

        try (Stream<Document> stream = mongoTemplate.stream(query, Document.class, "reviews")) {
            Iterator<Document> iterator = stream.iterator();
            List<Map<String, Object>> batch = new ArrayList<>();

            while (iterator.hasNext()) {
                Document reviewDoc = iterator.next();

                Map<String, Object> row = new HashMap<>();
                row.put("review_id", reviewDoc.get("reviewId"));
                row.put("customer_id", reviewDoc.getString("customerId"));
                row.put("product_id", reviewDoc.getInteger("productId"));
                row.put("rating", reviewDoc.get("rating"));
                row.put("comment", reviewDoc.getString("comment"));

                java.util.Date createdAt = reviewDoc.getDate("createdAt");
                String monthId = createdAt != null
                        ? createdAt.toInstant().atZone(java.time.ZoneId.systemDefault()).format(MONTH_FORMATTER)
                        : LocalDate.now().format(MONTH_FORMATTER);
                row.put("month_id", monthId);

                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {
                    backfillGraphRepository.batchInsertReviews(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                backfillGraphRepository.batchInsertReviews(batch);
            }
        }
    }
}