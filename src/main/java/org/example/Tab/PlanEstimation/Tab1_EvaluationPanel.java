package org.example.Tab.PlanEstimation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab1_EvaluationPanel extends JPanel {
    private JTextArea txtDanhGia;

    public Tab1_EvaluationPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("I. ĐÁNH GIÁ TÌNH HÌNH TÁC ĐỘNG ĐẾN HẬU CẦN, KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        // --- KHUNG NHẬP LIỆU ---
        txtDanhGia = new JTextArea();
        txtDanhGia.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtDanhGia.setLineWrap(true);
        txtDanhGia.setWrapStyleWord(true);
        txtDanhGia.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtDanhGia);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public String getDanhGia() { return txtDanhGia.getText().trim(); }

    public void setDanhGia(String value) {
        this.txtDanhGia.setText(value != null ? value : "");
    }

}