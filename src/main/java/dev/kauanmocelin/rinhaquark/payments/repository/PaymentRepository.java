package dev.kauanmocelin.rinhaquark.payments.repository;

import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                            PaymentProcessorType.fromCode(rs.getInt("type")).name(),
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

    public String executeRawJson(final Instant from, final Instant to) {
        String sql = """
            SELECT payment_processor_type AS type,
                   COUNT(*) AS total_requests,
                   SUM(amount) AS total_amount
            FROM payments
            WHERE requested_at >= ? AND requested_at <= ?
            GROUP BY payment_processor_type
            """;

        // valores padrão
        int defaultRequests = 0;
        BigDecimal defaultAmount = BigDecimal.ZERO;
        int fallbackRequests = 0;
        BigDecimal fallbackAmount = BigDecimal.ZERO;

        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setTimestamp(1, Timestamp.from(from));
                ps.setTimestamp(2, Timestamp.from(to));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int type = rs.getInt("type");
                        int totalRequests = rs.getInt("total_requests");
                        BigDecimal totalAmount = rs.getBigDecimal("total_amount");

                        if (type == PaymentProcessorType.DEFAULT.getCode()) {
                            defaultRequests = totalRequests;
                            defaultAmount = totalAmount;
                        } else if (type == PaymentProcessorType.FALLBACK.getCode()) {
                            fallbackRequests = totalRequests;
                            fallbackAmount = totalAmount;
                        }
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error querying payment summary", e);
            }
        }

        // monta JSON direto
        StringBuilder sb = new StringBuilder();
        sb.append("{")
            .append("\"default\":{")
            .append("\"type\":\"default\",")
            .append("\"totalRequests\":").append(defaultRequests).append(",")
            .append("\"totalAmount\":").append(defaultAmount)
            .append("},")
            .append("\"fallback\":{")
            .append("\"type\":\"fallback\",")
            .append("\"totalRequests\":").append(fallbackRequests).append(",")
            .append("\"totalAmount\":").append(fallbackAmount)
            .append("}")
            .append("}");
        return sb.toString();
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
                    ps.setInt(3, payment.paymentProcessorType.getCode());
                    ps.setBigDecimal(4, payment.amount);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir batch de pagamentos", e);
        }
    }

    public void createPayment(Payment payment) {
        String sql = """
            INSERT INTO payments (correlation_id, requested_at, payment_processor_type, amount)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, payment.correlationId);
            ps.setTimestamp(2, Timestamp.from(payment.requestedAt));
            ps.setInt(3, payment.paymentProcessorType.getCode());
            ps.setBigDecimal(4, payment.amount);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pagamento", e);
        }
    }

}