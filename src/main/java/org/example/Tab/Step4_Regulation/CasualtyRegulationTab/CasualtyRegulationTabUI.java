package org.example.Tab.Step4_Regulation.CasualtyRegulationTab;

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

public class CasualtyRegulationTabUI extends JPanel {
    private final CasualtyRegulationTabService service = new CasualtyRegulationTabService();

    private DefaultTableModel model;
    private JTable table;

    public CasualtyRegulationTabUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);



        String[] cols = {"Loại thương binh, bệnh binh", "Tỉ lệ (%)"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setBorder(new LineBorder(new Color(200, 200, 200)));

        table.getColumnModel().getColumn(0).setPreferredWidth(800);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        setupRenderers();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        scroll.getViewport().setBackground(Color.WHITE);

        add(pnlControls, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);


    }

    public void loadDataFromDatabase(int sessionId) {
        model.setRowCount(0);

        if (sessionId == -1) {
            loadDefaultData();
            return;
        }

        List<CasualtyRegulationTabService.Row> loaded;
        try {
            loaded = service.loadThuongBinh(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultData();
            return;
        }

        boolean hasData = false;
        for (CasualtyRegulationTabService.Row r : loaded) {
            hasData = true;
            String hienThiTiLe = (r.tiLe > 0) ? String.valueOf(r.tiLe) : "";
            model.addRow(new Object[]{r.loaiThuongBinh, hienThiTiLe});
        }

        if (!hasData) {
            loadDefaultData();
        }
    }

    public boolean saveToDatabase(int sessionId) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<CasualtyRegulationTabService.Row> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String ten = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString().trim() : "";
            rows.add(new CasualtyRegulationTabService.Row(ten, InputValidator.parseDoubleSafe(model.getValueAt(i, 1))));
        }
        return service.saveThuongBinh(sessionId, rows);
    }

    private void loadDefaultData() {
        model.addRow(new Object[]{"Tỷ lệ thương binh toàn trận", "0"});
        model.addRow(new Object[]{"Tỷ lệ thương binh ngày cao nhất", "0"});
        model.addRow(new Object[]{"Tỷ lệ bệnh binh toàn trận", "0"});
        model.addRow(new Object[]{"Tỷ lệ thương binh hoá học", "0"});

    }

    private void setupRenderers() {
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(224, 224, 224)),
                        BorderFactory.createEmptyBorder(0, 15, 0, 0)
                ));
                if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
                if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
    }
}
