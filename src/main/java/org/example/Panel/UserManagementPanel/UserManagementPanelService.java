package org.example.Panel.UserManagementPanel;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserManagementPanelService {

    public boolean isProtectedSystemUser(String username) {
        return username != null && "admin".equalsIgnoreCase(username.trim());
    }

    public List<Object[]> loadAllUsers() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return rows;
            }
            String sql = "SELECT id, username, password, ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi, role FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("ma_quan_nhan"),
                            rs.getString("full_name"),
                            rs.getString("dob"),
                            rs.getString("rank"),
                            rs.getString("chuc_vu"),
                            rs.getString("don_vi"),
                            rs.getString("role")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public boolean insertUser(String username, String password, String role, String maQuanNhan,
                              String fullName, String dob, String rank, String chucVu, String donVi) {
        String sql = "INSERT INTO users (username, password, role, ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, role);
                pstmt.setString(4, maQuanNhan);
                pstmt.setString(5, fullName);
                pstmt.setString(6, dob);
                pstmt.setString(7, rank);
                pstmt.setString(8, chucVu);
                pstmt.setString(9, donVi);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(String username, String role, String maQuanNhan,
                              String fullName, String dob, String rank, String chucVu, String donVi) {
        String sql = "UPDATE users SET role=?, ma_quan_nhan=?, full_name=?, dob=?, rank=?, chuc_vu=?, don_vi=? WHERE username=?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, role);
                pstmt.setString(2, maQuanNhan);
                pstmt.setString(3, fullName);
                pstmt.setString(4, dob);
                pstmt.setString(5, rank);
                pstmt.setString(6, chucVu);
                pstmt.setString(7, donVi);
                pstmt.setString(8, username);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUserByUsername(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
