package org.example.Panel.UserProfilePanel;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class UserProfilePanelUI extends JPanel {

    private final UserProfilePanelService profileService = new UserProfilePanelService();

    public UserProfilePanelUI(String username) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 15");

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 20, 30));
        header.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel("Thông tin hồ sơ của tài khoản: " + username + " (Nếu có sai sót hãy liên hệ Admin để thay đổi)");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(231, 76, 60));

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(5));
        header.add(lblSub);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 230, 230));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        header.add(Box.createVerticalStrut(15));
        header.add(line);
        card.add(header, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(new EmptyBorder(20, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.weightx = 1.0;

        int currentRow = 0;
        JTextField txtMaQN = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Mã Quân Nhân:");
        JTextField txtHoTen = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Họ và Tên:");
        JTextField txtNgaySinh = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Ngày sinh:");
        JTextField txtCapBac = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Cấp bậc:");
        JTextField txtChucVu = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Chức vụ:");
        JTextField txtDonVi = UIUtils.addReadOnlyField(formContent, gbc, currentRow++, "Đơn vị:");

        applyProfileToFields(username, txtMaQN, txtHoTen, txtNgaySinh, txtCapBac, txtChucVu, txtDonVi);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Color.WHITE);
        contentArea.putClientProperty("FlatLaf.style", "arc: 15");
        contentArea.add(formContent, BorderLayout.NORTH);

        card.add(contentArea, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void applyProfileToFields(String username, JTextField txtMaQN, JTextField txtHoTen,
                                      JTextField txtNgaySinh, JTextField txtCapBac, JTextField txtChucVu, JTextField txtDonVi) {
        UserProfilePanelService.ProfileLoadResult result = profileService.loadProfileByUsername(username);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu cá nhân: " + result.getErrorMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Map<String, String> f = result.getFields();
        txtMaQN.setText(displayField(f.get("ma_quan_nhan")));
        txtHoTen.setText(displayField(f.get("full_name")));
        txtNgaySinh.setText(displayField(f.get("dob")));
        txtCapBac.setText(displayField(f.get("rank")));
        txtChucVu.setText(displayField(f.get("chuc_vu")));
        txtDonVi.setText(displayField(f.get("don_vi")));
    }

    private static String displayField(String value) {
        return value != null ? value : "Chưa cập nhật";
    }
}
