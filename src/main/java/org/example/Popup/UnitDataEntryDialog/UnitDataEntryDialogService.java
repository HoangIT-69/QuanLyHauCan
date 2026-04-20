package org.example.Popup.UnitDataEntryDialog;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * RAM cache biên chế theo từng hướng (dùng chung Step 2 + dialog nhập đơn vị).
 */
public class UnitDataEntryDialogService {

    private static final Map<String, Vector<Vector<Object>>> SHARED_STORE = new HashMap<>();

    /** Tập hướng đã bị xóa khỏi sa bàn — cần DELETE khỏi DB khi lưu. */
    private static final Set<String> DELETED_HUONG = new LinkedHashSet<>();

    public static Map<String, Vector<Vector<Object>>> getSharedStore() {
        return SHARED_STORE;
    }

    public static void clearSharedStore() {
        SHARED_STORE.clear();
        DELETED_HUONG.clear();
    }

    public static void markHuongDeleted(String huong) {
        DELETED_HUONG.add(huong);
    }

    public static Set<String> getDeletedHuong() {
        return DELETED_HUONG;
    }

    public static void clearDeletedHuong() {
        DELETED_HUONG.clear();
    }

    public Set<String> loadUsedTenDonViAcrossSession(int sessionId) {
        Set<String> addedUnits = new HashSet<>();
        String sqlCheck = "SELECT q.ten_don_vi FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return addedUnits;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rsCheck = pstmt.executeQuery()) {
                    while (rsCheck.next()) {
                        addedUnits.add(rsCheck.getString("ten_don_vi").trim());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return addedUnits;
    }

    public List<Map<String, Object>> loadAvailableBiencheNotIn(Set<String> excludeTenDonVi) {
        List<Map<String, Object>> dbUnits = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return dbUnits;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM quyuoc_bienche")) {
                while (rs.next()) {
                    String name = rs.getString("ten_don_vi").trim();
                    if (!excludeTenDonVi.contains(name)) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("name", name);
                        data.put("nhom", rs.getString("nhom_don_vi"));
                        data.put("qs", rs.getInt("quan_so"));
                        data.put("sn", rs.getInt("sung_ngan"));
                        data.put("tl", rs.getInt("tieu_lien"));
                        data.put("trl", rs.getInt("trung_lien"));
                        data.put("dl", rs.getInt("dai_lien"));
                        data.put("b41", rs.getInt("b41"));
                        data.put("ld", rs.getInt("luu_dan"));
                        data.put("c60", rs.getInt("co60mm"));
                        data.put("c82", rs.getInt("co82mm"));
                        data.put("c100", rs.getInt("co100mm"));
                        data.put("spg9", rs.getInt("spg9"));
                        data.put("smpk", rs.getInt("smpk_127mm"));
                        dbUnits.add(data);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dbUnits;
    }

    public static final class LoadedUnitData {
        public final List<Object[]> listBienChe;
        public final List<Object[]> listTangCuong;
        public final boolean hasDataInDb;

        public LoadedUnitData(List<Object[]> listBienChe, List<Object[]> listTangCuong, boolean hasDataInDb) {
            this.listBienChe = listBienChe;
            this.listTangCuong = listTangCuong;
            this.hasDataInDb = hasDataInDb;
        }
    }

    public LoadedUnitData loadUnitFromDatabase(int sessionId, String unitName) {
        List<Object[]> listBienChe = new ArrayList<>();
        List<Object[]> listTangCuong = new ArrayList<>();
        boolean hasDataInDb = false;

        String sql = "SELECT s.phan_loai, q.* FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ? AND s.huong = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new LoadedUnitData(listBienChe, listTangCuong, false);
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                pstmt.setString(2, unitName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        hasDataInDb = true;
                        int phanLoai = rs.getInt("phan_loai");
                        String fullName = "[" + rs.getString("nhom_don_vi") + "] " + rs.getString("ten_don_vi");
                        Object[] rowData = new Object[]{
                                "  + " + fullName,
                                rs.getInt("quan_so"), rs.getInt("sung_ngan"), rs.getInt("tieu_lien"),
                                rs.getInt("trung_lien"), rs.getInt("dai_lien"), rs.getInt("b41"),
                                rs.getInt("luu_dan"), rs.getInt("co60mm"), rs.getInt("co82mm"),
                                rs.getInt("co100mm"), rs.getInt("spg9"), rs.getInt("smpk_127mm"),
                                0, 0
                        };
                        if (phanLoai == 1) {
                            listBienChe.add(rowData);
                        } else {
                            listTangCuong.add(rowData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp dữ liệu đơn vị từ DB: " + e.getMessage());
        }
        return new LoadedUnitData(listBienChe, listTangCuong, hasDataInDb);
    }

    public void putRamCopy(String unitName, Vector<Vector<Object>> data) {
        SHARED_STORE.put(unitName, data);
    }

    public void removeRam(String unitName) {
        SHARED_STORE.remove(unitName);
    }

    public boolean saveUnitToDatabase(int sessionId, String currentUnit, Vector<Vector<Object>> data) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM step2_bien_che WHERE session_id = ? AND huong = ?")) {
                del.setInt(1, sessionId);
                del.setString(2, currentUnit);
                del.executeUpdate();
            }

            String sql = "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, (SELECT id FROM quyuoc_bienche WHERE CONCAT('[', nhom_don_vi, '] ', ten_don_vi) = ? LIMIT 1), ?)";
            boolean hasUnits = false;
            try (PreparedStatement ins = conn.prepareStatement(sql)) {
                int currentLoai = 1;
                for (Vector<Object> row : data) {
                    if (row.isEmpty() || row.get(0) == null) {
                        continue;
                    }
                    String rawName = row.get(0).toString();

                    if (rawName.startsWith("1.")) {
                        currentLoai = 1;
                        continue;
                    }
                    if (rawName.startsWith("2.")) {
                        currentLoai = 2;
                        continue;
                    }
                    if (rawName.equals("TỔNG CỘNG")) {
                        continue;
                    }

                    String name = rawName.replace("  + ", "").trim();
                    if (name.startsWith("[") && name.contains("]")) {
                        ins.setInt(1, sessionId);
                        ins.setString(2, currentUnit);
                        ins.setString(3, name);
                        ins.setInt(4, currentLoai);
                        ins.addBatch();
                        hasUnits = true;
                    }
                }
                if (hasUnits) {
                    ins.executeBatch();
                } else {
                    try (PreparedStatement insFallback = conn.prepareStatement(
                            "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, 1, 1)")) {
                        insFallback.setInt(1, sessionId);
                        insFallback.setString(2, currentUnit);
                        insFallback.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
