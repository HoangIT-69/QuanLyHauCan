package org.example.Panel.Step1_DocumentInfoPanel;

import org.example.Panel.DataDeclarationContext;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Step1_DocumentInfoPanelUI extends JPanel {
    private final Step1_DocumentInfoPanelService step1Service = new Step1_DocumentInfoPanelService();
    private final DataDeclarationContext parent;

    private JTextField txtTenVK, txtViTri, txtThoiGian, txtTyLe, txtNam, txtChiHuy, txtThayThe;
    private JTextField[] txtMapFields;

    public Step1_DocumentInfoPanelUI(DataDeclarationContext parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 50, 20, 50));

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setBackground(Color.WHITE);
        formContent.putClientProperty("FlatLaf.style", "arc: 15");
        formContent.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        int row = 0;

        txtTenVK = UIUtils.createTextField("VD: Kế hoạch bảo đảm HC-KT năm 2026");
        UIUtils.addCustomRow(formContent, gbc, row++, "Tên văn kiện:", txtTenVK);

        txtViTri = UIUtils.createTextField("Nhập vị trí...");
        UIUtils.addCustomRow(formContent, gbc, row++, "Vị trí chỉ huy:", txtViTri);

        txtThoiGian = UIUtils.createTextField("VD: 15.00 N-3");
        UIUtils.addCustomRow(formContent, gbc, row++, "Thời gian:", txtThoiGian);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel lblMap = new JLabel("Mảnh bản đồ:");
        lblMap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMap.setForeground(new Color(71, 85, 105));
        formContent.add(lblMap, gbc);

        JPanel gridMapPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        gridMapPanel.setBackground(Color.WHITE);
        txtMapFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            txtMapFields[i] = new JTextField();
            txtMapFields[i].setPreferredSize(new Dimension(0, 35));
            gridMapPanel.add(txtMapFields[i]);
        }
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        formContent.add(gridMapPanel, gbc);

        txtTyLe = UIUtils.createTextField("VD: 1:50.000");
        UIUtils.addCustomRow(formContent, gbc, row++, "Tỷ lệ:", txtTyLe);

        txtNam = UIUtils.createTextField("VD: 2026");
        InputValidator.restrictToNumbers(txtNam, false);
        UIUtils.addCustomRow(formContent, gbc, row++, "Năm:", txtNam);

        txtChiHuy = UIUtils.createTextField("Họ và tên");
        UIUtils.addCustomRow(formContent, gbc, row++, "Chỉ huy HC-KT:", txtChiHuy);

        txtThayThe = UIUtils.createTextField("Họ và tên");
        UIUtils.addCustomRow(formContent, gbc, row++, "Người thay thế:", txtThayThe);

        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnNext = UIUtils.createNavButtonWithIcon("Tiếp tục", new Color(41, 128, 185), "/images/next.png", true);

        btnNext.addActionListener(e -> {
            if (validateInputs() && persistStep1()) {
                parent.navigateStep(2);
            }
        });

        bottomPanel.add(btnNext);
        add(bottomPanel, BorderLayout.SOUTH);

        loadFromDatabase();
    }

    private boolean validateInputs() {
        if (txtTenVK.getText().trim().isEmpty() || txtViTri.getText().trim().isEmpty()
                || txtChiHuy.getText().trim().isEmpty() || txtThayThe.getText().trim().isEmpty()) {
            showError("Vui lòng điền đầy đủ các thông tin!");
            return false;
        }
        if (!InputValidator.isValidMilitaryTime(txtThoiGian.getText())) {
            showError("Thời gian sai định dạng! Mẫu: 15.00 N-3");
            return false;
        }
        if (!InputValidator.isValidMapScale(txtTyLe.getText())) {
            showError("Tỷ lệ bản đồ sai định dạng! Mẫu: 1:50.000");
            return false;
        }
        if (!InputValidator.isValidYear(txtNam.getText())) {
            showError("Năm sai định dạng! Mẫu: 2026");
            return false;
        }
        for (JTextField mapField : txtMapFields) {
            String text = mapField.getText().trim();
            if (!text.isEmpty()) {
                mapField.setText(text.toUpperCase());
            }
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void loadFromDatabase() {
        int sessionId = parent.getCurrentSessionId();
        Step1_DocumentInfoPanelService.LoadResult result = step1Service.loadBySessionId(sessionId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu Step 1: " + result.getErrorMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Step1_DocumentInfoPanelService.Step1Record rec = result.getData();
        if (rec == null) {
            return;
        }
        txtTenVK.setText(nullToEmpty(rec.tenVanKien));
        txtViTri.setText(nullToEmpty(rec.viTriChiHuy));
        txtThoiGian.setText(nullToEmpty(rec.thoiGian));
        txtMapFields[0].setText(nullToEmpty(rec.map1));
        txtMapFields[1].setText(nullToEmpty(rec.map2));
        txtMapFields[2].setText(nullToEmpty(rec.map3));
        txtMapFields[3].setText(nullToEmpty(rec.map4));
        txtTyLe.setText(nullToEmpty(rec.tyLe));
        txtNam.setText(String.valueOf(rec.nam));
        txtChiHuy.setText(nullToEmpty(rec.chiHuy));
        txtThayThe.setText(nullToEmpty(rec.nguoiThayThe));
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private boolean persistStep1() {
        String[] maps = new String[4];
        for (int i = 0; i < 4; i++) {
            maps[i] = txtMapFields[i].getText().trim();
        }
        int nam = Integer.parseInt(txtNam.getText().trim());
        Step1_DocumentInfoPanelService.SaveResult result = step1Service.save(
                parent.getCurrentUserId(),
                parent.getCurrentSessionId(),
                txtTenVK.getText().trim(),
                txtViTri.getText().trim(),
                txtThoiGian.getText().trim(),
                maps,
                txtTyLe.getText().trim(),
                nam,
                txtChiHuy.getText().trim(),
                txtThayThe.getText().trim(),
                parent.getHinhThucTapBai()
        );
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu Database: " + result.getErrorMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (result.getNewSessionId() != null) {
            parent.setCurrentSessionId(result.getNewSessionId());
        }
        return true;
    }
}
