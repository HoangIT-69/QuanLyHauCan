package org.example.Panel.UserManagementPanel;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanelUI extends JPanel {
    private final UserManagementPanelService userService = new UserManagementPanelService();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtUsername, txtPassword, txtMaQN, txtHoTen, txtNgaySinh, txtChucVu, txtDonVi;
    private JComboBox<String> cbRole, cbCapBac;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public UserManagementPanelUI() {
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
        List<Object[]> rows = userService.loadAllUsers();
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    private void saveUser(boolean isNew) {
        String role = cbRole.getSelectedItem() != null ? cbRole.getSelectedItem().toString() : "";
        String capBac = cbCapBac.getSelectedItem() != null ? cbCapBac.getSelectedItem().toString() : "";

        boolean ok;
        if (isNew) {
            ok = userService.insertUser(
                    txtUsername.getText(),
                    txtPassword.getText(),
                    role,
                    txtMaQN.getText(),
                    txtHoTen.getText(),
                    txtNgaySinh.getText(),
                    capBac,
                    txtChucVu.getText(),
                    txtDonVi.getText()
            );
        } else {
            ok = userService.updateUser(
                    txtUsername.getText(),
                    role,
                    txtMaQN.getText(),
                    txtHoTen.getText(),
                    txtNgaySinh.getText(),
                    capBac,
                    txtChucVu.getText(),
                    txtDonVi.getText()
            );
        }
        if (ok) {
            JOptionPane.showMessageDialog(this, "Thành công!");
            loadData();
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        String user = table.getValueAt(row, 1).toString();
        if (userService.isProtectedSystemUser(user)) {
            JOptionPane.showMessageDialog(this, "Không thể xóa admin hệ thống!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa tài khoản " + user + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deleteUserByUsername(user)) {
                loadData();
                clearForm();
            }
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtUsername.setText(table.getValueAt(row, 1).toString());
            txtUsername.setEditable(false);
            txtPassword.setText(table.getValueAt(row, 2).toString());
            txtPassword.setEnabled(true);
            txtMaQN.setText(readCell(row, 3));
            txtHoTen.setText(readCell(row, 4));
            txtNgaySinh.setText(table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "");
            Object capBac = table.getValueAt(row, 6);
            cbCapBac.setSelectedItem(capBac != null ? capBac.toString() : "");
            txtChucVu.setText(readCell(row, 7));
            txtDonVi.setText(readCell(row, 8));
            Object r = table.getValueAt(row, 9);
            cbRole.setSelectedItem(r != null ? r.toString() : "USER");
        }
    }

    private String readCell(int row, int col) {
        Object v = table.getValueAt(row, col);
        return v != null ? v.toString() : "";
    }

    private void clearForm() {
        txtUsername.setText("");
        txtUsername.setEditable(true);
        txtPassword.setText("");
        txtPassword.setEnabled(true);
        txtMaQN.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        txtChucVu.setText("");
        txtDonVi.setText("");
        table.clearSelection();
    }
}
