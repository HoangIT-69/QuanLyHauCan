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
import java.util.concurrent.ConcurrentHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * JDBC và tính toán bảng vật chất (Tab 5 popup). {@code type}: 1 = VCHC, 2 = VTKT.
 */
public class Tab5_VatChatPanelService {

    /** Khóa trong value map của {@link #miniTableVCHC}: TL (tấn) Bổ sung GĐCB Kho/d (d). */
    public static final String TL_BO_SUNG_GDCB_KHO = "TL_BoSung_GDCB_Kho";
    /** Khóa: TL (tấn) Bổ sung GĐCB Kho/d (PT). */
    public static final String TL_BO_SUNG_GDCB_KHO_PT = "TL_BoSung_GDCB_Kho_PT";
    /** Khóa: TL (tấn) Bổ sung GĐCB ĐV (d). */
    public static final String TL_BO_SUNG_GDCB_DV_D = "TL_BoSung_GDCB_DV_d";
    /** Khóa: TL (tấn) Bổ sung GĐCB ĐV (PT). */
    public static final String TL_BO_SUNG_GDCB_DV_PT = "TL_BoSung_GDCB_DV_PT";
    /** Khóa: TL (tấn) Bổ sung GĐCĐ Kho/d (× TL Toàn d). */
    public static final String TL_BO_SUNG_GDCD_KHO = "TL_BoSung_GDCD_Kho";
    /** Khóa: TL (tấn) Bổ sung GĐCĐ ĐV (× TL Toàn d). */
    public static final String TL_BO_SUNG_GDCD_DV = "TL_BoSung_GDCD_DV";
    /** Khóa: TL (tấn) tổng (+) = GĐCB + GĐCĐ. */
    public static final String TL_BO_SUNG_TOTAL = "TL_BoSung_Total";

    /**
     * Tổng trọng lượng (tấn) khối I. Toàn d — 7 cột bổ sung. Đọc bởi các module khác (vận tải, …).
     */
    public static final Map<String, Double> globalTonnageVCHC = new ConcurrentHashMap<>();
    private static final Object GLOBAL_TONNAGE_LOCK = new Object();

    /**
     * Khối "I. Toàn d": mỗi vật chất → 7 TL bổ sung (tấn). Thứ tự theo duyệt bảng.
     */
    public static final Map<String, Map<String, Double>> miniTableVCHC = new LinkedHashMap<>();
    private static final Object MINI_VCHC_LOCK = new Object();

    /**
     * Dữ liệu bổ sung chi tiết theo từng Hướng.
     * Cấu trúc: Hướng -> (Tên vật chất -> Map<TL_BO_SUNG_..., Giá trị>)
     */
    public static final Map<String, Map<String, Map<String, Double>>> miniTableVCHCByDirection = new ConcurrentHashMap<>();

    // ---- Hằng số cho từng nhóm vật chất (dùng cho Tab9) ----
    public static final String CAT_QN   = "QN";
    public static final String CAT_QY   = "QY";
    public static final String CAT_DT   = "DT";
    public static final String CAT_VTKT = "VTKT";
    /** Khóa TL vận chuyển GĐCB (tấn, đã loại âm). */
    public static final String TL_GDCB      = "TL_GDCB";
    /** Khóa TL vận chuyển GĐCĐ (tấn, đã loại âm). */
    public static final String TL_GDCD      = "TL_GDCD";
    /** Khóa TL vận chuyển Toàn trận = GĐCB + GĐCĐ. */
    public static final String TL_TOAN_TRAN = "TL_ToanTran";

    /**
     * TL vận chuyển (tấn) theo từng nhóm vật chất: QN, QY, DT, VTKT.
     * Key ngoài = CAT_QN/QY/DT/VTKT; key trong = TL_GDCB/TL_GDCD/TL_TOAN_TRAN.
     */
    public static final Map<String, Map<String, Double>> globalTonnageVCHC_ByCat = new ConcurrentHashMap<>();

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

    private enum SectionType { TOAN_D, HUONG, LLC }

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
        appendHauCanSection(model, romanLabel(0) + ". Toàn d", toanD, groups, SectionType.TOAN_D);

