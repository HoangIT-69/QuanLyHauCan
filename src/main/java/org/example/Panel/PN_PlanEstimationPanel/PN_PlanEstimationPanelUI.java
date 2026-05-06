package org.example.Panel.PN_PlanEstimationPanel;

import org.example.Tab.PlanEstimation.*;
import org.example.Tab.PlanEstimation.Tab10_ProtectionPanelService;
import org.example.Tab.PlanEstimation.Tab10_ProtectionPanelUI;
import org.example.Tab.PlanEstimation.Tab11_CommandPanelService;
import org.example.Tab.PlanEstimation.Tab11_CommandPanelUI;
import org.example.Tab.PlanEstimation.Tab12_ConclusionPanelService;
import org.example.Tab.PlanEstimation.Tab12_ConclusionPanelUI;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel.Tab1_EvaluationPanelService;
import org.example.Tab.PlanEstimation.Tab1_EvaluationPanel.Tab1_EvaluationPanelUI;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel.Tab2_MissionPanelService;
import org.example.Tab.PlanEstimation.Tab2_MissionPanel.Tab2_MissionPanelUI;
import org.example.Tab.PlanEstimation.Tab3_OrganizationPanel.Tab3_OrganizationPanelService;
import org.example.Tab.PlanEstimation.Tab3_OrganizationPanel.Tab3_OrganizationPanelUI;
import org.example.Tab.PlanEstimation.Tab4_EquipmentPanel.Tab4_EquipmentPanelService;
import org.example.Tab.PlanEstimation.Tab4_EquipmentPanel.Tab4_EquipmentPanelUI;
import org.example.Tab.PlanEstimation.Tab5_MaterialPanel.Tab5_MaterialPanelService;
import org.example.Tab.PlanEstimation.Tab5_MaterialPanel.Tab5_MaterialPanelUI;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel.Tab6_LivingPanelService;
import org.example.Tab.PlanEstimation.Tab6_LivingPanel.Tab6_LivingPanelUI;
import org.example.Tab.PlanEstimation.Tab7_MedicalPanel.Tab7_MedicalPanelService;
import org.example.Tab.PlanEstimation.Tab7_MedicalPanel.Tab7_MedicalPanelUI;
import org.example.Tab.PlanEstimation.Tab8_MaintenancePanel.Tab8_MaintenancePanelUI;
import org.example.Tab.PlanEstimation.Tab9_TransportPanel.Tab9_TransportPanelService;
import org.example.Tab.PlanEstimation.Tab9_TransportPanel.Tab9_TransportPanelUI;
import org.example.Utils.ExportWord;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Container "Dự kiến kế hoạch" dành cho tập bài Phòng ngự (Tab I–XII).
 */
public class PN_PlanEstimationPanelUI extends JPanel {

    private final PN_PlanEstimationPanelService panelService = new PN_PlanEstimationPanelService();
    private final int sessionId;

    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton currentSelectedBtn = null;
    private JLabel lblTitle;

    private Tab1_EvaluationPanelUI tab1;
    private Tab2_MissionPanelUI tab2;
    private Tab3_OrganizationPanelUI tab3;
    private Tab4_EquipmentPanelUI tab4;
    private Tab5_MaterialPanelUI tab5;
    private Tab6_LivingPanelUI tab6;
    private Tab7_MedicalPanelUI tab7;
    private Tab8_MaintenancePanelUI tab8;
    private Tab9_TransportPanelUI tab9;
    private Tab10_ProtectionPanelUI tab10;
    private Tab11_CommandPanelUI tab11;
    private Tab12_ConclusionPanelUI tab12;

