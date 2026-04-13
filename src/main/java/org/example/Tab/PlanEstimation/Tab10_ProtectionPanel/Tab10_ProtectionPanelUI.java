package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab10_ProtectionPanelUI extends JPanel {

    private final int sessionId;
    private final Tab10_ProtectionPanelService service;

    private JTextArea txtTinhHuong;
    private JTextArea txtBienPhap;

    /** Assurance / màn không session: không đọc/ghi {@code pn_plan_estimation}. */
    public Tab10_ProtectionPanelUI() {
        this(-1, new Tab10_ProtectionPanelService());
    }

    public Tab10_ProtectionPanelUI(int sessionId, Tab10_ProtectionPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab10_ProtectionPanelService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("X. BẢO VỆ HẬU CẦN - KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tình huống có thể xảy ra"));
        txtTinhHuong = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTinhHuong, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("2. Biện pháp"));
        txtBienPhap = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBienPhap, 150));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        if (sessionId >= 1) {
            reloadFromDatabase();
        }
    }

    public void reloadFromDatabase() {
        if (sessionId < 1) {
            return;
        }
        Tab10_ProtectionPanelService.Tab10Fields f = new Tab10_ProtectionPanelService.Tab10Fields();
        service.loadInto(f, sessionId);
        txtTinhHuong.setText(f.tinhHuong);
        txtBienPhap.setText(f.bienPhap);
    }

    public void persistToDatabase() {
        if (sessionId < 1) {
            return;
        }
        service.save(sessionId, txtTinhHuong.getText(), txtBienPhap.getText());
    }

    public Map<String, String> getProtectionData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<tinh_huong_bv>>", txtTinhHuong.getText().trim());
        data.put("<<bien_phap_bv>>", txtBienPhap.getText().trim());
        return data;
    }

    public void setTinhHuong(String text) {
        txtTinhHuong.setText(text != null ? text : "");
    }

    public void setBienPhap(String text) {
        txtBienPhap.setText(text != null ? text : "");
    }
}
