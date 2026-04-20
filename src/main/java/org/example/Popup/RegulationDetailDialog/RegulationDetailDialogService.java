package org.example.Popup.RegulationDetailDialog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Lưu chi tiết phân cấp theo khóa {@code vatChat + "_" + targetColumn} (chỉ số cột bảng Step 4).
 * Mỗi giá trị là 8 phần CSV: kho, đơn_vị, p1..p6 (phân cấp, pad 0 nếu thiếu).
 */
public final class RegulationDetailDialogService {

    public static final Map<String, String[]> detailDataStore = new HashMap<>();

    public static final int SLOT_COUNT = 8;
    public static final String[] DEFAULT_EIGHT = {"0", "0", "0", "0", "0", "0", "0", "0"};

    private RegulationDetailDialogService() {
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

    private static String toStoreToken(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return "0";
        }
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.valueOf(d);
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
            return "0";
        }
        return s.trim().replace(",", ".");
    }

    /**
     * @return null nếu hợp lệ; ngược lại thông báo lỗi
     */
    public static String validateDonViVsTong(double tongSoLuong, double sumPhanCap) {
        if (sumPhanCap > tongSoLuong + 1e-9) {
            return "Số lượng phân cấp không được vượt quá Tổng số lượng hiện có!";
        }
        return null;
    }
}
