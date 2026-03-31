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

public class Tab9_TransportPanel extends JPanel {

    private JTextArea txtDuongVanTai;
    private DefaultTableModel modelKhoiLuong;
    private DefaultTableModel modelKeHoach;

    // Màu sắc hiện đại
    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);

    public Tab9_TransportPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("IX. CÔNG TÁC VẬN TẢI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Đường vận tải
        mainContainer.add(UIUtils.createSectionLabel("1. Đường vận tải"));
        mainContainer.add(Box.createVerticalStrut(10));
        txtDuongVanTai = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtDuongVanTai, 100));
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Dự tính khối lượng (BẢNG 2 TẦNG)
        mainContainer.add(UIUtils.createSectionLabel("2. Dự tính khối lượng vận tải"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createKhoiLuongTable());
        mainContainer.add(Box.createVerticalStrut(25));

        // 3. Kế hoạch (BẢNG THƯỜNG)
        mainContainer.add(UIUtils.createSectionLabel("3. Kế hoạch vận chuyển chi tiết"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createKeHoachTable());

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
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    // --- BẢNG 1: DỰ TÍNH KHỐI LƯỢNG (HEADER 2 TẦNG) ---
    private JPanel createKhoiLuongTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Gap 10px
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320)); // Cao hơn cho header 2 tầng
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Chiều rộng các cột
        int[] w = {50, 250, 100, 100, 100, 100, 150};
        String[] cols = new String[7]; for (int i=0; i<7; i++) cols[i] = ""; // Header trống vì dùng label tuyệt đối

        modelKhoiLuong = new DefaultTableModel(cols, 0);
        for(int i=0; i<4; i++) modelKhoiLuong.addRow(new Object[]{"", "", "", "", "", "", ""});

        JTable table = new JTable(modelKhoiLuong);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setTableHeader(null); // Không dùng header chuẩn
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Custom Table Renderer (Viền và Canh lề Số/Chữ)
        Color gridColor = new Color(226, 232, 240);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 6 ? 0 : 1, gridColor));

                // Canh lề: Nội dung canh trái, Số canh giữa
                if (column == 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                if (column == 0) setFont(new Font("Segoe UI", Font.BOLD, 14)); // TT in đậm
                else setFont(new Font("Segoe UI", Font.PLAIN, 14));

                if (isSelected) c.setBackground(new Color(219, 234, 254));
                else c.setBackground(Color.WHITE);
                return c;
            }
        };
        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // --- CUSTOME HEADER (Dùng Absolute Layout để hiện 2 tầng Tấn/Người) ---
        JPanel headerPanel = createHeaderKhoiLuong(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
        // Đồng bộ Scrollbar ngang với Header
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));

        // Gộp header tuyệt đối vào trên Scrollpane bảng
        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(null); // Viền nằm ở ngoài cùng của PANEL to

        // Wrapper cho Header để đồng bộ
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);

        // Tạo khoảng trống cho Scrollbar dọc để Header không bị lệch
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 60)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);

        // Thanh công cụ Nút
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        btnAdd.addActionListener(e -> modelKhoiLuong.addRow(new Object[]{"", "", "", "", "", "", ""}));
        btnDel.addActionListener(e -> { int r = table.getSelectedRow(); if(r!=-1) modelKhoiLuong.removeRow(r); });
        pnlBtn.add(btnAdd); pnlBtn.add(btnDel);

        pnl.add(pnlBtn, BorderLayout.NORTH);
        pnl.add(combined, BorderLayout.CENTER); // Viền chung
        return pnl;
    }

    // HÀM TẠO HEADER TUYỆT ĐỐI (CỐ ĐỊNH 2 TẦNG)
    private JPanel createHeaderKhoiLuong(int[] w) {
        JPanel p = new JPanel(null); // Absolute
        p.setBackground(new Color(241, 245, 249)); // Slate-100 Header
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 60)); // Cao 60px cho 2 tầng

        // Mảng x lưu vị trí ngang
        int[] x = new int[8]; x[0]=0; for(int i=0; i<7; i++) x[i+1] = x[i]+w[i];

        // --- HÀNG 1 (Cao 30px) ---
        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, w[0], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nội dung", x[1], 0, w[1], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Khối lượng", x[2], 0, w[2]+w[3], 30)); // header bao 2 ô con Tấn/Người
        p.add(UIUtils.createAbsoluteHeaderLabel("Phương thức", x[4], 0, w[4]+w[5], 30)); // header bao Cơ giới/Thô sơ
        p.add(UIUtils.createAbsoluteHeaderLabel("Ghi chú", x[6], 0, w[6], 60));

        // --- HÀNG 2 (Cao 30px, bắt đầu từ y=30) ---
        p.add(UIUtils.createAbsoluteHeaderLabel("Tấn", x[2], 30, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Người", x[3], 30, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ giới", x[4], 30, w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Thô sơ", x[5], 30, w[5], 30));

        return p;
    }

    // --- BẢNG 3: KẾ HOẠCH VẬN TẢI CHI TIẾT (Header thường 1 tầng) ---
    private JPanel createKeHoachTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Gap 10px
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"TT", "Giai đoạn", "Nội dung vận chuyển", "Khối lượng", "Phương tiện", "Ghi chú"};
        modelKeHoach = new DefaultTableModel(cols, 0);
        for(int i=0; i<3; i++) modelKeHoach.addRow(new Object[]{"", "", "", "", "", ""});

        JTable table = new JTable(modelKeHoach);
        table.setRowHeight(35); // Dòng rộng rãi
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // --- CUSTOM BẢNG THƯỜNG ---
        setupModernTableStyle(table, cols.length);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm kế hoạch", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa kế hoạch", new Color(231, 76, 60));
        btnAdd.addActionListener(e -> modelKeHoach.addRow(new Object[]{"", "", "", "", "", ""}));
        btnDel.addActionListener(e -> { int r = table.getSelectedRow(); if(r!=-1) modelKeHoach.removeRow(r); });
        pnlBtn.add(btnAdd); pnlBtn.add(btnDel);

        pnl.add(pnlBtn, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
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

                // Canh trái cho ô thứ 2+3, canh giữa cho ô khác
                if (column == 1 || column == 2) setHorizontalAlignment(SwingConstants.LEFT);
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
        data.put("<<duong_van_tai>>", txtDuongVanTai.getText().trim());
        data.put("<<rows_bang_khoi_luong_van_tai>>", buildTableHtml(modelKhoiLuong));
        data.put("<<rows_bang_ke_hoach_van_tai>>", buildTableHtml(modelKeHoach));
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