package org.example.Tab.AssurancePlan;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab4_EquipmentPanel extends JPanel {

    private final int[] colWidths = {120, 220, 60, 70, 70, 70, 60, 60, 110, 80, 110, 110, 110};

    private DefaultTableModel model;
    private JTable table;

    private JTextArea txtGiaiDoanChuanBi;
    private JTextArea txtGiaiDoanChienDau;
    private JTextArea txtSauChienDau;

    // Lưu lại Session ID để nút Refresh sử dụng
    private int currentSessionId = -1;

    public Tab4_EquipmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("IV. BẢO ĐẢM TRANG BỊ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(20));

        mainContainer.add(createTableSection());
        mainContainer.add(Box.createVerticalStrut(25));
        mainContainer.add(createMeasuresSection());

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createTableSection() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableContainer.setPreferredSize(new Dimension(1000, 400));
        tableContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(UIUtils.createSectionLabel("1. Chỉ tiêu"), BorderLayout.WEST);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlControls.setBackground(Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("🔄 Làm mới dữ liệu", new Color(46, 204, 113));
        pnlControls.add(btnRefresh);
        topPanel.add(pnlControls, BorderLayout.EAST);

        tableContainer.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Đơn vị", "Tên TBKT", "ĐVT", "Nhu cầu", "Tổng số", "Số tốt", "K_bđ", "K_t", "Phải có", "Số lượng", "Thời gian", "Địa điểm", "Phương thức"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column > 0; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Áp dụng viền và KÍCH THƯỚC CHUẨN để không bị lệch cột
        setupCustomRenderers();

        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        headerViewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(tableScroll);

        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e ->
                headerViewport.setViewPosition(new Point(e.getValue(), 0))
        );

        JPanel combinedTablePanel = new JPanel(new BorderLayout());
        combinedTablePanel.setBorder(new LineBorder(new Color(203, 213, 225), 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(scrollWidth, 60));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combinedTablePanel.add(headerWrapper, BorderLayout.NORTH);
        combinedTablePanel.add(tableScroll, BorderLayout.CENTER);

        tableContainer.add(combinedTablePanel, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            if (this.currentSessionId > 0) {
                loadDataFromDatabase(this.currentSessionId);
            } else {
                JOptionPane.showMessageDialog(this, "Chưa có phiên làm việc hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        return tableContainer;
    }

    // =====================================================================
    // CHỈ ĐỌC DATA TỪ DATABASE - BỎ HẾT DỮ LIỆU TĨNH KHÁC
    // =====================================================================
    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        model.setRowCount(0);
        if (sessionId <= 0) return;

        List<Object[]> groupTotal = new ArrayList<>(); // dBB1+PT
        List<Object[]> groupCY = new ArrayList<>();    // Hướng CY
        List<Object[]> groupTY = new ArrayList<>();    // Hướng TY
        List<Object[]> groupSau = new ArrayList<>();   // Phía sau
        List<Object[]> groupCL = new ArrayList<>();    // Còn lại

        String sql = "SELECT vat_chat, dvt, dt_chitiet, pc04_chitiet FROM step4_quy_dinh_du_tru WHERE session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String vatChat = rs.getString("vat_chat");
                String dvt = rs.getString("dvt");

                // CHỈ lọc ra VŨ KHÍ, TBKT
                boolean isWeapon = false;
                if (dvt != null && (dvt.equalsIgnoreCase("Khẩu") || dvt.equalsIgnoreCase("Cơ sở"))) {
                    isWeapon = true;
                } else if (vatChat != null) {
                    String lower = vatChat.toLowerCase();
                    if (lower.contains("súng") || lower.contains("cối") || lower.contains("smpk") || lower.contains("spg") || lower.contains("b41")) {
                        isWeapon = true;
                    }
                }

                if (!isWeapon) continue;

                String[] dtArr = rs.getString("dt_chitiet") != null ? rs.getString("dt_chitiet").split(",") : new String[0];
                String[] pcArr = rs.getString("pc04_chitiet") != null ? rs.getString("pc04_chitiet").split(",") : new String[0];

                double[] dt = parseArray(dtArr);
                double[] pc = parseArray(pcArr);

                double totalDt = dt[2] + dt[3] + dt[4] + dt[5];
                double totalPc = pc[2] + pc[3] + pc[4] + pc[5];

                if (totalDt > 0 || totalPc > 0) groupTotal.add(createRow("", vatChat, dvt, totalDt, totalPc));
                if (dt[2] > 0 || pc[2] > 0) groupCY.add(createRow("", vatChat, dvt, dt[2], pc[2]));
                if (dt[3] > 0 || pc[3] > 0) groupTY.add(createRow("", vatChat, dvt, dt[3], pc[3]));
                if (dt[4] > 0 || pc[4] > 0) groupSau.add(createRow("", vatChat, dvt, dt[4], pc[4]));
                if (dt[5] > 0 || pc[5] > 0) groupCL.add(createRow("", vatChat, dvt, dt[5], pc[5]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<List<Object[]>> allGroups = Arrays.asList(groupTotal, groupCY, groupTY, groupSau, groupCL);
        String[] groupNames = {"dBB1+PT", "Hướng PN chủ yếu", "Hướng PN thứ yếu", "Hướng PN phía sau", "Lực lượng còn lại"};

        for (int i = 0; i < allGroups.size(); i++) {
            List<Object[]> group = allGroups.get(i);
            if (!group.isEmpty()) {
                for (int j = 0; j < group.size(); j++) {
                    Object[] row = group.get(j);
                    if (j == 0) row[0] = groupNames[i]; // Gán tên nhóm ở dòng đầu để tạo hiệu ứng Rowspan
                    model.addRow(row);
                }
            }
        }
    }

    private double[] parseArray(String[] arr) {
        double[] res = new double[6];
        for (int i = 0; i < 6; i++) {
            if (i < arr.length) res[i] = InputValidator.parseDoubleSafe(arr[i]);
        }
        return res;
    }

    private Object[] createRow(String donVi, String ten, String dvt, double hienCo, double phaiCo) {
        String kb = "1,00";
        String kt = "1,00";
        String tenLower = ten != null ? ten.toLowerCase() : "";

        if (tenLower.contains("tiểu liên") || tenLower.contains("ak")) kt = "0,98";
        else if (tenLower.contains("b41")) kt = "0,97";

        double boSung = phaiCo - hienCo;
        String slBoSung = (boSung > 0) ? formatDouble(boSung) : "";
        String tg = (boSung > 0) ? "14.00 N-4" : "";
        String dd = (boSung > 0) ? "VTCH/đv" : "";
        String pt = (boSung > 0) ? "Tay ba" : "";

        return new Object[]{
                donVi, ten, dvt != null && !dvt.isEmpty() ? dvt : "Khẩu", "0",
                formatDouble(hienCo), formatDouble(hienCo), kb, kt,
                formatDouble(phaiCo), slBoSung, tg, dd, pt
        };
    }

    private String formatDouble(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        return String.format("%s", d).replace(".", ",");
    }

    // =====================================================================
    // UI RENDERERS - FIX LỆCH CỘT TUYỆT ĐỐI 100%
    // =====================================================================
    private void setupCustomRenderers() {
        Color gridColor = new Color(203, 213, 225);

        DefaultTableCellRenderer groupRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String text = value != null ? value.toString() : "";

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 14));

                boolean isLastRow = (row == table.getRowCount() - 1);
                boolean nextRowHasText = !isLastRow && table.getValueAt(row + 1, 0) != null && !table.getValueAt(row + 1, 0).toString().isEmpty();

                int top = text.isEmpty() ? 0 : 1;
                int bottom = (isLastRow || nextRowHasText) ? 1 : 0;

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(top, 1, bottom, 1, gridColor));
                c.setBackground(isSelected ? new Color(219, 234, 254) : Color.WHITE);
                return c;
            }
        };

        DefaultTableCellRenderer standardRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, gridColor));
                c.setBackground(isSelected ? new Color(219, 234, 254) : Color.WHITE);
                return c;
            }
        };

        // BÍ QUYẾT CHỐNG LỆCH CỘT LÀ SET MAX WIDTH
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]); // <-- ÉP CỨNG WIDTH

            if (i == 0) {
                table.getColumnModel().getColumn(i).setCellRenderer(groupRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(standardRenderer);
            }
        }
    }

    private JPanel createAbsoluteHeader() {
        int totalWidth = 0;
        for (int w : colWidths) totalWidth += w;

        JPanel p = new JPanel(null);
        p.setBackground(new Color(241, 245, 249));
        p.setPreferredSize(new Dimension(totalWidth, 60));

        int[] x = new int[colWidths.length + 1];
        x[0] = 0;
        for (int i = 0; i < colWidths.length; i++) {
            x[i + 1] = x[i] + colWidths[i];
        }

        p.add(createHeaderLabel("Đơn vị", x[0], 0, colWidths[0], 60, true));
        p.add(createHeaderLabel("Tên TBKT", x[1], 0, colWidths[1], 60, false));
        p.add(createHeaderLabel("ĐVT", x[2], 0, colWidths[2], 60, false));
        p.add(createHeaderLabel("Nhu cầu", x[3], 0, colWidths[3], 60, false));

        p.add(createHeaderLabel("HIỆN CÓ", x[4], 0, x[8] - x[4], 30, false));
        p.add(createHeaderLabel("<html><center>Phải có<br>trước CĐ</center></html>", x[8], 0, colWidths[8], 60, false));
        p.add(createHeaderLabel("PHẢI BỔ SUNG", x[9], 0, x[12] - x[9], 30, false));

        p.add(createHeaderLabel("Phương thức", x[12], 0, colWidths[12], 60, false));

        p.add(createHeaderLabel("Tổng số", x[4], 30, colWidths[4], 30, false));
        p.add(createHeaderLabel("Số tốt", x[5], 30, colWidths[5], 30, false));
        p.add(createHeaderLabel("Kbđ", x[6], 30, colWidths[6], 30, false));
        p.add(createHeaderLabel("Kt", x[7], 30, colWidths[7], 30, false));

        p.add(createHeaderLabel("Số lượng", x[9], 30, colWidths[9], 30, false));
        p.add(createHeaderLabel("Thời gian", x[10], 30, colWidths[10], 30, false));
        p.add(createHeaderLabel("Địa điểm", x[11], 30, colWidths[11], 30, false));

        return p;
    }

    private JLabel createHeaderLabel(String text, int x, int y, int w, int h, boolean isFirstColumn) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setOpaque(true);
        label.setBackground(new Color(241, 245, 249));

        int left = isFirstColumn ? 1 : 0;
        label.setBorder(BorderFactory.createMatteBorder(1, left, 1, 1, new Color(203, 213, 225)));

        label.setBounds(x, y, w, h);
        return label;
    }

    private JPanel createMeasuresSection() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnl.add(UIUtils.createSectionLabel("2. Biện pháp bảo đảm"));
        pnl.add(Box.createVerticalStrut(10));

        txtGiaiDoanChuanBi = UIUtils.createStandardTextArea();
        pnl.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        pnl.add(UIUtils.createTextAreaScroll(txtGiaiDoanChuanBi, 100));
        pnl.add(Box.createVerticalStrut(15));

        txtGiaiDoanChienDau = UIUtils.createStandardTextArea();
        pnl.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        pnl.add(UIUtils.createTextAreaScroll(txtGiaiDoanChienDau, 100));
        pnl.add(Box.createVerticalStrut(15));

        txtSauChienDau = UIUtils.createStandardTextArea();
        pnl.add(UIUtils.createSubSectionLabel("* Sau chiến đấu:"));
        pnl.add(UIUtils.createTextAreaScroll(txtSauChienDau, 100));

        return pnl;
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<bp_cb_tbkt>>", txtGiaiDoanChuanBi.getText().trim());
        data.put("<<bp_cd_tbkt>>", txtGiaiDoanChienDau.getText().trim());
        data.put("<<bp_scd_tbkt>>", txtSauChienDau.getText().trim());
        return data;
    }
}