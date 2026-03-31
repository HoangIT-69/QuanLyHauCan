package org.example.Popup;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RegulationDetailDialog extends JDialog {

    public static final Map<String, String[]> detailDataStore = new HashMap<>();

    private DefaultTableModel model;
    private JTable table;
    private DefaultTableModel parentModel;
    private int targetColumn;
    private String tableName;
    private boolean isUpdatingSum = false; // Cờ chống lặp vô hạn khi tự động cập nhật bảng

    private final int[] colWidths = {300, 80, 80, 90, 90, 100, 100, 100, 100};

    public RegulationDetailDialog(Frame owner, String tableName, DefaultTableModel parentModel, int targetColumn) {
        super(owner, "Khai báo chi tiết: " + tableName, true);
        this.parentModel = parentModel;
        this.targetColumn = targetColumn;
        this.tableName = tableName;

        setSize(1100, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel(tableName.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = {"Đạn, VC hậu cần, vật tư kỹ thuật", "ĐVT", "Tổng", "Kho", "Đơn vị", "Hướng CY", "Hướng TY", "PN Sau", "LL Còn lại"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3; // Chỉ cho phép nhập từ cột Kho trở đi
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null);
        table.setShowGrid(true); // Bật Grid để kẻ ô không bị mất
        table.setGridColor(new Color(224, 224, 224));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ÁP DỤNG RENDERER ĐỂ BÔI XÁM CÁC CỘT KHÔNG ĐƯỢC NHẬP
        setupCustomRenderers(columnNames.length);

        // ========================================================
        // TÍNH TỔNG TỰ ĐỘNG NGAY KHI GÕ (REAL-TIME)
        // ========================================================
        model.addTableModelListener(e -> {
            if (!isUpdatingSum && e.getType() == TableModelEvent.UPDATE && e.getColumn() >= 3) {
                isUpdatingSum = true;
                int row = e.getFirstRow();
                double rowSum = 0;

                // Cộng từ cột Kho(3) đến LL Còn Lại(8)
                for (int c = 3; c <= 8; c++) {
                    rowSum += InputValidator.parseDoubleSafe(model.getValueAt(row, c));
                }

                // Ghi vào cột Tổng (2), làm đẹp số nếu là số chẵn
                String strSum = (rowSum == (long) rowSum) ? String.format("%d", (long) rowSum) : String.valueOf(rowSum);
                model.setValueAt(strSum, row, 2);

                isUpdatingSum = false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel headerPanel = createComplexHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
            headerViewport.setViewPosition(new Point(e.getValue(), 0));
        });

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
        JButton btnRefresh = UIUtils.createStyledButton("🔄 Làm mới từ Tab 1", new Color(149, 165, 166));
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
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            saveDataToParent();
            dispose();
        });
        btnClose.addActionListener(e -> dispose());

        pnlSouth.add(btnClose); pnlSouth.add(btnSave);
        add(pnlSouth, BorderLayout.SOUTH);

        syncDataFromParent();
    }

    private void setupCustomRenderers(int colCount) {
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));

                if (col < 3) {
                    // Cột Tên, ĐVT, Tổng -> Tô xám nhạt để nhận biết không sửa được
                    c.setBackground(new Color(241, 245, 249));
                    c.setForeground(new Color(71, 85, 105));
                    if (col == 2) {
                        // Nhấn mạnh màu đỏ in đậm cho cột Tổng
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setForeground(new Color(192, 57, 43));
                    } else {
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else {
                    // Cột có thể nhập -> Trắng
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                // Hiệu ứng chọn chỉ áp dụng cho các cột có thể sửa
                if (isSelected && col >= 3) c.setBackground(new Color(219, 234, 254));
                return c;
            }
        };

        for (int i = 0; i < colCount; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }

    /**
     * Hàm kiểm tra dòng vàng giống như ở bảng cha
     */
    private boolean isHeaderRow(String text) {
        return text.equals("ĐẠN") || text.equals("VẬT CHẤT HẬU CẦN") || text.equals("VẬT TƯ KỸ THUẬT");
    }

    // ĐÃ SỬA: Đọc chỉ số cột theo cấu trúc mới (Cột 0: Tên, Cột 1: ĐVT)
    private void syncDataFromParent() {
        model.setRowCount(0);
        for (int i = 0; i < parentModel.getRowCount(); i++) {
            Object valTen = parentModel.getValueAt(i, 0);
            if (valTen == null) continue;

            String ten = valTen.toString().trim();

            // Nếu không phải là dòng tiêu đề vàng thì mới đẩy vào bảng chi tiết
            if (!isHeaderRow(ten)) {
                String dvt = parentModel.getValueAt(i, 1) != null ? parentModel.getValueAt(i, 1).toString().trim() : "";
                String tongParent = parentModel.getValueAt(i, targetColumn) != null ? parentModel.getValueAt(i, targetColumn).toString() : "0";

                String key = ten + "_" + targetColumn;
                if (detailDataStore.containsKey(key)) {
                    String[] saved = detailDataStore.get(key);
                    model.addRow(new Object[]{ten, dvt, tongParent, saved[0], saved[1], saved[2], saved[3], saved[4], saved[5]});
                } else {
                    model.addRow(new Object[]{ten, dvt, tongParent, "0", "0", "0", "0", "0", "0"});
                }
            }
        }
    }

    // ĐÃ SỬA: Ghi ngược dữ liệu vào đúng vị trí của bảng cha (so sánh cột 0)
    private void saveDataToParent() {
        for (int i = 0; i < model.getRowCount(); i++) {
            String ten = model.getValueAt(i, 0).toString();
            String[] details = new String[6];

            for (int col = 3; col <= 8; col++) {
                String val = model.getValueAt(i, col) != null ? model.getValueAt(i, col).toString().replace(",", ".") : "0";
                details[col - 3] = val;
            }

            detailDataStore.put(ten + "_" + targetColumn, details);

            // Cột tổng (index 2) đã được auto-sum tính sẵn, chỉ việc lấy ra
            String totalStr = model.getValueAt(i, 2).toString();

            // Cập nhật lại cột Target tương ứng trong bảng cha
            for (int j = 0; j < parentModel.getRowCount(); j++) {
                Object parentVal = parentModel.getValueAt(j, 0);
                if (parentVal != null && parentVal.toString().trim().equals(ten)) {
                    parentModel.setValueAt(totalStr, j, targetColumn);
                    break;
                }
            }
        }
    }

    private JPanel createComplexHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalW = 0; for(int width : colWidths) totalW += width;
        p.setPreferredSize(new Dimension(totalW, 100));

        int[] x = new int[colWidths.length + 1]; x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) x[i+1] = x[i] + colWidths[i];

        int wPhanCap = colWidths[5] + colWidths[6] + colWidths[7] + colWidths[8];

        p.add(UIUtils.createAbsoluteHeaderLabel("Đạn, VC hậu cần, vật tư kỹ thuật", x[0], 0, colWidths[0], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐVT", x[1], 0, colWidths[1], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[2], 0, colWidths[2], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[3], 0, colWidths[3], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("Đơn vị", x[4], 0, colWidths[4], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phân cấp", x[5], 0, wPhanCap, 50));
        p.add(UIUtils.createAbsoluteHeaderLabel("Hướng CY", x[5], 50, colWidths[5], 50));
        p.add(UIUtils.createAbsoluteHeaderLabel("Hướng TY", x[6], 50, colWidths[6], 50));
        p.add(UIUtils.createAbsoluteHeaderLabel("PN Sau", x[7], 50, colWidths[7], 50));
        p.add(UIUtils.createAbsoluteHeaderLabel("LL Còn lại", x[8], 50, colWidths[8], 50));
        return p;
    }
}