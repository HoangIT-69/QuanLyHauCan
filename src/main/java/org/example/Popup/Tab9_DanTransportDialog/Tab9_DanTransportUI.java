package org.example.Popup.Tab9_DanTransportDialog;

import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    private String phaseTitle;
    private int targetRow;
    private Map<String, Integer> currentAssignments;
    private Consumer<Map<String, Integer>> onConfirm;
    
    private DefaultTableModel model;
    private JTable table;

    public Tab9_DanTransportUI(Frame parent, String phaseTitle, int targetRow, Map<String, Integer> currentAssignments, Consumer<Map<String, Integer>> onConfirm) {
        super(parent, "Phân bổ dữ liệu Đạn - " + phaseTitle, true);
        this.phaseTitle = phaseTitle;
        this.targetRow = targetRow;
        this.currentAssignments = new HashMap<>(currentAssignments); // Work on a copy
        this.onConfirm = onConfirm;
        
        setSize(850, 500);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // Tiêu đề
        JLabel lblTitle = new JLabel("PHÂN BỔ KHỐI LƯỢNG ĐẠN (TẤN) - " + phaseTitle.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Bảng dữ liệu
        String weightColName = phaseTitle.contains("chuẩn bị") ? "Khối lượng GĐCB" : "Khối lượng GĐCĐ";
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
                Integer assignedRow = currentAssignments.get(label);
                // Cannot edit if assigned to a different row
                if (assignedRow != null && assignedRow != targetRow) {
                    return false;
                }
                return true;
            }
        };

        Map<String, Map<String, Double>> data = Tab5_DanPanelService.getMiniTableToanDReadOnly();
        int stt = 1;
        for (Map.Entry<String, Map<String, Double>> entry : data.entrySet()) {
            String label = entry.getKey().trim();
            if (label.equalsIgnoreCase("Đạn BB nhóm 1") || label.equalsIgnoreCase("Đạn BB nhóm 2") || 
                label.contains("nhóm 1") || label.contains("nhóm 2")) {
                continue; // Bỏ qua các dòng tổng phân loại nhóm đạn
            }
            
            Map<String, Double> values = entry.getValue();
            
            double dv = values.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_DV, 0.0);
            double kho = values.getOrDefault(Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
            double gdcb = dv + kho;
            double gdcd = values.getOrDefault(Tab5_DanPanelService.TL_THUC_HANH, 0.0);
            
            double displayWeight = phaseTitle.contains("chuẩn bị") ? gdcb : gdcd;
            
            // Nếu bằng 0 thì bỏ qua không cần phân bổ (tùy chọn, ở đây cứ hiện ra cũng được)
            
            Integer assignedRow = currentAssignments.get(label);
            boolean isChecked = (assignedRow != null && assignedRow == targetRow);
            
            model.addRow(new Object[]{
                    isChecked,
                    stt++,
                    label,
                    String.format(Locale.US, "%.2f", displayWeight)
            });
        }

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

        // Canh lề và Renderer cho màu sắc
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String label = table.getValueAt(row, 2).toString().trim();
                Integer assignedRow = currentAssignments.get(label);
                
                if (assignedRow != null && assignedRow != targetRow) {
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

        table.getColumnModel().getColumn(1).setCellRenderer(customRenderer); // TT
        table.getColumnModel().getColumn(2).setCellRenderer(customRenderer); // Tên loại
        table.getColumnModel().getColumn(3).setCellRenderer(customRenderer); // Khối lượng

        // Set độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(SLATE_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Nút bấm
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(SLATE_TEXT);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SLATE_BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(37, 99, 235)); // Blue
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(29, 78, 216)),
                new EmptyBorder(8, 25, 8, 25)
        ));
        btnConfirm.addActionListener(e -> confirmSelection());

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnConfirm);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void confirmSelection() {
        // Cập nhật lại Map assignments
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean isChecked = (Boolean) model.getValueAt(i, 0);
            String label = model.getValueAt(i, 2).toString().trim();
            
            Integer assignedRow = currentAssignments.get(label);
            if (isChecked) {
                currentAssignments.put(label, targetRow);
            } else if (assignedRow != null && assignedRow == targetRow) {
                // Chỉ xóa nếu nó đang được gán cho targetRow hiện tại (bỏ tick)
                currentAssignments.remove(label);
            }
        }
        
        if (onConfirm != null) {
            onConfirm.accept(currentAssignments);
        }
        dispose();
    }
}
