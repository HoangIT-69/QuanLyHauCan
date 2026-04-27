package org.example.Tab.AssurancePlan.Tab3_OrgPlanPanel;

import org.example.Utils.AssurancePlanUiUtils;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class Tab3_OrgPlanPanelUI extends JPanel {

    private final Tab3_OrgPlanPanelService service;

    private DefaultTableModel model;
    private JTable table;
    private JTextArea txtViTriChinhThuc;
    private JTextArea txtViTriDuBi;
    private boolean isCalculating = false;

    private static final int[] SUM_COLUMNS = {1, 2, 3, 5, 6, 7};

    public Tab3_OrgPlanPanelUI() {
        this(new Tab3_OrgPlanPanelService());
    }

    public Tab3_OrgPlanPanelUI(int sessionId) {
        this(new Tab3_OrgPlanPanelService());
        // Bỏ loadTenLucLuong từ DB, chuyển sang dùng danh sách cố định
    }

    public Tab3_OrgPlanPanelUI(Tab3_OrgPlanPanelService service) {
        this.service = service != null ? service : new Tab3_OrgPlanPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("III. TỔ CHỨC, SỬ DỤNG LỰC LƯỢNG, BỐ TRÍ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("1. Tổ chức sử dụng ,lực lượng"));
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTablePanel());
        
        // Nút thêm/xóa dòng
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlAction.setOpaque(false);
        pnlAction.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton btnAdd = new JButton("Thêm dòng");
        JButton btnDelete = new JButton("Xóa dòng");
        styleActionBtn(btnAdd);
        styleActionBtn(btnDelete);
        
        btnAdd.addActionListener(e -> addRow());
        btnDelete.addActionListener(e -> deleteRow());
        
        pnlAction.add(btnAdd);
        pnlAction.add(btnDelete);
        mainContainer.add(pnlAction);
        
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("2. Bố trí hậu cần kĩ thuật"));
        mainContainer.add(Box.createVerticalStrut(10));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí chính thức:"));
        txtViTriChinhThuc = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtViTriChinhThuc, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí dự bị:"));
        txtViTriDuBi = AssurancePlanUiUtils.createModernTextArea();
        mainContainer.add(AssurancePlanUiUtils.scrollBorderedTextArea(txtViTriDuBi, 80));
        mainContainer.add(Box.createVerticalStrut(20));

        add(AssurancePlanUiUtils.wrapVerticalScroll(mainContainer), BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setMaximumSize(new Dimension(900, 400));

        int[] w = {150, 60, 100, 60, 150, 120, 80, 130};
        String[] cols = new String[8];
        for (int i = 0; i < 8; i++) cols[i] = "";

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return row != getRowCount() - 1;
            }
        };

        String[] fixedForces = {
                "Trợ lý hậu cần",
                "N.viên QN+KT",
                "Quân y/d",
                "Quân y/e",
                "Vận tải bộ/d",
                "Vận tải bộ/e",
                "Nuôi quân",
                "Nhân viên QK",
                "Tổ sửa chữa/e"
        };

        for (String force : fixedForces) {
            model.addRow(new Object[]{force, "", "", "", "", "", "", ""});
        }
        // Thêm một vài dòng trống dự phòng
        for (int i = 0; i < 3; i++) {
            model.addRow(new Object[]{"", "", "", "", "", "", "", ""});
        }
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

                if (row == tbl.getRowCount() - 1) {
                    c.setFont(new Font("Times New Roman", Font.BOLD, 15));
                    c.setBackground(new Color(241, 245, 249));
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

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isCalculating) return;
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    if (row < model.getRowCount() - 1) {
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
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(scrollWidth, 60));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createHeader(int[] w) {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0;
        for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 60));
        int[] x = new int[9];
        x[0] = 0;
        for (int i = 0; i < 8; i++) x[i + 1] = x[i] + w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("Tên lực lượng", x[0], 0, w[0], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Quân số", x[1], 0, w[1] + w[2] + w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Khả năng", x[4], 0, w[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phân chia", x[5], 0, w[5] + w[6] + w[7], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("SQ", x[1], 30, w[1], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>QNCN<br>HSQ</center></html>", x[2], 30, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[3], 30, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bộ phận HCKT", x[5], 30, w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Dự bị", x[6], 30, w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>T.cường<br>cho dưới</center></html>", x[7], 30, w[7], 30));

        return p;
    }

    private void recalculateSum() {
        int rowCount = model.getRowCount();
        if (rowCount <= 1) return;
        
        int lastRow = rowCount - 1;
        for (int col : SUM_COLUMNS) {
            int sum = 0;
            boolean hasValue = false;
            for (int row = 0; row < lastRow; row++) {
                int val = parseIntSafe(getCellValue(row, col));
                if (val != 0 || !getCellValue(row, col).isEmpty()) hasValue = true;
                sum += val;
            }
            model.setValueAt(hasValue ? String.valueOf(sum) : "", lastRow, col);
        }
        model.setValueAt("", lastRow, 4);
        model.setValueAt("Cộng", lastRow, 0);
    }

    private void addRow() {
        isCalculating = true;
        int lastRow = model.getRowCount() - 1;
        model.insertRow(lastRow, new Object[]{"", "", "", "", "", "", "", ""});
        isCalculating = false;
    }

    private void deleteRow() {
        int selected = table.getSelectedRow();
        int lastRow = model.getRowCount() - 1;
        if (selected >= 0 && selected < lastRow) {
            isCalculating = true;
            model.removeRow(selected);
            recalculateSum();
            isCalculating = false;
        } else if (lastRow > 0) {
            isCalculating = true;
            model.removeRow(lastRow - 1);
            recalculateSum();
            isCalculating = false;
        }
    }

    private void styleActionBtn(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(248, 250, 252));
        btn.setForeground(AssurancePlanUiUtils.SLATE_TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private int parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(text.trim().replaceAll("^0+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // loadTenLucLuong đã được xóa bỏ theo yêu cầu cố định lực lượng.

    public Map<String, String> getExportData() {
        return service.buildExportData(model, txtViTriChinhThuc.getText(), txtViTriDuBi.getText());
    }

    private String getCellValue(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
