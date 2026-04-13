package org.example.Tab.PlanEstimation.Tab5_MaterialPanel;

import org.example.Tab.PlanEstimation.Tab5_MaterialPanel.Tab5_MaterialPanelService;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class Tab5_MaterialPanelUI extends JPanel {
    private final int sessionId;
    private final Tab5_MaterialPanelService service;

    private DefaultTableModel model1;
    private DefaultTableModel model2;
    private JTable table1;
    private JTable table2;
    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;
    private JTextArea txtSauChienDau;

    private final int[] colWidths2 = {50, 250, 80, 90, 90, 90, 90, 90, 90};

    public Tab5_MaterialPanelUI(int sessionId, Tab5_MaterialPanelService service) {
        this.sessionId = sessionId;
        this.service = service != null ? service : new Tab5_MaterialPanelService();

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

        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến khối lượng đạn, vật chất hậu cần, vật tư kỹ thuật"));
        mainContainer.add(createTable1Panel());
        mainContainer.add(Box.createVerticalStrut(30));

        mainContainer.add(UIUtils.createSectionLabel("2. Dự trữ vật chất hậu cần chi tiết (04.00N / sau chiến đấu) — tối đa 20 loại"));
        mainContainer.add(createTable2Panel());
        mainContainer.add(Box.createVerticalStrut(30));

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

        reloadTextFromDatabase();
        refreshTable1();
        refreshTable2();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshTable1();
                refreshTable2();
                reloadTextFromDatabase();
            }
        });
    }

    public void reloadTextFromDatabase() {
        Tab5_MaterialPanelService.Tab5TextFields f = new Tab5_MaterialPanelService.Tab5TextFields();
        service.loadTextsInto(f, sessionId);
        txtChuanBi.setText(f.chuanBi);
        txtChienDau.setText(f.chienDau);
        txtSauChienDau.setText(f.sauCd);
    }

    public void persistToDatabase() {
        service.saveTexts(sessionId, getTxtChuanBi(), getTxtChienDau(), getTxtSauChienDau());
    }

    public void refreshTable1() {
        model1.setRowCount(0);
        for (Object[] row : service.buildTable1Rows(sessionId)) {
            model1.addRow(row);
        }
    }

    /** Bảng 2 — chỉ đọc, Step 4 VCHC (9 cột, tối đa 20 dòng). */
    public void refreshTable2() {
        if (model2 == null) {
            return;
        }
        model2.setRowCount(0);
        for (Object[] row : service.buildTable2Rows(sessionId)) {
            model2.addRow(row);
        }
    }

    public String getTxtChuanBi() {
        return txtChuanBi.getText().trim();
    }

    public String getTxtChienDau() {
        return txtChienDau.getText().trim();
    }

    public String getTxtSauChienDau() {
        return txtSauChienDau.getText().trim();
    }

    public List<Object[]> getTable1Data() {
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < model1.getRowCount(); i++) {
            Object[] row = new Object[model1.getColumnCount()];
            for (int j = 0; j < model1.getColumnCount(); j++) {
                row[j] = model1.getValueAt(i, j);
            }
            data.add(row);
        }
        return data;
    }

    public List<Object[]> getTable2Data() {
        List<Object[]> data = new ArrayList<>();
        for (int i = 0; i < model2.getRowCount(); i++) {
            Object[] row = new Object[model2.getColumnCount()];
            for (int j = 0; j < model2.getColumnCount(); j++) {
                row[j] = model2.getValueAt(i, j);
            }
            data.add(row);
        }
        return data;
    }

    /** Truy cập service (xuất Word dùng {@link Tab5_MaterialPanelService#buildTable2Rows}). */
    public Tab5_MaterialPanelService getMaterialService() {
        return service;
    }

    private JPanel createTable1Panel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 220));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        String[] cols = {"Loại vật chất", "ĐVT", "Quy định dự trữ", "Hiện có", "Phải bổ sung"};
        model1 = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1 = new JTable(model1);
        table1.setRowHeight(35);
        table1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(table1);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        UIUtils.makeScrollPassThrough(scroll);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createTable2Panel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 560));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));

        String[] cols = {"TT", "Loại Vật chất", "ĐVT", "Kho d (04)", "Đơn vị (04)", "Tổng (04)", "Kho d (Sau)", "Đơn vị (Sau)", "Tổng (Sau)"};
        model2 = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table2 = new JTable(model2);
        table2.setRowHeight(35);
        table2.setTableHeader(null);
        table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table2.setFocusable(false);

        for (int i = 0; i < cols.length; i++) {
            table2.getColumnModel().getColumn(i).setPreferredWidth(colWidths2[i]);
            table2.getColumnModel().getColumn(i).setMinWidth(colWidths2[i]);
            table2.getColumnModel().getColumn(i).setCellRenderer(UIUtils.getStandardTableRenderer());
        }

        JScrollPane scrollPane = new JScrollPane(table2);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(scrollPane);

        JPanel headerPanel = createAbsoluteHeader2();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(ev ->
                headerViewport.setViewPosition(new Point(ev.getValue(), 0)));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        int sbWidth = UIManager.getInt("ScrollBar.width");
        if (sbWidth == 0) {
            sbWidth = 17;
        }
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

        return pnl;
    }

    private JPanel createAbsoluteHeader2() {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalW = 0;
        for (int w : colWidths2) {
            totalW += w;
        }
        p.setPreferredSize(new Dimension(totalW, 70));

        int[] x = new int[colWidths2.length + 1];
        x[0] = 0;
        for (int i = 0; i < colWidths2.length; i++) {
            x[i + 1] = x[i] + colWidths2[i];
        }

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
