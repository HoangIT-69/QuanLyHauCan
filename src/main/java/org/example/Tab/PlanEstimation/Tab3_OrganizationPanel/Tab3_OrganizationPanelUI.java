package org.example.Tab.PlanEstimation.Tab3_OrganizationPanel;

import org.example.Tab.PlanEstimation.Tab3_OrganizationPanel.Tab3_OrganizationPanelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * III. Tổ chức, bố trí — giao diện; dữ liệu qua {@link Tab3_OrganizationPanelService}.
 */
public class Tab3_OrganizationPanelUI extends JPanel {
    private final int sessionId;
    private final Tab3_OrganizationPanelService service;
    private JTextArea txtToChuc;
    private JTextArea txtBoTri;

    public Tab3_OrganizationPanelUI(int sessionId, Tab3_OrganizationPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab3_OrganizationPanelService();

        setLayout(new BorderLayout(0, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("III. TỔ CHỨC, BỐ TRÍ LỰC LƯỢNG HẬU CẦN, KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new GridLayout(2, 1, 0, 20));
        centerContainer.setBackground(Color.WHITE);

        JPanel pnlPart1 = new JPanel(new BorderLayout(0, 5));
        pnlPart1.setBackground(Color.WHITE);
        pnlPart1.add(createSectionLabel("1. Tổ chức lực lượng"), BorderLayout.NORTH);
        txtToChuc = createStandardTextArea();
        pnlPart1.add(createScrollPane(txtToChuc), BorderLayout.CENTER);

        JPanel pnlPart2 = new JPanel(new BorderLayout(0, 5));
        pnlPart2.setBackground(Color.WHITE);
        pnlPart2.add(createSectionLabel("2. Dự kiến bố trí hậu cần, kỹ thuật"), BorderLayout.NORTH);
        txtBoTri = createStandardTextArea();
        pnlPart2.add(createScrollPane(txtBoTri), BorderLayout.CENTER);

        centerContainer.add(pnlPart1);
        centerContainer.add(pnlPart2);

        add(centerContainer, BorderLayout.CENTER);

        reloadFromDatabase();
    }

    public void reloadFromDatabase() {
        setToChuc(service.loadToChuc(sessionId));
        setBoTri(service.loadBoTri(sessionId));
    }

    public void persistToDatabase() {
        service.saveBoth(sessionId, getToChuc(), getBoTri());
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

    public String getToChuc() {
        return txtToChuc.getText().trim();
    }

    public String getBoTri() {
        return txtBoTri.getText().trim();
    }

    public void setToChuc(String value) {
        txtToChuc.setText(value != null ? value : "");
    }

    public void setBoTri(String value) {
        txtBoTri.setText(value != null ? value : "");
    }

    /** Placeholder thay cho template Word (đồng bộ {@link org.example.Panel.PN_PlanEstimationPanel.PN_PlanEstimationPanelUI}). */
    public Map<String, String> getExportData() {
        Map<String, String> m = new HashMap<>();
        m.put("<<to_chuc_luc_luong>>", getToChuc());
        m.put("<<bo_tri_hckt>>", getBoTri());
        return m;
    }
}
