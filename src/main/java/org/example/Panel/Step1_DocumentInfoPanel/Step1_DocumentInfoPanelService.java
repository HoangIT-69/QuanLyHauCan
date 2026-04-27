package org.example.Panel.Step1_DocumentInfoPanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Step1_DocumentInfoPanelService {

    public static final class Step1Record {
        public final String tenVanKien;
        public final String viTriChiHuy;
        public final String thoiGian;
        public final String map1;
        public final String map2;
        public final String map3;
        public final String map4;
        public final String tyLe;
        public final int nam;
        public final String chiHuy;
        public final String nguoiThayThe;

        public Step1Record(String tenVanKien, String viTriChiHuy, String thoiGian,
                           String map1, String map2, String map3, String map4,
                           String tyLe, int nam, String chiHuy, String nguoiThayThe) {
            this.tenVanKien = tenVanKien;
            this.viTriChiHuy = viTriChiHuy;
            this.thoiGian = thoiGian;
            this.map1 = map1;
            this.map2 = map2;
            this.map3 = map3;
            this.map4 = map4;
            this.tyLe = tyLe;
            this.nam = nam;
            this.chiHuy = chiHuy;
            this.nguoiThayThe = nguoiThayThe;
        }
    }

    public static final class LoadResult {
        private final boolean success;
        private final Step1Record data;
        private final String errorMessage;

        private LoadResult(boolean success, Step1Record data, String errorMessage) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        public static LoadResult ok(Step1Record data) {
            return new LoadResult(true, data, null);
        }

        public static LoadResult fail(String message) {
            return new LoadResult(false, null, message);
        }

        public static LoadResult skip() {
            return new LoadResult(true, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public Step1Record getData() {
            return data;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static final class SaveResult {
        private final boolean success;
        private final String errorMessage;
        private final Integer newSessionId;

        private SaveResult(boolean success, String errorMessage, Integer newSessionId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.newSessionId = newSessionId;
        }

        public static SaveResult ok(Integer newSessionId) {
            return new SaveResult(true, null, newSessionId);
        }

        public static SaveResult okNoNewSession() {
            return new SaveResult(true, null, null);
        }

        public static SaveResult fail(String message) {
            return new SaveResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Integer getNewSessionId() {
            return newSessionId;
        }
    }

    public LoadResult loadBySessionId(int sessionId) {
        if (sessionId < 0) {
            return LoadResult.skip();
        }
        String sql = "SELECT * FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return LoadResult.fail("Không kết nối được CSDL.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Step1Record rec = new Step1Record(
                                rs.getString("ten_van_kien"),
                                rs.getString("vi_tri_chi_huy"),
                                rs.getString("thoi_gian"),
                                rs.getString("map_1"),
                                rs.getString("map_2"),
                                rs.getString("map_3"),
                                rs.getString("map_4"),
                                rs.getString("ty_le"),
                                rs.getInt("nam"),
                                rs.getString("chi_huy"),
                                rs.getString("nguoi_thay_the")
                        );
                        return LoadResult.ok(rec);
                    }
                    return LoadResult.ok(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return LoadResult.fail(e.getMessage());
        }
    }

    public SaveResult save(int userId, int sessionIdOrMinusOne, String tenVanKien, String viTriChiHuy,
                           String thoiGian, String[] map4, String tyLe, int nam, String chiHuy, String nguoiThayThe,
                           String hinhThucTapBai) {
        if (map4 == null || map4.length < 4) {
            return SaveResult.fail("Dữ liệu mảnh bản đồ không hợp lệ.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return SaveResult.fail("Không kết nối được CSDL.");
            }

            int sessionId = sessionIdOrMinusOne;
            Integer newId = null;

            if (sessionIdOrMinusOne < 0) {
                String sqlSession = "INSERT INTO sessions (user_id, ten_bai_tap, trang_thai, hinh_thuc_tap_bai, ngay_tao) VALUES (?, ?, 0, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement pstmtSession = conn.prepareStatement(sqlSession, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtSession.setInt(1, userId);
                    pstmtSession.setString(2, tenVanKien);
                    pstmtSession.setString(3, hinhThucTapBai != null ? hinhThucTapBai : "");
                    pstmtSession.executeUpdate();
                    try (ResultSet rs = pstmtSession.getGeneratedKeys()) {
                        if (rs.next()) {
                            sessionId = rs.getInt(1);
                            newId = sessionId;
                        }
                    }
                }
            } else {
                String sqlUpdateSession = "UPDATE sessions SET ten_bai_tap = ?, hinh_thuc_tap_bai = ? WHERE id = ?";
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateSession)) {
                    pstmtUpdate.setString(1, tenVanKien);
                    pstmtUpdate.setString(2, hinhThucTapBai != null ? hinhThucTapBai : "");
                    pstmtUpdate.setInt(3, sessionIdOrMinusOne);
                    pstmtUpdate.executeUpdate();
                }
            }

            String sqlUpdate = "UPDATE step1_thong_tin SET " +
                    "ten_van_kien=?, vi_tri_chi_huy=?, thoi_gian=?, " +
                    "map_1=?, map_2=?, map_3=?, map_4=?, " +
                    "ty_le=?, nam=?, chi_huy=?, nguoi_thay_the=? " +
                    "WHERE session_id=?";

            int updated;
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                pstmtUpdate.setString(1, tenVanKien);
                pstmtUpdate.setString(2, viTriChiHuy);
                pstmtUpdate.setString(3, thoiGian);
                pstmtUpdate.setString(4, map4[0]);
                pstmtUpdate.setString(5, map4[1]);
                pstmtUpdate.setString(6, map4[2]);
                pstmtUpdate.setString(7, map4[3]);
                pstmtUpdate.setString(8, tyLe);
                pstmtUpdate.setInt(9, nam);
                pstmtUpdate.setString(10, chiHuy);
                pstmtUpdate.setString(11, nguoiThayThe);
                pstmtUpdate.setInt(12, sessionId);
                updated = pstmtUpdate.executeUpdate();
            }

            if (updated == 0) {
                String sqlInsert = "INSERT INTO step1_thong_tin (session_id, ten_van_kien, vi_tri_chi_huy, thoi_gian, map_1, map_2, map_3, map_4, ty_le, nam, chi_huy, nguoi_thay_the) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                    pstmtInsert.setInt(1, sessionId);
                    pstmtInsert.setString(2, tenVanKien);
                    pstmtInsert.setString(3, viTriChiHuy);
                    pstmtInsert.setString(4, thoiGian);
                    pstmtInsert.setString(5, map4[0]);
                    pstmtInsert.setString(6, map4[1]);
                    pstmtInsert.setString(7, map4[2]);
                    pstmtInsert.setString(8, map4[3]);
                    pstmtInsert.setString(9, tyLe);
                    pstmtInsert.setInt(10, nam);
                    pstmtInsert.setString(11, chiHuy);
                    pstmtInsert.setString(12, nguoiThayThe);
                    pstmtInsert.executeUpdate();
                }
            }

            return newId != null ? SaveResult.ok(newId) : SaveResult.okNoNewSession();
        } catch (Exception e) {
            e.printStackTrace();
            return SaveResult.fail(e.getMessage());
        }
    }
}
