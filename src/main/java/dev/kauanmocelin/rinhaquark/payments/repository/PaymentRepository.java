package dev.kauanmocelin.rinhaquark.payments.repository;

import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PaymentRepository {

    @Inject
    DataSource dataSource;

    public List<PaymentSummary> summaryPayments(final Instant from, final Instant to) {
        String sql = """
            SELECT payment_processor_type AS type,
                   COUNT(*) AS total_requests,
                   SUM(amount) AS total_amount
            FROM payments
            WHERE requested_at >= ? AND requested_at <= ?
            GROUP BY payment_processor_type
            """;
        List<PaymentSummary> summaries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.from(from));
                ps.setTimestamp(2, Timestamp.from(to));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        summaries.add(new PaymentSummary(
                            rs.getString("type"),
                            rs.getInt("total_requests"),
                            rs.getBigDecimal("total_amount")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying payment summary", e);
        }

        return summaries;
    }

    public void createPaymentsBatch(List<Payment> paymentsBatch) {
        String sql = """
            INSERT INTO payments (correlation_id, requested_at, payment_processor_type, amount)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Payment payment : paymentsBatch) {
                    ps.setObject(1, payment.correlationId);
                    ps.setTimestamp(2, Timestamp.from(payment.requestedAt));
                    ps.setString(3, payment.paymentProcessorType != null ? payment.paymentProcessorType.name() : null);
                    ps.setBigDecimal(4, payment.amount);
                    ps.addBatch();
                }

                int[] results = ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir batch de pagamentos", e);
        }
    }
}