    public PN_PlanEstimationPanelUI(int sessionId) {
        this.sessionId = sessionId;
        setLayout(new BorderLayout(15, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String chiHuy = org.example.Utils.AppSession.getFullName();

        lblTitle = new JLabel(panelService.fetchTenVanKien(sessionId), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        Tab1_EvaluationPanelService tab1Svc = new Tab1_EvaluationPanelService();
        Tab2_MissionPanelService tab2Svc = new Tab2_MissionPanelService();

        initTabs(tab1Svc, tab2Svc);
        add(createFooterPanel(chiHuy), BorderLayout.SOUTH);

        if (menuPanel.getComponentCount() > 0) {
            ((JButton) menuPanel.getComponent(0)).doClick();
        }
    }

    private void initTabs(Tab1_EvaluationPanelService tab1Svc, Tab2_MissionPanelService tab2Svc) {
        bodyPanelSetup();

        tab1 = new Tab1_EvaluationPanelUI(sessionId, tab1Svc);
        addTab("I. Đánh giá tình hình", "tab1", tab1);
        tab2 = new Tab2_MissionPanelUI(sessionId, tab2Svc);
        addTab("II. Nhiệm vụ", "tab2", tab2);
        tab3 = new Tab3_OrganizationPanelUI(sessionId, new Tab3_OrganizationPanelService());
        addTab("III. Tổ chức sử dụng lực lượng, bố trí hậu cần kĩ thuật", "tab3", tab3);
        tab4 = new Tab4_EquipmentPanelUI(sessionId, new Tab4_EquipmentPanelService());
        addTab("IV. Bảo đảm vũ khí trang bị kỹ thuật", "tab4", tab4);
        tab5 = new Tab5_MaterialPanelUI(sessionId, new Tab5_MaterialPanelService());
        addTab("V. Bảo đảm đạn, VCHC, VTKT", "tab5", tab5);
        tab6 = new Tab6_LivingPanelUI(sessionId, new Tab6_LivingPanelService());
        addTab("VI. Bảo đảm Sinh hoạt", "tab6", tab6);
        tab7 = new Tab7_MedicalPanelUI(sessionId, new Tab7_MedicalPanelService());
        addTab("VII. Bảo đảm Quân y", "tab7", tab7);
        tab8 = new Tab8_MaintenancePanelUI(sessionId);
        addTab("VIII. Bảo dưỡng, Sửa chữa", "tab8", tab8);
        tab9 = new Tab9_TransportPanelUI(sessionId, new Tab9_TransportPanelService());
        addTab("IX. Công tác Vận tải", "tab9", tab9);
        tab10 = new Tab10_ProtectionPanelUI(sessionId, new Tab10_ProtectionPanelService());
        addTab("X. Bảo vệ HC-KT", "tab10", tab10);
        tab11 = new Tab11_CommandPanelUI(sessionId, new Tab11_CommandPanelService());
        addTab("XI. Chỉ huy HC-KT", "tab11", tab11);
        tab12 = new Tab12_ConclusionPanelUI(sessionId, new Tab12_ConclusionPanelService());
        addTab("*Kết luận & Đề nghị", "tab12", tab12);
    }

    private JPanel createFooterPanel(String chiHuy) {
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setOpaque(false);

        JButton btnSave = UIUtils.createStyledButton("Lưu Bản Nháp", new Color(34, 197, 94));
        JButton btnExport = UIUtils.createStyledButton("Xuất File Word", new Color(41, 128, 185));

        btnSave.addActionListener(e -> {
            tab1.persistToDatabase();
            tab2.persistToDatabase();
            tab3.persistToDatabase();
            tab4.persistToDatabase();
            tab5.persistToDatabase();
            tab6.persistToDatabase();
            tab8.persistToDatabase();
            tab10.persistToDatabase();
            tab11.persistToDatabase();
            tab12.persistToDatabase();
            JOptionPane.showMessageDialog(this,
                    "Đã lưu bản nháp Tab I–VI, VIII và X–XII (các tab có lưu nháp vào pn_plan_estimation).",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnExport.addActionListener(e -> performExport(chiHuy));

        pnlFooter.add(btnSave);
        pnlFooter.add(btnExport);
        return pnlFooter;
    }

    private void performExport(String chiHuy) {
        Map<String, String> dataMap = collectExportData();

        java.io.InputStream templateStream = getClass().getResourceAsStream("/docs/template-PN_DKKH.docx");
        if (templateStream == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: Không tìm thấy file mẫu tại src/main/resources/docs/template-PN_DKKH.docx",
                    "Lỗi Hệ Thống",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String namePart = (chiHuy != null && !chiHuy.isBlank())
                ? chiHuy.replaceAll("[\\\\/:*?\"<>|]", "").replace(" ", "_")
                : "Session_" + sessionId;
        String defaultFileName = "Du_kien_KH_cua_" + namePart + ".docx";

        String outputPath = ExportWord.chooseSaveDocxPath(this, defaultFileName);

        if (outputPath == null) {
            return;
        }

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
        String titleText = panelService.fetchTenVanKien(sessionId);

        safePut(dataMap, "header", () -> {
            dataMap.put("<<ten_van_kien>>", nz(titleText));
            dataMap.put("<<danh_gia_tinh_hinh>>", nz(tab1.getDanhGia()));
            dataMap.put("<<nhiem_vu>>", nz(tab2.getNhiemVu()));
        });
        safePut(dataMap, "tab3", () -> dataMap.putAll(tab3.getExportData()));
        safePut(dataMap, "tab4", () -> {
            dataMap.put("<<chi_tieu_kt>>", nz(tab4.getChiTieu()));
            dataMap.put("<<tiep_nhan_chuan_bi>>", nz(tab4.getChuanBi()));
            dataMap.put("<<tiep_nhan_chien_dau>>", nz(tab4.getChienDau()));
        });
        safePut(dataMap, "tab5_text", () -> {
            dataMap.put("<<ydinh_chuanbi_v5>>", nz(tab5.getTxtChuanBi()));
            dataMap.put("<<ydinh_chiendau_v5>>", nz(tab5.getTxtChienDau()));
            dataMap.put("<<ydinh_sau_v5>>", nz(tab5.getTxtSauChienDau()));
        });
        safePut(dataMap, "tab5_table1", () -> putTab5Table1(dataMap));
        safePut(dataMap, "tab5_table2", () -> putTab5Table2(dataMap));

        safePut(dataMap, "tab6", () -> {
            dataMap.put("<<bd_an_uong>>", nz(tab6.getAnUong()));
            dataMap.put("<<bd_mac>>", nz(tab6.getMac()));
            dataMap.put("<<bd_o_ngunghi>>", nz(tab6.getONguNghi()));
        });
        safePut(dataMap, "tab7", () -> dataMap.putAll(tab7.getMedicalData()));
        safePut(dataMap, "tab8_text", () -> dataMap.putAll(tab8.getMaintenanceTextData()));
        safePut(dataMap, "tab8_table", () -> putTab8Table(dataMap));
        safePut(dataMap, "tab9", () -> {
            dataMap.putAll(tab9.getTransportData());
            ExportWord.putTab9TransportExport(dataMap, tab9);
        });
        safePut(dataMap, "tab10", () -> dataMap.putAll(tab10.getProtectionData()));
        safePut(dataMap, "tab11", () -> dataMap.putAll(tab11.getCommandData()));
        safePut(dataMap, "tab12", () -> dataMap.putAll(tab12.getConclusionData()));

        return dataMap;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    private void safePut(Map<String, String> dataMap, String section, Runnable block) {
        try {
            block.run();
        } catch (Exception ex) {
            System.err.println("PN export: section \"" + section + "\" — " + ex.getMessage());
            ex.printStackTrace();
        }
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
        menuPanel.setPreferredSize(new Dimension(260, 0));

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
        btn.setMaximumSize(new Dimension(240, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    private String getCell(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return "";
        }
        return String.valueOf(row[index]).trim();
    }

    private void putTab5Table1(Map<String, String> dataMap) {
        putRows(
                tab5.getMaterialService().buildTable1Rows(sessionId),
                10,
                dataMap,
                new String[]{"<<lvcv5_%d>>", "<<dvtv5_%d>>", "<<qddtv5_%d>>", "<<hcv5_%d>>", "<<pbsv5_%d>>"}
        );
    }

    /** Bảng 2 VCHC: 20 dòng × 9 cột (không cột phân cấp). */
    private void putTab5Table2(Map<String, String> dataMap) {
        putRows(
                tab5.getMaterialService().buildTable2Rows(sessionId),
                Tab5_MaterialPanelService.TABLE2_MAX_ROWS,
                dataMap,
                new String[]{
                        "<<ttv52_%d>>", "<<lvcv52_%d>>", "<<dvtv52_%d>>",
                        "<<kd04_%d>>", "<<dv04_%d>>", "<<t04_%d>>",
                        "<<kdsau_%d>>", "<<dvsau_%d>>", "<<tsau_%d>>"
                }
        );
    }

    private void putTab8Table(Map<String, String> dataMap) {
        putRows(
                tab8.getTableData(),
                10,
                dataMap,
                new String[]{"<<sttv8_%d>>", "<<lvk_v8_%d>>", "<<sl_v8_%d>>", "<<tl_v8_%d>>", "<<slh_v8_%d>>"}
        );
    }

    private void putRows(java.util.List<Object[]> rows, int maxRows, Map<String, String> dataMap, String[] keyPatterns) {
        for (int i = 1; i <= maxRows; i++) {
            Object[] row = (i <= rows.size()) ? rows.get(i - 1) : null;

            for (int col = 0; col < keyPatterns.length; col++) {
                String key = String.format(keyPatterns[col], i);
                String value = (row != null) ? getCell(row, col) : "";
                dataMap.put(key, value);
            }
        }
    }

    public Tab1_EvaluationPanelUI getTab1() {
        return tab1;
    }

    public Tab2_MissionPanelUI getTab2() {
        return tab2;
    }

    public Tab6_LivingPanelUI getTab6() {
        return tab6;
    }

    public Tab10_ProtectionPanelUI getTab10() {
        return tab10;
    }

    public Tab11_CommandPanelUI getTab11() {
        return tab11;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void onDeclarationDataChanged() {
        refreshHeaderFromDeclaration();
        refreshDataTablesOnly();
    }

    private void refreshHeaderFromDeclaration() {
        if (lblTitle == null) {
            return;
        }
        lblTitle.setText(panelService.fetchTenVanKien(sessionId));
    }

    /**
     * Chỉ làm mới dữ liệu ở các bảng/nhãn tự tính, không reset text area hay JComboBox.
     */
    public void refreshDataTablesOnly() {
        // Tab V — bảng vật chất
        if (tab5 != null) {
            tab5.refreshTable1();
            tab5.refreshTable2();
        }
        // Tab VII — dự kiến TBBB (tự tính từ dữ liệu khai báo)
        if (tab7 != null) {
            tab7.applyTbbbFromDb();
        }
        // Tab VIII — bảng vũ khí hư hỏng
        if (tab8 != null) {
            tab8.loadDataFromDatabase(sessionId);
        }
        // Tab IX — nhãn khối lượng vận chuyển tự tính
        if (tab9 != null) {
            tab9.reloadSnapshot();
        }
        // Tab XI — người chỉ huy/người thay thế (lấy từ Step 1)
        if (tab11 != null) {
            tab11.refreshCommanderFieldsFromDeclaration();
        }
    }
}
