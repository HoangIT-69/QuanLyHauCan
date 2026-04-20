package org.example.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Giữ thông tin người dùng đang đăng nhập (singleton tĩnh).
 * Set một lần tại login, đọc từ bất kỳ đâu trong ứng dụng.
 */
public final class AppSession {

    private static String username = "";
    private static String fullName = "";
    private static int userId = -1;

    private AppSession() {
    }

    public static void set(int uid, String uname) {
        userId = uid;
        username = uname != null ? uname.trim() : "";
        fullName = "";

        // Lấy full_name từ DB ngay khi login
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT full_name FROM users WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String fn = rs.getString("full_name");
                            if (fn != null && !fn.isBlank()) {
                                fullName = fn.trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    /** Họ tên đầy đủ từ bảng users, fallback về username nếu chưa có. */
    public static String getFullName() {
        return fullName.isBlank() ? username : fullName;
    }
}
