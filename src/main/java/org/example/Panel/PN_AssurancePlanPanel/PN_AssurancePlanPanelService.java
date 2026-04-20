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

                thongTinChungData.put("{{ten_ke_hoach}}", tenVK);
                thongTinChungData.put("{{vi_tri_thoi_gian}}", rs.getString("vi_tri_chi_huy") + " " + rs.getString("thoi_gian"));
                
                thongTinChungData.put("{{bando_tyle}}", rs.getString("ty_le"));
                thongTinChungData.put("{{nam}}", String.valueOf(rs.getInt("nam")));

                thongTinChungData.put("{{bando_1}}", rs.getString("map_1"));
                thongTinChungData.put("{{bando_2}}", rs.getString("map_2"));
                thongTinChungData.put("{{bando_3}}", rs.getString("map_3"));
                thongTinChungData.put("{{bando_4}}", rs.getString("map_4"));

                // Các biến thể khác để đề phòng
                thongTinChungData.put("{{bando1}}", rs.getString("map_1"));
                thongTinChungData.put("{{bando2}}", rs.getString("map_2"));
                thongTinChungData.put("{{bando3}}", rs.getString("map_3"));
                thongTinChungData.put("{{bando4}}", rs.getString("map_4"));

                thongTinChungData.put("{{BANDO_1}}", rs.getString("map_1"));
                thongTinChungData.put("{{BANDO_2}}", rs.getString("map_2"));
                thongTinChungData.put("{{BANDO_3}}", rs.getString("map_3"));
                thongTinChungData.put("{{BANDO_4}}", rs.getString("map_4"));

                thongTinChungData.put("{{map_1}}", rs.getString("map_1"));
                thongTinChungData.put("{{map_2}}", rs.getString("map_2"));
                thongTinChungData.put("{{map_3}}", rs.getString("map_3"));
                thongTinChungData.put("{{map_4}}", rs.getString("map_4"));

                // Biến thể có khoảng trắng {{ bando_1 }}
                thongTinChungData.put("{{ bando_1 }}", rs.getString("map_1"));
                thongTinChungData.put("{{ bando_2 }}", rs.getString("map_2"));
                thongTinChungData.put("{{ bando_3 }}", rs.getString("map_3"));
                thongTinChungData.put("{{ bando_4 }}", rs.getString("map_4"));
                
                // Các key cũ/phụ trợ
                thongTinChungData.put("{{toa_do}}", "VTCH: " + rs.getString("vi_tri_chi_huy") + " " + rs.getString("thoi_gian"));
                thongTinChungData.put("{{nguoi_lap}}", rs.getString("chi_huy"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin chung: " + e.getMessage());
        }
        return new ThongTinChungLoad(title, thongTinChungData);
    }
}
