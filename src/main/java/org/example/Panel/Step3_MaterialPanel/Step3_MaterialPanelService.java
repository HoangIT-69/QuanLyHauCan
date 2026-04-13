package org.example.Panel.Step3_MaterialPanel;

import org.example.Popup.RegulationDetailDialog.RegulationDetailDialogService;
import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Truy vấn quyuoc_dan / quyuoc_vchc và bảng {@code step3_vat_chat}.
 * Phân cấp map trực tiếp vào các cột: huong_cy, huong_ty_1, huong_ty_2, ll_cd_vong_ngoai, ll_db_co_dong, db_bcht, ll_con_lai, ll_cd_tao_the.
 */
public class Step3_MaterialPanelService {

    public static final int PHAN_SLOT_COUNT = 6;

    public static final class DanOption {
        public final String name;
        public final String dvt;

        public DanOption(String name, String dvt) {
            this.name = name;
            this.dvt = dvt != null ? dvt : "";
        }
    }

    public static final class VchcOption {
        public final String name;
        public final String dvt;
        public final String danhMuc;

        public VchcOption(String name, String dvt, String danhMuc) {
            this.name = name;
            this.dvt = dvt != null ? dvt : "";
            this.danhMuc = danhMuc != null ? danhMuc : "";
        }

        public String displayLabel() {
            return "[" + danhMuc + "] " + name;
        }
    }

    public static final class PickerEntry {
        public final String name;
        public final String dvt;
        public final boolean isDan;
        public final boolean isVtkt;

        public PickerEntry(String name, String dvt, boolean isDan, boolean isVtkt) {
            this.name = name;
            this.dvt = dvt != null ? dvt : "";
            this.isDan = isDan;
            this.isVtkt = isVtkt;
        }
    }

