package org.example.Tab.Step4_Regulation.MaterialRegulationTab;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MaterialRegulationTabService {

    public static final class CatalogPickRow {
        public final String name;
        public final String dvt;
        public final boolean isDan;
        public final boolean isVtkt;

        public CatalogPickRow(String name, String dvt, boolean isDan, boolean isVtkt) {
            this.name = name;
            this.dvt = dvt != null ? dvt : "";
            this.isDan = isDan;
            this.isVtkt = isVtkt;
        }

        public String categoryTag() {
            if (isDan) {
                return "Đạn";
            }
            return isVtkt ? "VTKT" : "VCHC";
        }
    }

    public static final class LoadedRow {
        public final int loai;
        public final String vatChat;
        public final String dvt;
        public final double duTru;
        public final double phaiCo0400;
        public final double phaiCoScd;
        public final double tieuThuGdcb;
        public final double tieuThuGdcd;
        public final String dtChitiet;
        public final String pc04Chitiet;
        public final String scdChitiet;
        public final String gdcbChitiet;
        public final String gdcdChitiet;

        public LoadedRow(int loai, String vatChat, String dvt, double duTru, double phaiCo0400, double phaiCoScd,
                         double tieuThuGdcb, double tieuThuGdcd, String dtChitiet, String pc04Chitiet,
                         String scdChitiet, String gdcbChitiet, String gdcdChitiet) {
            this.loai = loai;
            this.vatChat = vatChat;
            this.dvt = dvt;
            this.duTru = duTru;
            this.phaiCo0400 = phaiCo0400;
            this.phaiCoScd = phaiCoScd;
            this.tieuThuGdcb = tieuThuGdcb;
            this.tieuThuGdcd = tieuThuGdcd;
            this.dtChitiet = dtChitiet != null ? dtChitiet : "";
            this.pc04Chitiet = pc04Chitiet != null ? pc04Chitiet : "";
            this.scdChitiet = scdChitiet != null ? scdChitiet : "";
            this.gdcbChitiet = gdcbChitiet != null ? gdcbChitiet : "";
            this.gdcdChitiet = gdcdChitiet != null ? gdcdChitiet : "";
        }
    }

    public static final class SaveRow {
        public final int loaiVatChat;
        public final String vatChat;
        public final String dvt;
        public final double duTru;
        public final double phaiCo0400;
        public final double phaiCoScd;
        public final double tieuThuGdcb;
        public final double tieuThuGdcd;
        public final String dtChitietJoined;
        public final String pc04ChitietJoined;
        public final String scdChitietJoined;
        public final String gdcbChitietJoined;
        public final String gdcdChitietJoined;

        public SaveRow(int loaiVatChat, String vatChat, String dvt, double duTru, double phaiCo0400, double phaiCoScd,
                       double tieuThuGdcb, double tieuThuGdcd, String dtChitietJoined, String pc04ChitietJoined,
                       String scdChitietJoined, String gdcbChitietJoined, String gdcdChitietJoined) {
            this.loaiVatChat = loaiVatChat;
            this.vatChat = vatChat;
            this.dvt = dvt;
            this.duTru = duTru;
            this.phaiCo0400 = phaiCo0400;
            this.phaiCoScd = phaiCoScd;
            this.tieuThuGdcb = tieuThuGdcb;
            this.tieuThuGdcd = tieuThuGdcd;
            this.dtChitietJoined = dtChitietJoined;
            this.pc04ChitietJoined = pc04ChitietJoined;
            this.scdChitietJoined = scdChitietJoined;
            this.gdcbChitietJoined = gdcbChitietJoined;
            this.gdcdChitietJoined = gdcdChitietJoined;
        }
    }

    public boolean isCatalogMaterial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String t = name.trim();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM quyuoc_dan WHERE loai_dan = ?")) {
                ps.setString(1, t);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = ?")) {
                ps.setString(1, t);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<CatalogPickRow> loadCatalogForPicker(Set<String> excludeNames) {
        List<CatalogPickRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return list;
            }
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs1 = stmt.executeQuery("SELECT loai_dan, don_vi_tinh FROM quyuoc_dan")) {
                    while (rs1.next()) {
                        String name = rs1.getString(1);
                        if (name != null && !excludeNames.contains(name.trim())) {
                            list.add(new CatalogPickRow(name, rs1.getString(2), true, false));
                        }
                    }
                }
                try (ResultSet rs2 = stmt.executeQuery("SELECT ten_vat_chat, don_vi_tinh, danh_muc FROM quyuoc_vchc")) {
                    while (rs2.next()) {
                        String name = rs2.getString(1);
                        if (name != null && !excludeNames.contains(name.trim())) {
                            String danhMuc = rs2.getString(3);
                            list.add(new CatalogPickRow(name, rs2.getString(2), false, isVtktCategory(danhMuc)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static boolean isVtktCategory(String danhMuc) {
        if (danhMuc == null) {
            return false;
        }
        String s = danhMuc.trim().toLowerCase(Locale.ROOT);
        return "vtkt".equals(s) || s.contains("vật tư kỹ thuật") || s.contains("vat tu ky thuat");
    }

    public List<LoadedRow> loadQuyDinhDuTru(int sessionId) {
        List<LoadedRow> out = new ArrayList<>();
        if (sessionId < 0) {
            return out;
        }
        String sql = "SELECT * FROM step4_quy_dinh_du_tru WHERE session_id = ? ORDER BY loai_vat_chat ASC, id ASC";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        out.add(new LoadedRow(
                                rs.getInt("loai_vat_chat"),
                                rs.getString("vat_chat"),
                                rs.getString("dvt"),
                                rs.getDouble("du_tru"),
                                rs.getDouble("phai_co_0400"),
                                rs.getDouble("phai_co_scd"),
                                rs.getDouble("tieu_thu_gdcb"),
                                rs.getDouble("tieu_thu_gdcd"),
                                rs.getString("dt_chitiet"),
                                rs.getString("pc04_chitiet"),
                                rs.getString("scd_chitiet"),
                                rs.getString("gdcb_chitiet"),
                                rs.getString("gdcd_chitiet")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public boolean saveQuyDinhDuTru(int sessionId, List<SaveRow> rows) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM step4_quy_dinh_du_tru WHERE session_id = ?")) {
                delStmt.setInt(1, sessionId);
                delStmt.executeUpdate();
            }

            String sql = "INSERT INTO step4_quy_dinh_du_tru (session_id, loai_vat_chat, vat_chat, dvt, du_tru, phai_co_0400, phai_co_scd, tieu_thu_gdcb, tieu_thu_gdcd, dt_chitiet, pc04_chitiet, scd_chitiet, gdcb_chitiet, gdcd_chitiet) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (SaveRow r : rows) {
                    pstmt.setInt(1, sessionId);
                    pstmt.setInt(2, r.loaiVatChat);
                    pstmt.setString(3, r.vatChat);
                    pstmt.setString(4, r.dvt);
                    pstmt.setDouble(5, r.duTru);
                    pstmt.setDouble(6, r.phaiCo0400);
                    pstmt.setDouble(7, r.phaiCoScd);
                    pstmt.setDouble(8, r.tieuThuGdcb);
                    pstmt.setDouble(9, r.tieuThuGdcd);
                    pstmt.setString(10, r.dtChitietJoined);
                    pstmt.setString(11, r.pc04ChitietJoined);
                    pstmt.setString(12, r.scdChitietJoined);
                    pstmt.setString(13, r.gdcbChitietJoined);
                    pstmt.setString(14, r.gdcdChitietJoined);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
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
}
