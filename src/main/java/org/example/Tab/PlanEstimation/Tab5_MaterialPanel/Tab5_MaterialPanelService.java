package org.example.Tab.PlanEstimation.Tab5_MaterialPanel;

import org.example.Popup.RegulationDetailDialog.RegulationDetailDialogService;
import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;
import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tab V — Đạn, vật chất: văn bản lưu {@code pn_plan_estimation}; Bảng 1 tổng hợp từ Step 3 + Step 4 (MaterialRegulation);
 * Bảng 2 chỉ đọc — VCHC (loại 2) từ {@code step4_quy_dinh_du_tru}; kho/đơn vị lấy 2 slot đầu của {@code pc04_chitiet}/{@code scd_chitiet}
 * (định dạng 8 phần {@link RegulationDetailDialogService}). Luôn 20 dòng (đệm ô trống nếu thiếu).
 */
public class Tab5_MaterialPanelService {

    /** Khớp {@link org.example.Tab.Step4_Regulation.MaterialRegulationTab.MaterialRegulationTabUI} — nhóm Vật chất hậu cần. */
    public static final int LOAI_VAT_CHAT_HAU_CAN = 2;

    /** Số dòng tối đa Bảng 2 (UI + xuất Word). */
    public static final int TABLE2_MAX_ROWS = 20;

    /** Một dòng VCHC Step 4 + CSV phân cấp đã chuẩn hóa 8 phần. */
    private static final class VchcStep4Row {
        final String ten;
        final String dvt;
        final double tong04;
        final double tongScd;
        final String[] pc04eight;
        final String[] scdEight;

        VchcStep4Row(String ten, String dvt, double tong04, double tongScd, String[] pc04eight, String[] scdEight) {
            this.ten = ten;
            this.dvt = dvt;
            this.tong04 = tong04;
            this.tongScd = tongScd;
            this.pc04eight = pc04eight;
            this.scdEight = scdEight;
        }
    }

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadTextsInto(Tab5TextFields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.chuanBi = r.tab5TxtChuanBi;
        target.chienDau = r.tab5TxtChienDau;
        target.sauCd = r.tab5TxtSauCd;
    }

