package org.example.Panel;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserProfilePanel extends JPanel {

    public UserProfilePanel(String username) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 15");

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 20, 30));
        header.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel("Thông tin hồ sơ của tài khoản: " + username + " (Nếu có sai sót hãy liên hệ Admin để thay đổi)");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(231, 76, 60));

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(5));
        header.add(lblSub);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 230, 230));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        header.add(Box.createVerticalStrut(15));
        header.add(line);
        card.add(header, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(new EmptyBorder(20, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.weightx = 1.0;

        int currentRow = 0;
        JTextField txtMaQN = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Mã Quân Nhân:");
        JTextField txtHoTen = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Họ và Tên:");
        JTextField txtNgaySinh = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Ngày sinh:");
        JTextField txtCapBac = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Cấp bậc:");
        JTextField txtChucVu = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Chức vụ:");
        JTextField txtDonVi = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Đơn vị:");

        loadDataFromDB(username, txtMaQN, txtHoTen, txtNgaySinh, txtCapBac, txtChucVu, txtDonVi);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Color.WHITE);
        contentArea.putClientProperty("FlatLaf.style", "arc: 15");
        contentArea.add(formContent, BorderLayout.NORTH);

        card.add(contentArea, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void loadDataFromDB(String username, JTextField txtMaQN, JTextField txtHoTen, JTextField txtNgaySinh, JTextField txtCapBac, JTextField txtChucVu, JTextField txtDonVi) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "SELECT ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi FROM users WHERE username = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    txtMaQN.setText(rs.getString("ma_quan_nhan") != null ? rs.getString("ma_quan_nhan") : "Chưa cập nhật");
                    txtHoTen.setText(rs.getString("full_name") != null ? rs.getString("full_name") : "Chưa cập nhật");
                    txtNgaySinh.setText(rs.getString("dob") != null ? rs.getString("dob") : "Chưa cập nhật");
                    txtCapBac.setText(rs.getString("rank") != null ? rs.getString("rank") : "Chưa cập nhật");
                    txtChucVu.setText(rs.getString("chuc_vu") != null ? rs.getString("chuc_vu") : "Chưa cập nhật");
                    txtDonVi.setText(rs.getString("don_vi") != null ? rs.getString("don_vi") : "Chưa cập nhật");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu cá nhân: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}