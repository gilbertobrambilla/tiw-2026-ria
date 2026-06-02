package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WPDAO {
    private final Connection connection;

    public WPDAO(Connection connection) {
        this.connection = connection;
    }

    public int createWP(int projectId, String title, int startMonth, int endMonth) throws SQLException {
        String sequenceQuery = "SELECT COALESCE(MAX(sequence_number), 0) + 1 AS next_seq FROM wp WHERE project_id = ?";
        String insertQuery = "INSERT INTO wp (project_id, sequence_number, title, start_month, end_month) VALUES (?, ?, ?, ?, ?)";

        int nextSeq = 1;

        // sequence number calculation executed within the same connection scope
        try (PreparedStatement seqStmt = connection.prepareStatement(sequenceQuery)) {
            seqStmt.setInt(1, projectId);
            try (ResultSet rs = seqStmt.executeQuery()) {
                if (rs.next()) {
                    nextSeq = rs.getInt("next_seq");
                }
            }
        }

        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, projectId);
            insertStmt.setInt(2, nextSeq);
            insertStmt.setString(3, title);
            insertStmt.setInt(4, startMonth);
            insertStmt.setInt(5, endMonth);
            insertStmt.executeUpdate();

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("WP creation failed, no generated id returned");
    }
}
