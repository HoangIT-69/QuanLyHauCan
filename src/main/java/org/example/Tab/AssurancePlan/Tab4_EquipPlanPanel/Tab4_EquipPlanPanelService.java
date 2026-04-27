package org.example.Tab.AssurancePlan.Tab4_EquipPlanPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Nạp dữ liệu Step 2 (Biên chế) vào bảng chỉ tiêu trang bị (Assurance Plan).
 */
public class Tab4_EquipPlanPanelService {

    public static final String[] GROUPS = {"dBB1+PT", "Hướng PN chủ yếu", "Hướng PN thứ yếu", "Hướng PN phía sau", "Lực lượng còn lại"};
    public static final String[] WEAPONS = {
            "SMPK 12,7mm", "Cối 100mm/e", "Cối 82mm/d", "Cối 60mm", "Súng SPG-9",
            "Súng B41", "Súng đại liên", "Súng trung liên", "Súng tiểu liên", "Súng ngắn", "Lựu đạn"
    };
    public static final String[] WEAPON_KEYS = {
            "smpk_127mm", "co100mm", "co82mm", "co60mm", "spg9",
            "b41", "dai_lien", "trung_lien", "tieu_lien", "sung_ngan", "luu_dan"
    };
    public static final String[] UNITS = {
            "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "quả"
    };

    public void loadFromDatabase(int sessionId, DefaultTableModel model) {
        if (sessionId <= 0) return;

        // Clear previous data
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 3; c < model.getColumnCount(); c++) {
                model.setValueAt("", r, c);
            }
        }

        // 1. Get weapon counts per direction from Step 2
        Map<String, Map<String, Integer>> countsMap = queryWeaponCounts(sessionId);
        List<String> sortedHuongs = new ArrayList<>(countsMap.keySet());
        Collections.sort(sortedHuongs);

        // 2. Calculate Total (dBB1+PT)
        Map<String, Integer> totalCounts = new HashMap<>();
        for (Map<String, Integer> hCounts : countsMap.values()) {
            for (String key : WEAPON_KEYS) {
                totalCounts.merge(key, hCounts.getOrDefault(key, 0), Integer::sum);
            }
        }

        // 3. Fill dBB1+PT (Group 0)
        fillGroup(model, 0, totalCounts);

        // 4. Fill directions (Group 1 to N)
        for (int i = 0; i < sortedHuongs.size() && i < 4; i++) {
            String huong = sortedHuongs.get(i);
            // Update group label if needed? No, user wants it to match the plan.
            // We just fill Group i+1.
            fillGroup(model, i + 1, countsMap.get(huong));
        }
    }

    private void fillGroup(DefaultTableModel model, int groupIdx, Map<String, Integer> counts) {
        int startRow = groupIdx * WEAPONS.length;
        for (int i = 0; i < WEAPONS.length; i++) {
            int count = counts.getOrDefault(WEAPON_KEYS[i], 0);
            if (count > 0) {
                int r = startRow + i;
                model.setValueAt(String.valueOf(count), r, 3); // Nhu cầu
                model.setValueAt(String.valueOf(count), r, 4); // Tổng số
                model.setValueAt("", r, 5);                   // Số tốt (Xóa dữ liệu để người dùng tự điền)
                model.setValueAt("1,00", r, 6);               // Kbđ
                model.setValueAt("1,00", r, 7);               // Kt
                model.setValueAt(String.valueOf(count), r, 8); // Phải có trước CĐ
                model.setValueAt("", r, 9);                   // Số lượng bổ sung (trống nếu bằng 0)
            }
        }
    }

    private Map<String, Map<String, Integer>> queryWeaponCounts(int sessionId) {
        Map<String, Map<String, Integer>> res = new HashMap<>();
        String sql = "SELECT s.huong, " +
                "SUM(q.luu_dan) as luu_dan, SUM(q.sung_ngan) as sung_ngan, SUM(q.tieu_lien) as tieu_lien, " +
                "SUM(q.trung_lien) as trung_lien, SUM(q.dai_lien) as dai_lien, SUM(q.b41) as b41, " +
                "SUM(q.co60mm) as co60mm, SUM(q.co82mm) as co82mm, SUM(q.co100mm) as co100mm, " +
                "SUM(q.spg9) as spg9, SUM(q.smpk_127mm) as smpk_127mm " +
                "FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id " +
                "WHERE s.session_id = ? GROUP BY s.huong";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String h = rs.getString("huong");
                if (h == null) continue;
                Map<String, Integer> counts = new HashMap<>();
                for (String key : WEAPON_KEYS) {
                    counts.put(key, rs.getInt(key));
                }
                res.put(h.trim(), counts);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
}
