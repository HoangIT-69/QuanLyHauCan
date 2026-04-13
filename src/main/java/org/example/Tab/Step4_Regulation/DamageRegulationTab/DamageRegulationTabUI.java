package org.example.Tab.Step4_Regulation.DamageRegulationTab;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DamageRegulationTabUI extends JPanel {
    private final DamageRegulationTabService service = new DamageRegulationTabService();

    private DefaultTableModel model;
    private JTable table;
    private int currentSessionId = -1;

    public DamageRegulationTabUI() {
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

    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        model.setRowCount(0);

        Map<String, Integer> weaponSums = service.fetchWeaponSums(sessionId);
        Map<String, Double> savedRates = service.fetchSavedRates(sessionId);

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
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        List<DamageRegulationTabService.SaveRow> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            rows.add(new DamageRegulationTabService.SaveRow(
                    model.getValueAt(i, 1).toString(),
                    InputValidator.parseIntSafe(model.getValueAt(i, 2)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, 3))
            ));
        }
        return service.saveHuHongVktbBatch(sessionId, rows);
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
