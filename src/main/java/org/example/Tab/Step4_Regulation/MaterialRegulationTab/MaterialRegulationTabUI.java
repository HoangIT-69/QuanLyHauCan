package org.example.Tab.Step4_Regulation.MaterialRegulationTab;

import org.example.Panel.Step4_RegulationPanel.Step4RegulationRamStore;
import org.example.Popup.RegulationDetailDialog.RegulationDetailDialogService;
import org.example.Popup.RegulationDetailDialog.RegulationDetailDialogUI;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialRegulationTabUI extends JPanel {

    public static final int COL_VAT_CHAT = 0;
    public static final int COL_DVT = 1;
    public static final int COL_DU_TRU = 2;
    public static final int COL_0400 = 3;
    public static final int COL_SCD = 4;
    public static final int COL_GDCB = 5;
    public static final int COL_GDCD = 6;

    private static final String MSG_PHAN_CAP = "Vui lòng chọn Vật chất và điền Tổng số lượng (+) trước khi phân cấp!";

    private final MaterialRegulationTabService service = new MaterialRegulationTabService();
    private final String hinhThucTapBai;
    private int currentSessionId = -1;

    private DefaultTableModel model;
    private JTable table;

    private final int[] colWidths = {400, 80, 188, 188, 188, 188, 188};

    public MaterialRegulationTabUI(String hinhThucTapBai) {
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Vật chất", "ĐVT", "Dự trữ", "04.00-N", "SCĐ", "GĐCB", "GĐCĐ"};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                String ten = getValueAt(row, 0) != null ? getValueAt(row, 0).toString().trim() : "";
                if (isHeaderRow(ten)) {
                    return false;
                }
                return column > COL_DVT;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(new Color(224, 224, 224));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
        }

        setupRenderers();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int r = table.rowAtPoint(e.getPoint());
                if (r < 0) {
                    return;
                }
                openDetailDialogForRow(r, COL_DU_TRU, "Quy định dự trữ chi tiết");
            }
        });

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAddFromDB = UIUtils.createStyledButton("➕ Chọn từ Danh mục", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa dòng", new Color(231, 76, 60));
        pnlControls.add(btnAddFromDB);
        pnlControls.add(btnDel);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel headerPanel = createAbsoluteMaterialHeader(colWidths);
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(headerPanel, BorderLayout.CENTER);

        JViewport headerViewport = new JViewport();
        headerViewport.setView(headerContainer);

        scroll.getHorizontalScrollBar().addAdjustmentListener(e -> headerViewport.setViewPosition(new Point(e.getValue(), 0)));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(headerViewport, BorderLayout.CENTER);

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(17, 100));
        spacer.setBackground(new Color(241, 245, 249));
        spacer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)));
        headerWrapper.add(spacer, BorderLayout.EAST);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.setBorder(new LineBorder(new Color(100, 116, 139), 2));
        combinedPanel.add(headerWrapper, BorderLayout.NORTH);
        combinedPanel.add(scroll, BorderLayout.CENTER);

        add(pnlControls, BorderLayout.NORTH);
        add(combinedPanel, BorderLayout.CENTER);

        btnAddFromDB.addActionListener(e -> showDatabaseSelectionPopup());
        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                String ten = model.getValueAt(r, 0).toString().trim();
                if (!isHeaderRow(ten)) {
                    model.removeRow(r);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa dòng phân loại!");
                }
            }
        });
    }

    private boolean isHeaderRow(String text) {
        return text.equals("ĐẠN") || text.equals("VẬT CHẤT HẬU CẦN") || text.equals("VẬT TƯ KỸ THUẬT");
    }

    private void setupRenderers() {
        DefaultTableCellRenderer commonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String ten = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim() : "";

                ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                        new EmptyBorder(0, 5, 0, 5)
                ));

                if (isHeaderRow(ten)) {
                    c.setBackground(new Color(255, 241, 118));
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                    setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                }

                if (isSelected && !isHeaderRow(ten)) {
                    c.setBackground(new Color(219, 234, 254));
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(commonRenderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        this.currentSessionId = sessionId;
        model.setRowCount(0);
        RegulationDetailDialogService.detailDataStore.clear();
        initBaseRows();

        if (sessionId <= 0) {
            List<MaterialRegulationTabService.SaveRow> ram = Step4RegulationRamStore.getMaterialDraft();
            if (ram != null && !ram.isEmpty()) {
                for (MaterialRegulationTabService.SaveRow s : ram) {
                    applyLoadedRowFromSave(s);
                }
            }
            return;
        }

        for (MaterialRegulationTabService.LoadedRow r : service.loadQuyDinhDuTru(sessionId)) {
            Object[] rowData = new Object[]{
                    r.vatChat,
                    r.dvt != null ? r.dvt : "",
                    String.valueOf(r.duTru),
                    String.valueOf(r.phaiCo0400),
                    String.valueOf(r.phaiCoScd),
                    String.valueOf(r.tieuThuGdcb),
                    String.valueOf(r.tieuThuGdcd)
            };
            addLoadedDataRow(r.loai, rowData);

            String vc = r.vatChat;
            RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_DU_TRU,
                    RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.dtChitiet)));
            RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_0400,
                    RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.pc04Chitiet)));
            RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_SCD,
                    RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.scdChitiet)));
            RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_GDCB,
                    RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.gdcbChitiet)));
            RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_GDCD,
                    RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.gdcdChitiet)));
        }
    }

    private void applyLoadedRowFromSave(MaterialRegulationTabService.SaveRow s) {
        MaterialRegulationTabService.LoadedRow r = new MaterialRegulationTabService.LoadedRow(
                s.loaiVatChat,
                s.vatChat,
                s.dvt,
                s.duTru,
                s.phaiCo0400,
                s.phaiCoScd,
                s.tieuThuGdcb,
                s.tieuThuGdcd,
                s.dtChitietJoined,
                s.pc04ChitietJoined,
                s.scdChitietJoined,
                s.gdcbChitietJoined,
                s.gdcdChitietJoined
        );
        Object[] rowData = new Object[]{
                r.vatChat,
                r.dvt != null ? r.dvt : "",
                String.valueOf(r.duTru),
                String.valueOf(r.phaiCo0400),
                String.valueOf(r.phaiCoScd),
                String.valueOf(r.tieuThuGdcb),
                String.valueOf(r.tieuThuGdcd)
        };
        addLoadedDataRow(r.loai, rowData);
        String vc = r.vatChat;
        RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_DU_TRU,
                RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.dtChitiet)));
        RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_0400,
                RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.pc04Chitiet)));
        RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_SCD,
                RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.scdChitiet)));
        RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_GDCB,
                RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.gdcbChitiet)));
        RegulationDetailDialogService.detailDataStore.put(vc + "_" + COL_GDCD,
                RegulationDetailDialogService.upgradeRawPartsToEight(splitCsv(r.gdcdChitiet)));
    }

    private static String[] splitCsv(String s) {
        if (s == null || s.isEmpty()) {
            return new String[0];
        }
        return s.split(",", -1);
    }

    private void addLoadedDataRow(int loai, Object[] rowData) {
        String headerName = loai == 1 ? "ĐẠN" : (loai == 2 ? "VẬT CHẤT HẬU CẦN" : "VẬT TƯ KỸ THUẬT");
        int insertIdx = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(headerName)) {
                insertIdx = i + 1;
                while (insertIdx < model.getRowCount() && !isHeaderRow(model.getValueAt(insertIdx, 0).toString())) {
                    insertIdx++;
                }
                break;
            }
        }
        if (insertIdx != -1) {
            model.insertRow(insertIdx, rowData);
        }
    }

    public boolean saveToDatabase(int sessionId) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        if (sessionId <= 0) {
            Step4RegulationRamStore.setMaterialDraft(collectSaveRows());
            return true;
        }
        return service.saveQuyDinhDuTru(sessionId, collectSaveRows());
    }

    private List<MaterialRegulationTabService.SaveRow> collectSaveRows() {
        List<MaterialRegulationTabService.SaveRow> list = new ArrayList<>();
        int currentType = 1;
        String[] def = RegulationDetailDialogService.DEFAULT_EIGHT;

        for (int i = 0; i < model.getRowCount(); i++) {
            String ten = model.getValueAt(i, 0).toString().trim();

            if (ten.equals("ĐẠN")) {
                currentType = 1;
                continue;
            }
            if (ten.equals("VẬT CHẤT HẬU CẦN")) {
                currentType = 2;
                continue;
            }
            if (ten.equals("VẬT TƯ KỸ THUẬT")) {
                currentType = 3;
                continue;
            }

            list.add(new MaterialRegulationTabService.SaveRow(
                    currentType,
                    ten,
                    model.getValueAt(i, COL_DVT) != null ? model.getValueAt(i, COL_DVT).toString().trim() : "",
                    InputValidator.parseDoubleSafe(model.getValueAt(i, COL_DU_TRU)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, COL_0400)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, COL_SCD)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, COL_GDCB)),
                    InputValidator.parseDoubleSafe(model.getValueAt(i, COL_GDCD)),
                    RegulationDetailDialogService.joinEight(RegulationDetailDialogService.detailDataStore.getOrDefault(ten + "_" + COL_DU_TRU, def)),
                    RegulationDetailDialogService.joinEight(RegulationDetailDialogService.detailDataStore.getOrDefault(ten + "_" + COL_0400, def)),
                    RegulationDetailDialogService.joinEight(RegulationDetailDialogService.detailDataStore.getOrDefault(ten + "_" + COL_SCD, def)),
                    RegulationDetailDialogService.joinEight(RegulationDetailDialogService.detailDataStore.getOrDefault(ten + "_" + COL_GDCB, def)),
                    RegulationDetailDialogService.joinEight(RegulationDetailDialogService.detailDataStore.getOrDefault(ten + "_" + COL_GDCD, def))
            ));
        }
        return list;
    }

    private void initBaseRows() {
        model.addRow(new Object[]{"ĐẠN", "", "", "", "", "", ""});
        model.addRow(new Object[]{"VẬT CHẤT HẬU CẦN", "", "", "", "", "", ""});
        model.addRow(new Object[]{"VẬT TƯ KỸ THUẬT", "", "", "", "", "", ""});
    }

    private JPanel createAbsoluteMaterialHeader(int[] w) {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        int totalW = 0;
        for (int width : w) {
            totalW += width;
        }
        p.setPreferredSize(new Dimension(totalW, 100));

        int[] x = new int[w.length + 1];
        x[0] = 0;
        for (int i = 0; i < w.length; i++) {
            x[i + 1] = x[i] + w[i];
        }

        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Đạn, vật chất hậu cần,<br>vật tư kỹ thuật</center></html>", x[0], 0, w[0], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐVT", x[1], 0, w[1], 100));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Quy định<br>dự trữ</center></html>", x[2], 0, w[2], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Phải có<br>04.00-N</center></html>", x[3], 0, w[3], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Phải có<br>SCĐ</center></html>", x[4], 0, w[4], 70));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tiêu thụ", x[5], 0, w[5] + w[6], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[5], 35, w[5], 35));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[6], 35, w[6], 35));

        p.add(createHeaderButton(x[2], 70, w[2], 30, "Quy định dự trữ chi tiết", COL_DU_TRU));
        p.add(createHeaderButton(x[3], 70, w[3], 30, "Phải có 04.00-N chi tiết", COL_0400));
        p.add(createHeaderButton(x[4], 70, w[4], 30, "Phải có SCĐ chi tiết", COL_SCD));
        p.add(createHeaderButton(x[5], 70, w[5], 30, "Tiêu thụ GĐCB chi tiết", COL_GDCB));
        p.add(createHeaderButton(x[6], 70, w[6], 30, "Tiêu thụ GĐCĐ chi tiết", COL_GDCD));

        return p;
    }

    private JButton createHeaderButton(int x, int y, int w, int h, String title, int colIndex) {
        JButton btn = new JButton("+");
        btn.setBounds(x, y, w, h);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 247, 255));
        btn.setForeground(new Color(41, 128, 185));
        btn.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (!ensureCanOpenDetail(colIndex, row)) {
                return;
            }
            openDetailDialogForRow(row, colIndex, title);
        });
        return btn;
    }

    private void openDetailDialogForRow(int row, int colIndex, String title) {
        if (!ensureCanOpenDetail(colIndex, row)) {
            return;
        }
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        RegulationDetailDialogUI dialog = new RegulationDetailDialogUI(owner, title, model, colIndex, hinhThucTapBai, currentSessionId);
        dialog.setVisible(true);
    }

    private boolean ensureCanOpenDetail(int targetCol, int row) {
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng dữ liệu trong bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String ten = model.getValueAt(row, COL_VAT_CHAT) != null ? model.getValueAt(row, COL_VAT_CHAT).toString().trim() : "";
        if (isHeaderRow(ten) || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, MSG_PHAN_CAP, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!service.isCatalogMaterial(ten)) {
            JOptionPane.showMessageDialog(this, MSG_PHAN_CAP, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        double tong = InputValidator.parseDoubleSafe(model.getValueAt(row, targetCol));
        if (tong <= 0) {
            JOptionPane.showMessageDialog(this, MSG_PHAN_CAP, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void showDatabaseSelectionPopup() {
        Set<String> existingItems = new HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, COL_VAT_CHAT) != null) {
                existingItems.add(model.getValueAt(i, COL_VAT_CHAT).toString().trim());
            }
        }

        List<MaterialRegulationTabService.CatalogPickRow> allItems = service.loadCatalogForPicker(existingItems);

        if (allItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả vật chất trong danh mục đã được thêm vào bảng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] display = allItems.stream().map(i -> "[" + i.categoryTag() + "] " + i.name).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Chọn vật chất:", "Danh mục", JOptionPane.PLAIN_MESSAGE, null, display, display[0]);

        if (sel != null) {
            for (MaterialRegulationTabService.CatalogPickRow item : allItems) {
                if (sel.contains(item.name)) {
                    insertData(item);
                    break;
                }
            }
        }
    }

    private void insertData(MaterialRegulationTabService.CatalogPickRow data) {
        int idx = -1;
        String search;
        if (data.isDan) {
            search = "ĐẠN";
        } else if (data.isVtkt) {
            search = "VẬT TƯ KỸ THUẬT";
        } else {
            search = "VẬT CHẤT HẬU CẦN";
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(search)) {
                idx = i + 1;
                while (idx < model.getRowCount() && !isHeaderRow(model.getValueAt(idx, 0).toString())) {
                    idx++;
                }
                break;
            }
        }
        if (idx != -1) {
            model.insertRow(idx, new Object[]{data.name, data.dvt, "", "", "", "", ""});
        }
    }
}
