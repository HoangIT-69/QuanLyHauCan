package org.example.Panel.CalculationConventionPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.NumberParseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CalculationConventionPanelService {

    private static final String SQL_VCHC_ORDER = "SELECT * FROM quyuoc_vchc ORDER BY " +
            "CASE danh_muc WHEN 'Quân lương' THEN 1 WHEN 'Quân trang' THEN 2 " +
            "WHEN 'Quân y' THEN 3 WHEN 'Doanh trại' THEN 4 WHEN 'Khác' THEN 99 ELSE 5 END, ten_vat_chat";

    private static final String SQL_DAN_ORDER = "SELECT * FROM quyuoc_dan ORDER BY " +
            "CASE danh_muc WHEN 'Đạn phòng không' THEN 1 WHEN 'Đạn chống tăng' THEN 2 " +
            "WHEN 'Đạn BB nhóm I' THEN 3 WHEN 'Đạn BB nhóm II' THEN 4 WHEN 'Mìn, Lựu đạn' THEN 99 ELSE 5 END, loai_dan";

    private static final String SQL_BIENCHE_ORDER = "SELECT * FROM quyuoc_bienche ORDER BY " +
            "CASE nhom_don_vi WHEN 'Tiểu đoàn' THEN 1 WHEN 'Trung đoàn' THEN 2 " +
            "WHEN 'Sư đoàn' THEN 3 WHEN 'Khác' THEN 99 ELSE 4 END, ten_don_vi";

    public List<Object[]> loadVatChatRows() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return rows;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(SQL_VCHC_ORDER)) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("danh_muc"),
                            rs.getString("ten_vat_chat"),
                            rs.getFloat("quy_uoc"),
                            rs.getString("don_vi_quy_uoc"),
                            rs.getString("don_vi_tinh")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public boolean insertVatChat(String danhMuc, String tenVatChat, float quyUoc, String donViQuyUoc, String donViTinh) {
        String sql = "INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, danhMuc);
                pstmt.setString(2, tenVatChat);
                pstmt.setFloat(3, quyUoc);
                pstmt.setString(4, donViQuyUoc);
                pstmt.setString(5, donViTinh);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateVatChat(int id, String danhMuc, String tenVatChat, float quyUoc, String donViQuyUoc, String donViTinh) {
        String sql = "UPDATE quyuoc_vchc SET danh_muc=?, ten_vat_chat=?, quy_uoc=?, don_vi_quy_uoc=?, don_vi_tinh=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, danhMuc);
                pstmt.setString(2, tenVatChat);
                pstmt.setFloat(3, quyUoc);
                pstmt.setString(4, donViQuyUoc);
                pstmt.setString(5, donViTinh);
                pstmt.setInt(6, id);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteVatChatById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM quyuoc_vchc WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> loadDanRows() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return rows;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(SQL_DAN_ORDER)) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("danh_muc"),
                            rs.getString("loai_dan"),
                            rs.getInt("so_vien_tren_coso"),
                            rs.getFloat("trong_luong_1_vien"),
                            rs.getString("don_vi_tinh")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public boolean insertDan(String danhMuc, String loaiDan, int soVien, float trongLuong, String donViTinh) {
        String sql = "INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, danhMuc);
                pstmt.setString(2, loaiDan);
                pstmt.setInt(3, soVien);
                pstmt.setFloat(4, trongLuong);
                pstmt.setString(5, donViTinh);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDan(int id, String danhMuc, String loaiDan, int soVien, float trongLuong, String donViTinh) {
        String sql = "UPDATE quyuoc_dan SET danh_muc=?, loai_dan=?, so_vien_tren_coso=?, trong_luong_1_vien=?, don_vi_tinh=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, danhMuc);
                pstmt.setString(2, loaiDan);
                pstmt.setInt(3, soVien);
                pstmt.setFloat(4, trongLuong);
                pstmt.setString(5, donViTinh);
                pstmt.setInt(6, id);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDanById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM quyuoc_dan WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> loadBienCheRows() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return rows;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(SQL_BIENCHE_ORDER)) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("nhom_don_vi"),
                            rs.getString("ten_don_vi"),
                            rs.getInt("quan_so"),
                            rs.getInt("luu_dan"),
                            rs.getInt("sung_ngan"),
                            rs.getInt("tieu_lien"),
                            rs.getInt("trung_lien"),
                            rs.getInt("dai_lien"),
                            rs.getInt("b41"),
                            rs.getInt("co60mm"),
                            rs.getInt("co82mm"),
                            rs.getInt("co100mm"),
                            rs.getInt("spg9"),
                            rs.getInt("smpk_127mm")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * @param fieldTexts độ dài 14: [0] nhóm ĐV, [1] tên ĐV, [2..13] các chỉ số số nguyên (chuỗi)
     */
    public boolean insertBienChe(String[] fieldTexts) {
        String sql = "INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        return saveBienCheInternal(sql, -1, fieldTexts, true);
    }

    public boolean updateBienChe(int id, String[] fieldTexts) {
        String sql = "UPDATE quyuoc_bienche SET nhom_don_vi=?, ten_don_vi=?, quan_so=?, luu_dan=?, sung_ngan=?, tieu_lien=?, trung_lien=?, dai_lien=?, b41=?, co60mm=?, co82mm=?, co100mm=?, spg9=?, smpk_127mm=? WHERE id=?";
        return saveBienCheInternal(sql, id, fieldTexts, false);
    }

    private boolean saveBienCheInternal(String sql, int id, String[] fieldTexts, boolean insert) {
        if (fieldTexts == null || fieldTexts.length < 14) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, fieldTexts[0]);
                pstmt.setString(2, fieldTexts[1]);
                int p = 3;
                for (int i = 2; i <= 13; i++) {
                    pstmt.setInt(p++, NumberParseUtils.parseInt(fieldTexts[i], 0));
                }
                if (!insert) {
                    pstmt.setInt(15, id);
                }
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBienCheById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM quyuoc_bienche WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public float parseQuyUocFromText(String text) {
        return NumberParseUtils.parseFloat(text, 0f);
    }

    public int parseSoVienFromText(String text) {
        return NumberParseUtils.parseInt(text, 0);
    }
}
