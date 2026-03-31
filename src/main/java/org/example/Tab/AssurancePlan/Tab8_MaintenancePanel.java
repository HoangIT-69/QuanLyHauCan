package org.example.Tab.AssurancePlan;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Tab8_MaintenancePanel extends JPanel {

    private JTextArea txtBdChuanBi, txtBdChienDau, txtBdSau;
    private DefaultTableModel modelSuaChua;
    private JTable tblSuaChua;
    private JTextArea txtScCanDoi, txtScChuanBi, txtScChienDau, txtScSau;

    // Màu sắc đồng nhất
    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);

    public Tab8_MaintenancePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("VIII. BẢO DƯỠNG, SỬA CHỮA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Bảo dưỡng
        mainContainer.add(UIUtils.createSectionLabel("1. Bảo dưỡng kỹ thuật"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdChuanBi = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtBdChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdChienDau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtBdChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtBdSau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtBdSau, 80));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Sửa chữa
        mainContainer.add(UIUtils.createSectionLabel("2. Sửa chữa"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createSuaChuaTable());
        mainContainer.add(Box.createVerticalStrut(15));

        txtScCanDoi = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Cân đối:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtScCanDoi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtScChuanBi = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtScChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtScChienDau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtScChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtScSau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtScSau, 80));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(SLATE_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding
        return area;
    }

    private JScrollPane createTextAreaScrollWithBorder(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1)); // Viền cho ô text
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private JPanel createSuaChuaTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Gap 10px
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"TT", "Loại TBKT / vũ khí", "Số lượng", "Tỉ lệ hư hỏng (%)", "Khả năng sửa chữa"};
        modelSuaChua = new DefaultTableModel(cols, 0);
        for(int i=0; i<3; i++) modelSuaChua.addRow(new Object[]{"", "", "", "", ""});

        tblSuaChua = new JTable(modelSuaChua);
        tblSuaChua.setRowHeight(35); // Chiều cao dòng
        tblSuaChua.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // --- CUSTOM BẢNG (TableHeader & Viền Slate) ---
        setupModernTableStyle(tblSuaChua, cols.length);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm trang bị", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa trang bị", new Color(231, 76, 60));
        btnAdd.addActionListener(e -> modelSuaChua.addRow(new Object[]{"", "", "", "", ""}));
        btnDel.addActionListener(e -> { int r = tblSuaChua.getSelectedRow(); if(r!=-1) modelSuaChua.removeRow(r); });
        pnlBtn.add(btnAdd); pnlBtn.add(btnDel);

        pnl.add(pnlBtn, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tblSuaChua);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1)); // Viền bảng
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private void setupModernTableStyle(JTable table, int columnCount) {
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setForeground(SLATE_TEXT);
        table.getTableHeader().setBackground(new Color(241, 245, 249)); // Slate-100 Header
        table.getTableHeader().setBorder(new LineBorder(SLATE_BORDER, 1));

        Color gridColor = new Color(226, 232, 240);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == columnCount - 1 ? 0 : 1, gridColor));

                // Canh trái cho ô thứ 2, canh giữa cho ô số
                if (column == 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                if (column == 0) setFont(new Font("Segoe UI", Font.BOLD, 14)); // TT in đậm
                else setFont(new Font("Segoe UI", Font.PLAIN, 14));

                if (isSelected) c.setBackground(new Color(219, 234, 254));
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
        data.put("<<bao_duong_kt_chuan_bi>>", txtBdChuanBi.getText().trim());
        data.put("<<bao_duong_kt_chien_dau>>", txtBdChienDau.getText().trim());
        data.put("<<bao_duong_kt_sau>>", txtBdSau.getText().trim());

        data.put("<<rows_bang_sua_chua>>", buildTableHtml(modelSuaChua));

        data.put("<<can_doi_sua_chua>>", txtScCanDoi.getText().trim());
        data.put("<<sua_chua_chuan_bi>>", txtScChuanBi.getText().trim());
        data.put("<<sua_chua_chien_dau>>", txtScChienDau.getText().trim());
        data.put("<<sua_chua_sau>>", txtScSau.getText().trim());
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