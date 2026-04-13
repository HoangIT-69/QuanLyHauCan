package org.example.Popup.Tab5_VatChatPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * JDBC và tính toán bảng vật chất (Tab 5 popup). {@code type}: 1 = VCHC, 2 = VTKT.
 */
public class Tab5_VatChatPanelService {

    // =========================================================
    // New (Hậu cần) read-only 5-column service per requirement
    // =========================================================

    /** 0: Tên, 1: ĐVT, 2: TL ĐVT Toàn d, 3: TL ĐVT d, 4: TL ĐVT PT */
    public static final int HC_COL_COUNT = 5;

    private static final String[] HC_COLS = {"Tên", "ĐVT", "TL ĐVT Toàn d", "TL ĐVT d", "TL ĐVT PT"};

    private static final String GROUP_1 = "1. Quân nhu";
    private static final String GROUP_2 = "2. Quân y";
    private static final String GROUP_3 = "3. Doanh trại";
    private static final String GROUP_4 = "4. VTKT";

    private static final String[] GROUPS_IN_ORDER = {GROUP_1, GROUP_2, GROUP_3, GROUP_4};
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private record Personnel(int quanSoD, int quanSoPt) {
    }

    private record VchcItem(String tenVatChat, String dvt, String danhMuc, double quyUoc, String donViTinh) {
    }

    /**
     * Model read-only cho Bảng Vật Chất Hậu Cần (5 cột).
     */
    public DefaultTableModel getHauCanTableModel(int sessionId) {
        DefaultTableModel model = new DefaultTableModel(HC_COLS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (sessionId <= 0) {
            return model;
        }
        loadHauCanDataFromDatabase(sessionId, model);
        return model;
    }

    /**
     * Load dữ liệu theo đúng 4 phase mô tả (read-only).
     */
    public void loadHauCanDataFromDatabase(int sessionId, DefaultTableModel model) {
        if (model == null) {
            return;
        }
        model.setRowCount(0);
        if (sessionId <= 0) {
            return;
        }

        // Phase 1: quân số theo Hướng và Toàn d (d/PT)
        Map<String, Personnel> personnelByHuong = loadPersonnelByHuong(sessionId);
        Personnel toanD = personnelByHuong.getOrDefault("__TOAN_D__", new Personnel(0, 0));

        // Phase 2: ONLY query quyuoc_vchc và phân 4 nhóm cố định (dù rỗng vẫn render)
        Map<String, List<VchcItem>> groups = loadAndGroupQuyUocVchcStrict();

        // Phase 3-4: render khung chuẩn
        appendHauCanSection(model, romanLabel(0) + ". Toàn d", toanD, groups);

        List<String> huongs = loadDistinctHuong(sessionId);
        for (int i = 0; i < huongs.size(); i++) {
            String huong = huongs.get(i);
            Personnel p = personnelByHuong.getOrDefault(huong, new Personnel(0, 0));
            appendHauCanSection(model, romanLabel(i + 1) + ". " + huong, p, groups);
        }
    }

    private void appendHauCanSection(DefaultTableModel model, String headerTitle, Personnel p, Map<String, List<VchcItem>> groups) {
        model.addRow(headerRow5(headerTitle));
        for (String g : GROUPS_IN_ORDER) {
            appendGroup(model, g, groups.getOrDefault(g, Collections.emptyList()), p);
        }
    }

    private void appendGroup(DefaultTableModel model, String groupName, List<VchcItem> items, Personnel p) {
        model.addRow(groupRow5(groupName));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (VchcItem it : items) {
            model.addRow(itemRow5(it, p));
        }
    }

    private Object[] headerRow5(String title) {
        Object[] r = new Object[HC_COL_COUNT];
        Arrays.fill(r, "");
        r[0] = title;
        return r;
    }

    private Object[] groupRow5(String groupName) {
        Object[] r = new Object[HC_COL_COUNT];
        Arrays.fill(r, "");
        r[0] = groupName;
        return r;
    }

    private Object[] itemRow5(VchcItem it, Personnel p) {
        Object[] r = new Object[HC_COL_COUNT];
        Arrays.fill(r, "");
        r[0] = it.tenVatChat();
        r[1] = it.dvt();

        double multi = isPercentUnit(it.donViTinh()) ? 0.01 : 1.0;
        // Phase 3 fix: quân số = 0 => TL = 0 (không default 1)
        double tlD = it.quyUoc() * p.quanSoD() * multi;
        double tlPt = it.quyUoc() * p.quanSoPt() * multi;
        double tlToanD = tlD + tlPt;

        r[2] = fmt2(tlToanD);
        r[3] = fmt2(tlD);
        r[4] = fmt2(tlPt);
        return r;
    }

    private static boolean isPercentUnit(String donViTinh) {
        if (donViTinh == null) {
            return false;
        }
        String t = donViTinh.trim();
        if (t.isEmpty()) {
            return false;
        }
        return t.contains("%") || t.toUpperCase(Locale.ROOT).contains("%QS");
    }

    private static String fmt2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String romanLabel(int idx) {
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= ROMAN.length) {
            idx = ROMAN.length - 1;
        }
        return ROMAN[idx];
    }

