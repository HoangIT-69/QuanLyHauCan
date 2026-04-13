package org.example.Popup.TotalDataEntryDialog;

import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TotalDataEntryDialogUI extends JDialog {

    private final TotalDataEntryDialogService totalService = new TotalDataEntryDialogService();
    private final int[] colWidths = {300, 80, 60, 60, 60, 60, 80, 70, 60, 60, 60, 70, 70, 70, 70};
    private final String filterGroup;
    private final int sessionId;
    private final List<String> allDirections;

    private DefaultTableModel model;
    private JTable table;
    private JComboBox<String> cbHuong;

    public TotalDataEntryDialogUI(Frame owner, String filterGroup, int sessionId, List<String> allDirections) {
        super(owner, "Tổng hợp dữ liệu: " + (filterGroup.equals("Tiểu đoàn") ? "BIÊN CHẾ NỘI BỘ" : "LỰC LƯỢNG PHỐI THUỘC"), true);
        this.filterGroup = filterGroup;
        this.sessionId = sessionId;
        this.allDirections = allDirections;

        System.out.println("DEBUG: TotalDataEntryDialogUI SessionID = " + sessionId + " | Cấp: " + filterGroup);

        setSize(1350, 850);
        setLocationRelativeTo(owner);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setBackground(Color.WHITE);

        String titleText = filterGroup.equals("Tiểu đoàn")
                ? "BẢNG TỔNG HỢP BIÊN CHẾ NỘI BỘ TIỂU ĐOÀN"
                : "BẢNG TỔNG HỢP LỰC LƯỢNG PHỐI THUỘC (TRUNG ĐOÀN)";
        JLabel lblMainTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        north.add(lblMainTitle);
        north.add(Box.createVerticalStrut(10));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filterRow.setBackground(Color.WHITE);
        filterRow.add(new JLabel("Lọc theo hướng / lực lượng:"));
        Vector<String> opts = new Vector<>();
        opts.add("Tất cả");
        opts.addAll(allDirections);
        cbHuong = new JComboBox<>(opts);
        cbHuong.setPreferredSize(new Dimension(380, 32));
        filterRow.add(cbHuong);
        north.add(filterRow);

        container.add(north, BorderLayout.NORTH);

        String[] columnNames = {"LỰC LƯỢNG", "QUÂN SỐ", "SN", "TL", "TrL", "ĐL", "B41, M79", "Lựu đạn", "60", "82", "100", "SPG9", "12.7", "ON", "ĐB"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refillTable();
        cbHuong.addActionListener(e -> refillTable());

        table = new JTable(model);
        table.setRowHeight(40);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(UIUtils.getStandardTableRenderer());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(100, 116, 139), 2));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(createPerfectHeader(), BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(17, 100));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combined = new JPanel(new BorderLayout());
        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);

        container.add(combined, BorderLayout.CENTER);

        JButton btnClose = UIUtils.createNavButton("Đóng bảng", new Color(148, 163, 184));
        btnClose.addActionListener(e -> dispose());
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlSouth.add(btnClose);
        container.add(pnlSouth, BorderLayout.SOUTH);

        add(container);
    }

    private void refillTable() {
        if (model == null) {
            return;
        }
        String huongFilter = null;
        if (cbHuong != null) {
            Object sel = cbHuong.getSelectedItem();
            if (sel != null && !"Tất cả".equals(sel.toString())) {
                huongFilter = sel.toString();
            }
        }

        TotalDataEntryDialogService.AggregatedLoadResult loadResult =
                totalService.loadAggregated(sessionId, filterGroup, huongFilter);
        Map<String, List<Object[]>> groupedData = new LinkedHashMap<>(loadResult.getGroupedData());

        model.setRowCount(0);
        int[] grandTotal = new int[15];

        if (groupedData.isEmpty()) {
            model.addRow(new Object[]{"KHÔNG CÓ DỮ LIỆU KHỚP VỚI " + filterGroup.toUpperCase(),
                    "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"});
            if (table != null) {
                table.repaint();
            }
            return;
        }

        for (String huong : groupedData.keySet()) {
            model.addRow(new Object[]{huong.toUpperCase(), "", "", "", "", "", "", "", "", "", "", "", "", "", ""});

            for (Object[] rowData : groupedData.get(huong)) {
                Object[] displayRow = new Object[15];
                displayRow[0] = "      " + rowData[0];
                for (int i = 1; i < 15; i++) {
                    int val = InputValidator.parseIntSafe(rowData[i]);
                    displayRow[i] = String.valueOf(val);
                    grandTotal[i] += val;
                }
                model.addRow(displayRow);
            }
        }

        Object[] totalRow = new Object[15];
        totalRow[0] = "TỔNG CỘNG " + filterGroup.toUpperCase();
        for (int i = 1; i < 15; i++) {
            totalRow[i] = String.valueOf(grandTotal[i]);
        }
        model.addRow(totalRow);

        if (table != null) {
            table.repaint();
        }
    }

    private JPanel createPerfectHeader() {
        int totalWidth = 0;
        for (int w : colWidths) {
            totalWidth += w;
        }
        JPanel p = new JPanel(null);
        p.setPreferredSize(new Dimension(totalWidth, 100));
        p.setBackground(Color.WHITE);
        int[] x = new int[16];
        x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) {
            x[i + 1] = x[i] + colWidths[i];
        }

        p.add(UIUtils.createAbsoluteHeaderLabel("LỰC LƯỢNG", x[0], 0, colWidths[0], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>QUÂN<br>SỐ</center></html>", x[1], 0, colWidths[1], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("TRANG BỊ KỸ THUẬT", x[2], 0, x[13] - x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("VẬT TƯ KỸ THUẬT", x[13], 0, x[15] - x[13], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Vũ khí bộ binh", x[2], 30, x[7] - x[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Lựu đạn", x[7], 30, colWidths[7], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cối", x[8], 30, x[11] - x[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PCT", x[11], 30, colWidths[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Pháo PK", x[12], 30, colWidths[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ON", x[13], 30, colWidths[13], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐB", x[14], 30, colWidths[14], 70));
        String[] subCols = {"SN", "TL", "TrL", "ĐL", "B41", "60", "82", "100", "SPG9", "12.7"};
        int[] subX = {2, 3, 4, 5, 6, 8, 9, 10, 11, 12};
        for (int i = 0; i < subCols.length; i++) {
            p.add(UIUtils.createAbsoluteHeaderLabel(subCols[i], x[subX[i]], 60, colWidths[subX[i]], 40));
        }
        return p;
    }
}
