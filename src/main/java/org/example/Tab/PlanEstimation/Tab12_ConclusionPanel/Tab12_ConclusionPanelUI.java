package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab12_ConclusionPanelUI extends JPanel {

    private final int sessionId;
    private final Tab12_ConclusionPanelService service;

    private JTextArea txtKetLuan;
    private JTextArea txtDeNghi;

    public Tab12_ConclusionPanelUI(int sessionId, Tab12_ConclusionPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab12_ConclusionPanelService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("XII. KẾT LUẬN VÀ ĐỀ NGHỊ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("- Kết luận"));
        txtKetLuan = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtKetLuan, 150));
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("- Đề nghị"));
        txtDeNghi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtDeNghi, 150));

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
        Tab12_ConclusionPanelService.Tab12Fields f = new Tab12_ConclusionPanelService.Tab12Fields();
        service.loadInto(f, sessionId);
        txtKetLuan.setText(f.ketLuan);
        txtDeNghi.setText(f.deNghi);
    }

    public void persistToDatabase() {
        if (sessionId < 1) {
            return;
        }
        service.save(sessionId, txtKetLuan.getText(), txtDeNghi.getText());
    }

    public Map<String, String> getConclusionData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<ket_luan>>", txtKetLuan.getText().trim());
        data.put("<<de_nghi>>", txtDeNghi.getText().trim());
        return data;
    }
}
