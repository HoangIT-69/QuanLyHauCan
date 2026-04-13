package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab11_CommandPanelUI extends JPanel {

    private final int sessionId;
    private final Tab11_CommandPanelService service;

    private JTextArea txtTrienKhai;
    private JTextArea txtTTChuanBi;
    private JTextArea txtTTChienDau;
    private JTextField txtChiHuy;
    private JTextField txtThayThe;
    private JTextField txtBaoCaoChuanBi;
    private JTextField txtBaoCaoChienDau1;
    private JTextField txtBaoCaoChienDau2;

    public Tab11_CommandPanelUI(int sessionId, Tab11_CommandPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab11_CommandPanelService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("XI. CHỈ HUY HẬU CẦN – KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("1. Chỉ huy hậu cần, kỹ thuật"));
        txtTrienKhai = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTrienKhai, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChiHuy = UIUtils.createAutoCalcField(300);
        mainContainer.add(createInlineRow("- Người chỉ huy:", txtChiHuy));
        mainContainer.add(Box.createVerticalStrut(5));

        txtThayThe = UIUtils.createAutoCalcField(300);
        mainContainer.add(createInlineRow("- Người thay thế:", txtThayThe));
        mainContainer.add(Box.createVerticalStrut(20));

        mainContainer.add(UIUtils.createSectionLabel("2. Quy định thông tin liên lạc"));
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị chiến đấu:"));
        txtTTChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTTChuanBi, 80));

        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtTTChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTTChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(20));

        mainContainer.add(UIUtils.createSectionLabel("3. Quy định báo cáo và mốc thời gian"));

        txtBaoCaoChuanBi = UIUtils.createTimeField(80);
        mainContainer.add(createFlowRow("- Giai đoạn chuẩn bị, báo cáo ngày 1 lần vào", txtBaoCaoChuanBi, ""));

        txtBaoCaoChienDau1 = UIUtils.createTimeField(80);
        txtBaoCaoChienDau2 = UIUtils.createTimeField(80);
        mainContainer.add(createBaoCaoChienDauRow());

        mainContainer.add(UIUtils.createNormalText("- Báo cáo đột xuất ngay khi có tình huống xảy ra./."));

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
        Tab11_CommandPanelService.Tab11Fields f = new Tab11_CommandPanelService.Tab11Fields();
        service.loadInto(f, sessionId);
        txtTrienKhai.setText(f.trienKhai);
        txtChiHuy.setText(f.chiHuy);
        txtThayThe.setText(f.nguoiThayThe);
        txtTTChuanBi.setText(f.ttChuanBi);
        txtTTChienDau.setText(f.ttChienDau);
        txtBaoCaoChuanBi.setText(f.baoCaoChuanBi);
        txtBaoCaoChienDau1.setText(f.baoCaoChienDau1);
        txtBaoCaoChienDau2.setText(f.baoCaoChienDau2);
    }

    public void persistToDatabase() {
        if (sessionId < 1) {
            return;
        }
        Tab11_CommandPanelService.Tab11Fields f = new Tab11_CommandPanelService.Tab11Fields();
        f.trienKhai = txtTrienKhai.getText();
        f.chiHuy = txtChiHuy.getText();
        f.nguoiThayThe = txtThayThe.getText();
        f.ttChuanBi = txtTTChuanBi.getText();
        f.ttChienDau = txtTTChienDau.getText();
        f.baoCaoChuanBi = txtBaoCaoChuanBi.getText();
        f.baoCaoChienDau1 = txtBaoCaoChienDau1.getText();
        f.baoCaoChienDau2 = txtBaoCaoChienDau2.getText();
        service.save(sessionId, f);
    }

    public Map<String, String> getCommandData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<trien_khai_ch>>", txtTrienKhai.getText().trim());
        data.put("<<chi_huy>>", txtChiHuy.getText().trim());
        data.put("<<nguoi_thay_the>>", txtThayThe.getText().trim());
        data.put("<<ttll_cb>>", txtTTChuanBi.getText().trim());
        data.put("<<ttll_cd>>", txtTTChienDau.getText().trim());
        data.put("<<bc_cb>>", txtBaoCaoChuanBi.getText().trim());
        data.put("<<bc_cd1>>", txtBaoCaoChienDau1.getText().trim());
        data.put("<<bc_cd2>>", txtBaoCaoChienDau2.getText().trim());
        return data;
    }

    private JPanel createInlineRow(String label, JTextField field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(0);
        JLabel lbl = UIUtils.createSubSectionLabel(label);
        lbl.setPreferredSize(new Dimension(130, 30));
        p.add(lbl);
        p.add(field);
        return p;
    }

    private JPanel createFlowRow(String text, JTextField field, String extraText) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(0);
        p.add(UIUtils.createNormalText(text));
        p.add(field);
        if (extraText != null && !extraText.isEmpty()) {
            p.add(UIUtils.createNormalText(extraText));
        }
        return p;
    }

    private JPanel createBaoCaoChienDauRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(0);
        p.add(UIUtils.createNormalText("- Giai đoạn chiến đấu, ngày báo cáo 2 lần vào"));
        p.add(txtBaoCaoChienDau1);
        p.add(UIUtils.createNormalText(" và "));
        p.add(txtBaoCaoChienDau2);
        return p;
    }

    public void setTrienKhai(String text) {
        txtTrienKhai.setText(text != null ? text : "");
    }

    public void setTTChuanBi(String text) {
        txtTTChuanBi.setText(text != null ? text : "");
    }

    public void setTTChienDau(String text) {
        txtTTChienDau.setText(text != null ? text : "");
    }

    public void setBaoCaoChuanBi(String text) {
        txtBaoCaoChuanBi.setText(text != null ? text : "");
    }

    public void setBaoCaoChienDau1(String text) {
        txtBaoCaoChienDau1.setText(text != null ? text : "");
    }

    public void setBaoCaoChienDau2(String text) {
        txtBaoCaoChienDau2.setText(text != null ? text : "");
    }
}
