package org.example.Tab.PlanEstimation.Tab4_EquipmentPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Tab4_EquipmentPanelUI extends JPanel {
    private final int sessionId;
    private final Tab4_EquipmentPanelService service;
    private JTextArea txtChiTieu;
    private JTextArea txtGiaiDoanChuanBi;
    private JTextArea txtGiaiDoanChienDau;

    public Tab4_EquipmentPanelUI(int sessionId, Tab4_EquipmentPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab4_EquipmentPanelService();

        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("IV. BẢO ĐẢM VŨ KHÍ TRANG BỊ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new GridLayout(3, 1, 0, 15));
        centerContainer.setBackground(Color.WHITE);

        JPanel pnlPart1 = new JPanel(new BorderLayout(0, 5));
        pnlPart1.setBackground(Color.WHITE);
        pnlPart1.add(createSectionLabel("1. Chỉ tiêu"), BorderLayout.NORTH);
        txtChiTieu = createStandardTextArea();
        pnlPart1.add(createScrollPane(txtChiTieu), BorderLayout.CENTER);

        JPanel pnlPart2 = new JPanel(new BorderLayout(0, 5));
        pnlPart2.setBackground(Color.WHITE);
        JPanel pnlTitle2 = new JPanel(new GridLayout(2, 1));
        pnlTitle2.setBackground(Color.WHITE);
        pnlTitle2.add(createSectionLabel("2. Ý định tiếp nhận, bổ sung"));
        pnlTitle2.add(createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        pnlPart2.add(pnlTitle2, BorderLayout.NORTH);
        txtGiaiDoanChuanBi = createStandardTextArea();
        pnlPart2.add(createScrollPane(txtGiaiDoanChuanBi), BorderLayout.CENTER);

        JPanel pnlPart3 = new JPanel(new BorderLayout(0, 5));
        pnlPart3.setBackground(Color.WHITE);
        pnlPart3.add(createSubSectionLabel("* Giai đoạn chiến đấu:"), BorderLayout.NORTH);
        txtGiaiDoanChienDau = createStandardTextArea();
        pnlPart3.add(createScrollPane(txtGiaiDoanChienDau), BorderLayout.CENTER);

        centerContainer.add(pnlPart1);
        centerContainer.add(pnlPart2);
        centerContainer.add(pnlPart3);

        add(centerContainer, BorderLayout.CENTER);

        reloadFromDatabase();
    }

    public void reloadFromDatabase() {
        Tab4_EquipmentPanelService.Tab4Fields f = new Tab4_EquipmentPanelService.Tab4Fields();
        service.loadInto(f, sessionId);
        txtChiTieu.setText(f.chiTieu);
        txtGiaiDoanChuanBi.setText(f.chuanBi);
        txtGiaiDoanChienDau.setText(f.chienDau);
    }

    public void persistToDatabase() {
        service.save(sessionId, getChiTieu(), getChuanBi(), getChienDau());
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
        return lbl;
    }

    private JLabel createSubSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 14));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    private JTextArea createStandardTextArea() {
        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBorder(new EmptyBorder(10, 10, 10, 10));
        return txt;
    }

    private JScrollPane createScrollPane(JTextArea textArea) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public String getChiTieu() {
        return txtChiTieu.getText().trim();
    }

    public String getChuanBi() {
        return txtGiaiDoanChuanBi.getText().trim();
    }

    public String getChienDau() {
        return txtGiaiDoanChienDau.getText().trim();
    }
}
