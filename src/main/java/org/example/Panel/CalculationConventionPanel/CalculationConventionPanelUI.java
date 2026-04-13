package org.example.Panel.CalculationConventionPanel;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CalculationConventionPanelUI extends JPanel {
    private final CalculationConventionPanelService conventionService = new CalculationConventionPanelService();

    private JTabbedPane tabbedPane;

    private DefaultTableModel modelVatChat;
    private JTable tableVatChat;
    private JComboBox<String> cbDanhMucVC;
    private JTextField txtTenVC, txtQuyUocVC, txtDonViQuyUocVC, txtDVTinhVC;
    private int selectedIdVC = -1;

    private DefaultTableModel modelDan;
    private JTable tableDan;
    private JComboBox<String> cbDanhMucDan;
    private JTextField txtLoaiDan, txtSoVienDan, txtTrongLuongDan, txtDVTinhDan;
    private int selectedIdDan = -1;

    private DefaultTableModel modelBienChe;
    private JTable tableBienChe;
    private JTextField[] fieldsBC;
    private int selectedIdBC = -1;

    public CalculationConventionPanelUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitle = new JLabel("QUẢN LÝ QUY ƯỚC TÍNH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tabbedPane.addTab("Vật chất Hậu cần", createVatChatPanel());
        tabbedPane.addTab("Quy ước Đạn", createQuyUocDanPanel());
        tabbedPane.addTab("Biên chế Vũ khí", createBienChePanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadDataVatChat();
        loadDataDan();
        loadDataBienChe();
    }

    private JPanel createVatChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông tin Vật chất"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbDanhMucVC = new JComboBox<>(new String[]{"Quân lương", "Quân trang", "Quân y", "Doanh trại", "VTKT", "Khác"});
        cbDanhMucVC.setEditable(true);
        txtTenVC = new JTextField();
        txtQuyUocVC = new JTextField();
        InputValidator.restrictToNumbers(txtQuyUocVC, true);
        txtDonViQuyUocVC = new JTextField();
        txtDVTinhVC = new JTextField();

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0; formPanel.add(new JLabel("Danh mục:"), gbc);
        gbc.gridx = 1; cbDanhMucVC.setPreferredSize(new Dimension(200, 35)); formPanel.add(cbDanhMucVC, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Tên vật chất:"), gbc);
        gbc.gridx = 3; txtTenVC.setPreferredSize(new Dimension(250, 35)); formPanel.add(txtTenVC, gbc);
        gbc.gridx = 4; gbc.weightx = 1.0; formPanel.add(new JLabel(""), gbc); gbc.weightx = 0;

        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(new JLabel("Chỉ số quy ước:"), gbc);
        gbc.gridx = 1; txtQuyUocVC.setPreferredSize(new Dimension(200, 35)); formPanel.add(txtQuyUocVC, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Đơn vị Q.Ước:"), gbc);
        gbc.gridx = 3; txtDonViQuyUocVC.setPreferredSize(new Dimension(250, 35)); formPanel.add(txtDonViQuyUocVC, gbc);

        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(new JLabel("Đơn vị tính chung:"), gbc);
        gbc.gridx = 1; txtDVTinhVC.setPreferredSize(new Dimension(200, 35)); formPanel.add(txtDVTinhVC, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        JButton btnAdd = UIUtils.createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnUpdate = UIUtils.createBtn("Cập nhật", new Color(41, 128, 185));
        JButton btnDelete = UIUtils.createBtn("Xóa", new Color(231, 76, 60));
        JButton btnClear = UIUtils.createBtn("Làm mới", Color.GRAY);
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);

        modelVatChat = new DefaultTableModel(new String[]{"ID", "Danh mục", "Tên vật chất", "Quy ước", "Đơn vị quy ước", "ĐVT chung"}, 0);
        tableVatChat = new JTable(modelVatChat);
        tableVatChat.setRowHeight(25);
        tableVatChat.setFillsViewportHeight(true);
        tableVatChat.removeColumn(tableVatChat.getColumnModel().getColumn(0));

        panel.add(new JScrollPane(tableVatChat), BorderLayout.CENTER);

        tableVatChat.getSelectionModel().addListSelectionListener(e -> {
            int row = tableVatChat.getSelectedRow();
            if (row >= 0) {
                selectedIdVC = (int) modelVatChat.getValueAt(row, 0);
                cbDanhMucVC.setSelectedItem(modelVatChat.getValueAt(row, 1).toString());
                txtTenVC.setText(modelVatChat.getValueAt(row, 2).toString());
                txtQuyUocVC.setText(modelVatChat.getValueAt(row, 3).toString());
                txtDonViQuyUocVC.setText(modelVatChat.getValueAt(row, 4).toString());
                txtDVTinhVC.setText(modelVatChat.getValueAt(row, 5).toString());
            }
        });

        btnAdd.addActionListener(e -> saveVatChat(true));
        btnUpdate.addActionListener(e -> saveVatChat(false));
        btnDelete.addActionListener(e -> deleteVatChat());
        btnClear.addActionListener(e -> {
            selectedIdVC = -1;
            txtTenVC.setText("");
            txtQuyUocVC.setText("");
            txtDonViQuyUocVC.setText("");
            txtDVTinhVC.setText("");
            tableVatChat.clearSelection();
        });

        return panel;
    }

    private void loadDataVatChat() {
        modelVatChat.setRowCount(0);
        List<Object[]> rows = conventionService.loadVatChatRows();
        for (Object[] r : rows) {
            modelVatChat.addRow(r);
        }
    }

    private void saveVatChat(boolean isNew) {
        if (!isNew && selectedIdVC == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để cập nhật!");
            return;
        }
        String danhMuc = cbDanhMucVC.getSelectedItem() != null ? cbDanhMucVC.getSelectedItem().toString() : "";
        float quyUoc = conventionService.parseQuyUocFromText(txtQuyUocVC.getText());
        boolean ok = isNew
                ? conventionService.insertVatChat(danhMuc, txtTenVC.getText(), quyUoc, txtDonViQuyUocVC.getText(), txtDVTinhVC.getText())
                : conventionService.updateVatChat(selectedIdVC, danhMuc, txtTenVC.getText(), quyUoc, txtDonViQuyUocVC.getText(), txtDVTinhVC.getText());
        if (ok) {
            loadDataVatChat();
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!");
        }
    }

    private void deleteVatChat() {
        if (selectedIdVC == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa [" + txtTenVC.getText() + "] không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && conventionService.deleteVatChatById(selectedIdVC)) {
            loadDataVatChat();
            selectedIdVC = -1;
            txtTenVC.setText("");
            txtQuyUocVC.setText("");
            txtDonViQuyUocVC.setText("");
            txtDVTinhVC.setText("");
        }
    }

    private JPanel createQuyUocDanPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông tin Đạn dược"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbDanhMucDan = new JComboBox<>(new String[]{"Đạn phòng không", "Đạn chống tăng", "Đạn BB nhóm I", "Đạn BB nhóm II", "Mìn, Lựu đạn"});
        cbDanhMucDan.setEditable(true);
        txtLoaiDan = new JTextField();
        txtSoVienDan = new JTextField();
        InputValidator.restrictToNumbers(txtSoVienDan, false);
        txtTrongLuongDan = new JTextField();
        InputValidator.restrictToNumbers(txtTrongLuongDan, true);
        txtDVTinhDan = new JTextField();

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0; formPanel.add(new JLabel("Danh mục:"), gbc);
        gbc.gridx = 1; cbDanhMucDan.setPreferredSize(new Dimension(200, 35)); formPanel.add(cbDanhMucDan, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Loại đạn:"), gbc);
        gbc.gridx = 3; txtLoaiDan.setPreferredSize(new Dimension(250, 35)); formPanel.add(txtLoaiDan, gbc);
        gbc.gridx = 4; gbc.weightx = 1.0; formPanel.add(new JLabel(""), gbc); gbc.weightx = 0;

        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(new JLabel("Số viên/Cơ số:"), gbc);
        gbc.gridx = 1; txtSoVienDan.setPreferredSize(new Dimension(200, 35)); formPanel.add(txtSoVienDan, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Trọng lượng 1 viên (kg):"), gbc);
        gbc.gridx = 3; txtTrongLuongDan.setPreferredSize(new Dimension(250, 35)); formPanel.add(txtTrongLuongDan, gbc);

        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(new JLabel("Đơn vị tính:"), gbc);
        gbc.gridx = 1; txtDVTinhDan.setPreferredSize(new Dimension(200, 35)); formPanel.add(txtDVTinhDan, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        JButton btnAdd = UIUtils.createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnUpdate = UIUtils.createBtn("Cập nhật", new Color(41, 128, 185));
        JButton btnDelete = UIUtils.createBtn("Xóa", new Color(231, 76, 60));
        JButton btnClear = UIUtils.createBtn("Làm mới", Color.GRAY);
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);

        modelDan = new DefaultTableModel(new String[]{"ID", "Danh mục", "Loại đạn", "Số viên/Cơ số", "Trọng lượng/Viên", "ĐVT"}, 0);
        tableDan = new JTable(modelDan);
        tableDan.setRowHeight(25);
        tableDan.setFillsViewportHeight(true);
        tableDan.removeColumn(tableDan.getColumnModel().getColumn(0));
        panel.add(new JScrollPane(tableDan), BorderLayout.CENTER);

        tableDan.getSelectionModel().addListSelectionListener(e -> {
            int row = tableDan.getSelectedRow();
            if (row >= 0) {
                selectedIdDan = (int) modelDan.getValueAt(row, 0);
                cbDanhMucDan.setSelectedItem(modelDan.getValueAt(row, 1).toString());
                txtLoaiDan.setText(modelDan.getValueAt(row, 2).toString());
                txtSoVienDan.setText(modelDan.getValueAt(row, 3).toString());
                txtTrongLuongDan.setText(modelDan.getValueAt(row, 4).toString());
                txtDVTinhDan.setText(modelDan.getValueAt(row, 5).toString());
            }
        });

        btnAdd.addActionListener(e -> saveDan(true));
        btnUpdate.addActionListener(e -> saveDan(false));
        btnDelete.addActionListener(e -> deleteDan());
        btnClear.addActionListener(e -> {
            selectedIdDan = -1;
            txtLoaiDan.setText("");
            txtSoVienDan.setText("");
            txtTrongLuongDan.setText("");
            txtDVTinhDan.setText("");
            tableDan.clearSelection();
        });

        return panel;
    }

    private void loadDataDan() {
        modelDan.setRowCount(0);
        List<Object[]> rows = conventionService.loadDanRows();
        for (Object[] r : rows) {
            modelDan.addRow(r);
        }
    }

    private void saveDan(boolean isNew) {
        if (!isNew && selectedIdDan == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để cập nhật!");
            return;
        }
        String danhMuc = cbDanhMucDan.getSelectedItem() != null ? cbDanhMucDan.getSelectedItem().toString() : "";
        int soVien = conventionService.parseSoVienFromText(txtSoVienDan.getText());
        float trongLuong = conventionService.parseQuyUocFromText(txtTrongLuongDan.getText());
        boolean ok = isNew
                ? conventionService.insertDan(danhMuc, txtLoaiDan.getText(), soVien, trongLuong, txtDVTinhDan.getText())
                : conventionService.updateDan(selectedIdDan, danhMuc, txtLoaiDan.getText(), soVien, trongLuong, txtDVTinhDan.getText());
        if (ok) {
            loadDataDan();
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!");
        }
    }

    private void deleteDan() {
        if (selectedIdDan == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa loại đạn [" + txtLoaiDan.getText() + "] không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && conventionService.deleteDanById(selectedIdDan)) {
            loadDataDan();
            selectedIdDan = -1;
            txtLoaiDan.setText("");
            txtSoVienDan.setText("");
            txtTrongLuongDan.setText("");
        }
    }

    private JPanel createBienChePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Biên chế Quân số & Vũ khí"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Nhóm ĐV:", "Tên ĐV:", "Quân số:", "Lựu đạn:", "Súng ngắn:", "Tiểu liên:", "Trung liên:", "Đại liên:", "B41:", "Cối 60:", "Cối 82:", "Cối 100:", "SPG-9:", "12.7mm:"};
        fieldsBC = new JTextField[labels.length];

        int r = 0, c = 0;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = r; gbc.gridx = c; gbc.weightx = 0;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = c + 1;
            fieldsBC[i] = new JTextField();
            fieldsBC[i].setPreferredSize(new Dimension(140, 35));
            if (i >= 2) {
                InputValidator.restrictToNumbers(fieldsBC[i], false);
                fieldsBC[i].setText("0");
            }
            formPanel.add(fieldsBC[i], gbc);

            c += 2;
            if (c >= 8) {
                if (r == 0) {
                    gbc.gridx = 8; gbc.weightx = 1.0; formPanel.add(new JLabel(""), gbc); gbc.weightx = 0;
                }
                c = 0; r++;
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        JButton btnAdd = UIUtils.createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnUpdate = UIUtils.createBtn("Cập nhật", new Color(41, 128, 185));
        JButton btnDelete = UIUtils.createBtn("Xóa", new Color(231, 76, 60));
        JButton btnClear = UIUtils.createBtn("Làm mới", Color.GRAY);
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);

        modelBienChe = new DefaultTableModel(new String[]{"ID", "Nhóm", "Tên", "QS", "Lựu đạn", "SN", "TL", "TrL", "ĐL", "B41", "Cối 60", "Cối 82", "Cối 100", "SPG", "12.7"}, 0);
        tableBienChe = new JTable(modelBienChe);
        tableBienChe.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableBienChe.setFillsViewportHeight(true);
        tableBienChe.removeColumn(tableBienChe.getColumnModel().getColumn(0));
        panel.add(new JScrollPane(tableBienChe), BorderLayout.CENTER);

        tableBienChe.getSelectionModel().addListSelectionListener(e -> {
            int row = tableBienChe.getSelectedRow();
            if (row >= 0) {
                selectedIdBC = (int) modelBienChe.getValueAt(row, 0);
                for (int i = 0; i < fieldsBC.length; i++) {
                    fieldsBC[i].setText(modelBienChe.getValueAt(row, i + 1).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> saveBienChe(true));
        btnUpdate.addActionListener(e -> saveBienChe(false));
        btnDelete.addActionListener(e -> deleteBienChe());
        btnClear.addActionListener(e -> {
            selectedIdBC = -1;
            fieldsBC[0].setText("");
            fieldsBC[1].setText("");
            for (int i = 2; i < fieldsBC.length; i++) {
                fieldsBC[i].setText("0");
            }
            tableBienChe.clearSelection();
        });

        return panel;
    }

    private void loadDataBienChe() {
        modelBienChe.setRowCount(0);
        List<Object[]> rows = conventionService.loadBienCheRows();
        for (Object[] r : rows) {
            modelBienChe.addRow(r);
        }
    }

    private String[] collectBienCheTexts() {
        String[] t = new String[14];
        for (int i = 0; i < 14; i++) {
            t[i] = fieldsBC[i].getText();
        }
        return t;
    }

    private void saveBienChe(boolean isNew) {
        if (!isNew && selectedIdBC == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để cập nhật!");
            return;
        }
        String[] texts = collectBienCheTexts();
        boolean ok = isNew
                ? conventionService.insertBienChe(texts)
                : conventionService.updateBienChe(selectedIdBC, texts);
        if (ok) {
            loadDataBienChe();
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!");
        }
    }

    private void deleteBienChe() {
        if (selectedIdBC == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa biên chế [" + fieldsBC[1].getText() + "] không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && conventionService.deleteBienCheById(selectedIdBC)) {
            loadDataBienChe();
            selectedIdBC = -1;
            fieldsBC[0].setText("");
            fieldsBC[1].setText("");
            for (int i = 2; i < fieldsBC.length; i++) {
                fieldsBC[i].setText("0");
            }
        }
    }
}
