package org.example.Popup.RegulationDetailDialog;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.text.Normalizer;

/**
 * Chi tiết phân cấp dạng JTable: Tổng (+) từ tab cha; Kho tự tính theo Tổng - Đơn vị;
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
    private final int sessionId;

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

    private boolean cacHuongEnabled = true;
    private Set<String> selectedStep2Directions = new LinkedHashSet<>();
    private final Set<Integer> lockedPhanColumns = new HashSet<>();
    private TableCellEditor defaultObjectEditor;
    private final TableCellEditor readOnlyDirectionEditor = new DefaultCellEditor(new JTextField()) {
        @Override
        public boolean isCellEditable(java.util.EventObject anEvent) {
            return false;
        }
    };

    public RegulationDetailDialogUI(Frame owner, String tableName, DefaultTableModel parentModel,
                                    int targetColumn, String hinhThucTapBai, int sessionId) {
        super(owner, "Khai báo chi tiết: " + tableName, true);
        this.parentModel = parentModel;
        this.targetColumn = targetColumn;
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";
        this.sessionId = sessionId;

        this.phanLabels = RegulationDetailDialogService.phanCapLabels(this.hinhThucTapBai);
        this.phanCount = phanLabels.length;
        this.totalCols = COL_PHAN_START + phanCount;

        loadDirectionLockState();

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
                if (column == COL_DON_VI) {
                    return true;
                }
                return column >= COL_PHAN_START && column < COL_PHAN_START + phanCount && !isPhanColumnLocked(column);
            }
        };

        table = new JTable(detailModel);
        defaultObjectEditor = table.getDefaultEditor(Object.class);
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
        applyColumnLock();

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
        applyColumnLock();
    }

    /**
     * Khóa các cột phân cấp chưa được khai báo ở Bước 2 để bảo toàn tính nhất quán dữ liệu.
     */
    private void applyColumnLock() {
        lockedPhanColumns.clear();

        // Session cũ có thể chưa có dữ liệu Step 2 chuẩn hóa; khi chưa xác định được thì không khóa cứng toàn bộ.
        if (selectedStep2Directions.isEmpty()) {
            for (int i = 0; i < phanCount; i++) {
                int col = COL_PHAN_START + i;
                table.getColumnModel().getColumn(col).setCellEditor(defaultObjectEditor);
            }
            table.repaint();
            return;
        }

        for (int i = 0; i < phanCount; i++) {
            int col = COL_PHAN_START + i;
            boolean lockCol = !cacHuongEnabled || !isDirectionEnabledByStep2Label(phanLabels[i]);
            if (lockCol) {
                lockedPhanColumns.add(col);
                table.getColumnModel().getColumn(col).setCellEditor(readOnlyDirectionEditor);
            } else {
                table.getColumnModel().getColumn(col).setCellEditor(defaultObjectEditor);
            }
        }
        table.repaint();
    }

    private void loadDirectionLockState() {
        selectedStep2Directions = new LinkedHashSet<>();
        cacHuongEnabled = false;
        RegulationDetailDialogService.Step2SelectionState st = RegulationDetailDialogService.loadStep2SelectionState(sessionId);
        if (st != null) {
            cacHuongEnabled = st.cacHuongEnabled;
            selectedStep2Directions = new LinkedHashSet<>(st.selectedDirections);
        }
    }

    private boolean isDirectionEnabledByStep2Label(String stepLabel) {
        String key = normalizeDirectionLabel(stepLabel);
        if (key.isEmpty()) {
            return false;
        }
        for (String s : selectedStep2Directions) {
            if (normalizeDirectionLabel(s).equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeDirectionLabel(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();
        return s.replaceAll("\\s+", " ");
    }

    private boolean isPhanColumnLocked(int column) {
        return column >= COL_PHAN_START && column < totalCols && lockedPhanColumns.contains(column);
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
        if (col != COL_DON_VI && (col < COL_PHAN_START || col >= totalCols)) {
            return;
        }
        int row = e.getFirstRow();
        if (row < 0 || row >= detailModel.getRowCount() || row >= tongSoLuongPerRow.size()) {
            return;
        }

        double tongRef = tongSoLuongPerRow.get(row);
        recalculateKhoAndSyncStore(row, tongRef);
    }

    private double sumPhanCapForRow(int row) {
        double s = 0;
        for (int c = COL_PHAN_START; c < totalCols; c++) {
            s += InputValidator.parseDoubleSafe(detailModel.getValueAt(row, c));
        }
        return s;
    }

    private void recalculateKhoAndSyncStore(int row, double tongRef) {
        double donVi = InputValidator.parseDoubleSafe(detailModel.getValueAt(row, COL_DON_VI));
        double kho = RegulationDetailDialogService.roundToTwoDecimals(tongRef - donVi);

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
            recalculateKhoAndSyncStore(r, tongRef);
        }
    }

    private static String formatCellFromStore(String s) {
        if (s == null || s.trim().isEmpty()) return "";
        double v = InputValidator.parseDoubleSafe(s);
        if (v == 0) return "";
        return formatDisplay(v);
    }

    private static String formatDisplay(double d) {
        d = RegulationDetailDialogService.roundToTwoDecimals(d);
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString().replace(".", ",");
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
        double value = InputValidator.parseDoubleSafe(v);
        return java.math.BigDecimal.valueOf(RegulationDetailDialogService.roundToTwoDecimals(value))
                .stripTrailingZeros()
                .toPlainString();
    }

    private void setupRenderers(int colCount) {
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(col == COL_NAME ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));

                boolean readOnly = col != COL_DON_VI && col < COL_PHAN_START;
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
                    if (isPhanColumnLocked(col)) {
                        c.setBackground(new Color(234, 236, 240));
                        c.setForeground(new Color(100, 116, 139));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }

                if (isSelected && ((col == COL_DON_VI) || (col >= COL_PHAN_START && !isPhanColumnLocked(col)))) {
                    c.setBackground(new Color(219, 234, 254));
                }
                if (isPhanColumnLocked(col)) {
                    ((JComponent) c).setToolTipText("Cột phân cấp bị khóa vì hướng tương ứng chưa khai báo ở Bước 2");
                } else {
                    ((JComponent) c).setToolTipText(null);
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
