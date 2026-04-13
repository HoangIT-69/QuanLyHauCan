package org.example.Tab.PlanEstimation;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Một dòng {@code pn_plan_estimation} theo {@code session_id} — Tab I–XII (Dự kiến — Phòng ngự).
 */
public final class PnPlanEstimationTextStore {

    public static final class PlanEstimationRow {
        public int sessionId;
        public String danhGia = "";
        public String nhiemVu = "";
        public String toChuc = "";
        public String boTri = "";
        public String tab4ChiTieu = "";
        public String tab4ChuanBi = "";
        public String tab4ChienDau = "";
        public String tab5TxtChuanBi = "";
        public String tab5TxtChienDau = "";
        public String tab5TxtSauCd = "";
        public String tab6AnUong = "";
        public String tab6Mac = "";
        public String tab6ONguNghi = "";
        public String tab10TinhHuong = "";
        public String tab10BienPhap = "";
        public String tab11TrienKhai = "";
        public String tab11ChiHuy = "";
        public String tab11NguoiThayThe = "";
        public String tab11TtCb = "";
        public String tab11TtCd = "";
        public String tab11BcCb = "";
        public String tab11BcCd1 = "";
        public String tab11BcCd2 = "";
        public String tab12KetLuan = "";
        public String tab12DeNghi = "";
    }

