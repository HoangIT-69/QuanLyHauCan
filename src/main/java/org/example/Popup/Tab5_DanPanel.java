package org.example.Popup;

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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab5_DanPanel extends JPanel {

    private DefaultTableModel danModel;

    public Tab5_DanPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        int[] colWidths = {200, 50, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60};
        String[] cols = new String[35]; for (int i = 0; i < 35; i++) cols[i] = "";

        danModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column > 30; }
        };

        JTable table = new JTable(danModel);
        table.setRowHeight(35);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setupDanRenderers(table, colWidths);

        JPanel headerPanel = createDanHeader(colWidths);
        JViewport viewport = new JViewport();
        viewport.setView(headerPanel);
        viewport.setPreferredSize(headerPanel.getPreferredSize());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(scroll);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> viewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBorder(new LineBorder(new Color(203, 213, 225), 1));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(viewport, BorderLayout.CENTER);

        int scrollWidth = UIManager.getInt("ScrollBar.width");
        if (scrollWidth == 0) scrollWidth = 17;
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 120)); spacer.setBackground(new Color(241, 245, 249)); spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        add(combined, BorderLayout.CENTER);
    }

    private void setupDanRenderers(JTable table, int[] colWidths) {
        Color gridColor = new Color(203, 213, 225);
        DefaultTableCellRenderer standardRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text0 = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim() : "";
                boolean isSummaryRow = text0.startsWith("dBB1+PT") || text0.startsWith("Hướng") || text0.startsWith("Phòng ngự") || text0.startsWith("Lực lượng");

                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1, 1, gridColor));

                if (isSummaryRow) {
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    c.setBackground(new Color(226, 232, 240));
                    if (column == 0 || !value.toString().isEmpty()) c.setForeground(new Color(192, 57, 43));
                    else c.setForeground(Color.BLACK);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    c.setForeground(Color.BLACK);
                    if (column <= 31 && !isSelected) c.setBackground(new Color(250, 252, 255));
                    else if (isSelected) c.setBackground(new Color(219, 234, 254));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(standardRenderer);
        }
    }

    private JPanel createDanHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 120));
        int[] x = new int[36]; x[0]=0; for(int i=0; i<35; i++) x[i+1] = x[i]+w[i];

        p.add(UIUtils.createAbsoluteHeaderLabel("Loại đạn", x[0], 0, w[0], 120));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Số<br>lượng<br>VK</center></html>", x[1], 0, w[1], 120));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>TL<br>ĐVT</center></html>", x[2], 0, w[2], 120));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nhu cầu", x[3], 0, x[5]-x[3], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Được tiêu thụ", x[5], 0, x[9]-x[5], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phải có sau CĐ", x[9], 0, x[13]-x[9], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Hiện có", x[13], 0, x[19]-x[13], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Phải có TQĐ", x[19], 0, x[23]-x[19], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kế hoạch tiếp nhận, bảo đảm", x[23], 0, x[35]-x[23], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng số", x[13], 30, x[15]-x[13], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Ở kho", x[15], 30, x[17]-x[15], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Ở đơn vị", x[17], 30, x[19]-x[17], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Bổ sung toàn trận", x[23], 30, x[27]-x[23], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("BS trước chiến đấu", x[27], 30, x[31]-x[27], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("BS trong chiến đấu", x[31], 30, x[35]-x[31], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[3], 60, w[3], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL(tấn)", x[4], 60, w[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("T.CS", x[5], 60, w[5], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[6], 60, w[6], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[7], 60, w[7], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL(tấn)", x[8], 60, w[8], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("T.CS", x[9], 60, w[9], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[10], 60, w[10], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[11], 60, w[11], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL(tấn)", x[12], 60, w[12], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[13], 60, w[13], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[14], 60, w[14], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[15], 60, w[15], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[16], 60, w[16], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[17], 60, w[17], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[18], 60, w[18], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("T.CS", x[19], 60, w[19], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[20], 60, w[20], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[21], 60, w[21], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[22], 60, w[22], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[23], 60, x[25]-x[23], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[24], 60, w[24], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[25], 60, w[25], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[27], 60, x[29]-x[27], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[28], 60, w[28], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[29], 60, w[29], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tổng", x[31], 60, x[33]-x[31], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[33], 60, w[33], 60)); p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[34], 60, w[34], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[23], 90, w[23], 30)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[24], 90, w[24], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[27], 90, w[27], 30)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[28], 90, w[28], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("CS", x[31], 90, w[31], 30)); p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[32], 90, w[32], 30));

        return p;
    }

    class GroupData {
        String groupName;
        List<Object[]> subRows = new ArrayList<>();
        GroupData(String name) { this.groupName = name; }
    }

    public void loadDataFromDatabase(int sessionId) {
        danModel.setRowCount(0);

        Map<String, Map<String, Integer>> gunMap = new HashMap<>();
        String[] dirs = {"Toàn d", "Hướng chủ yếu", "Hướng thứ yếu", "Phòng ngự phía sau", "Lực lượng còn lại", "Phối thuộc"};
        for (String dir : dirs) gunMap.put(dir, new HashMap<>());

        String sqlSungs = "SELECT s.huong, SUM(q.smpk_127mm) as smpk, SUM(q.co100mm) as c100, SUM(q.co82mm) as c82, SUM(q.co60mm) as c60, " +
                "SUM(q.spg9) as spg9, SUM(q.b41) as b41, SUM(q.dai_lien) as dl, SUM(q.trung_lien) as trl, SUM(q.tieu_lien) as tl, SUM(q.sung_ngan) as sn, SUM(q.luu_dan) as ld " +
                "FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ? GROUP BY s.huong";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlSungs)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String huong = rs.getString("huong");
                Map<String, Integer> counts = new HashMap<>();
                counts.put("SMPK 12,7mm", rs.getInt("smpk"));
                counts.put("Cối 100mm/e", rs.getInt("c100"));
                counts.put("Cối 82mm/d", rs.getInt("c82"));
                counts.put("Cối 60mm", rs.getInt("c60"));
                counts.put("Súng SPG-9", rs.getInt("spg9"));
                counts.put("Súng B41", rs.getInt("b41"));
                counts.put("Súng đại liên", rs.getInt("dl"));
                counts.put("Súng trung liên", rs.getInt("trl"));
                counts.put("Súng tiểu liên", rs.getInt("tl"));
                counts.put("Súng ngắn", rs.getInt("sn"));
                counts.put("Lựu đạn", rs.getInt("ld"));

                if(gunMap.containsKey(huong)) gunMap.put(huong, counts);
            }
        } catch (Exception e) { e.printStackTrace(); }

        Map<String, Double> weightMap = new HashMap<>();
        Map<String, Integer> roundsPerCSMap = new HashMap<>();
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT loai_dan, trong_luong_1_vien, so_vien_tren_coso FROM quyuoc_dan")) {
            while (rs.next()) {
                String ten = rs.getString("loai_dan").trim();
                weightMap.put(ten, rs.getDouble("trong_luong_1_vien"));
                roundsPerCSMap.put(ten, rs.getInt("so_vien_tren_coso"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        GroupData grpToanD = new GroupData("dBB1+PT (Toàn d)");
        GroupData grpCY = new GroupData("Hướng PN chủ yếu");
        GroupData grpTY = new GroupData("Hướng PN thứ yếu");
        GroupData grpSau = new GroupData("Phòng ngự phía sau");
        GroupData grpCL = new GroupData("Lực lượng còn lại");

        String sqlData = "SELECT * FROM step4_quy_dinh_du_tru WHERE session_id = ? AND loai_vat_chat = 1 ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlData)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ten = rs.getString("vat_chat").trim();
                String dvt = rs.getString("dvt") != null ? rs.getString("dvt") : "";

                boolean isGrenade = dvt.equalsIgnoreCase("Quả");
                double w1Vien = weightMap.getOrDefault(ten, 0.0);
                int rPerCS = isGrenade ? 1 : roundsPerCSMap.getOrDefault(ten, 0);

                double csTieuThuCB = rs.getDouble("tieu_thu_gdcb");
                double csTieuThuCD = rs.getDouble("tieu_thu_gdcd");
                double csPcSCD = rs.getDouble("phai_co_scd");
                double csNhuCau = csTieuThuCB + csTieuThuCD + csPcSCD;

                double[] dtArr = parseArray(rs.getString("dt_chitiet"));
                double[] pc04Arr = parseArray(rs.getString("pc04_chitiet"));

                double[] toanSums = new double[14];

                // ĐÃ FIX: Không ép súng về 1 cho Lựu đạn nữa. Lấy thẳng số liệu quân số từ Database
                int gunsCY = gunMap.get("Hướng chủ yếu").getOrDefault(ten, 0);
                if (gunsCY > 0 || dtArr[2] > 0 || pc04Arr[2] > 0) {
                    Object[] row = processDirectionRow(ten, w1Vien, rPerCS, isGrenade, gunsCY,
                            csNhuCau, csTieuThuCB, csTieuThuCD, csPcSCD, 0, dtArr[2], 0, pc04Arr[2], toanSums);
                    grpCY.subRows.add(row);
                }

                int gunsTY = gunMap.get("Hướng thứ yếu").getOrDefault(ten, 0);
                if (gunsTY > 0 || dtArr[3] > 0 || pc04Arr[3] > 0) {
                    Object[] row = processDirectionRow(ten, w1Vien, rPerCS, isGrenade, gunsTY,
                            csNhuCau, csTieuThuCB, csTieuThuCD, csPcSCD, 0, dtArr[3], 0, pc04Arr[3], toanSums);
                    grpTY.subRows.add(row);
                }

                int gunsSau = gunMap.get("Phòng ngự phía sau").getOrDefault(ten, 0);
                if (gunsSau > 0 || dtArr[4] > 0 || pc04Arr[4] > 0) {
                    Object[] row = processDirectionRow(ten, w1Vien, rPerCS, isGrenade, gunsSau,
                            csNhuCau, csTieuThuCB, csTieuThuCD, csPcSCD, 0, dtArr[4], 0, pc04Arr[4], toanSums);
                    grpSau.subRows.add(row);
                }

                int gunsCL = gunMap.get("Lực lượng còn lại").getOrDefault(ten, 0);
                int gunsPT = gunMap.get("Phối thuộc").getOrDefault(ten, 0);
                int gunsCL_PT = gunsCL + gunsPT;
                if (gunsCL_PT > 0 || dtArr[5] > 0 || pc04Arr[5] > 0 || dtArr[0] > 0) {
                    Object[] row = processDirectionRow(ten, w1Vien, rPerCS, isGrenade, gunsCL_PT,
                            csNhuCau, csTieuThuCB, csTieuThuCD, csPcSCD, dtArr[0], dtArr[5], pc04Arr[0], pc04Arr[5], toanSums);
                    grpCL.subRows.add(row);
                }

                int toanGuns = gunsCY + gunsTY + gunsSau + gunsCL_PT;
                if (toanGuns > 0 || dtArr[0]+dtArr[1]+dtArr[2]+dtArr[3]+dtArr[4]+dtArr[5] > 0) {
                    Object[] rowToan = buildToanDRow(ten, w1Vien, rPerCS, isGrenade, toanGuns,
                            csNhuCau, csTieuThuCB, csTieuThuCD, csPcSCD, toanSums);
                    grpToanD.subRows.add(rowToan);
                }
            }

            appendGroupToModel(danModel, grpToanD);
            appendGroupToModel(danModel, grpCY);
            appendGroupToModel(danModel, grpTY);
            appendGroupToModel(danModel, grpSau);
            appendGroupToModel(danModel, grpCL);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ĐÃ FIX: Gỡ bỏ parameter isSubTable, thay thế hoàn toàn chuỗi "-" bằng số "0"
    private Object[] processDirectionRow(String ten, double weight, int rPerCS, boolean isGrenade, int numGuns,
                                         double csNhuCau, double csTieuThuCB, double csTieuThuCD, double csPcSCD,
                                         double hcKhoCS, double hcDvCS, double pcKhoCS, double pcDvCS,
                                         double[] toanSums) {

        double tl1CS = isGrenade ? (weight / 1000.0) : (weight * rPerCS / 1000.0);
        String unitPrint = isGrenade ? "Quả" : f(tl1CS);

        // Lựu đạn thì multiplier là 1 vì bản thân CS của nó đã là số lượng Quả
        double multiplier = isGrenade ? 1.0 : numGuns;

        double nhuCauTL = csNhuCau * tl1CS * multiplier;
        double tieuThuCB_TL = csTieuThuCB * tl1CS * multiplier;
        double tieuThuCD_TL = csTieuThuCD * tl1CS * multiplier;
        double pcSCD_TL = csPcSCD * tl1CS * multiplier;

        double totalHcCS = hcKhoCS + hcDvCS;
        double hcKhoTL = hcKhoCS * tl1CS * multiplier;
        double hcDvTL = hcDvCS * tl1CS * multiplier;
        double totalHcTL = hcKhoTL + hcDvTL;

        double totalPcCS = pcKhoCS + pcDvCS;
        double pcKhoTL = pcKhoCS * tl1CS * multiplier;
        double pcDvTL = pcDvCS * tl1CS * multiplier;
        double totalPcTL = pcKhoTL + pcDvTL;

        double bsTruocCS = totalPcCS - totalHcCS; if (bsTruocCS < 0) bsTruocCS = 0;
        double bsTrongCS = csPcSCD + csTieuThuCD - totalPcCS; if (bsTrongCS < 0) bsTrongCS = 0;
        double bsToanCS = bsTruocCS + bsTrongCS;

        double bsTruocTL = bsTruocCS * tl1CS * multiplier;
        double bsTrongTL = bsTrongCS * tl1CS * multiplier;
        double bsToanTL = bsToanCS * tl1CS * multiplier;

        toanSums[0] += nhuCauTL;
        toanSums[1] += tieuThuCB_TL;
        toanSums[2] += tieuThuCD_TL;
        toanSums[3] += pcSCD_TL;
        toanSums[4] += hcKhoTL;
        toanSums[5] += hcDvTL;
        toanSums[6] += pcKhoTL;
        toanSums[7] += pcDvTL;
        toanSums[8] += bsTruocTL;
        toanSums[9] += bsTrongTL;

        if (isGrenade) {
            toanSums[10] += hcKhoCS; toanSums[11] += hcDvCS;
            toanSums[12] += pcKhoCS; toanSums[13] += pcDvCS;
        }

        Object[] r = new Object[35];
        r[0] = "      " + ten;
        r[1] = String.valueOf(numGuns); // Hiện số lượng thực cho tất cả Súng & Lựu đạn
        r[2] = unitPrint;
        r[3] = f(csNhuCau); r[4] = f(nhuCauTL);
        r[5] = f(csTieuThuCB + csTieuThuCD); r[6] = f(csTieuThuCB); r[7] = f(csTieuThuCD); r[8] = f(tieuThuCB_TL + tieuThuCD_TL);
        r[9] = f(csPcSCD); r[10] = "0"; r[11] = f(csPcSCD); r[12] = f(pcSCD_TL);

        r[13] = f(totalHcCS); r[14] = f(totalHcTL);
        r[15] = f(hcKhoCS); r[16] = f(hcKhoTL);
        r[17] = f(hcDvCS); r[18] = f(hcDvTL);

        r[19] = f(totalPcCS); r[20] = f(pcKhoCS); r[21] = f(pcDvCS); r[22] = f(totalPcTL);

        r[23] = f(bsToanCS); r[24] = f(bsToanTL); r[25] = "0"; r[26] = "0";
        r[27] = f(bsTruocCS); r[28] = f(bsTruocTL); r[29] = "0"; r[30] = "0";
        r[31] = f(bsTrongCS); r[32] = f(bsTrongTL); r[33] = "0"; r[34] = "0";

        return r;
    }

    private Object[] buildToanDRow(String ten, double weight, int rPerCS, boolean isGrenade, int totalGuns,
                                   double csNhuCau, double csTieuThuCB, double csTieuThuCD, double csPcSCD,
                                   double[] toanSums) {

        double tl1CS = isGrenade ? (weight / 1000.0) : (weight * rPerCS / 1000.0);
        String unitPrint = isGrenade ? "Quả" : f(tl1CS);
        double multiplier = isGrenade ? 1.0 : totalGuns;

        double hcKhoCS = isGrenade ? toanSums[10] : (tl1CS * multiplier > 0 ? toanSums[4] / (tl1CS * multiplier) : 0);
        double hcDvCS = isGrenade ? toanSums[11] : (tl1CS * multiplier > 0 ? toanSums[5] / (tl1CS * multiplier) : 0);
        double pcKhoCS = isGrenade ? toanSums[12] : (tl1CS * multiplier > 0 ? toanSums[6] / (tl1CS * multiplier) : 0);
        double pcDvCS = isGrenade ? toanSums[13] : (tl1CS * multiplier > 0 ? toanSums[7] / (tl1CS * multiplier) : 0);

        double totalHcCS = hcKhoCS + hcDvCS;
        double totalPcCS = pcKhoCS + pcDvCS;

        double bsTruocCS = totalPcCS - totalHcCS; if(bsTruocCS<0) bsTruocCS=0;
        double bsTrongCS = csPcSCD + csTieuThuCD - totalPcCS; if(bsTrongCS<0) bsTrongCS=0;

        Object[] r = new Object[35];
        r[0] = "      " + ten;
        r[1] = String.valueOf(totalGuns);
        r[2] = unitPrint;
        r[3] = f(csNhuCau); r[4] = f(toanSums[0]);
        r[5] = f(csTieuThuCB + csTieuThuCD); r[6] = f(csTieuThuCB); r[7] = f(csTieuThuCD); r[8] = f(toanSums[1] + toanSums[2]);
        r[9] = f(csPcSCD); r[10] = "0"; r[11] = f(csPcSCD); r[12] = f(toanSums[3]);

        r[13] = f(totalHcCS); r[14] = f(toanSums[4] + toanSums[5]);
        r[15] = f(hcKhoCS); r[16] = f(toanSums[4]);
        r[17] = f(hcDvCS); r[18] = f(toanSums[5]);

        r[19] = f(totalPcCS); r[20] = f(pcKhoCS); r[21] = f(pcDvCS); r[22] = f(toanSums[6] + toanSums[7]);

        r[23] = f(bsTruocCS + bsTrongCS); r[24] = f(toanSums[8] + toanSums[9]);
        r[25] = "0"; r[26] = "0";
        r[27] = f(bsTruocCS); r[28] = f(toanSums[8]); r[29] = "0"; r[30] = "0";
        r[31] = f(bsTrongCS); r[32] = f(toanSums[9]); r[33] = "0"; r[34] = "0";

        return r;
    }

    private void appendGroupToModel(DefaultTableModel model, GroupData grp) {
        if (!grp.subRows.isEmpty()) {
            Object[] sumRow = new Object[35];
            Arrays.fill(sumRow, "");
            sumRow[0] = grp.groupName;

            double[] groupSums = new double[35];
            int[] tlColsToSum = {4, 8, 12, 14, 16, 18, 22, 24, 28, 32};
            for (Object[] subRow : grp.subRows) {
                for (int c : tlColsToSum) {
                    String val = subRow[c].toString().replace(",", ".");
                    if (!val.isEmpty()) {
                        groupSums[c] += InputValidator.parseDoubleSafe(val);
                    }
                }
            }
            for (int c : tlColsToSum) sumRow[c] = f(groupSums[c]);
            model.addRow(sumRow);

            for (Object[] row : grp.subRows) {
                model.addRow(row);
            }
        }
    }

    private double[] parseArray(String arrStr) {
        double[] res = new double[6];
        if (arrStr != null && !arrStr.isEmpty()) {
            String[] arr = arrStr.split(",");
            for (int i = 0; i < Math.min(6, arr.length); i++) res[i] = InputValidator.parseDoubleSafe(arr[i]);
        }
        return res;
    }

    private String f(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value);
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<rows_bang_dan>>", buildTableHtml(danModel));
        return data;
    }

    private String buildTableHtml(DefaultTableModel m) {
        if (m == null || m.getRowCount() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < m.getColumnCount(); j++) {
                Object val = m.getValueAt(i, j);
                String text = (val == null) ? "" : val.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                if (j == 0) sb.append("<td class='text-left'>").append(text).append("</td>");
                else sb.append("<td>").append(text).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
    }
}