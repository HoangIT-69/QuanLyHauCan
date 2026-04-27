package org.example.Panel.PN_AssurancePlanPanel;

import org.example.Tab.AssurancePlan.Tab3_OrgPlanPanel.Tab3_OrgPlanPanelUI;
import org.example.Tab.AssurancePlan.Tab4_EquipPlanPanel.Tab4_EquipPlanPanelUI;
import org.example.Tab.AssurancePlan.Tab5_MaterialPlanPanel.Tab5_MaterialPanelUI;
import org.example.Tab.AssurancePlan.Tab7_MedPlanPanel.Tab7_MedPlanPanelUI;
import org.example.Tab.AssurancePlan.Tab8_MaintPlanPanel.Tab8_MaintPlanPanelUI;
import org.example.Tab.AssurancePlan.Tav9_TransportPanel.Tab9_TransportPanel;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel.Tab1_EvaluationPanelService;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel.Tab1_EvaluationPanelUI;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel.Tab2_MissionPanelService;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel.Tab2_MissionPanelUI;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel.Tab6_LivingPanelService;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel.Tab6_LivingPanelUI;
import org.example.Tab.PlanEstimation.Tab10_ProtectionPanelUI;
import org.example.Tab.PlanEstimation.Tab11_CommandPanelService;
import org.example.Tab.PlanEstimation.Tab11_CommandPanelUI;
import org.example.Utils.ExportWord;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PN_AssurancePlanPanelUI extends JPanel {

    private final PN_AssurancePlanPanelService panelService;

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

    private JLabel lblTitle;

    private Map<String, String> thongTinChungData = new HashMap<>();

    private Tab1_EvaluationPanelUI tab1;
    private Tab2_MissionPanelUI tab2;
    private Tab3_OrgPlanPanelUI tab3;
    private Tab4_EquipPlanPanelUI tab4;
    private Tab5_MaterialPanelUI tab5;
    private Tab6_LivingPanelUI tab6;
    private Tab7_MedPlanPanelUI tab7;
    private Tab8_MaintPlanPanelUI tab8;
    private Tab9_TransportPanel tab9;
    private Tab10_ProtectionPanelUI tab10;
    private Tab11_CommandPanelUI tab11;

    public PN_AssurancePlanPanelUI(String initialDanhGia, String initialNhiemVu,
                                   Map<String, String> initialTab6Data,
                                   Map<String, String> initialTab10Data,
                                   Map<String, String> initialTab11Data,
                                   int sessionId) {
        this(initialDanhGia, initialNhiemVu, initialTab6Data, initialTab10Data, initialTab11Data, sessionId, new PN_AssurancePlanPanelService(), null);
    }

    public PN_AssurancePlanPanelUI(String initialDanhGia, String initialNhiemVu,
                                   Map<String, String> initialTab6Data,
                                   Map<String, String> initialTab10Data,
                                   Map<String, String> initialTab11Data,
                                   int sessionId,
                                   PN_AssurancePlanPanelService panelService) {
        this(initialDanhGia, initialNhiemVu, initialTab6Data, initialTab10Data, initialTab11Data, sessionId, panelService, null);
    }

    /**
     * @param preloadedThongTinChung nếu khác null (đã load trên worker thread), bỏ query trùng trên EDT.
     */
    public PN_AssurancePlanPanelUI(String initialDanhGia, String initialNhiemVu,
                                   Map<String, String> initialTab6Data,
                                   Map<String, String> initialTab10Data,
                                   Map<String, String> initialTab11Data,
                                   int sessionId,
                                   PN_AssurancePlanPanelService panelService,
                                   PN_AssurancePlanPanelService.ThongTinChungLoad preloadedThongTinChung) {
        this.initialDanhGia = initialDanhGia;
        this.initialNhiemVu = initialNhiemVu;
        this.initialTab6Data = initialTab6Data;
        this.initialTab10Data = initialTab10Data;
        this.initialTab11Data = initialTab11Data;
        this.sessionId = sessionId;
        this.panelService = panelService != null ? panelService : new PN_AssurancePlanPanelService();

        setLayout(new BorderLayout(15, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        lblTitle = new JLabel("KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        PN_AssurancePlanPanelService.ThongTinChungLoad load = preloadedThongTinChung != null
                ? preloadedThongTinChung
                : this.panelService.loadThongTinChung(sessionId);
        lblTitle.setText(load.fullTitle);
        thongTinChungData.putAll(load.wordPlaceholders);

        initTabs();
        add(createFooterPanel(), BorderLayout.SOUTH);

        if (menuPanel.getComponentCount() > 0 && menuPanel.getComponent(0) instanceof JButton) {
            ((JButton) menuPanel.getComponent(0)).doClick();
        }
    }

    private void initTabs() {
        bodyPanelSetup();

        tab1 = new Tab1_EvaluationPanelUI(sessionId, new Tab1_EvaluationPanelService());
        tab1.setDanhGia(this.initialDanhGia);
        addTab("I. Kết luận, đánh giá tình hình", "tab1", tab1);

        tab2 = new Tab2_MissionPanelUI(sessionId, new Tab2_MissionPanelService());
        tab2.setNhiemVu(this.initialNhiemVu);
        addTab("II. Nhiệm vụ", "tab2", tab2);

        tab3 = new Tab3_OrgPlanPanelUI(sessionId);
        addTab("III. Tổ chức, sử dụng lực lượng...", "tab3", tab3);

        tab4 = new Tab4_EquipPlanPanelUI();
        tab4.loadDataFromDatabase(this.sessionId);
        addTab("IV. Bảo đảm vũ khí trang bị kỹ thuật", "tab4", tab4);

        tab5 = new Tab5_MaterialPanelUI();
        tab5.loadSessionData(this.sessionId);
        addTab("V. Bảo đảm đạn, vật chất...", "tab5", tab5);

        tab6 = new Tab6_LivingPanelUI(sessionId, new Tab6_LivingPanelService());
        if (initialTab6Data != null) {
            tab6.setAnUong(initialTab6Data.getOrDefault("<<bd_an_uong>>", ""));
            tab6.setMac(initialTab6Data.getOrDefault("<<bd_mac>>", ""));
            tab6.setONguNghi(initialTab6Data.getOrDefault("<<bd_o_ngunghi>>", ""));
        }
        addTab("VI. Bảo đảm sinh hoạt", "tab6", tab6);

        tab7 = new Tab7_MedPlanPanelUI();
        tab7.loadDataFromDatabase(this.sessionId);
        addTab("VII. Bảo đảm quân y", "tab7", tab7);

        tab8 = new Tab8_MaintPlanPanelUI();
        tab8.loadDataFromDatabase(this.sessionId);
        addTab("VIII. Bảo dưỡng, sửa chữa", "tab8", tab8);

        tab9 = new Tab9_TransportPanel(this.sessionId);
        addTab("IX. Công tác vận tải", "tab9", tab9);

        tab10 = new Tab10_ProtectionPanelUI();
        if (initialTab10Data != null) {
            tab10.setTinhHuong(initialTab10Data.getOrDefault("<<tinh_huong_bv>>", ""));
            tab10.setBienPhap(initialTab10Data.getOrDefault("<<bien_phap_bv>>", ""));
        }
        addTab("X. Tổ chức bảo vệ", "tab10", tab10);

        tab11 = new Tab11_CommandPanelUI(sessionId, new Tab11_CommandPanelService());
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

    private String buildAssuranceFileName() {
        String nguoiLap = org.example.Utils.AppSession.getFullName();
        if (nguoiLap.isBlank()) {
            return "Ke_hoach_BaoDam.docx";
        }
        String namePart = nguoiLap.trim().replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", "_");
        return "Ke_hoach_cua_" + namePart + ".docx";
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

        String outputPath = ExportWord.chooseSaveDocxPath(this, buildAssuranceFileName());
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

        dataMap.putAll(thongTinChungData);

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
            // CardLayout.show() không trigger setVisible() nên phải gọi refresh thủ công
            if ("tab9".equals(cardId) && tab9 != null) {
                tab9.refreshData();
            }
        });

        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(2));
    }
}
