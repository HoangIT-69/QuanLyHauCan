package org.example.Tab.AssurancePlan;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Tab7_MedicalPanel extends JPanel {

    private DefaultTableModel modelQuanY;
    private JTable tblQuanY;
    private JTextArea txtCanDoi;
    private JTextArea txtChuanBi;
    private JTextArea txtChienDau;
    private JTextArea txtVeSinh;

    private boolean isCalculating = false;

    private static final Color SLATE_TEXT = new Color(30, 41, 59);
    private static final Color SLATE_BORDER = new Color(203, 213, 225);

    public Tab7_MedicalPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("VII. BẢO ĐẢM QUÂN Y");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(SLATE_TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // 1. Bảng Tỷ lệ TBBB
        mainContainer.add(UIUtils.createSectionLabel("1. Dự kiến tỷ lệ TBBB"));
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(createQuanYTable());
        mainContainer.add(Box.createVerticalStrut(25));

        // 2. Cân đối
        mainContainer.add(UIUtils.createSectionLabel("2. Cân đối"));
        txtCanDoi = createModernTextArea();
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTextAreaScrollWithBorder(txtCanDoi, 100));
        mainContainer.add(Box.createVerticalStrut(25));

        // 3. Ý định bảo đảm
        mainContainer.add(UIUtils.createSectionLabel("3. Ý định bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChuanBi = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtChuanBi, 80));
        mainContainer.add(Box.createVerticalStrut(15));

        txtChienDau = createModernTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chiến đấu:"));
        mainContainer.add(createTextAreaScrollWithBorder(txtChienDau, 80));
        mainContainer.add(Box.createVerticalStrut(25));

        // 4. Vệ sinh phòng bệnh
        mainContainer.add(UIUtils.createSectionLabel("4. Vệ sinh phòng bệnh, phòng dịch"));
        txtVeSinh = createModernTextArea();
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createTextAreaScrollWithBorder(txtVeSinh, 100));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(SLATE_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return area;
    }

    private JScrollPane createTextAreaScrollWithBorder(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.makeScrollPassThrough(scroll);
        return scroll;
    }

    private JPanel createQuanYTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(1000, 420));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] w = {220, 80, 60, 70, 60, 70, 60, 90, 90, 60};
        String[] cols = new String[10]; for (int i=0; i<10; i++) cols[i] = "";

        modelQuanY = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0 || column == 1 || column == 3 || column == 5 || column == 6) return false;
                String ten = getValueAt(row, 0).toString();
                return !ten.startsWith("TB toàn trận") && !ten.startsWith("TB ngày cao nhất");
            }
        };

        modelQuanY.addRow(new Object[]{"TB toàn trận", "", "", "", "", "", "", "", "", ""});
        modelQuanY.addRow(new Object[]{"  Phân đội phòng ngự HCY", "", "", "", "", "", "", "", "", ""});
        modelQuanY.addRow(new Object[]{"  Phân đội phòng ngự HTY", "", "", "", "", "", "", "", "", ""});
        modelQuanY.addRow(new Object[]{"  Phân đội phòng ngự phía sau", "", "", "", "", "", "", "", "", ""});
        modelQuanY.addRow(new Object[]{"  Lực lượng còn lại", "", "", "", "", "", "", "", "", ""});
        modelQuanY.addRow(new Object[]{"TB ngày cao nhất", "", "", "", "", "", "", "", "", ""});

        modelQuanY.addTableModelListener(e -> {
            if (isCalculating) return;
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 1 || col == 2 || col == 4) {
                    isCalculating = true;
                    recalculateRow(row);
                    isCalculating = false;
                }
            }
        });

        tblQuanY = new JTable(modelQuanY);
        tblQuanY.setRowHeight(30);
        tblQuanY.setTableHeader(null);
        tblQuanY.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setupModernTableStyle(tblQuanY, w);

        JPanel headerPanel = createQuanYHeader(w);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(tblQuanY);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));
        UIUtils.makeScrollPassThrough(scroll);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(SLATE_BORDER, 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);
        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,SLATE_BORDER));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        pnl.add(combined, BorderLayout.CENTER);

        return pnl;
    }

    private void recalculateRow(int row) {
        try {
            String qsStr = modelQuanY.getValueAt(row, 1) != null ? modelQuanY.getValueAt(row, 1).toString() : "";
            if (qsStr.isEmpty()) return;
            int quanSo = Integer.parseInt(qsStr.replace(".", ""));

            String tbStr = modelQuanY.getValueAt(row, 2) != null ? modelQuanY.getValueAt(row, 2).toString().replace(",", ".") : "";
            double tlTb = tbStr.isEmpty() ? 0 : Double.parseDouble(tbStr);
            int snTb = (int) Math.round(quanSo * tlTb / 100.0);

            modelQuanY.setValueAt(snTb > 0 ? String.valueOf(snTb) : "0", row, 3);

            if (row != 5) {
                String bbStr = modelQuanY.getValueAt(row, 4) != null ? modelQuanY.getValueAt(row, 4).toString().replace(",", ".") : "";
                double tlBb = bbStr.isEmpty() ? 0 : Double.parseDouble(bbStr);
                int snBb = (int) Math.round(quanSo * tlBb / 100.0);

                modelQuanY.setValueAt(snBb > 0 ? String.valueOf(snBb) : "0", row, 5);
                modelQuanY.setValueAt(String.valueOf(snTb + snBb), row, 6);
            } else {
                modelQuanY.setValueAt("", row, 4);
                modelQuanY.setValueAt("", row, 5);
                modelQuanY.setValueAt(String.valueOf(snTb), row, 6);
            }
        } catch (Exception ex) {}
    }

    private JPanel createQuanYHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 90));
        int[] x = new int[11]; x[0]=0; for(int i=0; i<10; i++) x[i+1] = x[i]+w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("Đơn vị", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Quân số<br>chiến đấu<br>(Người)</center></html>", x[1], 0, w[1], 90));

        int wDuKien = w[2]+w[3]+w[4]+w[5]+w[6];
        p.add(UIUtils.createAbsoluteHeaderLabel("Dự kiến tỷ lệ thương binh, bệnh binh", x[2], 0, wDuKien, 30));

        int wNhuCau = w[7]+w[8]+w[9];
        p.add(UIUtils.createAbsoluteHeaderLabel("Nhu cầu vận chuyển TBBB", x[7], 0, wNhuCau, 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Thương binh", x[2], 30, w[2]+w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bệnh binh", x[4], 30, w[4]+w[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[6], 30, w[6], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Cáng bộ<br>(50%)</center></html>", x[7], 30, w[7], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Tự đi<br>(50%)</center></html>", x[8], 30, w[8], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[9], 30, w[9], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("Tỷ lệ (%)", x[2], 60, w[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Số người", x[3], 60, w[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tỷ lệ (%)", x[4], 60, w[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Số người", x[5], 60, w[5], 30));

        return p;
    }

    private void setupModernTableStyle(JTable table, int[] w) {
        Color gridColor = new Color(226, 232, 240);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text0 = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString() : "";
                boolean isSummary = text0.startsWith("TB toàn trận") || text0.startsWith("TB ngày cao nhất");

                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 9 ? 0 : 1, gridColor));
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);

                if (isSummary) {
                    setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 16));
                    c.setBackground(new Color(241, 245, 249));
                    c.setForeground(SLATE_TEXT);
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    c.setForeground(Color.BLACK);
                    if (column == 1 || column == 3 || column == 5 || column == 6) c.setBackground(new Color(250, 252, 255));
                    else if (isSelected && column != 0) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < w.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            table.getColumnModel().getColumn(i).setMinWidth(w[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(w[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        Map<String, Integer> quanSoMap = new HashMap<>();
        String sqlQS = "SELECT s.huong, SUM(q.quan_so) as total_qs FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ? GROUP BY s.huong";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQS)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) quanSoMap.put(rs.getString("huong"), rs.getInt("total_qs"));
        } catch (Exception e) {}

        int hcy = quanSoMap.getOrDefault("Hướng chủ yếu", 0);
        int hty = quanSoMap.getOrDefault("Hướng thứ yếu", 0);
        int phiaSau = quanSoMap.getOrDefault("Phòng ngự phía sau", 0);
        int conLai = quanSoMap.getOrDefault("Lực lượng còn lại", 0) + quanSoMap.getOrDefault("Phối thuộc", 0);
        int toanD = hcy + hty + phiaSau + conLai;

        Map<String, Double> tiLeMap = new HashMap<>();
        String sqlTiLe = "SELECT loai_thuong_binh, ti_le FROM step4_ti_le_thuong_binh WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlTiLe)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("loai_thuong_binh").trim().toLowerCase();
                tiLeMap.put(name, rs.getDouble("ti_le"));
            }
        } catch (Exception e) {}

        double bbToanTran = getTiLeByKeyword(tiLeMap, "bệnh binh toàn trận");

        isCalculating = true;

        updateRowData(0, toanD, getTiLeByKeyword(tiLeMap, "thương binh toàn trận"), bbToanTran);
        updateRowData(1, hcy, getTiLeByKeyword(tiLeMap, "chủ yếu"), 0);
        updateRowData(2, hty, getTiLeByKeyword(tiLeMap, "thứ yếu"), 0);
        updateRowData(3, phiaSau, getTiLeByKeyword(tiLeMap, "phía sau"), 0);
        updateRowData(4, conLai, getTiLeByKeyword(tiLeMap, "còn lại"), 0);
        updateRowData(5, toanD, getTiLeByKeyword(tiLeMap, "ngày cao nhất"), 0);

        isCalculating = false;

        for (int i = 0; i <= 5; i++) recalculateRow(i);
    }

    private double getTiLeByKeyword(Map<String, Double> map, String keyword) {
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (entry.getKey().contains(keyword.toLowerCase())) return entry.getValue();
        }
        return 0.0;
    }

    private void updateRowData(int row, int quanSo, double tyLeTB, double tyLeBB) {
        modelQuanY.setValueAt(quanSo, row, 1);
        if (tyLeTB > 0) modelQuanY.setValueAt(f(tyLeTB), row, 2);
        if (tyLeBB > 0) modelQuanY.setValueAt(f(tyLeBB), row, 4);
    }

    private String f(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value).replace(".", ",");
    }

    // =========================================================================
    // XUẤT TỪNG KEYWORD RA WORD
    // =========================================================================
    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();

        // Loop xuất 6 dòng (i từ 0 đến 5)
        for (int i = 0; i < 6; i++) {
            int r = i + 1; // 1 -> 6
            data.put("<<qy_qs_" + r + ">>", getVal(i, 1));
            data.put("<<qy_tb_tl_" + r + ">>", getVal(i, 2));
            data.put("<<qy_tb_sn_" + r + ">>", getVal(i, 3));
            data.put("<<qy_bb_tl_" + r + ">>", getVal(i, 4));
            data.put("<<qy_bb_sn_" + r + ">>", getVal(i, 5));
            data.put("<<qy_tong_" + r + ">>", getVal(i, 6));
            data.put("<<qy_cang_" + r + ">>", getVal(i, 7));
            data.put("<<qy_tudi_" + r + ">>", getVal(i, 8));
            data.put("<<qy_nc_tong_" + r + ">>", getVal(i, 9));
        }

        data.put("<<can_doi_quan_y>>", txtCanDoi.getText().trim());
        data.put("<<quany_chuan_bi>>", txtChuanBi.getText().trim());
        data.put("<<quany_chien_dau>>", txtChienDau.getText().trim());
        data.put("<<ve_sinh_phong_dich>>", txtVeSinh.getText().trim());
        return data;
    }

    private String getVal(int row, int col) {
        Object v = modelQuanY.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }
}