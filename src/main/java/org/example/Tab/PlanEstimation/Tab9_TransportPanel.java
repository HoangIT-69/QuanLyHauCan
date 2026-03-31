package org.example.Tab.PlanEstimation;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab9_TransportPanel extends JPanel {
    private JTextArea txtDuongVanTai, txtKhoiLuongToanTran, txtKhoiLuongChuanBi, txtKhoiLuongChienDau;
    private JTextField txtTanToanTran, txtTanChuanBi, txtTanChienDau;
    private JTextArea txtKhaNang, txtYdinhChuanBi, txtYdinhChienDau, txtYdinhSauChienDau;

    public Tab9_TransportPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề
        JLabel lblTitle = new JLabel("IX. CÔNG TÁC VẬN TẢI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // --- 1. Đường vận tải ---
        mainContainer.add(UIUtils.createSectionLabel("1. Đường vận tải"));
        txtDuongVanTai = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtDuongVanTai, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        // --- 2. Dự tính khối lượng ---
        mainContainer.add(UIUtils.createSectionLabel("2. Dự tính khối lượng vận chuyển"));

        txtTanToanTran = UIUtils.createNumberField(200); // Tận dụng Utils
        txtKhoiLuongToanTran = UIUtils.createStandardTextArea();
        mainContainer.add(createInputGroup("- Toàn trận (Khối lượng & ĐVT):", txtTanToanTran, "Trong đó:", txtKhoiLuongToanTran));
        mainContainer.add(Box.createVerticalStrut(15));

        txtTanChuanBi = UIUtils.createNumberField(200);
        txtKhoiLuongChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(createInputGroup("- Giai đoạn chuẩn bị:", txtTanChuanBi, "Trong đó:", txtKhoiLuongChuanBi));
        mainContainer.add(Box.createVerticalStrut(15));

        txtTanChienDau = UIUtils.createNumberField(200);
        txtKhoiLuongChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(createInputGroup("- Giai đoạn chiến đấu:", txtTanChienDau, "Trong đó:", txtKhoiLuongChienDau));
        mainContainer.add(Box.createVerticalStrut(20));

        // --- 3. Khả năng vận chuyển ---
        mainContainer.add(UIUtils.createSectionLabel("3. Khả năng vận chuyển"));
        mainContainer.add(UIUtils.createSubSectionLabel("* Cân đối về khả năng hoàn thành công tác vận tải:"));
        txtKhaNang = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtKhaNang, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        // --- 4. Ý định vận chuyển ---
        mainContainer.add(UIUtils.createSectionLabel("4. Ý định vận chuyển"));

        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        txtYdinhChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtYdinhChuanBi, 100));

        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtYdinhChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtYdinhChienDau, 100));

        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtYdinhSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtYdinhSauChienDau, 100));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================
    public Map<String, String> getTransportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<duong_van_tai>>", txtDuongVanTai.getText().trim());

        data.put("<<tan_toantran>>", txtTanToanTran.getText().trim());
        data.put("<<kl_toantran>>", txtKhoiLuongToanTran.getText().trim());

        data.put("<<tan_chuanbi>>", txtTanChuanBi.getText().trim());
        data.put("<<kl_chuanbi>>", txtKhoiLuongChuanBi.getText().trim());

        data.put("<<tan_chiendau>>", txtTanChienDau.getText().trim());
        data.put("<<kl_chiendau>>", txtKhoiLuongChienDau.getText().trim());

        data.put("<<can_doi_vt>>", txtKhaNang.getText().trim());
        data.put("<<ydinh_vt_cb>>", txtYdinhChuanBi.getText().trim());
        data.put("<<ydinh_vt_cd>>", txtYdinhChienDau.getText().trim());
        data.put("<<ydinh_vt_sau>>", txtYdinhSauChienDau.getText().trim());

        return data;
    }

    // --- LOGIC GIAO DIỆN NHÓM NHẬP LIỆU ---
    private JPanel createInputGroup(String topLabel, JTextField txtTop, String bottomLabel, JTextArea txtBottom) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(0);

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTop.setBackground(Color.WHITE);
        JLabel lblTop = UIUtils.createSubSectionLabel(topLabel);
        lblTop.setPreferredSize(new Dimension(240, 30));
        pnlTop.add(lblTop);
        pnlTop.add(txtTop);

        JPanel pnlBottom = new JPanel(new BorderLayout(0, 5));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.add(UIUtils.createSubSectionLabel(bottomLabel), BorderLayout.NORTH);
        pnlBottom.add(UIUtils.createTextAreaScroll(txtBottom, 80), BorderLayout.CENTER);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(pnlBottom, BorderLayout.CENTER);
        return pnl;
    }
}