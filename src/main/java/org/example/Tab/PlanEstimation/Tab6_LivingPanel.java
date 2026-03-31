package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class Tab6_LivingPanel extends JPanel {
    private JTextArea txtAnUong, txtMac, txtONguNghi;

    public Tab6_LivingPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("VI. BẢO ĐẢM SINH HOẠT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Ăn uống
        mainContainer.add(UIUtils.createSectionLabel("1. Bảo đảm ăn uống"));
        txtAnUong = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtAnUong, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Mặc
        mainContainer.add(UIUtils.createSectionLabel("2. Bảo đảm mặc"));
        txtMac = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtMac, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        // 3. Ở, ngủ nghỉ
        mainContainer.add(UIUtils.createSectionLabel("3. Bảo đảm ở, ngủ nghỉ"));
        txtONguNghi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtONguNghi, 150));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // --- CÁC HÀM GET DỮ LIỆU ĐỂ XUẤT WORD ---
    public String getAnUong() { return txtAnUong.getText().trim(); }
    public String getMac() { return txtMac.getText().trim(); }
    public String getONguNghi() { return txtONguNghi.getText().trim(); }

    public void setAnUong(String text) { txtAnUong.setText(text); }
    public void setMac(String text) { txtMac.setText(text); }
    public void setONguNghi(String text) { txtONguNghi.setText(text); }

    public Map<String, String> getExportData() {
        Map<String, String> data = new java.util.HashMap<>();
        data.put("<<bd_an_uong>>", getAnUong());
        data.put("<<bd_mac>>", getMac());
        data.put("<<bd_o_ngunghi>>", getONguNghi());
        return data;
    }
}