package org.example.Tab.AssurancePlan.Tav9_TransportPanel;

import org.example.Popup.Tab9_DanTransportDialog.Tab9_DanTransportUI;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

import static java.lang.Double.parseDouble;


public class Tab9_TransportPanel extends JPanel {

    private JTextArea txtDuongVanTai;
    private DefaultTableModel modelKhoiLuong;
    private DefaultTableModel modelKeHoach;
    private JTable tableKeHoach;
    private JScrollPane scrollKeHoach;
    private boolean isRecalculatingKeHoach = false;

    // Biện pháp bảo đảm
    private JTextArea txtBpChuanBi;
    private JTextArea txtBpChienDau;
    private JTextArea txtBpSauChienDau;

    private final Tab9_TransportService service = new Tab9_TransportService();

    private final int sessionId;

    // Trạng thái phân bổ Đạn
    private java.util.Map<String, Integer> danGdcbAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> danGdcdAssignments = new java.util.HashMap<>();
    
    // Trạng thái phân bổ VCHC
    private java.util.Map<String, Integer> qnGdcbAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> qyGdcbAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> dtGdcbAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> vtktGdcbAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> qnGdcdAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> qyGdcdAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> dtGdcdAssignments = new java.util.HashMap<>();
    private java.util.Map<String, Integer> vtktGdcdAssignments = new java.util.HashMap<>();

