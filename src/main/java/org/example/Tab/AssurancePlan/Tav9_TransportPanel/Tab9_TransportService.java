package org.example.Tab.AssurancePlan.Tav9_TransportPanel;

import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService;
import org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService;
import org.example.Utils.DBConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Tab9_TransportService {

    // =========================================================================
    // Cột index trong modelKhoiLuong (Bảng 1)
    // =========================================================================
    /** Cột "Đạn" trong Bảng 1 (index 2). */
    private static final int COL_DAN  = 2;
    private static final int COL_VTKT = 3;
    private static final int COL_QN   = 5;
    private static final int COL_QY   = 6;
    private static final int COL_DT   = 7;

    // Chỉ số dòng cố định trong modelKhoiLuong
    private static final int ROW_TOAN_TRAN   = 1;
    private static final int ROW_GDCB        = 2;
    private static final int ROW_GDCD        = 6;

    /**
     * Lắp dữ liệu Đạn vào Bảng 1 từ {@link Tab5_DanPanelService#globalTonnageData}.
     * <ul>
     *   <li>Toàn trận  = TL_TruocNo_DV + TL_TruocNo_Kho + TL_ThucHanh</li>
     *   <li>GĐCB       = TL_TruocNo_DV + TL_TruocNo_Kho</li>
     *   <li>GĐCĐ       = TL_ThucHanh</li>
     * </ul>
     */
    public void refreshDanData(DefaultTableModel modelKhoiLuong) {
        if (modelKhoiLuong == null || modelKhoiLuong.getRowCount() <= ROW_GDCD) return;

        Map<String, Double> tonnage = Tab5_DanPanelService.getGlobalTonnageDataReadOnly();
        double dv  = tonnage.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_DV,  0.0);
        double kho = tonnage.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
        double th  = tonnage.getOrDefault(Tab5_DanPanelService.TL_THUC_HANH,    0.0);

        modelKhoiLuong.setValueAt(fmtTon(dv + kho + th), ROW_TOAN_TRAN, COL_DAN);
        modelKhoiLuong.setValueAt(fmtTon(dv + kho),      ROW_GDCB,      COL_DAN);
        modelKhoiLuong.setValueAt(fmtTon(th),             ROW_GDCD,      COL_DAN);
    }

    private static String fmtTon(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /**
     * Lắp dữ liệu Vật chất (QN, QY, DT, VTKT) vào Bảng 1 từ {@link Tab5_VatChatPanelService#globalTonnageVCHC_ByCat}.
     */
    public void refreshVCHCData(DefaultTableModel modelKhoiLuong) {
        if (modelKhoiLuong == null || modelKhoiLuong.getRowCount() <= ROW_GDCD) return;

        Map<String, Map<String, Double>> byCat = Tab5_VatChatPanelService.getGlobalTonnageVCHC_ByCatReadOnly();

        int[] cols = {COL_QN, COL_QY, COL_DT, COL_VTKT};
        String[] keys = {Tab5_VatChatPanelService.CAT_QN, Tab5_VatChatPanelService.CAT_QY,
                         Tab5_VatChatPanelService.CAT_DT, Tab5_VatChatPanelService.CAT_VTKT};

        for (int i = 0; i < cols.length; i++) {
            Map<String, Double> m = byCat.getOrDefault(keys[i], Map.of());
            double gdcb = m.getOrDefault(Tab5_VatChatPanelService.TL_GDCB,      0.0);
            double gdcd = m.getOrDefault(Tab5_VatChatPanelService.TL_GDCD,      0.0);
            double toan = m.getOrDefault(Tab5_VatChatPanelService.TL_TOAN_TRAN, 0.0);
            modelKhoiLuong.setValueAt(fmtTon(toan), ROW_TOAN_TRAN, cols[i]);
            modelKhoiLuong.setValueAt(fmtTon(gdcb), ROW_GDCB,      cols[i]);
            modelKhoiLuong.setValueAt(fmtTon(gdcd), ROW_GDCD,      cols[i]);
        }
    }

    /**
     * Gom dữ liệu xuất Word từ Tab9: đường vận tải + 2 bảng.
     */
    public Map<String, String> getExportData(String duongVanTai,
                                              DefaultTableModel modelKhoiLuong,
                                              DefaultTableModel modelKeHoach) {
        Map<String, String> data = new HashMap<>();
        data.put("<<duong_van_tai>>", duongVanTai);
        data.put("<<rows_bang_khoi_luong_van_tai>>", buildTableHtml(modelKhoiLuong));
        data.put("<<rows_bang_ke_hoach_van_tai>>", buildTableHtml(modelKeHoach));
        return data;
    }

    /**
     * Chuyển DefaultTableModel → chuỗi HTML các dòng {@code <tr>...</tr>}.
     * Các dòng tổng/tiêu đề (Toàn trận, Giai đoạn, TỔNG, Hướng, LL) được in đậm ở 2 cột đầu.
     */
    public String buildTableHtml(DefaultTableModel m) {
        if (m == null || m.getRowCount() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < m.getColumnCount(); j++) {
                Object val = m.getValueAt(i, j);
                String text = val == null ? "" : val.toString()
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");

                if (j <= 1 && (text.startsWith("Toàn trận") || text.startsWith("Giai đoạn")
                        || text.startsWith("TỔNG") || text.startsWith("Hướng") || text.startsWith("LL"))) {
                    text = "<b>" + text + "</b>";
                }

                if (j <= 1) sb.append("<td class='text-left'>").append(text).append("</td>");
                else sb.append("<td>").append(text).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }

    /**
     * Lấy danh sách các Hướng từ bảng step2_bien_che theo sessionId.
     */
    public List<String> getDanhSachHuong(int sessionId) {
        List<String> list = new ArrayList<>();
        System.out.println("[Tab9_TransportService] getDanhSachHuong() called with sessionId=" + sessionId);
        String sql = "SELECT DISTINCT TRIM(huong) AS huong FROM step2_bien_che WHERE session_id = ? AND huong IS NOT NULL ORDER BY TRIM(huong)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String h = rs.getString("huong");
                    System.out.println("[Tab9_TransportService]   -> raw huong from DB: '" + h + "'");
                    if (h != null && !h.isBlank()) {
                        list.add(h);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Tab9_TransportService] ERROR querying huong: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[Tab9_TransportService] getDanhSachHuong result count=" + list.size() + " : " + list);
        return list;
    }
}
