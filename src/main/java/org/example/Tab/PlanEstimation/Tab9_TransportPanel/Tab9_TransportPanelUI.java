package org.example.Tab.PlanEstimation.Tab9_TransportPanel;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Tab9_TransportPanelUI extends JPanel {

    private static final int CHUYEN_MOI_NGAY = 4;

    private static final int NUM_FIELD_W = 60;
    private static final int NUM_FIELD_H = 30;
    private static final int BLANK_FIELD_W = 60;
    private static final int BLANK_FIELD_H = 25;
    private static final int CAP_FIELD_W = 100;

    /** Hệ số người (không có ô nhập trong form fill-in): mặc định 1. */
    private static final String DEFAULT_NGUOI = "1";

    private final int sessionId;
    private final Tab9_TransportPanelService service;

    private JTextArea txtDuongVanTai;

    private JLabel lblKlGdcb;
    private JLabel lblKlGdcd;
    private JLabel lblKlToanTran;

    private JTextField txtVtDMin;
    private JTextField txtVtDMax;
    private JTextField txtVtDXe;
    private JTextField txtVtEMin;
    private JTextField txtVtEMax;
    private JTextField txtVtEXe;
    private JTextField txtVtDqMin;
    private JTextField txtVtDqMax;
    private JTextField txtVtDqXe;

    private JLabel lblTongVtbKg;
    private JLabel lblCanDoiVanChuyen;
    /** Kết luận dạng thuần (xuất Word), đồng bộ với lblCanDoiVanChuyen. */
    private String ketLuanPlain = "";

    private JTextField tfKlCapMinhGdcb;
    private JTextField tfKlCapMinhGdcd;

    private JTextArea txtYdinhChuanBi;
    private JTextArea txtYdinhChienDau;
    private JTextArea txtYdinhSauChienDau;

    private Tab9_TransportPanelService.TransportDbSnapshot snapshot;

    public Tab9_TransportPanelUI(int sessionId, Tab9_TransportPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab9_TransportPanelService();
        this.snapshot = this.service.loadTransportSnapshot(sessionId);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = buildMainContainer();

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        wireRecalc(txtVtDMin, txtVtDMax, txtVtDXe, txtVtEMin, txtVtEMax, txtVtEXe,
                txtVtDqMin, txtVtDqMax, txtVtDqXe, tfKlCapMinhGdcb, tfKlCapMinhGdcd);

        applySnapshotLabels();
        recalcTransport();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                snapshot = Tab9_TransportPanelUI.this.service.loadTransportSnapshot(sessionId);
                applySnapshotLabels();
                recalcTransport();
            }
        });
    }

    private JPanel buildMainContainer() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("IX. CÔNG TÁC VẬN TẢI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        addLeft(mainContainer, lblTitle);
        mainContainer.add(Box.createVerticalStrut(20));

        addLeft(mainContainer, UIUtils.createSectionLabel("1. Đường vận tải"));
        txtDuongVanTai = UIUtils.createStandardTextArea();
        addLeft(mainContainer, wrapTextAreaScroll(txtDuongVanTai, 100));
        mainContainer.add(Box.createVerticalStrut(16));

        addLeft(mainContainer, UIUtils.createSectionLabel("2. Dự tính khối lượng vận chuyển "));
        lblKlGdcb = new JLabel(" ");
        lblKlGdcd = new JLabel(" ");
        lblKlToanTran = new JLabel(" ");
        styleAutoLabel(lblKlGdcb);
        styleAutoLabel(lblKlGdcd);
        styleAutoLabel(lblKlToanTran);
        addLeft(mainContainer, flowRow(lblKlGdcb));
        addLeft(mainContainer, flowRow(lblKlGdcd));
        addLeft(mainContainer, flowRow(lblKlToanTran));
        mainContainer.add(Box.createVerticalStrut(16));

        addLeft(mainContainer, UIUtils.createSectionLabel("3. Cân đối khả năng"));
        mainContainer.add(Box.createVerticalStrut(6));

        JLabel lblCanDoiHeading = new JLabel("* Cân đối về khả năng hoàn thành công tác vận tải");
        lblCanDoiHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCanDoiHeading.setForeground(new Color(30, 41, 59));
        addLeft(mainContainer, flowRowTight(lblCanDoiHeading));
        mainContainer.add(Box.createVerticalStrut(8));

        txtVtDMin = blankField();
        txtVtDMax = blankField();
        txtVtDXe = blankField();
        addLeft(mainContainer, flowRowTight(
                plainLabel("- Vận tải bộ của tiểu đoàn: "),
                txtVtDMin,
                plainLabel(" ÷ "),
                txtVtDMax,
                plainLabel(" kg/chuyến         Xe đạp thồ ( "),
                txtVtDXe,
                plainLabel(" xe)")
        ));
        mainContainer.add(Box.createVerticalStrut(4));

        txtVtEMin = blankField();
        txtVtEMax = blankField();
        txtVtEXe = blankField();
        addLeft(mainContainer, flowRowTight(
                plainLabel("- Vận tải bộ của trung đoàn: "),
                txtVtEMin,
                plainLabel(" ÷ "),
                txtVtEMax,
                plainLabel(" kg/chuyến         Xe đạp thồ ( "),
                txtVtEXe,
                plainLabel(" xe)")
        ));
        mainContainer.add(Box.createVerticalStrut(4));

        txtVtDqMin = blankField();
        txtVtDqMax = blankField();
        txtVtDqXe = blankField();
        addLeft(mainContainer, flowRowTight(
                plainLabel("- Dân quân phục vụ chiến đấu: "),
                txtVtDqMin,
                plainLabel(" ÷ "),
                txtVtDqMax,
                plainLabel(" kg/chuyến         Xe đạp thồ ( "),
                txtVtDqXe,
                plainLabel(" xe)")
        ));
        mainContainer.add(Box.createVerticalStrut(10));

        tfKlCapMinhGdcb = compactCapField();
        tfKlCapMinhGdcd = compactCapField();
        JPanel pCap = flowRow(
                UIUtils.createNormalText("Khối lượng cấp mình chuyển GĐCB (kg):"),
                tfKlCapMinhGdcb,
                UIUtils.createNormalText("GĐCĐ (kg):"),
                tfKlCapMinhGdcd
        );
        addLeft(mainContainer, pCap);
        mainContainer.add(Box.createVerticalStrut(10));

        lblTongVtbKg = new JLabel("0 ÷ 0");
        lblTongVtbKg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongVtbKg.setForeground(new Color(37, 99, 235));
        JLabel lblTongPrefix = new JLabel("Tổng cộng khả năng vận chuyển của VTB là: ");
        lblTongPrefix.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblTongSuffix = new JLabel(" kg/chuyến (Mỗi xe đạp thồ vận chuyển 250kg/chuyến)");
        lblTongSuffix.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addLeft(mainContainer, flowRowTight(lblTongPrefix, lblTongVtbKg, lblTongSuffix));
        mainContainer.add(Box.createVerticalStrut(8));

        lblCanDoiVanChuyen = new JLabel(" ");
        lblCanDoiVanChuyen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCanDoiVanChuyen.setForeground(new Color(30, 41, 59));
        JPanel pKetLuan = flowRowTight(lblCanDoiVanChuyen);
        pKetLuan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        addLeft(mainContainer, pKetLuan);
        mainContainer.add(Box.createVerticalStrut(18));

        addLeft(mainContainer, UIUtils.createSectionLabel("4. Ý định vận chuyển"));
        addLeft(mainContainer, UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        txtYdinhChuanBi = UIUtils.createStandardTextArea();
        addLeft(mainContainer, wrapTextAreaScroll(txtYdinhChuanBi, 100));
        mainContainer.add(Box.createVerticalStrut(8));
        addLeft(mainContainer, UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        txtYdinhChienDau = UIUtils.createStandardTextArea();
        addLeft(mainContainer, wrapTextAreaScroll(txtYdinhChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(8));
        addLeft(mainContainer, UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        txtYdinhSauChienDau = UIUtils.createStandardTextArea();
        addLeft(mainContainer, wrapTextAreaScroll(txtYdinhSauChienDau, 100));

        return mainContainer;
    }

    private static void addLeft(JPanel box, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(c);
    }

    /**
     * Một hàng căn trái, không giãn ngang vô hạn; chiều cao cố định để BoxLayout không đè.
     */
    private static JPanel flowRow(Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBackground(Color.WHITE);
        for (Component part : parts) {
            p.add(part);
        }
        int h = Math.max(NUM_FIELD_H + 8, p.getPreferredSize().height);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** FlowLayout bám sát từng phần tử (Mục 3 — dòng điền chỗ trống). */
    private static JPanel flowRowTight(Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        p.setBackground(Color.WHITE);
        for (Component part : parts) {
            p.add(part);
        }
        int h = Math.max(BLANK_FIELD_H + 8, p.getPreferredSize().height);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JLabel plainLabel(String text) {
        JLabel j = new JLabel(text);
        j.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        j.setForeground(new Color(30, 41, 59));
        return j;
    }

    private static JTextField blankField() {
        JTextField t = UIUtils.createNumberField(BLANK_FIELD_W);
        t.setHorizontalAlignment(JTextField.RIGHT);
        Dimension d = new Dimension(BLANK_FIELD_W, BLANK_FIELD_H);
        t.setPreferredSize(d);
        t.setMinimumSize(d);
        t.setMaximumSize(new Dimension(BLANK_FIELD_W + 8, BLANK_FIELD_H));
        return t;
    }

    private static JScrollPane wrapTextAreaScroll(JTextArea textArea, int height) {
        JScrollPane scroll = UIUtils.createTextAreaScroll(textArea, height);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private static JTextField compactNumField() {
        JTextField t = UIUtils.createNumberField(NUM_FIELD_W);
        t.setHorizontalAlignment(JTextField.RIGHT);
        Dimension d = new Dimension(NUM_FIELD_W, NUM_FIELD_H);
        t.setPreferredSize(d);
        t.setMinimumSize(d);
        t.setMaximumSize(new Dimension(NUM_FIELD_W + 8, NUM_FIELD_H));
        return t;
    }

    private static JTextField compactCapField() {
        JTextField t = UIUtils.createNumberField(CAP_FIELD_W);
        t.setHorizontalAlignment(JTextField.RIGHT);
        Dimension d = new Dimension(CAP_FIELD_W, NUM_FIELD_H);
        t.setPreferredSize(d);
        t.setMinimumSize(new Dimension(80, NUM_FIELD_H));
        t.setMaximumSize(new Dimension(CAP_FIELD_W + 20, NUM_FIELD_H));
        return t;
    }

    private static void styleAutoLabel(JLabel l) {
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void wireRecalc(JTextField... fields) {
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalcTransport();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalcTransport();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalcTransport();
            }
        };
        for (JTextField f : fields) {
            f.getDocument().addDocumentListener(dl);
        }
    }

    /** Reload khối lượng vận chuyển tự tính khi dữ liệu khai báo thay đổi (không reset text nhập tay). */
    public void reloadSnapshot() {
        this.snapshot = service.loadTransportSnapshot(sessionId);
        applySnapshotLabels();
        recalcTransport();
    }

    private void applySnapshotLabels() {
        double gdcb = snapshot.sumTieuThuGdcb;
        double gdcd = snapshot.sumTieuThuGdcd;
        double sum = gdcb + gdcd;
        lblKlGdcb.setText("Khối lượng bổ sung GĐCB (tấn): " + Tab9_TransportPanelService.formatTon(gdcb));
        lblKlGdcd.setText("Khối lượng bổ sung GĐCĐ (tấn): " + Tab9_TransportPanelService.formatTon(gdcd));
        lblKlToanTran.setText("Khối lượng bổ sung toàn trận — GĐCB + GĐCĐ (tấn): " + Tab9_TransportPanelService.formatTon(sum));

        // Tự động điền khối lượng cấp mình chuyển (tấn -> kg)
        if (gdcb > 0) {
            tfKlCapMinhGdcb.setText(String.valueOf((int) Math.round(gdcb * 1000)));
        }
        if (gdcd > 0) {
            tfKlCapMinhGdcd.setText(String.valueOf((int) Math.round(gdcd * 1000)));
        }
    }

    private void recalcTransport() {
        SwingUtilities.invokeLater(() -> {
            double n1 = InputValidator.parseDoubleSafe(DEFAULT_NGUOI);
            double x1 = InputValidator.parseDoubleSafe(getTdXeText());
            double n2 = InputValidator.parseDoubleSafe(DEFAULT_NGUOI);
            double x2 = InputValidator.parseDoubleSafe(getTrXeText());
            double n3 = InputValidator.parseDoubleSafe(DEFAULT_NGUOI);
            double x3 = InputValidator.parseDoubleSafe(getDqXeText());

            Tab9_TransportPanelService.CapacityBreakdown cap = Tab9_TransportPanelService.calculateTotalCapacity(
                    n1, getTdKgMinText(), getTdKgMaxText(), x1,
                    n2, getTrKgMinText(), getTrKgMaxText(), x2,
                    n3, getDqKgMinText(), getDqKgMaxText(), x3);

            String minStr = Tab9_TransportPanelService.formatKgChuyen(cap.tongMin);
            String maxStr = Tab9_TransportPanelService.formatKgChuyen(cap.tongMax);
            lblTongVtbKg.setText(minStr + " ÷ " + maxStr);

            double tongMinChuyen = cap.tongMin;
            double tongMaxChuyen = cap.tongMax;
            int nd = Math.max(1, snapshot.ngayAnGdcb);
            int ne = Math.max(1, snapshot.ngayAnGdcd);
            double khaGdcbMin = tongMinChuyen * CHUYEN_MOI_NGAY * nd;
            double khaGdcbMax = tongMaxChuyen * CHUYEN_MOI_NGAY * nd;
            double khaGdcdMin = tongMinChuyen * CHUYEN_MOI_NGAY * ne;
            double khaGdcdMax = tongMaxChuyen * CHUYEN_MOI_NGAY * ne;

            double capGdcb = InputValidator.parseDoubleSafe(tfKlCapMinhGdcb.getText());
            double capGdcd = InputValidator.parseDoubleSafe(tfKlCapMinhGdcd.getText());

            String lineGdcb = lineKetLuanRange("GĐCB", khaGdcbMin, khaGdcbMax, capGdcb);
            String lineGdcd = lineKetLuanRange("GĐCĐ", khaGdcdMin, khaGdcdMax, capGdcd);

            ketLuanPlain = lineGdcb + "\n" + lineGdcd;
            lblCanDoiVanChuyen.setText(ketLuanHtml(ketLuanPlain));
        });
    }

    private static String ketLuanHtml(String plain) {
        String esc = plain.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><body style='width:820px;font-family:Segoe UI;font-size:12px;font-weight:normal'>"
                + esc.replace("\n", "<br/>") + "</body></html>";
    }

    /** So khả năng khoảng [khaMin, khaMax] (kg cả giai đoạn) với nhu cầu cấp mình. */
    private static String lineKetLuanRange(String phase, double khaMin, double khaMax, double capMinh) {
        if (capMinh <= 0) {
            return phase + ": Nhập khối lượng cấp mình chuyển (kg) để đối chiếu.";
        }
        if (khaMax + 1e-6 >= capMinh) {
            return phase + ": Đủ khả năng (khoảng " + fmt(khaMin) + " – " + fmt(khaMax) + " kg ≥ nhu cầu " + fmt(capMinh) + " kg).";
        }
        return phase + ": Không đủ khả năng (khoảng " + fmt(khaMin) + " – " + fmt(khaMax) + " kg < nhu cầu " + fmt(capMinh) + " kg).";
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%,.0f", v);
    }

    public Map<String, String> getTransportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<duong_van_tai>>", nz(txtDuongVanTai.getText()));

        double gdcb = snapshot.sumTieuThuGdcb;
        double gdcd = snapshot.sumTieuThuGdcd;
        double sumT = gdcb + gdcd;
        data.put("<<tan_toantran>>", Tab9_TransportPanelService.formatTon(sumT));
        data.put("<<kl_toantran>>", "GĐCB: " + Tab9_TransportPanelService.formatTon(gdcb) + " tấn; GĐCĐ: "
                + Tab9_TransportPanelService.formatTon(gdcd) + " tấn; Tổng: " + Tab9_TransportPanelService.formatTon(sumT) + " tấn.");

        data.put("<<tan_chuanbi>>", Tab9_TransportPanelService.formatTon(gdcb));
        data.put("<<kl_chuanbi>>", "Số ngày GĐCB (Gạo/Lương): " + snapshot.ngayAnGdcb);
        data.put("<<tan_chiendau>>", Tab9_TransportPanelService.formatTon(gdcd));
        data.put("<<kl_chiendau>>", "Số ngày GĐCĐ: " + snapshot.ngayAnGdcd);

        data.put("<<ydinh_vt_cb>>", nz(txtYdinhChuanBi.getText()));
        data.put("<<ydinh_vt_cd>>", nz(txtYdinhChienDau.getText()));
        data.put("<<ydinh_vt_sau>>", nz(txtYdinhSauChienDau.getText()));
        return data;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    public String getTdNguoiText() {
        return DEFAULT_NGUOI;
    }

    public String getTdKgMinText() {
        return txtVtDMin != null ? txtVtDMin.getText() : "";
    }

    public String getTdKgMaxText() {
        return txtVtDMax != null ? txtVtDMax.getText() : "";
    }

    public String getTdXeText() {
        return txtVtDXe != null ? txtVtDXe.getText() : "";
    }

    public String getTrNguoiText() {
        return DEFAULT_NGUOI;
    }

    public String getTrKgMinText() {
        return txtVtEMin != null ? txtVtEMin.getText() : "";
    }

    public String getTrKgMaxText() {
        return txtVtEMax != null ? txtVtEMax.getText() : "";
    }

    public String getTrXeText() {
        return txtVtEXe != null ? txtVtEXe.getText() : "";
    }

    public String getDqNguoiText() {
        return DEFAULT_NGUOI;
    }

    public String getDqKgMinText() {
        return txtVtDqMin != null ? txtVtDqMin.getText() : "";
    }

    public String getDqKgMaxText() {
        return txtVtDqMax != null ? txtVtDqMax.getText() : "";
    }

    public String getDqXeText() {
        return txtVtDqXe != null ? txtVtDqXe.getText() : "";
    }

    public String getKetLuanText() {
        return ketLuanPlain != null ? ketLuanPlain.trim() : "";
    }
}
