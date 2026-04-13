package org.example.Panel.UserProfilePanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserProfilePanelService {

    public static final class ProfileLoadResult {
        private final boolean success;
        private final Map<String, String> fields;
        private final String errorMessage;

        private ProfileLoadResult(boolean success, Map<String, String> fields, String errorMessage) {
            this.success = success;
            this.fields = fields;
            this.errorMessage = errorMessage;
        }

        public static ProfileLoadResult ok(Map<String, String> fields) {
            return new ProfileLoadResult(true, fields, null);
        }

        public static ProfileLoadResult fail(String message) {
            return new ProfileLoadResult(false, new LinkedHashMap<>(), message);
        }

        public boolean isSuccess() {
            return success;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public ProfileLoadResult loadProfileByUsername(String username) {
        Map<String, String> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return ProfileLoadResult.ok(map);
            }
            String sql = "SELECT ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        map.put("ma_quan_nhan", rs.getString("ma_quan_nhan"));
                        map.put("full_name", rs.getString("full_name"));
                        map.put("dob", rs.getString("dob"));
                        map.put("rank", rs.getString("rank"));
                        map.put("chuc_vu", rs.getString("chuc_vu"));
                        map.put("don_vi", rs.getString("don_vi"));
                    }
                }
            }
            return ProfileLoadResult.ok(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ProfileLoadResult.fail(ex.getMessage());
        }
    }
}
