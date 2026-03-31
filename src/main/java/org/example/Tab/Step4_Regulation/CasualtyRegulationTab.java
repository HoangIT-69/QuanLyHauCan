package org.example.Tab.Step4_Regulation;

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

public class CasualtyRegulationTab extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public CasualtyRegulationTab() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. THANH ĐIỀU KHIỂN ---
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAdd);
        pnlControls.add(btnDel);

        // --- 2. CẤU CẤU HÌNH BẢNG ---
        String[] cols = {"Loại thương binh, bệnh binh", "Tỉ lệ (%)"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        // Thiết lập chiều cao dòng và font
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false); // Sẽ dùng Renderer để vẽ viền cho chuẩn

        // Header Style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setBorder(new LineBorder(new Color(200, 200, 200)));

        // Độ rộng cột (Tổng 1100px)
        table.getColumnModel().getColumn(0).setPreferredWidth(800);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        // Renderer cho đẹp
        setupRenderers();

        // --- 3. GỘP VÀO SCROLLPANE ---
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        scroll.getViewport().setBackground(Color.WHITE);

        add(pnlControls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Logic Buttons
        btnAdd.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            model.addRow(new Object[]{"Mục mới...", "0"});
        });

        btnDel.addActionListener(e -> {
            if (table.getSelectedRow() != -1) {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                model.removeRow(table.getSelectedRow());
            }
        });
    }

    // =========================================================
    // HÀM TẢI DỮ LIỆU TỪ DATABASE
    // =========================================================
    public void loadDataFromDatabase(int sessionId) {
        model.setRowCount(0); // Xóa trắng bảng

        if (sessionId == -1) {
            loadDefaultData(); // Nếu là tạo mới thì nạp file Excel giả lập
            return;
        }

        String sql = "SELECT * FROM step4_ti_le_thuong_binh WHERE session_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String loaiThuongBinh = rs.getString("loai_thuong_binh");
                double tiLe = rs.getDouble("ti_le");

                // Nếu tỉ lệ = 0 thì hiển thị chuỗi rỗng cho giống bản nháp ban đầu của bạn
                String hienThiTiLe = (tiLe > 0) ? String.valueOf(tiLe) : "";

                model.addRow(new Object[]{loaiThuongBinh, hienThiTiLe});
            }

            if (!hasData) loadDefaultData(); // Fallback nếu DB trống

        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultData();
        }
    }

    // =========================================================
    // HÀM LƯU DỮ LIỆU XUỐNG DATABASE
    // =========================================================
    public boolean saveToDatabase(int sessionId) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        try (Connection conn = DBConnection.getConnection()) {
            // Xóa dữ liệu cũ
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM step4_ti_le_thuong_binh WHERE session_id = ?")) {
                delStmt.setInt(1, sessionId);
                delStmt.executeUpdate();
            }

            String sql = "INSERT INTO step4_ti_le_thuong_binh (session_id, loai_thuong_binh, ti_le) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    String ten = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim() : "";

                    pstmt.setInt(1, sessionId);
                    pstmt.setString(2, ten);
                    pstmt.setDouble(3, InputValidator.parseDoubleSafe(model.getValueAt(i, 1))); // An toàn ép kiểu

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

    private void loadDefaultData() {
        model.addRow(new Object[]{"Tỷ lệ thương binh toàn trận", "20"});
        model.addRow(new Object[]{"Tỷ lệ thương binh trung bình ngày đêm", ""});
        model.addRow(new Object[]{"Tỷ lệ thương binh ngày cao nhất", "8"});
        model.addRow(new Object[]{"Tỷ lệ thương binh phải cáng", "50"});
        model.addRow(new Object[]{"Tỷ lệ bệnh binh toàn trận", "1.5"});
        model.addRow(new Object[]{"Tỷ lệ thương binh hoá học", ""});
        model.addRow(new Object[]{"Tỷ lệ thương binh Hướng chủ yếu", ""});
    }

    private void setupRenderers() {
        // Renderer cho cột Tên (Căn trái, thụt lề)
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(224, 224, 224)),
                        BorderFactory.createEmptyBorder(0, 15, 0, 0)
                ));
                if (isSelected) c.setBackground(new Color(219, 234, 254));
                else c.setBackground(Color.WHITE);
                return c;
            }
        });

        // Renderer cho cột Tỉ lệ (Căn giữa)
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
                if (isSelected) c.setBackground(new Color(219, 234, 254));
                else c.setBackground(Color.WHITE);
                return c;
            }
        });
    }
}