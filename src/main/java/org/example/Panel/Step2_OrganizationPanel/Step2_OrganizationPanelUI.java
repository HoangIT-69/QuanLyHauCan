package org.example.Panel.Step2_OrganizationPanel;

import org.example.Panel.DataDeclarationContext;
import org.example.Popup.TotalDataEntryDialog.TotalDataEntryDialogUI;
import org.example.Popup.UnitDataEntryDialog.UnitDataEntryDialogService;
import org.example.Popup.UnitDataEntryDialog.UnitDataEntryDialogUI;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Step2_OrganizationPanelUI extends JPanel {
    private final Step2_OrganizationPanelService step2Service = new Step2_OrganizationPanelService();
    private final DataDeclarationContext parent;
    private final String hinhThucTapBai;
    private final List<String> directionLabels;

    private JPanel sandboxPanel;
    private Map<String, JButton> unitButtons = new HashMap<>();
    private JComboBox<String> cbUnitTypes;

    public Step2_OrganizationPanelUI(DataDeclarationContext parent, String hinhThucTapBai) {
        this.parent = parent;
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";
        this.directionLabels = step2Service.getDirectionsForHinhThuc(this.hinhThucTapBai);

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        cbUnitTypes = new JComboBox<>(directionLabels.toArray(new String[0]));
        cbUnitTypes.setPreferredSize(new Dimension(280, 35));

        JButton btnAdd = UIUtils.createStyledButton("Thêm bộ phận", new Color(46, 204, 113));
        JButton btnReset = UIUtils.createStyledButton("Làm mới sa bàn", new Color(149, 165, 166));

        toolBar.add(new JLabel("Chọn bộ phận:"));
        toolBar.add(cbUnitTypes);
        toolBar.add(btnAdd);
        toolBar.add(btnReset);
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
        btnNext.addActionListener(e -> parent.navigateStep(3));

        bottomPanel.add(btnBack, BorderLayout.WEST);
        bottomPanel.add(btnNext, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            String selected = (String) cbUnitTypes.getSelectedItem();
            if (selected != null && !unitButtons.containsKey(selected)) {
                createEnhancedUnit(selected);
                repositionAllUnits();
            }
        });

        btnReset.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Xóa toàn bộ cấu hình hiện tại?", "Xác nhận", JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) {
                step2Service.clearStep2ForSession(parent.getCurrentSessionId());
                loadAndRestoreUnits();
            }
        });

        loadAndRestoreUnits();
    }

    private void loadAndRestoreUnits() {
        int sid = parent.getCurrentSessionId();
       

        unitButtons.clear();
        sandboxPanel.removeAll();
        UnitDataEntryDialogService.clearSharedStore();

        if (sid <= 0) {
            initDefaultUnits();
            repositionAllUnits();
            return;
        }

        Set<String> fromDb = step2Service.loadDistinctHuongFromDb(sid);
        Map<String, Vector<Vector<Object>>> store = UnitDataEntryDialogService.getSharedStore();
        for (String huong : fromDb) {
            store.put(huong, new Vector<>());
        }

        initDefaultUnits();
        for (String key : store.keySet()) {
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
        boolean isVip = "Tiểu đoàn".equals(name) || "Phối thuộc".equals(name);
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
                    if (isVip) {
                        return;
                    }
                    if (JOptionPane.showConfirmDialog(null, "Xóa bộ phận này khỏi hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION)
                            == JOptionPane.YES_OPTION) {
                        step2Service.deleteUnitFromDatabase(parent.getCurrentSessionId(), name);
                        unitButtons.remove(name);
                        UnitDataEntryDialogService.getSharedStore().remove(name);
                        sandboxPanel.remove(btn);
                        sandboxPanel.repaint();
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Frame f = (Frame) SwingUtilities.getWindowAncestor(Step2_OrganizationPanelUI.this);
                    int sid = parent.getCurrentSessionId();

                    if ("Tiểu đoàn".equals(name)) {
                        new TotalDataEntryDialogUI(f, "Tiểu đoàn", sid, directionLabels).setVisible(true);
                    } else if ("Phối thuộc".equals(name)) {
                        new TotalDataEntryDialogUI(f, "Trung đoàn", sid, directionLabels).setVisible(true);
                    } else {
                        new UnitDataEntryDialogUI(f, name, sid, directionLabels).setVisible(true);
                    }
                }
            }
        });
        unitButtons.put(name, btn);
        sandboxPanel.add(btn);
    }

    private void repositionAllUnits() {
        int w = sandboxPanel.getWidth();
        if (w <= 100) {
            return;
        }

        try {
            if (unitButtons.containsKey("Tiểu đoàn")) {
                unitButtons.get("Tiểu đoàn").setBounds(w / 2 - 280, 60, 250, 65);
            }
            if (unitButtons.containsKey("Phối thuộc")) {
                unitButtons.get("Phối thuộc").setBounds(w / 2 + 30, 60, 250, 65);
            }

            int n = directionLabels.size();
            int margin = 12;
            int btnW = Math.max(130, Math.min(220, (w - margin * 2) / Math.max(1, n) - margin));

            int rows = n <= 4 ? 1 : 2;
            int perRow = (n + rows - 1) / rows;
            int y1 = 200;
            int y2 = 300;
            int idx = 0;
            for (int r = 0; r < rows; r++) {
                int count = Math.min(perRow, n - idx);
                if (count <= 0) {
                    break;
                }
                int totalW = count * btnW + (count - 1) * margin;
                int startX = Math.max(10, (w - totalW) / 2);
                int y = r == 0 ? y1 : y2;
                for (int c = 0; c < count; c++) {
                    String name = directionLabels.get(idx++);
                    JButton b = unitButtons.get(name);
                    if (b != null) {
                        b.setBounds(startX + c * (btnW + margin), y, btnW, 55);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean saveStep2ToDatabase() {
        int sid = parent.getCurrentSessionId();
        if (sid <= 0) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy SessionID!");
            return false;
        }

        Map<String, Vector<Vector<Object>>> store = UnitDataEntryDialogService.getSharedStore();
        if (store == null || store.isEmpty()) {
            System.out.println("Cảnh báo: Dữ liệu unitDataStore đang trống. Bỏ qua lệnh Lưu để bảo toàn Database.");
            return true;
        }

        boolean ok = step2Service.saveStep2FromRamStore(sid);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu biên chế. Dữ liệu cũ đã được khôi phục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean saveToDatabase() {
        return saveStep2ToDatabase();
    }

    private void initDefaultUnits() {
        if (!unitButtons.containsKey("Tiểu đoàn")) {
            createEnhancedUnit("Tiểu đoàn");
        }
        if (!unitButtons.containsKey("Phối thuộc")) {
            createEnhancedUnit("Phối thuộc");
        }
    }
}