        List<String> huongs = loadDistinctHuong(sessionId);
        for (int i = 0; i < huongs.size(); i++) {
            String huong = huongs.get(i);
            Personnel p = personnelByHuong.getOrDefault(huong, new Personnel(0, 0));
            boolean isLLC = huong.toLowerCase(Locale.ROOT).contains("lực lượng còn lại");
            SectionType type = isLLC ? SectionType.LLC : SectionType.HUONG;
            appendHauCanSection(model, romanLabel(i + 1) + ". " + huong, p, groups, type);
        }
    }

    private void appendHauCanSection(DefaultTableModel model, String headerTitle, Personnel p, Map<String, List<VchcItem>> groups, SectionType type) {
        model.addRow(headerRow5(headerTitle));
        for (String g : GROUPS_IN_ORDER) {
            appendGroup(model, g, groups.getOrDefault(g, Collections.emptyList()), p, type);
        }
    }

    private void appendGroup(DefaultTableModel model, String groupName, List<VchcItem> items, Personnel p, SectionType type) {
        model.addRow(groupRow5(groupName));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (VchcItem it : items) {
            model.addRow(itemRow5(it, p, type));
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

    private Object[] itemRow5(VchcItem it, Personnel p, SectionType type) {
        Object[] r = new Object[HC_COL_COUNT];
        Arrays.fill(r, "");
        r[0] = it.tenVatChat();
        r[1] = it.dvt();

        double multi = isPercentUnit(it.donViTinh()) ? 0.01 : 1.0;
        switch (type) {
            case TOAN_D -> {
                double tlD = it.quyUoc() * p.quanSoD() * multi;
                double tlPt = it.quyUoc() * p.quanSoPt() * multi;
                r[2] = fmt2(tlD + tlPt);
                r[3] = fmt2(tlD);
                r[4] = fmt2(tlPt);
            }
            case HUONG -> {
                // Chỉ hiện TL Toàn d = quanSoD, cột d và PT để "-"
                r[2] = fmt2(it.quyUoc() * p.quanSoD() * multi);
                r[3] = "-";
                r[4] = "-";
            }
            case LLC -> {
                // Lực lượng còn lại: TL Toàn d = quanSoPt (phối thuộc), cột d và PT để "-"
                r[2] = fmt2(it.quyUoc() * p.quanSoPt() * multi);
                r[3] = "-";
                r[4] = "-";
            }
        }
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
                    String nhomTrim = nhom != null ? nhom.trim() : "";
                    if ("Tiểu đoàn".equalsIgnoreCase(nhomTrim)) {
                        arr[0] += qs;
                        sumD += qs;
                    } else if (nhomTrim.contains("\u0111o\u00e0n") && !nhomTrim.contains("Ti\u1ec3u")) {
                        // Trung đoàn + Sư đoàn đều tính vào phối thuộc (PT)
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
        /** Đơn vị tính từ quyuoc_vchc (dùng cho %QS / % trong TL). */
        String donViTinhQuyUoc;
        double quyUoc, wQddt, wTtGdcb, wTtGdcd, wPcScd, wPcTqd, wHcKhoD, wHcDonVi, wHcPhoiThuoc;
        // Định mức gốc (copy từ Toàn d xuống các Hướng, không tính lại theo hướng)
        double baseQddt, baseTtGdcb, baseTtGdcd;
        double basePcScdKhoD, basePcScdDv;
        double baseHcKhoD, baseHcKhoPt, baseHcDvD, baseHcDvPt;
        double basePcTqdKhoD, basePcTqdDv;
        boolean isPercent, isDauThap;
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

    /** Hệ số / định mức dòng con: bỏ .00 vô nghĩa (10 → "10", 1.5 → "1.5"). */
    private static String fmtCoeff(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        if (Math.abs(v) < 1e-12) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        String s = df.format(v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s.isEmpty() ? "0" : s;
    }

    /** Trọng lượng dòng tổng in đậm: luôn 2 chữ số thập phân (tấn). */
    private static String fmtTonBold(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /** Đường sữa / ĐSTB (tên vật chất). */
    private static boolean isDuongSuaOrDstbTen(String ten) {
        if (ten == null) {
            return false;
        }
        String t = ten.toLowerCase(Locale.ROOT);
        return t.contains("đường sữa") || t.contains("duong sua")
                || t.contains("đstb") || t.contains("dstb");
    }

    /** Chỉ tại block Hướng: ĐSTB → cả dòng số thành "-". LLCL hiển thị bình thường. */
    private static boolean isHuongDstbFullDash(String ten, boolean isDirection) {
        return isDirection && isDuongSuaOrDstbTen(ten);
    }

    /**
     * PT gạch theo nghiệp vụ; tại LLCL không áp dụng gạch PT riêng cho Đường sữa (hiển thị đủ như Toàn d).
     */
    private static boolean computePtExc(String ten, int catIndex, int type, boolean llclBlock) {
        if (llclBlock && isDuongSuaOrDstbTen(ten)) {
            return false;
        }
        return isPtExcludedVatChat(ten, catIndex, type);
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
        if (sessionId <= 0) {
            synchronized (MINI_VCHC_LOCK) {
                miniTableVCHC.clear();
                miniTableVCHCByDirection.clear();
            }
            return;
        }
        if (type == 1) {
            synchronized (MINI_VCHC_LOCK) {
                miniTableVCHC.clear();
                miniTableVCHCByDirection.clear();
            }
        }

        // Phase 1: quân số — Tiểu đoàn (d Toàn d), Trung đoàn (pt Toàn d), từng Hướng từ step2_bien_che.
        Map<String, Double> quanSoTheoHuongD = new LinkedHashMap<>();
        Map<String, Double> quanSoTheoHuongPT = new LinkedHashMap<>();
        double quanSo_d_ToanD = 0;
        double quanSo_pt_ToanD = 0;

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
                    quanSo_d_ToanD += qs;
                    quanSoTheoHuongD.put(huong, quanSoTheoHuongD.getOrDefault(huong, 0.0) + qs);
                } else if (nhom != null && nhom.contains("\u0111o\u00e0n") && !nhom.contains("Ti\u1ec3u")) {
                    // Trung đoàn + Sư đoàn đều tính vào phối thuộc (PT)
                    quanSo_pt_ToanD += qs;
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

        boolean includeVtktInVchc = (type == 1);
        String sqlData = "SELECT s4.*, s3.kho_d as hc_kho_d, s3.don_vi as hc_don_vi, COALESCE(s3.ll_cd_vong_ngoai, s3.phoi_thuoc) as hc_phoi_thuoc, "
                + "qv.quy_uoc, qv.danh_muc, qv.don_vi_tinh " +
                "FROM step4_quy_dinh_du_tru s4 " +
                "LEFT JOIN step3_vat_chat s3 ON s4.session_id = s3.session_id AND s4.vat_chat = s3.vat_chat AND s4.loai_vat_chat = s3.loai_vat_chat " +
                "LEFT JOIN quyuoc_vchc qv ON s4.vat_chat = qv.ten_vat_chat " +
                "WHERE s4.session_id = ? "
                + (includeVtktInVchc ? "AND s4.loai_vat_chat IN (2, 3) " : "AND s4.loai_vat_chat = 3 ")
                + "ORDER BY s4.id ASC";

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
                item.donViTinhQuyUoc = dvtQuyUoc;
                item.isPercent = dvtQuyUoc.contains("%QS") || dvtQuyUoc.contains("%");
                String tenLower = item.ten.toLowerCase(Locale.ROOT);
                item.isDauThap = tenLower.contains("dầu thắp") || tenLower.contains("dau thap");

                if (type == 2) {
                    listVTKT.add(item);
                } else if (item.danhMuc.contains("y")) listQuanY.add(item);
                else if (item.danhMuc.contains("trại") || item.danhMuc.contains("dầu")) listDoanhTrai.add(item);
                else if (item.danhMuc.contains("kỹ thuật")
                        || item.danhMuc.contains("ky thuat")
                        || item.danhMuc.contains("vtkt")
                        || rs.getInt("loai_vat_chat") == 3) listVTKT.add(item);
                else listQuanNhu.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] laMa = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
        List<String> listHuong = new ArrayList<>();
        listHuong.add("Toàn d");
        listHuong.addAll(loadDistinctHuongForVatChat(sessionId));

        double sumQuanSoDirectionD = 0;
        for (String hx : listHuong) {
            if (!"Toàn d".equals(hx)) {
                sumQuanSoDirectionD += quanSoTheoHuongD.getOrDefault(hx, 0.0);
            }
        }
        double quanSo_d_LLCL = Math.max(0.0, quanSo_d_ToanD - sumQuanSoDirectionD);
        double quanSo_pt_LLCL = quanSo_pt_ToanD;
        System.out.println("[Tab5_VatChatPanelService] Phân bổ QS: quanSo_d_ToanD=" + quanSo_d_ToanD
                + ", quanSo_pt_ToanD=" + quanSo_pt_ToanD
                + ", tổng quanSo_d_Huong=" + sumQuanSoDirectionD
                + ", quanSo_d_LLCL=" + quanSo_d_LLCL + ", quanSo_pt_LLCL=" + quanSo_pt_LLCL);

        List<String> blocks = new ArrayList<>(listHuong);
        blocks.add("__LLCL__");

        @SuppressWarnings("unchecked")
        List<ItemData>[] catLists = new List[]{listQuanNhu, listQuanY, listDoanhTrai, listVTKT};
        String[] catNames = {"1. Quân nhu", "2. Quân y", "3. Doanh trại", "4. VTKT"};

        /** LLCL: [0]=PC SCĐ Kho, [1]=Hiện có Kho/d, [2]=Hiện có Kho PT, [3]=b16, [4]=b18, [5]=b20 (đúng giá trị I. Toàn d). */
        Map<String, double[]> toanDKhoNumSnapByTen = new LinkedHashMap<>();

        int huongIndex = 0;
        double[] sumDirectionsAgg = new double[27];
        int rowIdxToanD = -1;
        double[] tongLlclAgg = null;

        // Tích lũy TL vận chuyển theo nhóm (chỉ khối Toàn d, type=1): [0]=QN, [1]=QY, [2]=DT, [3]=VTKT
        double[] catGdcb = new double[4];
        double[] catGdcd = new double[4];

        for (String huongName : blocks) {
            final boolean isLlcl = "__LLCL__".equals(huongName);
            final boolean isToanD = "Toàn d".equals(huongName);
            final boolean isDirection = !isToanD && !isLlcl;

            double quanSo_d_Huong = quanSoTheoHuongD.getOrDefault(huongName, 0.0);
            double quanSo_pt_Huong = quanSoTheoHuongPT.getOrDefault(huongName, 0.0);
            double quanSoDHuong = isLlcl ? quanSo_d_LLCL : quanSo_d_Huong;
            double quanSoPTHuong = isLlcl ? quanSo_pt_LLCL : quanSo_pt_Huong;

            boolean hasKho = isToanD || isLlcl;
            boolean hasPT = true;

            int rowHuongIdx = vatChatModel.getRowCount();
            if (isToanD) {
                rowIdxToanD = rowHuongIdx;
            }
            Object[] rowHuong = new Object[27];
            Arrays.fill(rowHuong, "");
            int laIdx = Math.min(huongIndex, laMa.length - 1);
            String labelBody = isLlcl ? "LL còn lại" : huongName;
            rowHuong[0] = "<html><b>" + laMa[laIdx] + ". " + labelBody + "</b></html>";
            huongIndex++;
            vatChatModel.addRow(rowHuong);
            double[] tongHuong = new double[27];

            for (int c = 0; c < 4; c++) {
                if (type == 2 && c != 3) {
                    continue;
                }

                int rowCatIdx = vatChatModel.getRowCount();
                Object[] rowCat = new Object[27];
                Arrays.fill(rowCat, "");
                rowCat[0] = "<html><b>" + catNames[c] + "</b></html>";
                rowCat[1] = "<html><b>Tấn</b></html>";
                vatChatModel.addRow(rowCat);
                double[] tongCat = new double[27];

                for (ItemData item : catLists[c]) {
                    if (isHuongDstbFullDash(item.ten, isDirection)) {
                        Object[] rowDash = new Object[27];
                        Arrays.fill(rowDash, "-");
                        rowDash[0] = item.ten;
                        rowDash[1] = item.dvt;
                        rowDash[22] = "";
                        rowDash[23] = "";
                        rowDash[24] = "";
                        rowDash[25] = "";
                        rowDash[26] = "";
                        vatChatModel.addRow(rowDash);
                        continue;
                    }

                    boolean ptExc = computePtExc(item.ten, c, type, isLlcl);

                    double tlDvtToanD;
                    double tlDvtD;
                    double tlDvtPT;
                    if (isToanD) {
                        tlDvtD = calculateTL(item.ten, item.quyUoc, item.donViTinhQuyUoc, quanSo_d_ToanD);
                        tlDvtPT = calculateTL(item.ten, item.quyUoc, item.donViTinhQuyUoc, quanSo_pt_ToanD);
                        tlDvtToanD = tlDvtD + tlDvtPT;
                    } else {
                        tlDvtD = calculateTL(item.ten, item.quyUoc, item.donViTinhQuyUoc, quanSoDHuong);
                        tlDvtPT = calculateTL(item.ten, item.quyUoc, item.donViTinhQuyUoc, quanSoPTHuong);
                        tlDvtToanD = tlDvtD + tlDvtPT;
                    }

                    double wQddt = item.baseQddt;
                    double wTtGdcb = item.baseTtGdcb;
                    double wTtGdcd = item.baseTtGdcd;
                    double scdKho = item.basePcScdKhoD;
                    double scdDv = item.basePcScdDv;
                    double pcTqdKhoRaw = item.basePcTqdKhoD;
                    double pcTqdDvRaw = item.basePcTqdDv;

                    double wHcKhoD = item.baseHcKhoD;
                    double wHcDonVi = item.baseHcDvD;
                    double wHcPhoiThuoc = item.baseHcDvPt;

                    double khoPtForCols = item.baseHcKhoPt;
                    double hcKhoForBs = (hasKho ? wHcKhoD : 0) + (hasKho && !ptExc ? khoPtForCols : 0);
                    double hcDvForBs = isDirection
                            ? wHcDonVi
                            : wHcDonVi + (hasPT && !ptExc ? wHcPhoiThuoc : 0);
                    double hcSumDisplay = isDirection ? wHcDonVi : hcKhoForBs + hcDvForBs;

                    if (isLlcl) {
                        double[] snK = toanDKhoNumSnapByTen.get(item.ten);
                        if (snK != null) {
                            scdKho = snK[0];
                            wHcKhoD = snK[1];
                            khoPtForCols = snK[2];
                            hcKhoForBs = (hasKho ? wHcKhoD : 0) + (hasKho && !ptExc ? khoPtForCols : 0);
                            hcSumDisplay = isDirection ? wHcDonVi : hcKhoForBs + hcDvForBs;
                        }
                    }

                    double wPcScd = scdKho + scdDv;

                    Object[] row = new Object[27];
                    Arrays.fill(row, "-");
                    row[0] = item.ten;
                    row[1] = item.dvt;
                    if (isDirection) {
                        // Các hướng: TL Toàn d = quanSoD, d và PT để "-"
                        row[2] = fmtCoeff(tlDvtD);
                        row[3] = "-";
                        row[4] = "-";
                    } else if (isLlcl) {
                        // Lực lượng còn lại: TL Toàn d = quanSoPt (phối thuộc), d và PT để "-"
                        row[2] = fmtCoeff(tlDvtPT);
                        row[3] = "-";
                        row[4] = "-";
                    } else {
                        // Toàn d: hiển thị đầy đủ
                        row[2] = fmtCoeff(tlDvtToanD);
                        row[3] = fmtCoeff(tlDvtD);
                        row[4] = ptExc ? "-" : fmtCoeff(tlDvtPT);
                    }

                    row[5] = fmtCoeff(wQddt);
                    row[6] = fmtCoeff(wTtGdcb);
                    row[7] = fmtCoeff(wTtGdcd);

                    double s0 = scdKho;
                    double s1 = scdDv;
                    double s2 = wPcScd;
                    if (isDirection) {
                        row[8] = "-";
                        row[9] = fmtCoeff(s1);
                        row[10] = fmtCoeff(s1);
                    } else {
                        row[8] = fmtCoeff(s0);
                        row[9] = fmtCoeff(s1);
                        row[10] = fmtCoeff(s2);
                    }

                    row[11] = hasKho ? fmtCoeff(wHcKhoD) : "-";
                    row[12] = hasKho && !ptExc ? fmtCoeff(khoPtForCols) : "-";
                    row[13] = fmtCoeff(wHcDonVi);
                    row[14] = (isDirection || !hasPT || ptExc) ? "-" : fmtCoeff(wHcPhoiThuoc);
                    row[15] = fmtCoeff(hcSumDisplay);

                    if (isDirection) {
                        addTonWeighted(tongCat, 5, wQddt, tlDvtD);
                        addTonWeighted(tongCat, 6, wTtGdcb, tlDvtD);
                        addTonWeighted(tongCat, 7, wTtGdcd, tlDvtD);
                        addTonWeighted(tongCat, 9, s1, tlDvtD);
                        addTonWeighted(tongCat, 10, s1, tlDvtD);
                        addTonWeighted(tongCat, 13, wHcDonVi, tlDvtD);
                        addTonWeighted(tongCat, 15, wHcDonVi, tlDvtD);
                    } else {
                        addTonWeighted(tongCat, 5, wQddt, tlDvtToanD);
                        addTonWeighted(tongCat, 6, wTtGdcb, tlDvtToanD);
                        addTonWeighted(tongCat, 7, wTtGdcd, tlDvtToanD);
                        addTonWeighted(tongCat, 8, s0, tlDvtToanD);
                        addTonWeighted(tongCat, 9, s1, tlDvtToanD);
                        addTonWeighted(tongCat, 10, s2, tlDvtToanD);
                        if (hasKho) {
                            addTonWeighted(tongCat, 11, wHcKhoD, tlDvtToanD);
                            if (!ptExc) {
                                addTonWeighted(tongCat, 12, khoPtForCols, tlDvtToanD);
                            }
                        }
                        addTonWeighted(tongCat, 13, wHcDonVi, tlDvtD);
                        if (hasPT && !ptExc) {
                            addTonWeighted(tongCat, 14, wHcPhoiThuoc, tlDvtPT);
                        }
                        addTonWeighted(tongCat, 15, hcKhoForBs + hcDvForBs, tlDvtToanD);
                    }

                    double pcTqdKho = hasKho ? pcTqdKhoRaw : 0;
                    double pcTqdDv = pcTqdDvRaw;
                    double ttGdcdKho = hasKho ? (wTtGdcd * 0.5) : 0;
                    double ttGdcdDv = wTtGdcd - ttGdcdKho;

                    double b16 = pcTqdKho - hcKhoForBs;
                    double b17 = pcTqdDv - hcDvForBs;
                    double b18 = b16;
                    double b19 = b17;
                    double b20 = s0 + ttGdcdKho - pcTqdKho;
                    double b21 = s1 + ttGdcdDv - pcTqdDv;

                    if (isLlcl) {
                        double[] snB = toanDKhoNumSnapByTen.get(item.ten);
                        if (snB != null) {
                            b16 = snB[3];
                            b18 = snB[4];
                            b20 = snB[5];
                        }
                    }

                    row[16] = hasKho ? fmtCoeff(b16) : "-";
                    row[17] = fmtCoeff(b17);
                    row[18] = hasKho ? fmtCoeff(b18) : "-";
                    row[19] = fmtCoeff(b19);
                    row[20] = fmtCoeff(b20);
                    row[21] = fmtCoeff(b21);
                    row[22] = "";

                    if (isDirection) {
                        addTonWeighted(tongCat, 17, b17, tlDvtD);
                        addTonWeighted(tongCat, 19, b19, tlDvtD);
                        addTonWeighted(tongCat, 21, b21, tlDvtD);
                        tongCat[22] += (b19 * tlDvtD + b21 * tlDvtD) / 1000.0;
                    } else {
                        addTonWeighted(tongCat, 16, b16, tlDvtToanD);
                        addTonWeighted(tongCat, 17, b17, tlDvtD);
                        addTonWeighted(tongCat, 18, b18, tlDvtToanD);
                        addTonWeighted(tongCat, 19, b19, tlDvtD);
                        addTonWeighted(tongCat, 20, b20, tlDvtToanD);
                        addTonWeighted(tongCat, 21, b21, tlDvtD);
                    }

                    row[23] = "";
                    row[24] = "";
                    row[25] = "";
                    row[26] = "";

                    if (type == 1 && isToanD && !isDuongSuaThuongBinh(item.ten)) {
                        // Bổ sung chỉ có Kho và ĐV, không có PT
                        // GĐCB Kho/d × TL Toàn d (khớp addTonWeighted col 18)
                        double tonGdcbKho = b18 * tlDvtToanD / 1000.0;
                        // GĐCB ĐV × TL d (khớp addTonWeighted col 19)
                        double tonGdcbDv  = b19 * tlDvtD     / 1000.0;
                        // GĐCĐ Kho/d × TL Toàn d (khớp addTonWeighted col 20)
                        double tonGdcdKho = b20 * tlDvtToanD / 1000.0;
                        // GĐCĐ ĐV × TL d (khớp addTonWeighted col 21)
                        double tonGdcdDv  = b21 * tlDvtD     / 1000.0;
                        double tonPlus = tonGdcbKho + tonGdcbDv + tonGdcdKho + tonGdcdDv;
                        recordMiniTableVchcRow(item.ten, c,
                                tonGdcbKho, tonGdcbDv, tonGdcdKho, tonGdcdDv, tonPlus);
                    } else if (type == 1 && isDirection && !isDuongSuaThuongBinh(item.ten)) {
                        double tonGdcbKho = b18 * tlDvtToanD / 1000.0;
                        double tonGdcbDv  = b19 * tlDvtD     / 1000.0;
                        double tonGdcdKho = b20 * tlDvtToanD / 1000.0;
                        double tonGdcdDv  = b21 * tlDvtD     / 1000.0;
                        synchronized (MINI_VCHC_LOCK) {
                            miniTableVCHCByDirection.computeIfAbsent(huongName, k -> new ConcurrentHashMap<>());
                            Map<String, Double> cmDir = new LinkedHashMap<>();
                            cmDir.put(TL_BO_SUNG_GDCB_KHO, Math.max(0, tonGdcbKho));
                            cmDir.put(TL_BO_SUNG_GDCB_DV_D, Math.max(0, tonGdcbDv));
                            cmDir.put(TL_BO_SUNG_GDCD_KHO, Math.max(0, tonGdcdKho));
                            cmDir.put(TL_BO_SUNG_GDCD_DV, Math.max(0, tonGdcdDv));
                            miniTableVCHCByDirection.get(huongName).put(item.ten, cmDir);
                        }
                    }

                    if (isToanD && hasKho) {
                        toanDKhoNumSnapByTen.put(item.ten, new double[]{
                                s0,
                                wHcKhoD,
                                item.baseHcKhoPt,
                                b16,
                                b18,
                                b20
                        });
                    }

                    vatChatModel.addRow(row);
                }

                if (hasKho) {
                    tongCat[22] = tongCat[18] + tongCat[19] + tongCat[20] + tongCat[21];
                }

                // Tích lũy TL vận chuyển (≥0) theo nhóm cho Tab9
                if (isToanD && type == 1) {
                    catGdcb[c] = Math.max(0.0, tongCat[18] + tongCat[19]);
                    catGdcd[c] = Math.max(0.0, tongCat[20] + tongCat[21]);
                }

                for (int col = 5; col <= 22; col++) {
                    boolean dirDashAgg = isDirection && (col == 8 || col == 11 || col == 12 || col == 16 || col == 18 || col == 20);
                    vatChatModel.setValueAt(
                            dirDashAgg ? "-" : "<html><b>" + fmtTonBold(tongCat[col]) + "</b></html>",
                            rowCatIdx,
                            col
                    );
                    tongHuong[col] += tongCat[col];
                }
            }

            for (int col = 5; col <= 22; col++) {
                boolean dirDashAgg = isDirection && (col == 8 || col == 11 || col == 12 || col == 16 || col == 18 || col == 20);
                vatChatModel.setValueAt(
                        dirDashAgg ? "-" : "<html><b>" + fmtTonBold(tongHuong[col]) + "</b></html>",
                        rowHuongIdx,
                        col
                );
            }

            if (isDirection) {
                for (int i = 0; i < 27; i++) {
                    sumDirectionsAgg[i] += tongHuong[i];
                }
            }
            if (isLlcl) {
                tongLlclAgg = Arrays.copyOf(tongHuong, 27);
            }
        }

        if (rowIdxToanD >= 0 && tongLlclAgg != null) {
            for (int col = 5; col <= 22; col++) {
                double v = sumDirectionsAgg[col] + tongLlclAgg[col];
                vatChatModel.setValueAt("<html><b>" + fmtTonBold(v) + "</b></html>", rowIdxToanD, col);
            }
        }

        // Ghi TL vận chuyển theo nhóm vào globalTonnageVCHC_ByCat
        if (type == 1) {
            String[] catKeys = {CAT_QN, CAT_QY, CAT_DT, CAT_VTKT};
            globalTonnageVCHC_ByCat.clear();
            for (int i = 0; i < 4; i++) {
                Map<String, Double> cm = new LinkedHashMap<>();
                cm.put(TL_GDCB,      catGdcb[i]);
                cm.put(TL_GDCD,      catGdcd[i]);
                cm.put(TL_TOAN_TRAN, catGdcb[i] + catGdcd[i]);
                globalTonnageVCHC_ByCat.put(catKeys[i], cm);
            }
            System.out.printf(Locale.US,
                    "[VCHC-ByCat] QN: GDCB=%.3f GDCD=%.3f | QY: GDCB=%.3f GDCD=%.3f | DT: GDCB=%.3f GDCD=%.3f | VTKT: GDCB=%.3f GDCD=%.3f (tấn)%n",
                    catGdcb[0], catGdcd[0], catGdcb[1], catGdcd[1],
                    catGdcb[2], catGdcd[2], catGdcb[3], catGdcd[3]);
        }

        // Cộng dồn tổng 7 cột Bổ sung (tấn) từ miniTableVCHC vào globalTonnageVCHC
        if (type == 1) {
            synchronized (MINI_VCHC_LOCK) {
                double sumGdcbKho = 0, sumGdcbDv = 0;
                double sumGdcdKho = 0, sumGdcdDv = 0, sumPlus = 0;
                for (Map<String, Double> m : miniTableVCHC.values()) {
                    sumGdcbKho += m.getOrDefault(TL_BO_SUNG_GDCB_KHO,   0.0);
                    sumGdcbDv  += m.getOrDefault(TL_BO_SUNG_GDCB_DV_D,  0.0);
                    sumGdcdKho += m.getOrDefault(TL_BO_SUNG_GDCD_KHO,   0.0);
                    sumGdcdDv  += m.getOrDefault(TL_BO_SUNG_GDCD_DV,    0.0);
                    sumPlus    += m.getOrDefault(TL_BO_SUNG_TOTAL,       0.0);
                }
                globalTonnageVCHC.put(TL_BO_SUNG_GDCB_KHO,   sumGdcbKho);
                globalTonnageVCHC.put(TL_BO_SUNG_GDCB_DV_D,  sumGdcbDv);
                globalTonnageVCHC.put(TL_BO_SUNG_GDCD_KHO,   sumGdcdKho);
                globalTonnageVCHC.put(TL_BO_SUNG_GDCD_DV,    sumGdcdDv);
                globalTonnageVCHC.put(TL_BO_SUNG_TOTAL,       sumPlus);
            }
            System.out.printf(Locale.US,
                    "[VCHC-TỔNG] GĐCBKho=%7.3f GĐCBDv=%7.3f | GĐCĐKho=%7.3f GĐCĐDv=%7.3f | (+)=%7.3f (tấn)%n",
                    globalTonnageVCHC.getOrDefault(TL_BO_SUNG_GDCB_KHO,  0.0),
                    globalTonnageVCHC.getOrDefault(TL_BO_SUNG_GDCB_DV_D, 0.0),
                    globalTonnageVCHC.getOrDefault(TL_BO_SUNG_GDCD_KHO,  0.0),
                    globalTonnageVCHC.getOrDefault(TL_BO_SUNG_GDCD_DV,   0.0),
                    globalTonnageVCHC.getOrDefault(TL_BO_SUNG_TOTAL,      0.0));
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

    /**
     * TL ĐVT: Túi y sĩ / y tá / cứu thương… = {@code quyUoc} (không nhân QS). Đường sữa/ĐSTB: render Hướng (Phase 3)
     * gạch dòng; LLCL/Toàn d nhân QS bình thường. %QS và dầu thắp giữ như cũ.
     */
    private double calculateTL(String tenVatChat, double quyUoc, String donViTinh, double quanSo) {
        if (quanSo == 0) {
            return 0;
        }
        String tenLower = tenVatChat == null ? "" : tenVatChat.toLowerCase(Locale.ROOT);

        if (tenLower.contains("túi") || tenLower.contains("tui")) {
            return quyUoc;
        }

        if (tenLower.contains("dầu thắp") || tenLower.contains("dau thap")) {
            return (quyUoc * quanSo) / 30.0;
        }

        if (donViTinh != null && (donViTinh.contains("%QS") || donViTinh.contains("%"))) {
            return quyUoc * quanSo * 0.01;
        }

        return quyUoc * quanSo;
    }

    private static double parseCellTon(Object cell) {
        if (cell == null || "-".equals(cell)) return 0;
        String s = cell.toString().replace(",", ".").replaceAll("<[^>]+>", "");
        return InputValidator.parseDoubleSafe(s);
    }

    /**
     * Bản sao chỉ đọc {@link #miniTableVCHC} (khối I. Toàn d, tấn).
     */
    public static Map<String, Map<String, Double>> getMiniTableVCHCReadOnly() {
        synchronized (MINI_VCHC_LOCK) {
            Map<String, Map<String, Double>> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Double>> e : miniTableVCHC.entrySet()) {
                copy.put(e.getKey(), Map.copyOf(e.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    public static Map<String, Map<String, Map<String, Double>>> getMiniTableVCHCByDirectionReadOnly() {
        synchronized (MINI_VCHC_LOCK) {
            Map<String, Map<String, Map<String, Double>>> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Map<String, Double>>> dirEntry : miniTableVCHCByDirection.entrySet()) {
                Map<String, Map<String, Double>> dirMap = new LinkedHashMap<>();
                for (Map.Entry<String, Map<String, Double>> itemEntry : dirEntry.getValue().entrySet()) {
                    dirMap.put(itemEntry.getKey(), Map.copyOf(itemEntry.getValue()));
                }
                copy.put(dirEntry.getKey(), Collections.unmodifiableMap(dirMap));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    /**
     * Bản sao chỉ đọc {@link #globalTonnageVCHC_ByCat}: TL vận chuyển theo nhóm QN/QY/DT/VTKT.
     */
    public static Map<String, Map<String, Double>> getGlobalTonnageVCHC_ByCatReadOnly() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(globalTonnageVCHC_ByCat));
    }

    private static boolean isDuongSuaThuongBinh(String ten) {
        if (ten == null) {
            return false;
        }
        return ten.trim().equalsIgnoreCase("Đường sữa thương binh");
    }

    /**
     * Ngoại lệ PT: không mang vác — gạch cột PT; VTKT (panel 2) hoặc nhóm VTKT trong VCHC (cat 3).
     * Túi*, Đường sữa* (trừ task sau cho TB). TL ĐVT Toàn d vẫn d+PT.
     */
    private static boolean isPtExcludedVatChat(String ten, int catIndex, int panelType) {
        if (panelType == 2) {
            return true;
        }
        if (ten == null) {
            return false;
        }
        String t = ten.toLowerCase(Locale.ROOT);
        if (t.contains("túi") || t.contains("tui")) {
            return true;
        }
        if (t.contains("đường sữa") || t.contains("duong sua")) {
            return true;
        }
        return catIndex == 3;
    }

    /**
     * Lưu 7 TL bổ sung (tấn) cho từng vật chất vào {@link #miniTableVCHC} và cộng dồn vào {@link #globalTonnageVCHC}.
     * In ra console để kiểm tra (tương tự Tab5_DanPanel).
     */
    private void recordMiniTableVchcRow(
            String tenVatChat,
            int catIndex,
            double tonGdcbKho,
            double tonGdcbDv,
            double tonGdcdKho,
            double tonGdcdDv,
            double tonPlus
    ) {
        if (tenVatChat == null || tenVatChat.isBlank()) {
            return;
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("CATEGORY", (double) catIndex);
        m.put(TL_BO_SUNG_GDCB_KHO, tonGdcbKho);
        m.put(TL_BO_SUNG_GDCB_DV_D, tonGdcbDv);
        m.put(TL_BO_SUNG_GDCD_KHO,  tonGdcdKho);
        m.put(TL_BO_SUNG_GDCD_DV,   tonGdcdDv);
        m.put(TL_BO_SUNG_TOTAL,      tonPlus);
        synchronized (MINI_VCHC_LOCK) {
            miniTableVCHC.put(tenVatChat.trim(), m);
        }
        
        String catPrefix = "VCHC";
        if (catIndex == 0) catPrefix = "QN";
        else if (catIndex == 1) catPrefix = "QY";
        else if (catIndex == 2) catPrefix = "DT";
        else if (catIndex == 3) catPrefix = "VTKT";
        
        System.out.printf(Locale.US,
                "[%s] %-30s | GĐCBKho=%7.3f GĐCBDv=%7.3f | GĐCĐKho=%7.3f GĐCĐDv=%7.3f | (+)=%7.3f (tấn)%n",
                catPrefix, tenVatChat.trim(),
                tonGdcbKho + 0.0, tonGdcbDv + 0.0,
                tonGdcdKho + 0.0, tonGdcdDv + 0.0, tonPlus + 0.0);
    }

    private String f(double value) {
        if (Math.abs(value) < 1e-12) {
            return "0";
        }
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    /**
     * Xuất dữ liệu bảng Vật chất theo keyword từng ô: {{vc_r{row}_c{col}}} (type=1 VCHC)
     * hoặc {{vtkt_r{row}_c{col}}} (type=2 VTKT).
     * <p>
     * Bảng Word template có 80 dòng dữ liệu × 28 ô/dòng (col 0..27).<br>
     * Mapping ô Word ↔ cột UI (27 cột 0..26):
     * <ul>
     *   <li>col 0  → STT (tự tăng)</li>
     *   <li>col 1  → UI col 0  (Chỉ tiêu)</li>
     *   <li>col 2  → UI col 1  (ĐVT)</li>
     *   <li>col 3  → UI col 2  (TL ĐVT Toàn d)</li>
     *   <li>col 4  → UI col 3  (TL ĐVT d)</li>
     *   <li>col 5  → UI col 4  (TL ĐVT PT)</li>
     *   <li>col 6  → UI col 5  (QDDT)</li>
     *   <li>col 7  → UI col 6  (TT GĐCB)</li>
     *   <li>col 8  → UI col 7  (TT GĐCĐ)</li>
     *   <li>col 9  → UI col 8  (PC SCĐ Kho/d)</li>
     *   <li>col 10 → UI col 9  (PC SCĐ ĐV)</li>
     *   <li>col 11 → UI col 10 (PC SCĐ +)</li>
     *   <li>col 12 → UI col 11 (HC Kho d)</li>
     *   <li>col 13 → UI col 12 (HC Kho PT)</li>
     *   <li>col 14 → UI col 13 (HC ĐV d)</li>
     *   <li>col 15 → UI col 14 (HC ĐV PT)</li>
     *   <li>col 16 → UI col 15 (HC +)</li>
     *   <li>col 17 → UI col 16 (BS TQĐ Kho/d)</li>
     *   <li>col 18 → UI col 17 (BS TQĐ ĐV)</li>
     *   <li>col 19 → UI col 18 (BS GĐCB Kho/d)</li>
     *   <li>col 20 → UI col 19 (BS GĐCB ĐV)</li>
     *   <li>col 21 → UI col 20 (BS GĐCĐ Kho/d)</li>
     *   <li>col 22 → UI col 21 (BS GĐCĐ ĐV)</li>
     *   <li>col 23 → UI col 22 (BS +)</li>
     *   <li>col 24 → UI col 23 (KH Thời gian)</li>
     *   <li>col 25 → UI col 24 (KH Địa điểm)</li>
     *   <li>col 26 → UI col 25 (KH Phương thức)</li>
     *   <li>col 27 → UI col 26 (KH Nhiệm vụ)</li>
     * </ul>
     */
    public Map<String, String> getExportData(int type, DefaultTableModel vatChatModel) {
        Map<String, String> data = new HashMap<>();
        final int WORD_MAX_ROWS = 80;   // số dòng dữ liệu trong Word template
        final int WORD_CELLS    = 28;   // 1 TT + 27 cột UI
        final int UI_COLS       = 27;   // số cột trong model UI

        // Prefix keyword theo type
        String prefix = (type == 1) ? "vc" : "vtkt";

        // Lấy danh sách hàng cần xuất
        List<Object[]> exportRows = new ArrayList<>();
        if (vatChatModel != null) {
            for (int i = 0; i < vatChatModel.getRowCount() && exportRows.size() < WORD_MAX_ROWS; i++) {
                Object[] row = new Object[UI_COLS];
                for (int j = 0; j < UI_COLS; j++) {
                    row[j] = vatChatModel.getValueAt(i, j);
                }
                exportRows.add(row);
            }
        }

        // Generate keyword → value cho từng ô trong 80 hàng × 28 cột
        for (int wordRow = 1; wordRow <= WORD_MAX_ROWS; wordRow++) {
            boolean hasData = wordRow <= exportRows.size();
            Object[] uiRow = hasData ? exportRows.get(wordRow - 1) : null;

            for (int wordCell = 0; wordCell < WORD_CELLS; wordCell++) {
                String key = "{{" + prefix + "_r" + wordRow + "_c" + wordCell + "}}";
                String val = "";

                if (hasData) {
                    if (wordCell == 0) {
                        // STT = số thứ tự (1-based)
                        val = String.valueOf(wordRow);
                    } else {
                        // wordCell 1..27 → UI col 0..26
                        int uiCol = wordCell - 1;
                        if (uiCol < UI_COLS) {
                            val = vcCellStr(uiRow[uiCol]);
                        }
                    }
                }
                data.put(key, val);
            }
        }
        return data;
    }

    /** Chuyển giá trị ô model sang String an toàn. Strip HTML tags. Bỏ "-". */
    private static String vcCellStr(Object o) {
        if (o == null) return "";
        String s = o.toString().trim();
        // Strip HTML tags (header rows dùng <html><b>...</b></html>)
        s = s.replaceAll("<[^>]+>", "").trim();
        return "-".equals(s) ? "" : s;
    }
}
