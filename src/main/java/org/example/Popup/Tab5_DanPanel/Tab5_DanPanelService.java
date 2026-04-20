package org.example.Popup.Tab5_DanPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bảng Đạn 29 cột: Toàn d (cộng dồn các Hướng) + từng Hướng; chỉ xem (read-only).
 * Hiện có (cột 11–16): {@code step3_vat_chat} (loai_vat_chat = 1); Kho PT (15) = 0.
 * <p>
 * Tổng TL Toàn d (cột 22, 25, 28) sau build được ghi vào {@link #globalTonnageData} — đầu vào cho các bảng tính
 * khác (vận tải, tiêu hao, …), không chỉ phục vụ xuất Word.
 * Chi tiết từng dòng (loại đạn/vật chất) trong khối Toàn d: {@link #miniTableToanD}.
 */
public class Tab5_DanPanelService {

    public static final int COL_COUNT = 29;

    /** Khóa trong {@link #globalTonnageData} / {@link #miniTableToanD} value: TL cột 22 (KHTN → Trước nổ → ĐV). */
    public static final String TL_TRUOC_NO_DV = "TL_TruocNo_DV";
    /** Khóa trong {@link #globalTonnageData} / {@link #miniTableToanD} value: TL cột 25 (KHTN → Trước nổ → Kho). */
    public static final String TL_TRUOC_NO_KHO = "TL_TruocNo_Kho";
    /** Khóa trong {@link #globalTonnageData} / {@link #miniTableToanD} value: TL cột 28 (KHTN → Thực hành). */
    public static final String TL_THUC_HANH = "TL_ThucHanh";

    /**
     * Trạng thái toàn cục: ba tổng trọng lượng (tấn/logic TL) của khối Toàn d, cập nhật sau mỗi lần
     * {@link #getDanTableModel(int)} chạy xong cộng dồn + làm sạch dòng tổng. Các class khác đọc Map này làm input.
     */
    public static final Map<String, Double> globalTonnageData = new ConcurrentHashMap<>();

    /**
     * Bảng thu nhỏ Toàn d: key = tên loại (cột 0, đã trim), thứ tự giống bảng (LinkedHashMap).
     * Value: ba TL Kế hoạch tiếp nhận — cột 22 / 25 / 28 (đồng bộ header {@link Tab5_DanPanelUI}).
     */
    public static final Map<String, Map<String, Double>> miniTableToanD = new LinkedHashMap<>();

    /**
     * Dữ liệu KHTN chi tiết theo từng Hướng.
     * Cấu trúc: Hướng -> (Tên loại đạn -> Map<TL_TRUOC_NO_..., Giá trị>)
     */
    public static final Map<String, Map<String, Map<String, Double>>> miniTableDanByDirection = new ConcurrentHashMap<>();

    private static final Object MINI_TABLE_LOCK = new Object();

    private static final String TOAN_D_HEADER = "Toàn d";

    private static final String[] DISPLAY_ROW_ORDER = {
            "SMPK 12,7mm",
            "Đạn BB nhóm 2",
            "Cối 100mm",
            "Cối 82mm",
            "Cối 60mm",
            "Súng SPG-9",
            "Đạn BB nhóm 1",
            "Súng B41",
            "Súng đại liên",
            "Súng trung liên",
            "Súng tiểu liên",
            "Súng ngắn",
            "Lựu đạn"
    };

    private static final String[] BB2_SOURCES = {"Cối 100mm", "Cối 82mm", "Cối 60mm", "Súng SPG-9"};
    private static final String[] BB1_SOURCES = {"Súng B41", "Súng đại liên", "Súng trung liên", "Súng tiểu liên", "Súng ngắn"};

    /**
     * 11 dòng vũ khí đơn lẻ (không gồm Đạn BB nhóm 1/2) — dùng cộng TL dòng tiêu đề & tránh nhân đôi.
     */
    private static final String[] LEAF_WEAPON_LABELS_FOR_SECTION_TL = {
            "SMPK 12,7mm",
            "Cối 100mm",
            "Cối 82mm",
            "Cối 60mm",
            "Súng SPG-9",
            "Súng B41",
            "Súng đại liên",
            "Súng trung liên",
            "Súng tiểu liên",
            "Súng ngắn",
            "Lựu đạn"
    };

    /** Cơ số định mức copy từ Toàn d (Bố) → từng Hướng (Con), trừ Lựu đạn. */
    private static final int[] CO_SO_COLUMNS_FROM_TOAN_D = {2, 4, 6, 8, 9, 17, 18};

    /**
     * Ở dòng Hướng: ẩn Kho & PT theo nghiệp vụ (đã tách khỏi hướng).
     */
    private static final int[] DIRECTION_KHO_PT_DISPLAY_MASK = {12, 14, 15, 16, 18, 23, 24, 25, 27};

    private static final Map<String, String> LABEL_TO_BC_COLUMN = new LinkedHashMap<>();

    /** Cơ số định mức (2,4,6,8,9,17,18) theo hướng = nhau; Lựu đạn là ngoại lệ (số lượng thực tế theo chi tiết). */
    private static final String LUA_DAN_LABEL = "Lựu đạn";

    /**
     * Map tĩnh label hiển thị → {@code loai_dan} trong bảng {@code quyuoc_dan}.
     * Dùng làm fallback khi step4 chưa có dữ liệu cho session.
     */
    private static final Map<String, String> LABEL_TO_DEFAULT_LOAI_DAN = new LinkedHashMap<>();

    static {
        LABEL_TO_BC_COLUMN.put("SMPK 12,7mm", "smpk_127mm");
        LABEL_TO_BC_COLUMN.put("Cối 100mm", "co100mm");
        LABEL_TO_BC_COLUMN.put("Cối 82mm", "co82mm");
        LABEL_TO_BC_COLUMN.put("Cối 60mm", "co60mm");
        LABEL_TO_BC_COLUMN.put("Súng SPG-9", "spg9");
        LABEL_TO_BC_COLUMN.put("Súng B41", "b41");
        LABEL_TO_BC_COLUMN.put("Súng đại liên", "dai_lien");
        LABEL_TO_BC_COLUMN.put("Súng trung liên", "trung_lien");
        LABEL_TO_BC_COLUMN.put("Súng tiểu liên", "tieu_lien");
        LABEL_TO_BC_COLUMN.put("Súng ngắn", "sung_ngan");
        LABEL_TO_BC_COLUMN.put("Lựu đạn", "luu_dan");

        LABEL_TO_DEFAULT_LOAI_DAN.put("SMPK 12,7mm",    "Đạn 12.7mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Cối 100mm",      "Đạn Cối 100mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Cối 82mm",       "Đạn Cối 82mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Cối 60mm",       "Đạn Cối 60mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng SPG-9",     "Đạn SPG-9");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng B41",       "Đạn B41");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng đại liên",  "Đạn 7.62mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng trung liên","Đạn 7.62mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng tiểu liên", "Đạn 7.62mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Súng ngắn",      "Đạn 9mm");
        LABEL_TO_DEFAULT_LOAI_DAN.put("Lựu đạn",        "Lựu đạn mỏ vịt");
    }

    /**
     * Dựng model bảng Đạn đầy đủ (Toàn d + từng Hướng).
     * <p>
     * <b>Source of Truth cho xuất Word:</b> {@code DefaultTableModel} trả về đây là nguồn hiển thị đã format sẵn
     * (dòng tổng chỉ còn TL + VK nơi cần); hàm xuất Word sau này nên truyền trực tiếp model này, không cần lọc/parse
     * lại từ {@link #globalTonnageData} — Map toàn cục chỉ phục vụ các mô-đun tính toán khác lấy 3 tổng TL cốt lõi.
     * Chi tiết từng dòng: {@link #miniTableToanD}.
     */
    public DefaultTableModel getDanTableModel(int sessionId) {
        String[] colIds = new String[COL_COUNT];
        Arrays.fill(colIds, "");
        DefaultTableModel model = new DefaultTableModel(colIds, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (sessionId <= 0) {
            synchronized (MINI_TABLE_LOCK) {
                globalTonnageData.clear();
                miniTableToanD.clear();
                miniTableDanByDirection.clear();
            }
            return model;
        }

        synchronized (MINI_TABLE_LOCK) {
            globalTonnageData.clear();
            miniTableToanD.clear();
            miniTableDanByDirection.clear();
        }

        Map<String, Map<String, Integer>> gunByHuong = loadGunCountsByHuong(sessionId);
        List<String> huongs = filterDirections(loadDistinctHuong(sessionId));
        List<Step4DanRow> step4Rows = loadStep4DanRows(sessionId);
        Map<String, Step3DanRow> step3ByVatChat = loadStep3DanMap(sessionId);
        Map<String, double[]> quyUocDan = loadQuyUocDan();

        Map<String, Map<String, Object[]>> leafByHuong = new LinkedHashMap<>();
        for (String huong : huongs) {
            leafByHuong.put(huong, fetchRawDataForDirectionInternal(huong, gunByHuong, step4Rows, step3ByVatChat, quyUocDan));
        }

        ToanDAggregateOutcome toanDAgg = aggregateToanDLeaf(leafByHuong, step4Rows, quyUocDan);
        Map<String, Object[]> toanDLeaf = toanDAgg.displayRows();
        inheritCoSoFromToanDAndRecomputeDirections(leafByHuong, toanDAgg.mergedFullByLabel(), step4Rows, quyUocDan);
        
        populateMiniTableDanByDirection(leafByHuong);

        double tlDv = globalTonnageData.getOrDefault(TL_TRUOC_NO_DV, 0.0);
        double tlKho = globalTonnageData.getOrDefault(TL_TRUOC_NO_KHO, 0.0);
        double tlTh = globalTonnageData.getOrDefault(TL_THUC_HANH, 0.0);
        System.out.println("[Tab5_DanPanelService] Toàn d totals (globalTonnageData): TL_TruocNo_DV=" + tlDv
                + ", TL_TruocNo_Kho=" + tlKho
                + ", TL_ThucHanh=" + tlTh);
        logMiniTableToanD();

        appendDirectionBlock(model, TOAN_D_HEADER, toanDLeaf, false);
        for (String huong : huongs) {
            appendDirectionBlock(model, huong, leafByHuong.get(huong), true);
        }

        return model;
    }

    /**
     * Kết quả cộng dồn Toàn d: bản hiển thị (đã làm sạch dòng súng) + bản merge đầy đủ (để copy cơ số xuống Hướng).
     */
    private record ToanDAggregateOutcome(
            Map<String, Object[]> displayRows,
            Map<String, Object[]> mergedFullByLabel
    ) {
    }

    /**
     * Map nhãn súng → Object[29] (TL / KHTN tạm 0); Hiện có từ step3.
     */
    public Map<String, Object[]> fetchRawDataForDirection(int sessionId, String huong) {
        if (sessionId <= 0 || huong == null) {
            return new HashMap<>();
        }
        return fetchRawDataForDirectionInternal(
                huong.trim(),
                loadGunCountsByHuong(sessionId),
                loadStep4DanRows(sessionId),
                loadStep3DanMap(sessionId),
                loadQuyUocDan()
        );
    }

    private Map<String, Object[]> fetchRawDataForDirectionInternal(
            String huong,
            Map<String, Map<String, Integer>> gunByHuong,
            List<Step4DanRow> step4Rows,
            Map<String, Step3DanRow> step3ByVatChat,
            Map<String, double[]> quyUocDan
    ) {
        Map<String, Object[]> out = new HashMap<>();
        Map<String, Integer> counts = gunByHuong.getOrDefault(huong, Collections.emptyMap());
        int dtIdx = dtIndexForHuong(huong);

        for (String label : LABEL_TO_BC_COLUMN.keySet()) {
            String colBc = LABEL_TO_BC_COLUMN.get(label);
            int vk = counts.getOrDefault(colBc, 0);
            String vatChat = resolveVatChatForLabel(label, step4Rows);
            Step4DanRow s4 = findStep4Row(step4Rows, vatChat);
            Step3DanRow s3 = findStep3Row(step3ByVatChat, vatChat);
            Object[] row = isLuuDan(label)
                    ? buildDataRowLuuDan(padName(label), vk, s4, dtIdx, s3)
                    : buildDataRowDinhMucChung(padName(label), vk, s4, s3);
            applyKhtn(row);
            applyTl(row, vatChat, quyUocDan);
            out.put(label, row);
        }
        return out;
    }

    private static boolean isLuuDan(String weaponLabel) {
        return LUA_DAN_LABEL.equalsIgnoreCase(weaponLabel != null ? weaponLabel.trim() : "");
    }

    /**
     * Index 0: {@code trong_luong_1_vien}; index 1: {@code so_vien_tren_coso} (tối thiểu 1).
     */
    public Map<String, double[]> loadQuyUocDan() {
        Map<String, double[]> map = new HashMap<>();
        String sql = "SELECT loai_dan, trong_luong_1_vien, so_vien_tren_coso FROM quyuoc_dan";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String loai = rs.getString("loai_dan");
                if (loai == null || loai.isBlank()) continue;
                double w1 = rs.getDouble("trong_luong_1_vien");
                double sv = rs.getDouble("so_vien_tren_coso");
                if (sv == 0) {
                    sv = 1;
                }
                map.put(normalizeLoaiDan(loai.trim()), new double[]{w1, sv});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * KHTN cơ số (20–27):
     * <ul>
     *   <li>Trước nổ súng (PC TQĐ định mức chung trừ Hiện có d/PT): ĐV 20=17−11, 21=17−12; Kho 23=18−14, 24=18−15.</li>
     *   <li>Thực hành nổ súng (gom ĐV/Kho; GĐCĐ chỉ cộng ĐV): 26=8+6−17, 27=9−18.</li>
     * </ul>
     * Âm → 0. TL tương ứng tính trong {@link #applyTl} từ cơ số 20,21 / 23,24.
     */
    private void applyKhtn(Object[] r) {
        double v6 = parseCellDouble(r[6]);
        double v8 = parseCellDouble(r[8]);
        double v9 = parseCellDouble(r[9]);
        double v11 = parseCellDouble(r[11]);
        double v12 = parseCellDouble(r[12]);
        double v14 = parseCellDouble(r[14]);
        double v15 = parseCellDouble(r[15]);
        double v17 = parseCellDouble(r[17]);
        double v18 = parseCellDouble(r[18]);

        r[20] = fmtNonNeg(v17 - v11);
        r[21] = fmtNonNeg(v17 - v12);
        r[23] = fmtNonNeg(v18 - v14);
        r[24] = fmtNonNeg(v18 - v15);

        r[26] = fmtNonNeg(v8 + v6 - v17);
        r[27] = fmtNonNeg(v9 - v18);
    }

    /**
     * TL = trong_luong_1_vien × so_vien_tren_coso × Số súng (cột 1) × cơ số tại ô (tuyến tính theo cơ số).
     * KHTN Trước nổ: TL cột 22 = từ tổng cơ số (20+21), cột 25 = (23+24). Thực hành (28): từ (26+27).
     */
    private void applyTl(Object[] r, String vatChat, Map<String, double[]> quyUocDan) {
        double[] q = lookupQuyUocDan(quyUocDan, vatChat);
        double w1 = q[0];
        // Lựu đạn tính theo "Quả": so_vien_tren_coso ép = 1 (dù DB có 0/NULL hay giá trị khác).
        double rPerCs = isLuuDanVatChat(vatChat) ? 1.0 : Math.max(1.0, q[1]);
        double vk = parseVkSafe(r[1]);

        r[3] = computeTlCell(parseCellDouble(r[2]), w1, rPerCs, vk);
        r[5] = computeTlCell(parseCellDouble(r[4]), w1, rPerCs, vk);
        r[7] = computeTlCell(parseCellDouble(r[6]), w1, rPerCs, vk);
        r[10] = computeTlCell(parseCellDouble(r[8]) + parseCellDouble(r[9]), w1, rPerCs, vk);
        r[13] = computeTlCell(parseCellDouble(r[11]) + parseCellDouble(r[12]), w1, rPerCs, vk);
        r[16] = computeTlCell(parseCellDouble(r[14]) + parseCellDouble(r[15]), w1, rPerCs, vk);
        r[19] = computeTlCell(parseCellDouble(r[17]) + parseCellDouble(r[18]), w1, rPerCs, vk);
        r[22] = computeTlCell(parseCellDouble(r[20]) + parseCellDouble(r[21]), w1, rPerCs, vk);
        r[25] = computeTlCell(parseCellDouble(r[23]) + parseCellDouble(r[24]), w1, rPerCs, vk);
        r[28] = computeTlCell(parseCellDouble(r[26]) + parseCellDouble(r[27]), w1, rPerCs, vk);
    }

    /**
     * TL = trong_luong_1_vien × so_vien_tren_coso × Số súng (cột 1) × cơ số tại ô.
     * Cột 1 null/rỗng → vk = 0 → TL = 0. Kết quả TL âm → 0.
     */
    private String computeTlCell(double cs, double w1, double rPerCs, double vk) {
        if (vk == 0 || w1 == 0) {
            return "0";
        }
        if (Math.abs(cs) < 1e-12) {
            return "0";
        }
        double tl = w1 * rPerCs * vk * cs;
        if (tl < 0) {
            tl = 0;
        }
        return fmtTl(tl);
    }

    private double parseVkSafe(Object o) {
        if (o == null) {
            return 0;
        }
        String t = o.toString().trim();
        if (t.isEmpty()) {
            return 0;
        }
        return InputValidator.parseDoubleSafe(t.replace(",", "."));
    }

    private double[] lookupQuyUocDan(Map<String, double[]> quyUocDan, String vatChat) {
        if (vatChat == null || vatChat.isEmpty()) {
            return new double[]{0, 1};
        }
        double[] d = quyUocDan.get(normalizeLoaiDan(vatChat));
        if (d != null) {
            return d;
        }
        for (Map.Entry<String, double[]> e : quyUocDan.entrySet()) {
            if (matchesLabel(e.getKey(), vatChat) || matchesLabel(vatChat, e.getKey())) {
                return e.getValue();
            }
        }
        String nv = normalizeLoaiDan(vatChat);
        for (Map.Entry<String, double[]> e : quyUocDan.entrySet()) {
            String k = e.getKey();
            if (k != null && (nv.contains(k) || k.contains(nv))) {
                return e.getValue();
            }
        }
        return new double[]{0, 1};
    }

    private static boolean isLuuDanVatChat(String vatChat) {
        if (vatChat == null) {
            return false;
        }
        String t = vatChat.trim();
        if (t.isEmpty()) {
            return false;
        }
        // So khớp nhãn hiển thị và fallback theo normalize để chịu được dữ liệu "Luu dan"/khác dấu.
        if (LUA_DAN_LABEL.equalsIgnoreCase(t)) {
            return true;
        }
        String n = t.toLowerCase();
        return n.contains("lựu đạn") || n.contains("luu dan");
    }

    private String fmtNonNeg(double v) {
        if (v < 0) {
            v = 0;
        }
        return fmt(v);
    }

    private static String fmtTl(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /** Các cột TL (2 chữ số thập phân) theo header 29 cột. */
    private static boolean isTlColumn(int c) {
        return c == 3 || c == 5 || c == 7 || c == 10 || c == 13 || c == 16 || c == 19 || c == 22 || c == 25 || c == 28;
    }

    /** KHTN (không phải TL): 20,21,23,24,26,27 — trước applyKhtn gán 0 khi cộng Toàn d vũ khí thường. */
    private static boolean isKhtnOnlyColumn(int c) {
        return c == 20 || c == 21 || c == 23 || c == 24 || c == 26 || c == 27;
    }

    /**
     * Bản sao chỉ đọc {@link #globalTonnageData} (an toàn khi luồng khác đọc song song lúc build bảng).
     */
    public static Map<String, Double> getGlobalTonnageDataReadOnly() {
        return Map.copyOf(globalTonnageData);
    }

    /**
     * Bản sao chỉ đọc {@link #miniTableToanD} (thứ tự bảo toàn).
     */
    public static Map<String, Map<String, Double>> getMiniTableToanDReadOnly() {
        synchronized (MINI_TABLE_LOCK) {
            Map<String, Map<String, Double>> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Double>> e : miniTableToanD.entrySet()) {
                copy.put(e.getKey(), Map.copyOf(e.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    public static Map<String, Map<String, Map<String, Double>>> getMiniTableDanByDirectionReadOnly() {
        synchronized (MINI_TABLE_LOCK) {
            Map<String, Map<String, Map<String, Double>>> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Map<String, Double>>> dirEntry : miniTableDanByDirection.entrySet()) {
                Map<String, Map<String, Double>> dirMap = new LinkedHashMap<>();
                for (Map.Entry<String, Map<String, Double>> itemEntry : dirEntry.getValue().entrySet()) {
                    dirMap.put(itemEntry.getKey(), Map.copyOf(itemEntry.getValue()));
                }
                copy.put(dirEntry.getKey(), Collections.unmodifiableMap(dirMap));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    private void logMiniTableToanD() {
        synchronized (MINI_TABLE_LOCK) {
            if (miniTableToanD.isEmpty()) {
                System.out.println("[Tab5_DanPanelService] miniTableToanD: (empty)");
                return;
            }
            System.out.println("[Tab5_DanPanelService] ========== miniTableToanD (Toàn d — TL KHTN cột 22/25/28) ==========");
            for (Map.Entry<String, Map<String, Double>> e : miniTableToanD.entrySet()) {
                Map<String, Double> m = e.getValue();
                System.out.printf(Locale.US, "  %-32s | %s: %8.2f | %s: %8.2f | %s: %8.2f%n",
                        e.getKey(),
                        TL_TRUOC_NO_DV, m.getOrDefault(TL_TRUOC_NO_DV, 0.0),
                        TL_TRUOC_NO_KHO, m.getOrDefault(TL_TRUOC_NO_KHO, 0.0),
                        TL_THUC_HANH, m.getOrDefault(TL_THUC_HANH, 0.0));
            }
            System.out.println("[Tab5_DanPanelService] ===================================================================");
        }
    }

    /**
     * Cộng ngang Toàn d: vũ khí thường — không cộng định mức (2,4,6,8,9,17,18) và không cộng KHTN (20–27);
     * cộng VK, hiện có (11,12,14,15), TL; KHTN tính lại bằng applyKhtn + applyTl. Lựu đạn: cộng mọi cột số liệu gồm CS và KHTN.
     * Lưu bản merge đầy đủ trước khi làm sạch dòng hiển thị — phục vụ {@link #inheritCoSoFromToanDAndRecomputeDirections}.
     */
    private ToanDAggregateOutcome aggregateToanDLeaf(
            Map<String, Map<String, Object[]>> leafByHuong,
            List<Step4DanRow> step4Rows,
            Map<String, double[]> quyUocDan
    ) {
        Map<String, Object[]> display = new LinkedHashMap<>();
        Map<String, Object[]> mergedFullByLabel = new LinkedHashMap<>();
        List<String> huongs = new ArrayList<>(leafByHuong.keySet());
        if (huongs.isEmpty()) {
            for (String label : LABEL_TO_BC_COLUMN.keySet()) {
                Object[] row = emptyDataRow(padName(label));
                mergedFullByLabel.put(label, Arrays.copyOf(row, COL_COUNT));
                display.put(label, row);
            }
            populateGlobalTonnageDataFromToanD(display);
            return new ToanDAggregateOutcome(display, mergedFullByLabel);
        }

        for (String label : LABEL_TO_BC_COLUMN.keySet()) {
            List<Object[]> rows = new ArrayList<>();
            for (String h : huongs) {
                Map<String, Object[]> m = leafByHuong.get(h);
                Object[] r = m != null ? m.get(label) : null;
                rows.add(r != null ? r : emptyDataRow(padName(label)));
            }
            Object[] merged = mergeRowsToToanD(rows, label, step4Rows, quyUocDan);
            mergedFullByLabel.put(label, Arrays.copyOf(merged, COL_COUNT));
            display.put(label, merged);
        }
        populateGlobalTonnageDataFromToanD(display);
        return new ToanDAggregateOutcome(display, mergedFullByLabel);
    }

    /**
     * Phase 1: Copy cơ số từ merge Toàn d (trước khi clear ô hiển thị) xuống từng dòng Hướng (trừ Lựu đạn).
     * Phase 2: Gạch Kho/PT ở Hướng. Luôn tính lại KHTN + TL sau khi áp cơ số Bố.
     */
    private void inheritCoSoFromToanDAndRecomputeDirections(
            Map<String, Map<String, Object[]>> leafByHuong,
            Map<String, Object[]> toanDMergedFullByLabel,
            List<Step4DanRow> step4Rows,
            Map<String, double[]> quyUocDan
    ) {
        if (leafByHuong == null || toanDMergedFullByLabel == null) {
            return;
        }
        for (Map<String, Object[]> dirMap : leafByHuong.values()) {
            if (dirMap == null) {
                continue;
            }
            for (String label : LABEL_TO_BC_COLUMN.keySet()) {
                Object[] row = dirMap.get(label);
                if (row == null) {
                    continue;
                }
                if (!isLuuDan(label)) {
                    Object[] parentMerged = toanDMergedFullByLabel.get(label);
                    if (parentMerged != null) {
                        copyCoSoFromToanDMerged(parentMerged, row);
                    }
                }
                String vatChat = resolveVatChatForLabel(label, step4Rows);
                applyKhtn(row);
                applyTl(row, vatChat, quyUocDan);
            }
        }
    }

    private static void copyCoSoFromToanDMerged(Object[] parentMergedFull, Object[] directionRow) {
        for (int c : CO_SO_COLUMNS_FROM_TOAN_D) {
            directionRow[c] = parentMergedFull[c] != null ? parentMergedFull[c].toString() : "0";
        }
    }

    private static void applyDirectionKhoPtDisplayMask(Object[] row) {
        for (int c : DIRECTION_KHO_PT_DISPLAY_MASK) {
            row[c] = "-";
        }
    }

    /**
     * Dòng tiêu đề khối (Toàn d / Hướng…):
     * - Cột 1 = tổng VK của 11 súng đơn (không gồm BB nhóm 1/2)
     * - Các cột TL = SUM TL 11 súng đơn
     * - Toàn bộ cột cơ số khác = rỗng (không hiển thị 0 ở dòng tổng)
     */
    private Object[] buildSectionHeaderRow(String headerTitle, Map<String, Object[]> leafMap) {
        Object[] r = new Object[COL_COUNT];
        Arrays.fill(r, "");
        r[0] = headerTitle;
        long sumVk = 0;
        for (String lab : LEAF_WEAPON_LABELS_FOR_SECTION_TL) {
            Object[] row = leafMap.get(lab);
            if (row != null) {
                sumVk += parseCellLong(row[1]);
            }
        }
        r[1] = String.valueOf(sumVk);
        for (int c = 2; c < COL_COUNT; c++) {
            if (isTlColumn(c)) {
                double s = 0;
                for (String lab : LEAF_WEAPON_LABELS_FOR_SECTION_TL) {
                    Object[] row = leafMap.get(lab);
                    if (row != null) {
                        s += parseCellDouble(row[c]);
                    }
                }
                r[c] = fmtTl(s);
            }
        }
        clearCoSoForTotalsRow(r, true);
        return r;
    }

    private Object[] mergeRowsToToanD(
            List<Object[]> rows,
            String weaponLabel,
            List<Step4DanRow> step4Rows,
            Map<String, double[]> quyUocDan
    ) {
        if (rows.isEmpty()) {
            return emptyDataRow("");
        }
        if (isLuuDan(weaponLabel)) {
            return mergeRowsToToanDLuuDan(rows);
        }
        return mergeRowsToToanDRegular(rows, weaponLabel, step4Rows, quyUocDan);
    }

    /** Lựu đạn: cộng dồn mọi cột số (gồm cơ số và KHTN), giống tổng các hướng. */
    private Object[] mergeRowsToToanDLuuDan(List<Object[]> rows) {
        Object[] first = rows.get(0);
        String col0 = first[0] != null ? first[0].toString() : "";
        Object[] t = emptyNumericRow(col0);

        long sumVk = 0;
        for (Object[] row : rows) {
            sumVk += parseCellLong(row[1]);
        }
        t[1] = String.valueOf(sumVk);

        for (int c = 2; c < COL_COUNT; c++) {
            double s = 0;
            for (Object[] row : rows) {
                s += parseCellDouble(row[c]);
            }
            t[c] = isTlColumn(c) ? fmtTl(s) : fmt(s);
        }
        return t;
    }

    /**
     * Vũ khí thường: định mức = hàng đầu; cộng VK, hiện có 11,12,14,15, TL; KHTN (20,21,23,24,26,27) tính lại sau applyKhtn + applyTl.
     */
    private Object[] mergeRowsToToanDRegular(
            List<Object[]> rows,
            String weaponLabel,
            List<Step4DanRow> step4Rows,
            Map<String, double[]> quyUocDan
    ) {
        Object[] first = rows.get(0);
        String col0 = first[0] != null ? first[0].toString() : "";
        Object[] t = emptyNumericRow(col0);

        long sumVk = 0;
        for (Object[] row : rows) {
            sumVk += parseCellLong(row[1]);
        }
        t[1] = String.valueOf(sumVk);

        int[] csCols = {2, 4, 6, 8, 9, 17, 18};
        for (int c : csCols) {
            t[c] = first[c] != null ? first[c].toString() : "0";
        }

        int[] hienCoSo = {11, 12, 14, 15};
        for (int c : hienCoSo) {
            double s = 0;
            for (Object[] row : rows) {
                s += parseCellDouble(row[c]);
            }
            t[c] = fmt(s);
        }

        for (int c = 2; c < COL_COUNT; c++) {
            if (contains(csCols, c) || contains(hienCoSo, c)) {
                continue;
            }
            if (isTlColumn(c)) {
                double s = 0;
                for (Object[] row : rows) {
                    s += parseCellDouble(row[c]);
                }
                t[c] = fmtTl(s);
            } else if (isKhtnOnlyColumn(c)) {
                t[c] = "0";
            }
        }

        applyKhtn(t);
        String vatChat = resolveVatChatForLabel(weaponLabel, step4Rows);
        applyTl(t, vatChat, quyUocDan);
        return t;
    }

    /**
     * Dòng tổng hợp (Toàn d / các dòng tổng súng ở Toàn d): xóa toàn bộ cột cơ số để tránh rối mắt/đúng nghiệp vụ;
     * giữ cột 0 (tên), tùy chọn cột 1 (VK); các cột TL giữ nguyên (đã là SUM từ cộng dồn).
     */
    private void clearTotalsRowKeepVkAndTl(Object[] r, boolean keepVkCount) {
        if (r == null) {
            return;
        }
        clearCoSoForTotalsRow(r, keepVkCount);
    }

    /**
     * Xóa (set = "") toàn bộ các cột cơ số ở dòng tổng, chỉ giữ cột 1 và các cột TL.
     * Danh sách cột cơ số theo yêu cầu khách hàng.
     */
    private static void clearCoSoForTotalsRow(Object[] r, boolean keepVkCount) {
        if (r == null) {
            return;
        }
        int[] clearCols = {2, 4, 6, 8, 9, 11, 12, 14, 15, 17, 18, 20, 21, 23, 24, 26, 27};
        for (int c : clearCols) {
            if (c == 1 && keepVkCount) {
                continue;
            }
            if (c >= 0 && c < COL_COUNT) {
                r[c] = "";
            }
        }
        // Các cột TL luôn giữ nguyên; cột 1 giữ nếu keepVkCount=true (mặc định không đụng ở đây).
    }

    /**
     * Sau cộng dồn Toàn d: (1) điền {@link #miniTableToanD} theo thứ tự dòng loại đạn + 2 dòng BB (cùng khối Toàn d);
     * (2) cộng TL cột 22 / 25 / 28 trên các dòng lá (không gồm dòng BB) vào {@link #globalTonnageData}.
     */
    private void populateGlobalTonnageDataFromToanD(Map<String, Object[]> toanDLeaf) {
        double s22 = 0;
        double s25 = 0;
        double s28 = 0;
        synchronized (MINI_TABLE_LOCK) {
            miniTableToanD.clear();
            if (toanDLeaf != null) {
                for (String label : LABEL_TO_BC_COLUMN.keySet()) {
                    Object[] row = toanDLeaf.get(label);
                    if (row == null) {
                        continue;
                    }
                    s22 += parseCellDouble(row[22]);
                    s25 += parseCellDouble(row[25]);
                    s28 += parseCellDouble(row[28]);
                    putMiniTableRow(row);
                }
                Map<String, Object[]> leafMap = new LinkedHashMap<>();
                for (String label : LABEL_TO_BC_COLUMN.keySet()) {
                    Object[] row = toanDLeaf.get(label);
                    leafMap.put(label, row != null ? row : emptyDataRow(padName(label)));
                }
                putMiniTableRow(aggregateGroupRow("      Đạn BB nhóm 2", BB2_SOURCES, leafMap));
                putMiniTableRow(aggregateGroupRow("      Đạn BB nhóm 1", BB1_SOURCES, leafMap));
            }
        }
        globalTonnageData.put(TL_TRUOC_NO_DV, s22);
        globalTonnageData.put(TL_TRUOC_NO_KHO, s25);
        globalTonnageData.put(TL_THUC_HANH, s28);
    }

    /** Cột 0 = tên; cột 22/25/28 = TL KHTN (Kế hoạch tiếp nhận — Trước nổ ĐV / Trước nổ Kho / Thực hành). */
    private void putMiniTableRow(Object[] row) {
        if (row == null) {
            return;
        }
        String tenVatChat = row[0] != null ? row[0].toString().strip() : "";
        if (tenVatChat.isEmpty()) {
            return;
        }
        Map<String, Double> tlParams = new LinkedHashMap<>();
        tlParams.put(TL_TRUOC_NO_DV, parseCellDouble(row[22]));
        tlParams.put(TL_TRUOC_NO_KHO, parseCellDouble(row[25]));
        tlParams.put(TL_THUC_HANH, parseCellDouble(row[28]));
        miniTableToanD.put(tenVatChat, tlParams);
    }

    private static boolean contains(int[] arr, int v) {
        for (int x : arr) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }

    /** Nếu mọi ô cùng giá trị số → giữ một bản (tránh nhân N khi Hiện có trùng). Ngược lại → cộng. */
    private String sumOrFirstIfAllEqual(List<Object[]> rows, int col) {
        if (rows.isEmpty()) {
            return "0";
        }
        double first = parseCellDouble(rows.get(0)[col]);
        boolean allSame = true;
        double sum = 0;
        for (Object[] row : rows) {
            double v = parseCellDouble(row[col]);
            sum += v;
            if (Math.abs(v - first) > 1e-9) {
                allSame = false;
            }
        }
        if (allSame) {
            return rows.get(0)[col] != null ? rows.get(0)[col].toString() : "0";
        }
        return fmt(sum);
    }

    private long parseCellLong(Object o) {
        if (o == null) {
            return 0;
        }
        String t = o.toString().trim();
        if (t.isEmpty() || "-".equals(t)) {
            return 0;
        }
        try {
            return Long.parseLong(t.replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            return (long) InputValidator.parseDoubleSafe(t.replace(",", "."));
        }
    }

    /**
     * @param applyHuongKhoPtMask true: khối Hướng — tiêu đề lấy TL từ bản chưa mask; các dòng đưa vào model là bản sao có gạch "-".
     */
    private void appendDirectionBlock(DefaultTableModel model, String headerTitle, Map<String, Object[]> leaf, boolean applyHuongKhoPtMask) {
        Map<String, Object[]> leafMap = new LinkedHashMap<>();
        for (String label : LABEL_TO_BC_COLUMN.keySet()) {
            Object[] row = leaf.get(label);
            leafMap.put(label, row != null ? row : emptyDataRow(padName(label)));
        }
        model.addRow(buildSectionHeaderRow(headerTitle, leafMap));
        Object[] bb2 = aggregateGroupRow("      Đạn BB nhóm 2", BB2_SOURCES, leafMap);
        Object[] bb1 = aggregateGroupRow("      Đạn BB nhóm 1", BB1_SOURCES, leafMap);
        // Dòng nhóm BB là dòng tổng hợp: xóa toàn bộ cột cơ số (giữ TL).
        clearCoSoForTotalsRow(bb2, true);
        clearCoSoForTotalsRow(bb1, true);
        if (applyHuongKhoPtMask) {
            applyDirectionKhoPtDisplayMask(bb2);
            applyDirectionKhoPtDisplayMask(bb1);
        }
        for (String label : DISPLAY_ROW_ORDER) {
            if ("Đạn BB nhóm 2".equals(label)) {
                model.addRow(bb2);
            } else if ("Đạn BB nhóm 1".equals(label)) {
                model.addRow(bb1);
            } else {
                Object[] src = leafMap.get(label);
                model.addRow(applyHuongKhoPtMask ? copyRowApplyHuongMask(src) : src);
            }
        }
    }

    private static Object[] copyRowApplyHuongMask(Object[] src) {
        if (src == null) {
            return emptyDataRowStatic("");
        }
        Object[] c = Arrays.copyOf(src, COL_COUNT);
        applyDirectionKhoPtDisplayMask(c);
        return c;
    }

    private static Object[] emptyDataRowStatic(String col0) {
        Object[] r = new Object[COL_COUNT];
        Arrays.fill(r, "");
        r[0] = col0;
        for (int i = 1; i < COL_COUNT; i++) {
            r[i] = "0";
        }
        return r;
    }

    /** Loại bỏ hướng trùng tên khối Toàn d (nếu có trong DB). */
    private List<String> filterDirections(List<String> huongs) {
        List<String> out = new ArrayList<>();
        for (String h : huongs) {
            if (h == null) continue;
            String t = h.trim();
            if (t.equalsIgnoreCase(TOAN_D_HEADER) || t.toLowerCase().startsWith("toàn d")) {
                continue;
            }
            out.add(t);
        }
        return out;
    }

    public void loadDataFromDatabase(int sessionId, DefaultTableModel danModel) {
        if (danModel == null) {
            return;
        }
        DefaultTableModel fresh = getDanTableModel(sessionId);
        danModel.setRowCount(0);
        danModel.setColumnCount(fresh.getColumnCount());
        for (int i = 0; i < fresh.getRowCount(); i++) {
            danModel.addRow(copyRow(fresh, i));
        }
    }

    private static Object[] copyRow(DefaultTableModel m, int row) {
        Object[] r = new Object[COL_COUNT];
        for (int c = 0; c < COL_COUNT; c++) {
            r[c] = m.getValueAt(row, c);
        }
        return r;
    }

    private String padName(String label) {
        return "      " + label.trim();
    }

    private List<String> loadDistinctHuong(int sessionId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT TRIM(huong) AS huong FROM step2_bien_che WHERE session_id = ? AND huong IS NOT NULL ORDER BY huong";
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

    private Map<String, Map<String, Integer>> loadGunCountsByHuong(int sessionId) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        String sql = "SELECT TRIM(s.huong) AS huong, " +
                "SUM(q.smpk_127mm) AS smpk_127mm, SUM(q.co100mm) AS co100mm, SUM(q.co82mm) AS co82mm, SUM(q.co60mm) AS co60mm, " +
                "SUM(q.spg9) AS spg9, SUM(q.b41) AS b41, SUM(q.dai_lien) AS dai_lien, SUM(q.trung_lien) AS trung_lien, " +
                "SUM(q.tieu_lien) AS tieu_lien, SUM(q.sung_ngan) AS sung_ngan, SUM(q.luu_dan) AS luu_dan " +
                "FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ? " +
                "GROUP BY TRIM(s.huong)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String huong = rs.getString("huong");
                    if (huong == null) continue;
                    huong = huong.trim();
                    Map<String, Integer> m = new HashMap<>();
                    m.put("smpk_127mm", rs.getInt("smpk_127mm"));
                    m.put("co100mm", rs.getInt("co100mm"));
                    m.put("co82mm", rs.getInt("co82mm"));
                    m.put("co60mm", rs.getInt("co60mm"));
                    m.put("spg9", rs.getInt("spg9"));
                    m.put("b41", rs.getInt("b41"));
                    m.put("dai_lien", rs.getInt("dai_lien"));
                    m.put("trung_lien", rs.getInt("trung_lien"));
                    m.put("tieu_lien", rs.getInt("tieu_lien"));
                    m.put("sung_ngan", rs.getInt("sung_ngan"));
                    m.put("luu_dan", rs.getInt("luu_dan"));
                    map.put(huong, m);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private Map<String, Step3DanRow> loadStep3DanMap(int sessionId) {
        Map<String, Step3DanRow> map = new HashMap<>();
        String sql = "SELECT vat_chat, don_vi, phoi_thuoc, kho_d FROM step3_vat_chat WHERE session_id = ? AND loai_vat_chat = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Step3DanRow r = new Step3DanRow();
                    r.vatChat = rs.getString("vat_chat") != null ? rs.getString("vat_chat").trim() : "";
                    r.donVi = rs.getDouble("don_vi");
                    r.phoiThuoc = rs.getDouble("phoi_thuoc");
                    r.khoD = rs.getDouble("kho_d");
                    if (!r.vatChat.isEmpty()) {
                        map.put(normalizeLoaiDan(r.vatChat), r);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private Step3DanRow findStep3Row(Map<String, Step3DanRow> byNorm, String vatChat) {
        if (vatChat == null || vatChat.isEmpty()) return null;
        Step3DanRow direct = byNorm.get(normalizeLoaiDan(vatChat));
        if (direct != null) return direct;
        for (Map.Entry<String, Step3DanRow> e : byNorm.entrySet()) {
            if (matchesLabel(e.getValue().vatChat, vatChat)) {
                return e.getValue();
            }
        }
        return null;
    }

    private List<Step4DanRow> loadStep4DanRows(int sessionId) {
        List<Step4DanRow> list = new ArrayList<>();
        String sql = "SELECT vat_chat, dvt, du_tru, phai_co_0400, phai_co_scd, tieu_thu_gdcb, tieu_thu_gdcd, "
                + "dt_chitiet, pc04_chitiet, scd_chitiet, gdcb_chitiet, gdcd_chitiet "
                + "FROM step4_quy_dinh_du_tru WHERE session_id = ? AND loai_vat_chat = 1 ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Step4DanRow r = new Step4DanRow();
                    r.vatChat = rs.getString("vat_chat") != null ? rs.getString("vat_chat").trim() : "";
                    r.dvt = rs.getString("dvt") != null ? rs.getString("dvt") : "";
                    r.duTru = rs.getDouble("du_tru");
                    r.phaiCo0400 = rs.getDouble("phai_co_0400");
                    r.phaiCoScd = rs.getDouble("phai_co_scd");
                    r.tieuThuGdcb = rs.getDouble("tieu_thu_gdcb");
                    r.tieuThuGdcd = rs.getDouble("tieu_thu_gdcd");
                    r.dtChitiet = rs.getString("dt_chitiet");
                    r.pc04Chitiet = rs.getString("pc04_chitiet");
                    r.scdChitiet = rs.getString("scd_chitiet");
                    r.gdcbChitiet = rs.getString("gdcb_chitiet");
                    r.gdcdChitiet = rs.getString("gdcd_chitiet");
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Step4DanRow findStep4Row(List<Step4DanRow> rows, String vatChat) {
        if (vatChat == null || vatChat.isEmpty()) return null;
        String norm = normalizeLoaiDan(vatChat);
        for (Step4DanRow r : rows) {
            if (normalizeLoaiDan(r.vatChat).equals(norm)) {
                return r;
            }
        }
        return null;
    }

    private String resolveVatChatForLabel(String label, List<Step4DanRow> rows) {
        for (Step4DanRow r : rows) {
            if (matchesLabel(r.vatChat, label)) {
                return r.vatChat;
            }
        }
        // Fallback: map tĩnh label → loai_dan mặc định khi step4 chưa có dữ liệu
        String defaultLoaiDan = LABEL_TO_DEFAULT_LOAI_DAN.get(label != null ? label.trim() : "");
        return defaultLoaiDan != null ? defaultLoaiDan : label;
    }

    private boolean matchesLabel(String loaiDan, String label) {
        if (loaiDan == null) return false;
        String a = normalizeLoaiDan(loaiDan);
        String b = normalizeLoaiDan(label);
        if (a.equals(b)) return true;
        if (a.contains(b) || b.contains(a)) return true;
        if ("Cối 100mm".equals(label) && a.contains("100") && a.contains("cối")) return true;
        if ("Cối 82mm".equals(label) && a.contains("82")) return true;
        if ("Cối 60mm".equals(label) && a.contains("60") && a.contains("cối")) return true;
        return false;
    }

    private String normalizeLoaiDan(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private int dtIndexForHuong(String huong) {
        if (huong == null) return -1;
        String h = huong.trim();
        if (h.startsWith("Toàn") || h.contains("Toàn d")) return 0;
        if (h.contains("chủ yếu")) return 2;
        if (h.contains("thứ yếu")) return 3;
        if (h.contains("phía sau") || h.contains("Phòng ngự phía sau")) return 4;
        if (h.contains("còn lại")) return 5;
        if (h.contains("Phối thuộc")) return 5;
        return -1;
    }

    private double[] parseArray(String arrStr) {
        double[] res = new double[6];
        if (arrStr != null && !arrStr.isEmpty()) {
            String[] arr = arrStr.split(",");
            for (int i = 0; i < Math.min(6, arr.length); i++) {
                res[i] = InputValidator.parseDoubleSafe(arr[i]);
            }
        }
        return res;
    }

    /**
     * Lựu đạn: cơ số theo chi tiết từng hướng (dtIdx + *_chitiet), giống Tab4.
     * step3: Hiện có 11–16.
     */
    private Object[] buildDataRowLuuDan(String col0, int numVk, Step4DanRow s4, int dtIdx, Step3DanRow s3) {
        Object[] r = emptyNumericRow(col0);
        r[1] = String.valueOf(numVk);

        if (s4 != null) {
            double[] dt = parseArray(s4.dtChitiet);
            double[] pc = parseArray(s4.pc04Chitiet);
            double[] scd = parseArray(s4.scdChitiet);
            double[] gdcb = parseArray(s4.gdcbChitiet);
            double[] gdcd = parseArray(s4.gdcdChitiet);

            r[2] = fmt(nhuCauCoSoFromDuTru(dt, s4.duTru, dtIdx));
            r[3] = "0";
            r[4] = fmt(tieuThuPlusChitiet(s4.tieuThuGdcb, gdcb, dtIdx));
            r[5] = "0";
            r[6] = fmt(tieuThuPlusChitiet(s4.tieuThuGdcd, gdcd, dtIdx));
            r[7] = "0";

            double[] dvKhoScd = phaiCoDvKhoFromChitiet(scd, s4.phaiCoScd, dtIdx);
            r[8] = fmt(dvKhoScd[0]);
            r[9] = fmt(dvKhoScd[1]);
            r[10] = "0";

            double[] dvKho04 = phaiCoDvKhoFromChitiet(pc, s4.phaiCo0400, dtIdx);
            r[17] = fmt(dvKho04[0]);
            r[18] = fmt(dvKho04[1]);
            r[19] = "0";

            for (int c = 20; c < COL_COUNT; c++) {
                r[c] = "0";
            }
        }

        applyHienCoFromStep3(r, s3);
        return r;
    }

    /**
     * Vũ khí thường: định mức từ {@code step4_quy_dinh_du_tru} (scalar) — giống Toàn d và mọi Hướng; không dùng ô chi tiết theo hướng.
     */
    private Object[] buildDataRowDinhMucChung(String col0, int numVk, Step4DanRow s4, Step3DanRow s3) {
        Object[] r = emptyNumericRow(col0);
        r[1] = String.valueOf(numVk);
        if (s4 != null) {
            double[] pc = parseArray(s4.pc04Chitiet);
            double[] scd = parseArray(s4.scdChitiet);
            r[2] = fmt(s4.duTru);
            r[3] = "0";
            r[4] = fmt(s4.tieuThuGdcb);
            r[5] = "0";
            r[6] = fmt(s4.tieuThuGdcd);
            r[7] = "0";
            double[] dvKhoScd = phaiCoDvKhoFromChitiet(scd, s4.phaiCoScd, -1);
            r[8] = fmt(dvKhoScd[0]);
            r[9] = fmt(dvKhoScd[1]);
            r[10] = "0";
            double[] dvKho04 = phaiCoDvKhoFromChitiet(pc, s4.phaiCo0400, -1);
            r[17] = fmt(dvKho04[0]);
            r[18] = fmt(dvKho04[1]);
            r[19] = "0";
            for (int c = 20; c < COL_COUNT; c++) {
                r[c] = "0";
            }
        }
        applyHienCoFromStep3(r, s3);
        return r;
    }

    /**
     * Cột 2: phần tử dt_chitiet theo hướng; nếu cả chuỗi phân cấp = 0 thì dùng du_tru (một giá trị tổng).
     */
    private double nhuCauCoSoFromDuTru(double[] dt, double duTru, int dtIdx) {
        double slot = (dtIdx >= 0 && dtIdx < 6) ? dt[dtIdx] : 0;
        if (Math.abs(slot) > 1e-12) {
            return slot;
        }
        if (Math.abs(duTru) < 1e-12) {
            return 0;
        }
        boolean allDtZero = true;
        for (double d : dt) {
            if (Math.abs(d) > 1e-12) {
                allDtZero = false;
                break;
            }
        }
        return allDtZero ? duTru : 0;
    }

    private double tieuThuPlusChitiet(double base, double[] chitiet6, int dtIdx) {
        double slot = (dtIdx >= 0 && dtIdx < 6) ? chitiet6[dtIdx] : 0;
        return base + slot;
    }

    /**
     * PC SCĐ (8,9) và PC TQĐ (17,18): từ phai_co_* + pc04/scd_chitiet (6 phần, đồng bộ Tab4).
     * Nếu tại ô hướng có giá trị &gt; 0: chia đều ĐV/Kho (tránh dồn hết vào cột 17).
     * Nếu ô hướng = 0 nhưng tổng phai_co_* &gt; 0: chia đều tổng (fallback khi thiếu chi tiết).
     */
    private double[] phaiCoDvKhoFromChitiet(double[] chitiet6, double phaiCoTong, int dtIdx) {
        double slot = (dtIdx >= 0 && dtIdx < 6) ? chitiet6[dtIdx] : 0;
        if (Math.abs(slot) > 1e-12) {
            return new double[]{slot * 0.5, slot * 0.5};
        }
        if (Math.abs(phaiCoTong) < 1e-12) {
            return new double[]{0, 0};
        }
        return new double[]{phaiCoTong * 0.5, phaiCoTong * 0.5};
    }

    private void applyHienCoFromStep3(Object[] r, Step3DanRow s3) {
        if (s3 == null) {
            r[11] = "0";
            r[12] = "0";
            r[13] = "0";
            r[14] = "0";
            r[15] = "0";
            r[16] = "0";
            return;
        }
        r[11] = fmt(s3.donVi);
        r[12] = fmt(s3.phoiThuoc);
        r[13] = "0";
        r[14] = fmt(s3.khoD);
        r[15] = "0";
        r[16] = "0";
    }

    private Object[] emptyNumericRow(String col0) {
        Object[] r = new Object[COL_COUNT];
        Arrays.fill(r, "");
        r[0] = col0;
        for (int c = 1; c < COL_COUNT; c++) {
            r[c] = "0";
        }
        return r;
    }

    private Object[] emptyDataRow(String col0) {
        return emptyNumericRow(col0);
    }

    /**
     * Cộng dồn nhóm đạn (BB): SUM tất cả cột TL (3,5,7,10,13,16,19,22,25,28) từ dòng con; các cột khác = 0.
     */
    private Object[] aggregateGroupRow(String col0, String[] sources, Map<String, Object[]> leafRows) {
        Object[] sum = new Object[COL_COUNT];
        Arrays.fill(sum, "");
        sum[0] = col0;
        sum[1] = "";
        for (int c = 2; c < COL_COUNT; c++) {
            if (!isTlColumn(c)) {
                continue;
            }
            double s = 0;
            for (String src : sources) {
                Object[] row = leafRows.get(src);
                if (row == null) {
                    continue;
                }
                s += parseCellDouble(row[c]);
            }
            sum[c] = fmtTl(s);
        }
        return sum;
    }

    private double parseCellDouble(Object o) {
        if (o == null) {
            return 0;
        }
        String t = o.toString().trim().replace(",", ".");
        if (t.isEmpty() || "-".equals(t)) {
            return 0;
        }
        return InputValidator.parseDoubleSafe(t);
    }

    private String fmt(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value);
    }

    /**
     * Xuất dữ liệu bảng Đạn theo keyword từng ô: {{dan_r{row}_c{cell}}}.
     * <p>
     * Bảng Word template có 40 dòng dữ liệu (row 1..40) × 35 ô (cell 0..34).
     * Mapping ô Word ↔ cột UI (29 cột 0..28):
     * <ul>
     *   <li>cell 0  → STT (số thứ tự tự tăng theo dòng UI)</li>
     *   <li>cell 1  → UI col 0  (Loại đạn)</li>
     *   <li>cell 2  → UI col 1  (Số lượng VK)</li>
     *   <li>cell 3  → UI col 2  (Nhu cầu CS)</li>
     *   <li>cell 4  → UI col 3  (Nhu cầu TL)</li>
     *   <li>cell 5  → UI col 4  (GĐCB CS)</li>
     *   <li>cell 6  → UI col 5  (GĐCB TL)</li>
     *   <li>cell 7  → UI col 6  (GĐCĐ CS)</li>
     *   <li>cell 8  → UI col 7  (GĐCĐ TL)</li>
     *   <li>cell 9  → UI col 8  (PC SCĐ ĐV)</li>
     *   <li>cell 10 → UI col 9  (PC SCĐ Kho)</li>
     *   <li>cell 11 → UI col 10 (PC SCĐ TL)</li>
     *   <li>cell 12 → UI col 11 (Hiện có ĐV d)</li>
     *   <li>cell 13 → UI col 12 (Hiện có ĐV PT)</li>
     *   <li>cell 14 → UI col 13 (Hiện có ĐV TL)</li>
     *   <li>cell 15 → UI col 14 (Hiện có Kho d)</li>
     *   <li>cell 16 → UI col 15 (Hiện có Kho PT)</li>
     *   <li>cell 17 → UI col 16 (Hiện có Kho TL)</li>
     *   <li>cell 18 → UI col 17 (PC TQĐ ĐV)</li>
     *   <li>cell 19 → UI col 18 (PC TQĐ Kho)</li>
     *   <li>cell 20 → UI col 19 (PC TQĐ TL)</li>
     *   <li>cell 21 → UI col 20 (KHTN TrướcNổ ĐV d)</li>
     *   <li>cell 22 → UI col 21 (KHTN TrướcNổ ĐV PT)</li>
     *   <li>cell 23 → UI col 22 (KHTN TrướcNổ ĐV TL)</li>
     *   <li>cell 24 → UI col 23 (KHTN TrướcNổ Kho d)</li>
     *   <li>cell 25 → UI col 24 (KHTN TrướcNổ Kho PT)</li>
     *   <li>cell 26 → UI col 25 (KHTN TrướcNổ Kho TL)</li>
     *   <li>cell 27 → UI col 26 (KHTN ThựcHành ĐV)</li>
     *   <li>cell 28 → UI col 27 (KHTN ThựcHành Kho)</li>
     *   <li>cell 29 → UI col 28 (KHTN ThựcHành TL)</li>
     *   <li>cell 30..34 → (ô thừa trong Word - để trống)</li>
     * </ul>
     * Các dòng UI vượt quá 40 sẽ bị bỏ. Các dòng Word vượt quá số dòng UI → keyword rỗng ("").
     */
    public Map<String, String> getExportData(DefaultTableModel danModel) {
        Map<String, String> data = new HashMap<>();
        final int WORD_MAX_ROWS = 80;   // số dòng dữ liệu trong Word template
        final int WORD_CELLS    = 35;   // số ô vật lý mỗi dòng
        final int UI_COLS       = COL_COUNT; // = 29

        // Lấy danh sách hàng cần xuất từ model
        List<Object[]> exportRows = new ArrayList<>();
        if (danModel != null) {
            for (int i = 0; i < danModel.getRowCount() && exportRows.size() < WORD_MAX_ROWS; i++) {
                Object[] row = new Object[UI_COLS];
                for (int j = 0; j < UI_COLS; j++) {
                    row[j] = danModel.getValueAt(i, j);
                }
                exportRows.add(row);
            }
        }

        // Generate keyword → value cho từng ô trong 40 hàng
        for (int wordRow = 1; wordRow <= WORD_MAX_ROWS; wordRow++) {
            boolean hasData = wordRow <= exportRows.size();
            Object[] uiRow = hasData ? exportRows.get(wordRow - 1) : null;

            for (int wordCell = 0; wordCell < WORD_CELLS; wordCell++) {
                String key = "{{dan_r" + wordRow + "_c" + wordCell + "}}";
                String val = "";

                if (hasData) {
                    if (wordCell == 0) {
                        // STT = số thứ tự (dòng trong export, 1-based)
                        val = String.valueOf(wordRow);
                    } else if (wordCell == 1) {
                        // UI col 0: Loại đạn
                        val = cellStr(uiRow[0]);
                    } else if (wordCell == 2) {
                        // UI col 1: Số lượng VK
                        val = cellStr(uiRow[1]);
                    } else {
                        // wordCell 3..29 → UI col (wordCell - 3 + 2) = wordCell - 1
                        // wordCell 3 → UI col 2
                        // wordCell 4 → UI col 3
                        // ...
                        // wordCell 29 → UI col 28
                        // wordCell 30..34 → thừa, để trống
                        int uiCol = wordCell - 1; // cell3→col2, cell4→col3,..., cell29→col28
                        if (uiCol >= 2 && uiCol < UI_COLS) {
                            val = cellStr(uiRow[uiCol]);
                        }
                        // else val = "" (ô thừa cell 30-34)
                    }
                }
                data.put(key, val);
            }
        }
        return data;
    }

    /** Chuyển giá trị ô model sang String an toàn. Bỏ giá trị "-" (mask ô Hướng). */
    private static String cellStr(Object o) {
        if (o == null) return "";
        String s = o.toString().trim();
        return "-".equals(s) ? "" : s;
    }

    static final class Step3DanRow {
        String vatChat;
        double donVi;
        double phoiThuoc;
        double khoD;
    }

    private void populateMiniTableDanByDirection(Map<String, Map<String, Object[]>> leafByHuong) {
        synchronized (MINI_TABLE_LOCK) {
            miniTableDanByDirection.clear();
            for (Map.Entry<String, Map<String, Object[]>> dirEntry : leafByHuong.entrySet()) {
                String huong = dirEntry.getKey();
                Map<String, Object[]> dirMap = dirEntry.getValue();
                Map<String, Map<String, Double>> outMap = new LinkedHashMap<>();
                
                for (Map.Entry<String, Object[]> itemEntry : dirMap.entrySet()) {
                    String label = itemEntry.getKey().trim();
                    Object[] row = itemEntry.getValue();
                    
                    double tlDv = 0;
                    double tlKho = 0;
                    double tlTh = 0;
                    
                    try {
                        if (row[22] != null && !row[22].toString().isBlank() && !"-".equals(row[22])) {
                            tlDv = Double.parseDouble(row[22].toString().replace(",", "."));
                        }
                        if (row[25] != null && !row[25].toString().isBlank() && !"-".equals(row[25])) {
                            tlKho = Double.parseDouble(row[25].toString().replace(",", "."));
                        }
                        if (row[28] != null && !row[28].toString().isBlank() && !"-".equals(row[28])) {
                            tlTh = Double.parseDouble(row[28].toString().replace(",", "."));
                        }
                    } catch (Exception e) {}
                    
                    Map<String, Double> valMap = new LinkedHashMap<>();
                    valMap.put(TL_TRUOC_NO_DV, tlDv);
                    valMap.put(TL_TRUOC_NO_KHO, tlKho);
                    valMap.put(TL_THUC_HANH, tlTh);
                    
                    outMap.put(label, valMap);
                }
                miniTableDanByDirection.put(huong, outMap);
            }
        }
    }

    static final class Step4DanRow {
        String vatChat;
        String dvt;
        double duTru;
        double phaiCo0400;
        double phaiCoScd;
        double tieuThuGdcb;
        double tieuThuGdcd;
        String dtChitiet;
        String pc04Chitiet;
        String scdChitiet;
        String gdcbChitiet;
        String gdcdChitiet;
    }
}
