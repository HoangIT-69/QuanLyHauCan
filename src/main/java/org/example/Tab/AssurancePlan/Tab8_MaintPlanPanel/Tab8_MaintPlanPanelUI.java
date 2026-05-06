package org.example.Tab.AssurancePlan.Tab8_MaintPlanPanel;

import org.example.Utils.AssurancePlanUiUtils;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab8_MaintPlanPanelUI extends JPanel {

    private final Tab8_MaintPlanPanelService service;

    private JTextArea txtBdChuanBi, txtBdChienDau, txtBdSau;
    private DefaultTableModel modelSuaChua;
    private JTable tblSuaChua;
    private JTextArea txtScChuanBi, txtScChienDau, txtScSau;

    private boolean isCalculating = false;
    private int currentSessionId = -1;

    // 12 Dòng cố định theo mẫu
    private final String[][] FIXED_ROWS = {
            {"1", "SMPK 12,7mm"},
            {"2", "Cối 100mm"},
            {"3", "Cối 82mm"},
            {"4", "Cối 60mm"},
            {"5", "Súng SPG-9"},
            {"6", "Súng B41"},
            {"7", "Súng đại liên"},
            {"8", "Súng trung liên"},
            {"9", "Súng tiểu liên"},
            {"10", "Súng ngắn"}
    };

    public Tab8_MaintPlanPanelUI() {
        this(new Tab8_MaintPlanPanelService());
    }

    public Tab8_MaintPlanPanelUI(Tab8_MaintPlanPanelService service) {
        this.service = service != null ? service : new Tab8_MaintPlanPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("VIII. BẢO DƯỠNG, SỬA CHỮA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Bảo dưỡng
        mainContainer.add(UIUtils.createSectionLabel("1. Bảo dưỡng kỹ thuật"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdChuanBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtBdChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtBdChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdSau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtBdSau, 80));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Sửa chữa
        mainContainer.add(UIUtils.createSectionLabel("2. Sửa chữa"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createSuaChuaTable());
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("b) Biện pháp:"));
        mainContainer.add(Box.createVerticalStrut(10));

        txtScChuanBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtScChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtScChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtScChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtScSau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtScSau, 80));

        add(AssurancePlanUiUtils.wrapVerticalScroll(mainContainer), BorderLayout.CENTER);
    }

    private JPanel createSuaChuaTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1100, 480));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(UIUtils.createSubSectionLabel("a) Chỉ tiêu"), BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createStyledButton("🔄 Làm mới dữ liệu DB", new Color(46, 204, 113));
        btnRefresh.addActionListener(e -> {
            if (currentSessionId > 0) {
                loadDataFromDatabase(currentSessionId);
                JOptionPane.showMessageDialog(this,
                        "Đã quét Database!",
                        "Trạng thái Database", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Chưa có phiên làm việc hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });
        topPanel.add(btnRefresh, BorderLayout.EAST);
        pnl.add(topPanel, BorderLayout.NORTH);

        int[] w = {40, 150, 90, 80, 50, 50, 50, 50, 60, 50, 50, 60, 50, 50, 50, 50, 60};
        String[] cols = new String[17]; for (int i=0; i<17; i++) cols[i] = "";

        modelSuaChua = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row < 2) return false;
                // Chỉ cho sửa Số lượng (2), Tỷ lệ (3), và Khả năng sửa chữa Nhẹ/Vừa (9, 10)
                return column == 2 || column == 3 || column == 9 || column == 10;
            }
        };

        for (String[] r : FIXED_ROWS) {
            modelSuaChua.addRow(new Object[]{r[0], r[1], "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});
        }

        modelSuaChua.addTableModelListener(e -> {
            if (isCalculating) return;
            if (e.getType() == TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if ((col >= 2 && col <= 7) || col == 9 || col == 10) {
                    isCalculating = true;
                    recalculateRow(e.getFirstRow());
                    isCalculating = false;
                }
            }
        });

        tblSuaChua = new JTable(modelSuaChua);
        tblSuaChua.setRowHeight(35);
        tblSuaChua.setTableHeader(null);
        tblSuaChua.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupModernTableStyle(tblSuaChua, w);

        JPanel headerPanel = createSuaChuaHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(tblSuaChua);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(AssurancePlanUiUtils.SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 60)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AssurancePlanUiUtils.SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);

        return pnl;
    }

    private void recalculateRow(int row) {
        try {
            int soLuong = parseSafeInt(modelSuaChua.getValueAt(row, 2));
            double tiLe = parseSafeDouble(modelSuaChua.getValueAt(row, 3));

            int tongHuHong = (int) Math.round(soLuong * tiLe / 100.0);
            modelSuaChua.setValueAt(tongHuHong > 0 ? String.valueOf(tongHuHong) : "0", row, 8);

            // Tự động tính Nhẹ (50%), Vừa (25%), Nặng (15%), Hủy (10%)
            int huNhe = (int) Math.round(tongHuHong * 0.50);
            int huVua = (int) Math.round(tongHuHong * 0.25);
            int huNang = (int) Math.round(tongHuHong * 0.15);
            int huHuy = tongHuHong - huNhe - huVua - huNang; // Khớp tổng

            modelSuaChua.setValueAt(huNhe > 0 ? String.valueOf(huNhe) : "0", row, 4);
            modelSuaChua.setValueAt(huVua > 0 ? String.valueOf(huVua) : "0", row, 5);
            modelSuaChua.setValueAt(huNang > 0 ? String.valueOf(huNang) : "0", row, 6);
            modelSuaChua.setValueAt(huHuy > 0 ? String.valueOf(huHuy) : "0", row, 7);

            int suaNhe = parseSafeInt(modelSuaChua.getValueAt(row, 9));
            int suaVua = parseSafeInt(modelSuaChua.getValueAt(row, 10));

            int tongSuaChua = suaNhe + suaVua;
            modelSuaChua.setValueAt(tongSuaChua > 0 ? String.valueOf(tongSuaChua) : "0", row, 11);

            int conNhe = Math.max(0, huNhe - suaNhe);
            int conVua = Math.max(0, huVua - suaVua);
            int conNang = huNang;
            int conHuy = huHuy;

            // ĐÃ SỬA LẠI LOGIC: TỔNG CÒN LẠI = TỔNG HỎNG - TỔNG SỬA CHỮA
            int tongConLai = Math.max(0, tongHuHong - tongSuaChua);

            modelSuaChua.setValueAt(conNhe > 0 ? String.valueOf(conNhe) : "0", row, 12);
            modelSuaChua.setValueAt(conVua > 0 ? String.valueOf(conVua) : "0", row, 13);
            modelSuaChua.setValueAt(conNang > 0 ? String.valueOf(conNang) : "0", row, 14);
            modelSuaChua.setValueAt(conHuy > 0 ? String.valueOf(conHuy) : "0", row, 15);

            modelSuaChua.setValueAt(tongConLai > 0 ? String.valueOf(tongConLai) : "0", row, 16);

        } catch (Exception ignored) {}
    }

    private int parseSafeInt(Object obj) {
        if (obj == null || obj.toString().trim().isEmpty()) return 0;
        try { return Integer.parseInt(obj.toString().trim()); } catch (Exception e) { return 0; }
    }

    private double parseSafeDouble(Object obj) {
        if (obj == null || obj.toString().trim().isEmpty()) return 0;
        try { return Double.parseDouble(obj.toString().trim().replace(",", ".")); } catch (Exception e) { return 0; }
    }

    private JPanel createSuaChuaHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 60));
        int[] x = new int[18]; x[0]=0; for(int i=0; i<17; i++) x[i+1] = x[i]+w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, w[0], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Loại TBKT", x[1], 0, w[1], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Số lượng<br>TBKT<br>tham gia CĐ</center></html>", x[2], 0, w[2], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Dự kiến<br>tỉ lệ hư hỏng<br>1 ngày đêm (%)</center></html>", x[3], 0, w[3], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng số TBKT hư hỏng", x[4], 0, w[4]*4+w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Khả năng sửa chữa<br>trong 1 ngày đêm</center></html>", x[9], 0, w[9]*2+w[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Còn lại chưa sửa chữa được", x[12], 0, w[12]*4+w[16], 30));

        String[] subCols = {"Nhẹ", "Vừa", "Nặng", "Hủy", "+", "Nhẹ", "Vừa", "+", "Nhẹ", "Vừa", "Nặng", "Hủy", "+"};
        for (int i = 0; i < subCols.length; i++) {
            p.add(UIUtils.createAbsoluteHeaderLabel(subCols[i], x[4 + i], 30, w[4 + i], 30));
        }
        return p;
    }

    private void setupModernTableStyle(JTable table, int[] w) {
        Color gridColor = new Color(226, 232, 240);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 16 ? 0 : 1, gridColor));

                if (column == 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                if (row < 2) {
                    setFont(new Font("Times New Roman", Font.BOLD, 15));
                    c.setBackground(new Color(248, 250, 252));
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    if (column < 4 || column == 8 || column == 11 || column >= 12) c.setBackground(new Color(250, 252, 255));
                    else if (isSelected) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        };

        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        isCalculating = true;
        try {
            service.loadFromDatabase(sessionId, modelSuaChua, this);
        } finally {
            isCalculating = false;
            int rowCount = modelSuaChua.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                recalculateRow(i);
            }
        }
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();

        data.put("{{bao_duong_kt_chuan_bi}}", txtBdChuanBi.getText().trim());
        data.put("{{bao_duong_kt_chien_dau}}", txtBdChienDau.getText().trim());
        data.put("{{bao_duong_kt_sau}}", txtBdSau.getText().trim());

        data.put("{{sua_chua_chuan_bi}}", txtScChuanBi.getText().trim());
        data.put("{{sua_chua_chien_dau}}", txtScChienDau.getText().trim());
        data.put("{{sua_chua_sau}}", txtScSau.getText().trim());

        String[] colKeys = {"sl", "tl", "hn", "hv", "hna", "hh", "ht", "sn", "sv", "st", "cn", "cv", "cna", "ch", "ct"};

        int rowCount = modelSuaChua.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int legacyIndex = i + 1;
            int templateIndex = i + 3;
            for (int c = 2; c <= 16; c++) {
                String value = getVal(i, c);
                String keySuffix = colKeys[c - 2];
                data.put("{{sc_" + keySuffix + "_" + legacyIndex + "}}", value);
                data.put("{{sc_" + keySuffix + "_" + templateIndex + "}}", value);
            }
        }
        return data;
    }

    private String getVal(int row, int col) {
        Object v = modelSuaChua.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }
}