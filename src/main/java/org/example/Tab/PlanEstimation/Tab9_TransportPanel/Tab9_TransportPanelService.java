package org.example.Tab.PlanEstimation.Tab9_TransportPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

/**
 * Tab IX — Vận tải: tổng tiêu thụ GĐCB/GĐCĐ từ Step 4; số ngày GĐCB/GĐCĐ ước lượng từ dòng Lương thực/Gạo (VCHC);
 * khả năng VTB (kg/chuyến) theo khoảng Min–Max từng lực lượng.
 */
public class Tab9_TransportPanelService {

    /** Khối lượng quy ước một xe đạp thồ (kg/chuyến). */
    public static final double KG_MOT_XE_DAP_THO = 250.0;

    /** Kết quả tính khả năng từng lực lượng và tổng (kg/chuyến). */
    public static final class CapacityBreakdown {
        public double tdMin;
        public double tdMax;
        public double trMin;
        public double trMax;
        public double dqMin;
        public double dqMax;
        public double tongMin;
        public double tongMax;
    }

    /**
     * Tính khả năng kg/chuyến cho từng lực lượng và tổng Min/Max.
     * Mỗi lực lượng: (người × kg_min/max) + (xe × {@link #KG_MOT_XE_DAP_THO}).
     */
    public static CapacityBreakdown computeCapacityBreakdown(
            double nTd, String tdKgMin, String tdKgMax, double xeTd,
            double nTr, String trKgMin, String trKgMax, double xeTr,
            double nDq, String dqKgMin, String dqKgMax, double xeDq) {
        CapacityBreakdown b = new CapacityBreakdown();
        double kTdMin = InputValidator.parseDoubleSafe(tdKgMin);
        double kTdMax = InputValidator.parseDoubleSafe(tdKgMax);
        double kTrMin = InputValidator.parseDoubleSafe(trKgMin);
        double kTrMax = InputValidator.parseDoubleSafe(trKgMax);
        double kDqMin = InputValidator.parseDoubleSafe(dqKgMin);
        double kDqMax = InputValidator.parseDoubleSafe(dqKgMax);

        b.tdMin = nTd * kTdMin + xeTd * KG_MOT_XE_DAP_THO;
        b.tdMax = nTd * kTdMax + xeTd * KG_MOT_XE_DAP_THO;
        b.trMin = nTr * kTrMin + xeTr * KG_MOT_XE_DAP_THO;
        b.trMax = nTr * kTrMax + xeTr * KG_MOT_XE_DAP_THO;
        b.dqMin = nDq * kDqMin + xeDq * KG_MOT_XE_DAP_THO;
        b.dqMax = nDq * kDqMax + xeDq * KG_MOT_XE_DAP_THO;

        b.tongMin = b.tdMin + b.trMin + b.dqMin;
        b.tongMax = b.tdMax + b.trMax + b.dqMax;
        return b;
    }

    /**
     * Tổng khả năng Min/Max (kg/chuyến) — tên gọi theo nghiệp vụ; tương đương {@link #computeCapacityBreakdown}.
     */
    public static CapacityBreakdown calculateTotalCapacity(
            double nTd, String tdKgMin, String tdKgMax, double xeTd,
            double nTr, String trKgMin, String trKgMax, double xeTr,
            double nDq, String dqKgMin, String dqKgMax, double xeDq) {
        return computeCapacityBreakdown(nTd, tdKgMin, tdKgMax, xeTd,
                nTr, trKgMin, trKgMax, xeTr,
                nDq, dqKgMin, dqKgMax, xeDq);
    }

    public static String formatKgChuyen(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        return String.format(Locale.ROOT, "%,.0f", v);
    }

    public static final class TransportDbSnapshot {
        /** Tấn — SUM {@code tieu_thu_gdcb} */
        public double sumTieuThuGdcb;
        /** Tấn — SUM {@code tieu_thu_gdcd} */
        public double sumTieuThuGdcd;
        /** Số ngày đại diện GĐCB (từ dòng Gạo/Lương thực nếu có; mặc định 1) */
        public int ngayAnGdcb = 1;
        /** Số ngày đại diện GĐCĐ */
        public int ngayAnGdcd = 1;
    }

    public TransportDbSnapshot loadTransportSnapshot(int sessionId) {
        TransportDbSnapshot s = new TransportDbSnapshot();
        if (sessionId < 1) {
            return s;
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return s;
            }
            String sumSql = "SELECT COALESCE(SUM(tieu_thu_gdcb), 0) AS sdcb, COALESCE(SUM(tieu_thu_gdcd), 0) AS sdcd "
                    + "FROM step4_quy_dinh_du_tru WHERE session_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        s.sumTieuThuGdcb = rs.getDouble("sdcb");
                        s.sumTieuThuGdcd = rs.getDouble("sdcd");
                    }
                }
            }

            String riceSql = "SELECT vat_chat, tieu_thu_gdcb, tieu_thu_gdcd FROM step4_quy_dinh_du_tru "
                    + "WHERE session_id = ? AND loai_vat_chat = 2 ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(riceSql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ten = rs.getString("vat_chat");
                        if (!isRiceLike(ten)) {
                            continue;
                        }
                        s.ngayAnGdcb = interpretAsDays(rs.getDouble("tieu_thu_gdcb"));
                        s.ngayAnGdcd = interpretAsDays(rs.getDouble("tieu_thu_gdcd"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    private static boolean isRiceLike(String ten) {
        if (ten == null) {
            return false;
        }
        String n = ten.toLowerCase(Locale.ROOT);
        return n.contains("gạo") || n.contains("gao") || n.contains("lương thực") || n.contains("luong thuc") || n.contains("lương");
    }

    /**
     * Nếu giá trị trong khoảng 1..366 coi là số ngày; nếu lớn hơn (khối lượng tấn) dùng mặc định 30 ngày.
     */
    static int interpretAsDays(double v) {
        if (Double.isNaN(v) || v <= 0) {
            return 1;
        }
        if (v <= 366) {
            return Math.max(1, (int) Math.round(v));
        }
        return 30;
    }

    public static String formatTon(double t) {
        if (Double.isNaN(t) || Double.isInfinite(t)) {
            return "0";
        }
        if (t == (long) t) {
            return String.format(Locale.ROOT, "%d", (long) t);
        }
        return String.format(Locale.ROOT, "%.3f", t).replace('.', ',');
    }
}
