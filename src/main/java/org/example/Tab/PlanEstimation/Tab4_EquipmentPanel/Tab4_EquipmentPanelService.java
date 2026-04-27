package org.example.Tab.PlanEstimation.Tab4_EquipmentPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;
import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Tab IV — Trang bị kỹ thuật (Bảng Chỉ tiêu trong {@code pn_plan_estimation}).
 */
public class Tab4_EquipmentPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public static class EquipTableRow {
        public String donVi = "";
        public String tenTBKT = "";
        public String dvt = "";
        public int nhuCau = 0;
        public int tongSo = 0;
        public int soTot = 0;
        public double kbd = 1.0;
        public double kt = 1.0;
        public int phaiCoTruocCD = 0;
        public int soLuongBoSung = 0;
        public String thoiGian = "";
        public String diaDiem = "";
        public String phuongThuc = "";

        public EquipTableRow() {}

        // Simple string serialization for storage
        public String serialize() {
            return String.join("|", 
                donVi, tenTBKT, dvt, String.valueOf(nhuCau), String.valueOf(tongSo), 
                String.valueOf(soTot), String.format("%.2f", kbd), String.format("%.2f", kt), 
                String.valueOf(phaiCoTruocCD), String.valueOf(soLuongBoSung), 
                thoiGian, diaDiem, phuongThuc
            );
        }

        public static EquipTableRow deserialize(String s) {
            if (s == null || s.isEmpty()) return null;
            String[] parts = s.split("\\|", -1);
            if (parts.length < 13) return null;
            EquipTableRow r = new EquipTableRow();
            r.donVi = parts[0];
            r.tenTBKT = parts[1];
            r.dvt = parts[2];
            r.nhuCau = parseInt(parts[3]);
            r.tongSo = parseInt(parts[4]);
            r.soTot = parseInt(parts[5]);
            r.kbd = parseDouble(parts[6]);
            r.kt = parseDouble(parts[7]);
            r.phaiCoTruocCD = parseInt(parts[8]);
            r.soLuongBoSung = parseInt(parts[9]);
            r.thoiGian = parts[10];
            r.diaDiem = parts[11];
            r.phuongThuc = parts[12];
            return r;
        }

