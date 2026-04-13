package org.example.Panel.PN_PlanEstimationPanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Lấy tiêu đề hiển thị từ bước 1 (tên văn kiện) cho vùng Dự kiến kế hoạch — Phòng ngự.
 */
public class PN_PlanEstimationPanelService {

    public String fetchTenVanKien(int sessionId) {
        String sql = "SELECT ten_van_kien FROM step1_thong_tin WHERE session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                return defaultTitle();
            }

            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String fetched = rs.getString("ten_van_kien");
                    if (fetched != null && !fetched.isEmpty()) {
                        return fetched.toUpperCase().startsWith("DỰ KIẾN")
                                ? fetched.toUpperCase()
                                : "DỰ KIẾN " + fetched.toUpperCase();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultTitle();
    }

    private static String defaultTitle() {
        return "DỰ KIẾN KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT";
    }
}
