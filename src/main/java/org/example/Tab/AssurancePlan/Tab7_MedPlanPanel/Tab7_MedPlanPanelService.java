package org.example.Tab.AssurancePlan.Tab7_MedPlanPanel;

import org.example.Utils.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tab7_MedPlanPanelService {

    /**
     * Thứ tự hiển thị các hướng Phòng ngự chuẩn (chỉ các giá trị này được lấy từ DB).
     */
    public static final List<String> HUONG_PHONG_NGU_CHUAN = List.of(
            "Hướng chủ yếu",
            "Hướng thứ yếu",
            "Lực lượng chiến đấu vòng ngoài",
            "Lực lượng dự bị cơ động",
            "Lực lượng còn lại"
    );

    /**
     * Danh sách {@code huong} DISTINCT trong {@code step2_bien_che} cho phiên, chỉ thuộc danh sách Phòng ngự chuẩn.
     */
    public List<String> getDanhSachHuongSCD(int sessionId) {
        List<String> ordered = new ArrayList<>();
        if (sessionId <= 0) {
            return ordered;
        }
        Set<String> found = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT huong FROM step2_bien_che WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String h = rs.getString("huong");
                if (h == null) continue;
                h = h.trim();
                if (HUONG_PHONG_NGU_CHUAN.contains(h)) {
                    found.add(h);
                }
            }
        } catch (Exception ignored) {
        }
        for (String canonical : HUONG_PHONG_NGU_CHUAN) {
            if (found.contains(canonical)) {
                ordered.add(canonical);
            }
        }
        return ordered;
    }

    public void loadFromDatabase(int sessionId, DefaultTableModel model) {
        List<String> huongs = getDanhSachHuongSCD(sessionId);

        Map<String, Integer> quanSoMap = new HashMap<>();
        String sqlQS = "SELECT s.huong, SUM(q.quan_so) as total_qs FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ? GROUP BY s.huong";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQS)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString("huong");
                if (key != null) {
                    quanSoMap.put(key.trim(), rs.getInt("total_qs"));
                }
            }
        } catch (Exception ignored) {
        }

        int toanD = 0;
        for (String h : huongs) {
            toanD += quanSoMap.getOrDefault(h, 0);
        }

        Map<String, Double> tiLeMap = new HashMap<>();
        String sqlTiLe = "SELECT loai_thuong_binh, ti_le FROM step4_ti_le_thuong_binh WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlTiLe)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("loai_thuong_binh");
                if (name == null) continue;
                name = name.trim().toLowerCase();
                tiLeMap.put(name, rs.getDouble("ti_le"));
            }
        } catch (Exception ignored) {
        }

        double bbToanTran = getTiLeByKeyword(tiLeMap, "bệnh binh toàn trận");

        int lastRow = 1 + huongs.size();
        if (model.getRowCount() != lastRow + 1) {
            return;
        }

        updateRowData(model, 0, toanD, getTiLeByKeyword(tiLeMap, "thương binh toàn trận"), bbToanTran);

        for (int i = 0; i < huongs.size(); i++) {
            String h = huongs.get(i);
            int qs = quanSoMap.getOrDefault(h, 0);
            double tyLeTb = tyLeThuongBinhChoHuong(tiLeMap, h);
            updateRowData(model, 1 + i, qs, tyLeTb, 0);
        }

        updateRowData(model, lastRow, toanD, getTiLeByKeyword(tiLeMap, "ngày cao nhất"), 0);
    }

    private double tyLeThuongBinhChoHuong(Map<String, Double> tiLeMap, String huong) {
        if (huong == null) return 0;
        if ("Hướng chủ yếu".equals(huong)) return getTiLeByKeyword(tiLeMap, "chủ yếu");
        if ("Hướng thứ yếu".equals(huong)) return getTiLeByKeyword(tiLeMap, "thứ yếu");
        if ("Lực lượng chiến đấu vòng ngoài".equals(huong)) {
            double v = getTiLeByKeyword(tiLeMap, "vòng ngoài");
            if (v > 0) return v;
            return getTiLeByKeyword(tiLeMap, "phía sau");
        }
        if ("Lực lượng dự bị cơ động".equals(huong)) {
            double v = getTiLeByKeyword(tiLeMap, "dự bị");
            if (v > 0) return v;
            return getTiLeByKeyword(tiLeMap, "cơ động");
        }
        if ("Lực lượng còn lại".equals(huong)) return getTiLeByKeyword(tiLeMap, "còn lại");
        return 0;
    }

    private double getTiLeByKeyword(Map<String, Double> map, String keyword) {
        String k = keyword.toLowerCase();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (entry.getKey().contains(k)) return entry.getValue();
        }
        return 0.0;
    }

    private void updateRowData(DefaultTableModel model, int row, int quanSo, double tyLeTB, double tyLeBB) {
        model.setValueAt(quanSo, row, 1);
        if (tyLeTB > 0) model.setValueAt(f(tyLeTB), row, 2);
        if (tyLeBB > 0) model.setValueAt(f(tyLeBB), row, 4);
    }

    private String f(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value).replace(".", ",");
    }
}
