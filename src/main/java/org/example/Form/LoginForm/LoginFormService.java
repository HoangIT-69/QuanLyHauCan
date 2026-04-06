package org.example.Form.LoginForm;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Nghiệp vụ đăng nhập và truy vấn CSDL. Không phụ thuộc Swing/AWT.
 */
public class LoginFormService {

    public enum LoginStatus {
        SUCCESS,
        INVALID_CREDENTIALS,
        DATABASE_UNAVAILABLE,
        ERROR
    }

    public static final class AuthenticatedUser {
        private final int userId;
        private final String username;
        private final String role;

        public AuthenticatedUser(int userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }

    public static final class LoginResult {
        private final LoginStatus status;
        private final AuthenticatedUser user;
        private final String errorDetail;

        private LoginResult(LoginStatus status, AuthenticatedUser user, String errorDetail) {
            this.status = status;
            this.user = user;
            this.errorDetail = errorDetail;
        }

        public static LoginResult success(AuthenticatedUser user) {
            return new LoginResult(LoginStatus.SUCCESS, user, null);
        }

        public static LoginResult invalidCredentials() {
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, null);
        }

        public static LoginResult databaseUnavailable() {
            return new LoginResult(LoginStatus.DATABASE_UNAVAILABLE, null, null);
        }

        public static LoginResult error(String message) {
            return new LoginResult(LoginStatus.ERROR, null, message);
        }

        public LoginStatus getStatus() {
            return status;
        }

        public AuthenticatedUser getUser() {
            return user;
        }

        public String getErrorDetail() {
            return errorDetail;
        }
    }

    /**
     * Xác thực tài khoản. Không hiển thị UI; chỉ trả về {@link LoginResult}.
     */
    public LoginResult authenticate(String username, String password) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            return LoginResult.databaseUnavailable();
        }

        String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String role = rs.getString("role");
                    return LoginResult.success(new AuthenticatedUser(userId, username, role));
                }
                return LoginResult.invalidCredentials();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return LoginResult.error(ex.getMessage());
        }
    }
}
