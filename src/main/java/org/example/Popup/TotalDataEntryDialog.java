package org.example.Popup;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TotalDataEntryDialog extends JDialog {
    private final int[] colWidths = {300, 80, 60, 60, 60, 60, 80, 70, 60, 60, 60, 70, 70, 70, 70};
    private DefaultTableModel model;
    private JTable table;
    private String filterGroup;
    private int sessionId;

    public TotalDataEntryDialog(Frame owner, String filterGroup, int sessionId) {
        super(owner, "Tổng hợp dữ liệu: " + (filterGroup.equals("Tiểu đoàn") ? "BIÊN CHẾ NỘI BỘ" : "LỰC LƯỢNG PHỐI THUỘC"), true);
        this.filterGroup = filterGroup;
        this.sessionId = sessionId;

        System.out.println("DEBUG: TotalDataEntryDialog đang nạp SessionID = " + sessionId + " | Cấp: " + filterGroup);

        setSize(1350, 850);
        setLocationRelativeTo(owner);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        String titleText = filterGroup.equals("Tiểu đoàn") ? "BẢNG TỔNG HỢP BIÊN CHẾ NỘI BỘ TIỂU ĐOÀN" : "BẢNG TỔNG HỢP LỰC LƯỢNG PHỐI THUỘC (TRUNG ĐOÀN)";
        JLabel lblMainTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        container.add(lblMainTitle, BorderLayout.NORTH);

        String[] columnNames = {"LỰC LƯỢNG", "QUÂN SỐ", "SN", "TL", "TrL", "ĐL", "B41, M79", "Lựu đạn", "60", "82", "100", "SPG9", "12.7", "ON", "ĐB"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        loadFilteredAggregatedData();

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
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(17, 100));
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

    private void loadFilteredAggregatedData() {
        model.setRowCount(0);
        int[] grandTotal = new int[15];

        Map<String, java.util.List<Object[]>> groupedData = new LinkedHashMap<>();

        String sql = "SELECT s.huong, q.* FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ? ORDER BY s.huong";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String huong = rs.getString("huong");
                String nhom = rs.getString("nhom_don_vi");

                if (nhom.equalsIgnoreCase(this.filterGroup) || (this.filterGroup.equals("Tiểu đoàn") && nhom.contains("Tiểu đoàn"))) {
                    Object[] row = new Object[15];
                    row[0] = "[" + nhom + "] " + rs.getString("ten_don_vi");
                    row[1] = rs.getInt("quan_so");
                    row[2] = rs.getInt("sung_ngan");
                    row[3] = rs.getInt("tieu_lien");
                    row[4] = rs.getInt("trung_lien");
                    row[5] = rs.getInt("dai_lien");
                    row[6] = rs.getInt("b41");
                    row[7] = rs.getInt("luu_dan");
                    row[8] = rs.getInt("co60mm");
                    row[9] = rs.getInt("co82mm");
                    row[10] = rs.getInt("co100mm");
                    row[11] = rs.getInt("spg9");
                    row[12] = rs.getInt("smpk_127mm");
                    row[13] = 0;
                    row[14] = 0;

                    groupedData.computeIfAbsent(huong, k -> new ArrayList<>()).add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (groupedData.isEmpty()) {
            model.addRow(new Object[]{"KHÔNG CÓ DỮ LIỆU KHỚP VỚI " + filterGroup.toUpperCase(), "0","0","0","0","0","0","0","0","0","0","0","0","0","0"});
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
        for (int i = 1; i < 15; i++) totalRow[i] = String.valueOf(grandTotal[i]);
        model.addRow(totalRow);
    }

    private JPanel createPerfectHeader() {
        int totalWidth = 0; for(int w : colWidths) totalWidth += w;
        JPanel p = new JPanel(null); p.setPreferredSize(new Dimension(totalWidth, 100)); p.setBackground(Color.WHITE);
        int[] x = new int[16]; x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) x[i + 1] = x[i] + colWidths[i];

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
        int[] subX = {2,3,4,5,6,8,9,10,11,12};
        for(int i=0; i<subCols.length; i++) p.add(UIUtils.createAbsoluteHeaderLabel(subCols[i], x[subX[i]], 60, colWidths[subX[i]], 40));
        return p;
    }
}