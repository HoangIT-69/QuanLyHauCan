package org.example.Panel.PN_AssurancePlanPanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class PN_AssurancePlanPanelService {

    public static final class ThongTinChungLoad {
        public final String fullTitle;
        public final Map<String, String> wordPlaceholders;

        public ThongTinChungLoad(String fullTitle, Map<String, String> wordPlaceholders) {
            this.fullTitle = fullTitle;
            this.wordPlaceholders = wordPlaceholders;
        }
    }

    public ThongTinChungLoad loadThongTinChung(int sessionId) {
        Map<String, String> thongTinChungData = new HashMap<>();
        String title = "KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT";
        String sql = "SELECT * FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String tenVK = rs.getString("ten_van_kien");
                if (tenVK != null && !tenVK.trim().isEmpty()) {
                    title = "KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT " + tenVK.toUpperCase();
                }

                thongTinChungData.put("{{nguoi_phe_chuan}}", rs.getString("chi_huy"));
                thongTinChungData.put("{{toa_do}}", "VTCH: " + rs.getString("vi_tri_chi_huy") + " " + rs.getString("thoi_gian"));
                thongTinChungData.put("{{ban_do_su_dung}}", "Bản đồ tỷ lệ " + rs.getString("ty_le") + " BTTM in năm " + rs.getString("nam"));

                thongTinChungData.put("{{map_1}}", rs.getString("map_1"));
                thongTinChungData.put("{{map_2}}", rs.getString("map_2"));
                thongTinChungData.put("{{map_3}}", rs.getString("map_3"));
                thongTinChungData.put("{{map_4}}", rs.getString("map_4"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin chung: " + e.getMessage());
        }
        return new ThongTinChungLoad(title, thongTinChungData);
    }
}
