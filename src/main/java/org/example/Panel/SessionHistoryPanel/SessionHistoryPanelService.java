package org.example.Panel.SessionHistoryPanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lịch sử session — lọc theo {@code user_id} và {@code hinh_thuc_tap_bai}.
 */
public class SessionHistoryPanelService {

    public static final class SessionRow {
        public final int id;
        public final String tenBaiTap;
        public final String ngayTao;
        public final int trangThai;

        public SessionRow(int id, String tenBaiTap, String ngayTao, int trangThai) {
            this.id = id;
            this.tenBaiTap = tenBaiTap != null ? tenBaiTap : "";
            this.ngayTao = ngayTao != null ? ngayTao : "";
            this.trangThai = trangThai;
        }
    }

    /**
     * Chỉ lấy session của user và đúng hình thức tập bài (đồng bộ với phiên đăng nhập).
     */
    public List<SessionRow> loadSessions(int userId, String hinhThucTapBai) {
        List<SessionRow> out = new ArrayList<>();
        if (userId < 1) {
            return out;
        }
        String h = hinhThucTapBai != null ? hinhThucTapBai : "";
        String sql = "SELECT id, ten_bai_tap, ngay_tao, trang_thai FROM sessions "
                + "WHERE user_id = ? AND COALESCE(hinh_thuc_tap_bai, '') = ? "
                + "ORDER BY ngay_tao DESC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, h);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        out.add(new SessionRow(
                                rs.getInt("id"),
                                rs.getString("ten_bai_tap"),
                                rs.getString("ngay_tao"),
                                rs.getInt("trang_thai")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public boolean deleteSession(int sessionId) {
        if (sessionId < 1) {
            return false;
        }
        String sql = "DELETE FROM sessions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
