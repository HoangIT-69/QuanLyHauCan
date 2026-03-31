package org.example.Popup;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;

public class UnitDataEntryDialog extends JDialog {

    public static Map<String, Vector<Vector<Object>>> unitDataStore = new HashMap<>();

    private final int[] colWidths = {300, 80, 60, 60, 60, 60, 80, 70, 60, 60, 60, 70, 70, 70, 70};
    private String currentUnit;
    private int sessionId;
    private DefaultTableModel model;
    private JTable table;
    private boolean isUpdatingSum = false;

    public UnitDataEntryDialog(Frame owner, String unitName,int sessionId) {
        super(owner, "Khai báo dữ liệu: " + unitName, true);
        this.currentUnit = unitName;
        this.sessionId = sessionId;

        System.out.println("DEBUG: UnitDataEntryDialog đang nạp SessionID = " + sessionId + " | Hướng: " + unitName);

        setSize(1350, 850);
        setLocationRelativeTo(owner);

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblMainTitle = new JLabel("TỔ CHỨC BIÊN CHẾ, TRANG BỊ " + unitName.toUpperCase(), SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        container.add(lblMainTitle, BorderLayout.NORTH);

        String[] columnNames = {"LỰC LƯỢNG", "QUÂN SỐ", "SN", "TL", "TrL", "ĐL", "B41, M79", "Lựu đạn", "60", "82", "100", "SPG9", "12.7", "ON", "ĐB"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row < 0 || row >= getRowCount()) return false;
                Object val = getValueAt(row, 0);
                if (val == null) return true;

                String unitStr = val.toString().trim();
                if (unitStr.startsWith("1.") || unitStr.startsWith("2.") || unitStr.equals("TỔNG CỘNG") || column == 0) return false;
                return true;
            }
        };

