package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab12_ConclusionPanel extends JPanel {
    private JTextArea txtKetLuan, txtDeNghi;

    public Tab12_ConclusionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề
        JLabel lblTitle = new JLabel("XII. KẾT LUẬN VÀ ĐỀ NGHỊ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Kết luận
        mainContainer.add(UIUtils.createSectionLabel("- Kết luận"));
        txtKetLuan = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtKetLuan, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Đề nghị
        mainContainer.add(UIUtils.createSectionLabel("- Đề nghị"));
        txtDeNghi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtDeNghi, 150));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================
    public Map<String, String> getConclusionData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<ket_luan>>", txtKetLuan.getText().trim());
        data.put("<<de_nghi>>", txtDeNghi.getText().trim());
        return data;
    }
}