package org.example.Tab.Step4_Regulation;

import org.example.Popup.RegulationDetailDialog;
import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialRegulationTab extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    // Bỏ cột TT (50px), gộp 50px đó sang cột Vật chất (350 + 50 = 400)
    private final int[] colWidths = {400, 80, 188, 188, 188, 188, 188};

    public MaterialRegulationTab() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Đã bỏ cột TT
        String[] columnNames = {"Vật chất", "ĐVT", "Dự trữ", "04.00-N", "SCĐ", "GĐCB", "GĐCĐ"};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String ten = getValueAt(row, 0) != null ? getValueAt(row, 0).toString().trim() : "";
                if (isHeaderRow(ten)) return false; // Không cho sửa dòng vàng
                return column > 1; // Chỉ cho sửa các cột số, không sửa tên và ĐVT
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(new Color(224, 224, 224));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
        }

        setupRenderers();

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAddFromDB = UIUtils.createStyledButton("➕ Chọn từ Danh mục", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAddFromDB);
        pnlControls.add(btnDel);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel headerPanel = createAbsoluteMaterialHeader(colWidths);
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(headerPanel, BorderLayout.CENTER);

        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerContainer);

        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(17, 100));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(scroll, BorderLayout.CENTER);

        add(pnlControls, BorderLayout.NORTH);
        add(combinedPanel, BorderLayout.CENTER);

        btnAddFromDB.addActionListener(e -> showDatabaseSelectionPopup());
        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String ten = model.getValueAt(r, 0).toString().trim();
                if (!isHeaderRow(ten)) {
                    model.removeRow(r);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa dòng phân loại!");
                }
            }
        });
    }

    /**
     * Hàm kiểm tra xem dòng hiện tại có phải là dòng tiêu đề vàng không
     */
    private boolean isHeaderRow(String text) {
        return text.equals("ĐẠN") || text.equals("VẬT CHẤT HẬU CẦN") || text.equals("VẬT TƯ KỸ THUẬT");
    }

    private void setupRenderers() {
        DefaultTableCellRenderer commonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String ten = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim() : "";

                ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                        new EmptyBorder(0, 5, 0, 5)
                ));

                if (isHeaderRow(ten)) {
                    c.setBackground(new Color(255, 241, 118)); // Bôi vàng
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                    setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                }

                if (isSelected && !isHeaderRow(ten)) {
                    c.setBackground(new Color(219, 234, 254));
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(commonRenderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        model.setRowCount(0);
        RegulationDetailDialog.detailDataStore.clear();
        initBaseRows(); // Luôn tạo 3 dòng vàng làm khung trước

        if (sessionId == -1) return;

        // Đã cập nhật SQL theo bảng mới (loai_vat_chat thay cho tt)
        String sql = "SELECT * FROM step4_quy_dinh_du_tru WHERE session_id = ? ORDER BY loai_vat_chat ASC, id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int loai = rs.getInt("loai_vat_chat");
                String vatChat = rs.getString("vat_chat");
                String dvt = rs.getString("dvt");

                Object[] rowData = new Object[]{
                        vatChat, dvt != null ? dvt : "",
                        String.valueOf(rs.getDouble("du_tru")),
                        String.valueOf(rs.getDouble("phai_co_0400")),
                        String.valueOf(rs.getDouble("phai_co_scd")),
                        String.valueOf(rs.getDouble("tieu_thu_gdcb")),
                        String.valueOf(rs.getDouble("tieu_thu_gdcd"))
                };

                addLoadedDataRow(loai, rowData);

                // Do các cột lùi lại 1 index (bỏ TT), index chi tiết cũng lùi theo từ 3->2, 4->3...
                RegulationDetailDialog.detailDataStore.put(vatChat + "_2", rs.getString("dt_chitiet").split(","));
                RegulationDetailDialog.detailDataStore.put(vatChat + "_3", rs.getString("pc04_chitiet").split(","));
                RegulationDetailDialog.detailDataStore.put(vatChat + "_4", rs.getString("scd_chitiet").split(","));
                RegulationDetailDialog.detailDataStore.put(vatChat + "_5", rs.getString("gdcb_chitiet").split(","));
                RegulationDetailDialog.detailDataStore.put(vatChat + "_6", rs.getString("gdcd_chitiet").split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLoadedDataRow(int loai, Object[] rowData) {
        String headerName = loai == 1 ? "ĐẠN" : (loai == 2 ? "VẬT CHẤT HẬU CẦN" : "VẬT TƯ KỸ THUẬT");
        int insertIdx = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(headerName)) {
                insertIdx = i + 1;
                // Tìm vị trí cuối cùng của nhóm này để chèn vào
                while (insertIdx < model.getRowCount() && !isHeaderRow(model.getValueAt(insertIdx, 0).toString())) {
                    insertIdx++;
                }
                break;
            }
        }
        if (insertIdx != -1) model.insertRow(insertIdx, rowData);
    }

    public boolean saveToDatabase(int sessionId) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM step4_quy_dinh_du_tru WHERE session_id = ?")) {
                delStmt.setInt(1, sessionId);
                delStmt.executeUpdate();
            }

            // Đã cập nhật SQL dùng loai_vat_chat
            String sql = "INSERT INTO step4_quy_dinh_du_tru (session_id, loai_vat_chat, vat_chat, dvt, du_tru, phai_co_0400, phai_co_scd, tieu_thu_gdcb, tieu_thu_gdcd, dt_chitiet, pc04_chitiet, scd_chitiet, gdcb_chitiet, gdcd_chitiet) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int currentType = 1;

                for (int i = 0; i < model.getRowCount(); i++) {
                    String ten = model.getValueAt(i, 0).toString().trim();

                    // Xác định loại dựa trên dòng vàng đi trước (KHÔNG LƯU DÒNG VÀNG NÀY)
                    if (ten.equals("ĐẠN")) { currentType = 1; continue; }
                    if (ten.equals("VẬT CHẤT HẬU CẦN")) { currentType = 2; continue; }
                    if (ten.equals("VẬT TƯ KỸ THUẬT")) { currentType = 3; continue; }

                    pstmt.setInt(1, sessionId);
                    pstmt.setInt(2, currentType); // Phân loại (1, 2, 3)
                    pstmt.setString(3, ten);
                    pstmt.setString(4, model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString().trim() : "");

                    pstmt.setDouble(5, InputValidator.parseDoubleSafe(model.getValueAt(i, 2)));
                    pstmt.setDouble(6, InputValidator.parseDoubleSafe(model.getValueAt(i, 3)));
                    pstmt.setDouble(7, InputValidator.parseDoubleSafe(model.getValueAt(i, 4)));
                    pstmt.setDouble(8, InputValidator.parseDoubleSafe(model.getValueAt(i, 5)));
                    pstmt.setDouble(9, InputValidator.parseDoubleSafe(model.getValueAt(i, 6)));

                    pstmt.setString(10, String.join(",", RegulationDetailDialog.detailDataStore.getOrDefault(ten + "_2", new String[]{"0","0","0","0","0","0"})));
                    pstmt.setString(11, String.join(",", RegulationDetailDialog.detailDataStore.getOrDefault(ten + "_3", new String[]{"0","0","0","0","0","0"})));
                    pstmt.setString(12, String.join(",", RegulationDetailDialog.detailDataStore.getOrDefault(ten + "_4", new String[]{"0","0","0","0","0","0"})));
                    pstmt.setString(13, String.join(",", RegulationDetailDialog.detailDataStore.getOrDefault(ten + "_5", new String[]{"0","0","0","0","0","0"})));
                    pstmt.setString(14, String.join(",", RegulationDetailDialog.detailDataStore.getOrDefault(ten + "_6", new String[]{"0","0","0","0","0","0"})));

                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initBaseRows() {
        model.addRow(new Object[]{"ĐẠN", "", "", "", "", "", ""});
        model.addRow(new Object[]{"VẬT CHẤT HẬU CẦN", "", "", "", "", "", ""});
        model.addRow(new Object[]{"VẬT TƯ KỸ THUẬT", "", "", "", "", "", ""});
    }

    private JPanel createAbsoluteMaterialHeader(int[] w) {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalW = 0; for(int width : w) totalW += width;
        p.setPreferredSize(new Dimension(totalW, 100));

        int[] x = new int[w.length + 1]; x[0] = 0;
        for (int i = 0; i < w.length; i++) x[i+1] = x[i] + w[i];

        // Đã bỏ cột TT
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Đạn, vật chất hậu cần,<br>vật tư kỹ thuật</center></html>", x[0], 0, w[0], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐVT", x[1], 0, w[1], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Quy định<br>dự trữ</center></html>", x[2], 0, w[2], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Phải có<br>04.00-N</center></html>", x[3], 0, w[3], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Phải có<br>SCĐ</center></html>", x[4], 0, w[4], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tiêu thụ", x[5], 0, w[5] + w[6], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[5], 35, w[5], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[6], 35, w[6], 35));

        p.add(createHeaderButton(x[2], 70, w[2], 30, "Quy định dự trữ chi tiết", 2));
        p.add(createHeaderButton(x[3], 70, w[3], 30, "Phải có 04.00-N chi tiết", 3));
        p.add(createHeaderButton(x[4], 70, w[4], 30, "Phải có SCĐ chi tiết", 4));
        p.add(createHeaderButton(x[5], 70, w[5], 30, "Tiêu thụ GĐCB chi tiết", 5));
        p.add(createHeaderButton(x[6], 70, w[6], 30, "Tiêu thụ GĐCĐ chi tiết", 6));

        return p;
    }

    private JButton createHeaderButton(int x, int y, int w, int h, String title, int colIndex) {
        JButton btn = new JButton("+");
        btn.setBounds(x, y, w, h);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 247, 255));
        btn.setForeground(new Color(41, 128, 185));
        btn.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            RegulationDetailDialog dialog = new RegulationDetailDialog(owner, title, this.model, colIndex);
            dialog.setVisible(true);
        });
        return btn;
    }

    private void showDatabaseSelectionPopup() {
        Set<String> existingItems = new HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0) != null) {
                existingItems.add(model.getValueAt(i, 0).toString().trim()); // Lấy tên ở cột 0
            }
        }

        List<String[]> allItems = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT loai_dan, don_vi_tinh FROM quyuoc_dan");
            while (rs1.next()) {
                String name = rs1.getString(1);
                if (!existingItems.contains(name)) allItems.add(new String[]{name, rs1.getString(2), "Đạn"});
            }

            ResultSet rs2 = stmt.executeQuery("SELECT ten_vat_chat, don_vi_tinh FROM quyuoc_vchc");
            while (rs2.next()) {
                String name = rs2.getString(1);
                if (!existingItems.contains(name)) allItems.add(new String[]{name, rs2.getString(2), "VCHC"});
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        if (allItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả vật chất trong danh mục đã được thêm vào bảng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] display = allItems.stream().map(i -> "[" + i[2] + "] " + i[0]).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Chọn vật chất:", "Danh mục", JOptionPane.PLAIN_MESSAGE, null, display, display[0]);

        if (sel != null) {
            for (String[] item : allItems) {
                if (sel.contains(item[0])) {
                    insertData(item);
                    break;
                }
            }
        }
    }

    private void insertData(String[] data) {
        int idx = -1;
        String search = data[2].equals("Đạn") ? "ĐẠN" : "VẬT CHẤT HẬU CẦN";
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(search)) {
                idx = i + 1;
                while (idx < model.getRowCount() && !isHeaderRow(model.getValueAt(idx, 0).toString())) {
                    idx++;
                }
                break;
            }
        }
        if (idx != -1) {
            model.insertRow(idx, new Object[]{data[0], data[1], "0", "0", "0", "0", "0"});
        }
    }
}