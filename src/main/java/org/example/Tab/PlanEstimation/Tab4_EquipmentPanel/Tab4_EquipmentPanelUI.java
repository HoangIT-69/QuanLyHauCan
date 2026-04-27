package org.example.Tab.PlanEstimation.Tab4_EquipmentPanel;

import org.example.Utils.AssurancePlanUiUtils;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Tab IV — Trang bị kỹ thuật (Dự kiến kế hoạch).
 * Trả lại code cũ sử dụng JTextArea và tự động sinh câu tóm tắt.
 */
public class Tab4_EquipmentPanelUI extends JPanel {

    private final Tab4_EquipmentPanelService service;
    private final int sessionId;

    private JTextArea txtChiTieu;
    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;

    public Tab4_EquipmentPanelUI(int sessionId, Tab4_EquipmentPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab4_EquipmentPanelService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("IV. TRANG BỊ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(20));

        // 1. Chỉ tiêu
        mainContainer.add(UIUtils.createSectionLabel("1. Chỉ tiêu"));
        mainContainer.add(Box.createVerticalStrut(10));
        txtChiTieu = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChiTieu, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        // 2. Tiếp nhận, chuẩn bị
        mainContainer.add(UIUtils.createSectionLabel("2. Tiếp nhận, chuẩn bị"));
        mainContainer.add(Box.createVerticalStrut(10));
        txtChuanBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChuanBi, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        // 3. Tiếp nhận trong chiến đấu
        mainContainer.add(UIUtils.createSectionLabel("3. Tiếp nhận trong chiến đấu"));
        mainContainer.add(Box.createVerticalStrut(10));
        txtChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChienDau, 100));

        add(AssurancePlanUiUtils.wrapVerticalScroll(mainContainer), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        Tab4_EquipmentPanelService.Tab4Fields f = new Tab4_EquipmentPanelService.Tab4Fields();
        service.loadInto(f, sessionId);

        txtChiTieu.setText(f.chiTieu);
        txtChuanBi.setText(f.chuanBi);
        txtChienDau.setText(f.chienDau);

        // Nếu chi tiêu trống, tự động sinh văn bản tóm tắt
        if (f.chiTieu == null || f.chiTieu.trim().isEmpty()) {
            generateAutoChiTieu();
        }
    }

    private void generateAutoChiTieu() {
        String summary = service.generateSummaryText(sessionId);
        if (!summary.isEmpty()) {
            txtChiTieu.setText(summary);
        }
    }

    public void persistToDatabase() {
        service.save(sessionId, txtChiTieu.getText(), txtChuanBi.getText(), txtChienDau.getText());
    }

    // Getters for Word Export
    public String getChiTieu() { return txtChiTieu.getText(); }
    public String getChuanBi() { return txtChuanBi.getText(); }
    public String getChienDau() { return txtChienDau.getText(); }
}
