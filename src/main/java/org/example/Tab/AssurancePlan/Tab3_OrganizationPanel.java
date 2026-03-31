package org.example.Tab.AssurancePlan;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab3_OrganizationPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextArea txtViTriChinhThuc;
    private JTextArea txtViTriDuBi;

    // Các cột số cần tính tổng: SQ(1), QNCN+HSQ(2), +(3), Bộ phận HCKT(5), Dự bị(6), T.cường cho dưới(7)
    private static final int[] SUM_COLUMNS = {1, 2, 3, 5, 6, 7};

    public Tab3_OrganizationPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("III. TỔ CHỨC, SỬ DỤNG LỰC LƯỢNG, BỐ TRÍ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // ============================================================
        // 1. Tổ chức lực lượng
        // ============================================================
        mainContainer.add(UIUtils.createSectionLabel("1. Tổ chức lực lượng"));
        mainContainer.add(createTablePanel());
        mainContainer.add(Box.createVerticalStrut(25));

        // ============================================================
        // 2. Bố trí
        // ============================================================
        mainContainer.add(UIUtils.createSectionLabel("2. Bố trí"));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí chính thức:"));
        txtViTriChinhThuc = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtViTriChinhThuc, 100));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("Vị trí dự bị:"));
        txtViTriDuBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtViTriDuBi, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(900, 320));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlControls.setBackground(Color.WHITE);

        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));

        pnlControls.add(btnAdd);
        pnlControls.add(Box.createHorizontalStrut(10));
        pnlControls.add(btnDel);

        pnl.add(pnlControls, BorderLayout.NORTH);

        // Cột theo đúng mẫu doc
        String[] cols = {
                "Tên lực lượng",  // 0
                "SQ",             // 1
                "QNCN + HSQ",     // 2
                "+",              // 3
                "Khả năng",       // 4
                "Bộ phận HCKT",   // 5
                "Dự bị",          // 6
                "T.cường cho dưới" // 7
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Dòng cuối cùng (dòng Cộng) không cho sửa
                if (row == getRowCount() - 1) {
                    return false;
                }
                return true;
            }
        };

        // Thêm dòng "Cộng" mặc định
        model.addRow(new Object[]{"Cộng", "", "", "", "", "", "", ""});

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Renderer cho dòng "Cộng" — in đậm, nền khác
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (row == model.getRowCount() - 1) {
                    // Dòng Cộng
                    c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (!isSelected) {
                        c.setBackground(new Color(254, 249, 195)); // Vàng nhạt
                        c.setForeground(new Color(30, 41, 59));
                    }
                } else {
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    if (!isSelected) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(new Color(30, 41, 59));
                    }
                }

                // Căn giữa cho cột số
                if (column >= 1 && column <= 3 || column >= 5 && column <= 7) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        // Lắng nghe thay đổi dữ liệu để tự động cập nhật dòng Cộng
        model.addTableModelListener(new TableModelListener() {
            private boolean updating = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (updating) return;
                // Chỉ tính lại khi sửa dữ liệu (không phải dòng Cộng)
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    if (row < model.getRowCount() - 1) {
                        updating = true;
                        recalculateSum();
                        updating = false;
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        UIUtils.makeScrollPassThrough(scroll);

        pnl.add(scroll, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            // Không tính dòng Cộng, tối đa 10 dòng dữ liệu
            int dataRowCount = model.getRowCount() - 1;
            if (dataRowCount >= 10) {
                JOptionPane.showMessageDialog(this, "Tối đa 10 dòng để phù hợp biểu mẫu.");
                return;
            }
            // Chèn trước dòng Cộng
            int insertIndex = model.getRowCount() - 1;
            model.insertRow(insertIndex, new Object[]{"", "", "", "", "", "", "", ""});
            refreshTenLuong();
            recalculateSum();
        });

        btnDel.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1 && selectedRow < model.getRowCount() - 1) {
                // Không cho xóa dòng Cộng
                model.removeRow(selectedRow);
                refreshTenLuong();
                recalculateSum();
            } else if (selectedRow == model.getRowCount() - 1) {
                JOptionPane.showMessageDialog(this, "Không thể xóa dòng Cộng.");
            }
        });

        return pnl;
    }

    /**
     * Không tự động đánh STT số nữa, vì cột đầu là "Tên lực lượng" (text).
     * Chỉ đảm bảo dòng cuối luôn là "Cộng".
     */
    private void refreshTenLuong() {
        int lastRow = model.getRowCount() - 1;
        model.setValueAt("Cộng", lastRow, 0);
    }

    /**
     * Tự động tính tổng các cột số và cập nhật dòng Cộng.
     */
    private void recalculateSum() {
        int lastRow = model.getRowCount() - 1;
        if (lastRow < 0) return;

        for (int col : SUM_COLUMNS) {
            int sum = 0;
            boolean hasValue = false;
            for (int row = 0; row < lastRow; row++) {
                int val = parseIntSafe(getCellValue(row, col));
                if (val != 0) hasValue = true;
                sum += val;
            }
            model.setValueAt(hasValue ? String.valueOf(sum) : "", lastRow, col);
        }

        // Cột "Khả năng" (4) không tính tổng — để trống
        model.setValueAt("", lastRow, 4);
        // Cột "Tên lực lượng" (0) luôn là "Cộng"
        model.setValueAt("Cộng", lastRow, 0);
    }

    private int parseIntSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            // Thử parse số có tiền tố 0 như "03"
            try {
                return Integer.parseInt(text.trim().replaceAll("^0+", ""));
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();

        data.put("{{rows_bang_to_chuc_luc_luong}}", buildRowsHtml());
        data.put("{{vi_tri_chinh_thuc}}", txtViTriChinhThuc.getText().trim());
        data.put("{{vi_tri_du_bi}}", txtViTriDuBi.getText().trim());

        return data;
    }

    private String buildRowsHtml() {
        StringBuilder sb = new StringBuilder();

        if (model.getRowCount() <= 1) {
            // Chỉ có dòng Cộng, không có dữ liệu
            sb.append("<tr>")
                    .append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>")
                    .append("</tr>");
            return sb.toString();
        }

        // Các dòng dữ liệu (không bao gồm dòng Cộng)
        for (int i = 0; i < model.getRowCount() - 1; i++) {
            sb.append("<tr>");
            for (int j = 0; j < model.getColumnCount(); j++) {
                String cellValue = escapeHtml(getCellValue(i, j));
                if (j == 0 || j == 4) {
                    sb.append("<td class='text-left'>").append(cellValue).append("</td>");
                } else {
                    sb.append("<td>").append(cellValue).append("</td>");
                }
            }
            sb.append("</tr>");
        }

        // Dòng Cộng (dòng cuối)
        int lastRow = model.getRowCount() - 1;
        sb.append("<tr style='font-weight:bold;'>");
        for (int j = 0; j < model.getColumnCount(); j++) {
            String cellValue = escapeHtml(getCellValue(lastRow, j));
            if (j == 0) {
                sb.append("<td class='text-left'><b>").append(cellValue).append("</b></td>");
            } else {
                sb.append("<td><b>").append(cellValue).append("</b></td>");
            }
        }
        sb.append("</tr>");

        return sb.toString();
    }

    private String getCellValue(int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