    // Màu sắc hiện đại
    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);
    private static final Color ROW_GRAY = new Color(241, 245, 249); // Màu xám cho các dòng Tổng/Cha

    private JPanel pnlKeHoach;

    public Tab9_TransportPanel(int sessionId) {
        this.sessionId = sessionId;
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
        JPanel pnlHeader3 = new JPanel(new BorderLayout());
        pnlHeader3.setOpaque(false);
        pnlHeader3.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHeader3.add(UIUtils.createSectionLabel("3. Kế hoạch vận chuyển do hậu cần, kỹ thuật tiểu đoàn đảm nhiệm"), BorderLayout.WEST);
        
        JButton btnExpand = new JButton("Phóng to / Xem chi tiết");
        btnExpand.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExpand.setBackground(new Color(41, 128, 185));
        btnExpand.setForeground(Color.WHITE);
        btnExpand.addActionListener(e -> openDetailedPlanDialog());
        pnlHeader3.add(btnExpand, BorderLayout.EAST);
        
        mainContainer.add(pnlHeader3);
        mainContainer.add(Box.createVerticalStrut(15));
        
        pnlKeHoach = createKeHoachTable();
        mainContainer.add(pnlKeHoach);
        mainContainer.add(Box.createVerticalStrut(25));

        // 4. Biện pháp bảo đảm
        mainContainer.add(UIUtils.createSectionLabel("4. Biện pháp bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(10));
        
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị:"));
        txtBpChuanBi = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtBpChuanBi, 100));
        mainContainer.add(Box.createVerticalStrut(10));
        
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtBpChienDau = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtBpChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(10));
        
        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtBpSauChienDau = createModernTextArea();
        mainContainer.add(createTextAreaScrollWithBorder(txtBpSauChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(25));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshData();
        }
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
    // BẢNG 1: DỰ TÍNH KHỐI LƯỢNG VẬN TẢI (13 Cột, Header 3 tầng)
    // =========================================================================
    private JPanel createKhoiLuongTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1300, 450));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {40, 200, 60, 60, 60, 60, 60, 60, 60, 90, 70, 70, 120};
        String[] cols = new String[13]; for (int i=0; i<13; i++) cols[i] = "";

        modelKhoiLuong = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Khóa cột TT và Nội dung, và các cột tự động tính (+, Cộng)
                if (column <= 1 || column == 4 || column == 8 || column == 10) return false;

                String nd = getValueAt(row, 1).toString();
                boolean isKhoRow = nd.trim().equals("- Kho");

                // Với các dòng phân bổ theo hướng: khóa cột phân bổ bằng popup (Đạn, VTKT, QN, QY, DT)
                // Riêng dòng "Kho" cho phép nhập tay toàn bộ.
                if (!isKhoRow && (column == 2 || column == 3 || column == 5 || column == 6 || column == 7)) return false;

                return !nd.startsWith("Toàn trận") && !nd.startsWith("Giai đoạn") && !nd.startsWith("Đơn vị tính");
            }
        };

        // --- ADD DỮ LIỆU CỨNG (14 cột) ---
        modelKhoiLuong.addRow(new Object[]{"", "Đơn vị tính", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Tấn", "Người", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Toàn trận", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Giai đoạn chuẩn bị", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Trên chuyển", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Cấp mình", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Dưới chuyển", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Kho", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "Giai đoạn chiến đấu", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Trên chuyển", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Cấp mình", "", "", "", "", "", "", "", "", "", "", ""});
        modelKhoiLuong.addRow(new Object[]{"", "  - Dưới chuyển", "", "", "", "", "", "", "", "", "", "", ""});

        modelKhoiLuong.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 1 && col >= 2) {
                    recalculateKhoiLuong();
                }
            }
        });

        JTable table = new JTable(modelKhoiLuong);
        table.setRowHeight(30);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Thêm sự kiện Double Click để mở Popup phân bổ Đạn
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    
                    if (col == 2) { // Cột Đạn
                        Window window = SwingUtilities.getWindowAncestor(Tab9_TransportPanel.this);
                        if (window instanceof Frame) {
                            java.util.List<String> dirs = service.getDanhSachHuong(sessionId);
                            if (row >= 3 && row <= 5) {
                                Tab9_DanTransportUI dialog = new Tab9_DanTransportUI((Frame) window, "Giai đoạn chuẩn bị", row, danGdcbAssignments, dirs, updatedMap -> {
                                    danGdcbAssignments = updatedMap;
                                    updateDanWeightsFromAssignments(true);
                                });
                                dialog.setVisible(true);
                            } else if (row >= 8 && row <= 10) {
                                Tab9_DanTransportUI dialog = new Tab9_DanTransportUI((Frame) window, "Giai đoạn chiến đấu", row, danGdcdAssignments, dirs, updatedMap -> {
                                    danGdcdAssignments = updatedMap;
                                    updateDanWeightsFromAssignments(false);
                                });
                                dialog.setVisible(true);
                            }
                        }
                    } else if (col == 3 || col == 5 || col == 6 || col == 7) { // VTKT, QN, QY, DT
                        Window window = SwingUtilities.getWindowAncestor(Tab9_TransportPanel.this);
                        if (window instanceof Frame) {
                            int filterCategoryIndex = -1;
                            if (col == 5) filterCategoryIndex = 0; // QN
                            else if (col == 6) filterCategoryIndex = 1; // QY
                            else if (col == 7) filterCategoryIndex = 2; // DT
                            else if (col == 3) filterCategoryIndex = 3; // VTKT

                            final int fCatIdx = filterCategoryIndex;
                            java.util.List<String> dirs = service.getDanhSachHuong(sessionId);

                            if (row >= 3 && row <= 5) {
                                java.util.Map<String, Integer> currentAssignments = getVchcAssignmentMap(fCatIdx, true);
                                org.example.Popup.Tab9_VCHCTransportDialog.Tab9_VCHCTransportUI dialog = new org.example.Popup.Tab9_VCHCTransportDialog.Tab9_VCHCTransportUI((Frame) window, "Giai đoạn chuẩn bị", row, fCatIdx, currentAssignments, dirs, updatedMap -> {
                                    setVchcAssignmentMap(fCatIdx, true, updatedMap);
                                    updateVchcWeightsFromAssignments(fCatIdx, col, true);
                                });
                                dialog.setVisible(true);
                            } else if (row >= 8 && row <= 10) {
                                java.util.Map<String, Integer> currentAssignments = getVchcAssignmentMap(fCatIdx, false);
                                org.example.Popup.Tab9_VCHCTransportDialog.Tab9_VCHCTransportUI dialog = new org.example.Popup.Tab9_VCHCTransportDialog.Tab9_VCHCTransportUI((Frame) window, "Giai đoạn chiến đấu", row, fCatIdx, currentAssignments, dirs, updatedMap -> {
                                    setVchcAssignmentMap(fCatIdx, false, updatedMap);
                                    updateVchcWeightsFromAssignments(fCatIdx, col, false);
                                });
                                dialog.setVisible(true);
                            }
                        }
                    }
                }
            }
        });

        Color gridColor = new Color(226, 232, 240);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);
                ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, c == 12 ? 0 : 1, gridColor));

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
        int[] x = new int[14]; x[0]=0; for(int i=0; i<13; i++) x[i+1] = x[i]+w[i];

        // L1
        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nội dung", x[1], 0, w[1], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Khối lượng vận chuyển", x[2], 0, x[11]-x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Người", x[11], 0, w[11], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Ghi chú", x[12], 0, w[12], 90));

        // L2
        p.add(UIUtils.createAbsoluteHeaderLabel("Vũ khí trang bị kỹ thuật", x[2], 30, w[2]+w[3]+w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vật chất hậu cần", x[5], 30, w[5]+w[6]+w[7]+w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Vật chất<br>khác</center></html>", x[9], 30, w[9], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cộng", x[10], 30, w[10], 60));

        // L3
        p.add(UIUtils.createAbsoluteHeaderLabel("Đạn", x[2], 60, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("VTKT", x[3], 60, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[4], 60, w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("QN", x[5], 60, w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("QY", x[6], 60, w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("DT", x[7], 60, w[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[8], 60, w[8], 30));

        return p;
    }

    // =========================================================================
    // BẢNG 2: KẾ HOẠCH CHI TIẾT (14 Cột, Merge Cột 1 & 2 Ảo)
    // =========================================================================
    private JPanel createKeHoachTable() {
        pnlKeHoach = new JPanel(new BorderLayout(0, 0));
        pnlKeHoach.setOpaque(false);
        pnlKeHoach.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {40, 160, 120, 80, 80, 80, 50, 60, 80, 80, 70, 70};
        String[] cols = new String[12]; for (int i=0; i<12; i++) cols[i] = "";

        modelKeHoach = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Khóa hoàn toàn dòng Group Header
                if (row % 11 == 0) return false;
                
                // Khóa hoàn toàn cụm TỔNG SỐ (từ dòng 1 đến 10) vì đây là các dòng tự tính toán
                if (row < 11) return false;
                
                // Khóa các cột 0, 1, 2
                if (column == 0 || column == 1 || column == 2) return false;
                return true;
            }
        };

        // --- FETCH DỮ LIỆU ĐỘNG TỪ DB (sẽ được gọi lại khi refresh) ---
        rebuildKeHoachRows();
        
        // Thêm Listener tự động cộng tổng
        modelKeHoach.addTableModelListener(e -> {
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (row < 0) return;
            
            // Chỉ tính toán lại nếu sửa cột 2 (Người) hoặc 4 (Khối lượng) 
            // Lưu ý: Cột 4 (Khối lượng) sẽ được code đổ vào, còn cột 2 người dùng nhập.
            if (col == 2 || col == 4) {
                recalculateKeHoachTotals();
            }
        });

        tableKeHoach = new JTable(modelKeHoach);
        tableKeHoach.setRowHeight(30);
        tableKeHoach.setTableHeader(null);
        tableKeHoach.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableKeHoach.setFillsViewportHeight(false);

        // Custom Renderer: Tạo hiệu ứng Merge Cell Ảo
        Color gridColor = new Color(226, 232, 240);
        tableKeHoach.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);

                boolean isGroupHeader = (r % 11 == 0);
                boolean isFirstOfPhase = (r % 11 == 1) || (r % 11 == 6);
                
                int topBorder = 1;
                if (!isGroupHeader) {
                    if (c == 0 || c == 1) { // Merge ô cho Nội dung, Đơn vị
                        if (!isFirstOfPhase) {
                            topBorder = 0; 
                        }
                    }
                }

                ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(topBorder, 0, 1, c == 11 ? 0 : 1, gridColor));

                if (c <= 2) setHorizontalAlignment(SwingConstants.LEFT);
                else setHorizontalAlignment(SwingConstants.CENTER);

                // Tô nền xám nhạt và in đậm cho 2 cột đầu tiên và Group Header để nhấn mạnh cấu trúc
                if (isGroupHeader) {
                    setFont(new Font("Times New Roman", Font.BOLD, 15));
                    comp.setBackground(new Color(226, 232, 240)); // Màu đậm hơn cho Header Nhóm
                    comp.setForeground(SLATE_TEXT);
                } else if (c <= 2) {
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
            tableKeHoach.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            tableKeHoach.getColumnModel().getColumn(i).setMinWidth(w[i]);
            tableKeHoach.getColumnModel().getColumn(i).setMaxWidth(w[i]);
        }

        JPanel headerPanel = createKeHoachHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        scrollKeHoach = new JScrollPane(tableKeHoach);
        scrollKeHoach.setBorder(BorderFactory.createEmptyBorder());
        scrollKeHoach.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollKeHoach.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollKeHoach.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scrollKeHoach);

        // Đồng bộ scrollbar header với table
        scrollKeHoach.getViewport().addChangeListener(e2 -> {
            viewport.setViewPosition(new Point(scrollKeHoach.getViewport().getViewPosition().x, 0));
        });

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90)); spacer.setBackground(ROW_GRAY); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scrollKeHoach, BorderLayout.CENTER);
        pnlKeHoach.add(combined, BorderLayout.CENTER);
        return pnlKeHoach;
    }

    private JPanel createKeHoachHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(ROW_GRAY);
        int totalWidth = 0; for (int width : w) totalWidth += width; p.setPreferredSize(new Dimension(totalWidth, 90));
        int[] x = new int[13]; x[0]=0; for(int i=0; i<12; i++) x[i+1] = x[i]+w[i];

        // L1
        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Nội dung, Đơn vị<br>được vận chuyển</center></html>", x[1], 0, w[1], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Chủng loại<br>VKTBKT, vật chất</center></html>", x[2], 0, w[2], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Khối lượng<br>vận chuyển</center></html>", x[3], 0, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Địa điểm", x[4], 0, w[4]+w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Cự ly<br>(km)</center></html>", x[6], 0, w[6], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vận chuyển bằng cơ giới", x[7], 0, w[7]+w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vận chuyển thô sơ", x[9], 0, w[9]+w[10]+w[11], 30));

        // L2
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Vật chất<br>(tấn)</center></html>", x[3], 30, w[3], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nơi nhận", x[4], 30, w[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nơi giao", x[5], 30, w[5], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Khối lượng<br>vận chuyển</center></html>", x[7], 30, w[7]+w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Vật chất<br>(tấn)</center></html>", x[9], 30, w[9], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Thời gian", x[10], 30, w[10]+w[11], 30));

        // L3
        p.add(UIUtils.createAbsoluteHeaderLabel("Người", x[7], 60, w[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>V/chất<br>(tấn)</center></html>", x[8], 60, w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bắt đầu", x[10], 60, w[10], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kết thúc", x[11], 60, w[11], 30));

        return p;
    }

    public Map<String, String> getExportData() {
        return service.getExportData(
                txtDuongVanTai.getText().trim(),
                modelKhoiLuong,
                modelKeHoach,
                txtBpChuanBi.getText().trim(),
                txtBpChienDau.getText().trim(),
                txtBpSauChienDau.getText().trim()
        );
    }

    /**
     * Cập nhật dữ liệu tính toán vào Bảng 1 (gọi sau khi Tab5 đã build xong dữ liệu đạn/vật chất).
     */
    public void refreshData() {
        System.out.println("[Tab9_TransportPanel] refreshData() called, sessionId=" + sessionId);
        rebuildKeHoachRows();   // Rebuild bảng kế hoạch với hướng mới nhất từ DB
        service.refreshDanData(modelKhoiLuong);
        service.refreshVCHCData(modelKhoiLuong);
        recalculateKhoiLuong();
    }

    /**
     * Query lại danh sách hướng từ DB theo sessionId và rebuild toàn bộ các dòng
     * của Bảng Kế Hoạch (giữ nguyên TỔNG SỐ ở đầu, thêm mỗi hướng tiếp theo).
     * Sử dụng flag isRecalculatingKeHoach để tránh listener trigger trong khi đang rebuild.
     */
    private void rebuildKeHoachRows() {
        if (modelKeHoach == null) return;
        isRecalculatingKeHoach = true; // Tắt listener tạm thời
        try {
            modelKeHoach.setRowCount(0); // Xóa toàn bộ dữ liệu cũ

            java.util.List<String> dbDirections = service.getDanhSachHuong(this.sessionId);
            java.util.List<String> groups = new java.util.ArrayList<>();
            groups.add("TỔNG SỐ");
            groups.addAll(dbDirections);

            String[] items = {"QN", "QY", "DT", "VTKT", "Đạn"};

            for (String g : groups) {
                // Dòng Group Header
                modelKeHoach.addRow(new Object[]{g, "", "", "", "", "", "", "", "", "", "", ""});
                for (int p = 0; p < 2; p++) {
                    String phase = (p == 0) ? "GĐCB" : "GĐCĐ";
                    for (int i = 0; i < items.length; i++) {
                        String col1 = (i == 0) ? phase : "";
                        String col2 = items[i];
                        modelKeHoach.addRow(new Object[]{"", col1, col2, "", "", "", "", "", "", "", "", ""});
                    }
                }
            }
        } finally {
            isRecalculatingKeHoach = false; // Bật lại listener
        }

        // --- CẬP NHẬT CHIỀU CAO ĐỘNG ---
        if (scrollKeHoach != null && tableKeHoach != null) {
            int rowCount = modelKeHoach.getRowCount();
            // Chiều cao = 90px (Header) + (số dòng * 30px) + một ít padding
            int requiredHeight = 90 + (rowCount * 30) + 20;
            
            Dimension d = new Dimension(scrollKeHoach.getPreferredSize().width, requiredHeight);
            scrollKeHoach.setPreferredSize(d);
            scrollKeHoach.setMinimumSize(d);
            
            System.out.println("[Tab9_TransportPanel] Rebuilt rows: " + rowCount + ", Updated Height: " + requiredHeight);
        }

        // Yêu cầu Swing tính toán lại layout sau khi số dòng thay đổi
        revalidate();
        repaint();
    }

    private boolean isCalculating = false;

    private String str(Object v) {
        return v == null ? "" : v.toString().trim();
    }

    private double parse(String s) {
        if (s.isEmpty()) return 0.0;
        try {
            return parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String fmtTon(double d) {
        return String.format(java.util.Locale.US, "%.2f", d);
    }

    private String fmtNguoi(double d) {
        return String.format(java.util.Locale.US, "%.0f", d);
    }

    private void recalculateKhoiLuong() {
        if (isCalculating || modelKhoiLuong == null) return;
        isCalculating = true;

        try {
            // 1. Tính tổng dọc: GĐCB = tổng các dòng con (có cả Kho), GĐCĐ = tổng các dòng con, Toàn trận = GĐCB + GĐCĐ
            int[] sumCols = {2, 3, 5, 6, 7, 9, 11};
            for (int c : sumCols) {
                String r3 = str(modelKhoiLuong.getValueAt(3, c));
                String r4 = str(modelKhoiLuong.getValueAt(4, c));
                String r5 = str(modelKhoiLuong.getValueAt(5, c));
                String r6 = str(modelKhoiLuong.getValueAt(6, c));
                if (!r3.isEmpty() || !r4.isEmpty() || !r5.isEmpty() || !r6.isEmpty()) {
                    double gdcb = parse(r3) + parse(r4) + parse(r5) + parse(r6);
                    modelKhoiLuong.setValueAt(c == 11 ? fmtNguoi(gdcb) : fmtTon(gdcb), 2, c);
                } else {
                    modelKhoiLuong.setValueAt("", 2, c);
                }

                String r8 = str(modelKhoiLuong.getValueAt(8, c));
                String r9 = str(modelKhoiLuong.getValueAt(9, c));
                String r10 = str(modelKhoiLuong.getValueAt(10, c));
                if (!r8.isEmpty() || !r9.isEmpty() || !r10.isEmpty()) {
                    double gdcd = parse(r8) + parse(r9) + parse(r10);
                    modelKhoiLuong.setValueAt(c == 11 ? fmtNguoi(gdcd) : fmtTon(gdcd), 7, c);
                } else {
                    modelKhoiLuong.setValueAt("", 7, c);
                }

                String r2 = str(modelKhoiLuong.getValueAt(2, c));
                String r7 = str(modelKhoiLuong.getValueAt(7, c));
                if (!r2.isEmpty() || !r7.isEmpty()) {
                    double toan = parse(r2) + parse(r7);
                    modelKhoiLuong.setValueAt(c == 11 ? fmtNguoi(toan) : fmtTon(toan), 1, c);
                } else {
                    modelKhoiLuong.setValueAt("", 1, c);
                }
            }

            // 2. Tính tổng ngang cho tất cả các dòng (1 -> 10)
            for (int r = 1; r <= 10; r++) {
                String c2 = str(modelKhoiLuong.getValueAt(r, 2)); // Đạn
                String c3 = str(modelKhoiLuong.getValueAt(r, 3)); // VTKT
                if (!c2.isEmpty() || !c3.isEmpty()) {
                    double plusVK = parse(c2) + parse(c3);
                    modelKhoiLuong.setValueAt(fmtTon(plusVK), r, 4);
                } else {
                    modelKhoiLuong.setValueAt("", r, 4);
                }

                String c5 = str(modelKhoiLuong.getValueAt(r, 5)); // QN
                String c6 = str(modelKhoiLuong.getValueAt(r, 6)); // QY
                String c7 = str(modelKhoiLuong.getValueAt(r, 7)); // DT
                if (!c5.isEmpty() || !c6.isEmpty() || !c7.isEmpty()) {
                    double plusVCHC = parse(c5) + parse(c6) + parse(c7);
                    modelKhoiLuong.setValueAt(fmtTon(plusVCHC), r, 8);
                } else {
                    modelKhoiLuong.setValueAt("", r, 8);
                }

                String c4 = str(modelKhoiLuong.getValueAt(r, 4)); // + Đạn+VTKT
                String c8 = str(modelKhoiLuong.getValueAt(r, 8)); // + VCHC
                String c9 = str(modelKhoiLuong.getValueAt(r, 9)); // Khác
                if (!c4.isEmpty() || !c8.isEmpty() || !c9.isEmpty()) {
                    double cong = parse(c4) + parse(c8) + parse(c9);
                    modelKhoiLuong.setValueAt(fmtTon(cong), r, 10);
                } else {
                    modelKhoiLuong.setValueAt("", r, 10);
                }
            }
        } finally {
            isCalculating = false;
        }
    }

    private void updateDanWeightsFromAssignments(boolean isGdcb) {
        if (modelKhoiLuong == null) return;

        java.util.Map<String, Integer> assignments = isGdcb ? danGdcbAssignments : danGdcdAssignments;

        double weightRow1 = 0; // row 3 hoặc 7 (Trên chuyển)
        double weightRow2 = 0; // row 4 hoặc 8 (Cấp mình)
        double weightRow3 = 0; // row 5 hoặc 9 (Dưới chuyển)

        // Duyệt qua tất cả keys dạng "huong::label"
        java.util.Map<String, java.util.Map<String, java.util.Map<String, Double>>> allDirDan =
                org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableDanByDirectionReadOnly();
        java.util.Map<String, java.util.Map<String, Double>> toanDDan =
                org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableToanDReadOnly();

        for (java.util.Map.Entry<String, Integer> entry : assignments.entrySet()) {
            String key = entry.getKey(); // "huong::label"
            Integer assignedRow = entry.getValue();
            if (assignedRow == null) continue;
            int sep = key.indexOf("::");
            if (sep < 0) continue;
            String huong = key.substring(0, sep);
            String label = key.substring(sep + 2);

            // Lấy giá trị từ data theo hướng, fallback toàn đội
            java.util.Map<String, java.util.Map<String, Double>> dirData = allDirDan.get(huong);
            java.util.Map<String, Double> values = (dirData != null) ? dirData.get(label) : null;
            if (values == null) values = toanDDan.get(label);
            if (values == null) continue;

            double dv  = values.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_DV,  0.0);
            double kho = values.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
            double weight = isGdcb ? (dv + kho) :
                            values.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_THUC_HANH, 0.0);

            if (isGdcb) {
                if (assignedRow == 3) weightRow1 += weight;
                else if (assignedRow == 4) weightRow2 += weight;
                else if (assignedRow == 5) weightRow3 += weight;
            } else {
                if (assignedRow == 8) weightRow1 += weight;
                else if (assignedRow == 9) weightRow2 += weight;
                else if (assignedRow == 10) weightRow3 += weight;
            }
        }

        int r1 = isGdcb ? 3 : 8;
        int r2 = isGdcb ? 4 : 9;
        int r3 = isGdcb ? 5 : 10;

        modelKhoiLuong.setValueAt(weightRow1 > 0 ? fmtTon(weightRow1) : "", r1, 2);
        modelKhoiLuong.setValueAt(weightRow2 > 0 ? fmtTon(weightRow2) : "", r2, 2);
        modelKhoiLuong.setValueAt(weightRow3 > 0 ? fmtTon(weightRow3) : "", r3, 2);

        updateKeHoachDetails();
    }

    private java.util.Map<String, Integer> getVchcAssignmentMap(int categoryIndex, boolean isGdcb) {
        if (isGdcb) {
            if (categoryIndex == 0) return qnGdcbAssignments;
            if (categoryIndex == 1) return qyGdcbAssignments;
            if (categoryIndex == 2) return dtGdcbAssignments;
            return vtktGdcbAssignments;
        } else {
            if (categoryIndex == 0) return qnGdcdAssignments;
            if (categoryIndex == 1) return qyGdcdAssignments;
            if (categoryIndex == 2) return dtGdcdAssignments;
            return vtktGdcdAssignments;
        }
    }

    private void setVchcAssignmentMap(int categoryIndex, boolean isGdcb, java.util.Map<String, Integer> map) {
        if (isGdcb) {
            if (categoryIndex == 0) qnGdcbAssignments = map;
            else if (categoryIndex == 1) qyGdcbAssignments = map;
            else if (categoryIndex == 2) dtGdcbAssignments = map;
            else vtktGdcbAssignments = map;
        } else {
            if (categoryIndex == 0) qnGdcdAssignments = map;
            else if (categoryIndex == 1) qyGdcdAssignments = map;
            else if (categoryIndex == 2) dtGdcdAssignments = map;
            else vtktGdcdAssignments = map;
        }
    }

    private void updateVchcWeightsFromAssignments(int categoryIndex, int col, boolean isGdcb) {
        if (modelKhoiLuong == null) return;

        java.util.Map<String, Integer> assignments = getVchcAssignmentMap(categoryIndex, isGdcb);

        double weightRow1 = 0;
        double weightRow2 = 0;
        double weightRow3 = 0;

        java.util.Map<String, java.util.Map<String, java.util.Map<String, Double>>> allDirVchc =
                org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.getMiniTableVCHCByDirectionReadOnly();
        java.util.Map<String, java.util.Map<String, Double>> toanDVchc =
                org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.getMiniTableVCHCReadOnly();

        for (java.util.Map.Entry<String, Integer> entry : assignments.entrySet()) {
            String key = entry.getKey(); // "huong::label"
            Integer assignedRow = entry.getValue();
            if (assignedRow == null) continue;
            int sep = key.indexOf("::");
            if (sep < 0) continue;
            String huong = key.substring(0, sep);
            String label = key.substring(sep + 2);

            java.util.Map<String, java.util.Map<String, Double>> dirData = allDirVchc.get(huong);
            java.util.Map<String, Double> values = (dirData != null) ? dirData.get(label) : null;
            if (values == null) values = toanDVchc.get(label);
            if (values == null) continue;

            double gdcbKho = values.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_KHO, 0.0);
            double gdcbDv  = values.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_DV_D, 0.0);
            double gdcdKho = values.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_KHO, 0.0);
            double gdcdDv  = values.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_DV, 0.0);
            double weight = isGdcb ? (gdcbKho + gdcbDv) : (gdcdKho + gdcdDv);

            if (isGdcb) {
                if (assignedRow == 3) weightRow1 += weight;
                else if (assignedRow == 4) weightRow2 += weight;
                else if (assignedRow == 5) weightRow3 += weight;
            } else {
                if (assignedRow == 8) weightRow1 += weight;
                else if (assignedRow == 9) weightRow2 += weight;
                else if (assignedRow == 10) weightRow3 += weight;
            }
        }

        int r1 = isGdcb ? 3 : 8;
        int r2 = isGdcb ? 4 : 9;
        int r3 = isGdcb ? 5 : 10;

        modelKhoiLuong.setValueAt(weightRow1 > 0 ? fmtTon(weightRow1) : "", r1, col);
        modelKhoiLuong.setValueAt(weightRow2 > 0 ? fmtTon(weightRow2) : "", r2, col);
        modelKhoiLuong.setValueAt(weightRow3 > 0 ? fmtTon(weightRow3) : "", r3, col);

        updateKeHoachDetails();
    }

    
    private void recalculateKeHoachTotals() {
        if (isRecalculatingKeHoach || modelKeHoach == null) return;
        isRecalculatingKeHoach = true;
        
        try {
            int rowCount = modelKeHoach.getRowCount();
            if (rowCount == 0) return;
            
            double tongNguoiToanBo = 0;
            double tongKhoiLuongToanBo = 0;
            
            // Bỏ qua cụm "TỔNG SỐ" (index 0 đến 10), duyệt từ nhóm thứ 2 trở đi
            for (int r = 11; r < rowCount; r += 11) {
                double nguoiGroup = 0;
                double klGroup = 0;
                
                // NGUOI: GĐCB ở r+1, GĐCĐ ở r+6
                double nguoiGdcb = parseDoubleObj(modelKeHoach.getValueAt(r + 1, 2));
                double nguoiGdcd = parseDoubleObj(modelKeHoach.getValueAt(r + 6, 2));
                nguoiGroup = nguoiGdcb + nguoiGdcd;
                
                // KHOI LUONG: 10 dòng con (cột 4)
                for (int i = 1; i <= 10; i++) {
                    klGroup += parseDoubleObj(modelKeHoach.getValueAt(r + i, 4));
                }
                
                // Gán lên Group Header
                modelKeHoach.setValueAt(nguoiGroup > 0 ? fmtTon(nguoiGroup) : "", r, 2);
                modelKeHoach.setValueAt(klGroup > 0 ? fmtTon(klGroup) : "", r, 4);
                
                tongNguoiToanBo += nguoiGroup;
                tongKhoiLuongToanBo += klGroup;
            }
            
            // Cập nhật lên dòng TỔNG SỐ (Group 0) - Lấy tổng từ các dòng con của TỔNG SỐ (index 1-10)
            double trueTongKhoiLuong = 0;
            for (int i = 1; i <= 10; i++) {
                trueTongKhoiLuong += parseDoubleObj(modelKeHoach.getValueAt(i, 4));
            }
            
            modelKeHoach.setValueAt(tongNguoiToanBo > 0 ? fmtTon(tongNguoiToanBo) : "", 0, 2);
            modelKeHoach.setValueAt(trueTongKhoiLuong > 0 ? fmtTon(trueTongKhoiLuong) : "", 0, 4);
            
            // Xử lý các dòng con của TỔNG SỐ
            // NGUOI GĐCB / GĐCĐ của TỔNG SỐ
            double tongNguoiGdcb = 0;
            double tongNguoiGdcd = 0;
            for (int r = 11; r < rowCount; r += 11) {
                tongNguoiGdcb += parseDoubleObj(modelKeHoach.getValueAt(r + 1, 2));
                tongNguoiGdcd += parseDoubleObj(modelKeHoach.getValueAt(r + 6, 2));
            }
            modelKeHoach.setValueAt(tongNguoiGdcb > 0 ? fmtTon(tongNguoiGdcb) : "", 1, 2);
            modelKeHoach.setValueAt(tongNguoiGdcd > 0 ? fmtTon(tongNguoiGdcd) : "", 6, 2);
            
            // KHOI LUONG TỔNG SỐ ĐÃ ĐƯỢC TÍNH TOÁN TRỰC TIẾP TỪ TOÀN D TRONG updateKeHoachDetails()
            // KHÔNG ĐƯỢC GHI ĐÈ BẰNG TỔNG CÁC HƯỚNG VÌ CÁC HƯỚNG KHÔNG CÓ KHO VÀ SẼ BỊ LỆCH SỐ LIỆU.
            
        } finally {
            isRecalculatingKeHoach = false;
        }
    }
    
    private double parseDoubleObj(Object obj) {
        if (obj == null) return 0;
        String s = obj.toString().trim().replace(",", ".");
        if (s.isEmpty()) return 0;
        try {
            return parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void openDetailedPlanDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(window instanceof Frame ? (Frame) window : null, "KẾ HOẠCH VẬN CHUYỂN CHI TIẾT", true);
        dialog.setLayout(new BorderLayout());
        
        // Tạo một bản sao UI của bảng (dùng chung model để đồng bộ)
        JPanel pnlTableContent = createKeHoachTable(); // Method này sẽ được gọi lại, nhưng cần cẩn thận với fields
        
        // Để không làm hỏng fields của Panel chính, ta sẽ clone logic hoặc chỉ đơn giản là chuyển pnlKeHoach vào dialog tạm thời
        // Cách an toàn nhất: Tạo một panel mới chỉ chứa bảng cho Dialog
        dialog.add(pnlTableContent, BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Đóng & Lưu");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.add(btnClose);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        
        dialog.setSize(new Dimension(1200, 800));
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
        
        // Sau khi đóng dialog, ta cần cập nhật lại pnlKeHoach trong mainContainer 
        // vì method createKeHoachTable() đã gán fields mới.
        rebuildKeHoachRows(); 
    }

    private void updateKeHoachDetails() {
        if (modelKeHoach == null || modelKeHoach.getRowCount() == 0) return;
        isRecalculatingKeHoach = true;
        try {
            int rowCount = modelKeHoach.getRowCount();
            String currentGroup = "";
            String currentPhase = "GĐCB"; // default

            java.util.Map<String, java.util.Map<String, Double>> toanDVchc =
                    org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.getMiniTableVCHCReadOnly();
            java.util.Map<String, java.util.Map<String, Double>> toanDDan =
                    org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableToanDReadOnly();
            java.util.Map<String, java.util.Map<String, java.util.Map<String, Double>>> dirVchc =
                    org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.getMiniTableVCHCByDirectionReadOnly();
            java.util.Map<String, java.util.Map<String, java.util.Map<String, Double>>> dirDan =
                    org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.getMiniTableDanByDirectionReadOnly();

            for (int r = 0; r < rowCount; r++) {
                boolean isGroupHeader = !String.valueOf(modelKeHoach.getValueAt(r, 0)).trim().isEmpty();
                if (isGroupHeader) {
                    currentGroup = String.valueOf(modelKeHoach.getValueAt(r, 0)).trim();
                    currentPhase = "GĐCB";
                    continue;
                }

                // Track current phase by looking for "GĐCB"/"GĐCĐ" in col 1
                String phaseCol = String.valueOf(modelKeHoach.getValueAt(r, 1)).trim();
                if ("GĐCB".equals(phaseCol)) currentPhase = "GĐCB";
                else if ("GĐCĐ".equals(phaseCol)) currentPhase = "GĐCĐ";
                boolean isGdcb = !"GĐCĐ".equals(currentPhase);
                // Cột 2 = item type
                String itemType = String.valueOf(modelKeHoach.getValueAt(r, 2)).trim();

                double weight = 0.0;
                boolean isToanD = "TỔNG SỐ".equals(currentGroup);
                // huong để tra cứu trong assignment keys
                String huongKey = currentGroup; // ví dụ "Hướng A"

                if ("Đạn".equals(itemType)) {
                    java.util.Map<String, Integer> assignments = isGdcb ? danGdcbAssignments : danGdcdAssignments;
                    int targetAssignedRow = isGdcb ? 4 : 9;

                    if (isToanD) {
                        // TỔNG SỐ: cộng tất cả assignments có assignedRow đúng
                        for (java.util.Map.Entry<String, Integer> ae : assignments.entrySet()) {
                            if (!ae.getValue().equals(targetAssignedRow)) continue;
                            String key = ae.getKey();
                            int sep = key.indexOf("::");
                            if (sep < 0) continue;
                            String hdir = key.substring(0, sep);
                            String label = key.substring(sep + 2);
                            java.util.Map<String, java.util.Map<String, Double>> dd = dirDan.get(hdir);
                            java.util.Map<String, Double> vals = (dd != null) ? dd.get(label) : null;
                            if (vals == null) vals = toanDDan.get(label);
                            if (vals == null) continue;
                            if (isGdcb) {
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_DV,  0.0);
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
                            } else {
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_THUC_HANH, 0.0);
                            }
                        }
                    } else {
                        // Hướng cụ thể: chỉ cộng assignments có prefix "huong::"
                        String prefix = huongKey + "::";
                        java.util.Map<String, java.util.Map<String, Double>> dd = dirDan.get(huongKey);
                        for (java.util.Map.Entry<String, Integer> ae : assignments.entrySet()) {
                            if (!ae.getValue().equals(targetAssignedRow)) continue;
                            if (!ae.getKey().startsWith(prefix)) continue;
                            String label = ae.getKey().substring(prefix.length());
                            java.util.Map<String, Double> vals = (dd != null) ? dd.get(label) : null;
                            if (vals == null) vals = toanDDan.get(label);
                            if (vals == null) continue;
                            if (isGdcb) {
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_DV,  0.0);
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_TRUOC_NO_KHO, 0.0);
                            } else {
                                weight += vals.getOrDefault(org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService.TL_THUC_HANH, 0.0);
                            }
                        }
                    }
                } else {
                    int catIdx = -1;
                    if ("QN".equals(itemType)) catIdx = 0;
                    else if ("QY".equals(itemType)) catIdx = 1;
                    else if ("DT".equals(itemType)) catIdx = 2;
                    else if ("VTKT".equals(itemType)) catIdx = 3;

                    if (catIdx != -1) {
                        java.util.Map<String, Integer> assignments = getVchcAssignmentMap(catIdx, isGdcb);
                        int targetAssignedRow = isGdcb ? 4 : 9;

                        if (isToanD) {
                            for (java.util.Map.Entry<String, Integer> ae : assignments.entrySet()) {
                                if (!ae.getValue().equals(targetAssignedRow)) continue;
                                String key = ae.getKey();
                                int sep = key.indexOf("::");
                                if (sep < 0) continue;
                                String hdir = key.substring(0, sep);
                                String label = key.substring(sep + 2);
                                java.util.Map<String, java.util.Map<String, Double>> dv = dirVchc.get(hdir);
                                java.util.Map<String, Double> vals = (dv != null) ? dv.get(label) : null;
                                if (vals == null) vals = toanDVchc.get(label);
                                if (vals == null) continue;
                                if (isGdcb) {
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_KHO,  0.0);
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_DV_D, 0.0);
                                } else {
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_KHO, 0.0);
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_DV,  0.0);
                                }
                            }
                        } else {
                            String prefix = huongKey + "::";
                            java.util.Map<String, java.util.Map<String, Double>> dv = dirVchc.get(huongKey);
                            for (java.util.Map.Entry<String, Integer> ae : assignments.entrySet()) {
                                if (!ae.getValue().equals(targetAssignedRow)) continue;
                                if (!ae.getKey().startsWith(prefix)) continue;
                                String label = ae.getKey().substring(prefix.length());
                                java.util.Map<String, Double> vals = (dv != null) ? dv.get(label) : null;
                                if (vals == null) vals = toanDVchc.get(label);
                                if (vals == null) continue;
                                if (isGdcb) {
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_KHO,  0.0);
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCB_DV_D, 0.0);
                                } else {
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_KHO, 0.0);
                                    weight += vals.getOrDefault(org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelService.TL_BO_SUNG_GDCD_DV,  0.0);
                                }
                            }
                        }
                    }
                }

                modelKeHoach.setValueAt(weight > 0 ? fmtTon(weight) : "", r, 4);
            }
        } finally {
            isRecalculatingKeHoach = false;
            recalculateKeHoachTotals();
        }
    }
}