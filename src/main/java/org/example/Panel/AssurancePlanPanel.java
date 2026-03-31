package org.example.Panel;

import org.example.Tab.AssurancePlan.*;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel;
import org.example.Tab.PlanEstimation.Tab10_ProtectionPanel;
import org.example.Tab.PlanEstimation.Tab11_CommandPanel;
import org.example.Utils.ExportWord;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssurancePlanPanel extends JPanel {

    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton currentSelectedBtn = null;

    private final String initialDanhGia;
    private final String initialNhiemVu;
    // Thêm các biến chứa dữ liệu từ Plan Estimation
    private final Map<String, String> initialTab6Data;
    private final Map<String, String> initialTab10Data;
    private final Map<String, String> initialTab11Data;

    private final int sessionId;

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

    // ĐÃ CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN THÊM DATA TỪ TAB 6, 10, 11
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

        JLabel lblTitle = new JLabel("KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        initTabs();
        add(createFooterPanel(), BorderLayout.SOUTH);

        if (menuPanel.getComponentCount() > 0 && menuPanel.getComponent(0) instanceof JButton) {
            ((JButton) menuPanel.getComponent(0)).doClick();
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

        // NẠP DỮ LIỆU TỪ MAP VÀO TAB 6
        tab6 = new Tab6_LivingPanel();
        if (initialTab6Data != null) {
            tab6.setAnUong(initialTab6Data.getOrDefault("<<bd_an_uong>>", ""));
            tab6.setMac(initialTab6Data.getOrDefault("<<bd_mac>>", ""));
            tab6.setONguNghi(initialTab6Data.getOrDefault("<<bd_o_ngunghi>>", ""));
        }
        addTab("VI. Bảo đảm đời sống", "tab6", tab6);

        tab7 = new Tab7_MedicalPanel();
        addTab("VII. Bảo đảm quân y", "tab7", tab7);

        tab8 = new Tab8_MaintenancePanel();
        addTab("VIII. Bảo dưỡng, sửa chữa", "tab8", tab8);

        tab9 = new Tab9_TransportPanel();
        addTab("IX. Công tác vận tải", "tab9", tab9);

        // NẠP DỮ LIỆU TỪ MAP VÀO TAB 10
        tab10 = new Tab10_ProtectionPanel();
        if (initialTab10Data != null) {
            tab10.setTinhHuong(initialTab10Data.getOrDefault("<<tinh_huong_bv>>", ""));
            tab10.setBienPhap(initialTab10Data.getOrDefault("<<bien_phap_bv>>", ""));
        }
        addTab("X. Tổ chức bảo vệ", "tab10", tab10);

        // NẠP DỮ LIỆU TỪ MAP VÀO TAB 11
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

        InputStream templateStream = getClass().getResourceAsStream("/docs/word2.docx");
        if (templateStream == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy file mẫu tại src/main/resources/docs/word2.docx",
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

        dataMap.put("<<danh_gia_tinh_hinh>>", tab1.getDanhGia()); // FIX KEY THEO TAB 1 NẾU CẦN
        dataMap.put("<<nhiem_vu>>", tab2.getNhiemVu());           // FIX KEY THEO TAB 2 NẾU CẦN
        dataMap.putAll(tab3.getExportData());
        dataMap.putAll(tab4.getExportData());
        dataMap.putAll(tab5.getExportData());

        // LẤY MAP TỪ TAB 6
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