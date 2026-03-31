package org.example.Tab.PlanEstimation;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Tab11_CommandPanel extends JPanel {
    private int sessionId;

    private JTextArea txtTrienKhai, txtTTChuanBi, txtTTChienDau;
    private JTextField txtChiHuy, txtThayThe;
    private JTextField txtBaoCaoChuanBi, txtBaoCaoChienDau1, txtBaoCaoChienDau2;

    public Tab11_CommandPanel(int sessionId) {
        this.sessionId = sessionId;
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

        // --- 1. Chỉ huy ---
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

        // --- 2. Thông tin ---
        mainContainer.add(UIUtils.createSectionLabel("2. Quy định thông tin liên lạc"));
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị chiến đấu:"));
        txtTTChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTTChuanBi, 80));

        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtTTChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtTTChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(20));

        // --- 3. Báo cáo ---
        mainContainer.add(UIUtils.createSectionLabel("3. Quy định báo cáo và mốc thời gian"));

        txtBaoCaoChuanBi = UIUtils.createTimeField(80);
        mainContainer.add(createFlowRow("- Giai đoạn chuẩn bị, báo cáo ngày 1 lần vào", txtBaoCaoChuanBi, ""));

        txtBaoCaoChienDau1 = UIUtils.createTimeField(80);
        txtBaoCaoChienDau2 = UIUtils.createTimeField(80);
        mainContainer.add(createFlowRow("- Giai đoạn chiến đấu, ngày báo cáo 2 lần vào", txtBaoCaoChienDau1, " và "));
        ((JPanel)mainContainer.getComponent(mainContainer.getComponentCount()-1)).add(txtBaoCaoChienDau2);

        mainContainer.add(UIUtils.createNormalText("- Báo cáo đột xuất ngay khi có tình huống xảy ra./."));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        autoFetchCommandersFromDB();
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================
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

    // --- CÁC HÀM TRỢ GIÚP GIAO DIỆN ---
    private JPanel createInlineRow(String label, JTextField field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(Color.WHITE); p.setAlignmentX(0);
        JLabel lbl = UIUtils.createSubSectionLabel(label);
        lbl.setPreferredSize(new Dimension(130, 30));
        p.add(lbl); p.add(field);
        return p;
    }

    private JPanel createFlowRow(String text, JTextField field, String extraText) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        p.setBackground(Color.WHITE); p.setAlignmentX(0);
        p.add(UIUtils.createNormalText(text));
        p.add(field);
        if(!extraText.isEmpty()) p.add(UIUtils.createNormalText(extraText));
        return p;
    }

    private void autoFetchCommandersFromDB() {
        String sql = "SELECT chi_huy, nguoi_thay_the FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, sessionId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtChiHuy.setText(rs.getString("chi_huy"));
                txtThayThe.setText(rs.getString("nguoi_thay_the"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setTrienKhai(String text) { txtTrienKhai.setText(text); }
    public void setTTChuanBi(String text) { txtTTChuanBi.setText(text); }
    public void setTTChienDau(String text) { txtTTChienDau.setText(text); }
    public void setBaoCaoChuanBi(String text) { txtBaoCaoChuanBi.setText(text); }
    public void setBaoCaoChienDau1(String text) { txtBaoCaoChienDau1.setText(text); }
    public void setBaoCaoChienDau2(String text) { txtBaoCaoChienDau2.setText(text); }
}