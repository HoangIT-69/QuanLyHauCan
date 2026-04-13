package org.example.Panel.Step3_MaterialPanel;

import org.example.Panel.DataDeclarationContext;
import org.example.Popup.RegulationDetailDialog.RegulationDetailDialogService;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bước 3: bảng vật chất — cột phân cấp phụ thuộc {@code hinhThucTapBai} (đồng bộ nhãn với RegulationDetailDialog).
 */
public class Step3_MaterialPanelUI extends JPanel {
    private final DataDeclarationContext parent;
    private final String hinhThucTapBai;
    private final Step3_MaterialPanelService service = new Step3_MaterialPanelService();

    private final int phanCount;
    private final String[] phanLabels;
    private final int totalCols;
    /** Cột ghi chú = 6 + phanCount */
    private final int colNote;
    private final int[] colWidths;

    private DefaultTableModel model;
    private JTable table;

    private List<Step3_MaterialPanelService.DanOption> danOptions = new ArrayList<>();
    private List<Step3_MaterialPanelService.VchcOption> vchcOptions = new ArrayList<>();

    private boolean isUpdatingSum = false;

    public Step3_MaterialPanelUI(DataDeclarationContext parent, String hinhThucTapBai) {
        this.parent = parent;
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";
        this.phanLabels = RegulationDetailDialogService.phanCapLabels(this.hinhThucTapBai);
        this.phanCount = phanLabels.length;
        this.totalCols = 7 + phanCount;
        this.colNote = 6 + phanCount;
        this.colWidths = buildColWidths(phanCount);

        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("KHAI BÁO VẬT CHẤT HIỆN CÓ CỦA TIỂU ĐOÀN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(192, 57, 43));
        lblTitle.setBorder(new EmptyBorder(5, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = buildColumnNames();
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String ttStr = getValueAt(row, 0) != null ? getValueAt(row, 0).toString().trim().toUpperCase() : "";
                if (ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    return false;
                }
                if (column == 3) {
                    return false;
                }
                return true;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null);
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < totalCols; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
        }

        table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new MaterialNameCellEditor());

        DefaultTableCellRenderer commonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));

                String ttStr = tbl.getValueAt(row, 0) != null ? tbl.getValueAt(row, 0).toString().trim().toUpperCase() : "";

                if (ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    c.setBackground(new Color(255, 241, 118));
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                } else if (col == 3) {
                    c.setBackground(new Color(241, 245, 249));
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                }

                if (isSelected && !ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    c.setBackground(new Color(219, 234, 254));
                }
                return c;
            }
        };

        for (int i = 0; i < totalCols; i++) {
            if (i != 1) {
                table.getColumnModel().getColumn(i).setCellRenderer(commonRenderer);
            }
        }

        model.addTableModelListener(e -> {
            if (!isUpdatingSum && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col >= 4 && col <= 5) {
                    int r = e.getFirstRow();
                    String ttStr = model.getValueAt(r, 0) != null ? model.getValueAt(r, 0).toString().trim().toUpperCase() : "";
                    if (!ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                        isUpdatingSum = true;
                        try {
                            double kho = InputValidator.parseDoubleSafe(model.getValueAt(r, 4));
                            double donVi = InputValidator.parseDoubleSafe(model.getValueAt(r, 5));
                            double sum = kho + donVi;
                            model.setValueAt(formatDouble(sum), r, 3);
                        } finally {
                            isUpdatingSum = false;
                        }
                    }
                }
            }
        });

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnPickDb = UIUtils.createStyledButton("➕ Chọn từ Danh mục", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnPickDb);
        pnlControls.add(btnDel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(pnlControls, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);

        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e -> {
            Point p = headerViewport.getViewPosition();
            p.x = e.getValue();
            headerViewport.setViewPosition(p);
        });

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(Color.WHITE);
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        int sbWidth = UIManager.getInt("ScrollBar.width");
        if (sbWidth == 0) {
            sbWidth = 17;
        }
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(sbWidth, 130));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(combinedPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton btnBack = UIUtils.createNavButtonWithIcon("Quay lại", new Color(149, 165, 166), "/images/return.png", false);
        JButton btnNext = UIUtils.createNavButtonWithIcon("Tiếp tục", new Color(41, 128, 185), "/images/next.png", true);
        bottomPanel.add(btnBack, BorderLayout.WEST);
        bottomPanel.add(btnNext, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnPickDb.addActionListener(e -> showDatabaseSelectionPopup());
        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String tt = model.getValueAt(r, 0) != null ? model.getValueAt(r, 0).toString().trim().toUpperCase() : "";
                if (tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    JOptionPane.showMessageDialog(this, "Không được xóa dòng phân loại!");
                    return;
                }
                model.removeRow(r);
                recalculateTT();
            }
        });

        btnBack.addActionListener(e -> parent.navigateStep(2));
        btnNext.addActionListener(e -> {
            if (saveToDatabase()) {
                parent.navigateStep(4);
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataFromDatabase();
            }
        });
    }

    private static int[] buildColWidths(int phanCount) {
        int[] w = new int[7 + phanCount];
        w[0] = 50;
        w[1] = 310;
        w[2] = 70;
        w[3] = 70;
        w[4] = 70;
        w[5] = 70;
        for (int i = 0; i < phanCount; i++) {
            w[6 + i] = 118;
        }
        w[6 + phanCount] = 160;
        return w;
    }

    private String[] buildColumnNames() {
        String[] names = new String[totalCols];
        names[0] = "TT";
        names[1] = "Loại vật chất";
        names[2] = "ĐVT";
        names[3] = "Tổng";
        names[4] = "Kho/d";
        names[5] = "Đơn vị";
        for (int i = 0; i < phanCount; i++) {
            names[6 + i] = phanLabels[i];
        }
        names[colNote] = "Ghi chú";
        return names;
    }

    public void loadDataFromDatabase() {
        reloadCatalogFromDb();
        int sessionId = parent.getCurrentSessionId();
        model.setRowCount(0);
        if (sessionId < 1) {
            initDefaultRows();
            return;
        }

        Step3_MaterialPanelService.LoadedGroups g = service.loadStep3FromDatabase(sessionId, phanCount, hinhThucTapBai);

        model.addRow(newSectionLabelRow("I", "ĐẠN"));
        for (Object[] row : g.danRows) {
            model.addRow(row);
        }

        model.addRow(newSectionLabelRow("II", "VẬT CHẤT HẬU CẦN"));
        for (Object[] row : g.hcRows) {
            model.addRow(row);
        }

        model.addRow(newSectionLabelRow("III", "VẬT TƯ KỸ THUẬT"));
        for (Object[] row : g.ktRows) {
            model.addRow(row);
        }

        recalculateTT();
    }

    private Object[] newSectionLabelRow(String tt, String col1) {
        Object[] r = new Object[totalCols];
        Arrays.fill(r, "");
        r[0] = tt;
        r[1] = col1;
        return r;
    }

    private Object[] newDataRowFromPicker(Step3_MaterialPanelService.PickerEntry entry) {
        Object[] r = new Object[totalCols];
        Arrays.fill(r, "");
        r[0] = "";
        r[1] = entry.name;
        r[2] = entry.dvt != null ? entry.dvt : "";
        r[3] = "0";
        r[4] = "0";
        r[5] = "0";
        for (int i = 0; i < phanCount; i++) {
            r[6 + i] = "0";
        }
        r[colNote] = "";
        return r;
    }

    private void reloadCatalogFromDb() {
        danOptions = service.loadQuyuocDan();
        vchcOptions = service.loadQuyuocVchc();
    }

    private void showDatabaseSelectionPopup() {
        reloadCatalogFromDb();
        Set<String> existing = new HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String ttStr = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim().toUpperCase() : "";
            if (ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                continue;
            }
            Object v1 = model.getValueAt(i, 1);
            if (v1 != null && !v1.toString().trim().isEmpty()) {
                existing.add(v1.toString().trim());
            }
        }

        List<Step3_MaterialPanelService.PickerEntry> items = service.buildCatalogPickerEntries(existing);
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả vật chất trong danh mục đã được thêm vào bảng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] display = items.stream().map(Step3_MaterialPanelUI::displayForPicker).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Chọn vật chất:", "Danh mục", JOptionPane.PLAIN_MESSAGE, null, display, display[0]);

        if (sel != null) {
            for (Step3_MaterialPanelService.PickerEntry item : items) {
                if (sel.equals(displayForPicker(item))) {
                    insertPickerRow(item);
                    break;
                }
            }
        }
    }

    private static String displayForPicker(Step3_MaterialPanelService.PickerEntry e) {
        return e.isDan ? "[Đạn] " + e.name : "[VCHC] " + e.name;
    }

    private void insertPickerRow(Step3_MaterialPanelService.PickerEntry entry) {
        String headerCol1 = entry.isDan ? "ĐẠN" : "VẬT CHẤT HẬU CẦN";
        int idx = findInsertIndexAfterSection(headerCol1);
        if (idx < 0) {
            return;
        }
        model.insertRow(idx, newDataRowFromPicker(entry));
        recalculateTT();
    }

    private int findInsertIndexAfterSection(String headerCol1) {
        for (int i = 0; i < model.getRowCount(); i++) {
            Object c1 = model.getValueAt(i, 1);
            if (c1 != null && c1.toString().trim().equals(headerCol1)) {
                int idx = i + 1;
                while (idx < model.getRowCount()) {
                    String tt = model.getValueAt(idx, 0) != null ? model.getValueAt(idx, 0).toString().trim().toUpperCase() : "";
                    if (tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                        break;
                    }
                    idx++;
                }
                return idx;
            }
        }
        return -1;
    }

    private boolean saveToDatabase() {
        int sessionId = parent.getCurrentSessionId();
        if (sessionId < 1) {
            JOptionPane.showMessageDialog(this, "Không có SessionID hợp lệ!");
            return false;
        }

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<Step3_MaterialPanelService.Step3SaveRow> rows = collectRowsForSave();
        boolean ok = service.saveStep3ToDatabase(sessionId, rows, hinhThucTapBai);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu Database!");
        }
        return ok;
    }

    private List<Step3_MaterialPanelService.Step3SaveRow> collectRowsForSave() {
        List<Step3_MaterialPanelService.Step3SaveRow> rows = new ArrayList<>();
        int currentType = 1;

        for (int i = 0; i < model.getRowCount(); i++) {
            String ttStr = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim().toUpperCase() : "";
            String vatChat = model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString().trim() : "";

            if (ttStr.equals("I") || vatChat.equals("ĐẠN")) {
                currentType = 1;
                continue;
            }
            if (ttStr.equals("II") || vatChat.equals("VẬT CHẤT HẬU CẦN")) {
                currentType = 2;
                continue;
            }
            if (ttStr.equals("III") || vatChat.equals("VẬT TƯ KỸ THUẬT")) {
                currentType = 3;
                continue;
            }

            if (vatChat.isEmpty()) {
                continue;
            }

            double[] six = new double[Step3_MaterialPanelService.PHAN_SLOT_COUNT];
            for (int j = 0; j < phanCount; j++) {
                six[j] = InputValidator.parseDoubleSafe(model.getValueAt(i, 6 + j));
            }

            rows.add(new Step3_MaterialPanelService.Step3SaveRow(
                    currentType,
                    vatChat,
                    model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "",
                    InputValidator.parseDoubleSafe(model.getValueAt(i, 4)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, 5)),
                    six,
                    model.getValueAt(i, colNote) != null ? model.getValueAt(i, colNote).toString() : ""
            ));
        }
        return rows;
    }

    private void recalculateTT() {
        int tt = 1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String val = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().toUpperCase() : "";
            if (val.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                tt = 1;
            } else {
                model.setValueAt(String.valueOf(tt++), i, 0);
            }
        }
    }

    private String formatDouble(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        }
        return String.format("%s", d).replace(".", ",");
    }

    private void initDefaultRows() {
        model.addRow(newSectionLabelRow("I", "ĐẠN"));
        model.addRow(newSectionLabelRow("II", "VẬT CHẤT HẬU CẦN"));
        model.addRow(newSectionLabelRow("III", "VẬT TƯ KỸ THUẬT"));
    }

    int resolveSectionForRow(int row) {
        for (int i = row; i >= 0; i--) {
            String tt = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim().toUpperCase() : "";
            if (tt.equals("I")) {
                return 1;
            }
            if (tt.equals("II")) {
                return 2;
            }
            if (tt.equals("III")) {
                return 3;
            }
        }
        return 2;
    }

    String lookupDvt(String name, int section) {
        if (name == null) {
            return null;
        }
        if (section == 1) {
            for (Step3_MaterialPanelService.DanOption d : danOptions) {
                if (d.name.equals(name)) {
                    return d.dvt;
                }
            }
        } else {
            for (Step3_MaterialPanelService.VchcOption v : vchcOptions) {
                if (v.name.equals(name)) {
                    return v.dvt;
                }
            }
        }
        return null;
    }

    private JPanel createAbsoluteHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalWidth = 0;
        for (int w : colWidths) {
            totalWidth += w;
        }
        p.setPreferredSize(new Dimension(totalWidth, 130));

        int[] x = new int[totalCols + 1];
        x[0] = 0;
        for (int i = 0; i < totalCols; i++) {
            x[i + 1] = x[i] + colWidths[i];
        }

        int hienCoStart = 3;
        int hienCoEndExclusive = colNote;
        int hienCoWidth = x[hienCoEndExclusive] - x[hienCoStart];

        p.add(absLabel("TT", x[0], 0, colWidths[0], 130));
        p.add(absLabel("Đạn, vật chất hậu cần, vật tư kỹ thuật", x[1], 0, colWidths[1], 130));
        p.add(absLabel("ĐVT", x[2], 0, colWidths[2], 130));
        p.add(absLabel("Hiện có", x[hienCoStart], 0, hienCoWidth, 35));
        p.add(absLabel("Ghi chú", x[colNote], 0, colWidths[colNote], 130));

        int tieuDoanW = colWidths[3] + colWidths[4] + colWidths[5];
        p.add(absLabel("Tiểu đoàn", x[3], 35, tieuDoanW, 35));

        int phanW = x[colNote] - x[6];
        p.add(absLabel("Phân cấp", x[6], 35, phanW, 35));

        p.add(absLabel("Tổng", x[3], 70, colWidths[3], 60));
        p.add(absLabel("Kho/d", x[4], 70, colWidths[4], 60));
        p.add(absLabel("Đơn vị", x[5], 70, colWidths[5], 60));

        for (int i = 0; i < phanCount; i++) {
            p.add(absLabel(phanHeaderHtml(phanLabels[i]), x[6 + i], 70, colWidths[6 + i], 60));
        }
        return p;
    }

    private static String phanHeaderHtml(String label) {
        if (label == null || label.isEmpty()) {
            return "";
        }
        if (label.length() <= 16) {
            return "<html><center>" + label + "</center></html>";
        }
        int mid = label.length() / 2;
        int sp = label.lastIndexOf(' ', mid);
        if (sp < 3) {
            sp = mid;
        }
        String a = label.substring(0, sp).trim();
        String b = label.substring(sp).trim();
        return "<html><center>" + a + "<br>" + b + "</center></html>";
    }

    private JLabel absLabel(String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        l.setBounds(x, y, w, h);
        l.setOpaque(true);
        l.setBackground(new Color(248, 250, 252));
        return l;
    }

    private class MaterialNameCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<String> combo = new JComboBox<>();
        private int editingRow = -1;

        MaterialNameCellEditor() {
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            combo.setEditable(false);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            combo.removeAllItems();

            reloadCatalogFromDb();
            int section = resolveSectionForRow(row);
            if (section == 1) {
                for (Step3_MaterialPanelService.DanOption d : danOptions) {
                    combo.addItem(d.name);
                }
            } else {
                for (Step3_MaterialPanelService.VchcOption v : vchcOptions) {
                    combo.addItem(v.name);
                }
            }

            String current = value != null ? value.toString().trim() : "";
            if (!current.isEmpty()) {
                combo.setSelectedItem(current);
                if (combo.getSelectedIndex() < 0) {
                    combo.insertItemAt(current, 0);
                    combo.setSelectedIndex(0);
                }
            }
            return combo;
        }

        @Override
        public Object getCellEditorValue() {
            Object sel = combo.getSelectedItem();
            final int row = editingRow;
            if (row >= 0 && sel != null) {
                String name = sel.toString();
                int sec = resolveSectionForRow(row);
                String dvt = lookupDvt(name, sec);
                if (dvt != null) {
                    SwingUtilities.invokeLater(() -> {
                        if (row < model.getRowCount()) {
                            model.setValueAt(dvt, row, 2);
                        }
                    });
                }
            }
            return sel != null ? sel : "";
        }
    }

    private class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
        MultiLineTableCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value != null ? value.toString() : "");
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1), new EmptyBorder(5, 5, 5, 5)));
            String tt = tbl.getValueAt(row, 0) != null ? tbl.getValueAt(row, 0).toString().trim().toUpperCase() : "";
            if (tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                setBackground(new Color(255, 241, 118));
                setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                setBackground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 15));
            }
            if (isSelected && !tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                setBackground(new Color(219, 234, 254));
            }
            return this;
        }
    }
}