    /**
     * Phase 1: JOIN step2_bien_che + quyuoc_bienche theo session_id, tổng quân số theo hướng và theo nhom_don_vi:
     * - 'Tiểu đoàn' -> quan_so_d
     * - 'Trung đoàn' -> quan_so_pt
     */
    private Map<String, Personnel> loadPersonnelByHuong(int sessionId) {
        Map<String, int[]> tmp = new LinkedHashMap<>();
        int sumD = 0;
        int sumPt = 0;

        String sql = "SELECT TRIM(s2.huong) AS huong, q.nhom_don_vi, SUM(q.quan_so) AS tong_qs "
                + "FROM step2_bien_che s2 JOIN quyuoc_bienche q ON s2.quyuoc_id = q.id "
                + "WHERE s2.session_id = ? AND s2.huong IS NOT NULL "
                + "GROUP BY TRIM(s2.huong), q.nhom_don_vi";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String huong = rs.getString("huong");
                    if (huong == null) {
                        continue;
                    }
                    huong = huong.trim();
                    String nhom = rs.getString("nhom_don_vi");
                    int qs = rs.getInt("tong_qs");
                    int[] arr = tmp.computeIfAbsent(huong, k -> new int[]{0, 0});
                    if ("Tiểu đoàn".equalsIgnoreCase(nhom != null ? nhom.trim() : "")) {
                        arr[0] += qs;
                        sumD += qs;
                    } else if ("Trung đoàn".equalsIgnoreCase(nhom != null ? nhom.trim() : "")) {
                        arr[1] += qs;
                        sumPt += qs;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Personnel> out = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> e : tmp.entrySet()) {
            out.put(e.getKey(), new Personnel(e.getValue()[0], e.getValue()[1]));
        }
        out.put("__TOAN_D__", new Personnel(sumD, sumPt));
        return out;
    }

