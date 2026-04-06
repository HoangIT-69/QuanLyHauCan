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

public class Tab9_TransportPanel extends JPanel {

    private JTextArea txtDuongVanTai;
    private DefaultTableModel modelKhoiLuong;
    private DefaultTableModel modelKeHoach;

    // Màu sắc hiện đại
    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);
    private static final Color ROW_GRAY = new Color(241, 245, 249); // Màu xám cho các dòng Tổng/Cha

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

        // 2. Dự tính khối lượng (BẢNG 1)
        mainContainer.add(UIUtils.createSectionLabel("2. Khối lượng vận tải và phân cấp vận chuyển"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createKhoiLuongTable());
        mainContainer.add(Box.createVerticalStrut(35));

        // 3. Kế hoạch (BẢNG 2)
        mainContainer.add(UIUtils.createSectionLabel("3. Kế hoạch vận chuyển do hậu cần, kỹ thuật tiểu đoàn đảm nhiệm"));
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
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return area;
    }

    private JScrollPane createTextAreaScrollWithBorder(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.makeScrollPassThrough(scroll);
        return scroll;
    }

    // =========================================================================
    // BẢNG 1: DỰ TÍNH KHỐI LƯỢNG VẬN TẢI (14 Cột, Header 3 tầng)
    // =========================================================================
    private JPanel createKhoiLuongTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1100, 450));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {40, 180, 50, 50, 50, 50, 50, 50, 50, 50, 80, 80, 60, 100};
        String[] cols = new String[14]; for (int i=0; i<14; i++) cols[i] = "";

        modelKhoiLuong = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Khóa cột TT và Nội dung, chỉ cho phép điền số liệu
                if (column <= 1) return false;
                String nd = getValueAt(row, 1).toString();
                return !nd.startsWith("Toàn trận") && !nd.startsWith("Giai đoạn") && !nd.startsWith("Đơn vị tính");
            }
        };

        // --- ADD DỮ LIỆU CỨNG (ĐÃ SỬA THEO ẢNH 2) ---
        modelKhoiLuong.addRow(new Object[]{"", "Đơn vị tính", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Người", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Toàn trận", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Giai đoạn chuẩn bị", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Trên chuyển", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Cấp mình", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Dưới chuyển", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Giai đoạn chiến đấu", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Trên chuyển", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Cấp mình", "", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Dưới chuyển", "", "", "", "", "", "", "", "", "", "", "", ""});

        JTable table = new JTable(modelKhoiLuong);
        table.setRowHeight(30);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        Color gridColor = new Color(226, 232, 240);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);
                ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, c == 13 ? 0 : 1, gridColor));

                String nd = t.getValueAt(r, 1).toString();
                if (c <= 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                if (r == 0) { // Dòng Đơn vị tính
                    setFont(new Font("Times New Roman", Font.ITALIC, 15));
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(SLATE_TEXT);
                } else if (nd.equals("Toàn trận") || nd.startsWith("Giai đoạn")) { // Dòng Cha to
                    setFont(new Font("Times New Roman", Font.BOLD, 15));
                    comp.setBackground(ROW_GRAY);
                    comp.setForeground(Color.BLACK);
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    comp.setForeground(Color.BLACK);
                    if (isSel && c > 1) comp.setBackground(new Color(219, 234, 254));
                    else comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        });

        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
        }

        JPanel headerPanel = createHeaderKhoiLuong(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90)); spacer.setBackground(ROW_GRAY); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createHeaderKhoiLuong(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(ROW_GRAY);
        int totalWidth = 0; for (int width : w) totalWidth += width; p.setPreferredSize(new Dimension(totalWidth, 90));
        int[] x = new int[15]; x[0]=0; for(int i=0; i<14; i++) x[i+1] = x[i]+w[i];

        // L1
        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nội dung", x[1], 0, w[1], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Khối lượng vận chuyển", x[2], 0, x[12]-x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Người", x[12], 0, w[12], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Ghi chú", x[13], 0, w[13], 90));

        // L2
        p.add(UIUtils.createAbsoluteHeaderLabel("Vũ khí trang bị kỹ thuật", x[2], 30, w[2]*4, 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vật chất hậu cần", x[6], 30, w[6]*4, 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Vật chất<br>khác</center></html>", x[10], 30, w[10], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cộng", x[11], 30, w[11], 60));

        // L3
        p.add(UIUtils.createAbsoluteHeaderLabel("VKTB", x[2], 60, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Đạn", x[3], 60, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("VTKT", x[4], 60, w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[5], 60, w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("QN", x[6], 60, w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("QY", x[7], 60, w[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("DT", x[8], 60, w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[9], 60, w[9], 30));

        return p;
    }

    // =========================================================================
    // BẢNG 2: KẾ HOẠCH CHI TIẾT (14 Cột, Merge Cột 1 & 2 Ảo)
    // =========================================================================
    private JPanel createKeHoachTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1100, 450));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {120, 80, 60, 70, 70, 80, 80, 60, 70, 70, 70, 70, 70, 70};
        String[] cols = new String[14]; for (int i=0; i<14; i++) cols[i] = "";

        modelKeHoach = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 3; // Chỉ cho phép nhập dữ liệu từ Cột Khối lượng trở đi
            }
        };

        // --- ADD DỮ LIỆU CỨNG PHÂN NHÓM (ĐÃ SỬA CỘT TỔNG SỐ) ---
        String[] groups = {"TỔNG SỐ", "Hướng CY", "Hướng TY", "LL phía sau", "LL còn lại"};
        String[] items = {"QN", "QY", "DT", "VTKT", "Đạn"};

        for (String g : groups) {
            for (int p = 0; p < 2; p++) {
                String phase = (p == 0) ? "GĐCB" : "GĐCĐ";
                for (int i = 0; i < items.length; i++) {
                    String col0 = (p == 0 && i == 0) ? g : ""; // Chỉ in Tên Nhóm ở dòng đầu tiên của Nhóm
                    String col1 = (i == 0) ? phase : ""; // Chỉ in Tên Giai đoạn ở dòng đầu tiên của GĐ
                    String col2 = (i == 0) ? "0" : ""; // Cột Người chỉ có ở dòng đầu
                    String col3 = items[i];
                    modelKeHoach.addRow(new Object[]{col0, col1, col2, col3, "", "", "", "", "", "", "", "", "", ""});
                }
            }
        }

        JTable table = new JTable(modelKeHoach);
        table.setRowHeight(30);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Custom Renderer: Tạo hiệu ứng Merge Cell Ảo
        Color gridColor = new Color(226, 232, 240);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);

                String valStr = v != null ? v.toString() : "";

                // Nếu là 3 cột đầu và ô đang trống -> Ẩn viền trên để tạo cảm giác gộp ô (Merge Cell)
                boolean isMerged = (c <= 2) && valStr.isEmpty() && r > 0;
                int topBorder = isMerged ? 0 : 1;

                ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(topBorder, 0, 1, c == 13 ? 0 : 1, gridColor));

                if (c <= 1) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                // Tô nền xám nhạt và in đậm cho 2 cột đầu tiên để nhấn mạnh cấu trúc
                if (c <= 1) {
                    setFont(new Font("Times New Roman", Font.BOLD, 15));
                    comp.setBackground(new Color(248, 250, 252));
                    comp.setForeground(SLATE_TEXT);
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    comp.setForeground(Color.BLACK);
                    if (isSel && c > 2) comp.setBackground(new Color(219, 234, 254));
                    else comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        });

        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
        }

        JPanel headerPanel = createKeHoachHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90)); spacer.setBackground(ROW_GRAY); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createKeHoachHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(ROW_GRAY);
        int totalWidth = 0; for (int width : w) totalWidth += width; p.setPreferredSize(new Dimension(totalWidth, 90));
        int[] x = new int[15]; x[0]=0; for(int i=0; i<14; i++) x[i+1] = x[i]+w[i];

        // L1
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Nội dung, Đơn vị<br>được vận chuyển</center></html>", x[0], 0, w[0]+w[1], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Người", x[2], 0, w[2], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("TBKT, vật chất HC, KT", x[3], 0, w[3]+w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Địa điểm", x[5], 0, w[5]+w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Cự ly<br>(km)</center></html>", x[7], 0, w[7], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vận chuyển thô sơ", x[8], 0, w[8]+w[9]+w[10], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vận chuyển khác", x[11], 0, w[11]+w[12]+w[13], 30));

        // L2
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Chủng<br>loại</center></html>", x[3], 30, w[3], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Khối<br>lượng (tấn)</center></html>", x[4], 30, w[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nơi nhận", x[5], 30, w[5], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nơi giao", x[6], 30, w[6], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>vật chất<br>(tấn)</center></html>", x[8], 30, w[8], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Thời gian", x[9], 30, w[9]+w[10], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>vật chất<br>(tấn)</center></html>", x[11], 30, w[11], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Thời gian", x[12], 30, w[12]+w[13], 30));

        // L3
        p.add(UIUtils.createAbsoluteHeaderLabel("Bắt đầu", x[9], 60, w[9], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kết thúc", x[10], 60, w[10], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bắt đầu", x[12], 60, w[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kết thúc", x[13], 60, w[13], 30));

        return p;
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
                String text = val == null ? "" : val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

                // Tô đậm các dòng tổng / tiêu đề khi xuất Word
                if (text.startsWith("Toàn trận") || text.startsWith("Giai đoạn") || text.startsWith("TỔNG") || text.startsWith("Hướng") || text.startsWith("LL")) {
                    if (j <= 1) text = "<b>" + text + "</b>";
                }

                if (j <= 1) sb.append("<td class='text-left'>").append(text).append("</td>");
                else sb.append("<td>").append(text).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }
}