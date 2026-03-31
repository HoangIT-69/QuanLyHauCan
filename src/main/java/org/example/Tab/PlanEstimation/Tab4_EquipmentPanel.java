package org.example.Tab.PlanEstimation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Tab4_EquipmentPanel extends JPanel {
    private JTextArea txtChiTieu;
    private JTextArea txtGiaiDoanChuanBi;
    private JTextArea txtGiaiDoanChienDau;

    public Tab4_EquipmentPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TIÊU ĐỀ LỚN ---
        JLabel lblTitle = new JLabel("IV. BẢO ĐẢM TRANG BỊ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        // --- KHUNG CHỨA TRUNG TÂM (Chia 3 dòng đều nhau) ---
        JPanel centerContainer = new JPanel(new GridLayout(3, 1, 0, 15));
        centerContainer.setBackground(Color.WHITE);

        // --- MỤC 1: CHỈ TIÊU ---
        JPanel pnlPart1 = new JPanel(new BorderLayout(0, 5));
        pnlPart1.setBackground(Color.WHITE);
        pnlPart1.add(createSectionLabel("1. Chỉ tiêu"), BorderLayout.NORTH);
        txtChiTieu = createStandardTextArea();
        pnlPart1.add(createScrollPane(txtChiTieu), BorderLayout.CENTER);

        // --- MỤC 2: GIAI ĐOẠN CHUẨN BỊ ---
        JPanel pnlPart2 = new JPanel(new BorderLayout(0, 5));
        pnlPart2.setBackground(Color.WHITE);

        JPanel pnlTitle2 = new JPanel(new GridLayout(2, 1));
        pnlTitle2.setBackground(Color.WHITE);
        pnlTitle2.add(createSectionLabel("2. Ý định tiếp nhận, bổ sung"));
        pnlTitle2.add(createSubSectionLabel("* Giai đoạn chuẩn bị:"));

        pnlPart2.add(pnlTitle2, BorderLayout.NORTH);
        txtGiaiDoanChuanBi = createStandardTextArea();
        pnlPart2.add(createScrollPane(txtGiaiDoanChuanBi), BorderLayout.CENTER);

        // --- MỤC 3: GIAI ĐOẠN CHIẾN ĐẤU ---
        JPanel pnlPart3 = new JPanel(new BorderLayout(0, 5));
        pnlPart3.setBackground(Color.WHITE);
        pnlPart3.add(createSubSectionLabel("* Giai đoạn chiến đấu:"), BorderLayout.NORTH);
        txtGiaiDoanChienDau = createStandardTextArea();
        pnlPart3.add(createScrollPane(txtGiaiDoanChienDau), BorderLayout.CENTER);

        // Thêm vào vùng trung tâm
        centerContainer.add(pnlPart1);
        centerContainer.add(pnlPart2);
        centerContainer.add(pnlPart3);

        add(centerContainer, BorderLayout.CENTER);
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


    public String getChiTieu() { return txtChiTieu.getText().trim(); }
    public String getChuanBi() { return txtGiaiDoanChuanBi.getText().trim(); }
    public String getChienDau() { return txtGiaiDoanChienDau.getText().trim(); }
}