package org.example.Panel;

import org.example.Tab.AssurancePlan.*;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel;
import org.example.Tab.PlanEstimation.Tab10_ProtectionPanel;
import org.example.Tab.PlanEstimation.Tab11_CommandPanel;
import org.example.Utils.DBConnection;
import org.example.Utils.ExportWord;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AssurancePlanPanel extends JPanel {

    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton currentSelectedBtn = null;

    private final String initialDanhGia;
    private final String initialNhiemVu;
    private final Map<String, String> initialTab6Data;
    private final Map<String, String> initialTab10Data;
    private final Map<String, String> initialTab11Data;

    private final int sessionId;

    // Đưa lblTitle lên thành biến toàn cục để dễ dàng đổi tên
    private JLabel lblTitle;

    // Map chứa dữ liệu chung (Tên văn kiện, Tọa độ, Bản đồ...) để ném vào Word
    private Map<String, String> thongTinChungData = new HashMap<>();

    private Tab1_EvaluationPanel tab1;
    private Tab2_MissionPanel tab2;
    private Tab3_OrganizationPanel tab3;
    private Tab4_EquipmentPanel tab4;
    private Tab5_MaterialPanel tab5;
    private Tab6_LivingPanel tab6;
    private Tab7_MedicalPanel tab7;
    private Tab8_MaintenancePanel tab8;
    private Tab9_TransportPanel tab9;
    private Tab10_ProtectionPanel tab10;
    private Tab11_CommandPanel tab11;

    public AssurancePlanPanel(String initialDanhGia, String initialNhiemVu,
                              Map<String, String> initialTab6Data,
                              Map<String, String> initialTab10Data,
                              Map<String, String> initialTab11Data,
                              int sessionId) {
        this.initialDanhGia = initialDanhGia;
        this.initialNhiemVu = initialNhiemVu;
        this.initialTab6Data = initialTab6Data;
        this.initialTab10Data = initialTab10Data;
        this.initialTab11Data = initialTab11Data;
        this.sessionId = sessionId;

        setLayout(new BorderLayout(15, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        lblTitle = new JLabel("KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        // Kéo dữ liệu chung từ DB để đổi Tiêu đề và gom thông tin xuất Word
        loadThongTinChung();

        initTabs();
        add(createFooterPanel(), BorderLayout.SOUTH);

        if (menuPanel.getComponentCount() > 0 && menuPanel.getComponent(0) instanceof JButton) {
            ((JButton) menuPanel.getComponent(0)).doClick();
        }
    }

    // =========================================================================
    // HÀM LẤY THÔNG TIN CHUNG TỪ DB ĐỂ HIỂN THỊ TIÊU ĐỀ & XUẤT WORD
    // =========================================================================
    private void loadThongTinChung() {
        // Đổi tên bảng step1_ttchung thành tên đúng trong CSDL của bạn nếu khác
        String sql = "SELECT * FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String tenVK = rs.getString("ten_van_kien");
                if (tenVK != null && !tenVK.trim().isEmpty()) {
                    // Đổi tên Tiêu đề Giao diện
                    lblTitle.setText("KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT " + tenVK.toUpperCase());
                }

                // Gắn dữ liệu vào Map Word với format {{...}} y hệt trong file mẫu DOCX
                thongTinChungData.put("{{nguoi_phe_chuan}}", rs.getString("chi_huy"));
                thongTinChungData.put("{{toa_do}}", "VTCH: " + rs.getString("vi_tri_chi_huy") + " " + rs.getString("thoi_gian"));
                thongTinChungData.put("{{ban_do_su_dung}}", "Bản đồ tỷ lệ " + rs.getString("ty_le") + " BTTM in năm " + rs.getString("nam"));

                // Nếu Word có cài đặt thẻ cho 4 mảnh bản đồ:
                thongTinChungData.put("{{map_1}}", rs.getString("map_1"));
                thongTinChungData.put("{{map_2}}", rs.getString("map_2"));
                thongTinChungData.put("{{map_3}}", rs.getString("map_3"));
                thongTinChungData.put("{{map_4}}", rs.getString("map_4"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin chung: " + e.getMessage());
        }
    }

    private void initTabs() {
        bodyPanelSetup();

        tab1 = new Tab1_EvaluationPanel();
        tab1.setDanhGia(this.initialDanhGia);
        addTab("I. Kết luận, đánh giá tình hình", "tab1", tab1);

        tab2 = new Tab2_MissionPanel();
        tab2.setNhiemVu(this.initialNhiemVu);
        addTab("II. Nhiệm vụ", "tab2", tab2);

        tab3 = new Tab3_OrganizationPanel();
        addTab("III. Tổ chức, sử dụng lực lượng...", "tab3", tab3);

        tab4 = new Tab4_EquipmentPanel();
        tab4.loadDataFromDatabase(this.sessionId);
        addTab("IV. Bảo đảm trang bị kĩ thuật", "tab4", tab4);

        tab5 = new Tab5_MaterialPanel();
        tab5.loadSessionData(this.sessionId);
        addTab("V. Bảo đảm đạn, vật chất...", "tab5", tab5);

        tab6 = new Tab6_LivingPanel();
        if (initialTab6Data != null) {
            tab6.setAnUong(initialTab6Data.getOrDefault("<<bd_an_uong>>", ""));
            tab6.setMac(initialTab6Data.getOrDefault("<<bd_mac>>", ""));
            tab6.setONguNghi(initialTab6Data.getOrDefault("<<bd_o_ngunghi>>", ""));
        }
        addTab("VI. Bảo đảm đời sống", "tab6", tab6);

        tab7 = new Tab7_MedicalPanel();
        tab7.loadDataFromDatabase(this.sessionId);
        addTab("VII. Bảo đảm quân y", "tab7", tab7);

        tab8 = new Tab8_MaintenancePanel();
        tab8.loadDataFromDatabase(this.sessionId);
        addTab("VIII. Bảo dưỡng, sửa chữa", "tab8", tab8);

        tab9 = new Tab9_TransportPanel();
        addTab("IX. Công tác vận tải", "tab9", tab9);

        tab10 = new Tab10_ProtectionPanel();
        if (initialTab10Data != null) {
            tab10.setTinhHuong(initialTab10Data.getOrDefault("<<tinh_huong_bv>>", ""));
            tab10.setBienPhap(initialTab10Data.getOrDefault("<<bien_phap_bv>>", ""));
        }
        addTab("X. Tổ chức bảo vệ", "tab10", tab10);

        tab11 = new Tab11_CommandPanel(sessionId);
        if (initialTab11Data != null) {
            tab11.setTrienKhai(initialTab11Data.getOrDefault("<<trien_khai_ch>>", ""));
            tab11.setTTChuanBi(initialTab11Data.getOrDefault("<<ttll_cb>>", ""));
            tab11.setTTChienDau(initialTab11Data.getOrDefault("<<ttll_cd>>", ""));
            tab11.setBaoCaoChuanBi(initialTab11Data.getOrDefault("<<bc_cb>>", ""));
            tab11.setBaoCaoChienDau1(initialTab11Data.getOrDefault("<<bc_cd1>>", ""));
            tab11.setBaoCaoChienDau2(initialTab11Data.getOrDefault("<<bc_cd2>>", ""));
        }
        addTab("XI. Tổ chức chỉ huy", "tab11", tab11);
    }

    private JPanel createFooterPanel() {
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setOpaque(false);

        JButton btnSave = UIUtils.createStyledButton("Lưu Bản Nháp", new Color(34, 197, 94));
        JButton btnExport = UIUtils.createStyledButton("Xuất File Word", new Color(41, 128, 185));

        btnExport.addActionListener(e -> performExport());

        pnlFooter.add(btnSave);
        pnlFooter.add(btnExport);

        return pnlFooter;
    }

    private void performExport() {
        Map<String, String> dataMap = collectExportData();

        InputStream templateStream = getClass().getResourceAsStream("/docs/template-PN_BDKH.docx");
        if (templateStream == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy file mẫu tại src/main/resources/docs/template-PN_BDKH.docx",
                    "Lỗi Hệ Thống",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String outputPath = ExportWord.chooseSaveDocxPath(this, "KeHoachBaoDam_HoanChinh.docx");
        if (outputPath == null) return;

        try {
            ExportWord.exportDataToWord(templateStream, outputPath, dataMap);
            JOptionPane.showMessageDialog(
                    this,
                    "Xuất file Word thành công:\n" + outputPath,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi xuất file: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Map<String, String> collectExportData() {
        Map<String, String> dataMap = new HashMap<>();

        // 1. CHÈN TOÀN BỘ DATA THÔNG TIN CHUNG (Trang bìa Word) VÀO TRƯỚC
        dataMap.putAll(thongTinChungData);

        // 2. CHÈN DATA TỪ CÁC TAB
        dataMap.put("<<danh_gia_tinh_hinh>>", tab1.getDanhGia());
        dataMap.put("<<nhiem_vu>>", tab2.getNhiemVu());
        dataMap.putAll(tab3.getExportData());
        dataMap.putAll(tab4.getExportData());
        dataMap.putAll(tab5.getExportData());

        Map<String, String> mapTab6 = tab6.getExportData();
        if (mapTab6 != null) dataMap.putAll(mapTab6);

        dataMap.putAll(tab7.getExportData());
        dataMap.putAll(tab8.getExportData());
        dataMap.putAll(tab9.getExportData());
        dataMap.putAll(tab10.getProtectionData());
        dataMap.putAll(tab11.getCommandData());

        return dataMap;
    }

    private void bodyPanelSetup() {
        JPanel bodyPanel = new JPanel(new BorderLayout(15, 0));
        bodyPanel.setOpaque(false);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 230), 1),
                new EmptyBorder(10, 5, 10, 5)
        ));
        menuPanel.setPreferredSize(new Dimension(300, 0));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new LineBorder(new Color(220, 225, 230), 1));

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);

        bodyPanel.add(menuScroll, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);
    }

    private void addTab(String buttonText, String cardId, JPanel panelToAdd) {
        contentPanel.add(panelToAdd, cardId);

        JButton btn = new JButton(buttonText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 15, 12, 10));
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addActionListener(e -> {
            if (currentSelectedBtn != null) {
                currentSelectedBtn.setBackground(Color.WHITE);
                currentSelectedBtn.setForeground(new Color(71, 85, 105));
            }

            currentSelectedBtn = btn;
            btn.setBackground(new Color(219, 234, 254));
            btn.setForeground(new Color(41, 128, 185));

            cardLayout.show(contentPanel, cardId);
        });

        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(2));
    }
}