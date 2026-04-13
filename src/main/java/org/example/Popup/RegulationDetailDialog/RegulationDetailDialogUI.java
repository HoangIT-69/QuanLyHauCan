package org.example.Popup.RegulationDetailDialog;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chi tiết phân cấp dạng JTable: Tổng (+) từ tab cha; Kho/Đơn vị khóa, tự tính từ phân cấp;
 * cột phân cấp 5 (Phòng ngự) hoặc 6 (Tiến công). Đồng bộ {@link RegulationDetailDialogService#detailDataStore} theo thời gian thực.
 */
public class RegulationDetailDialogUI extends JDialog {

    private static final int COL_NAME = 0;
    private static final int COL_DVT = 1;
    private static final int COL_TONG = 2;
    private static final int COL_KHO = 3;
    private static final int COL_DON_VI = 4;
    private static final int COL_PHAN_START = 5;

    private final DefaultTableModel parentModel;
    private final int targetColumn;
    private final String hinhThucTapBai;

    private DefaultTableModel detailModel;
    private JTable table;
    private boolean isProgrammaticUpdate = false;

    /** Tổng số lượng (+) theo từng dòng dialog — khớp {@link #detailModel}. */
    private final List<Double> tongSoLuongPerRow = new ArrayList<>();

    private final String[] phanLabels;
    private final int phanCount;
    private final int[] colWidths;
    private final int totalCols;
    private final int totalTableWidth;

    public RegulationDetailDialogUI(Frame owner, String tableName, DefaultTableModel parentModel,
                                    int targetColumn, String hinhThucTapBai) {
        super(owner, "Khai báo chi tiết: " + tableName, true);
        this.parentModel = parentModel;
        this.targetColumn = targetColumn;
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";

        this.phanLabels = RegulationDetailDialogService.phanCapLabels(this.hinhThucTapBai);
        this.phanCount = phanLabels.length;
        this.totalCols = COL_PHAN_START + phanCount;

        this.colWidths = buildColumnWidths();
        this.totalTableWidth = sumWidths();

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int frameW = Math.min(totalTableWidth + 100, screen.width - 24);
        int frameH = Math.min(740, screen.height - 40);
        setSize(frameW, frameH);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel(tableName.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = buildColumnNames();
        detailModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= COL_PHAN_START && column < COL_PHAN_START + phanCount;
            }
        };

        table = new JTable(detailModel);
        table.setRowHeight(36);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(new Color(224, 224, 224));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < totalCols; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
        }

        setupRenderers(totalCols);

        detailModel.addTableModelListener(this::onDetailTableChanged);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setPreferredSize(new Dimension(
                Math.min(totalTableWidth, frameW - 80),
                Math.min(480, frameH - 220)));

        JPanel headerPanel = createComplexHeader(columnNames);
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e ->
                headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(17, 100));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedTablePanel = new JPanel(new BorderLayout());
        combinedTablePanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        combinedTablePanel.add(headerWrapper, BorderLayout.NORTH);
        combinedTablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlTableContainer = new JPanel(new BorderLayout(0, 10));
        pnlTableContainer.setBorder(new EmptyBorder(10, 20, 10, 20));
        pnlTableContainer.setBackground(Color.WHITE);

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setBackground(Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("🔄 Làm mới từ Tab", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> syncDataFromParent());
        pnlToolbar.add(btnRefresh);

        pnlTableContainer.add(pnlToolbar, BorderLayout.NORTH);
        pnlTableContainer.add(combinedTablePanel, BorderLayout.CENTER);
        add(pnlTableContainer, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnClose = new JButton("Hủy bỏ");
        JButton btnSave = new JButton("Lưu và Đồng bộ");
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnSave.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            flushAllRowsToStoreAndParent();
            dispose();
        });
        btnClose.addActionListener(e -> dispose());

        pnlSouth.add(btnClose);
        pnlSouth.add(btnSave);
        add(pnlSouth, BorderLayout.SOUTH);

        syncDataFromParent();
    }

    private int[] buildColumnWidths() {
        int[] w = new int[totalCols];
        w[COL_NAME] = 300;
        w[COL_DVT] = 76;
        w[COL_TONG] = 96;
        w[COL_KHO] = 88;
        w[COL_DON_VI] = 92;
        int phanW = RegulationDetailDialogService.isPhongNgu(hinhThucTapBai) ? 132 : 124;
        for (int i = COL_PHAN_START; i < totalCols; i++) {
            w[i] = phanW;
        }
        return w;
    }

    private int sumWidths() {
        int s = 0;
        for (int v : colWidths) {
            s += v;
        }
        return s;
    }

    private void onDetailTableChanged(TableModelEvent e) {
        if (isProgrammaticUpdate) {
            return;
        }
        if (e.getType() != TableModelEvent.UPDATE) {
            return;
        }
        int col = e.getColumn();
        if (col == TableModelEvent.ALL_COLUMNS) {
            return;
        }
        if (col < COL_PHAN_START || col >= totalCols) {
            return;
        }
        int row = e.getFirstRow();
        if (row < 0 || row >= detailModel.getRowCount() || row >= tongSoLuongPerRow.size()) {
            return;
        }

        double tongRef = tongSoLuongPerRow.get(row);
        double sumPhan = sumPhanCapForRow(row);

        if (sumPhan > tongRef + 1e-9) {
            final int r = row;
            final int c = col;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Tổng phân cấp không được vượt quá Tổng số lượng (+). Giá trị vừa nhập được đặt lại về 0.",
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                isProgrammaticUpdate = true;
                try {
                    detailModel.setValueAt("0", r, c);
                } finally {
                    isProgrammaticUpdate = false;
                }
            });
            return;
        }

        applyKhoDonViAndSyncStore(row, tongRef, sumPhan);
    }

    private double sumPhanCapForRow(int row) {
        double s = 0;
        for (int c = COL_PHAN_START; c < totalCols; c++) {
            s += InputValidator.parseDoubleSafe(detailModel.getValueAt(row, c));
        }
        return s;
    }

    private void applyKhoDonViAndSyncStore(int row, double tongRef, double sumPhan) {
        double donVi = sumPhan;
        double kho = tongRef - donVi;

        isProgrammaticUpdate = true;
        try {
            detailModel.setValueAt(formatDisplay(donVi), row, COL_DON_VI);
            detailModel.setValueAt(formatDisplay(kho), row, COL_KHO);
        } finally {
            isProgrammaticUpdate = false;
        }

        String ten = detailModel.getValueAt(row, COL_NAME).toString();
        String key = ten + "_" + targetColumn;
        double[] ph6 = new double[6];
        for (int p = 0; p < phanCount; p++) {
            ph6[p] = InputValidator.parseDoubleSafe(detailModel.getValueAt(row, COL_PHAN_START + p));
        }
        RegulationDetailDialogService.putComputedRow(key, kho, donVi, ph6);
    }

    private String[] buildColumnNames() {
        String[] names = new String[totalCols];
        names[COL_NAME] = "Đạn, VC hậu cần, vật tư kỹ thuật";
        names[COL_DVT] = "ĐVT";
        names[COL_TONG] = "Tổng (+)";
        names[COL_KHO] = "Kho";
        names[COL_DON_VI] = "Đơn vị";
        System.arraycopy(phanLabels, 0, names, COL_PHAN_START, phanCount);
        return names;
    }

    private boolean isHeaderRow(String text) {
        return text.equals("ĐẠN") || text.equals("VẬT CHẤT HẬU CẦN") || text.equals("VẬT TƯ KỸ THUẬT");
    }

    private void syncDataFromParent() {
        tongSoLuongPerRow.clear();
        detailModel.setRowCount(0);
        for (int i = 0; i < parentModel.getRowCount(); i++) {
            Object valTen = parentModel.getValueAt(i, COL_NAME);
            if (valTen == null) {
                continue;
            }
            String ten = valTen.toString().trim();
            if (isHeaderRow(ten)) {
                continue;
            }

            String dvt = parentModel.getValueAt(i, COL_DVT) != null ? parentModel.getValueAt(i, COL_DVT).toString().trim() : "";
            String tongParent = parentModel.getValueAt(i, targetColumn) != null ? parentModel.getValueAt(i, targetColumn).toString() : "0";
            double tongRef = InputValidator.parseDoubleSafe(tongParent);

            String key = ten + "_" + targetColumn;
            String[] raw = RegulationDetailDialogService.detailDataStore.get(key);
            String[] eight = RegulationDetailDialogService.upgradeRawPartsToEight(
                    raw != null ? raw : RegulationDetailDialogService.DEFAULT_EIGHT.clone());

            Object[] row = new Object[totalCols];
            row[COL_NAME] = ten;
            row[COL_DVT] = dvt;
            row[COL_TONG] = tongParent;
            row[COL_KHO] = formatCellFromStore(eight[0]);
            row[COL_DON_VI] = formatCellFromStore(eight[1]);
            for (int p = 0; p < phanCount; p++) {
                row[COL_PHAN_START + p] = formatCellFromStore(eight[2 + p]);
            }

            tongSoLuongPerRow.add(tongRef);
            detailModel.addRow(row);

            int r = detailModel.getRowCount() - 1;
            double sumPhan = sumPhanCapForRow(r);
            applyKhoDonViAndSyncStore(r, tongRef, sumPhan);
        }
    }

    private static String formatCellFromStore(String s) {
        double v = InputValidator.parseDoubleSafe(s);
        return formatDisplay(v);
    }

    private static String formatDisplay(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.valueOf(d).replace(".", ",");
    }

    /**
     * Lưu toàn bộ dòng vào store (đã cập nhật real-time) và đồng bộ cột nghiệp vụ trên bảng cha.
     */
    private void flushAllRowsToStoreAndParent() {
        for (int i = 0; i < detailModel.getRowCount(); i++) {
            String ten = detailModel.getValueAt(i, COL_NAME).toString();
            String key = ten + "_" + targetColumn;

            String[] out = new String[8];
            out[0] = cellToStore(detailModel.getValueAt(i, COL_KHO));
            out[1] = cellToStore(detailModel.getValueAt(i, COL_DON_VI));
            for (int p = 0; p < 6; p++) {
                if (p < phanCount) {
                    out[2 + p] = cellToStore(detailModel.getValueAt(i, COL_PHAN_START + p));
                } else {
                    out[2 + p] = "0";
                }
            }
            RegulationDetailDialogService.putDetailEight(key, out);

            String totalStr = detailModel.getValueAt(i, COL_TONG) != null ? detailModel.getValueAt(i, COL_TONG).toString() : "0";
            for (int j = 0; j < parentModel.getRowCount(); j++) {
                Object parentVal = parentModel.getValueAt(j, COL_NAME);
                if (parentVal != null && parentVal.toString().trim().equals(ten)) {
                    parentModel.setValueAt(totalStr, j, targetColumn);
                    break;
                }
            }
        }
    }

    private static String cellToStore(Object v) {
        if (v == null) {
            return "0";
        }
        return v.toString().trim().replace(",", ".");
    }

    private void setupRenderers(int colCount) {
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(col == COL_NAME ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));

                boolean readOnly = col <= COL_DON_VI;
                if (readOnly) {
                    c.setBackground(new Color(241, 245, 249));
                    c.setForeground(new Color(71, 85, 105));
                    if (col == COL_TONG) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setForeground(new Color(192, 57, 43));
                    } else {
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                if (isSelected && col >= COL_PHAN_START) {
                    c.setBackground(new Color(219, 234, 254));
                }
                return c;
            }
        };

        for (int i = 0; i < colCount; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }

    private JPanel createComplexHeader(String[] names) {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(totalTableWidth, 100));

        int[] x = new int[totalCols + 1];
        x[0] = 0;
        for (int i = 0; i < totalCols; i++) {
            x[i + 1] = x[i] + colWidths[i];
        }

        int wPhan = 0;
        for (int i = COL_PHAN_START; i < totalCols; i++) {
            wPhan += colWidths[i];
        }

        p.add(absLabel(htmlShort(names[COL_NAME]), x[COL_NAME], 0, colWidths[COL_NAME], 100));
        p.add(absLabel(names[COL_DVT], x[COL_DVT], 0, colWidths[COL_DVT], 100));
        p.add(absLabel(htmlShort(names[COL_TONG]), x[COL_TONG], 0, colWidths[COL_TONG], 100));
        p.add(absLabel(names[COL_KHO], x[COL_KHO], 0, colWidths[COL_KHO], 100));
        p.add(absLabel(names[COL_DON_VI], x[COL_DON_VI], 0, colWidths[COL_DON_VI], 100));

        p.add(absLabel("<html><center>Phân cấp</center></html>", x[COL_PHAN_START], 0, wPhan, 38));
        for (int j = COL_PHAN_START; j < totalCols; j++) {
            p.add(absLabel(htmlShort(phanLabels[j - COL_PHAN_START]), x[j], 38, colWidths[j], 62));
        }

        return p;
    }

    private static String htmlShort(String s) {
        if (s.length() > 32) {
            return "<html><center>" + s.substring(0, 29) + "…</center></html>";
        }
        return "<html><center>" + s + "</center></html>";
    }

    private JLabel absLabel(String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        l.setBounds(x, y, w, h);
        l.setOpaque(true);
        l.setBackground(new Color(248, 250, 252));
        return l;
    }
}
