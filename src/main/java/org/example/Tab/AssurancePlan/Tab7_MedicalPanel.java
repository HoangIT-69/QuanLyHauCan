package org.example.Tab.AssurancePlan;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab7_MedicalPanel extends JPanel {

    private DefaultTableModel modelQuanY;
    private JTable tblQuanY;
    private JTextArea txtCanDoi;
    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;
    private JTextArea txtVeSinh;

    // Bảng màu sang trọng
    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);

    public Tab7_MedicalPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30)); // Padding rộng rãi

        JLabel lblTitle = new JLabel("VII. BẢO ĐẢM QUÂN Y");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25)); // Khoảng cách

        // 1. Bảng Tỷ lệ TBBB
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tỷ lệ TBBB"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createQuanYTable());
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Cân đối
        mainContainer.add(UIUtils.createSectionLabel("2. Cân đối"));
        txtCanDoi = createModernTextArea();
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTextAreaScrollWithBorder(txtCanDoi, 100));
        mainContainer.add(Box.createVerticalStrut(25));

        // 3. Ý định bảo đảm
        mainContainer.add(UIUtils.createSectionLabel("3. Ý định bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChuanBi = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChienDau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(25));

        // 4. Vệ sinh phòng bệnh
        mainContainer.add(UIUtils.createSectionLabel("4. Vệ sinh phòng bệnh, phòng dịch"));
        txtVeSinh = createModernTextArea();
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTextAreaScrollWithBorder(txtVeSinh, 100));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // --- HÀM TẠO Ô NHẬP TEXT HIỆN ĐẠI (Padding bên trong) ---
    private JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(SLATE_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Padding: 5px trên dưới, 10px trái phải
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return area;
    }

    // --- HÀM TẠO SCROLLBAR CÓ VIỀN ĐẸP ---
    private JScrollPane createTextAreaScrollWithBorder(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1)); // Viền Slate nhạt
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private JPanel createQuanYTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Gap 10px
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"TT", "Chỉ tiêu", "ĐVT", "Số lượng", "Ghi chú"};
        modelQuanY = new DefaultTableModel(cols, 0);
        modelQuanY.addRow(new Object[]{"1", "Tỷ lệ TBBB (người/1000 người/ngày)", "", "", ""});
        modelQuanY.addRow(new Object[]{"2", "Hệ số k", "", "", "Dự kiến, quy đổi, bổ sung..."});
        modelQuanY.addRow(new Object[]{"3", "Tỷ lệ bệnh (người/ngày)", "", "", ""});

        tblQuanY = new JTable(modelQuanY);
        tblQuanY.setRowHeight(35); // Chiều cao dòng rộng rãi
        tblQuanY.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // --- CUSTOM BẢNG (TableHeader & Viền Slate) ---
        setupModernTableStyle(tblQuanY, cols.length);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        btnAdd.addActionListener(e -> modelQuanY.addRow(new Object[]{"", "", "", "", ""}));
        btnDel.addActionListener(e -> { int r = tblQuanY.getSelectedRow(); if(r!=-1) modelQuanY.removeRow(r); });
        pnlBtn.add(btnAdd); pnlBtn.add(btnDel);

        pnl.add(pnlBtn, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tblQuanY);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1)); // Viền cho bảng
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    // Hàm hỗ trợ style bảng hiện đại
    private void setupModernTableStyle(JTable table, int columnCount) {
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setForeground(SLATE_TEXT);
        table.getTableHeader().setBackground(new Color(241, 245, 249)); // Màu Slate-100 cực sang
        table.getTableHeader().setBorder(new LineBorder(SLATE_BORDER, 1));

        Color gridColor = new Color(226, 232, 240); // Grid nhạt hơn
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == columnCount - 1 ? 0 : 1, gridColor));

                if (column == 1) setHorizontalAlignment(SwingConstants.LEFT); // Chữ canh trái
                else setHorizontalAlignment(SwingConstants.CENTER); // Ô khác canh giữa

                if (column == 0) setFont(new Font("Segoe UI", Font.BOLD, 14)); // TT in đậm
                else setFont(new Font("Segoe UI", Font.PLAIN, 14));

                if (isSelected) c.setBackground(new Color(219, 234, 254)); // Màu chọn
                else c.setBackground(Color.WHITE);
                return c;
            }
        };
        for (int i = 0; i < columnCount; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            if (i == 0) table.getColumnModel().getColumn(i).setMaxWidth(40);
        }
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<rows_bang_quan_y>>", buildTableHtml(modelQuanY));
        data.put("<<can_doi_quan_y>>", txtCanDoi.getText().trim());
        data.put("<<quany_chuan_bi>>", txtChuanBi.getText().trim());
        data.put("<<quany_chien_dau>>", txtChienDau.getText().trim());
        data.put("<<ve_sinh_phong_dich>>", txtVeSinh.getText().trim());
        return data;
    }

    private String buildTableHtml(DefaultTableModel m) {
        if (m == null || m.getRowCount() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < m.getColumnCount(); j++) {
                Object val = m.getValueAt(i, j);
                sb.append("<td>").append(val == null ? "" : val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }
}