package org.example.Tab.PlanEstimation;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Tab5_MaterialPanel extends JPanel {
    private DefaultTableModel model1, model2;
    private JTable table1, table2;
    private JTextArea txtChuanBi, txtChienDau, txtSauChienDau;

    private boolean isUpdatingTable1 = false;
    private boolean isUpdatingTable2 = false;

    private final int[] colWidths2 = {50, 250, 80, 90, 90, 90, 90, 90, 90};

    public Tab5_MaterialPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("V. BẢO ĐẢM ĐẠN, VẬT CHẤT HẬU CẦN, VẬT TƯ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // Mục 1: Bảng 1
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến khối lượng đạn, vật chất hậu cần, vật tư kỹ thuật"));
        mainContainer.add(createTable1Panel());
        mainContainer.add(Box.createVerticalStrut(30));

        // Mục 2: Bảng 2
        mainContainer.add(UIUtils.createSectionLabel("2. Phân cấp dự trữ một số loại vật chất chủ yếu"));
        mainContainer.add(createTable2Panel());
        mainContainer.add(Box.createVerticalStrut(30));

        // Mục 3: Ý định
        mainContainer.add(UIUtils.createSectionLabel("3. Ý định tổ chức tiếp nhận, bổ sung"));

        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        txtChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtChuanBi, 120));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        txtChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtChienDau, 120));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        txtSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSauChienDau, 120));
        mainContainer.add(Box.createVerticalStrut(20));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================
    public String getTxtChuanBi() { return txtChuanBi.getText().trim(); }
    public String getTxtChienDau() { return txtChienDau.getText().trim(); }
    public String getTxtSauChienDau() { return txtSauChienDau.getText().trim(); }

    public List<Object[]> getTable1Data() {
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < model1.getRowCount(); i++) {
            Object[] row = new Object[model1.getColumnCount()];
            for (int j = 0; j < model1.getColumnCount(); j++) row[j] = model1.getValueAt(i, j);
            data.add(row);
        }
        return data;
    }

    public List<Object[]> getTable2Data() {
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < model2.getRowCount(); i++) {
            Object[] row = new Object[model2.getColumnCount()];
            for (int j = 0; j < model2.getColumnCount(); j++) row[j] = model2.getValueAt(i, j);
            data.add(row);
        }
        return data;
    }

    // =====================================================================
    // LOGIC GIAO DIỆN BẢNG
    // =====================================================================

    private JPanel createTable1Panel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 250));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAdd); pnlControls.add(btnDel);
        pnl.add(pnlControls, BorderLayout.NORTH);

        String[] cols = {"Loại vật chất", "ĐVT", "Quy định dự trữ", "Hiện có", "Phải bổ sung"};
        model1 = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column != 4; }
        };
        table1 = new JTable(model1);
        table1.setRowHeight(35);
        table1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        model1.addTableModelListener(e -> {
            if (!isUpdatingTable1 && e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 2 || e.getColumn() == 3)) {
                isUpdatingTable1 = true;
                int r = e.getFirstRow();
                double duTru = InputValidator.parseDoubleSafe(model1.getValueAt(r, 2));
                double hienCo = InputValidator.parseDoubleSafe(model1.getValueAt(r, 3));
                double boSung = duTru - hienCo;
                model1.setValueAt(boSung > 0 ? String.valueOf(boSung) : "0", r, 4);
                isUpdatingTable1 = false;
            }
        });

        JScrollPane scroll = new JScrollPane(table1);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        UIUtils.makeScrollPassThrough(scroll);
        pnl.add(scroll, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            if (model1.getRowCount() >= 10) {
                JOptionPane.showMessageDialog(this, "Tối đa 10 dòng để phù hợp mẫu Word.");
                return;
            }
            model1.addRow(new Object[]{"", "", "0", "0", "0"});
        });
        btnDel.addActionListener(e -> { if (table1.getSelectedRow() != -1) model1.removeRow(table1.getSelectedRow()); });

        return pnl;
    }

    private JPanel createTable2Panel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 300));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm dòng", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAdd); pnlControls.add(btnDel);
        pnl.add(pnlControls, BorderLayout.NORTH);

        String[] cols = {"TT", "Loại Vật chất", "ĐVT", "Kho d (04)", "Đơn vị (04)", "Tổng (04)", "Kho d (Sau)", "Đơn vị (Sau)", "Tổng (Sau)"};
        model2 = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col != 5 && col != 8; }
        };
        table2 = new JTable(model2);
        table2.setRowHeight(35);
        table2.setTableHeader(null);
        table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < cols.length; i++) {
            table2.getColumnModel().getColumn(i).setPreferredWidth(colWidths2[i]);
            table2.getColumnModel().getColumn(i).setMinWidth(colWidths2[i]);
            table2.getColumnModel().getColumn(i).setCellRenderer(UIUtils.getStandardTableRenderer());
        }

        model2.addTableModelListener(e -> {
            if (!isUpdatingTable2 && e.getType() == TableModelEvent.UPDATE) {
                isUpdatingTable2 = true;
                int r = e.getFirstRow(), c = e.getColumn();
                if (c == 3 || c == 4) {
                    double k = InputValidator.parseDoubleSafe(model2.getValueAt(r, 3));
                    double d = InputValidator.parseDoubleSafe(model2.getValueAt(r, 4));
                    model2.setValueAt(String.valueOf(k + d), r, 5);
                }
                if (c == 6 || c == 7) {
                    double k = InputValidator.parseDoubleSafe(model2.getValueAt(r, 6));
                    double d = InputValidator.parseDoubleSafe(model2.getValueAt(r, 7));
                    model2.setValueAt(String.valueOf(k + d), r, 8);
                }
                isUpdatingTable2 = false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table2);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(scrollPane);

        JPanel headerPanel = createAbsoluteHeader2();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        int sbWidth = UIManager.getInt("ScrollBar.width");
        if (sbWidth == 0) sbWidth = 17;
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(sbWidth, 70));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedTablePanel = new JPanel(new BorderLayout());
        combinedTablePanel.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        combinedTablePanel.add(headerWrapper, BorderLayout.NORTH);
        combinedTablePanel.add(scrollPane, BorderLayout.CENTER);

        pnl.add(combinedTablePanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            if (model2.getRowCount() >= 10) {
                JOptionPane.showMessageDialog(this, "Tối đa 10 dòng để phù hợp mẫu Word.");
                return;
            }
            model2.addRow(new Object[]{model2.getRowCount() + 1, "", "", "0", "0", "0", "0", "0", "0"});
        });
        btnDel.addActionListener(e -> { if (table2.getSelectedRow() != -1) model2.removeRow(table2.getSelectedRow()); });

        return pnl;
    }

    private JPanel createAbsoluteHeader2() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalW = 0; for(int w : colWidths2) totalW += w;
        p.setPreferredSize(new Dimension(totalW, 70));

        int[] x = new int[colWidths2.length + 1]; x[0] = 0;
        for (int i = 0; i < colWidths2.length; i++) x[i+1] = x[i] + colWidths2[i];

        int w04 = colWidths2[3] + colWidths2[4] + colWidths2[5];
        int wSau = colWidths2[6] + colWidths2[7] + colWidths2[8];

        p.add(UIUtils.createAbsoluteHeaderLabel("TT", x[0], 0, colWidths2[0], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Loại Vật chất", x[1], 0, colWidths2[1], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐVT", x[2], 0, colWidths2[2], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phải có 04.00N", x[3], 0, w04, 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phải có sau chiến đấu", x[6], 0, wSau, 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho d", x[3], 35, colWidths2[3], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Đơn vị", x[4], 35, colWidths2[4], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[5], 35, colWidths2[5], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho d", x[6], 35, colWidths2[6], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Đơn vị", x[7], 35, colWidths2[7], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[8], 35, colWidths2[8], 35));
        return p;
    }
}