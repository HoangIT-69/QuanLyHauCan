package org.example.Tab.Step4_Regulation.DamageRegulationTab;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageRegulationTabService {

    public Map<String, Integer> fetchWeaponSums(int sessionId) {
        Map<String, Integer> sums = new HashMap<>();
        if (sessionId <= 0) {
            return sums;
        }

        String sql = "SELECT " +
                "SUM(q.sung_ngan) as sung_ngan, SUM(q.tieu_lien) as tieu_lien, " +
                "SUM(q.trung_lien) as trung_lien, SUM(q.dai_lien) as dai_lien, " +
                "SUM(q.b41) as b41, SUM(q.co60mm) as co60mm, " +
                "SUM(q.co82mm) as co82mm, SUM(q.co100mm) as co100mm, " +
                "SUM(q.spg9) as spg9, SUM(q.smpk_127mm) as smpk_127mm " +
                "FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return sums;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        sums.put("sung_ngan", rs.getInt("sung_ngan"));
                        sums.put("tieu_lien", rs.getInt("tieu_lien"));
                        sums.put("trung_lien", rs.getInt("trung_lien"));
                        sums.put("dai_lien", rs.getInt("dai_lien"));
                        sums.put("b41", rs.getInt("b41"));
                        sums.put("co60mm", rs.getInt("co60mm"));
                        sums.put("co82mm", rs.getInt("co82mm"));
                        sums.put("co100mm", rs.getInt("co100mm"));
                        sums.put("spg9", rs.getInt("spg9"));
                        sums.put("smpk_127mm", rs.getInt("smpk_127mm"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sums;
    }

    public Map<String, Double> fetchSavedRates(int sessionId) {
        Map<String, Double> rates = new HashMap<>();
        if (sessionId <= 0) {
            return rates;
        }
        String sql = "SELECT loai_vktb, ti_le_hu_hong FROM step4_hu_hong_vktb WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return rates;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String key = rs.getString("loai_vktb");
                        if (key != null) {
                            rates.put(key.trim().toLowerCase(), rs.getDouble("ti_le_hu_hong"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rates;
    }

    public static final class SaveRow {
        public final String loaiVktb;
        public final int soLuongThamGia;
        public final double tiLeHuHong;

        public SaveRow(String loaiVktb, int soLuongThamGia, double tiLeHuHong) {
            this.loaiVktb = loaiVktb;
            this.soLuongThamGia = soLuongThamGia;
            this.tiLeHuHong = tiLeHuHong;
        }
    }

    public boolean saveHuHongVktbBatch(int sessionId, List<SaveRow> rows) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM step4_hu_hong_vktb WHERE session_id = ?")) {
                    del.setInt(1, sessionId);
                    del.executeUpdate();
                }
                String sql = "INSERT INTO step4_hu_hong_vktb (session_id, loai_vktb, so_luong_tham_gia, ti_le_hu_hong) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (SaveRow r : rows) {
                        pstmt.setInt(1, sessionId);
                        pstmt.setString(2, r.loaiVktb);
                        pstmt.setInt(3, r.soLuongThamGia);
                        pstmt.setDouble(4, r.tiLeHuHong);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                return false;
            }
        } catch (Exception e) {
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
