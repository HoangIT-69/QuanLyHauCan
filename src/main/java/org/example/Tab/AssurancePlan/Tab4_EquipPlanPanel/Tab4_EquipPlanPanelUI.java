package org.example.Tab.AssurancePlan.Tab4_EquipPlanPanel;

import org.example.Utils.AssurancePlanUiUtils;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab4_EquipPlanPanelUI extends JPanel {

    private final Tab4_EquipPlanPanelService service;

    private final int[] colWidths = {120, 160, 60, 70, 70, 70, 60, 60, 100, 80, 110, 110, 110};

    private DefaultTableModel model;
    private JTable table;

    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;
    private JTextArea txtSauChienDau;

    private int currentSessionId = -1;

    public Tab4_EquipPlanPanelUI() {
        this(new Tab4_EquipPlanPanelService());
    }

    public Tab4_EquipPlanPanelUI(Tab4_EquipPlanPanelService service) {
        this.service = service != null ? service : new Tab4_EquipPlanPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("IV. BẢO ĐẢM VŨ KHÍ TRANG BỊ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Chỉ tiêu (Bảng 55 dòng)
        mainContainer.add(UIUtils.createSectionLabel("1. Chỉ tiêu"));
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTablePanel());
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Ý định, biện pháp
        mainContainer.add(UIUtils.createSectionLabel("2.biện pháp bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(10));

        mainContainer.add(UIUtils.createSubSectionLabel("Giai đoạn chuẩn bị:"));
        txtChuanBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("Giai đoạn chiến đấu:"));
        txtChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("Sau chiến đấu:"));
        txtSauChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtSauChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(20));

        add(AssurancePlanUiUtils.wrapVerticalScroll(mainContainer), BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(1100, 500));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("🔄 Làm mới dữ liệu DB", new Color(46, 204, 113));
        topPanel.add(btnRefresh, BorderLayout.EAST);
        pnl.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = new String[13]; for(int i=0; i<13; i++) columnNames[i]="";

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column > 2; } // Khóa 3 cột đầu
        };

        // KHỞI TẠO 55 DÒNG CỐ ĐỊNH (5 nhóm x 11 vũ khí)
        for (int i = 0; i < Tab4_EquipPlanPanelService.GROUPS.length; i++) {
            for (int j = 0; j < Tab4_EquipPlanPanelService.WEAPONS.length; j++) {
                String groupName = (j == 0) ? Tab4_EquipPlanPanelService.GROUPS[i] : "";
                model.addRow(new Object[]{
                        groupName, Tab4_EquipPlanPanelService.WEAPONS[j], Tab4_EquipPlanPanelService.UNITS[j], "", "", "", "", "", "", "", "", "", ""
                });
            }
        }

        table = new JTable(model);
        table.setRowHeight(35);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupCustomRenderers();

        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        headerViewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(tableScroll);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel combinedTablePanel = new JPanel(new BorderLayout());
        combinedTablePanel.setBorder(new LineBorder(new Color(203, 213, 225), 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 60)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combinedTablePanel.add(headerWrapper, BorderLayout.NORTH);
        combinedTablePanel.add(tableScroll, BorderLayout.CENTER);
        pnl.add(combinedTablePanel, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            if (this.currentSessionId > 0) loadDataFromDatabase(this.currentSessionId);
            else JOptionPane.showMessageDialog(this, "Chưa có phiên làm việc hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        });

        return pnl;
    }
    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        service.loadFromDatabase(sessionId, model);
    }

    // =====================================================================
    // UI RENDERERS VÀ HEADER
    // =====================================================================
    private void setupCustomRenderers() {
        Color gridColor = new Color(203, 213, 225);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                boolean isMerged = (column == 0) && (value == null || value.toString().isEmpty());
                int topBorder = isMerged ? 0 : 1;
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(topBorder, 0, 1, 1, gridColor));

                if (column <= 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                if (column == 0 && !isMerged) {
                    setFont(new Font("Times New Roman", Font.BOLD, 15));
                    c.setBackground(new Color(248, 250, 252));
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 14));
                    if (isSelected && column > 0) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        });

        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
        }
    }

    private JPanel createAbsoluteHeader() {
        int totalWidth = 0; for (int w : colWidths) totalWidth += w;
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249)); p.setPreferredSize(new Dimension(totalWidth, 60));
        int[] x = new int[colWidths.length + 1]; x[0] = 0; for (int i = 0; i < colWidths.length; i++) x[i + 1] = x[i] + colWidths[i];

        p.add(createHeaderLabel("Đơn vị", x[0], 0, colWidths[0], 60));
        p.add(createHeaderLabel("Tên TBKT", x[1], 0, colWidths[1], 60));
        p.add(createHeaderLabel("ĐVT", x[2], 0, colWidths[2], 60));
        p.add(createHeaderLabel("Nhu cầu", x[3], 0, colWidths[3], 60));

        p.add(createHeaderLabel("Hiện có", x[4], 0, x[8] - x[4], 30));
        p.add(createHeaderLabel("<html><center>Phải có<br>trước CĐ</center></html>", x[8], 0, colWidths[8], 60));
        p.add(createHeaderLabel("Phải bổ sung", x[9], 0, x[13] - x[9], 30));

        p.add(createHeaderLabel("Tổng số", x[4], 30, colWidths[4], 30));
        p.add(createHeaderLabel("Số tốt", x[5], 30, colWidths[5], 30));
        p.add(createHeaderLabel("Kbđ", x[6], 30, colWidths[6], 30));
        p.add(createHeaderLabel("Kt", x[7], 30, colWidths[7], 30));

        p.add(createHeaderLabel("Số lượng", x[9], 30, colWidths[9], 30));
        p.add(createHeaderLabel("Thời gian", x[10], 30, colWidths[10], 30));
        p.add(createHeaderLabel("Địa điểm", x[11], 30, colWidths[11], 30));
        p.add(createHeaderLabel("Phương thức", x[12], 30, colWidths[12], 30));

        return p;
    }

    private JLabel createHeaderLabel(String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setOpaque(true); label.setBackground(new Color(241, 245, 249));
        label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(203, 213, 225)));
        label.setBounds(x, y, w, h);
        return label;
    }

    // =========================================================================
    // XUẤT TỪNG KEYWORD RA WORD
    // =========================================================================
    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();

        // Loop xuất 55 dòng (i từ 0 đến 54)
        for (int i = 0; i < 55; i++) {
            int r = i + 1; // 1 -> 55
            data.put("<<tbkt_nhu_cau_" + r + ">>", getVal(i, 3));
            data.put("<<tbkt_hc_tong_" + r + ">>", getVal(i, 4));
            data.put("<<tbkt_hc_tot_" + r + ">>", getVal(i, 5));
            data.put("<<tbkt_kbd_" + r + ">>", getVal(i, 6));
            data.put("<<tbkt_kt_" + r + ">>", getVal(i, 7));
            data.put("<<tbkt_phai_co_" + r + ">>", getVal(i, 8));
            data.put("<<tbkt_bs_sl_" + r + ">>", getVal(i, 9));
            data.put("<<tbkt_bs_tg_" + r + ">>", getVal(i, 10));
            data.put("<<tbkt_bs_dd_" + r + ">>", getVal(i, 11));
            data.put("<<tbkt_bs_pt_" + r + ">>", getVal(i, 12));
        }

        data.put("<<tbkt_chuan_bi>>", txtChuanBi.getText().trim());
        data.put("<<tbkt_chien_dau>>", txtChienDau.getText().trim());
        data.put("<<tbkt_sau_chien_dau>>", txtSauChienDau.getText().trim());

        return data;
    }

    private String getVal(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }
}