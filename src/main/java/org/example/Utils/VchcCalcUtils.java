package org.example.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Các hàm tiện ích dùng chung cho bảng Vật chất (Tab5_VatChatPanel).
 * Tất cả phương thức là static, lớp không thể khởi tạo.
 */
public final class VchcCalcUtils {

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private VchcCalcUtils() {
    }

    // =========================================================
    // Định dạng số
    // =========================================================

    /**
     * Hệ số / định mức dòng con: bỏ .00 vô nghĩa (10 → "10", 1.5 → "1.5").
     */
    public static String fmtCoeff(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        if (Math.abs(v) < 1e-12) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingUsed(false);
        String s = df.format(v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s.isEmpty() ? "0" : s;
    }

    /**
     * Trọng lượng dòng tổng in đậm: luôn 1 chữ số thập phân (tấn).
     */
    public static String fmtTonBold(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    /**
     * Format 1 chữ số thập phân (dùng cho bảng Hậu cần 5 cột).
     */
    public static String fmt2(double v) {
        return String.format(Locale.US, "%.1f", v);
    }

    /**
     * Trả về ký hiệu La Mã theo chỉ số (0-based). Clamp vào phạm vi hợp lệ.
     */
    public static String romanLabel(int idx) {
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= ROMAN.length) {
            idx = ROMAN.length - 1;
        }
        return ROMAN[idx];
    }

    // =========================================================
    // Mảng và chuỗi
    // =========================================================

    /**
     * Truy cập an toàn vào mảng double[]; trả về 0 nếu ngoài phạm vi.
     */
    public static double at(double[] a, int i) {
        if (a == null || i < 0 || i >= a.length) {
            return 0;
        }
        return a[i];
    }

    /**
     * Parse chuỗi "a,b,c,d,e,f" thành double[6] (dùng cho các cột chi tiết).
     */
    public static double[] parse6(String s) {
        double[] res = new double[6];
        if (s == null || s.isEmpty()) {
            return res;
        }
        String[] parts = s.split(",", -1);
        for (int i = 0; i < Math.min(6, parts.length); i++) {
            res[i] = InputValidator.parseDoubleSafe(parts[i].trim());
        }
        return res;
    }

    /**
     * Cộng dồn trọng lượng (tấn): heSo × tlDvt / 1000 vào vị trí col của mảng.
     */
    public static void addTonWeighted(double[] arr, int col, double heSo, double tlDvt) {
        arr[col] += (heSo * tlDvt) / 1000.0;
    }

    /**
     * Chuyển giá trị ô model sang String an toàn. Strip HTML tags. Bỏ "-".
     */
    public static String vcCellStr(Object o) {
        if (o == null) {
            return "";
        }
        String s = o.toString().trim();
        s = s.replaceAll("<[^>]+>", "").trim();
        return "-".equals(s) ? "" : s;
    }

    // =========================================================
    // Nghiệp vụ nhận diện vật chất
    // =========================================================

    /**
     * Kiểm tra tên vật chất là Đường sữa / ĐSTB (bao gồm mọi biến thể).
     */
    public static boolean isDuongSuaOrDstbTen(String ten) {
        if (ten == null) {
            return false;
        }
        String t = ten.toLowerCase(Locale.ROOT);
        return t.contains("đường sữa") || t.contains("duong sua")
                || t.contains("đstb") || t.contains("dstb");
    }

    /**
     * Kiểm tra tên vật chất khớp chính xác "Đường sữa thương binh".
     * Dùng để bỏ qua khi ghi miniTable.
     */
    public static boolean isDuongSuaThuongBinh(String ten) {
        if (ten == null) {
            return false;
        }
        return ten.trim().equalsIgnoreCase("Đường sữa thương binh");
    }

    /**
     * Tại khối Hướng: Đường sữa / ĐSTB → cả dòng số thành "-". LLCL hiển thị bình thường.
     */
    public static boolean isHuongDstbFullDash(String ten, boolean isDirection) {
        return isDirection && isDuongSuaOrDstbTen(ten);
    }

    // =========================================================
    // Nghiệp vụ PT
    // =========================================================

    /**
     * Vật chất không mang vác: gạch cột PT.
     * Ngoại lệ tại LLCL: Đường sữa không bị gạch.
     */
    public static boolean computePtExc(String ten, int catIndex, int type, boolean llclBlock) {
        if (llclBlock && isDuongSuaOrDstbTen(ten)) {
            return false;
        }
        return isPtExcludedVatChat(ten, catIndex, type);
    }

    /**
     * Quy tắc gạch PT: VTKT (type=2 hoặc catIndex=3), Túi*, Đường sữa*.
     */
    public static boolean isPtExcludedVatChat(String ten, int catIndex, int panelType) {
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

    // =========================================================
    // Tính TL ĐVT
    // =========================================================

    /**
     * Tính TL ĐVT (kg) từ quy ước và quân số.
     * Phân loại theo DVT và tên, xử lý các trường hợp đặc biệt:
     * <ul>
     *   <li>DVT Túi            → quyUoc (không nhân QS)</li>
     *   <li>Tên Dầu thắp       → quyUoc * quanSo / 30</li>
     *   <li>Tên ĐSTB           → (quyUoc/7) * quanSo * 0.01</li>
     *   <li>DVT %QS/%          → quyUoc * quanSo * 0.01</li>
     *   <li>DVT Bộ/Cái         → quyUoc (không nhân QS)</li>
     *   <li>Mặc định           → quyUoc * quanSo</li>
     * </ul>
     */
    public static double calculateTL(String tenVatChat, double quyUoc, String donViTinh, double quanSo) {
        if (quanSo == 0) {
            return 0;
        }
        String tenLower = tenVatChat == null ? "" : tenVatChat.toLowerCase(Locale.ROOT);
        String dvtLower = donViTinh != null ? donViTinh.trim().toLowerCase(Locale.ROOT) : "";

        if (dvtLower.startsWith("túi") || dvtLower.startsWith("tui")) {
            return quyUoc;
        }
        if (tenLower.contains("dầu thắp") || tenLower.contains("dau thap")) {
            return (quyUoc * quanSo) / 30.0;
        }
        if (isDuongSuaOrDstbTen(tenVatChat)) {
            return (quyUoc / 7.0) * quanSo * 0.01;
        }
        if (dvtLower.contains("%")) {
            return quyUoc * quanSo * 0.01;
        }
        if (dvtLower.equals("bộ") || dvtLower.equals("bo")
                || dvtLower.equals("cái") || dvtLower.equals("cai")) {
            return quyUoc;
        }
        return quyUoc * quanSo;
    }
}
