package org.example.Tab.AssurancePlan.Tab7_MedPlanPanel;

import org.example.Utils.AssurancePlanUiUtils;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab7_MedPlanPanelUI extends JPanel {

    private final Tab7_MedPlanPanelService service;

    private DefaultTableModel modelQuanY;
    private JTable tblQuanY;
    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;
    private JTextArea txtVeSinh;

    private boolean isCalculating = false;

    public Tab7_MedPlanPanelUI() {
        this(new Tab7_MedPlanPanelService());
    }

    public Tab7_MedPlanPanelUI(Tab7_MedPlanPanelService service) {
        this.service = service != null ? service : new Tab7_MedPlanPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("VII. BẢO ĐẢM QUÂN Y");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Bảng Tỷ lệ TBBB
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tỷ lệ TBBB"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createQuanYTable());
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Ý định bảo đảm
        mainContainer.add(UIUtils.createSectionLabel("2. Ý định bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChuanBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChienDau = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(25));

        // 3. Vệ sinh phòng bệnh
        mainContainer.add(UIUtils.createSectionLabel("3. Vệ sinh phòng bệnh, phòng dịch"));
        txtVeSinh = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtVeSinh, 100));

        add(AssurancePlanUiUtils.wrapVerticalScroll(mainContainer), BorderLayout.CENTER);
    }

    private JPanel createQuanYTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1000, 720));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {220, 80, 60, 70, 60, 70, 60, 90, 90, 60};
        String[] cols = new String[10]; for (int i=0; i<10; i++) cols[i] = "";

        modelQuanY = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                Object nameObj = getValueAt(row, 0);
                String ten = nameObj != null ? nameObj.toString().trim() : "";
                boolean summary = ten.startsWith("TB toàn trận") || ten.startsWith("TB ngày cao nhất");
                if (summary) return false;
                if (column == 0 || column == 1 || column == 3 || column == 5 || column == 6
                        || column == 7 || column == 8 || column == 9) return false;
                return column == 2 || column == 4;
            }
        };

        rebuildQuanYModelRows(0);

        modelQuanY.addTableModelListener(e -> {
            if (isCalculating) return;
            if (e.getType() == TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col == TableModelEvent.ALL_COLUMNS) {
                    isCalculating = true;
                    try {
                        for (int r = 0; r < modelQuanY.getRowCount(); r++) {
                            calculateRow(r);
                        }
                    } finally {
                        isCalculating = false;
                    }
                    autoFillLastEmptyRow();
                } else if (col == 1 || col == 2 || col == 4) {
                    isCalculating = true;
                    try {
                        for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                            calculateRow(r);
                        }
                    } finally {
                        isCalculating = false;
                    }
                    autoFillLastEmptyRow();
                }
            }
        });

        tblQuanY = new JTable(modelQuanY);
        tblQuanY.setRowHeight(30);
        tblQuanY.setTableHeader(null);
        tblQuanY.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupModernTableStyle(tblQuanY, w);

        JPanel headerPanel = createQuanYHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(tblQuanY);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(AssurancePlanUiUtils.SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AssurancePlanUiUtils.SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);

        return pnl;
    }

    /**
     * Cột 0 Đơn vị; 1 QS; 2 TL TB%; 3 SN TB; 4 TL BB%; 5 SN BB; 6 Tổng TBBB; 7 Cáng bộ; 8 Tự đi; 9 Tổng NC.
     */
    private void rebuildQuanYModelRows(int sessionId) {
        List<String> huongs = service.getDanhSachHuongSCD(sessionId);
        while (modelQuanY.getRowCount() > 0) {
            modelQuanY.removeRow(0);
        }
        modelQuanY.addRow(new Object[]{"TB toàn trận", "", "", "", "", "", "", "", "", ""});
        for (String h : huongs) {
            modelQuanY.addRow(new Object[]{"  " + h, "", "", "", "", "", "", "", "", ""});
        }
        modelQuanY.addRow(new Object[]{"TB ngày cao nhất", "", "", "", "", "", "", "", "", ""});
    }

    /**
     * Nếu chỉ còn đúng 1 dòng hướng chưa điền TL%, tự tính SN và TL% cho dòng đó
     * = (Tổng TB toàn trận SN) - (Tổng SN các dòng khác).
     */
    private void autoFillLastEmptyRow() {
        int toanTranRow = -1;
        List<Integer> directionRows = new ArrayList<>();

        for (int r = 0; r < modelQuanY.getRowCount(); r++) {
            Object v = modelQuanY.getValueAt(r, 0);
            String name = v != null ? v.toString().trim() : "";
            if (name.startsWith("TB toàn trận")) {
                toanTranRow = r;
            } else if (!name.startsWith("TB ngày cao nhất")) {
                directionRows.add(r);
            }
        }

        if (toanTranRow < 0 || directionRows.isEmpty()) return;

        // 1. Xử lý cột Thương binh (col 2/3)
        List<Integer> unfilledTb = new ArrayList<>();
        for (int r : directionRows) {
            if (parseTiLePhanTram(modelQuanY.getValueAt(r, 2)) == 0) {
                unfilledTb.add(r);
            }
        }

        if (unfilledTb.size() == 1) {
            int emptyRow = unfilledTb.get(0);
            int emptyQS = parseQuanSo(modelQuanY.getValueAt(emptyRow, 1));
            int totalSnTb = parseQuanSo(modelQuanY.getValueAt(toanTranRow, 3));
            if (emptyQS > 0 && totalSnTb > 0) {
                int sumSnTb = 0;
                for (int r : directionRows) {
                    if (r != emptyRow) sumSnTb += parseQuanSo(modelQuanY.getValueAt(r, 3));
                }
                int remainSnTb = Math.max(0, totalSnTb - sumSnTb);
                double tlTb = remainSnTb * 100.0 / emptyQS;

                isCalculating = true;
                try {
                    modelQuanY.setValueAt(String.format("%.0f", tlTb), emptyRow, 2);
                    calculateRow(emptyRow);
                } finally {
                    isCalculating = false;
                }
            }
        }

        // 2. Xử lý cột Bệnh binh (col 4/5)
        List<Integer> unfilledBb = new ArrayList<>();
        for (int r : directionRows) {
            if (parseTiLePhanTram(modelQuanY.getValueAt(r, 4)) == 0) {
                unfilledBb.add(r);
            }
        }

        if (unfilledBb.size() == 1) {
            int emptyRow = unfilledBb.get(0);
            int emptyQS = parseQuanSo(modelQuanY.getValueAt(emptyRow, 1));
            int totalSnBb = parseQuanSo(modelQuanY.getValueAt(toanTranRow, 5));
            if (emptyQS > 0 && totalSnBb > 0) {
                int sumSnBb = 0;
                for (int r : directionRows) {
                    if (r != emptyRow) sumSnBb += parseQuanSo(modelQuanY.getValueAt(r, 5));
                }
                int remainSnBb = Math.max(0, totalSnBb - sumSnBb);
                double tlBb = remainSnBb * 100.0 / emptyQS;

                isCalculating = true;
                try {
                    modelQuanY.setValueAt(String.format("%.0f", tlBb), emptyRow, 4);
                    calculateRow(emptyRow);
                } finally {
                    isCalculating = false;
                }
            }
        }
    }

    private boolean isTbNgayCaoNhatRow(int row) {
        Object v = modelQuanY.getValueAt(row, 0);
        return v != null && v.toString().trim().startsWith("TB ngày cao nhất");
    }

    private int parseQuanSo(Object o) {
        if (o == null) return 0;
        String s = o.toString().trim().replace(".", "");
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseTiLePhanTram(Object o) {
        if (o == null) return 0;
        String s = o.toString().trim().replace(",", ".");
        if (s.isEmpty()) return 0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Tính SN TB/BB, tổng TBBB, Cáng bộ (ceil 50%), Tự đi, tổng nhu cầu vận chuyển.
     */
    private void calculateRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= modelQuanY.getRowCount()) return;

        int quanSo = parseQuanSo(modelQuanY.getValueAt(rowIndex, 1));
        double tlTb = parseTiLePhanTram(modelQuanY.getValueAt(rowIndex, 2));
        int snTb = (int) Math.round(quanSo * tlTb / 100.0);

        modelQuanY.setValueAt(snTb > 0 ? String.valueOf(snTb) : "0", rowIndex, 3);

        int snBb;
        int tongTbbb;
        if (isTbNgayCaoNhatRow(rowIndex)) {
            modelQuanY.setValueAt("", rowIndex, 4);
            modelQuanY.setValueAt("", rowIndex, 5);
            snBb = 0;
            tongTbbb = snTb;
        } else {
            double tlBb = parseTiLePhanTram(modelQuanY.getValueAt(rowIndex, 4));
            snBb = (int) Math.round(quanSo * tlBb / 100.0);
            modelQuanY.setValueAt(snBb > 0 ? String.valueOf(snBb) : "0", rowIndex, 5);
            tongTbbb = snTb + snBb;
        }

        modelQuanY.setValueAt(String.valueOf(tongTbbb), rowIndex, 6);
        applyCangTuDiVaTongVanChuyen(rowIndex, tongTbbb);
    }

    private void applyCangTuDiVaTongVanChuyen(int rowIndex, int tongTbbb) {
        int cang = (int) Math.ceil(tongTbbb / 2.0);
        int tuDi = tongTbbb - cang;
        modelQuanY.setValueAt(String.valueOf(cang), rowIndex, 7);
        modelQuanY.setValueAt(String.valueOf(tuDi), rowIndex, 8);
        modelQuanY.setValueAt(String.valueOf(cang + tuDi), rowIndex, 9);
    }

    private JPanel createQuanYHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 90));
        int[] x = new int[11]; x[0]=0; for(int i=0; i<10; i++) x[i+1] = x[i]+w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("Đơn vị", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Quân số<br>chiến đấu<br>(Người)</center></html>", x[1], 0, w[1], 90));

        int wDuKien = w[2]+w[3]+w[4]+w[5]+w[6];
        p.add(UIUtils.createAbsoluteHeaderLabel("Dự kiến tỷ lệ thương binh, bệnh binh", x[2], 0, wDuKien, 30));

        int wNhuCau = w[7]+w[8]+w[9];
        p.add(UIUtils.createAbsoluteHeaderLabel("Nhu cầu vận chuyển TBBB", x[7], 0, wNhuCau, 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Thương binh", x[2], 30, w[2]+w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bệnh binh", x[4], 30, w[4]+w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[6], 30, w[6], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Cáng bộ<br>(50%)</center></html>", x[7], 30, w[7], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Tự đi<br>(50%)</center></html>", x[8], 30, w[8], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[9], 30, w[9], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("Tỷ lệ (%)", x[2], 60, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Số người", x[3], 60, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tỷ lệ (%)", x[4], 60, w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Số người", x[5], 60, w[5], 30));

        return p;
    }

    private void setupModernTableStyle(JTable table, int[] w) {
        Color gridColor = new Color(226, 232, 240);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text0 = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim() : "";
                boolean isSummary = text0.startsWith("TB toàn trận") || text0.startsWith("TB ngày cao nhất");

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 9 ? 0 : 1, gridColor));
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);

                if (isSummary) {
                    setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
                    c.setBackground(new Color(241, 245, 249));
                    c.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    c.setForeground(Color.BLACK);
                    if (column == 1 || column == 3 || column == 5 || column == 6 || column == 7 || column == 8 || column == 9) {
                        c.setBackground(new Color(250, 252, 255));
                    }
                    else if (isSelected && column != 0) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                }
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
        isCalculating = true;
        try {
            rebuildQuanYModelRows(sessionId);
            service.loadFromDatabase(sessionId, modelQuanY);
        } finally {
            isCalculating = false;
        }

        isCalculating = true;
        try {
            for (int i = 0; i < modelQuanY.getRowCount(); i++) {
                calculateRow(i);
            }
        } finally {
            isCalculating = false;
        }
    }

    // =========================================================================
    // XUẤT TỪNG KEYWORD RA WORD
    // =========================================================================
    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        
        final int MAX_ROWS = 7; // Co dinh 7 dong: Toan tran + 5 huong + Ngay cao nhat
        int rowCount = modelQuanY.getRowCount();
        for (int i = 0; i < MAX_ROWS; i++) {
            int r = i + 1;
            if (i < rowCount) {
                data.put("<<qy_qs_" + r + ">>", getVal(i, 1));
                data.put("<<qy_tb_tl_" + r + ">>", getVal(i, 2));
                data.put("<<qy_tb_sn_" + r + ">>", getVal(i, 3));
                data.put("<<qy_bb_tl_" + r + ">>", getVal(i, 4));
                data.put("<<qy_bb_sn_" + r + ">>", getVal(i, 5));
                data.put("<<qy_tong_" + r + ">>", getVal(i, 6));
                data.put("<<qy_cang_" + r + ">>", getVal(i, 7));
                data.put("<<qy_tudi_" + r + ">>", getVal(i, 8));
                data.put("<<qy_nc_tong_" + r + ">>", getVal(i, 9));
            } else {
                data.put("<<qy_qs_" + r + ">>", "");
                data.put("<<qy_tb_tl_" + r + ">>", "");
                data.put("<<qy_tb_sn_" + r + ">>", "");
                data.put("<<qy_bb_tl_" + r + ">>", "");
                data.put("<<qy_bb_sn_" + r + ">>", "");
                data.put("<<qy_tong_" + r + ">>", "");
                data.put("<<qy_cang_" + r + ">>", "");
                data.put("<<qy_tudi_" + r + ">>", "");
                data.put("<<qy_nc_tong_" + r + ">>", "");
            }
        }

        data.put("<<quany_chuan_bi>>", txtChuanBi.getText().trim());
        data.put("<<quany_chien_dau>>", txtChienDau.getText().trim());
        data.put("<<ve_sinh_phong_dich>>", txtVeSinh.getText().trim());
        return data;
    }

    private String getVal(int row, int col) {
        Object v = modelQuanY.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }
}