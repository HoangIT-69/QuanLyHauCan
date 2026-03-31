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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab8_MaintenancePanel extends JPanel {
    // Các ô nhập văn bản
    private JTextArea txtBDChuanBi, txtBDChienDau, txtBDSauChienDau;
    private JTextArea txtCanDoi;
    private JTextArea txtSuaChuaChuanBi, txtSuaChuaChienDau, txtSuaChuaSauChienDau;

    // Bảng dữ liệu
    private DefaultTableModel model;
    private JTable table;
    private boolean isUpdatingTable = false;

    public Tab8_MaintenancePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề lớn
        JLabel lblTitle = new JLabel("VIII. BẢO DƯỠNG, SỬA CHỮA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        // --- 1. BẢO DƯỠNG KỸ THUẬT ---
        mainContainer.add(UIUtils.createSectionLabel("1. Bảo dưỡng kỹ thuật"));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị:"));
        txtBDChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDChuanBi, 100));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtBDChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDChienDau, 100));

        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtBDSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtBDSauChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(30));

        // --- 2. SỬA CHỮA ---
        mainContainer.add(UIUtils.createSectionLabel("2. Sửa chữa"));
        mainContainer.add(UIUtils.createSubSectionLabel("a) Dự kiến tỉ lệ vũ khí trang bị hư hỏng"));
        mainContainer.add(createTablePanel());
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(UIUtils.createSubSectionLabel("* Cân đối:"));
        txtCanDoi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtCanDoi, 100));
        mainContainer.add(Box.createVerticalStrut(20));

        mainContainer.add(UIUtils.createSubSectionLabel("b) Ý định bảo đảm"));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chuẩn bị:"));
        txtSuaChuaChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaChuanBi, 120));

        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        txtSuaChuaChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaChienDau, 120));

        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        txtSuaChuaSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createTextAreaScroll(txtSuaChuaSauChienDau, 120));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    // =====================================================================
    // HÀM LẤY DỮ LIỆU ĐỂ XUẤT WORD
    // =====================================================================

    public Map<String, String> getMaintenanceTextData() {
        Map<String, String> data = new HashMap<>();
        data.put("<<bd_kt_cb>>", txtBDChuanBi.getText().trim());
        data.put("<<bd_kt_cd>>", txtBDChienDau.getText().trim());
        data.put("<<bd_kt_sau>>", txtBDSauChienDau.getText().trim());
        data.put("<<can_doi_sc>>", txtCanDoi.getText().trim());
        data.put("<<ydinh_sc_cb>>", txtSuaChuaChuanBi.getText().trim());
        data.put("<<ydinh_sc_cd>>", txtSuaChuaChienDau.getText().trim());
        data.put("<<ydinh_sc_sau>>", txtSuaChuaSauChienDau.getText().trim());
        return data;
    }

    public List<Object[]> getTableData() {
        List<Object[]> tableData = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[model.getColumnCount()];
            for (int j = 0; j < model.getColumnCount(); j++) {
                row[j] = model.getValueAt(i, j);
            }
            tableData.add(row);
        }
        return tableData;
    }

    // =====================================================================
    // LOGIC TẠO BẢNG TÍNH TOÁN
    // =====================================================================
    private JPanel createTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setPreferredSize(new Dimension(800, 250));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Nút điều khiển
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlControls.setBackground(Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("➕ Thêm vũ khí", new Color(41, 128, 185));
        JButton btnDel = UIUtils.createStyledButton("➖ Xóa", new Color(231, 76, 60));
        pnlControls.add(btnAdd); pnlControls.add(btnDel);
        pnl.add(pnlControls, BorderLayout.NORTH);

        String[] cols = {"STT", "Loại vũ khí", "Số lượng", "Tỉ lệ hư hỏng (%)", "Số vũ khí hư hỏng"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column != 4; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        // Tự động tính số lượng hư hỏng (Làm tròn lên)
        model.addTableModelListener(e -> {
            if (!isUpdatingTable && e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 2 || e.getColumn() == 3)) {
                isUpdatingTable = true;
                int r = e.getFirstRow();
                double soLuong = InputValidator.parseDoubleSafe(model.getValueAt(r, 2));
                double tiLe = InputValidator.parseDoubleSafe(model.getValueAt(r, 3));
                int huHong = (int) Math.ceil((soLuong * tiLe) / 100.0);
                model.setValueAt(String.valueOf(huHong), r, 4);
                isUpdatingTable = false;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        UIUtils.makeScrollPassThrough(scroll);
        pnl.add(scroll, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> {
            if (model.getRowCount() >= 10) {
                JOptionPane.showMessageDialog(this, "Tối đa 10 dòng để phù hợp mẫu Word.");
                return;
            }
            model.addRow(new Object[]{model.getRowCount() + 1, "", "0", "0", "0"});
        });
        btnDel.addActionListener(e -> {
            if (table.getSelectedRow() != -1) {
                model.removeRow(table.getSelectedRow());
                // Cập nhật lại STT sau khi xóa
                for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(i + 1, i, 0);
            }
        });

        return pnl;
    }
}