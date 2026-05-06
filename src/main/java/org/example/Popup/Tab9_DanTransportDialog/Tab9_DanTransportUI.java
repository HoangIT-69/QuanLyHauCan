package org.example.Popup.Tab9_DanTransportDialog;

import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

public class Tab9_DanTransportUI extends JDialog {

    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);
    private static final Color ROW_GRAY = new Color(241, 245, 249);
    private static final Color HOVER_COLOR = new Color(219, 234, 254);
    private static final Color DISABLED_COLOR = new Color(226, 232, 240);

    private static final String TOAN_DOI = "Toàn đội";
    private static final String TOAN_D = "Toàn d";

    private final String phaseTitle;
    private final int targetRow;
    /** Key = "huong::label" → targetRow */
    private final Map<String, Integer> currentAssignments;
    private final Consumer<Map<String, Integer>> onConfirm;
    private final List<String> directions;

    private DefaultTableModel model;
    private JTable table;
    private JComboBox<String> cmbHuong;
    /** Hướng đang được chọn trong dropdown */
    private String selectedHuong;

    public Tab9_DanTransportUI(Frame parent, String phaseTitle, int targetRow,
                                Map<String, Integer> currentAssignments,
                                List<String> directions,
                                Consumer<Map<String, Integer>> onConfirm) {
        super(parent, "Phân bổ dữ liệu Đạn - " + phaseTitle, true);
        this.phaseTitle = phaseTitle;
        this.targetRow = targetRow;
        this.currentAssignments = new HashMap<>(currentAssignments);
        this.directions = directions;
        this.onConfirm = onConfirm;
        this.selectedHuong = directions.isEmpty() ? "" : directions.get(0);

        setSize(920, 540);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // --- NORTH: tiêu đề + dropdown hướng ---
        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("PHÂN BỔ KHỐI LƯỢNG ĐẠN (TẤN) - " + phaseTitle.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        northPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);
        JLabel lblHuong = new JLabel("Chọn hướng:");
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

        // --- CENTER: bảng ---
        String weightColName = isPreparationPhase() ? "Khối lượng GĐCB" : "Khối lượng GĐCĐ";
        String[] columnNames = {"Chọn", "TT", "Tên loại đạn", weightColName};

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
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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

        // --- SOUTH: nút bấm ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(SLATE_TEXT);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SLATE_BORDER), new EmptyBorder(8, 20, 8, 20)));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(37, 99, 235));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(29, 78, 216)), new EmptyBorder(8, 25, 8, 25)));
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

    /** Đổ dữ liệu đạn theo hướng đang chọn vào bảng. */
    private void populateTable() {
        model.setRowCount(0);
        if (selectedHuong == null || selectedHuong.isEmpty()) return;

        Map<String, Map<String, Double>> toanD = org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableToanDReadOnly();
        Map<String, Map<String, Double>> dirAll = org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableDanByDirectionReadOnly().get(selectedHuong);
        Map<String, Map<String, Double>> data = dirAll != null ? dirAll : toanD;

        int stt = 1;
        Map<String, Map<String, Double>> orderedSource = toanD.isEmpty() ? data : toanD;
        for (Map.Entry<String, Map<String, Double>> entry : orderedSource.entrySet()) {
            String label = entry.getKey() != null ? entry.getKey().trim() : "";
            if (label.contains("nhóm 1") || label.contains("nhóm 2")) continue;

            Map<String, Double> values = data.get(label);
            if (values == null) {
                values = toanD.get(label);
            }
            if (values == null) continue;

            double dv  = values.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_DV,  0.0);
            double kho = values.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
            double gdcb = dv + kho;
            double gdcd = values.getOrDefault(Tab5_DanPanelService.TL_THUC_HANH, 0.0);
            double displayWeight = isPreparationPhase() ? gdcb : gdcd;

            String key = selectedHuong + "::" + label;
            Integer assignedRow = currentAssignments.get(key);
            boolean isChecked = (assignedRow != null && assignedRow == targetRow);

            model.addRow(new Object[]{isChecked, stt++, label, String.format(Locale.US, "%.2f", displayWeight)});
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
