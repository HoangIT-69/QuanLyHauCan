package org.example.Tab.PlanEstimation;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Tab7_MedicalPanel extends JPanel {

    private int sessionId;
    private int tbCaoNhatValue = 0;

    // Thành phần UI
    private JTextField txtTBToanTran, txtBBToanTran, txtTBCaoNhat;
    private JTextField txtCC_d_Min, txtCC_d_Max, txtCC_e_Min, txtCC_e_Max, txtCC_xa_Min, txtCC_xa_Max;
    private JLabel lblCC_SumMin, lblCC_SumMax;
    private JTextField txtVC_d_Min, txtVC_d_Max, txtVC_e_Min, txtVC_e_Max, txtVC_xa_Min, txtVC_xa_Max;
    private JLabel lblVC_SumMin, lblVC_SumMax;
    private JLabel lblKetLuan;
    private JTextArea txtChuanBi, txtChienDau, txtPhongBenh;

    public Tab7_MedicalPanel(int sessionId) {
        this.sessionId = sessionId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề
        JLabel lblTitle = new JLabel("VII. BẢO ĐẢM QUÂN Y");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        initFields();

        // --- 1. Dự kiến tỷ lệ ---
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tỷ lệ TBBB (Hệ thống tự động tính)"));
        // Dòng 1: Đầy đủ 2 ô nhập
        mainContainer.add(createRowPanel("- Thương binh toàn trận:", txtTBToanTran, "người; bệnh binh toàn trận:", txtBBToanTran, "người."));
        mainContainer.add(Box.createVerticalStrut(5));
        // Dòng 2: Chỉ 1 ô nhập (Đã sửa lỗi hiện ô thừa ở cuối)
        mainContainer.add(createRowPanel("- Thương binh ngày cao nhất:", txtTBCaoNhat, "người.", null, null));
        mainContainer.add(Box.createVerticalStrut(20));

        // --- 2. Cân đối ---
        mainContainer.add(UIUtils.createSectionLabel("2. Cân đối"));
        mainContainer.add(UIUtils.createSubSectionLabel("- Khả năng cấp cứu thương binh:"));
        mainContainer.add(createRangeRow("+ Quân y/d khả năng BSCC:", txtCC_d_Min, txtCC_d_Max, "TB/ngày"));
        mainContainer.add(createRangeRow("+ Quân y/e phối thuộc khả năng BSCC:", txtCC_e_Min, txtCC_e_Max, "TB/ngày"));
        mainContainer.add(createRangeRow("+ Trạm (điểm trạm) y tế xã:", txtCC_xa_Min, txtCC_xa_Max, "TB/ngày"));

        mainContainer.add(createSummaryRow("  Cộng:", lblCC_SumMin, lblCC_SumMax, " TB/ngày"));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("- Khả năng vận chuyển thương binh:"));
        mainContainer.add(createRangeRow("+ bVTB(-)/d vận chuyển:", txtVC_d_Min, txtVC_d_Max, "TB/chuyến"));
        mainContainer.add(createRangeRow("+ bVTB/e phối thuộc vận chuyển:", txtVC_e_Min, txtVC_e_Max, "TB/chuyến"));
        mainContainer.add(createRangeRow("+ b DQTV/xã vận chuyển:", txtVC_xa_Min, txtVC_xa_Max, "TB/chuyến"));

        mainContainer.add(createSummaryRow("  Cộng:", lblVC_SumMin, lblVC_SumMax, " TB/chuyến"));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(lblKetLuan);
        mainContainer.add(Box.createVerticalStrut(25));

        // --- 3 & 4. Văn bản ---
        mainContainer.add(UIUtils.createSectionLabel("3. Ý định bảo đảm"));
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        txtChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtChuanBi, 100));

        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        txtChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtChienDau, 100));

        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(UIUtils.createSectionLabel("4. Bảo đảm vệ sinh phòng bệnh, phòng dịch"));
        txtPhongBenh = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtPhongBenh, 100));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        autoCalculateTBBBFromDB();
        triggerCalculations();
    }

    public Map<String, String> getMedicalData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<tb_toantran>>", txtTBToanTran.getText());
        data.put("<<bb_toantran>>", txtBBToanTran.getText());
        data.put("<<tb_caonhat>>", txtTBCaoNhat.getText());
        data.put("<<can_doi_quany>>", lblKetLuan.getText().replaceAll("\\<.*?\\>", ""));
        data.put("<<ydinh_quany_cb>>", txtChuanBi.getText());
        data.put("<<ydinh_quany_cd>>", txtChienDau.getText());
        data.put("<<phong_benh>>", txtPhongBenh.getText());
        return data;
    }

    private void initFields() {
        txtTBToanTran = UIUtils.createAutoCalcField(80);
        txtBBToanTran = UIUtils.createAutoCalcField(80);
        txtTBCaoNhat = UIUtils.createAutoCalcField(80);

        txtCC_d_Min = createNumField(); txtCC_d_Max = createNumField();
        txtCC_e_Min = createNumField(); txtCC_e_Max = createNumField();
        txtCC_xa_Min = createNumField(); txtCC_xa_Max = createNumField();
        lblCC_SumMin = UIUtils.createSumLabel(); lblCC_SumMax = UIUtils.createSumLabel();

        txtVC_d_Min = createNumField(); txtVC_d_Max = createNumField();
        txtVC_e_Min = createNumField(); txtVC_e_Max = createNumField();
        txtVC_xa_Min = createNumField(); txtVC_xa_Max = createNumField();
        lblVC_SumMin = UIUtils.createSumLabel(); lblVC_SumMax = UIUtils.createSumLabel();

        lblKetLuan = new JLabel("<html>=> Cân đối giữa nhu cầu và khả năng...</html>");
        lblKetLuan.setFont(new Font("Segoe UI", Font.ITALIC | Font.BOLD, 15));
        lblKetLuan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private JTextField createNumField() {
        JTextField txt = UIUtils.createNumberField(60);
        txt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { triggerCalculations(); }
            public void removeUpdate(DocumentEvent e) { triggerCalculations(); }
            public void changedUpdate(DocumentEvent e) { triggerCalculations(); }
        });
        return txt;
    }

    private void triggerCalculations() {
        SwingUtilities.invokeLater(() -> {
            int ccMin = UIUtils.safeInt(txtCC_d_Min) + UIUtils.safeInt(txtCC_e_Min) + UIUtils.safeInt(txtCC_xa_Min);
            int ccMax = UIUtils.safeInt(txtCC_d_Max) + UIUtils.safeInt(txtCC_e_Max) + UIUtils.safeInt(txtCC_xa_Max);
            lblCC_SumMin.setText(String.valueOf(ccMin));
            lblCC_SumMax.setText(String.valueOf(ccMax));

            int vcMin = UIUtils.safeInt(txtVC_d_Min) + UIUtils.safeInt(txtVC_e_Min) + UIUtils.safeInt(txtVC_xa_Min);
            int vcMax = UIUtils.safeInt(txtVC_d_Max) + UIUtils.safeInt(txtVC_e_Max) + UIUtils.safeInt(txtVC_xa_Max);
            lblVC_SumMin.setText(String.valueOf(vcMin));
            lblVC_SumMax.setText(String.valueOf(vcMax));

            String result = (ccMax >= tbCaoNhatValue) ? "<font color='#16a085'>đủ khả năng bảo đảm</font>" : "<font color='#c0392b'>không đủ khả năng</font>";
            lblKetLuan.setText("<html>=> Cân đối giữa nhu cầu và khả năng bảo đảm quân y trong ngày cao nhất, HC,KT/d " + result + " thu dung cấp cứu, vận chuyển TBBB.</html>");
        });
    }

    /**
     * HÀM ĐÃ SỬA: Loại bỏ các ô thừa khi f2 hoặc t3 truyền vào là null
     */
    private JPanel createRowPanel(String t1, JTextField f1, String t2, JTextField f2, String t3) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(0);

        if (t1 != null) p.add(UIUtils.createNormalText(t1));
        if (f1 != null) p.add(f1);
        if (t2 != null) p.add(UIUtils.createNormalText(t2));
        if (f2 != null) p.add(f2);
        if (t3 != null) p.add(UIUtils.createNormalText(t3));

        return p;
    }

    private JPanel createRangeRow(String label, JTextField min, JTextField max, String unit) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        p.setBackground(Color.WHITE); p.setAlignmentX(0);
        JLabel lbl = UIUtils.createNormalText("  " + label); lbl.setPreferredSize(new Dimension(320, 25));
        p.add(lbl); p.add(min); p.add(UIUtils.createNormalText(" ÷ ")); p.add(max); p.add(UIUtils.createNormalText(" " + unit));
        return p;
    }

    private JPanel createSummaryRow(String label, JLabel sMin, JLabel sMax, String unit) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(Color.WHITE); p.setAlignmentX(0);
        JLabel lbl = UIUtils.createNormalText(label); lbl.setPreferredSize(new Dimension(320, 25));
        p.add(lbl); p.add(sMin); p.add(UIUtils.createNormalText(" ÷ ")); p.add(sMax); p.add(UIUtils.createNormalText(unit));
        return p;
    }

    private void autoCalculateTBBBFromDB() {
        try (Connection conn = DBConnection.getConnection()) {
            String sqlQs = "SELECT SUM(q.quan_so) AS tong_qs FROM step2_bien_che s2 JOIN quyuoc_bienche q ON s2.quyuoc_id = q.id WHERE s2.session_id = ?";
            PreparedStatement pst = conn.prepareStatement(sqlQs);
            pst.setInt(1, sessionId);
            ResultSet rs = pst.executeQuery();
            int tongQs = rs.next() ? rs.getInt("tong_qs") : 0;

            String sqlTiLe = "SELECT loai_thuong_binh, ti_le FROM step4_ti_le_thuong_binh WHERE session_id = ?";
            pst = conn.prepareStatement(sqlTiLe); pst.setInt(1, sessionId);
            rs = pst.executeQuery();
            double tlTB = 0, tlBB = 0, tlMax = 0;
            while (rs.next()) {
                String l = rs.getString("loai_thuong_binh").toLowerCase();
                double v = rs.getDouble("ti_le");
                if (l.contains("thương binh toàn trận")) tlTB = v;
                else if (l.contains("bệnh binh toàn trận")) tlBB = v;
                else if (l.contains("ngày cao nhất")) tlMax = v;
            }
            txtTBToanTran.setText(String.valueOf(Math.round(tongQs * tlTB / 100.0)));
            txtBBToanTran.setText(String.valueOf(Math.round(tongQs * tlBB / 100.0)));
            this.tbCaoNhatValue = (int) Math.round(tongQs * tlMax / 100.0);
            txtTBCaoNhat.setText(String.valueOf(tbCaoNhatValue));
        } catch (Exception e) { e.printStackTrace(); }
    }
}