    public List<DanOption> loadQuyuocDan() {
        List<DanOption> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return list;
            }
            try (var stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT loai_dan, don_vi_tinh FROM quyuoc_dan")) {
                while (rs.next()) {
                    list.add(new DanOption(rs.getString(1), rs.getString(2)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<VchcOption> loadQuyuocVchc() {
        List<VchcOption> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return list;
            }
            try (var stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT ten_vat_chat, don_vi_tinh, danh_muc FROM quyuoc_vchc ORDER BY " +
                                 "CASE danh_muc WHEN 'Quân lương' THEN 1 WHEN 'Quân trang' THEN 2 " +
                                 "WHEN 'Quân y' THEN 3 WHEN 'Doanh trại' THEN 4 WHEN 'VTKT' THEN 5 WHEN 'Khác' THEN 99 ELSE 6 END, ten_vat_chat")) {
                while (rs.next()) {
                    list.add(new VchcOption(rs.getString(1), rs.getString(2), rs.getString(3)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PickerEntry> buildCatalogPickerEntries(java.util.Set<String> excludeNames) {
        List<PickerEntry> out = new ArrayList<>();
        for (DanOption d : loadQuyuocDan()) {
            if (!excludeNames.contains(d.name.trim())) {
                out.add(new PickerEntry(d.name, d.dvt, true, false));
            }
        }
        for (VchcOption v : loadQuyuocVchc()) {
            if (!excludeNames.contains(v.name.trim())) {
                out.add(new PickerEntry(v.name, v.dvt, false, isVtktCategory(v.danhMuc)));
            }
        }
        return out;
    }

    static boolean isVtktCategory(String danhMuc) {
        if (danhMuc == null) {
            return false;
        }
        String s = danhMuc.trim().toLowerCase(Locale.ROOT);
        return "vtkt".equals(s) || s.contains("vật tư kỹ thuật") || s.contains("vat tu ky thuat");
    }

    public static final class LoadedGroups {
        public final List<Object[]> danRows;
        public final List<Object[]> hcRows;
        public final List<Object[]> ktRows;

        public LoadedGroups(List<Object[]> danRows, List<Object[]> hcRows, List<Object[]> ktRows) {
            this.danRows = danRows;
            this.hcRows = hcRows;
            this.ktRows = ktRows;
        }

        public static LoadedGroups empty() {
            return new LoadedGroups(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
    }

    static final class PhanFields {
        double huong_cy;
        double huong_ty_1;
        double huong_ty_2;
        double ll_cd_vong_ngoai;
        double ll_db_co_dong;
        double db_bcht;
        double ll_con_lai;
        double ll_cd_tao_the;
    }

    public LoadedGroups loadStep3FromDatabase(int sessionId, int phanCount, String hinhThucTapBai) {
        List<Object[]> listDan = new ArrayList<>();
        List<Object[]> listHC = new ArrayList<>();
        List<Object[]> listKT = new ArrayList<>();

        if (sessionId <= 0 || phanCount < 1) {
            return LoadedGroups.empty();
        }
        int pc = Math.min(phanCount, PHAN_SLOT_COUNT);
        boolean phongNgu = RegulationDetailDialogService.isPhongNgu(hinhThucTapBai);

        String sql = "SELECT * FROM step3_vat_chat WHERE session_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return LoadedGroups.empty();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    Set<String> colSet = columnLabelsLower(md);
                    while (rs.next()) {
                        PhanFields f = readPhanFields(rs, colSet);
                        int loai = rs.getInt("loai_vat_chat");
                        double kho = rs.getDouble("kho_d");
                        double donVi = rs.getDouble("don_vi");

                        double[] ui = phanFieldsToUi(f, phongNgu, pc);

                        Object[] rowData = new Object[7 + pc];
                        rowData[0] = "";
                        rowData[1] = rs.getString("vat_chat");
                        rowData[2] = rs.getString("dvt");
                        rowData[3] = formatDouble(kho + donVi);
                        rowData[4] = formatDouble(kho);
                        rowData[5] = formatDouble(donVi);
                        for (int i = 0; i < pc; i++) {
                            rowData[6 + i] = formatDouble(ui[i]);
                        }
                        rowData[6 + pc] = rs.getString("ghi_chu");

                        if (loai == 1) {
                            listDan.add(rowData);
                        } else if (loai == 2) {
                            listHC.add(rowData);
                        } else {
                            listKT.add(rowData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LoadedGroups(listDan, listHC, listKT);
    }

    private static Set<String> columnLabelsLower(ResultSetMetaData md) throws Exception {
        Set<String> colSet = new HashSet<>();
        int n = md.getColumnCount();
        for (int i = 1; i <= n; i++) {
            colSet.add(md.getColumnLabel(i).toLowerCase(Locale.ROOT));
        }
        return colSet;
    }

    private static PhanFields readPhanFields(ResultSet rs, Set<String> colSet) throws Exception {
        PhanFields f = new PhanFields();
        f.huong_cy = getDoubleCol(rs, colSet, "huong_cy");

        if (colSet.contains("huong_ty_1")) {
            f.huong_ty_1 = getDoubleCol(rs, colSet, "huong_ty_1");
        } else if (colSet.contains("huong_ty")) {
            f.huong_ty_1 = getDoubleCol(rs, colSet, "huong_ty");
        }

        f.huong_ty_2 = colSet.contains("huong_ty_2") ? getDoubleCol(rs, colSet, "huong_ty_2") : 0;

        if (colSet.contains("ll_cd_vong_ngoai")) {
            f.ll_cd_vong_ngoai = getDoubleCol(rs, colSet, "ll_cd_vong_ngoai");
        } else if (colSet.contains("phoi_thuoc")) {
            f.ll_cd_vong_ngoai = getDoubleCol(rs, colSet, "phoi_thuoc");
        }

        if (colSet.contains("ll_db_co_dong")) {
            f.ll_db_co_dong = getDoubleCol(rs, colSet, "ll_db_co_dong");
        } else if (colSet.contains("pn_sau")) {
            f.ll_db_co_dong = getDoubleCol(rs, colSet, "pn_sau");
        }

        f.db_bcht = colSet.contains("db_bcht") ? getDoubleCol(rs, colSet, "db_bcht") : 0;
        f.ll_con_lai = colSet.contains("ll_con_lai") ? getDoubleCol(rs, colSet, "ll_con_lai") : 0;
        f.ll_cd_tao_the = colSet.contains("ll_cd_tao_the") ? getDoubleCol(rs, colSet, "ll_cd_tao_the") : 0;

        return f;
    }

    private static double getDoubleCol(ResultSet rs, Set<String> colSet, String col) throws Exception {
        String key = col.toLowerCase(Locale.ROOT);
        if (!colSet.contains(key)) {
            return 0;
        }
        double v = rs.getDouble(col);
        return rs.wasNull() ? 0 : v;
    }

    static double[] phanFieldsToUi(PhanFields f, boolean phongNgu, int phanCount) {
        double[] ui = new double[phanCount];
        if (phongNgu) {
            if (phanCount > 0) {
                ui[0] = f.huong_cy;
            }
            if (phanCount > 1) {
                ui[1] = f.huong_ty_1;
            }
            if (phanCount > 2) {
                ui[2] = f.ll_cd_vong_ngoai;
            }
            if (phanCount > 3) {
                ui[3] = f.ll_db_co_dong;
            }
            if (phanCount > 4) {
                ui[4] = f.ll_con_lai;
            }
        } else {
            if (phanCount > 0) {
                ui[0] = f.huong_cy;
            }
            if (phanCount > 1) {
                ui[1] = f.huong_ty_1;
            }
            if (phanCount > 2) {
                ui[2] = f.huong_ty_2;
            }
            if (phanCount > 3) {
                ui[3] = f.db_bcht;
            }
            if (phanCount > 4) {
                ui[4] = f.ll_con_lai;
            }
            if (phanCount > 5) {
                ui[5] = f.ll_cd_tao_the;
            }
        }
        return ui;
    }

    static PhanFields uiPhanToFields(double[] p, boolean phongNgu) {
        PhanFields f = new PhanFields();
        double[] six = p != null ? p : new double[PHAN_SLOT_COUNT];
        if (phongNgu) {
            f.huong_cy = six.length > 0 ? six[0] : 0;
            f.huong_ty_1 = six.length > 1 ? six[1] : 0;
            f.ll_cd_vong_ngoai = six.length > 2 ? six[2] : 0;
            f.ll_db_co_dong = six.length > 3 ? six[3] : 0;
            f.ll_con_lai = six.length > 4 ? six[4] : 0;
        } else {
            f.huong_cy = six.length > 0 ? six[0] : 0;
            f.huong_ty_1 = six.length > 1 ? six[1] : 0;
            f.huong_ty_2 = six.length > 2 ? six[2] : 0;
            f.db_bcht = six.length > 3 ? six[3] : 0;
            f.ll_con_lai = six.length > 4 ? six[4] : 0;
            f.ll_cd_tao_the = six.length > 5 ? six[5] : 0;
        }
        return f;
    }

    public boolean saveStep3ToDatabase(int sessionId, List<Step3SaveRow> rows, String hinhThucTapBai) {
        Connection conn = null;
        boolean phongNgu = RegulationDetailDialogService.isPhongNgu(hinhThucTapBai);
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM step3_vat_chat WHERE session_id = ?")) {
                psDel.setInt(1, sessionId);
                psDel.executeUpdate();
            }

            String sql = "INSERT INTO step3_vat_chat (session_id, loai_vat_chat, vat_chat, dvt, kho_d, don_vi, "
                    + "huong_cy, huong_ty_1, huong_ty_2, ll_cd_vong_ngoai, ll_db_co_dong, db_bcht, ll_con_lai, ll_cd_tao_the, ghi_chu) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Step3SaveRow r : rows) {
                    PhanFields f = uiPhanToFields(r.phanSixNormalized(), phongNgu);
                    ps.setInt(1, sessionId);
                    ps.setInt(2, r.loai);
                    ps.setString(3, r.vatChat);
                    ps.setString(4, r.dvt);
                    ps.setDouble(5, r.khoD);
                    ps.setDouble(6, r.donVi);
                    ps.setDouble(7, f.huong_cy);
                    ps.setDouble(8, f.huong_ty_1);
                    ps.setDouble(9, f.huong_ty_2);
                    ps.setDouble(10, f.ll_cd_vong_ngoai);
                    ps.setDouble(11, f.ll_db_co_dong);
                    ps.setDouble(12, f.db_bcht);
                    ps.setDouble(13, f.ll_con_lai);
                    ps.setDouble(14, f.ll_cd_tao_the);
                    ps.setString(15, r.ghiChu != null ? r.ghiChu : "");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static final class Step3SaveRow {
        public final int loai;
        public final String vatChat;
        public final String dvt;
        public final double khoD;
        public final double donVi;
        private final double[] phanSix;
        public final String ghiChu;

        public Step3SaveRow(int loai, String vatChat, String dvt, double khoD, double donVi,
                            double[] phanSix, String ghiChu) {
            this.loai = loai;
            this.vatChat = vatChat;
            this.dvt = dvt;
            this.khoD = khoD;
            this.donVi = donVi;
            this.phanSix = phanSix != null ? phanSix.clone() : new double[PHAN_SLOT_COUNT];
            this.ghiChu = ghiChu;
        }

        public double[] phanSixNormalized() {
            double[] p = new double[PHAN_SLOT_COUNT];
            for (int i = 0; i < PHAN_SLOT_COUNT && i < phanSix.length; i++) {
                p[i] = phanSix[i];
            }
            return p;
        }
    }

    private static String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.format("%s", d).replace(".", ",");
    }
}
