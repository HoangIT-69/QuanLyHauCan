package org.example.Popup.RegulationDetailDialog;

import org.example.Utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lưu chi tiết phân cấp theo khóa {@code vatChat + "_" + targetColumn} (chỉ số cột bảng Step 4).
 * Mỗi giá trị là 8 phần CSV: kho, đơn_vị, p1..p6 (phân cấp, pad 0 nếu thiếu).
 */
public final class RegulationDetailDialogService {

    public static final Map<String, String[]> detailDataStore = new HashMap<>();

    public static final int SLOT_COUNT = 8;
    public static final String[] DEFAULT_EIGHT = {"", "", "", "", "", "", "", ""};

    private RegulationDetailDialogService() {
    }

    public static final class Step2SelectionState {
        public final boolean cacHuongEnabled;
        public final Set<String> selectedDirections;

        public Step2SelectionState(boolean cacHuongEnabled, Set<String> selectedDirections) {
            this.cacHuongEnabled = cacHuongEnabled;
            this.selectedDirections = selectedDirections != null ? selectedDirections : new LinkedHashSet<>();
        }
    }

    public static boolean isPhongNgu(String hinhThucTapBai) {
        if (hinhThucTapBai == null || hinhThucTapBai.isEmpty()) {
            return true;
        }
        String n = hinhThucTapBai.toLowerCase(Locale.ROOT);
        if (n.contains("tiến công") || n.contains("tien cong")) {
            return false;
        }
        return true;
    }

    public static int phanCapFieldCount(String hinhThucTapBai) {
        return isPhongNgu(hinhThucTapBai) ? 6 : 6;
    }

    public static String[] phanCapLabels(String hinhThucTapBai) {
        if (isPhongNgu(hinhThucTapBai)) {
            return new String[]{
                    "Hướng chủ yếu",
                    "Hướng thứ yếu",
                    "Lực lượng chiến đấu vòng ngoài",
                    "Lực lượng dự bị cơ động",
                    "Lực lượng còn lại",
                    "Hướng PN phía sau"
            };
        }
        return new String[]{
                "Hướng chủ yếu",
                "Hướng thứ yếu 1",
                "Hướng thứ yếu 2",
                "Dự bị binh chủng hợp thành",
                "Lực lượng còn lại",
                "Lực lượng chiến đấu tạo thế"
        };
    }

    /**
     * Ghi ngay 8 slot vào store (đồng bộ real-time từ dialog).
     */
    public static void putDetailEight(String key, String[] eightSlots) {
        if (key == null) {
            return;
        }
        detailDataStore.put(key, upgradeRawPartsToEight(eightSlots != null ? eightSlots : DEFAULT_EIGHT.clone()));
    }

    /**
     * Ghi kho, đơn vị và tối đa 6 giá trị phân cấp (cột thừa = 0).
     */
    public static void putComputedRow(String key, double kho, double donVi, double[] phanUpToSix) {
        if (key == null) {
            return;
        }
        String[] row = DEFAULT_EIGHT.clone();
        row[0] = toStoreToken(kho);
        row[1] = toStoreToken(donVi);
        if (phanUpToSix != null) {
            for (int i = 0; i < 6 && i < phanUpToSix.length; i++) {
                row[2 + i] = toStoreToken(phanUpToSix[i]);
            }
        }
        detailDataStore.put(key, row);
    }

    public static double roundToTwoDecimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String toStoreToken(double d) {
        d = roundToTwoDecimals(d);
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
            return "";
        }
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }

    /**
     * Nâng dữ liệu CSV cũ (6 số) hoặc mới (8 số) lên đúng 8 phần.
     */
    public static String[] upgradeRawPartsToEight(String[] raw) {
        String[] out = DEFAULT_EIGHT.clone();
        if (raw == null || raw.length == 0) {
            return out;
        }
        if (raw.length >= 8) {
            for (int i = 0; i < 8; i++) {
                out[i] = safeNum(raw[i]);
            }
            return out;
        }
        if (raw.length >= 6) {
            for (int i = 0; i < 6; i++) {
                out[i] = safeNum(raw[i]);
            }
            out[6] = "0";
            out[7] = "0";
            return out;
        }
        for (int i = 0; i < raw.length && i < 8; i++) {
            out[i] = safeNum(raw[i]);
        }
        return out;
    }

    public static String joinEight(String[] parts) {
        String[] eight = upgradeRawPartsToEight(parts != null ? parts : new String[0]);
        return String.join(",", eight);
    }

    private static String safeNum(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "";
        }
        return s.trim().replace(",", ".");
    }

    public static Step2SelectionState loadStep2SelectionState(int sessionId) {
        Set<String> selected = new LinkedHashSet<>();
        boolean enabled = false;
        if (sessionId <= 0) {
            return new Step2SelectionState(false, selected);
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new Step2SelectionState(false, selected);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT cac_huong_enabled, step2_selected_huongs FROM sessions WHERE id = ?")) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        enabled = rs.getInt("cac_huong_enabled") == 1;
                        selected = parseDirections(rs.getString("step2_selected_huongs"));
                    }
                }
            } catch (Exception ignored) {
            }

            if (selected.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT DISTINCT huong FROM step2_bien_che WHERE session_id = ?")) {
                    ps.setInt(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String huong = rs.getString("huong");
                            if (huong != null && !huong.isBlank()) {
                                selected.add(huong.trim());
                            }
                        }
                    }
                }
            }

            // Ưu tiên dữ liệu hướng thực tế: có hướng thì xem như đã bật phân cấp, kể cả cờ cũ chưa được cập nhật.
            if (!selected.isEmpty()) {
                enabled = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Step2SelectionState(enabled, selected);
    }

    private static Set<String> parseDirections(String raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        String[] items = raw.split("\\|");
        for (String s : items) {
            if (s != null && !s.isBlank()) {
                out.add(s.trim());
            }
        }
        return out;
    }
}
