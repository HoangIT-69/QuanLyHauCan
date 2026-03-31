package org.example.Tab.PlanEstimation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Tab2_MissionPanel extends JPanel {
    private JTextArea txtNhiemVu;

    public Tab2_MissionPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("II. NHIỆM VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        txtNhiemVu = new JTextArea();
        txtNhiemVu.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNhiemVu.setLineWrap(true);
        txtNhiemVu.setWrapStyleWord(true);
        txtNhiemVu.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtNhiemVu);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public String getNhiemVu() { return txtNhiemVu.getText().trim(); }

    public void setNhiemVu(String value) {
        this.txtNhiemVu.setText(value != null ? value : "");
    }
}