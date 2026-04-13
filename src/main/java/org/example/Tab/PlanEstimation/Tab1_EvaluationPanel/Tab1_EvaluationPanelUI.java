package org.example.Tab.PlanEstimation.Tab1_EvaluationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Tab1_EvaluationPanelUI extends JPanel {
    private final int sessionId;
    private final Tab1_EvaluationPanelService service;
    private JTextArea txtDanhGia;

    public Tab1_EvaluationPanelUI(int sessionId, Tab1_EvaluationPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab1_EvaluationPanelService();

        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("I. ĐÁNH GIÁ TÌNH HÌNH TÁC ĐỘNG ĐẾN HẬU CẦN, KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        txtDanhGia = new JTextArea();
        txtDanhGia.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtDanhGia.setLineWrap(true);
        txtDanhGia.setWrapStyleWord(true);
        txtDanhGia.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtDanhGia);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        reloadFromDatabase();
    }

    public void reloadFromDatabase() {
        setDanhGia(this.service.loadDanhGia(sessionId));
    }

    public void persistToDatabase() {
        this.service.saveDanhGia(sessionId, getDanhGia());
    }

    public String getDanhGia() {
        return txtDanhGia.getText().trim();
    }

    public void setDanhGia(String value) {
        this.txtDanhGia.setText(value != null ? value : "");
    }
}
