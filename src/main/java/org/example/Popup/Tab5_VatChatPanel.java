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
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class Tab5_VatChatPanel extends JPanel {

    private DefaultTableModel vatChatModel;
    private int type;

    public Tab5_VatChatPanel(String title, int type) {
        this.type = type;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // TỔNG CỘNG 27 CỘT THEO ĐÚNG THIẾT KẾ MỚI
        int[] colWidths = {
                150, // 0: Chỉ tiêu
                50, // 1: ĐVT
                70, // 2: TL ĐVT (Toàn d)
                60, // 3: TL ĐVT (d)
                60, // 4: TL ĐVT (PT)
                70, // 5: Quy định dự trữ
                60, // 6: GĐCB
                60, // 7: GĐCĐ
                50, // 8: PC SCĐ -> Kho/d
                50, // 9: PC SCĐ -> ĐV
                50, // 10: PC SCĐ -> +
                50, // 11: Hiện có -> Kho/d -> d
                50, // 12: Hiện có -> Kho/d -> PT
                50, // 13: Hiện có -> ĐV -> d
                50, // 14: Hiện có -> ĐV -> PT
                50, // 15: Hiện có -> +
                50, // 16: Bổ sung -> PC TQĐ -> Kho/d
                50, // 17: Bổ sung -> PC TQĐ -> ĐV
                50, // 18: Bổ sung -> GĐCB -> Kho/d
                50, // 19: Bổ sung -> GĐCB -> ĐV
                50, // 20: Bổ sung -> GĐCĐ -> Kho/d
                50, // 21: Bổ sung -> GĐCĐ -> ĐV
                50, // 22: Bổ sung -> +
                70, // 23: Thời gian
                80, // 24: Địa điểm
                80, // 25: Phương thức
                80  // 26: nhiệm vụ
        };

        String[] cols = new String[27]; for(int i=0; i<27; i++) cols[i] = "";

        vatChatModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column >= 23; } // Chỉ sửa 4 cột cuối
        };

        JTable table = new JTable(vatChatModel);
        table.setRowHeight(35);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        Color gridColor = new Color(203, 213, 225);
        DefaultTableCellRenderer vatChatRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 26 ? 0 : 1, gridColor));

                if (column < 23 && !isSelected) c.setBackground(new Color(248, 250, 252));
                else if (isSelected) c.setBackground(new Color(219, 234, 254));
                else c.setBackground(Color.WHITE);
                return c;
            }
        };

        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(vatChatRenderer);
        }

        JPanel headerPanel = createVatChatHeader(colWidths);
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

        // Chiều cao Spacer là 90px tương ứng với 3 tầng header
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(scrollWidth, 90));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(203,213,225)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combined.add(headerWrapper, BorderLayout.NORTH);
        combined.add(scroll, BorderLayout.CENTER);
        add(combined, BorderLayout.CENTER);
    }

    // --- XÂY DỰNG HEADER 3 TẦNG CHUẨN THIẾT KẾ MỚI ---
    private JPanel createVatChatHeader(int[] w) {
        JPanel p = new JPanel(null); p.setBackground(new Color(241, 245, 249));
        int totalWidth = 0; for (int width : w) totalWidth += width;
        p.setPreferredSize(new Dimension(totalWidth, 90)); // Cao 90px (3 dòng x 30px)

        int[] x = new int[28]; x[0]=0; for(int i=0; i<27; i++) x[i+1] = x[i]+w[i];

        // --- LEVEL 1 (Hàng trên cùng, Y = 0) ---
        p.add(UIUtils.createAbsoluteHeaderLabel("Chỉ tiêu", x[0], 0, w[0], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐVT", x[1], 0, w[1], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>TL ĐVT<br>(Toàn d)</center></html>", x[2], 0, w[2], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>TL ĐVT<br>(d)</center></html>", x[3], 0, w[3], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>TL ĐVT<br>(PT)</center></html>", x[4], 0, w[4], 90));

        // Bao trọn: Quy định dự trữ (5), Tiêu thụ (6,7), PC SCĐ (8,9,10)
        int wQddt = w[5]+w[6]+w[7]+w[8]+w[9]+w[10];
        p.add(UIUtils.createAbsoluteHeaderLabel("Quy định dự trữ, tiêu thụ", x[5], 0, wQddt, 30));

        // Bao trọn: Hiện có (11,12,13,14,15)
        int wHienCo = w[11]+w[12]+w[13]+w[14]+w[15];
        p.add(UIUtils.createAbsoluteHeaderLabel("Hiện có", x[11], 0, wHienCo, 30));

        // Bao trọn: Bổ sung (16 -> 22)
        int wBoSung = w[16]+w[17]+w[18]+w[19]+w[20]+w[21]+w[22];
        p.add(UIUtils.createAbsoluteHeaderLabel("Bổ sung", x[16], 0, wBoSung, 30));

        // Bao trọn: Kế hoạch (23,24,25,26)
        int wKeHoach = w[23]+w[24]+w[25]+w[26];
        p.add(UIUtils.createAbsoluteHeaderLabel("Kế hoạch tiếp nhận, bảo đảm", x[23], 0, wKeHoach, 30));


        // --- LEVEL 2 (Hàng giữa, Y = 30) ---
        p.add(UIUtils.createAbsoluteHeaderLabel("Quy định dự trữ", x[5], 30, w[5], 60)); // Rớt đáy
        p.add(UIUtils.createAbsoluteHeaderLabel("Tiêu thụ", x[6], 30, w[6]+w[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PC SCĐ", x[8], 30, w[8]+w[9]+w[10], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Kho/d", x[11], 30, w[11]+w[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[13], 30, w[13]+w[14], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[15], 30, w[15], 60)); // Rớt đáy

        p.add(UIUtils.createAbsoluteHeaderLabel("PC TQĐ", x[16], 30, w[16]+w[17], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[18], 30, w[18]+w[19], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[20], 30, w[20]+w[21], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[22], 30, w[22], 60)); // Rớt đáy

        p.add(UIUtils.createAbsoluteHeaderLabel("Thời gian", x[23], 30, w[23], 60)); // Rớt đáy
        p.add(UIUtils.createAbsoluteHeaderLabel("Địa điểm", x[24], 30, w[24], 60)); // Rớt đáy
        p.add(UIUtils.createAbsoluteHeaderLabel("Phương thức", x[25], 30, w[25], 60)); // Rớt đáy
        p.add(UIUtils.createAbsoluteHeaderLabel("nhiệm vụ", x[26], 30, w[26], 60)); // Rớt đáy


        // --- LEVEL 3 (Hàng dưới cùng, Y = 60) ---
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[6], 60, w[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[7], 60, w[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho/d", x[8], 60, w[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[9], 60, w[9], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("+", x[10], 60, w[10], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[11], 60, w[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[12], 60, w[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[13], 60, w[13], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[14], 60, w[14], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Kho/d", x[16], 60, w[16], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[17], 60, w[17], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho/d", x[18], 60, w[18], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[19], 60, w[19], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho/d", x[20], 60, w[20], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[21], 60, w[21], 30));

        return p;
    }

    class ItemData {
        String ten, dvt, danhMuc;
        double quyUoc, wQddt, wTtGdcb, wTtGdcd, wPcScd, wPcTqd, wHcKhoD, wHcDonVi, wHcPhoiThuoc;
        boolean isPercent, isDauThap, isTYS, isTYT, isTCT, ptCannotHold;
    }

    public void loadDataFromDatabase(int sessionId) {
        vatChatModel.setRowCount(0);

        // =====================================================================
        // BƯỚC 1: LẤY QUÂN SỐ CỦA TỪNG HƯỚNG VÀ TÍNH TỔNG (Toàn d)
        // =====================================================================
        Map<String, Integer> qsHuongD = new LinkedHashMap<>();
        Map<String, Integer> qsHuongPT = new LinkedHashMap<>();
        int qsToanD_d = 0, qsToanD_PT = 0;

        String sqlQS = "SELECT s2.huong, s2.phan_loai, SUM(q.quan_so) as tong_qs " +
                "FROM step2_bien_che s2 JOIN quyuoc_bienche q ON s2.quyuoc_id = q.id " +
                "WHERE s2.session_id = ? GROUP BY s2.huong, s2.phan_loai ORDER BY s2.id ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQS)) {
            ps.setInt(1, sessionId);
            ResultSet rsQs = ps.executeQuery();
            while (rsQs.next()) {
                String huong = rsQs.getString("huong").trim();
                int phanLoai = rsQs.getInt("phan_loai");
                int qs = rsQs.getInt("tong_qs");

                if (phanLoai == 1) {
                    qsHuongD.put(huong, qsHuongD.getOrDefault(huong, 0) + qs);
                    qsToanD_d += qs;
                } else if (phanLoai == 2) {
                    qsHuongPT.put(huong, qsHuongPT.getOrDefault(huong, 0) + qs);
                    qsToanD_PT += qs;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }


        // =====================================================================
        // BƯỚC 2: LẤY VÀ PHÂN LOẠI VẬT CHẤT VÀO 4 NHÓM
        // =====================================================================
        List<ItemData> listQuanNhu = new ArrayList<>();
        List<ItemData> listQuanY = new ArrayList<>();
        List<ItemData> listDoanhTrai = new ArrayList<>();
        List<ItemData> listVTKT = new ArrayList<>();

        String sqlData = "SELECT s4.*, s3.kho_d as hc_kho_d, s3.don_vi as hc_don_vi, s3.phoi_thuoc as hc_phoi_thuoc, qv.quy_uoc, qv.danh_muc " +
                "FROM step4_quy_dinh_du_tru s4 " +
                "LEFT JOIN step3_vat_chat s3 ON s4.session_id = s3.session_id AND s4.vat_chat = s3.vat_chat " +
                "LEFT JOIN quyuoc_vchc qv ON s4.vat_chat = qv.ten_vat_chat " +
                "WHERE s4.session_id = ? ORDER BY s4.id ASC"; // Bỏ lọc type để lấy cả VTKT nếu có

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlData)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ItemData item = new ItemData();
                item.ten = rs.getString("vat_chat").trim();
                item.danhMuc = rs.getString("danh_muc") != null ? rs.getString("danh_muc").toLowerCase() : "";
                item.dvt = rs.getString("dvt") != null ? rs.getString("dvt").trim() : "";
                item.quyUoc = rs.getDouble("quy_uoc");
                // Lấy các giá trị hệ số gốc (chưa nhân TLĐVT)
                item.wQddt = rs.getDouble("du_tru");
                item.wTtGdcb = rs.getDouble("tieu_thu_gdcb");
                item.wTtGdcd = rs.getDouble("tieu_thu_gdcd");
                item.wPcScd = rs.getDouble("phai_co_scd");
                item.wPcTqd = rs.getDouble("phai_co_0400");
                item.wHcKhoD = rs.getDouble("hc_kho_d");
                item.wHcDonVi = rs.getDouble("hc_don_vi");
                item.wHcPhoiThuoc = rs.getDouble("hc_phoi_thuoc");

                item.isPercent = item.dvt.contains("%QS") || item.dvt.equals("%");
                item.isDauThap = item.ten.equalsIgnoreCase("Dầu thắp");
                item.isTYS = item.ten.equalsIgnoreCase("TYS") || item.ten.contains("Túi y sĩ");
                item.isTYT = item.ten.equalsIgnoreCase("TYT") || item.ten.contains("Túi y tá");
                item.isTCT = item.ten.equalsIgnoreCase("TCT") || item.ten.contains("Túi cứu thương");
                item.ptCannotHold = item.isTYS || item.isTYT || item.isTCT || item.ten.contains("Đường sữa");

                // Phân loại vào 4 List dựa vào cột danh_muc trong bảng quyuoc_vchc
                if (item.danhMuc.contains("y")) listQuanY.add(item);
                else if (item.danhMuc.contains("trại") || item.danhMuc.contains("dầu")) listDoanhTrai.add(item);
                else if (item.danhMuc.contains("kỹ thuật") || rs.getInt("loai_vat_chat") == 3) listVTKT.add(item);
                else listQuanNhu.add(item); // Mặc định là Quân nhu (Lương thực, thực phẩm)
            }
        } catch (Exception e) { e.printStackTrace(); }


        // =====================================================================
        // BƯỚC 3: VẼ BẢNG THEO THỨ TỰ HƯỚNG -> DANH MỤC -> VẬT CHẤT
        // =====================================================================
        String[] laMa = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        List<String> listHuong = new ArrayList<>();
        listHuong.add("Toàn d"); // Thêm Toàn d lên đầu tiên
        for (String h : qsHuongD.keySet()) {
            if (!h.equalsIgnoreCase("LL còn lại")) listHuong.add(h);
        }
        listHuong.add("LL còn lại"); // LLCL luôn nằm cuối

        int huongIndex = 0;
        for (String huongName : listHuong) {
            // Xác định quân số và tính chất của Hướng hiện tại
            int qsD = huongName.equals("Toàn d") ? qsToanD_d : qsHuongD.getOrDefault(huongName, 0);
            int qsPT = huongName.equals("Toàn d") ? qsToanD_PT : qsHuongPT.getOrDefault(huongName, 0);

            // XỬ LÝ NGOẠI LỆ: Các hướng thường KHÔNG có Kho và KHÔNG có Phối thuộc
            boolean hasKho = huongName.equals("Toàn d") || huongName.equalsIgnoreCase("LL còn lại");
            boolean hasPT = huongName.equals("Toàn d") || huongName.equalsIgnoreCase("LL còn lại");

            // --- TẠO DÒNG TIÊU ĐỀ HƯỚNG (Vd: I. Toàn d) ---
            int rowHuongIdx = vatChatModel.getRowCount();
            Object[] rowHuong = new Object[27];
            Arrays.fill(rowHuong, "");
            rowHuong[0] = "<html><b>" + laMa[huongIndex++] + ". " + huongName + "</b></html>";
            vatChatModel.addRow(rowHuong);
            double[] tongHuong = new double[27];

            // DUYỆT QUA 4 LOẠI DANH MỤC CỦA HƯỚNG ĐÓ
            String[] catNames = {"1. Quân nhu", "2. Quân y", "3. Doanh trại", "4. VTKT"};
            List<ItemData>[] catLists = new List[]{listQuanNhu, listQuanY, listDoanhTrai, listVTKT};

            for (int c = 0; c < 4; c++) {
                if (catLists[c].isEmpty()) continue;

                // --- TẠO DÒNG TIÊU ĐỀ DANH MỤC (Vd: 1. Quân nhu) ---
                int rowCatIdx = vatChatModel.getRowCount();
                Object[] rowCat = new Object[27];
                Arrays.fill(rowCat, "");
                rowCat[0] = "<html><b>" + catNames[c] + "</b></html>";
                rowCat[1] = "<html><b>Tấn</b></html>";
                vatChatModel.addRow(rowCat);
                double[] tongCat = new double[27];

                // DUYỆT TỪNG VẬT CHẤT TRONG DANH MỤC
                for (ItemData item : catLists[c]) {
                    double tlDvtToanD = 0, tlDvtD = 0, tlDvtPT = 0;

                    // Tính TL ĐVT
                    if (item.isTYS) { tlDvtToanD = 6; tlDvtD = 6; tlDvtPT = 6; }
                    else if (item.isTYT) { tlDvtToanD = 4; tlDvtD = 4; tlDvtPT = 4; }
                    else if (item.isTCT) { tlDvtToanD = 2; tlDvtD = 2; tlDvtPT = 2; }
                    else if (item.isDauThap) {
                        tlDvtToanD = 0.5 * (qsToanD_d + qsToanD_PT) / 30.0;
                        tlDvtD = 0.5 * qsD / 30.0;
                        tlDvtPT = 0.5 * qsPT / 30.0;
                    } else {
                        double multi = item.isPercent ? 0.01 : 1.0;
                        tlDvtToanD = item.quyUoc * (qsToanD_d + qsToanD_PT) * multi;
                        tlDvtD = item.quyUoc * qsD * multi;
                        tlDvtPT = item.quyUoc * qsPT * multi;
                    }
                    if (tlDvtToanD == 0) tlDvtToanD = 1; if (tlDvtD == 0) tlDvtD = 1; if (tlDvtPT == 0) tlDvtPT = 1;

                    // Tính khối lượng thực tế (Dựa vào hasKho và hasPT)
                    double wQddt = item.wQddt * tlDvtToanD;
                    double wTtGdcb = item.wTtGdcb * tlDvtToanD;
                    double wTtGdcd = item.wTtGdcd * tlDvtToanD;
                    double wPcScd = item.wPcScd * tlDvtToanD;
                    double wPcTqd = item.wPcTqd * tlDvtToanD;

                    double wHcKhoD = hasKho ? (item.wHcKhoD * tlDvtToanD) : 0;
                    double wHcDonVi = item.wHcDonVi * tlDvtD;
                    double wHcPhoiThuoc = hasPT ? (item.wHcPhoiThuoc * tlDvtPT) : 0;

                    // Tạo dòng
                    Object[] row = new Object[27];
                    Arrays.fill(row, "-"); // Dùng "-" làm mặc định cho các ô trống
                    row[0] = item.ten;
                    row[1] = item.dvt;
                    if (huongName.equals("Toàn d")) {
                        row[2] = f(tlDvtToanD); row[3] = f(tlDvtD); row[4] = f(tlDvtPT);
                    } else {
                        row[2] = ""; row[3] = ""; row[4] = ""; // Chỉ điền TL ĐVT ở Toàn d theo Excel
                    }

                    // Điền dữ liệu và cộng dồn vào mảng tổng (chia 1000 ra Tấn)
                    // (Đoạn này bạn bê logic gán array và cộng dồn từ code trước vào)
                    // Ví dụ:
                    row[5] = f(wQddt); tongCat[5] += wQddt/1000;
                    row[6] = f(wTtGdcb); tongCat[6] += wTtGdcb/1000;
                    // ... (Tương tự cho các cột tính toán GĐCB, GĐCĐ) ...

                    vatChatModel.addRow(row);
                }

                // --- CẬP NHẬT TỔNG VÀO DÒNG DANH MỤC TƯƠNG ỨNG ---
                for(int col=5; col<=22; col++) {
                    vatChatModel.setValueAt("<html><b>" + f(tongCat[col]) + "</b></html>", rowCatIdx, col);
                    tongHuong[col] += tongCat[col]; // Cộng dồn lên tổng Hướng
                }
            }

            // --- CẬP NHẬT TỔNG VÀO DÒNG HƯỚNG TƯƠNG ỨNG ---
            for(int col=5; col<=22; col++) {
                vatChatModel.setValueAt("<html><b>" + f(tongHuong[col]) + "</b></html>", rowHuongIdx, col);
            }
        }
    }


    private String f(double value) {
        if (value == 0) return "0";
        if (value == (long) value) return String.format("%d", (long) value);
        return String.format("%.2f", value);
    }

    public Map<String, String> getExportData() {
        Map<String, String> data = new HashMap<>();
        String key = (type == 1) ? "<<rows_bang_vat_chat>>" : "<<rows_bang_vat_tu>>";
        data.put(key, buildTableHtml(vatChatModel));
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