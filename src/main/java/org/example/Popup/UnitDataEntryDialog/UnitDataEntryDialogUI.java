package org.example.Popup.UnitDataEntryDialog;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class UnitDataEntryDialogUI extends JDialog {

    private final UnitDataEntryDialogService unitService = new UnitDataEntryDialogService();
    private final int[] colWidths = {300, 80, 60, 60, 60, 60, 80, 70, 60, 60, 60, 70, 70, 70, 70};
    private final int sessionId;
    private final List<String> allDirections;

    private String activeUnitName;
    private DefaultTableModel model;
    private JTable table;
    private boolean isUpdatingSum = false;
    private final JLabel lblMainTitle;

    public UnitDataEntryDialogUI(Frame owner, String unitName, int sessionId, List<String> allDirections) {
        super(owner, "Khai báo dữ liệu: " + unitName, true);
        this.activeUnitName = unitName;
        this.sessionId = sessionId;
        this.allDirections = allDirections;

        System.out.println("DEBUG: UnitDataEntryDialogUI SessionID = " + sessionId + " | Hướng: " + unitName);

        setSize(1350, 850);
        setLocationRelativeTo(owner);

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        lblMainTitle = new JLabel("TỔ CHỨC BIÊN CHẾ, TRANG BỊ " + unitName.toUpperCase(), SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        String[] columnNames = {"LỰC LƯỢNG", "QUÂN SỐ", "SN", "TL", "TrL", "ĐL", "B41, M79", "Lựu đạn", "60", "82", "100", "SPG9", "12.7", "ON", "ĐB"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row < 0 || row >= getRowCount()) {
                    return false;
                }
                Object val = getValueAt(row, 0);
                if (val == null) {
                    return true;
                }
                String unitStr = val.toString().trim();
                return !unitStr.startsWith("1.") && !unitStr.startsWith("2.") && !unitStr.equals("TỔNG CỘNG") && column != 0;
            }
        };

        table = new JTable(model);
        table.setRowHeight(38);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(UIUtils.getStandardTableRenderer());
        }

        model.addTableModelListener(e -> {
            if (!isUpdatingSum && e.getType() == TableModelEvent.UPDATE && e.getColumn() > 0) {
                updateAutoSums();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(17, 100));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlControls.setBackground(Color.WHITE);

        JButton btnAddFromDB = UIUtils.createStyledButton("➕ Thêm từ Danh mục", new Color(41, 128, 185));
        JButton btnDelRow = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        JLabel lblHelp = new JLabel("<html><b>HD:</b> 1. Chọn dòng vàng -> 2. Bấm Thêm để chọn đơn vị từ DB.</html>");

        pnlControls.add(btnAddFromDB);
        pnlControls.add(btnDelRow);
        pnlControls.add(lblHelp);

        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnReset = UIUtils.createNavButton("Làm mới", Color.ORANGE);
        JButton btnSave = UIUtils.createNavButton("Lưu & Đóng", new Color(34, 197, 94));
        pnlSouth.add(btnReset);
        pnlSouth.add(btnSave);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(pnlControls, BorderLayout.NORTH);
        centerPanel.add(combinedPanel, BorderLayout.CENTER);

        container.add(centerPanel, BorderLayout.CENTER);
        container.add(pnlSouth, BorderLayout.SOUTH);

        btnAddFromDB.addActionListener(e -> handleAddFromDB());
        btnDelRow.addActionListener(e -> handleDeleteRow());
        btnSave.addActionListener(e -> {
            saveCurrentData();
            dispose();
        });
        btnReset.addActionListener(e -> resetData());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.add(new JLabel("Chuyển hướng / lực lượng:"));
        JComboBox<String> cbDirection = new JComboBox<>(new Vector<>(allDirections));
        cbDirection.setPreferredSize(new Dimension(360, 32));
        cbDirection.addActionListener(e -> {
            String sel = (String) cbDirection.getSelectedItem();
            if (sel == null || sel.equals(activeUnitName)) {
                return;
            }
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            snapshotTableToRam(activeUnitName);
            activeUnitName = sel;
            setTitle("Khai báo dữ liệu: " + activeUnitName);
            lblMainTitle.setText("TỔ CHỨC BIÊN CHẾ, TRANG BỊ " + activeUnitName.toUpperCase());
            loadUnitData(activeUnitName);
        });
        pnlTop.add(cbDirection);

        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setBackground(Color.WHITE);
        northStack.add(pnlTop);
        northStack.add(Box.createVerticalStrut(8));
        northStack.add(lblMainTitle);
        container.add(northStack, BorderLayout.NORTH);

        loadUnitData(activeUnitName);
        cbDirection.setSelectedItem(unitName);

        add(container);
    }

    private void snapshotTableToRam(String unit) {
        Vector<Vector<Object>> data = new Vector<>();
        for (Object row : model.getDataVector()) {
            data.add(new Vector<>((Vector<?>) row));
        }
        UnitDataEntryDialogService.getSharedStore().put(unit, data);
    }

    private void handleAddFromDB() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn dòng tiêu đề vàng (1. hoặc 2.) trước!");
            return;
        }
        String header = model.getValueAt(row, 0).toString().trim();
        if (!header.startsWith("1.") && !header.startsWith("2.")) {
            JOptionPane.showMessageDialog(this, "Hãy chọn đúng dòng Tiêu đề vàng!");
            return;
        }

        Set<String> addedUnits = new HashSet<>(unitService.loadUsedTenDonViAcrossSession(sessionId));

        for (Vector<Vector<Object>> allData : UnitDataEntryDialogService.getSharedStore().values()) {
            for (Vector<Object> r : allData) {
                if (r.isEmpty() || r.get(0) == null) {
                    continue;
                }
                String name = r.get(0).toString().replace("  + ", "").trim();
                if (name.contains("]")) {
                    addedUnits.add(name.substring(name.indexOf("]") + 1).trim());
                }
            }
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null) {
                String name = val.toString().replace("  + ", "").trim();
                if (name.contains("]")) {
                    addedUnits.add(name.substring(name.indexOf("]") + 1).trim());
                }
            }
        }

        List<Map<String, Object>> dbUnits = unitService.loadAvailableBiencheNotIn(addedUnits);
        if (dbUnits.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả đơn vị đã được sử dụng!");
            return;
        }

        String[] options = dbUnits.stream().map(m -> "[" + m.get("nhom") + "] " + m.get("name")).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Chọn đơn vị:", "Danh mục", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (sel != null) {
            for (Map<String, Object> d : dbUnits) {
                String checkName = "[" + d.get("nhom") + "] " + d.get("name");
                if (sel.equals(checkName)) {
                    model.insertRow(row + 1, new Object[]{
                            "  + " + checkName, d.get("qs"), d.get("sn"), d.get("tl"), d.get("trl"),
                            d.get("dl"), d.get("b41"), d.get("ld"), d.get("c60"), d.get("c82"),
                            d.get("c100"), d.get("spg9"), d.get("smpk"), 0, 0
                    });
                    break;
                }
            }
            updateAutoSums();
        }
    }

    private void handleDeleteRow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }
        String name = model.getValueAt(row, 0).toString().trim();
        if (name.startsWith("1.") || name.startsWith("2.") || name.equals("TỔNG CỘNG")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa dòng này?") == JOptionPane.YES_OPTION) {
            model.removeRow(row);
            updateAutoSums();
        }
    }

    private void updateAutoSums() {
        if (isUpdatingSum || model.getRowCount() == 0) {
            return;
        }

        isUpdatingSum = true;
        try {
            int totalRow = -1, rowVang1 = -1, rowVang2 = -1;
            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 0);
                if (val == null) {
                    continue;
                }
                String s = val.toString();
                if (s.startsWith("1.")) {
                    rowVang1 = i;
                } else if (s.startsWith("2.")) {
                    rowVang2 = i;
                } else if (s.equals("TỔNG CỘNG")) {
                    totalRow = i;
                }
            }

            if (totalRow == -1) {
                return;
            }

            for (int col = 1; col < model.getColumnCount(); col++) {
                int sum1 = 0, sum2 = 0;
                for (int r = 0; r < totalRow; r++) {
                    Object nameObj = model.getValueAt(r, 0);
                    if (nameObj == null) {
                        continue;
                    }

                    String name = nameObj.toString();
                    if (name.startsWith("1.") || name.startsWith("2.")) {
                        continue;
                    }

                    int val = InputValidator.parseIntSafe(model.getValueAt(r, col));

                    if (rowVang1 != -1 && r > rowVang1 && (rowVang2 == -1 || r < rowVang2)) {
                        sum1 += val;
                    } else if (rowVang2 != -1 && r > rowVang2) {
                        sum2 += val;
                    }
                }

                if (rowVang1 != -1) {
                    model.setValueAt(sum1, rowVang1, col);
                }
                if (rowVang2 != -1) {
                    model.setValueAt(sum2, rowVang2, col);
                }
                model.setValueAt(sum1 + sum2, totalRow, col);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính toán: " + e.getMessage());
        } finally {
            isUpdatingSum = false;
        }
    }

    private void saveCurrentData() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        Vector<Vector<Object>> data = new Vector<>();
        for (Object row : model.getDataVector()) {
            data.add(new Vector<>((Vector<?>) row));
        }
        UnitDataEntryDialogService.getSharedStore().put(activeUnitName, data);
    }

    private void loadUnitData(String unitName) {
        isUpdatingSum = true;
        model.setRowCount(0);

        Object[] header1 = {"1. Trong biên chế", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Object[] header2 = {"2. LL tăng cường", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Object[] footer = {"TỔNG CỘNG", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        UnitDataEntryDialogService.LoadedUnitData loaded = unitService.loadUnitFromDatabase(sessionId, unitName);
        Map<String, Vector<Vector<Object>>> ram = UnitDataEntryDialogService.getSharedStore();

        if (ram.containsKey(unitName) && !ram.get(unitName).isEmpty()) {
            for (Vector<Object> row : ram.get(unitName)) {
                model.addRow(row);
            }
        } else {
            model.addRow(header1);
            for (Object[] r : loaded.listBienChe) {
                model.addRow(r);
            }
            model.addRow(header2);
            for (Object[] r : loaded.listTangCuong) {
                model.addRow(r);
            }
            model.addRow(footer);
        }

        isUpdatingSum = false;
        updateAutoSums();
    }

    private JPanel createAbsoluteHeader() {
        int totalWidth = 0;
        for (int w : colWidths) {
            totalWidth += w;
        }
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(totalWidth, 100));
        int[] x = new int[16];
        x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) {
            x[i + 1] = x[i] + colWidths[i];
        }

        p.add(UIUtils.createAbsoluteHeaderLabel("LỰC LƯỢNG", x[0], 0, colWidths[0], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>QUÂN<br>SỐ</center></html>", x[1], 0, colWidths[1], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("TRANG BỊ KỸ THUẬT", x[2], 0, x[13] - x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("VẬT TƯ KỸ THUẬT", x[13], 0, x[15] - x[13], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vũ khí bộ binh", x[2], 30, x[7] - x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Lựu đạn", x[7], 30, colWidths[7], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cối", x[8], 30, x[11] - x[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PCT", x[11], 30, colWidths[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Pháo PK", x[12], 30, colWidths[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ON", x[13], 30, colWidths[13], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐB", x[14], 30, colWidths[14], 70));

        String[] subCols = {"SN", "TL", "TrL", "ĐL", "B41, M79", "60", "82", "100", "SPG9", "12.7"};
        int[] subX = {2, 3, 4, 5, 6, 8, 9, 10, 11, 12};
        for (int i = 0; i < subCols.length; i++) {
            p.add(UIUtils.createAbsoluteHeaderLabel(subCols[i], x[subX[i]], 60, colWidths[subX[i]], 40));
        }
        return p;
    }

    private void resetData() {
        if (JOptionPane.showConfirmDialog(this, "Làm mới toàn bộ bảng?") == JOptionPane.YES_OPTION) {
            UnitDataEntryDialogService.getSharedStore().remove(activeUnitName);
            loadUnitData(activeUnitName);
        }
    }
}
