package org.turtleshop.api.modules.recommendation.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.recommendation.repository.BackfillGraphRepository;

import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            
            do {
                Map<String, Object> row = new HashMap<>();
                row.put("orderId", rs.getInt("order_id"));
                row.put("customerId", rs.getString("customer_id"));
                row.put("productId", rs.getInt("product_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("quantity", rs.getInt("quantity"));
                
                // Transform timestamp to Month ID (e.g., "2026-05")
                String monthId = rs.getTimestamp("order_date").toLocalDateTime().format(MONTH_FORMATTER);
                row.put("month_id", monthId);
                
                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {
                    backfillGraphRepository.batchInsertOrders(batch);
                    batch.clear();
                }
            } while (rs.next());

            if (!batch.isEmpty()) {
                backfillGraphRepository.batchInsertOrders(batch);
            }
            return null;
        });
    }

    public void executeReviewDataBackfill() {
        Query query = new Query();
        query.cursorBatchSize(BATCH_SIZE);
        try (java.util.stream.Stream<Document> stream = mongoTemplate.stream(query, Document.class, "reviews")) {
            List<Map<String, Object>> batch = new ArrayList<>();
            java.util.Iterator<Document> cursor = stream.iterator();

            while (cursor.hasNext()) {
                Document reviewDoc = cursor.next();

                Map<String, Object> row = new HashMap<>();
                
                row.put("reviewId", reviewDoc.get("reviewId"));
                row.put("customerId", reviewDoc.get("customerId").toString());
                row.put("productId", reviewDoc.get("productId"));
                row.put("rating", reviewDoc.get("rating"));
                row.put("comment", reviewDoc.get("comment"));

                // Handle the MongoDB ISODate to Month ID transformation
                java.util.Date createdAt = (java.util.Date) reviewDoc.get("createdAt");
                String monthId = createdAt.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(MONTH_FORMATTER);
                
                row.put("monthId", monthId);
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