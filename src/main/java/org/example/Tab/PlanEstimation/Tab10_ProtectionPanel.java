package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab10_ProtectionPanel extends JPanel {
    private JTextArea txtTinhHuong, txtBienPhap;

    public Tab10_ProtectionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề
        JLabel lblTitle = new JLabel("X. BẢO VỆ HẬU CẦN - KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Tình huống
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tình huống có thể xảy ra"));
        txtTinhHuong = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTinhHuong, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Biện pháp
        mainContainer.add(UIUtils.createSectionLabel("2. Biện pháp"));
        txtBienPhap = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBienPhap, 150));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================
    public Map<String, String> getProtectionData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<tinh_huong_bv>>", txtTinhHuong.getText().trim());
        data.put("<<bien_phap_bv>>", txtBienPhap.getText().trim());
        return data;
    }
    public void setTinhHuong(String text) { txtTinhHuong.setText(text); }
    public void setBienPhap(String text) { txtBienPhap.setText(text); }
}