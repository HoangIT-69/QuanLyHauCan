package org.example.Tab.PlanEstimation.Tab8_MaintenancePanel;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab8_MaintenancePanelUI extends JPanel {

    private final Tab8_MaintenancePanelService service = new Tab8_MaintenancePanelService();
    private int currentSessionId = -1;

    private JTextArea txtBDChuanBi;
    private JTextArea txtBDChienDau;
    private JTextArea txtBDSauChienDau;
    private JTextArea txtCanDoi;
    private JTextArea txtSuaChuaChuanBi;
    private JTextArea txtSuaChuaChienDau;
    private JTextArea txtSuaChuaSauChienDau;

    private DefaultTableModel model;
    private JTable table;

    public Tab8_MaintenancePanelUI(int sessionId) {
        this();
        this.currentSessionId = sessionId;
        loadDataFromDatabase(sessionId);
    }

    public Tab8_MaintenancePanelUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("VIII. BẢO DƯỠNG, SỬA CHỮA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("1. Bảo dưỡng kỹ thuật"));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị:"));
        txtBDChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDChuanBi, 100));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtBDChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDChienDau, 100));

        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtBDSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDSauChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(30));

        mainContainer.add(UIUtils.createSectionLabel("2. Sửa chữa"));
        mainContainer.add(UIUtils.createSubSectionLabel("a) Dự kiến tỉ lệ vũ khí trang bị hư hỏng"));
        mainContainer.add(createTablePanel());
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("* Cân đối:"));
        txtCanDoi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtCanDoi, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        mainContainer.add(UIUtils.createSubSectionLabel("b) Ý định bảo đảm"));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị:"));
        txtSuaChuaChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaChuanBi, 120));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtSuaChuaChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaChienDau, 120));

        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtSuaChuaSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaSauChienDau, 120));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    /**
     * Nạp số lượng vũ khí và tỉ lệ hư hỏng đã khai báo từ DB vào bảng 2a (tự động, không nhập tay).
     */
    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        Map<String, Integer> sums = (sessionId > 0)
                ? service.fetchWeaponSums(sessionId)
                : service.fetchWeaponSumsFromSharedStep2Store();
        Map<String, Double> savedRates = service.fetchSavedRates(sessionId);

        String[][] weapons = {
            {"1",  "Súng ngắn",       "sung_ngan"},
            {"2",  "Súng Tiểu liên",  "tieu_lien"},
            {"3",  "Súng Trung liên", "trung_lien"},
            {"4",  "Súng Đại liên",  "dai_lien"},
            {"5",  "B41",              "b41"},
            {"6",  "Cối 60mm",        "co60mm"},
            {"7",  "Cối 82mm",        "co82mm"},
            {"8",  "Cối 100mm",       "co100mm"},
            {"9",  "SPG-9",            "spg9"},
            {"10", "SMPK 12,7",       "smpk_127mm"}
        };

        model.setRowCount(0);
        for (String[] w : weapons) {
            int soLuong = sums.getOrDefault(w[2], 0);
            double tiLe = savedRates.getOrDefault(w[1].trim().toLowerCase(), 0.0);
            String strRate = (tiLe == (long) tiLe) ? String.valueOf((long) tiLe) : String.valueOf(tiLe);
            int huHong = (int) Math.ceil((soLuong * tiLe) / 100.0);
            model.addRow(new Object[]{w[0], w[1], soLuong, strRate, String.valueOf(huHong)});
        }
    }

    public Map<String, String> getMaintenanceTextData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<bd_kt_cb>>", txtBDChuanBi.getText().trim());
        data.put("<<bd_kt_cd>>", txtBDChienDau.getText().trim());
        data.put("<<bd_kt_sau>>", txtBDSauChienDau.getText().trim());
        data.put("<<can_doi_sc>>", txtCanDoi.getText().trim());
        data.put("<<ydinh_sc_cb>>", txtSuaChuaChuanBi.getText().trim());
        data.put("<<ydinh_sc_cd>>", txtSuaChuaChienDau.getText().trim());
        data.put("<<ydinh_sc_sau>>", txtSuaChuaSauChienDau.getText().trim());
        return data;
    }

    public List<Object[]> getTableData() {
        List<Object[]> tableData = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[model.getColumnCount()];
            for (int j = 0; j < model.getColumnCount(); j++) {
                row[j] = model.getValueAt(i, j);
            }
            tableData.add(row);
        }
        return tableData;
    }

    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 310));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));

        String[] cols = {"STT", "Loại vũ khí", "Số lượng", "Tỉ lệ hư hỏng (%)", "Số vũ khí hư hỏng"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        UIUtils.makeScrollPassThrough(scroll);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }
}
