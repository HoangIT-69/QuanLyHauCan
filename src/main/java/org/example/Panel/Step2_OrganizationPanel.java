package org.example.Panel;

import org.example.Popup.TotalDataEntryDialog;
import org.example.Popup.UnitDataEntryDialog;
import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class Step2_OrganizationPanel extends JPanel {
    private DataDeclarationPanel parent;
    private JPanel sandboxPanel;
    private Map<String, JButton> unitButtons = new HashMap<>();

    public Step2_OrganizationPanel(DataDeclarationPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        String[] unitTypes = {"Hướng chủ yếu", "Hướng thứ yếu", "Phòng ngự phía sau", "Lực lượng còn lại"};
        JComboBox<String> cbUnitTypes = new JComboBox<>(unitTypes);
        cbUnitTypes.setPreferredSize(new Dimension(200, 35));

        JButton btnAdd = UIUtils.createStyledButton("Thêm bộ phận", new Color(46, 204, 113));
        JButton btnReset = UIUtils.createStyledButton("Làm mới sa bàn", new Color(149, 165, 166));

        toolBar.add(new JLabel("Chọn bộ phận:"));
        toolBar.add(cbUnitTypes); toolBar.add(btnAdd); toolBar.add(btnReset);
        add(toolBar, BorderLayout.NORTH);

        sandboxPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 30));
                g2d.setColor(new Color(230, 235, 240));
                String watermark = "BỐ TRÍ ĐỘI HÌNH CHIẾN ĐẤU";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(watermark, (getWidth() - fm.stringWidth(watermark)) / 2, getHeight() / 2 + 50);
            }
        };
        sandboxPanel.setBackground(Color.WHITE);
        add(sandboxPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JButton btnBack = UIUtils.createNavButtonWithIcon("Quay lại", new Color(149, 165, 166), "/images/return.png", false);
        JButton btnNext = UIUtils.createNavButtonWithIcon("Tiếp tục", new Color(41, 128, 185), "/images/next.png", true);

        btnBack.addActionListener(e -> parent.navigateStep(1));
        btnNext.addActionListener(e -> {
            if (saveStep2ToDatabase()) parent.navigateStep(3);
        });

        bottomPanel.add(btnBack, BorderLayout.WEST);
        bottomPanel.add(btnNext, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            String selected = (String) cbUnitTypes.getSelectedItem();
            if (!unitButtons.containsKey(selected)) {
                createEnhancedUnit(selected);
                repositionAllUnits();
            }
        });

        btnReset.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Xóa toàn bộ cấu hình hiện tại?", "Xác nhận", 0) == 0) {
                clearDataInDB(parent.getCurrentSessionId());
                loadAndRestoreUnits();
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadAndRestoreUnits();
            }
        });
    }

    private void loadAndRestoreUnits() {
        int sid = parent.getCurrentSessionId();
        System.out.println("DEBUG: Step 2 đang nạp SessionID = " + sid);

        unitButtons.clear();
        sandboxPanel.removeAll();
        UnitDataEntryDialog.unitDataStore.clear();

        if (sid <= 0) {
            initDefaultUnits();
            repositionAllUnits();
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT huong FROM step2_bien_che WHERE session_id = ?")) {
            ps.setInt(1, sid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String huong = rs.getString("huong");
                UnitDataEntryDialog.unitDataStore.put(huong, new java.util.Vector<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initDefaultUnits();
        for (String key : UnitDataEntryDialog.unitDataStore.keySet()) {
            if (!unitButtons.containsKey(key)) {
                createEnhancedUnit(key);
            }
        }

        SwingUtilities.invokeLater(() -> {
            repositionAllUnits();
            sandboxPanel.revalidate();
            sandboxPanel.repaint();
        });
    }

    private void createEnhancedUnit(String name) {
        boolean isVip = name.equals("Tiểu đoàn") || name.equals("Phối thuộc");
        JButton btn = new JButton(name.toUpperCase());
        btn.setFont(new Font("Segoe UI", Font.BOLD, isVip ? 15 : 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(isVip ? new Color(30, 58, 138) : new Color(71, 85, 105));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.WHITE, 2));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (isVip) return;
                    if (JOptionPane.showConfirmDialog(null, "Xóa bộ phận này khỏi hệ thống?", "Xác nhận", 0) == 0) {
                        deleteUnitFromDB(parent.getCurrentSessionId(), name);
                        unitButtons.remove(name);
                        UnitDataEntryDialog.unitDataStore.remove(name);
                        sandboxPanel.remove(btn);
                        sandboxPanel.repaint();
                    }
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Frame f = (Frame) SwingUtilities.getWindowAncestor(Step2_OrganizationPanel.this);
                    int sid = parent.getCurrentSessionId();

                    if (name.equals("Tiểu đoàn")) {
                        new TotalDataEntryDialog(f, "Tiểu đoàn", sid).setVisible(true);
                    } else if (name.equals("Phối thuộc")) {
                        new TotalDataEntryDialog(f, "Trung đoàn", sid).setVisible(true);
                    } else {
                        new UnitDataEntryDialog(f, name, sid).setVisible(true);
                    }
                }
            }
        });
        unitButtons.put(name, btn);
        sandboxPanel.add(btn);
    }

    private void repositionAllUnits() {
        int w = sandboxPanel.getWidth();
        if (w <= 100) return;

        try {
            if (unitButtons.containsKey("Tiểu đoàn")) unitButtons.get("Tiểu đoàn").setBounds(w/2 - 280, 60, 250, 65);
            if (unitButtons.containsKey("Phối thuộc")) unitButtons.get("Phối thuộc").setBounds(w/2 + 30, 60, 250, 65);

            int midY = 250;
            if (unitButtons.containsKey("Hướng chủ yếu")) unitButtons.get("Hướng chủ yếu").setBounds(w/2 - 350, midY, 220, 55);
            if (unitButtons.containsKey("Hướng thứ yếu")) unitButtons.get("Hướng thứ yếu").setBounds(w/2 - 110, midY, 220, 55);
            if (unitButtons.containsKey("Phòng ngự phía sau")) unitButtons.get("Phòng ngự phía sau").setBounds(w/2 + 130, midY, 220, 55);
            if (unitButtons.containsKey("Lực lượng còn lại")) unitButtons.get("Lực lượng còn lại").setBounds(w/2 - 110, 420, 220, 55);
        } catch (Exception e) {}
    }

    private boolean saveStep2ToDatabase() {
        int sid = parent.getCurrentSessionId();
        if (sid <= 0) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy SessionID!");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM step2_bien_che WHERE session_id = ?")) {
                del.setInt(1, sid);
                del.executeUpdate();
            }

            // Đã cập nhật phan_loai vào SQL
            String sql = "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, (SELECT id FROM quyuoc_bienche WHERE CONCAT('[', nhom_don_vi, '] ', ten_don_vi) = ? LIMIT 1), ?)";
            String sqlFallback = "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, 1, 1)";

            try (PreparedStatement ins = conn.prepareStatement(sql);
                 PreparedStatement insFallback = conn.prepareStatement(sqlFallback)) {

                for (String huong : UnitDataEntryDialog.unitDataStore.keySet()) {
                    if (huong.equals("Tiểu đoàn") || huong.equals("Phối thuộc")) continue;

                    Vector<Vector<Object>> rows = UnitDataEntryDialog.unitDataStore.get(huong);
                    boolean hasUnits = false;

                    if (rows != null && !rows.isEmpty()) {
                        int currentLoai = 1; // 1 = Biên chế, 2 = Tăng cường
                        for (Vector<Object> row : rows) {
                            if (row.isEmpty() || row.get(0) == null) continue;
                            String rawName = row.get(0).toString();

                            // Tự động phân loại dựa trên dòng đang duyệt
                            if (rawName.startsWith("1.")) { currentLoai = 1; continue; }
                            if (rawName.startsWith("2.")) { currentLoai = 2; continue; }
                            if (rawName.equals("TỔNG CỘNG")) continue;

                            String name = rawName.replace("  + ", "").trim();

                            if (name.startsWith("[") && name.contains("]")) {
                                ins.setInt(1, sid);
                                ins.setString(2, huong);
                                ins.setString(3, name);
                                ins.setInt(4, currentLoai); // Lưu 1 hoặc 2
                                ins.addBatch();
                                hasUnits = true;
                            }
                        }
                    }

                    if (!hasUnits) {
                        insFallback.setInt(1, sid);
                        insFallback.setString(2, huong);
                        insFallback.addBatch();
                    }
                }
                ins.executeBatch();
                insFallback.executeBatch();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteUnitFromDB(int sid, String huong) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM step2_bien_che WHERE session_id = ? AND huong = ?")) {
            ps.setInt(1, sid);
            ps.setString(2, huong);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearDataInDB(int sid) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM step2_bien_che WHERE session_id = ?")) {
            ps.setInt(1, sid);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initDefaultUnits() {
        if (!unitButtons.containsKey("Tiểu đoàn")) createEnhancedUnit("Tiểu đoàn");
        if (!unitButtons.containsKey("Phối thuộc")) createEnhancedUnit("Phối thuộc");
    }
}