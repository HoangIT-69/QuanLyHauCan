package org.example.Tab.PlanEstimation.Tab8_MaintenancePanel;

import org.example.Popup.UnitDataEntryDialog.UnitDataEntryDialogService;
import org.example.Panel.Step4_RegulationPanel.Step4RegulationRamStore;
import org.example.Tab.Step4_Regulation.DamageRegulationTab.DamageRegulationTabService;
import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Tab8_MaintenancePanelService {

    /**
     * Cộng dồn số lượng trang bị từ RAM biên chế Step 2 (khi chưa có sessionId hợp lệ).
     */
    public Map<String, Integer> fetchWeaponSumsFromSharedStep2Store() {
        Map<String, Integer> sums = initEmptySums();
        Map<String, Vector<Vector<Object>>> store = UnitDataEntryDialogService.getSharedStore();
        if (store == null) return sums;
        for (Vector<Vector<Object>> rows : store.values()) {
            if (rows == null) continue;
            for (Vector<Object> row : rows) {
                if (row == null || row.isEmpty() || row.get(0) == null) continue;
                String raw = row.get(0).toString().replace("  + ", "").trim();
                if (raw.startsWith("1.") || raw.startsWith("2.") || raw.equals("TỔNG CỘNG")) continue;
                if (!raw.startsWith("[")) continue;
                if (row.size() < 13) continue;
                sums.merge("sung_ngan",   InputValidator.parseIntSafe(row.get(2)),  Integer::sum);
                sums.merge("tieu_lien",   InputValidator.parseIntSafe(row.get(3)),  Integer::sum);
                sums.merge("trung_lien",  InputValidator.parseIntSafe(row.get(4)),  Integer::sum);
                sums.merge("dai_lien",    InputValidator.parseIntSafe(row.get(5)),  Integer::sum);
                sums.merge("b41",         InputValidator.parseIntSafe(row.get(6)),  Integer::sum);
                sums.merge("co60mm",      InputValidator.parseIntSafe(row.get(8)),  Integer::sum);
                sums.merge("co82mm",      InputValidator.parseIntSafe(row.get(9)),  Integer::sum);
                sums.merge("co100mm",     InputValidator.parseIntSafe(row.get(10)), Integer::sum);
                sums.merge("spg9",        InputValidator.parseIntSafe(row.get(11)), Integer::sum);
                sums.merge("smpk_127mm",  InputValidator.parseIntSafe(row.get(12)), Integer::sum);
            }
        }
        return sums;
    }

    /**
     * Lấy tổng trang bị từ DB theo session_id (join quyuoc_bienche + step2_bien_che).
     */
    public Map<String, Integer> fetchWeaponSums(int sessionId) {
        Map<String, Integer> sums = initEmptySums();
        if (sessionId <= 0) return sums;
        String sql = "SELECT " +
                "SUM(q.sung_ngan) AS sung_ngan, SUM(q.tieu_lien) AS tieu_lien, " +
                "SUM(q.trung_lien) AS trung_lien, SUM(q.dai_lien) AS dai_lien, " +
                "SUM(q.b41) AS b41, SUM(q.co60mm) AS co60mm, " +
                "SUM(q.co82mm) AS co82mm, SUM(q.co100mm) AS co100mm, " +
                "SUM(q.spg9) AS spg9, SUM(q.smpk_127mm) AS smpk_127mm " +
                "FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return sums;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sums.put("sung_ngan",  rs.getInt("sung_ngan"));
                        sums.put("tieu_lien",  rs.getInt("tieu_lien"));
                        sums.put("trung_lien", rs.getInt("trung_lien"));
                        sums.put("dai_lien",   rs.getInt("dai_lien"));
                        sums.put("b41",        rs.getInt("b41"));
                        sums.put("co60mm",     rs.getInt("co60mm"));
                        sums.put("co82mm",     rs.getInt("co82mm"));
                        sums.put("co100mm",    rs.getInt("co100mm"));
                        sums.put("spg9",       rs.getInt("spg9"));
                        sums.put("smpk_127mm", rs.getInt("smpk_127mm"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sums;
    }

    private Map<String, Integer> initEmptySums() {
        Map<String, Integer> m = new HashMap<>();
        m.put("sung_ngan", 0); m.put("tieu_lien", 0); m.put("trung_lien", 0);
        m.put("dai_lien", 0);  m.put("b41", 0);        m.put("co60mm", 0);
        m.put("co82mm", 0);    m.put("co100mm", 0);    m.put("spg9", 0);
        m.put("smpk_127mm", 0);
        return m;
    }

    /**
     * Lấy tỉ lệ hư hỏng đã khai báo từ DB (bảng step4_hu_hong_vktb).
     */
    public Map<String, Double> fetchSavedRates(int sessionId) {
        Map<String, Double> rates = new HashMap<>();
        if (sessionId <= 0) {
            // Lấy từ RAM draft của Step4RegulationRamStore
            List<DamageRegulationTabService.SaveRow> draft = Step4RegulationRamStore.getDamageDraft();
            if (draft != null) {
                for (DamageRegulationTabService.SaveRow r : draft) {
                    if (r.loaiVktb != null) {
                        rates.put(r.loaiVktb.trim().toLowerCase(), r.tiLeHuHong);
                    }
                }
            }
            return rates;
        }
        String sql = "SELECT loai_vktb, ti_le_hu_hong FROM step4_hu_hong_vktb WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return rates;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
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
}
