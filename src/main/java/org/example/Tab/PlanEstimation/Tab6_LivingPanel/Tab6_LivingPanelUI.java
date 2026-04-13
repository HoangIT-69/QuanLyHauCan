package org.example.Tab.PlanEstimation.Tab6_LivingPanel;

import org.example.Tab.PlanEstimation.Tab6_LivingPanel.Tab6_LivingPanelService;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab6_LivingPanelUI extends JPanel {
    private final int sessionId;
    private final Tab6_LivingPanelService service;
    private JTextArea txtAnUong;
    private JTextArea txtMac;
    private JTextArea txtONguNghi;

    public Tab6_LivingPanelUI(int sessionId, Tab6_LivingPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab6_LivingPanelService();

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

        mainContainer.add(UIUtils.createSectionLabel("1. Bảo đảm ăn uống"));
        txtAnUong = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtAnUong, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("2. Bảo đảm mặc"));
        txtMac = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtMac, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("3. Bảo đảm ở, ngủ nghỉ"));
        txtONguNghi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtONguNghi, 150));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        reloadFromDatabase();
    }

    public void reloadFromDatabase() {
        Tab6_LivingPanelService.Tab6Fields f = new Tab6_LivingPanelService.Tab6Fields();
        service.loadInto(f, sessionId);
        txtAnUong.setText(f.anUong);
        txtMac.setText(f.mac);
        txtONguNghi.setText(f.oNguNghi);
    }

    public void persistToDatabase() {
        service.save(sessionId, getAnUong(), getMac(), getONguNghi());
    }

    public String getAnUong() {
        return txtAnUong.getText().trim();
    }

    public String getMac() {
        return txtMac.getText().trim();
    }

    public String getONguNghi() {
        return txtONguNghi.getText().trim();
    }

    public void setAnUong(String text) {
        txtAnUong.setText(text != null ? text : "");
    }

    public void setMac(String text) {
        txtMac.setText(text != null ? text : "");
    }

    public void setONguNghi(String text) {
        txtONguNghi.setText(text != null ? text : "");
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<bd_an_uong>>", getAnUong());
        data.put("<<bd_mac>>", getMac());
        data.put("<<bd_o_ngunghi>>", getONguNghi());
        return data;
    }
}
