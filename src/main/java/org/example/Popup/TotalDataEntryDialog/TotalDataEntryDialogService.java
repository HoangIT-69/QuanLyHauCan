package org.example.Popup.TotalDataEntryDialog;

import org.example.Popup.UnitDataEntryDialog.UnitDataEntryDialogService;
import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TotalDataEntryDialogService {

    /**
     * @param filterHuong null hoặc rỗng = tất cả hướng; ngược lại chỉ nhóm có {@code huong} khớp
     */
    public AggregatedLoadResult loadAggregated(int sessionId, String filterGroup, String filterHuong) {
        Map<String, List<Object[]>> groupedData = new LinkedHashMap<>();

        String sql = "SELECT s.huong, q.* FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ? ORDER BY s.huong";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new AggregatedLoadResult(groupedData);
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String huong = rs.getString("huong");
                        if (filterHuong != null && !filterHuong.isBlank() && !filterHuong.equals(huong)) {
                            continue;
                        }
                        String nhom = rs.getString("nhom_don_vi");

                        if (nhom.equalsIgnoreCase(filterGroup)
                                || (filterGroup.equals("Tiểu đoàn") && nhom.contains("Tiểu đoàn"))) {
                            Object[] row = new Object[15];
                            row[0] = "[" + nhom + "] " + rs.getString("ten_don_vi");
                            row[1] = rs.getInt("quan_so");
                            row[2] = rs.getInt("sung_ngan");
                            row[3] = rs.getInt("tieu_lien");
                            row[4] = rs.getInt("trung_lien");
                            row[5] = rs.getInt("dai_lien");
                            row[6] = rs.getInt("b41");
                            row[7] = rs.getInt("luu_dan");
                            row[8] = rs.getInt("co60mm");
                            row[9] = rs.getInt("co82mm");
                            row[10] = rs.getInt("co100mm");
                            row[11] = rs.getInt("spg9");
                            row[12] = rs.getInt("smpk_127mm");
                            row[13] = 0;
                            row[14] = 0;

                            groupedData.computeIfAbsent(huong, k -> new ArrayList<>()).add(row);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        overlayRamStore(groupedData, filterGroup, filterHuong);
        return new AggregatedLoadResult(groupedData);
    }

    /**
     * Khi biên chế chỉ nằm trong RAM (chưa lưu bước 4), vẫn tổng hợp được theo cùng logic lọc nhóm như truy vấn DB.
     * Nếu một hướng có dòng đơn vị trong RAM thì thay thế hoàn toàn nhóm đó (ghi đè phần đã đọc từ DB).
     */
    private void overlayRamStore(Map<String, List<Object[]>> groupedData, String filterGroup, String filterHuong) {
        Map<String, Vector<Vector<Object>>> store = UnitDataEntryDialogService.getSharedStore();
        if (store == null || store.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Vector<Vector<Object>>> e : store.entrySet()) {
            String huong = e.getKey();
            if ("Tiểu đoàn".equals(huong) || "Phối thuộc".equals(huong)) {
                continue;
            }
            if (filterHuong != null && !filterHuong.isBlank() && !filterHuong.equals(huong)) {
                continue;
            }
            List<Object[]> fromRam = extractRowsForFilter(e.getValue(), filterGroup);
            if (!fromRam.isEmpty()) {
                groupedData.put(huong, fromRam);
            }
        }
    }

    private static boolean nhomMatchesFilter(String nhom, String filterGroup) {
        if (nhom == null || filterGroup == null) {
            return false;
        }
        return nhom.equalsIgnoreCase(filterGroup)
                || ("Tiểu đoàn".equals(filterGroup) && nhom.contains("Tiểu đoàn"));
    }

    private List<Object[]> extractRowsForFilter(Vector<Vector<Object>> rows, String filterGroup) {
        List<Object[]> out = new ArrayList<>();
        if (rows == null || filterGroup == null) {
            return out;
        }
        for (Vector<Object> row : rows) {
            if (row.isEmpty() || row.get(0) == null) {
                continue;
            }
            String raw = row.get(0).toString().replace("  + ", "").trim();
            if (raw.startsWith("1.") || raw.startsWith("2.") || raw.equals("TỔNG CỘNG")) {
                continue;
            }
            if (!raw.startsWith("[")) {
                continue;
            }
            int close = raw.indexOf(']');
            if (close < 1) {
                continue;
            }
            String nhom = raw.substring(1, close).trim();
            if (!nhomMatchesFilter(nhom, filterGroup)) {
                continue;
            }
            Object[] o = new Object[15];
            o[0] = raw;
            for (int i = 1; i < 15; i++) {
                o[i] = i < row.size() ? row.get(i) : 0;
            }
            out.add(o);
        }
        return out;
    }

    public static final class AggregatedLoadResult {
        private final Map<String, List<Object[]>> groupedData;

        public AggregatedLoadResult(Map<String, List<Object[]>> groupedData) {
            this.groupedData = groupedData;
        }

        public Map<String, List<Object[]>> getGroupedData() {
            return groupedData;
        }

        public boolean isEmpty() {
            return groupedData.isEmpty();
        }
    }
}
