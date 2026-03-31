package org.example.Panel;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class UserManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtUsername, txtPassword, txtMaQN, txtHoTen, txtNgaySinh, txtChucVu, txtDonVi;
    private JComboBox<String> cbRole, cbCapBac;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.putClientProperty("FlatLaf.style", "arc: 15");
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIUtils.addInput(inputPanel, gbc, 0, 0, "Tên đăng nhập:", txtUsername = new JTextField());
        UIUtils.addInput(inputPanel, gbc, 1, 0, "Mật khẩu:", txtPassword = new JTextField());
        UIUtils.addInput(inputPanel, gbc, 2, 0, "Quyền:", cbRole = new JComboBox<>(new String[]{"ADMIN", "USER"}));
        UIUtils.addInput(inputPanel, gbc, 3, 0, "Mã Quân Nhân:", txtMaQN = new JTextField());

        UIUtils.addInput(inputPanel, gbc, 0, 2, "Họ và Tên:", txtHoTen = new JTextField());
        UIUtils.addInput(inputPanel, gbc, 1, 2, "Ngày sinh (YYYY-MM-DD):", txtNgaySinh = new JTextField());
        UIUtils.addInput(inputPanel, gbc, 2, 2, "Cấp bậc:", cbCapBac = new JComboBox<>(new String[]{"Binh nhì", "Binh nhất", "Hạ sĩ", "Trung sĩ", "Thượng sĩ", "Thiếu úy", "Trung úy", "Thượng úy", "Đại úy", "Thiếu tá", "Trung tá", "Thượng tá", "Đại tá"}));
        UIUtils.addInput(inputPanel, gbc, 3, 2, "Chức vụ:", txtChucVu = new JTextField());

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Đơn vị:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        inputPanel.add(txtDonVi = new JTextField(), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAdd = UIUtils.createStyledButton("Thêm mới", new Color(46, 204, 113)));
        buttonPanel.add(btnUpdate = UIUtils.createStyledButton("Cập nhật", new Color(52, 152, 219)));
        buttonPanel.add(btnDelete = UIUtils.createStyledButton("Xóa", new Color(231, 76, 60)));
        buttonPanel.add(btnClear = UIUtils.createStyledButton("Làm mới", new Color(149, 165, 166)));

        formCard.add(inputPanel, BorderLayout.CENTER);
        formCard.add(buttonPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tài khoản", "Mật khẩu", "Mã QN", "Họ Tên", "Ngày sinh", "Cấp bậc", "Chức vụ", "Đơn vị", "Role"}, 0
        );
        table = new JTable(tableModel);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(formCard, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        loadData();
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());
        btnAdd.addActionListener(e -> saveUser(true));
        btnUpdate.addActionListener(e -> saveUser(false));
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, password, ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi, role FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("password"));
                row.add(rs.getString("ma_quan_nhan"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("dob"));
                row.add(rs.getString("rank"));
                row.add(rs.getString("chuc_vu"));
                row.add(rs.getString("don_vi"));
                row.add(rs.getString("role"));
                tableModel.addRow(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveUser(boolean isNew) {
        String sql = isNew ?
                "INSERT INTO users (username, password, role, ma_quan_nhan, full_name, dob, rank, chuc_vu, don_vi) VALUES (?,?,?,?,?,?,?,?,?)" :
                "UPDATE users SET role=?, ma_quan_nhan=?, full_name=?, dob=?, rank=?, chuc_vu=?, don_vi=? WHERE username=?";

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (isNew) {
                pstmt.setString(1, txtUsername.getText());
                pstmt.setString(2, txtPassword.getText());
                pstmt.setString(3, cbRole.getSelectedItem().toString());
                pstmt.setString(4, txtMaQN.getText());
                pstmt.setString(5, txtHoTen.getText());
                pstmt.setString(6, txtNgaySinh.getText());
                pstmt.setString(7, cbCapBac.getSelectedItem().toString());
                pstmt.setString(8, txtChucVu.getText());
                pstmt.setString(9, txtDonVi.getText());
            } else {
                pstmt.setString(1, cbRole.getSelectedItem().toString());
                pstmt.setString(2, txtMaQN.getText());
                pstmt.setString(3, txtHoTen.getText());
                pstmt.setString(4, txtNgaySinh.getText());
                pstmt.setString(5, cbCapBac.getSelectedItem().toString());
                pstmt.setString(6, txtChucVu.getText());
                pstmt.setString(7, txtDonVi.getText());
                pstmt.setString(8, txtUsername.getText());
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thành công!");
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String user = table.getValueAt(row, 1).toString();
        if (user.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Không thể xóa admin hệ thống!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa tài khoản " + user + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE username = ?");
                pstmt.setString(1, user);
                pstmt.executeUpdate();
                loadData();
                clearForm();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtUsername.setText(table.getValueAt(row, 1).toString());
            txtUsername.setEditable(false);
            txtPassword.setText(table.getValueAt(row, 2).toString());
            txtPassword.setEnabled(true);
            txtMaQN.setText(table.getValueAt(row, 3).toString());
            txtHoTen.setText(table.getValueAt(row, 4).toString());
            txtNgaySinh.setText(table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "");
            cbCapBac.setSelectedItem(table.getValueAt(row, 6).toString());
            txtChucVu.setText(table.getValueAt(row, 7).toString());
            txtDonVi.setText(table.getValueAt(row, 8).toString());
            cbRole.setSelectedItem(table.getValueAt(row, 9).toString());
        }
    }

    private void clearForm() {
        txtUsername.setText(""); txtUsername.setEditable(true);
        txtPassword.setText(""); txtPassword.setEnabled(true);
        txtMaQN.setText(""); txtHoTen.setText(""); txtNgaySinh.setText("");
        txtChucVu.setText(""); txtDonVi.setText("");
        table.clearSelection();
    }
}