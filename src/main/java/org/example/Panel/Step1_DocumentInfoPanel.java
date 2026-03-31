package org.example.Panel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Step1_DocumentInfoPanel extends JPanel {
    private DataDeclarationPanel parent;
    private JTextField txtTenVK, txtViTri, txtThoiGian, txtTyLe, txtNam, txtChiHuy, txtThayThe;
    private JTextField[] txtMapFields;

    public Step1_DocumentInfoPanel(DataDeclarationPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 50, 20, 50));

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setBackground(Color.WHITE);
        formContent.putClientProperty("FlatLaf.style", "arc: 15");
        formContent.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        int row = 0;

        txtTenVK = UIUtils.createTextField("VD: Kế hoạch bảo đảm HC-KT năm 2026");
        UIUtils.addCustomRow(formContent, gbc, row++, "Tên văn kiện:", txtTenVK);

        txtViTri = UIUtils.createTextField("Nhập vị trí...");
        UIUtils.addCustomRow(formContent, gbc, row++, "Vị trí chỉ huy:", txtViTri);

        txtThoiGian = UIUtils.createTextField("VD: 15.00 N-3");
        UIUtils.addCustomRow(formContent, gbc, row++, "Thời gian:", txtThoiGian);

        gbc.gridy = row++; gbc.gridx = 0; gbc.weightx = 0.2;
        JLabel lblMap = new JLabel("Mảnh bản đồ:");
        lblMap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMap.setForeground(new Color(71, 85, 105));
        formContent.add(lblMap, gbc);

        JPanel gridMapPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        gridMapPanel.setBackground(Color.WHITE);
        txtMapFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            txtMapFields[i] = new JTextField();
            txtMapFields[i].setPreferredSize(new Dimension(0, 35));
            gridMapPanel.add(txtMapFields[i]);
        }
        gbc.gridx = 1; gbc.weightx = 0.8;
        formContent.add(gridMapPanel, gbc);

        txtTyLe = UIUtils.createTextField("VD: 1:50.000");
        UIUtils.addCustomRow(formContent, gbc, row++, "Tỷ lệ:", txtTyLe);

        txtNam = UIUtils.createTextField("VD: 2026");
        InputValidator.restrictToNumbers(txtNam, false);
        UIUtils.addCustomRow(formContent, gbc, row++, "Năm:", txtNam);

        txtChiHuy = UIUtils.createTextField("Họ và tên");
        UIUtils.addCustomRow(formContent, gbc, row++, "Chỉ huy HC-KT:", txtChiHuy);

        txtThayThe = UIUtils.createTextField("Họ và tên");
        UIUtils.addCustomRow(formContent, gbc, row++, "Người thay thế:", txtThayThe);

        JScrollPane scrollPane = new JScrollPane(formContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnNext = UIUtils.createNavButtonWithIcon("Tiếp tục", new Color(41, 128, 185), "/images/next.png", true);

        // LOGIC LƯU DATABASE KHI BẤM NEXT
        btnNext.addActionListener(e -> {
            if (validateInputs() && saveToDatabase()) {
                parent.navigateStep(2);
            }
        });

        bottomPanel.add(btnNext);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- GỌI HÀM TẢI DỮ LIỆU CŨ NẾU LÀ TIẾP TỤC SESSION ---
        loadFromDatabase();
    }

    private boolean validateInputs() {
        if (txtTenVK.getText().trim().isEmpty() || txtViTri.getText().trim().isEmpty() ||
                txtChiHuy.getText().trim().isEmpty() || txtThayThe.getText().trim().isEmpty()) {
            showError("Vui lòng điền đầy đủ các thông tin!");
            return false;
        }
        if (!InputValidator.isValidMilitaryTime(txtThoiGian.getText())) {
            showError("Thời gian sai định dạng! Mẫu: 15.00 N-3");
            return false;
        }
        if (!InputValidator.isValidMapScale(txtTyLe.getText())) {
            showError("Tỷ lệ bản đồ sai định dạng! Mẫu: 1:50.000");
            return false;
        }
        if (!InputValidator.isValidYear(txtNam.getText())) {
            showError("Năm sai định dạng! Mẫu: 2026");
            return false;
        }
        for (JTextField mapField : txtMapFields) {
            String text = mapField.getText().trim();
            if (!text.isEmpty()) mapField.setText(text.toUpperCase());
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // --- HÀM TẢI DỮ LIỆU CŨ TỪ DATABASE LÊN GIAO DIỆN ---
    private void loadFromDatabase() {
        int sessionId = parent.getCurrentSessionId();
        if (sessionId == -1) return; // Nếu là -1 tức là đang tạo Kế hoạch mới, không cần load gì cả

        String sql = "SELECT * FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    txtTenVK.setText(rs.getString("ten_van_kien"));
                    txtViTri.setText(rs.getString("vi_tri_chi_huy"));
                    txtThoiGian.setText(rs.getString("thoi_gian"));
                    txtMapFields[0].setText(rs.getString("map_1"));
                    txtMapFields[1].setText(rs.getString("map_2"));
                    txtMapFields[2].setText(rs.getString("map_3"));
                    txtMapFields[3].setText(rs.getString("map_4"));
                    txtTyLe.setText(rs.getString("ty_le"));
                    txtNam.setText(String.valueOf(rs.getInt("nam")));
                    txtChiHuy.setText(rs.getString("chi_huy"));
                    txtThayThe.setText(rs.getString("nguoi_thay_the"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu Step 1: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- HÀM THỰC THI LƯU DATABASE ---
    private boolean saveToDatabase() {
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Nếu là tạo mới -> Insert bảng sessions trước để lấy ID
            if (parent.getCurrentSessionId() == -1) {
                String sqlSession = "INSERT INTO sessions (user_id, ten_bai_tap, trang_thai) VALUES (?, ?, 0)";
                try (PreparedStatement pstmtSession = conn.prepareStatement(sqlSession, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtSession.setInt(1, parent.getCurrentUserId());
                    pstmtSession.setString(2, txtTenVK.getText().trim());
                    pstmtSession.executeUpdate();

                    ResultSet rs = pstmtSession.getGeneratedKeys();
                    if (rs.next()) {
                        parent.setCurrentSessionId(rs.getInt(1)); // Lưu ID phiên lại cho Step sau
                    }
                }
            } else {
                // Nếu là cập nhật phiên cũ, cập nhật lại tên bài tập trong bảng sessions cho đồng bộ
                String sqlUpdateSession = "UPDATE sessions SET ten_bai_tap = ? WHERE id = ?";
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateSession)) {
                    pstmtUpdate.setString(1, txtTenVK.getText().trim());
                    pstmtUpdate.setInt(2, parent.getCurrentSessionId());
                    pstmtUpdate.executeUpdate();
                }
            }

            // 2. Lưu vào bảng step1_thong_tin (Cập nhật nếu đã tồn tại session_id)
            String sqlStep1 = "INSERT INTO step1_thong_tin (session_id, ten_van_kien, vi_tri_chi_huy, thoi_gian, map_1, map_2, map_3, map_4, ty_le, nam, chi_huy, nguoi_thay_the) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "ten_van_kien=VALUES(ten_van_kien), vi_tri_chi_huy=VALUES(vi_tri_chi_huy), thoi_gian=VALUES(thoi_gian), " +
                    "map_1=VALUES(map_1), map_2=VALUES(map_2), map_3=VALUES(map_3), map_4=VALUES(map_4), " +
                    "ty_le=VALUES(ty_le), nam=VALUES(nam), chi_huy=VALUES(chi_huy), nguoi_thay_the=VALUES(nguoi_thay_the)";

            try (PreparedStatement pstmtStep1 = conn.prepareStatement(sqlStep1)) {
                pstmtStep1.setInt(1, parent.getCurrentSessionId());
                pstmtStep1.setString(2, txtTenVK.getText().trim());
                pstmtStep1.setString(3, txtViTri.getText().trim());
                pstmtStep1.setString(4, txtThoiGian.getText().trim());
                pstmtStep1.setString(5, txtMapFields[0].getText().trim());
                pstmtStep1.setString(6, txtMapFields[1].getText().trim());
                pstmtStep1.setString(7, txtMapFields[2].getText().trim());
                pstmtStep1.setString(8, txtMapFields[3].getText().trim());
                pstmtStep1.setString(9, txtTyLe.getText().trim());
                pstmtStep1.setInt(10, Integer.parseInt(txtNam.getText().trim()));
                pstmtStep1.setString(11, txtChiHuy.getText().trim());
                pstmtStep1.setString(12, txtThayThe.getText().trim());

                pstmtStep1.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu Database: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}