        private static int parseInt(String s) {
            try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
        }
        private static double parseDouble(String s) {
            try { return Double.parseDouble(s.replace(",", ".")); } catch (Exception e) { return 0.0; }
        }
    }

    public void loadInto(Tab4Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.chiTieu = r.tab4ChiTieu;
        target.chuanBi = r.tab4ChuanBi;
        target.chienDau = r.tab4ChienDau;
    }

    public void save(int sessionId, String chiTieu, String chuanBi, String chienDau) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab4ChiTieu = chiTieu != null ? chiTieu : "";
        r.tab4ChuanBi = chuanBi != null ? chuanBi : "";
        r.tab4ChienDau = chienDau != null ? chienDau : "";
        store.save(r);
    }

    public List<EquipTableRow> fetchInitialData(int sessionId) {
        List<EquipTableRow> rows = new ArrayList<>();
        if (sessionId <= 0) return rows;

        List<String> huongs = getDanhSachHuong(sessionId);
        Map<String, Map<String, Integer>> countsMap = queryWeaponCounts(sessionId);

        String[] weapons = {"SMPK 12,7mm", "Cối 100mm", "Cối 82mm", "Cối 60mm", "Súng SPG-9", "Súng B41", "Súng đại liên", "Súng trung liên", "Súng tiểu liên", "Súng ngắn", "Lựu đạn"};
        String[] weaponKeys = {"smpk_127mm", "co100mm", "co82mm", "co60mm", "spg9", "b41", "dai_lien", "trung_lien", "tieu_lien", "sung_ngan", "luu_dan"};
        String[] units = {"Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "quả"};

        // Add dBB1+PT row
        Map<String, Integer> totalCounts = new HashMap<>();
        for (Map<String, Integer> hCounts : countsMap.values()) {
            for (String key : weaponKeys) {
                totalCounts.merge(key, hCounts.getOrDefault(key, 0), Integer::sum);
            }
        }
        addDirectionRows(rows, "dBB1+PT", totalCounts, weapons, weaponKeys, units);

        // Add other directions
        for (String h : huongs) {
            addDirectionRows(rows, h, countsMap.getOrDefault(h, new HashMap<>()), weapons, weaponKeys, units);
        }

        return rows;
    }

    private void addDirectionRows(List<EquipTableRow> rows, String direction, Map<String, Integer> counts, String[] weapons, String[] keys, String[] units) {
        for (int i = 0; i < weapons.length; i++) {
            int count = counts.getOrDefault(keys[i], 0);
            if (count > 0) {
                EquipTableRow r = new EquipTableRow();
                r.donVi = direction;
                r.tenTBKT = weapons[i];
                r.dvt = units[i];
                r.nhuCau = count;
                r.tongSo = count;
                r.soTot = count; // Default to all good
                r.kbd = 1.0;
                r.kt = 1.0;
                r.phaiCoTruocCD = count;
                r.soLuongBoSung = 0;
                rows.add(r);
            }
        }
    }

    private List<String> getDanhSachHuong(int sessionId) {
        List<String> huongs = new ArrayList<>();
        String sql = "SELECT DISTINCT huong FROM step2_bien_che WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String h = rs.getString("huong");
                if (h != null) huongs.add(h.trim());
            }
        } catch (Exception ignored) {}
        return huongs;
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
                counts.put("luu_dan", rs.getInt("luu_dan"));
                counts.put("sung_ngan", rs.getInt("sung_ngan"));
                counts.put("tieu_lien", rs.getInt("tieu_lien"));
                counts.put("trung_lien", rs.getInt("trung_lien"));
                counts.put("dai_lien", rs.getInt("dai_lien"));
                counts.put("b41", rs.getInt("b41"));
                counts.put("co60mm", rs.getInt("co60mm"));
                counts.put("co82mm", rs.getInt("co82mm"));
                counts.put("co100mm", rs.getInt("co100mm"));
                counts.put("spg9", rs.getInt("spg9"));
                counts.put("smpk_127mm", rs.getInt("smpk_127mm"));
                res.put(h.trim(), counts);
            }
        } catch (Exception ignored) {}
        return res;
    }

    public String generateSummaryText(int sessionId) {
        Map<String, Map<String, Integer>> countsMap = queryWeaponCounts(sessionId);
        int totalWeapons = 0;
        for (Map<String, Integer> hCounts : countsMap.values()) {
            for (int val : hCounts.values()) {
                totalWeapons += val;
            }
        }
        if (totalWeapons == 0) return "";

        Map<String, Double> damageRates = new HashMap<>();
        String sql = "SELECT loai_vktb, ti_le_hu_hong FROM step4_hu_hong_vktb WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                damageRates.put(rs.getString("loai_vktb").toLowerCase().trim(), rs.getDouble("ti_le_hu_hong"));
            }
        } catch (Exception ignored) {}

        StringBuilder sb = new StringBuilder();
        sb.append("Số vũ khí: Quy định ").append(totalWeapons).append(" khẩu; ");
        sb.append("hiện có ").append(totalWeapons).append(" khẩu; ");

        int totalDamaged = 0;
        List<String> damageDetails = new ArrayList<>();

        String[] weapons = {"SMPK 12,7mm", "Cối 100mm", "Cối 82mm", "Cối 60mm", "Súng SPG-9", "Súng B41", "Súng đại liên", "Súng trung liên", "Súng tiểu liên", "Súng ngắn"};
        String[] weaponKeys = {"smpk_127mm", "co100mm", "co82mm", "co60mm", "spg9", "b41", "dai_lien", "trung_lien", "tieu_lien", "sung_ngan"};

        Map<String, Integer> totalPerWeapon = new HashMap<>();
        for (Map<String, Integer> hCounts : countsMap.values()) {
            for (int i = 0; i < weaponKeys.length; i++) {
                totalPerWeapon.merge(weaponKeys[i], hCounts.getOrDefault(weaponKeys[i], 0), Integer::sum);
            }
        }

        for (int i = 0; i < weapons.length; i++) {
            int qty = totalPerWeapon.getOrDefault(weaponKeys[i], 0);
            if (qty <= 0) continue;

            double rate = 0;
            String wName = weapons[i].toLowerCase();
            for (String k : damageRates.keySet()) {
                if (wName.contains(k) || k.contains(wName)) {
                    rate = damageRates.get(k);
                    break;
                }
            }

            if (rate > 0) {
                int damagedQty = (int) Math.round(qty * rate / 100.0);
                if (damagedQty > 0) {
                    totalDamaged += damagedQty;
                    int nhe = (int) Math.round(damagedQty * 0.50);
                    int vua = (int) Math.round(damagedQty * 0.25);
                    int nang = (int) Math.round(damagedQty * 0.15);
                    int huy = damagedQty - nhe - vua - nang;

                    if (nhe > 0) damageDetails.add("hỏng nhẹ " + String.format("%02d", nhe) + " khẩu " + weapons[i]);
                    if (vua > 0) damageDetails.add("hỏng vừa " + String.format("%02d", vua) + " khẩu " + weapons[i]);
                    if (nang > 0) damageDetails.add("hỏng nặng " + String.format("%02d", nang) + " khẩu " + weapons[i]);
                    if (huy > 0) damageDetails.add("hủy " + String.format("%02d", huy) + " khẩu " + weapons[i]);
                }
            }
        }

        sb.append("trong đó, số tốt ").append(totalWeapons - totalDamaged).append(" khẩu");
        if (!damageDetails.isEmpty()) {
            sb.append(", ").append(String.join(", ", damageDetails));
        }
        sb.append(".");

        return sb.toString();
    }

    public static final class Tab4Fields {
        public String chiTieu = "";
        public String chuanBi = "";
        public String chienDau = "";
    }
}
