package org.example.Panel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Step3_MaterialPanel extends JPanel {
    private DataDeclarationPanel parent;
    private DefaultTableModel model;
    private JTable table;

    // Cờ chống lặp vô hạn khi tính tổng
    private boolean isUpdatingSum = false;

    // Chiều rộng chốt cứng các cột
    private final int[] colWidths = {50, 310, 70, 70, 70, 70, 110, 110, 110, 150, 140, 160};

    public Step3_MaterialPanel(DataDeclarationPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("KHAI BÁO VẬT CHẤT HIỆN CÓ CỦA TIỂU ĐOÀN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(192, 57, 43));
        lblTitle.setBorder(new EmptyBorder(5, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        // ĐÃ SỬA: Đổi "+" thành "Tổng"
        String[] columnNames = {"TT", "Loại vật chất", "ĐVT", "Tổng", "Kho/d", "Đơn vị", "Phối thuộc", "Hướng chủ yếu", "Hướng thứ yếu", "Phòng ngự phía sau", "Lực lượng còn lại", "Ghi chú"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String ttStr = getValueAt(row, 0) != null ? getValueAt(row, 0).toString().trim().toUpperCase() : "";
                if (ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) return false; // Khóa dòng vàng

                // ĐÃ SỬA: Khóa cột Tổng (index 3) không cho người dùng sửa tay
                if (column == 3) return false;

                return true;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null); // Tắt header mặc định
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
        }

        table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());

        DefaultTableCellRenderer commonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));

                String ttStr = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim().toUpperCase() : "";

                if (ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    c.setBackground(new Color(255, 241, 118)); // Bôi vàng dòng tiêu đề
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                } else if (col == 3) {
                    // Tô màu nền xám nhẹ hoặc xanh nhạt cho cột Tổng để báo hiệu đây là ô tự động tính
                    c.setBackground(new Color(241, 245, 249));
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                }

                // Hiệu ứng chọn dòng
                if (isSelected && !ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    c.setBackground(new Color(219, 234, 254));
                }
                return c;
            }
        };

        for (int i = 0; i < columnNames.length; i++) {
            if (i != 1) table.getColumnModel().getColumn(i).setCellRenderer(commonRenderer);
        }

        // Tự động tính cột tổng (Kho + Đơn vị) an toàn
        model.addTableModelListener(e -> {
            if (!isUpdatingSum && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int col = e.getColumn();
                if (col >= 4 && col <= 5) {
                    int r = e.getFirstRow();
                    String ttStr = model.getValueAt(r, 0) != null ? model.getValueAt(r, 0).toString().trim().toUpperCase() : "";
                    if (!ttStr.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                        isUpdatingSum = true; // Bật cờ chặn vòng lặp
                        try {
                            double kho = InputValidator.parseDoubleSafe(model.getValueAt(r, 4));
                            double donVi = InputValidator.parseDoubleSafe(model.getValueAt(r, 5));
                            double sum = kho + donVi;
                            model.setValueAt(formatDouble(sum), r, 3);
                        } finally {
                            isUpdatingSum = false; // Tắt cờ
                        }
                    }
                }
            }
        });

        // --- 3. KHU VỰC ĐIỀU KHIỂN (THÊM/XÓA) ---
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAdd); pnlControls.add(btnDel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(pnlControls, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        // --- KHÔI PHỤC HEADER 3 TẦNG VÀ ĐỒNG BỘ SCROLL ---
        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);

        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e -> {
            Point p = headerViewport.getViewPosition();
            p.x = e.getValue();
            headerViewport.setViewPosition(p);
        });

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(Color.WHITE);
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        int sbWidth = UIManager.getInt("ScrollBar.width");
        if (sbWidth == 0) sbWidth = 17;
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(sbWidth, 130));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(combinedPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- 4. ĐIỀU HƯỚNG ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JButton btnBack = UIUtils.createNavButtonWithIcon("Quay lại", new Color(149, 165, 166), "/images/return.png", false);
        JButton btnNext = UIUtils.createNavButtonWithIcon("Tiếp tục", new Color(41, 128, 185), "/images/next.png", true);
        bottomPanel.add(btnBack, BorderLayout.WEST); bottomPanel.add(btnNext, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- LOGIC EVENTS ---
        btnAdd.addActionListener(e -> {
            int idx = table.getSelectedRow() == -1 ? model.getRowCount() : table.getSelectedRow() + 1;
            model.insertRow(idx, new Object[]{"", "", "", "0", "0", "0", "0", "0", "0", "0", "0", ""});
            recalculateTT();
        });

        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String tt = model.getValueAt(r, 0) != null ? model.getValueAt(r, 0).toString().trim().toUpperCase() : "";
                if (tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                    JOptionPane.showMessageDialog(this, "Không được xóa dòng phân loại!");
                    return;
                }
                model.removeRow(r);
                recalculateTT();
            }
        });

        btnBack.addActionListener(e -> parent.navigateStep(2));
        btnNext.addActionListener(e -> { if (saveToDatabase()) parent.navigateStep(4); });

        // TỰ ĐỘNG LOAD KHI HIỂN THỊ
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) { loadDataFromDatabase(); }
        });
    }

    public void loadDataFromDatabase() {
        int sessionId = parent.getCurrentSessionId();
        model.setRowCount(0);
        if (sessionId == -1) { initDefaultRows(); return; }

        java.util.List<Object[]> listDan = new java.util.ArrayList<>();
        java.util.List<Object[]> listHC = new java.util.ArrayList<>();
        java.util.List<Object[]> listKT = new java.util.ArrayList<>();

        String sql = "SELECT * FROM step3_vat_chat WHERE session_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int loai = rs.getInt("loai_vat_chat");
                double kho = rs.getDouble("kho_d");
                double donVi = rs.getDouble("don_vi");

                Object[] rowData = new Object[]{
                        "", // TT sẽ được tính lại sau
                        rs.getString("vat_chat"), rs.getString("dvt"),
                        formatDouble(kho + donVi), formatDouble(kho), formatDouble(donVi),
                        formatDouble(rs.getDouble("phoi_thuoc")), formatDouble(rs.getDouble("huong_cy")), formatDouble(rs.getDouble("huong_ty")),
                        formatDouble(rs.getDouble("pn_sau")), formatDouble(rs.getDouble("ll_con_lai")), rs.getString("ghi_chu")
                };

                if (loai == 1) listDan.add(rowData);
                else if (loai == 2) listHC.add(rowData);
                else listKT.add(rowData);
            }
        } catch (Exception e) { e.printStackTrace(); }

        model.addRow(new Object[]{"I", "ĐẠN", "", "", "", "", "", "", "", "", "", ""});
        for (Object[] row : listDan) model.addRow(row);

        model.addRow(new Object[]{"II", "VẬT CHẤT HẬU CẦN", "", "", "", "", "", "", "", "", "", ""});
        for (Object[] row : listHC) model.addRow(row);

        model.addRow(new Object[]{"III", "VẬT TƯ KỸ THUẬT", "", "", "", "", "", "", "", "", "", ""});
        for (Object[] row : listKT) model.addRow(row);

        recalculateTT();
    }

    private boolean saveToDatabase() {
        int sessionId = parent.getCurrentSessionId();
        if (sessionId == -1) {
            JOptionPane.showMessageDialog(this, "Không có SessionID hợp lệ!");
            return false;
        }

        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM step3_vat_chat WHERE session_id = ?")) {
                psDel.setInt(1, sessionId);
                psDel.executeUpdate();
            }

            String sql = "INSERT INTO step3_vat_chat (session_id, loai_vat_chat, vat_chat, dvt, kho_d, don_vi, phoi_thuoc, huong_cy, huong_ty, pn_sau, ll_con_lai, ghi_chu) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int currentType = 1;

                for (int i = 0; i < model.getRowCount(); i++) {
                    String ttStr = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim().toUpperCase() : "";
                    String vatChat = model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString().trim() : "";

                    if (ttStr.equals("I") || vatChat.equals("ĐẠN")) { currentType = 1; continue; }
                    if (ttStr.equals("II") || vatChat.equals("VẬT CHẤT HẬU CẦN")) { currentType = 2; continue; }
                    if (ttStr.equals("III") || vatChat.equals("VẬT TƯ KỸ THUẬT")) { currentType = 3; continue; }

                    if (vatChat.isEmpty()) continue;

                    ps.setInt(1, sessionId);
                    ps.setInt(2, currentType);
                    ps.setString(3, vatChat);
                    ps.setString(4, model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "");
                    ps.setDouble(5, InputValidator.parseDoubleSafe(model.getValueAt(i, 4)));
                    ps.setDouble(6, InputValidator.parseDoubleSafe(model.getValueAt(i, 5)));
                    ps.setDouble(7, InputValidator.parseDoubleSafe(model.getValueAt(i, 6)));
                    ps.setDouble(8, InputValidator.parseDoubleSafe(model.getValueAt(i, 7)));
                    ps.setDouble(9, InputValidator.parseDoubleSafe(model.getValueAt(i, 8)));
                    ps.setDouble(10, InputValidator.parseDoubleSafe(model.getValueAt(i, 9)));
                    ps.setDouble(11, InputValidator.parseDoubleSafe(model.getValueAt(i, 10)));
                    ps.setString(12, model.getValueAt(i, 11) != null ? model.getValueAt(i, 11).toString() : "");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu Database!");
            return false;
        }
    }

    private void recalculateTT() {
        int tt = 1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String val = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().toUpperCase() : "";
            if (val.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                tt = 1;
            } else {
                model.setValueAt(String.valueOf(tt++), i, 0);
            }
        }
    }

    private String formatDouble(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        return String.format("%s", d).replace(".", ",");
    }

    private void initDefaultRows() {
        model.addRow(new Object[]{"I", "ĐẠN", "", "", "", "", "", "", "", "", "", ""});
        model.addRow(new Object[]{"II", "VẬT CHẤT HẬU CẦN", "", "", "", "", "", "", "", "", "", ""});
        model.addRow(new Object[]{"III", "VẬT TƯ KỸ THUẬT", "", "", "", "", "", "", "", "", "", ""});
    }

    private JPanel createAbsoluteHeader() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalWidth = 0;
        for (int w : colWidths) totalWidth += w;
        p.setPreferredSize(new Dimension(totalWidth, 130));

        int[] x = new int[13]; x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) x[i + 1] = x[i] + colWidths[i];

        p.add(absLabel("TT", x[0], 0, colWidths[0], 130));
        p.add(absLabel("Loại vật chất", x[1], 0, colWidths[1], 130));
        p.add(absLabel("ĐVT", x[2], 0, colWidths[2], 130));
        p.add(absLabel("Hiện có", x[3], 0, (colWidths[3]+colWidths[4]+colWidths[5] + colWidths[6]+colWidths[7]+colWidths[8]+colWidths[9]+colWidths[10]), 35));
        p.add(absLabel("Ghi chú", x[11], 0, colWidths[11], 130));
        p.add(absLabel("Tiểu đoàn", x[3], 35, (colWidths[3]+colWidths[4]+colWidths[5]), 35));
        p.add(absLabel("Phân cấp", x[6], 35, (colWidths[6]+colWidths[7]+colWidths[8]+colWidths[9]+colWidths[10]), 35));

        // ĐÃ SỬA: Đổi "+" thành "Tổng"
        p.add(absLabel("Tổng", x[3], 70, colWidths[3], 60));

        p.add(absLabel("Kho/d", x[4], 70, colWidths[4], 60));
        p.add(absLabel("Đơn vị", x[5], 70, colWidths[5], 60));
        p.add(absLabel("<html><center>Phối<br>thuộc</center></html>", x[6], 70, colWidths[6], 60));
        p.add(absLabel("<html><center>Hướng<br>chủ yếu</center></html>", x[7], 70, colWidths[7], 60));
        p.add(absLabel("<html><center>Hướng<br>thứ yếu</center></html>", x[8], 70, colWidths[8], 60));
        p.add(absLabel("<html><center>Phòng ngự<br>phía sau</center></html>", x[9], 70, colWidths[9], 60));
        p.add(absLabel("<html><center>Lực lượng<br>còn lại</center></html>", x[10], 70, colWidths[10], 60));
        return p;
    }

    private JLabel absLabel(String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        l.setBounds(x, y, w, h);
        l.setOpaque(true); l.setBackground(new Color(248, 250, 252));
        return l;
    }

    class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineTableCellRenderer() { setLineWrap(true); setWrapStyleWord(true); setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value != null ? value.toString() : "");
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1), new EmptyBorder(5, 5, 5, 5)));
            String tt = table.getValueAt(row, 0).toString().trim().toUpperCase();
            if (tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                setBackground(new Color(255, 241, 118)); setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                setBackground(Color.WHITE); setFont(new Font("Segoe UI", Font.PLAIN, 15));
            }
            if (isSelected && !tt.matches("^(I|II|III|IV|V|VI|VII|VIII|IX|X)$")) {
                setBackground(new Color(219, 234, 254));
            }
            return this;
        }
    }
}