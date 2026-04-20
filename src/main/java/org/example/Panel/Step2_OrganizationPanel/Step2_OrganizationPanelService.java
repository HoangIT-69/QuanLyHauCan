package org.example.Panel.Step2_OrganizationPanel;

import org.example.Popup.UnitDataEntryDialog.UnitDataEntryDialogService;
import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Danh sách hướng/lực lượng theo hình thức tập bài (đồng bộ với lựa chọn trên {@code LoginFormUI}).
 */
public class Step2_OrganizationPanelService {

    private static final List<String> DIRECTIONS_PHONG_NGU = List.of(
            "Hướng chủ yếu",
            "Hướng thứ yếu",
            "Lực lượng chiến đấu vòng ngoài",
            "Lực lượng dự bị cơ động",
            "Lực lượng còn lại",
            "Hướng PN phía sau"
    );

    private static final List<String> DIRECTIONS_TIEN_CONG = List.of(
            "Hướng chủ yếu",
            "Hướng thứ yếu 1",
            "Hướng thứ yếu 2",
            "Dự bị binh chủng hợp thành",
            "Lực lượng còn lại",
            "Lực lượng chiến đấu tạo thế"
    );

    /**
     * Chuẩn hóa theo chuỗi từ đăng nhập: "Tiến công" / "Phòng ngự" (hoặc chứa các từ khóa tương ứng).
     */
    public List<String> getDirectionsForHinhThuc(String hinhThucTapBai) {
        if (hinhThucTapBai == null || hinhThucTapBai.isBlank()) {
            return List.copyOf(DIRECTIONS_TIEN_CONG);
        }
        String n = hinhThucTapBai.trim().toLowerCase();
        if (n.contains("phòng") && n.contains("ngự")) {
            return List.copyOf(DIRECTIONS_PHONG_NGU);
        }
        if (n.contains("tiến") && n.contains("công")) {
            return List.copyOf(DIRECTIONS_TIEN_CONG);
        }
        return List.copyOf(DIRECTIONS_TIEN_CONG);
    }

    public Set<String> loadDistinctHuongFromDb(int sessionId) {
        Set<String> out = new LinkedHashSet<>();
        if (sessionId <= 0) {
            return out;
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DISTINCT huong FROM step2_bien_che WHERE session_id = ?")) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String h = rs.getString("huong");
                        if (h != null && !h.isBlank()) {
                            out.add(h.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public void deleteUnitFromDatabase(int sessionId, String huong) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM step2_bien_che WHERE session_id = ? AND huong = ?")) {
                ps.setInt(1, sessionId);
                ps.setString(2, huong);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearStep2ForSession(int sessionId) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM step2_bien_che WHERE session_id = ?")) {
                ps.setInt(1, sessionId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lưu toàn bộ step2 từ bộ nhớ RAM ({@link UnitDataEntryDialogService#getSharedStore()}).
     * Chỉ ghi đè DB cho hướng nào có dữ liệu thật trong RAM (rows không rỗng).
     * Hướng nào vector rỗng (chưa mở dialog) sẽ bỏ qua — giữ nguyên dữ liệu DB cũ.
     */
    public boolean saveStep2FromRamStore(int sessionId) {
        Map<String, Vector<Vector<Object>>> store = UnitDataEntryDialogService.getSharedStore();
        if (store == null || store.isEmpty()) {
            return true;
        }
        if (sessionId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try {
                String sqlDel = "DELETE FROM step2_bien_che WHERE session_id = ? AND huong = ?";
                String sqlIns = "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, " +
                        "(SELECT id FROM quyuoc_bienche WHERE CONCAT('[', nhom_don_vi, '] ', ten_don_vi) = ? LIMIT 1), ?)";

                try (PreparedStatement del = conn.prepareStatement(sqlDel);
                     PreparedStatement ins = conn.prepareStatement(sqlIns)) {

                    // 1. Xóa DB các hướng đã bị xóa khỏi sa bàn (right-click delete)
                    for (String huong : UnitDataEntryDialogService.getDeletedHuong()) {
                        del.setInt(1, sessionId);
                        del.setString(2, huong);
                        del.addBatch();
                    }

                    // 2. Lưu các hướng có dữ liệu trong RAM (dialog đã được mở)
                    for (Map.Entry<String, Vector<Vector<Object>>> entry : store.entrySet()) {
                        String huong = entry.getKey();
                        Vector<Vector<Object>> rows = entry.getValue();

                        // Bỏ qua các key đặc biệt (sandbox node)
                        if ("Tiểu đoàn".equals(huong) || "Phối thuộc".equals(huong)) {
                            continue;
                        }

                        // Vector rỗng = dialog chưa bao giờ được mở → giữ nguyên DB
                        if (rows == null || rows.isEmpty()) {
                            continue;
                        }

                        // Dialog đã mở (vector có ít nhất header rows) → xóa DB cũ rồi insert lại
                        del.setInt(1, sessionId);
                        del.setString(2, huong);
                        del.addBatch();

                        int currentLoai = 1;
                        for (Vector<Object> row : rows) {
                            if (row.isEmpty() || row.get(0) == null) continue;
                            String rawName = row.get(0).toString();

                            if (rawName.startsWith("1.")) { currentLoai = 1; continue; }
                            if (rawName.startsWith("2.")) { currentLoai = 2; continue; }
                            if (rawName.equals("TỔNG CỘNG")) continue;

                            String name = rawName.replace("  + ", "").trim();
                            if (name.startsWith("[") && name.contains("]")) {
                                ins.setInt(1, sessionId);
                                ins.setString(2, huong);
                                ins.setString(3, name);
                                ins.setInt(4, currentLoai);
                                ins.addBatch();
                            }
                        }
                    }

                    del.executeBatch();
                    ins.executeBatch();
                }

                conn.commit();
                UnitDataEntryDialogService.clearDeletedHuong();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
