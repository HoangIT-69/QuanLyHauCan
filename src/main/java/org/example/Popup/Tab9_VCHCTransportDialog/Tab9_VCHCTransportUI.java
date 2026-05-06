package org.example.Popup.Tab9_VCHCTransportDialog;

import org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

public class Tab9_VCHCTransportUI extends JDialog {

    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);
    private static final Color ROW_GRAY = new Color(241, 245, 249);
    private static final Color HOVER_COLOR = new Color(219, 234, 254);
    private static final Color DISABLED_COLOR = new Color(226, 232, 240);

    private static final String TOAN_DOI = "Toàn đội";
    private static final String TOAN_D = "Toàn d";

    private final String phaseTitle;
    private final int targetRow;
    private final int filterCategoryIndex;
    private final String categoryName;
    private final Map<String, Integer> currentAssignments;
    private final Consumer<Map<String, Integer>> onConfirm;
    private final List<String> directions;

    private DefaultTableModel model;
    private JTable table;
    private JComboBox<String> cmbHuong;
    private String selectedHuong;

    public Tab9_VCHCTransportUI(Frame parent, String phaseTitle, int targetRow,
                                int filterCategoryIndex,
                                Map<String, Integer> currentAssignments,
                                List<String> directions,
                                Consumer<Map<String, Integer>> onConfirm) {
        super(parent, "", true);
        this.phaseTitle = phaseTitle;
        this.targetRow = targetRow;
        this.filterCategoryIndex = filterCategoryIndex;
        this.currentAssignments = new HashMap<>(currentAssignments);
        this.directions = directions;
        this.onConfirm = onConfirm;
        this.selectedHuong = directions.isEmpty() ? "" : directions.get(0);

        if (filterCategoryIndex == 0) this.categoryName = "QUAN NHU";
        else if (filterCategoryIndex == 1) this.categoryName = "QUAN Y";
        else if (filterCategoryIndex == 2) this.categoryName = "DOANH TRAI";
        else if (filterCategoryIndex == 3) this.categoryName = "VTKT";
        else this.categoryName = "VAT CHAT HAU CAN";

        setTitle("Phan bo du lieu - " + this.categoryName + " - " + phaseTitle);
        setSize(920, 540);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("PHAN BO KHOI LUONG " + categoryName + " (TAN) - " + phaseTitle.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        northPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);
        JLabel lblHuong = new JLabel("Chon huong:");
        lblHuong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHuong.setForeground(SLATE_TEXT);
        cmbHuong = new JComboBox<>(directions.toArray(new String[0]));
        cmbHuong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbHuong.setPreferredSize(new Dimension(220, 32));
        if (!directions.isEmpty()) cmbHuong.setSelectedIndex(0);
        filterPanel.add(lblHuong);
        filterPanel.add(cmbHuong);
        northPanel.add(filterPanel, BorderLayout.SOUTH);

        mainPanel.add(northPanel, BorderLayout.NORTH);

        String weightColName = isPreparationPhase() ? "Khoi luong GDCB" : "Khoi luong GDCD";
        String[] columnNames = {"Chon", "TT", "Ten vat chat", weightColName};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 0) return false;
                String label = getValueAt(row, 2).toString().trim();
                String key = selectedHuong + "::" + label;
                Integer assignedRow = currentAssignments.get(key);
                if (assignedRow != null && assignedRow != targetRow) return false;
                // Khóa xung đột Toàn đội ↔ hướng cụ thể
                return !isLockedByConflict(label);
            }
        };

        model.addTableModelListener(e -> {
            if (e.getType() != javax.swing.event.TableModelEvent.UPDATE) return;
            if (e.getColumn() != 0) return;
            int row = e.getFirstRow();
            if (row < 0 || row >= model.getRowCount()) return;
            if (selectedHuong == null || selectedHuong.isEmpty()) return;

            String label = model.getValueAt(row, 2).toString().trim();
            String key = selectedHuong + "::" + label;
            boolean isChecked = Boolean.TRUE.equals(model.getValueAt(row, 0));
            if (isChecked) {
                currentAssignments.put(key, targetRow);
            } else {
                Integer assignedRow = currentAssignments.get(key);
                if (assignedRow != null && assignedRow == targetRow) {
                    currentAssignments.remove(key);
                }
            }
            if (table != null) {
                table.repaint();
            }
        });

        populateTable();

        cmbHuong.addActionListener(e -> {
            selectedHuong = (String) cmbHuong.getSelectedItem();
            populateTable();
        });

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(ROW_GRAY);
        table.getTableHeader().setForeground(SLATE_TEXT);
        table.getTableHeader().setBorder(new LineBorder(SLATE_BORDER));
        table.setSelectionBackground(HOVER_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(SLATE_BORDER);
        table.setShowGrid(true);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String label = t.getValueAt(row, 2).toString().trim();
                String key = selectedHuong + "::" + label;
                Integer assignedRow = currentAssignments.get(key);
                boolean lockedByOtherRow = assignedRow != null && assignedRow != targetRow;
                boolean lockedByConflict = isLockedByConflict(label);
                if (lockedByOtherRow || lockedByConflict) {
                    c.setBackground(DISABLED_COLOR);
                    c.setForeground(Color.GRAY);
                } else {
                    c.setBackground(isSelected ? HOVER_COLOR : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                if (column == 1 || column == 3) setHorizontalAlignment(SwingConstants.CENTER);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(customRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(customRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(customRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(SLATE_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnCancel = new JButton("Huy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(SLATE_TEXT);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SLATE_BORDER), new EmptyBorder(8, 20, 8, 20)));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("Xac nhan");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(37, 99, 235));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(29, 78, 216)), new EmptyBorder(8, 25, 8, 25)));
        btnConfirm.addActionListener(e -> confirmSelection());

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnConfirm);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    /**
     * Kiểm tra xem vật chất (label) có bị khóa do xung đột Toàn đội/Hướng không.
     * - Đang ở Toàn đội: khóa nếu đã tích ở bất kỳ hướng cụ thể nào.
     * - Đang ở hướng cụ thể: khóa nếu đã tích ở Toàn đội.
     */
    private boolean isLockedByConflict(String label) {
        if (isToanDoiScope(selectedHuong)) {
            return hasAssignmentForLabelInScope(label, false);
        }
        return hasAssignmentForLabelInScope(label, true);
    }

    private boolean isToanDoiScope(String huong) {
        if (huong == null) return false;
        String h = huong.trim();
        return TOAN_DOI.equalsIgnoreCase(h) || TOAN_D.equalsIgnoreCase(h);
    }

    private boolean hasAssignmentForLabelInScope(String label, boolean toanDoiScope) {
        String suffix = "::" + label;
        for (Map.Entry<String, Integer> e : currentAssignments.entrySet()) {
            if (e.getValue() == null || e.getValue() != targetRow) continue;
            String key = e.getKey();
            if (key == null || !key.endsWith(suffix)) continue;
            int sep = key.indexOf("::");
            String huong = sep >= 0 ? key.substring(0, sep) : "";
            boolean isToanDoiEntry = isToanDoiScope(huong);
            if (toanDoiScope == isToanDoiEntry) {
                return true;
            }
        }
        return false;
    }

    private void populateTable() {
        model.setRowCount(0);
        if (selectedHuong == null || selectedHuong.isEmpty()) return;

        Map<String, Map<String, Double>> toanD = Tab5_VatChatPanelService.getMiniTableVCHCReadOnly();
        Map<String, Map<String, Double>> dirData = Tab5_VatChatPanelService.getMiniTableVCHCByDirectionReadOnly().get(selectedHuong);
        Map<String, Map<String, Double>> data = (dirData != null && !dirData.isEmpty()) ? dirData : toanD;

        int stt = 1;
        Map<String, Map<String, Double>> orderedSource = toanD.isEmpty() ? data : toanD;
        for (Map.Entry<String, Map<String, Double>> entry : orderedSource.entrySet()) {
            String label = entry.getKey() != null ? entry.getKey().trim() : "";
            Map<String, Double> toanDValues = toanD.get(label);
            if (toanDValues == null) continue;
            double catIdx = toanDValues.getOrDefault("CATEGORY", -1.0);
            if (Math.round(catIdx) != filterCategoryIndex) continue;

            Map<String, Double> values = data.get(label);
            if (values == null) {
                values = toanDValues;
            }
            double gdcbKho = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_KHO, 0.0);
            double gdcbDv  = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_DV_D, 0.0);
            double gdcdKho = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_KHO, 0.0);
            double gdcdDv  = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_DV, 0.0);
            // Theo nghiệp vụ: ở Hướng thì Kho = 0 (chỉ lấy ĐV); Toàn đội mới lấy Kho + ĐV.
            boolean isToanDoi = isToanDoiScope(selectedHuong);
            double gdcb = isToanDoi ? (gdcbKho + gdcbDv) : gdcbDv;
            double gdcd = isToanDoi ? (gdcdKho + gdcdDv) : gdcdDv;
            double displayWeight = isPreparationPhase() ? gdcb : gdcd;

            String key = selectedHuong + "::" + label;
            Integer assignedRow = currentAssignments.get(key);
            boolean isChecked = (assignedRow != null && assignedRow == targetRow);

            model.addRow(new Object[]{isChecked, stt++, label, String.format(Locale.US, "%.3f", displayWeight)});
        }
    }

    private void confirmSelection() {
        // Chỉ xóa các keys của hướng này mà đang trỏ về targetRow hiện tại
        String prefix = selectedHuong + "::";
        currentAssignments.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix) && entry.getValue() == targetRow);

        for (int i = 0; i < model.getRowCount(); i++) {
            boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (isChecked) {
                String label = model.getValueAt(i, 2).toString().trim();
                currentAssignments.put(selectedHuong + "::" + label, targetRow);
            }
        }

        if (onConfirm != null) {
            onConfirm.accept(currentAssignments);
        }
        dispose();
    }

    private boolean isPreparationPhase() {
        if (phaseTitle == null) return false;
        String s = phaseTitle.toLowerCase(Locale.ROOT);
        return s.contains("chuẩn bị") || s.contains("chuan bi");
    }
}