    /** Danh sách hướng từ step2_bien_che (distinct). */
    private List<String> loadDistinctHuong(int sessionId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT TRIM(huong) AS huong FROM step2_bien_che "
                + "WHERE session_id = ? AND huong IS NOT NULL ORDER BY TRIM(huong)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String h = rs.getString("huong");
                    if (h != null && !h.isBlank()) {
                        list.add(h.trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Phase 2 (strict): chỉ lấy danh sách vật chất từ 1 query duy nhất `SELECT * FROM quyuoc_vchc` và gom nhóm theo danh_muc:
     * - Quân nhu: 'Quân lương' hoặc 'Quân trang'
     * - Quân y: 'Quân y'
     * - Doanh trại: 'Doanh trại'
     * - VTKT: 'Khác'
     */
    private Map<String, List<VchcItem>> loadAndGroupQuyUocVchcStrict() {
        Map<String, List<VchcItem>> groups = new LinkedHashMap<>();
        groups.put(GROUP_1, new ArrayList<>());
        groups.put(GROUP_2, new ArrayList<>());
        groups.put(GROUP_3, new ArrayList<>());
        groups.put(GROUP_4, new ArrayList<>());

        String sql = "SELECT * FROM quyuoc_vchc ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String danhMuc = rs.getString("danh_muc") != null ? rs.getString("danh_muc").trim() : "";
                String ten = rs.getString("ten_vat_chat") != null ? rs.getString("ten_vat_chat").trim() : "";
                double quyUoc = rs.getDouble("quy_uoc");
                String dvt = rs.getString("don_vi_tinh") != null ? rs.getString("don_vi_tinh").trim() : "";
                String donViTinh = dvt;

                String g = mapDanhMucToGroupStrict(danhMuc);
                groups.get(g).add(new VchcItem(ten, dvt, danhMuc, quyUoc, donViTinh));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }

    private static String mapDanhMucToGroupStrict(String danhMuc) {
        String t = danhMuc != null ? danhMuc.trim() : "";
        if ("Quân lương".equalsIgnoreCase(t) || "Quân trang".equalsIgnoreCase(t)) {
            return GROUP_1;
        }
        if ("Quân y".equalsIgnoreCase(t)) {
            return GROUP_2;
        }
        if ("Doanh trại".equalsIgnoreCase(t)) {
            return GROUP_3;
        }
        return GROUP_4; // 'Khác'
    }

    static final class ItemData {
        String ten, dvt, danhMuc;
        double quyUoc, wQddt, wTtGdcb, wTtGdcd, wPcScd, wPcTqd, wHcKhoD, wHcDonVi, wHcPhoiThuoc;
        // Định mức gốc (copy từ Toàn d xuống các Hướng, không tính lại theo hướng)
        double baseQddt, baseTtGdcb, baseTtGdcd;
        double basePcScdKhoD, basePcScdDv;
        double baseHcKhoD, baseHcKhoPt, baseHcDvD, baseHcDvPt;
        double basePcTqdKhoD, basePcTqdDv;
        boolean isPercent, isDauThap, isTYS, isTYT, isTCT, ptCannotHold;
        double[] pc04 = new double[6];
        double[] scd = new double[6];
        double[] gdcb = new double[6];
        double[] gdcd = new double[6];
    }

    private static int huongDetailIndex(String huongName) {
        if (huongName == null) return 0;
        if ("Toàn d".equals(huongName)) return 0;
        if (huongName.contains("chủ yếu")) return 2;
        if (huongName.contains("thứ yếu")) return 3;
        if (huongName.contains("phía sau") || huongName.contains("vòng ngoài")) return 4;
        String low = huongName.toLowerCase();
        if (low.contains("ll") && huongName.contains("còn lại")) return 5;
        return 1;
    }

    private static double at(double[] a, int i) {
        if (a == null || i < 0 || i >= a.length) return 0;
        return a[i];
    }

    private static double[] parse6(String s) {
        double[] res = new double[6];
        if (s == null || s.isEmpty()) return res;
        String[] p = s.split(",", -1);
        for (int i = 0; i < Math.min(6, p.length); i++) {
            res[i] = InputValidator.parseDoubleSafe(p[i].trim());
        }
        return res;
    }

    public void loadDataFromDatabase(int sessionId, int type, DefaultTableModel vatChatModel) {
        vatChatModel.setRowCount(0);
        if (sessionId <= 0) return;

        // Phase 1: Query quân số từ step2_bien_che JOIN quyuoc_bienche.
        Map<String, Double> quanSoTheoHuongD = new LinkedHashMap<>();
        Map<String, Double> quanSoTheoHuongPT = new LinkedHashMap<>();
        double totalQuanSoD = 0;
        double totalQuanSoPT = 0;

        String sqlQS = "SELECT TRIM(s.huong) AS huong, q.nhom_don_vi, q.quan_so "
                + "FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id "
                + "WHERE s.session_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQS)) {
            ps.setInt(1, sessionId);
            ResultSet rsQs = ps.executeQuery();
            while (rsQs.next()) {
                String huong = rsQs.getString("huong");
                if (huong == null || huong.isBlank()) {
                    continue;
                }
                huong = huong.trim();
                String nhom = rsQs.getString("nhom_don_vi") != null ? rsQs.getString("nhom_don_vi").trim() : "";
                double qs = rsQs.getDouble("quan_so");

                if ("Tiểu đoàn".equalsIgnoreCase(nhom)) {
                    totalQuanSoD += qs;
                    quanSoTheoHuongD.put(huong, quanSoTheoHuongD.getOrDefault(huong, 0.0) + qs);
                } else if ("Trung đoàn".equalsIgnoreCase(nhom)) {
                    totalQuanSoPT += qs;
                    quanSoTheoHuongPT.put(huong, quanSoTheoHuongPT.getOrDefault(huong, 0.0) + qs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<ItemData> listQuanNhu = new ArrayList<>();
        List<ItemData> listQuanY = new ArrayList<>();
        List<ItemData> listDoanhTrai = new ArrayList<>();
        List<ItemData> listVTKT = new ArrayList<>();

        String sqlData = "SELECT s4.*, s3.kho_d as hc_kho_d, s3.don_vi as hc_don_vi, COALESCE(s3.ll_cd_vong_ngoai, s3.phoi_thuoc) as hc_phoi_thuoc, "
                + "qv.quy_uoc, qv.danh_muc, qv.don_vi_tinh " +
                "FROM step4_quy_dinh_du_tru s4 " +
                "LEFT JOIN step3_vat_chat s3 ON s4.session_id = s3.session_id AND s4.vat_chat = s3.vat_chat AND s3.loai_vat_chat IN (2, 3) " +
                "LEFT JOIN quyuoc_vchc qv ON s4.vat_chat = qv.ten_vat_chat " +
                "WHERE s4.session_id = ? AND s4.loai_vat_chat IN (2, 3) ORDER BY s4.id ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlData)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ItemData item = new ItemData();
                item.ten = rs.getString("vat_chat").trim();
                item.danhMuc = rs.getString("danh_muc") != null ? rs.getString("danh_muc").toLowerCase() : "";
                item.dvt = rs.getString("dvt") != null ? rs.getString("dvt").trim() : "";
                item.quyUoc = rs.getDouble("quy_uoc");
                item.wQddt = rs.getDouble("du_tru");
                item.wTtGdcb = rs.getDouble("tieu_thu_gdcb");
                item.wTtGdcd = rs.getDouble("tieu_thu_gdcd");
                item.wPcScd = rs.getDouble("phai_co_scd");
                item.wPcTqd = rs.getDouble("phai_co_0400");
                item.wHcKhoD = rs.getDouble("hc_kho_d");
                item.wHcDonVi = rs.getDouble("hc_don_vi");
                item.wHcPhoiThuoc = rs.getDouble("hc_phoi_thuoc");

                item.pc04 = parse6(rs.getString("pc04_chitiet"));
                item.scd = parse6(rs.getString("scd_chitiet"));
                item.gdcb = parse6(rs.getString("gdcb_chitiet"));
                item.gdcd = parse6(rs.getString("gdcd_chitiet"));

                // Phase 2: ghép số liệu gốc cho Toàn d (sẽ copy cho mọi Hướng)
                item.baseQddt = item.wQddt;
                item.baseTtGdcb = item.wTtGdcb;
                item.baseTtGdcd = item.wTtGdcd;

                // PC SCĐ: ưu tiên bóc tách từ scd_chitiet[0],[1], fallback từ phai_co_scd
                double scdKho = at(item.scd, 0);
                double scdDv = at(item.scd, 1);
                if (Math.abs(scdKho) + Math.abs(scdDv) < 1e-9) {
                    scdKho = item.wPcScd * 0.5;
                    scdDv = item.wPcScd * 0.5;
                }
                item.basePcScdKhoD = scdKho;
                item.basePcScdDv = scdDv;

                // Hiện có từ step3: kho_d, don_vi, phoi_thuoc
                item.baseHcKhoD = item.wHcKhoD;
                item.baseHcKhoPt = 0;
                item.baseHcDvD = item.wHcDonVi;
                item.baseHcDvPt = item.wHcPhoiThuoc;

                // PC TQĐ: ưu tiên bóc tách từ pc04_chitiet[0],[1], fallback từ phai_co_0400
                double pc04Kho = at(item.pc04, 0);
                double pc04Dv = at(item.pc04, 1);
                if (Math.abs(pc04Kho) + Math.abs(pc04Dv) < 1e-9) {
                    pc04Kho = item.wPcTqd * 0.5;
                    pc04Dv = item.wPcTqd * 0.5;
                }
                item.basePcTqdKhoD = pc04Kho;
                item.basePcTqdDv = pc04Dv;

                String dvtQuyUoc = rs.getString("don_vi_tinh") != null ? rs.getString("don_vi_tinh").trim() : "";
                item.isPercent = dvtQuyUoc.contains("%QS") || dvtQuyUoc.contains("%");
                String tenLower = item.ten.toLowerCase(Locale.ROOT);
                item.isDauThap = tenLower.contains("dầu thắp") || tenLower.contains("dau thap");
                item.isTYS = item.ten.equalsIgnoreCase("TYS") || tenLower.contains("túi y sĩ") || tenLower.contains("tui y si");
                item.isTYT = item.ten.equalsIgnoreCase("TYT") || tenLower.contains("túi y tá") || tenLower.contains("tui y ta");
                item.isTCT = item.ten.equalsIgnoreCase("TCT") || tenLower.contains("túi cứu thương") || tenLower.contains("tui cuu thuong");
                item.ptCannotHold = item.isTYS || item.isTYT || item.isTCT || item.ten.contains("Đường sữa");

                if (item.danhMuc.contains("y")) listQuanY.add(item);
                else if (item.danhMuc.contains("trại") || item.danhMuc.contains("dầu")) listDoanhTrai.add(item);
                else if (item.danhMuc.contains("kỹ thuật") || rs.getInt("loai_vat_chat") == 3) listVTKT.add(item);
                else listQuanNhu.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] laMa = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        List<String> listHuong = new ArrayList<>();
        listHuong.add("Toàn d");
        listHuong.addAll(loadDistinctHuongForVatChat(sessionId));

        @SuppressWarnings("unchecked")
        List<ItemData>[] catLists = new List[]{listQuanNhu, listQuanY, listDoanhTrai, listVTKT};
        String[] catNames = {"1. Quân nhu", "2. Quân y", "3. Doanh trại", "4. VTKT"};

        int huongIndex = 0;
        for (String huongName : listHuong) {
            double quanSoDHuong = quanSoTheoHuongD.getOrDefault(huongName, 0.0);
            double quanSoPTHuong = quanSoTheoHuongPT.getOrDefault(huongName, 0.0);

            // Hướng chỉ tính lực lượng trực tiếp chiến đấu -> ẩn Kho/d và ĐV PT phần Hiện có.
            boolean hasKho = huongName.equals("Toàn d");
            boolean hasPT = true;

            int rowHuongIdx = vatChatModel.getRowCount();
            Object[] rowHuong = new Object[27];
            Arrays.fill(rowHuong, "");
            int laIdx = Math.min(huongIndex, laMa.length - 1);
            rowHuong[0] = "<html><b>" + laMa[laIdx] + ". " + huongName + "</b></html>";
            huongIndex++;
            vatChatModel.addRow(rowHuong);
            double[] tongHuong = new double[27];

            for (int c = 0; c < 4; c++) {
                if (type == 1 && c == 3) continue;
                if (type == 2 && c != 3) continue;

                int rowCatIdx = vatChatModel.getRowCount();
                Object[] rowCat = new Object[27];
                Arrays.fill(rowCat, "");
                rowCat[0] = "<html><b>" + catNames[c] + "</b></html>";
                rowCat[1] = "<html><b>Tấn</b></html>";
                vatChatModel.addRow(rowCat);
                double[] tongCat = new double[27];

                for (ItemData item : catLists[c]) {
                    // Phase 2-3: khóa chặt tính TL ĐVT bằng hàm dùng chung.
                    double tlDvtToanD;
                    double tlDvtD;
                    double tlDvtPT;
                    if (huongName.equals("Toàn d")) {
                        tlDvtD = calculateTL(item.ten, item.quyUoc, item.dvt, totalQuanSoD);
                        tlDvtPT = calculateTL(item.ten, item.quyUoc, item.dvt, totalQuanSoPT);
                        tlDvtToanD = tlDvtD + tlDvtPT;
                    } else {
                        tlDvtD = calculateTL(item.ten, item.quyUoc, item.dvt, quanSoDHuong);
                        tlDvtPT = calculateTL(item.ten, item.quyUoc, item.dvt, quanSoPTHuong);
                        tlDvtToanD = tlDvtD + tlDvtPT;
                    }

                    // Định mức gốc: copy nguyên từ Toàn d xuống Hướng (không query/tính lại theo hướng)
                    double wQddt = item.baseQddt;
                    double wTtGdcb = item.baseTtGdcb;
                    double wTtGdcd = item.baseTtGdcd;
                    double scdKho = item.basePcScdKhoD;
                    double scdDv = item.basePcScdDv;
                    double wPcScd = scdKho + scdDv;
                    double pcTqdKho = item.basePcTqdKhoD;
                    double pcTqdDv = item.basePcTqdDv;
                    double wPcTqd = pcTqdKho + pcTqdDv;

                    double wHcKhoD = item.baseHcKhoD;
                    double wHcDonVi = item.baseHcDvD;
                    double wHcPhoiThuoc = item.baseHcDvPt;

                    Object[] row = new Object[27];
                    Arrays.fill(row, "-");
                    row[0] = item.ten;
                    row[1] = item.dvt;
                    if (huongName.equals("Toàn d")) {
                        row[2] = f(tlDvtToanD);
                        row[3] = f(tlDvtD);
                        row[4] = f(tlDvtPT);
                    } else {
                        row[2] = f(tlDvtToanD);
                        row[3] = f(tlDvtD);
                        row[4] = f(tlDvtPT);
                    }

                    row[5] = f(wQddt);
                    row[6] = f(wTtGdcb);
                    row[7] = f(wTtGdcd);
                    addTonWeighted(tongCat, 5, wQddt, tlDvtToanD);
                    addTonWeighted(tongCat, 6, wTtGdcb, tlDvtToanD);
                    addTonWeighted(tongCat, 7, wTtGdcd, tlDvtToanD);

                    double s0 = scdKho;
                    double s1 = scdDv;
                    double s2 = wPcScd;
                    row[8] = f(s0);
                    row[9] = f(s1);
                    row[10] = f(s2);
                    addTonWeighted(tongCat, 8, s0, tlDvtToanD);
                    addTonWeighted(tongCat, 9, s1, tlDvtToanD);
                    addTonWeighted(tongCat, 10, s2, tlDvtToanD);

                    row[11] = hasKho ? f(wHcKhoD) : "-";
                    row[12] = hasKho ? f(item.baseHcKhoPt) : "-";
                    row[13] = f(wHcDonVi);
                    row[14] = hasPT ? f(wHcPhoiThuoc) : "-";
                    double hcSum = (hasKho ? (wHcKhoD + item.baseHcKhoPt) : 0) + wHcDonVi + (hasPT ? wHcPhoiThuoc : 0);
                    row[15] = f(hcSum);
                    if (hasKho) {
                        addTonWeighted(tongCat, 11, wHcKhoD, tlDvtToanD);
                        addTonWeighted(tongCat, 12, item.baseHcKhoPt, tlDvtToanD);
                    }
                    addTonWeighted(tongCat, 13, wHcDonVi, tlDvtD);
                    if (hasPT) addTonWeighted(tongCat, 14, wHcPhoiThuoc, tlDvtPT);
                    addTonWeighted(tongCat, 15, hcSum, tlDvtToanD);

                    // Bổ sung theo logic thiếu hụt tương tự "Trước nổ súng/Thực hành": âm -> 0.
                    pcTqdKho = hasKho ? pcTqdKho : 0;
                    pcTqdDv = pcTqdDv;
                    double gdcbKho = hasKho ? (wTtGdcb * 0.5) : 0;
                    double gdcbDv = wTtGdcb - gdcbKho;
                    double gdcdKho = hasKho ? (wTtGdcd * 0.5) : 0;
                    double gdcdDv = wTtGdcd - gdcdKho;

                    double hcKho = hasKho ? parseCellTon(row[11]) + parseCellTon(row[12]) : 0;
                    double hcDv = parseCellTon(row[13]);

                    double b16 = nonNeg(pcTqdKho - hcKho);
                    double b17 = nonNeg(pcTqdDv - hcDv);
                    double b18 = nonNeg(gdcbKho + pcTqdKho - hcKho);
                    double b19 = nonNeg(gdcbDv + pcTqdDv - hcDv);
                    double b20 = nonNeg(gdcdKho + gdcbKho - pcTqdKho);
                    double b21 = nonNeg(gdcdDv + gdcbDv - pcTqdDv);
                    double bsMid = b16 + b17 + b18 + b19 + b20 + b21;
                    row[16] = f(b16);
                    row[17] = f(b17);
                    row[18] = f(b18);
                    row[19] = f(b19);
                    row[20] = f(b20);
                    row[21] = f(b21);
                    row[22] = f(bsMid);
                    addTonWeighted(tongCat, 16, b16, tlDvtToanD);
                    addTonWeighted(tongCat, 17, b17, tlDvtD);
                    addTonWeighted(tongCat, 18, b18, tlDvtToanD);
                    addTonWeighted(tongCat, 19, b19, tlDvtD);
                    addTonWeighted(tongCat, 20, b20, tlDvtToanD);
                    addTonWeighted(tongCat, 21, b21, tlDvtD);
                    addTonWeighted(tongCat, 22, bsMid, tlDvtToanD);

                    row[23] = "";
                    row[24] = "";
                    row[25] = "";
                    row[26] = "";

                    vatChatModel.addRow(row);
                }

                for (int col = 5; col <= 22; col++) {
                    vatChatModel.setValueAt("<html><b>" + f(tongCat[col]) + "</b></html>", rowCatIdx, col);
                    tongHuong[col] += tongCat[col];
                }
            }

            for (int col = 5; col <= 22; col++) {
                vatChatModel.setValueAt("<html><b>" + f(tongHuong[col]) + "</b></html>", rowHuongIdx, col);
            }
        }
    }

    private static void addTon(double[] tongCat, int col, double kg) {
        tongCat[col] += kg / 1000.0;
    }

    /**
     * Cộng dồn trọng lượng cho dòng tổng (tấn): hệ số * TL ĐVT tương ứng / 1000.
     */
    private static void addTonWeighted(double[] tongCat, int col, double heSo, double tlDvt) {
        tongCat[col] += (heSo * tlDvt) / 1000.0;
    }

    private List<String> loadDistinctHuongForVatChat(int sessionId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT TRIM(huong) AS huong FROM step2_bien_che "
                + "WHERE session_id = ? AND huong IS NOT NULL ORDER BY TRIM(huong)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String h = rs.getString("huong");
                    if (h != null && !h.isBlank() && !h.equalsIgnoreCase("Toàn d")) {
                        list.add(h.trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private double calculateTL(String tenVatChat, double quyUoc, String donViTinh, double quanSo) {
        if (quanSo == 0) return 0;
        String tenLower = tenVatChat == null ? "" : tenVatChat.toLowerCase(Locale.ROOT);

        // 1) Ngoại lệ Túi y tế: Không nhân quân số
        if (tenLower.contains("túi") || tenLower.contains("tui")) return quyUoc;

        // 2) Ngoại lệ Dầu thắp: Chia 30 ngày
        if (tenLower.contains("dầu thắp") || tenLower.contains("dau thap")) return (quyUoc * quanSo) / 30.0;

        // 3) Ngoại lệ %QS
        if (donViTinh != null && donViTinh.contains("%")) return quyUoc * quanSo * 0.01;

        // 4) Cơ bản
        return quyUoc * quanSo;
    }

    private static double parseCellTon(Object cell) {
        if (cell == null || "-".equals(cell)) return 0;
        String s = cell.toString().replace(",", ".").replaceAll("<[^>]+>", "");
        return InputValidator.parseDoubleSafe(s);
    }

    private static double nonNeg(double v) {
        return v < 0 ? 0 : v;
    }

    private static boolean tenLowerContainsTui(String tenVatChat) {
        if (tenVatChat == null) {
            return false;
        }
        String t = tenVatChat.toLowerCase(Locale.ROOT);
        return t.contains("túi") || t.contains("tui");
    }

    private String f(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value);
    }

    public Map<String, String> getExportData(int type, DefaultTableModel vatChatModel) {
        Map<String, String> data = new HashMap<>();
        String key = (type == 1) ? "<<rows_bang_vat_chat>>" : "<<rows_bang_vat_tu>>";
        data.put(key, buildTableHtml(vatChatModel));
        return data;
    }

    private String buildTableHtml(DefaultTableModel m) {
        if (m == null || m.getRowCount() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < m.getColumnCount(); j++) {
                Object val = m.getValueAt(i, j);
                String text = (val == null) ? "" : val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                if (j == 0) sb.append("<td class='text-left'>").append(text).append("</td>");
                else sb.append("<td>").append(text).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }
}
