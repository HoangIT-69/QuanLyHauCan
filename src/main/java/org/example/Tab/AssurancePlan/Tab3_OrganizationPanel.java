package org.example.Tab.AssurancePlan;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab3_OrganizationPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextArea txtViTriChinhThuc;
    private JTextArea txtViTriDuBi;
    private boolean isCalculating = false;

    // Các cột số cần tính tổng: SQ(1), QNCN+HSQ(2), +(3), Bộ phận HCKT(5), Dự bị(6), T.cường cho dưới(7)
    private static final int[] SUM_COLUMNS = {1, 2, 3, 5, 6, 7};
    private static final Color SLATE_TEXT = new Color(30, 41, 59);

    public Tab3_OrganizationPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("III. TỔ CHỨC, SỬ DỤNG LỰC LƯỢNG, BỐ TRÍ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Tổ chức lực lượng
        mainContainer.add(UIUtils.createSectionLabel("1. Tổ chức lực lượng"));
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTablePanel());
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Bố trí
        mainContainer.add(UIUtils.createSectionLabel("2. Bố trí hậu cần kĩ thuật"));
        mainContainer.add(Box.createVerticalStrut(10));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí chính thức:"));
        txtViTriChinhThuc = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtViTriChinhThuc, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí dự bị:"));
        txtViTriDuBi = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtViTriDuBi, 80));
        mainContainer.add(Box.createVerticalStrut(20));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(SLATE_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return area;
    }

    private JScrollPane createTextAreaScrollWithBorder(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.makeScrollPassThrough(scroll);
        return scroll;
    }

    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setMaximumSize(new Dimension(900, 400)); // Nới rộng xíu để chứa 11 dòng

        int[] w = {150, 60, 100, 60, 150, 120, 80, 130};
        String[] cols = new String[8]; for (int i=0; i<8; i++) cols[i] = "";

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return row != 10; // Cố định 10 dòng nhập (index 0->9), dòng 10 (Cộng) khóa lại
            }
        };

        // KẺ SẴN 10 DÒNG TRỐNG
        for (int i = 0; i < 10; i++) {
            model.addRow(new Object[]{"", "", "", "", "", "", "", ""});
        }
        // DÒNG 11 LÀ DÒNG CỘNG
        model.addRow(new Object[]{"Cộng", "", "", "", "", "", "", ""});

        table = new JTable(model);
        table.setRowHeight(35);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 7 ? 0 : 1, new Color(226, 232, 240)));

                if (row == 10) { // Dòng Cộng
                    c.setFont(new Font("Times New Roman", Font.BOLD, 15));
                    c.setBackground(new Color(241, 245, 249)); // Màu xám nhạt
                    c.setForeground(Color.BLACK);
                } else {
                    c.setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    if (isSelected) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                if (column == 0 || column == 4) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
        }

        // Tự động cộng tổng
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isCalculating) return;
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    if (row < 10) { // Nếu sửa trong 10 dòng đầu
                        isCalculating = true;
                        recalculateSum();
                        isCalculating = false;
                    }
                }
            }
        });

        JPanel headerPanel = createHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(new Color(203, 213, 225), 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 60)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(203, 213, 225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 60));
        int[] x = new int[9]; x[0]=0; for(int i=0; i<8; i++) x[i+1] = x[i]+w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("Tên lực lượng", x[0], 0, w[0], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Quân số", x[1], 0, w[1]+w[2]+w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Khả năng", x[4], 0, w[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phân chia", x[5], 0, w[5]+w[6]+w[7], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("SQ", x[1], 30, w[1], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>QNCN<br>HSQ</center></html>", x[2], 30, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[3], 30, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bộ phận HCKT", x[5], 30, w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Dự bị", x[6], 30, w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>T.cường<br>cho dưới</center></html>", x[7], 30, w[7], 30));

        return p;
    }

    private void recalculateSum() {
        for (int col : SUM_COLUMNS) {
            int sum = 0;
            boolean hasValue = false;
            for (int row = 0; row < 10; row++) {
                int val = parseIntSafe(getCellValue(row, col));
                if (val != 0 || !getCellValue(row, col).isEmpty()) hasValue = true;
                sum += val;
            }
            model.setValueAt(hasValue ? String.valueOf(sum) : "", 10, col);
        }
        model.setValueAt("", 10, 4); // Cột Khả năng trống
        model.setValueAt("Cộng", 10, 0); // Cột tên là Cộng
    }

    private int parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try { return Integer.parseInt(text.trim().replaceAll("^0+", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    // =========================================================================
    // XUẤT TỪNG KEYWORD RA WORD (THAY THẾ CHUỖI HTML)
    // =========================================================================
    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();

        // Loop xuất 10 dòng (i từ 0 đến 9)
        // Cú pháp keyword: <<tcll_cột_dòng>>. VD dòng 1 cột SQ: <<tcll_sq_1>>
        for (int i = 0; i < 10; i++) {
            int r = i + 1; // Dòng 1 -> 10
            data.put("<<tcll_name_" + r + ">>", getCellValue(i, 0));
            data.put("<<tcll_sq_" + r + ">>", getCellValue(i, 1));
            data.put("<<tcll_qn_" + r + ">>", getCellValue(i, 2));
            data.put("<<tcll_cong_" + r + ">>", getCellValue(i, 3));
            data.put("<<tcll_kn_" + r + ">>", getCellValue(i, 4));
            data.put("<<tcll_hckt_" + r + ">>", getCellValue(i, 5));
            data.put("<<tcll_db_" + r + ">>", getCellValue(i, 6));
            data.put("<<tcll_tc_" + r + ">>", getCellValue(i, 7));
        }

        // Xuất dòng Cộng (Dòng số 10 trong JTable)
        data.put("<<tcll_sq_sum>>", getCellValue(10, 1));
        data.put("<<tcll_qn_sum>>", getCellValue(10, 2));
        data.put("<<tcll_cong_sum>>", getCellValue(10, 3));
        data.put("<<tcll_hckt_sum>>", getCellValue(10, 5));
        data.put("<<tcll_db_sum>>", getCellValue(10, 6));
        data.put("<<tcll_tc_sum>>", getCellValue(10, 7));

        data.put("<<vi_tri_chinh_thuc>>", txtViTriChinhThuc.getText().trim());
        data.put("<<vi_tri_du_bi>>", txtViTriDuBi.getText().trim());

        return data;
    }

    private String getCellValue(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }
}