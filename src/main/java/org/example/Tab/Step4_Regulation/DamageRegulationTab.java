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
import java.util.HashMap;
import java.util.Map;

public class DamageRegulationTab extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private int currentSessionId = -1;

    public DamageRegulationTab() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("🔄 Cập nhật số lượng từ Sa bàn", new Color(41, 128, 185));
        pnlControls.add(btnRefresh);

        String[] cols = {"TT", "Loại VKTBKT", "Số lượng TBKT tham gia CĐ", "Dự kiến tỷ lệ hư hỏng 1 ngày đêm (%)"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        table = new JTable(model);

        // Định dạng JTable
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setShowGrid(true);
        table.setGridColor(new Color(224, 224, 224));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(350);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);

        setupRenderers();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        add(pnlControls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            if (currentSessionId > 0) {
                loadDataFromDatabase(currentSessionId);
            } else {
                JOptionPane.showMessageDialog(this, "Chưa xác định Session ID!");
            }
        });
    }

    /**
     * TRUY VẤN MỚI: Join 2 bảng quyuoc_bienche và step2_bien_che
     */
    private Map<String, Integer> fetchWeaponSums(int sessionId) {
        Map<String, Integer> sums = new HashMap<>();
        if (sessionId <= 0) return sums;

        // Câu lệnh SQL JOIN chính xác theo ảnh DB của bạn
        String sql = "SELECT " +
                "SUM(q.sung_ngan) as sung_ngan, SUM(q.tieu_lien) as tieu_lien, " +
                "SUM(q.trung_lien) as trung_lien, SUM(q.dai_lien) as dai_lien, " +
                "SUM(q.b41) as b41, SUM(q.co60mm) as co60mm, " +
                "SUM(q.co82mm) as co82mm, SUM(q.co100mm) as co100mm, " +
                "SUM(q.spg9) as spg9, SUM(q.smpk_127mm) as smpk_127mm " +
                "FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sums.put("sung_ngan", rs.getInt("sung_ngan"));
                    sums.put("tieu_lien", rs.getInt("tieu_lien"));
                    sums.put("trung_lien", rs.getInt("trung_lien"));
                    sums.put("dai_lien", rs.getInt("dai_lien"));
                    sums.put("b41", rs.getInt("b41"));
                    sums.put("co60mm", rs.getInt("co60mm"));
                    sums.put("co82mm", rs.getInt("co82mm"));
                    sums.put("co100mm", rs.getInt("co100mm"));
                    sums.put("spg9", rs.getInt("spg9"));
                    sums.put("smpk_127mm", rs.getInt("smpk_127mm"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sums;
    }

    private Map<String, Double> fetchSavedRates(int sessionId) {
        Map<String, Double> rates = new HashMap<>();
        String sql = "SELECT loai_vktb, ti_le_hu_hong FROM step4_hu_hong_vktb WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rates.put(rs.getString("loai_vktb").trim().toLowerCase(), rs.getDouble("ti_le_hu_hong"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rates;
    }

    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        model.setRowCount(0);

        Map<String, Integer> weaponSums = fetchWeaponSums(sessionId);
        Map<String, Double> savedRates = fetchSavedRates(sessionId);

        // Hiển thị 10 dòng cố định
        addRowToModel("1", "Súng ngắn", weaponSums.getOrDefault("sung_ngan", 0), savedRates);
        addRowToModel("2", "Súng Tiểu liên", weaponSums.getOrDefault("tieu_lien", 0), savedRates);
        addRowToModel("3", "Súng Trung liên", weaponSums.getOrDefault("trung_lien", 0), savedRates);
        addRowToModel("4", "Súng Đại liên", weaponSums.getOrDefault("dai_lien", 0), savedRates);
        addRowToModel("5", "B41, M79", weaponSums.getOrDefault("b41", 0), savedRates);
        addRowToModel("6", "Cối 60mm", weaponSums.getOrDefault("co60mm", 0), savedRates);
        addRowToModel("7", "Cối 82mm", weaponSums.getOrDefault("co82mm", 0), savedRates);
        addRowToModel("8", "Cối 100mm", weaponSums.getOrDefault("co100mm", 0), savedRates);
        addRowToModel("9", "SPG-9", weaponSums.getOrDefault("spg9", 0), savedRates);
        addRowToModel("10", "SMPK 12,7", weaponSums.getOrDefault("smpk_127mm", 0), savedRates);
    }

    private void addRowToModel(String tt, String name, int sum, Map<String, Double> savedRates) {
        String key = name.trim().toLowerCase();
        double rate = savedRates.getOrDefault(key, 0.0);
        String strRate = (rate == (long) rate) ? String.format("%d", (long) rate) : String.valueOf(rate);
        model.addRow(new Object[]{tt, name, sum, strRate});
    }

    public boolean saveToDatabase(int sessionId) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM step4_hu_hong_vktb WHERE session_id = ?")) {
                    del.setInt(1, sessionId);
                    del.executeUpdate();
                }
                String sql = "INSERT INTO step4_hu_hong_vktb (session_id, loai_vktb, so_luong_tham_gia, ti_le_hu_hong) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < model.getRowCount(); i++) {
                        pstmt.setInt(1, sessionId);
                        pstmt.setString(2, model.getValueAt(i, 1).toString());
                        pstmt.setInt(3, InputValidator.parseIntSafe(model.getValueAt(i, 2)));
                        pstmt.setDouble(4, InputValidator.parseDoubleSafe(model.getValueAt(i, 3)));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                return false;
            }
        } catch (Exception e) { return false; }
    }

    private void setupRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
    }
}