    public PlanEstimationRow load(int sessionId) {
        PlanEstimationRow row = new PlanEstimationRow();
        row.sessionId = sessionId;
        if (sessionId < 1) {
            return row;
        }
        String sql = "SELECT danh_gia, nhiem_vu, to_chuc, bo_tri, "
                + "tab4_chi_tieu, tab4_chuan_bi, tab4_chien_dau, "
                + "tab5_txt_chuan_bi, tab5_txt_chien_dau, tab5_txt_sau_cd, "
                + "tab6_an_uong, tab6_mac, tab6_o_ngu_nghi, "
                + "tab10_tinh_huong, tab10_bien_phap, "
                + "tab11_trien_khai, tab11_chi_huy, tab11_nguoi_thay_the, tab11_tt_cb, tab11_tt_cd, "
                + "tab11_bc_cb, tab11_bc_cd1, tab11_bc_cd2, "
                + "tab12_ket_luan, tab12_de_nghi "
                + "FROM pn_plan_estimation WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return row;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        row.danhGia = nz(rs.getString("danh_gia"));
                        row.nhiemVu = nz(rs.getString("nhiem_vu"));
                        row.toChuc = nz(safeCol(rs, "to_chuc"));
                        row.boTri = nz(safeCol(rs, "bo_tri"));
                        row.tab4ChiTieu = nz(safeCol(rs, "tab4_chi_tieu"));
                        row.tab4ChuanBi = nz(safeCol(rs, "tab4_chuan_bi"));
                        row.tab4ChienDau = nz(safeCol(rs, "tab4_chien_dau"));
                        row.tab5TxtChuanBi = nz(safeCol(rs, "tab5_txt_chuan_bi"));
                        row.tab5TxtChienDau = nz(safeCol(rs, "tab5_txt_chien_dau"));
                        row.tab5TxtSauCd = nz(safeCol(rs, "tab5_txt_sau_cd"));
                        row.tab6AnUong = nz(safeCol(rs, "tab6_an_uong"));
                        row.tab6Mac = nz(safeCol(rs, "tab6_mac"));
                        row.tab6ONguNghi = nz(safeCol(rs, "tab6_o_ngu_nghi"));
                        row.tab10TinhHuong = nz(safeCol(rs, "tab10_tinh_huong"));
                        row.tab10BienPhap = nz(safeCol(rs, "tab10_bien_phap"));
                        row.tab11TrienKhai = nz(safeCol(rs, "tab11_trien_khai"));
                        row.tab11ChiHuy = nz(safeCol(rs, "tab11_chi_huy"));
                        row.tab11NguoiThayThe = nz(safeCol(rs, "tab11_nguoi_thay_the"));
                        row.tab11TtCb = nz(safeCol(rs, "tab11_tt_cb"));
                        row.tab11TtCd = nz(safeCol(rs, "tab11_tt_cd"));
                        row.tab11BcCb = nz(safeCol(rs, "tab11_bc_cb"));
                        row.tab11BcCd1 = nz(safeCol(rs, "tab11_bc_cd1"));
                        row.tab11BcCd2 = nz(safeCol(rs, "tab11_bc_cd2"));
                        row.tab12KetLuan = nz(safeCol(rs, "tab12_ket_luan"));
                        row.tab12DeNghi = nz(safeCol(rs, "tab12_de_nghi"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    private static String safeCol(ResultSet rs, String col) {
        try {
            return rs.getString(col);
        } catch (Exception e) {
            return "";
        }
    }

    public void save(PlanEstimationRow r) {
        if (r == null || r.sessionId < 1) {
            return;
        }
        String sql = "INSERT INTO pn_plan_estimation (session_id, danh_gia, nhiem_vu, to_chuc, bo_tri, "
                + "tab4_chi_tieu, tab4_chuan_bi, tab4_chien_dau, "
                + "tab5_txt_chuan_bi, tab5_txt_chien_dau, tab5_txt_sau_cd, "
                + "tab6_an_uong, tab6_mac, tab6_o_ngu_nghi, "
                + "tab10_tinh_huong, tab10_bien_phap, "
                + "tab11_trien_khai, tab11_chi_huy, tab11_nguoi_thay_the, tab11_tt_cb, tab11_tt_cd, "
                + "tab11_bc_cb, tab11_bc_cd1, tab11_bc_cd2, "
                + "tab12_ket_luan, tab12_de_nghi) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE danh_gia = VALUES(danh_gia), nhiem_vu = VALUES(nhiem_vu), "
                + "to_chuc = VALUES(to_chuc), bo_tri = VALUES(bo_tri), "
                + "tab4_chi_tieu = VALUES(tab4_chi_tieu), tab4_chuan_bi = VALUES(tab4_chuan_bi), tab4_chien_dau = VALUES(tab4_chien_dau), "
                + "tab5_txt_chuan_bi = VALUES(tab5_txt_chuan_bi), tab5_txt_chien_dau = VALUES(tab5_txt_chien_dau), tab5_txt_sau_cd = VALUES(tab5_txt_sau_cd), "
                + "tab6_an_uong = VALUES(tab6_an_uong), tab6_mac = VALUES(tab6_mac), tab6_o_ngu_nghi = VALUES(tab6_o_ngu_nghi), "
                + "tab10_tinh_huong = VALUES(tab10_tinh_huong), tab10_bien_phap = VALUES(tab10_bien_phap), "
                + "tab11_trien_khai = VALUES(tab11_trien_khai), tab11_chi_huy = VALUES(tab11_chi_huy), tab11_nguoi_thay_the = VALUES(tab11_nguoi_thay_the), "
                + "tab11_tt_cb = VALUES(tab11_tt_cb), tab11_tt_cd = VALUES(tab11_tt_cd), "
                + "tab11_bc_cb = VALUES(tab11_bc_cb), tab11_bc_cd1 = VALUES(tab11_bc_cd1), tab11_bc_cd2 = VALUES(tab11_bc_cd2), "
                + "tab12_ket_luan = VALUES(tab12_ket_luan), tab12_de_nghi = VALUES(tab12_de_nghi)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                ps.setInt(i++, r.sessionId);
                ps.setString(i++, r.danhGia);
                ps.setString(i++, r.nhiemVu);
                ps.setString(i++, r.toChuc);
                ps.setString(i++, r.boTri);
                ps.setString(i++, r.tab4ChiTieu);
                ps.setString(i++, r.tab4ChuanBi);
                ps.setString(i++, r.tab4ChienDau);
                ps.setString(i++, r.tab5TxtChuanBi);
                ps.setString(i++, r.tab5TxtChienDau);
                ps.setString(i++, r.tab5TxtSauCd);
                ps.setString(i++, r.tab6AnUong);
                ps.setString(i++, r.tab6Mac);
                ps.setString(i++, r.tab6ONguNghi);
                ps.setString(i++, r.tab10TinhHuong);
                ps.setString(i++, r.tab10BienPhap);
                ps.setString(i++, r.tab11TrienKhai);
                ps.setString(i++, r.tab11ChiHuy);
                ps.setString(i++, r.tab11NguoiThayThe);
                ps.setString(i++, r.tab11TtCb);
                ps.setString(i++, r.tab11TtCd);
                ps.setString(i++, r.tab11BcCb);
                ps.setString(i++, r.tab11BcCd1);
                ps.setString(i++, r.tab11BcCd2);
                ps.setString(i++, r.tab12KetLuan);
                ps.setString(i, r.tab12DeNghi);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
