package org.example.Panel;

import org.example.Tab.PlanEstimation.*;
import org.example.Utils.DBConnection;
import org.example.Utils.ExportWord;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class PlanEstimationPanel extends JPanel {

    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton currentSelectedBtn = null;
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
    private Tab12_ConclusionPanel tab12;

    public PlanEstimationPanel(int sessionId) {
        this.sessionId = sessionId;
        setLayout(new BorderLayout(15, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String titleText = fetchTenVanKien(sessionId);

        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        initTabs();
        add(createFooterPanel(titleText), BorderLayout.SOUTH);

        if (menuPanel.getComponentCount() > 0) {
            ((JButton) menuPanel.getComponent(0)).doClick();
        }
    }

    private void initTabs() {
        bodyPanelSetup();

        tab1 = new Tab1_EvaluationPanel(); addTab("I. Đánh giá tình hình", "tab1", tab1);
        tab2 = new Tab2_MissionPanel(); addTab("II. Nhiệm vụ", "tab2", tab2);
        tab3 = new Tab3_OrganizationPanel(); addTab("III. Tổ chức, Bố trí", "tab3", tab3);
        tab4 = new Tab4_EquipmentPanel(); addTab("IV. Trang bị kỹ thuật", "tab4", tab4);
        tab5 = new Tab5_MaterialPanel(); addTab("V. Đạn, Vật chất", "tab5", tab5);
        tab6 = new Tab6_LivingPanel(); addTab("VI. Sinh hoạt", "tab6", tab6);
        tab7 = new Tab7_MedicalPanel(sessionId); addTab("VII. Quân y", "tab7", tab7);
        tab8 = new Tab8_MaintenancePanel(); addTab("VIII. Bảo dưỡng, Sửa chữa", "tab8", tab8);
        tab9 = new Tab9_TransportPanel(); addTab("IX. Công tác Vận tải", "tab9", tab9);
        tab10 = new Tab10_ProtectionPanel(); addTab("X. Bảo vệ HC-KT", "tab10", tab10);
        tab11 = new Tab11_CommandPanel(sessionId); addTab("XI. Chỉ huy HC-KT", "tab11", tab11);
        tab12 = new Tab12_ConclusionPanel(); addTab("XII. Kết luận & Đề nghị", "tab12", tab12);
    }

    private JPanel createFooterPanel(String titleText) {
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setOpaque(false);

        JButton btnSave = UIUtils.createStyledButton("Lưu Bản Nháp", new Color(34, 197, 94));
        JButton btnExport = UIUtils.createStyledButton("Xuất File Word", new Color(41, 128, 185));

        btnExport.addActionListener(e -> performExport(titleText));

        pnlFooter.add(btnSave);
        pnlFooter.add(btnExport);
        return pnlFooter;
    }

    private void performExport(String titleText) {
        Map<String, String> dataMap = collectExportData(titleText);

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

        String outputPath = ExportWord.chooseSaveDocxPath(
                this,
                "Ket_Qua_Export_Session_" + sessionId + ".docx"
        );

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

    private Map<String, String> collectExportData(String titleText) {
        Map<String, String> dataMap = new HashMap<>();

        dataMap.put("<<ten_van_kien>>", titleText);
        dataMap.put("<<danh_gia_tinh_hinh>>", tab1.getDanhGia());
        dataMap.put("<<nhiem_vu>>", tab2.getNhiemVu());
        dataMap.put("<<to_chuc_luc_luong>>", tab3.getToChuc());
        dataMap.put("<<bo_tri_hckt>>", tab3.getBoTri());
        dataMap.put("<<chi_tieu_kt>>", tab4.getChiTieu());
        dataMap.put("<<tiep_nhan_chuan_bi>>", tab4.getChuanBi());
        dataMap.put("<<tiep_nhan_chien_dau>>", tab4.getChienDau());

        dataMap.put("<<ydinh_chuanbi_v5>>", tab5.getTxtChuanBi());
        dataMap.put("<<ydinh_chiendau_v5>>", tab5.getTxtChienDau());
        dataMap.put("<<ydinh_sau_v5>>", tab5.getTxtSauChienDau());
        putTab5Table1(dataMap);
        putTab5Table2(dataMap);

        dataMap.put("<<bd_an_uong>>", tab6.getAnUong());
        dataMap.put("<<bd_mac>>", tab6.getMac());
        dataMap.put("<<bd_o_ngunghi>>", tab6.getONguNghi());

        dataMap.putAll(tab7.getMedicalData());

        dataMap.putAll(tab8.getMaintenanceTextData());
        putTab8Table(dataMap);

        dataMap.putAll(tab9.getTransportData());
        dataMap.putAll(tab10.getProtectionData());
        dataMap.putAll(tab11.getCommandData());
        dataMap.putAll(tab12.getConclusionData());

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

    private String fetchTenVanKien(int sessionId) {
        String sql = "SELECT ten_van_kien FROM step1_thong_tin WHERE session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fetched = rs.getString("ten_van_kien");
                if (fetched != null && !fetched.isEmpty()) {
                    return fetched.toUpperCase().startsWith("DỰ KIẾN")
                            ? fetched.toUpperCase()
                            : "DỰ KIẾN " + fetched.toUpperCase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "DỰ KIẾN KẾ HOẠCH BẢO ĐẢM HẬU CẦN, KỸ THUẬT";
    }

    private String getCell(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return "";
        }
        return String.valueOf(row[index]).trim();
    }

    private void putTab5Table1(Map<String, String> dataMap) {
        putRows(
                tab5.getTable1Data(),
                10,
                dataMap,
                new String[]{"<<lvcv5_%d>>", "<<dvtv5_%d>>", "<<qddtv5_%d>>", "<<hcv5_%d>>", "<<pbsv5_%d>>"}
        );
    }

    private void putTab5Table2(Map<String, String> dataMap) {
        putRows(
                tab5.getTable2Data(),
                10,
                dataMap,
                new String[]{"<<ttv52_%d>>", "<<lvcv52_%d>>", "<<dvtv52_%d>>", "<<kd04_%d>>", "<<dv04_%d>>", "<<t04_%d>>", "<<kdsau_%d>>", "<<dvsau_%d>>", "<<tsau_%d>>"}
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

    public Tab1_EvaluationPanel getTab1() { return tab1; }
    public Tab2_MissionPanel getTab2() { return tab2; }
    public Tab6_LivingPanel getTab6() { return tab6; }
    public Tab10_ProtectionPanel getTab10() { return tab10; }
    public Tab11_CommandPanel getTab11() { return tab11; }
}