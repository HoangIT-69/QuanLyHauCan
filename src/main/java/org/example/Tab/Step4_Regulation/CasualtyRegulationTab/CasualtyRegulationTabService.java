package org.example.Tab.Step4_Regulation.CasualtyRegulationTab;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CasualtyRegulationTabService {

    public static final class Row {
        public final String loaiThuongBinh;
        public final double tiLe;

        public Row(String loaiThuongBinh, double tiLe) {
            this.loaiThuongBinh = loaiThuongBinh != null ? loaiThuongBinh : "";
            this.tiLe = tiLe;
        }
    }

    public List<Row> loadThuongBinh(int sessionId) throws Exception {
        List<Row> out = new ArrayList<>();
        if (sessionId < 0) {
            return out;
        }
        String sql = "SELECT * FROM step4_ti_le_thuong_binh WHERE session_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        out.add(new Row(rs.getString("loai_thuong_binh"), rs.getDouble("ti_le")));
                    }
                }
            }
        }
        return out;
    }

    public boolean saveThuongBinh(int sessionId, List<Row> rows) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM step4_ti_le_thuong_binh WHERE session_id = ?")) {
                delStmt.setInt(1, sessionId);
                delStmt.executeUpdate();
            }

            String sql = "INSERT INTO step4_ti_le_thuong_binh (session_id, loai_thuong_binh, ti_le) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Row r : rows) {
                    pstmt.setInt(1, sessionId);
                    pstmt.setString(2, r.loaiThuongBinh);
                    pstmt.setDouble(3, r.tiLe);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