        table = new JTable(model);
        table.setRowHeight(38);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(UIUtils.getStandardTableRenderer());
        }

        model.addTableModelListener(e -> {
            if (!isUpdatingSum && e.getType() == TableModelEvent.UPDATE && e.getColumn() > 0) updateAutoSums();
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel headerPanel = createAbsoluteHeader();
        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerPanel);
        tableScroll.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);
        JPanel spacer = new JPanel(); spacer.setPreferredSize(new Dimension(17, 100));
        headerWrapper.add(spacer, BorderLayout.EAST);

        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlControls.setBackground(Color.WHITE);

        JButton btnAddFromDB = UIUtils.createStyledButton("➕ Thêm từ Danh mục", new Color(41, 128, 185));
        JButton btnDelRow = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        JLabel lblHelp = new JLabel("<html><b>HD:</b> 1. Chọn dòng vàng -> 2. Bấm Thêm để chọn đơn vị từ DB.</html>");

        pnlControls.add(btnAddFromDB); pnlControls.add(btnDelRow); pnlControls.add(lblHelp);

        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnReset = UIUtils.createNavButton("Làm mới", Color.ORANGE);
        JButton btnSave = UIUtils.createNavButton("Lưu & Đóng", new Color(34, 197, 94));
        pnlSouth.add(btnReset); pnlSouth.add(btnSave);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(pnlControls, BorderLayout.NORTH);
        centerPanel.add(combinedPanel, BorderLayout.CENTER);

        container.add(centerPanel, BorderLayout.CENTER);
        container.add(pnlSouth, BorderLayout.SOUTH);

        btnAddFromDB.addActionListener(e -> handleAddFromDB());
        btnDelRow.addActionListener(e -> handleDeleteRow());
        btnSave.addActionListener(e -> { saveCurrentData(); dispose(); });
        btnReset.addActionListener(e -> resetData());

        loadUnitData(currentUnit);
        add(container);
    }

    private void handleAddFromDB() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn dòng tiêu đề vàng (1. hoặc 2.) trước!"); return; }
        String header = model.getValueAt(row, 0).toString().trim();
        if (!header.startsWith("1.") && !header.startsWith("2.")) { JOptionPane.showMessageDialog(this, "Hãy chọn đúng dòng Tiêu đề vàng!"); return; }

        Set<String> addedUnits = new HashSet<>();

        // BƯỚC 1: QUÉT TRONG DATABASE (Phát hiện trùng lặp xuyên suốt Session, kể cả khi Log out/Login)
        String sqlCheck = "SELECT q.ten_don_vi FROM step2_bien_che s JOIN quyuoc_bienche q ON s.quyuoc_id = q.id WHERE s.session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setInt(1, this.sessionId);
            ResultSet rsCheck = pstmt.executeQuery();
            while (rsCheck.next()) {
                addedUnits.add(rsCheck.getString("ten_don_vi").trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // BƯỚC 2: QUÉT TRONG BỘ NHỚ RAM (Phát hiện trùng lặp khi người dùng vừa add nhưng chưa Lưu & Đóng)
        for (Vector<Vector<Object>> allData : unitDataStore.values()) {
            for (Vector<Object> r : allData) {
                if (r.isEmpty() || r.get(0) == null) continue;
                String name = r.get(0).toString().replace("  + ", "").trim();
                if (name.contains("]")) addedUnits.add(name.substring(name.indexOf("]") + 1).trim());
            }
        }

        // BƯỚC 3: QUÉT TRỰC TIẾP TRÊN BẢNG HIỆN TẠI (Đề phòng)
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null) {
                String name = val.toString().replace("  + ", "").trim();
                if (name.contains("]")) addedUnits.add(name.substring(name.indexOf("]") + 1).trim());
            }
        }

        // Tạo danh sách các mục CHƯA TỒN TẠI để hiển thị cho người dùng chọn
        List<Map<String, Object>> dbUnits = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM quyuoc_bienche")) {
            while (rs.next()) {
                String name = rs.getString("ten_don_vi").trim(); // Thêm trim() để an toàn hơn
                if (!addedUnits.contains(name)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name); data.put("nhom", rs.getString("nhom_don_vi"));
                    data.put("qs", rs.getInt("quan_so")); data.put("sn", rs.getInt("sung_ngan"));
                    data.put("tl", rs.getInt("tieu_lien")); data.put("trl", rs.getInt("trung_lien"));
                    data.put("dl", rs.getInt("dai_lien")); data.put("b41", rs.getInt("b41"));
                    data.put("ld", rs.getInt("luu_dan")); data.put("c60", rs.getInt("co60mm"));
                    data.put("c82", rs.getInt("co82mm")); data.put("c100", rs.getInt("co100mm"));
                    data.put("spg9", rs.getInt("spg9")); data.put("smpk", rs.getInt("smpk_127mm"));
                    dbUnits.add(data);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        if (dbUnits.isEmpty()) { JOptionPane.showMessageDialog(this, "Tất cả đơn vị đã được sử dụng!"); return; }

        String[] options = dbUnits.stream().map(m -> "[" + m.get("nhom") + "] " + m.get("name")).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Chọn đơn vị:", "Danh mục", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (sel != null) {
            for (Map<String, Object> d : dbUnits) {
                String checkName = "[" + d.get("nhom") + "] " + d.get("name");
                if (sel.equals(checkName)) {
                    model.insertRow(row + 1, new Object[]{
                            "  + " + checkName, d.get("qs"), d.get("sn"), d.get("tl"), d.get("trl"),
                            d.get("dl"), d.get("b41"), d.get("ld"), d.get("c60"), d.get("c82"),
                            d.get("c100"), d.get("spg9"), d.get("smpk"), 0, 0
                    }); break;
                }
            }
            updateAutoSums();
        }
    }

    private void handleDeleteRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String name = model.getValueAt(row, 0).toString().trim();
        if (name.startsWith("1.") || name.startsWith("2.") || name.equals("TỔNG CỘNG")) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa dòng này?") == JOptionPane.YES_OPTION) {
            model.removeRow(row); updateAutoSums();
        }
    }

    private void updateAutoSums() {
        if (isUpdatingSum || model.getRowCount() == 0) return;

        isUpdatingSum = true;
        try {
            int totalRow = -1, rowVang1 = -1, rowVang2 = -1;
            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 0);
                if (val == null) continue;
                String s = val.toString();
                if (s.startsWith("1.")) rowVang1 = i;
                else if (s.startsWith("2.")) rowVang2 = i;
                else if (s.equals("TỔNG CỘNG")) totalRow = i;
            }

            if (totalRow == -1) return;

            for (int col = 1; col < model.getColumnCount(); col++) {
                int sum1 = 0, sum2 = 0;
                for (int r = 0; r < totalRow; r++) {
                    Object nameObj = model.getValueAt(r, 0);
                    if (nameObj == null) continue;

                    String name = nameObj.toString();
                    if (name.startsWith("1.") || name.startsWith("2.")) continue;

                    int val = InputValidator.parseIntSafe(model.getValueAt(r, col));

                    if (rowVang1 != -1 && r > rowVang1 && (rowVang2 == -1 || r < rowVang2)) sum1 += val;
                    else if (rowVang2 != -1 && r > rowVang2) sum2 += val;
                }

                if (rowVang1 != -1) model.setValueAt(sum1, rowVang1, col);
                if (rowVang2 != -1) model.setValueAt(sum2, rowVang2, col);
                model.setValueAt(sum1 + sum2, totalRow, col);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính toán: " + e.getMessage());
        } finally { isUpdatingSum = false; }
    }

    private void saveCurrentData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        Vector<Vector<Object>> data = new Vector<>();
        for (Object row : model.getDataVector()) {
            data.add(new Vector<>((Vector<?>) row));
        }
        unitDataStore.put(currentUnit, data);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM step2_bien_che WHERE session_id = ? AND huong = ?")) {
                del.setInt(1, sessionId);
                del.setString(2, currentUnit);
                del.executeUpdate();
            }

            String sql = "INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, (SELECT id FROM quyuoc_bienche WHERE CONCAT('[', nhom_don_vi, '] ', ten_don_vi) = ? LIMIT 1), ?)";
            boolean hasUnits = false;
            try (PreparedStatement ins = conn.prepareStatement(sql)) {
                int currentLoai = 1;
                for (Vector<Object> row : data) {
                    if (row.isEmpty() || row.get(0) == null) continue;
                    String rawName = row.get(0).toString();

                    if (rawName.startsWith("1.")) { currentLoai = 1; continue; }
                    if (rawName.startsWith("2.")) { currentLoai = 2; continue; }
                    if (rawName.equals("TỔNG CỘNG")) continue;

                    String name = rawName.replace("  + ", "").trim();
                    if (name.startsWith("[") && name.contains("]")) {
                        ins.setInt(1, sessionId);
                        ins.setString(2, currentUnit);
                        ins.setString(3, name);
                        ins.setInt(4, currentLoai);
                        ins.addBatch();
                        hasUnits = true;
                    }
                }
                if (hasUnits) {
                    ins.executeBatch();
                } else {
                    try (PreparedStatement insFallback = conn.prepareStatement("INSERT INTO step2_bien_che (session_id, huong, quyuoc_id, phan_loai) VALUES (?, ?, 1, 1)")) {
                        insFallback.setInt(1, sessionId);
                        insFallback.setString(2, currentUnit);
                        insFallback.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUnitData(String unitName) {
        isUpdatingSum = true;
        model.setRowCount(0);

        Object[] header1 = {"1. Trong biên chế", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Object[] header2 = {"2. LL tăng cường", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Object[] footer = {"TỔNG CỘNG", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<Object[]> listBienChe = new ArrayList<>();
        List<Object[]> listTangCuong = new ArrayList<>();
        boolean hasDataInDB = false;

        String sql = "SELECT s.phan_loai, q.* FROM quyuoc_bienche q " +
                "JOIN step2_bien_che s ON q.id = s.quyuoc_id " +
                "WHERE s.session_id = ? AND s.huong = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.sessionId);
            pstmt.setString(2, unitName);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                hasDataInDB = true;
                int phanLoai = rs.getInt("phan_loai");
                String fullName = "[" + rs.getString("nhom_don_vi") + "] " + rs.getString("ten_don_vi");
                Object[] rowData = new Object[]{
                        "  + " + fullName,
                        rs.getInt("quan_so"), rs.getInt("sung_ngan"), rs.getInt("tieu_lien"),
                        rs.getInt("trung_lien"), rs.getInt("dai_lien"), rs.getInt("b41"),
                        rs.getInt("luu_dan"), rs.getInt("co60mm"), rs.getInt("co82mm"),
                        rs.getInt("co100mm"), rs.getInt("spg9"), rs.getInt("smpk_127mm"),
                        0, 0
                };
                if (phanLoai == 1) {
                    listBienChe.add(rowData);
                } else {
                    listTangCuong.add(rowData);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp dữ liệu đơn vị từ DB: " + e.getMessage());
        }

        if (!hasDataInDB && unitDataStore.containsKey(unitName) && !unitDataStore.get(unitName).isEmpty()) {
            model.setRowCount(0);
            for (Vector<Object> row : unitDataStore.get(unitName)) {
                model.addRow(row);
            }
        } else {
            model.setRowCount(0);
            model.addRow(header1);
            for (Object[] r : listBienChe) model.addRow(r);
            model.addRow(header2);
            for (Object[] r : listTangCuong) model.addRow(r);
            model.addRow(footer);
        }

        isUpdatingSum = false;
        updateAutoSums();
    }

    private JPanel createAbsoluteHeader() {
        int totalWidth = 0; for (int w : colWidths) totalWidth += w;
        JPanel p = new JPanel(null); p.setBackground(Color.WHITE); p.setPreferredSize(new Dimension(totalWidth, 100));
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

        String[] subCols = {"SN", "TL", "TrL", "ĐL", "B41, M79", "60", "82", "100", "SPG9", "12.7"};
        int[] subX = {2, 3, 4, 5, 6, 8, 9, 10, 11, 12};
        for (int i = 0; i < subCols.length; i++) {
            p.add(UIUtils.createAbsoluteHeaderLabel(subCols[i], x[subX[i]], 60, colWidths[subX[i]], 40));
        }
        return p;
    }

    private void resetData() {
        if (JOptionPane.showConfirmDialog(this, "Làm mới toàn bộ bảng?") == JOptionPane.YES_OPTION) {
            unitDataStore.remove(currentUnit);
            loadUnitData(currentUnit);
        }
    }
}