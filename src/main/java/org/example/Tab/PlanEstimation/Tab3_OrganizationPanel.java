package org.example.Tab.PlanEstimation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Tab3_OrganizationPanel extends JPanel {
    private JTextArea txtToChuc;
    private JTextArea txtBoTri;

    public Tab3_OrganizationPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TIÊU ĐỀ LỚN ---
        JLabel lblTitle = new JLabel("III. TỔ CHỨC, BỐ TRÍ LỰC LƯỢNG HẬU CẦN, KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        // --- KHUNG CHỨA TRUNG TÂM (Chia 2 dòng đều nhau) ---
        JPanel centerContainer = new JPanel(new GridLayout(2, 1, 0, 20));
        centerContainer.setBackground(Color.WHITE);

        // --- MỤC 1 ---
        JPanel pnlPart1 = new JPanel(new BorderLayout(0, 5));
        pnlPart1.setBackground(Color.WHITE);
        pnlPart1.add(createSectionLabel("1. Tổ chức lực lượng"), BorderLayout.NORTH);
        txtToChuc = createStandardTextArea();
        pnlPart1.add(createScrollPane(txtToChuc), BorderLayout.CENTER);

        // --- MỤC 2 ---
        JPanel pnlPart2 = new JPanel(new BorderLayout(0, 5));
        pnlPart2.setBackground(Color.WHITE);
        pnlPart2.add(createSectionLabel("2. Dự kiến bố trí hậu cần, kỹ thuật"), BorderLayout.NORTH);
        txtBoTri = createStandardTextArea();
        pnlPart2.add(createScrollPane(txtBoTri), BorderLayout.CENTER);

        centerContainer.add(pnlPart1);
        centerContainer.add(pnlPart2);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
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

    public String getToChuc() { return txtToChuc.getText().trim(); }
    public String getBoTri() { return txtBoTri.getText().trim(); }
}