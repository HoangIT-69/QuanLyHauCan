package org.example.Popup.Tab9_VCHCTransportDialog;

import org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService;

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

public class Tab9_VCHCTransportUI extends JDialog {

    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);
    private static final Color ROW_GRAY = new Color(241, 245, 249);
    private static final Color HOVER_COLOR = new Color(219, 234, 254);
    private static final Color DISABLED_COLOR = new Color(226, 232, 240);

    private String phaseTitle;
    private int targetRow;
    private int filterCategoryIndex; // 0=QN, 1=QY, 2=DT, 3=VTKT
    private String categoryName;
    private Map<String, Integer> currentAssignments;
    private Consumer<Map<String, Integer>> onConfirm;
    
    private DefaultTableModel model;
    private JTable table;

    public Tab9_VCHCTransportUI(Frame parent, String phaseTitle, int targetRow, int filterCategoryIndex, Map<String, Integer> currentAssignments, Consumer<Map<String, Integer>> onConfirm) {
        super(parent, "", true);
        this.phaseTitle = phaseTitle;
        this.targetRow = targetRow;
        this.filterCategoryIndex = filterCategoryIndex;
        this.currentAssignments = new HashMap<>(currentAssignments); // Work on a copy
        this.onConfirm = onConfirm;
        
        if (filterCategoryIndex == 0) this.categoryName = "QUÂN NHU";
        else if (filterCategoryIndex == 1) this.categoryName = "QUÂN Y";
        else if (filterCategoryIndex == 2) this.categoryName = "DOANH TRẠI";
        else if (filterCategoryIndex == 3) this.categoryName = "VTKT";
        else this.categoryName = "VẬT CHẤT HẬU CẦN";

        setTitle("Phân bổ dữ liệu - " + this.categoryName + " - " + phaseTitle);
        setSize(850, 500);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // Tiêu đề
        JLabel lblTitle = new JLabel("PHÂN BỔ KHỐI LƯỢNG " + categoryName + " (TẤN) - " + phaseTitle.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Bảng dữ liệu
        String weightColName = phaseTitle.contains("chuẩn bị") ? "Khối lượng GĐCB" : "Khối lượng GĐCĐ";
        String[] columnNames = {"Chọn", "TT", "Tên vật chất", weightColName};
        
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

        Map<String, Map<String, Double>> data = Tab5_VatChatPanelService.getMiniTableVCHCReadOnly();
        int stt = 1;
        for (Map.Entry<String, Map<String, Double>> entry : data.entrySet()) {
            Map<String, Double> values = entry.getValue();
            
            // Chỉ lấy các vật chất có category trùng khớp với cột đang click
            double catIdx = values.getOrDefault("CATEGORY", -1.0);
            if (Math.round(catIdx) != filterCategoryIndex) continue;
            
            String label = entry.getKey().trim();
            
            double gdcbKho = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_KHO, 0.0);
            double gdcbDv = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_DV_D, 0.0);
            double gdcdKho = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_KHO, 0.0);
            double gdcdDv = values.getOrDefault(Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_DV, 0.0);
            
            double gdcb = gdcbKho + gdcbDv;
            double gdcd = gdcdKho + gdcdDv;
            
            double displayWeight = phaseTitle.contains("chuẩn bị") ? gdcb : gdcd;
            
            Integer assignedRow = currentAssignments.get(label);
            boolean isChecked = (assignedRow != null && assignedRow == targetRow);
            
            model.addRow(new Object[]{
                    isChecked,
                    stt++,
                    label,
                    String.format(Locale.US, "%.3f", displayWeight)
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
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean isChecked = (Boolean) model.getValueAt(i, 0);
            String label = model.getValueAt(i, 2).toString().trim();
            
            Integer assignedRow = currentAssignments.get(label);
            if (isChecked) {
                currentAssignments.put(label, targetRow);
            } else if (assignedRow != null && assignedRow == targetRow) {
                currentAssignments.remove(label);
            }
        }
        
        if (onConfirm != null) {
            onConfirm.accept(currentAssignments);
        }
        dispose();
    }
}