    public void saveTexts(int sessionId, String chuanBi, String chienDau, String sauCd) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab5TxtChuanBi = chuanBi != null ? chuanBi : "";
        r.tab5TxtChienDau = chienDau != null ? chienDau : "";
        r.tab5TxtSauCd = sauCd != null ? sauCd : "";
        store.save(r);
    }

    public static final class Tab5TextFields {
        public String chuanBi = "";
        public String chienDau = "";
        public String sauCd = "";
    }

    /**
     * 4 dòng cố định: Đạn, VCHC, VTKT, Cộng — cột số từ DB (read-only trên UI).
     */
    public List<Object[]> buildTable1Rows(int sessionId) {
        List<Object[]> rows = new ArrayList<>();
        String dvt = "Tấn";
        String[] labels = {"1. Đạn, Lựu đạn", "2. Vật chất hậu cần", "3. Vật tư kỹ thuật"};

        Map<Integer, Double> duTru = new HashMap<>();
        Map<Integer, Double> phaiBoSung = new HashMap<>();
        loadStep4Aggregates(sessionId, duTru, phaiBoSung);
        Map<Integer, Double> hienCo = sumHienCoByLoai(sessionId);

        double sumQd = 0;
        double sumHc = 0;
        double sumBs = 0;

        for (int i = 0; i < 3; i++) {
            int loai = i + 1;
            double qd = duTru.getOrDefault(loai, 0.0);
            double hc = hienCo.getOrDefault(loai, 0.0);
            double bs = phaiBoSung.getOrDefault(loai, 0.0);
            sumQd += qd;
            sumHc += hc;
            sumBs += bs;
            rows.add(new Object[]{labels[i], dvt, formatNum(qd), formatNum(hc), formatNum(bs)});
        }
        rows.add(new Object[]{"4. Cộng", dvt, formatNum(sumQd), formatNum(sumHc), formatNum(sumBs)});
        return rows;
    }

    /**
     * Một lần quét Step 4: Quy định dự trữ (SUM du_tru) và Phải bổ sung (SUM phai_co_0400 + phai_co_scd) theo {@code loai_vat_chat}.
     */
    private void loadStep4Aggregates(int sessionId, Map<Integer, Double> duTruOut, Map<Integer, Double> phaiBoSungOut) {
        if (sessionId < 1) {
            return;
        }
        String sql = "SELECT loai_vat_chat, "
                + "COALESCE(SUM(du_tru), 0) AS s_du, "
                + "COALESCE(SUM(COALESCE(phai_co_0400, 0) + COALESCE(phai_co_scd, 0)), 0) AS s_bs "
                + "FROM step4_quy_dinh_du_tru WHERE session_id = ? GROUP BY loai_vat_chat";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int loai = rs.getInt("loai_vat_chat");
                        duTruOut.put(loai, rs.getDouble("s_du"));
                        phaiBoSungOut.put(loai, rs.getDouble("s_bs"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Step 3: Hiện có = SUM(kho_d + don_vi) theo {@code loai_vat_chat}.
     */
    private Map<Integer, Double> sumHienCoByLoai(int sessionId) {
        Map<Integer, Double> map = new HashMap<>();
        if (sessionId < 1) {
            return map;
        }
        String sql = "SELECT loai_vat_chat, "
                + "COALESCE(SUM(COALESCE(kho_d, 0) + COALESCE(don_vi, 0)), 0) AS s "
                + "FROM step3_vat_chat WHERE session_id = ? GROUP BY loai_vat_chat";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return map;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int loai = rs.getInt("loai_vat_chat");
                        map.put(loai, rs.getDouble("s"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Bảng 2 — đúng 20 dòng × 9 cột: STT, tên VC, ĐVT, kho/đơn vị/tổng (04), kho/đơn vị/tổng (sau CĐ).
     * Tối đa 20 loại VCHC từ Step 4; dòng thiếu điền chuỗi rỗng. Tổng: {@code phai_co_0400}, {@code phai_co_scd}; kho/đơn vị: 2 slot đầu CSV.
     */
    public List<Object[]> buildTable2Rows(int sessionId) {
        List<Object[]> rows = new ArrayList<>();
        int tt = 1;
        for (VchcStep4Row r : loadVchcStep4Rows(sessionId)) {
            double k04 = InputValidator.parseDoubleSafe(r.pc04eight[0]);
            double dv04 = InputValidator.parseDoubleSafe(r.pc04eight[1]);
            double kScd = InputValidator.parseDoubleSafe(r.scdEight[0]);
            double dvScd = InputValidator.parseDoubleSafe(r.scdEight[1]);
            rows.add(new Object[]{
                    String.valueOf(tt++),
                    r.ten,
                    r.dvt,
                    formatNum(k04),
                    formatNum(dv04),
                    formatNum(r.tong04),
                    formatNum(kScd),
                    formatNum(dvScd),
                    formatNum(r.tongScd)
            });
        }
        while (rows.size() < TABLE2_MAX_ROWS) {
            rows.add(new Object[]{"", "", "", "", "", "", "", "", ""});
        }
        return rows;
    }

    private List<VchcStep4Row> loadVchcStep4Rows(int sessionId) {
        List<VchcStep4Row> out = new ArrayList<>();
        if (sessionId < 1) {
            return out;
        }
        String sql = "SELECT vat_chat, dvt, phai_co_0400, phai_co_scd, pc04_chitiet, scd_chitiet "
                + "FROM step4_quy_dinh_du_tru WHERE session_id = ? AND loai_vat_chat = ? ORDER BY id ASC LIMIT "
                + TABLE2_MAX_ROWS;
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                ps.setInt(2, LOAI_VAT_CHAT_HAU_CAN);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ten = nz(rs.getString("vat_chat"));
                        if (ten.isEmpty()) {
                            continue;
                        }
                        String dvt = nz(rs.getString("dvt"));
                        double tong04 = rs.getDouble("phai_co_0400");
                        double tongScd = rs.getDouble("phai_co_scd");
                        String[] pc04 = RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(rs.getString("pc04_chitiet")));
                        String[] scd = RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(rs.getString("scd_chitiet")));
                        out.add(new VchcStep4Row(ten, dvt, tong04, tongScd, pc04, scd));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    private static String[] splitCsv(String s) {
        if (s == null || s.isEmpty()) {
            return new String[0];
        }
        return s.split(",", -1);
    }

    private static String nz(String s) {
        return s != null ? s.trim() : "";
    }

    private static String formatNum(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        if (v == (long) v) {
            return String.format(Locale.ROOT, "%d", (long) v);
        }
        return String.format(Locale.ROOT, "%s", v).replace('.', ',');
    }
}
