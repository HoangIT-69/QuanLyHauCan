package org.example.Form.LoginForm;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.example.Form.DashboardForm.DashboardFormUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.net.URL;
import java.util.Objects;

public class LoginFormUI extends JFrame {
    private final LoginFormService loginService = new LoginFormService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JCheckBox chkShowPass;

    private static final int LOGO_SIZE = 140;

    public LoginFormUI() {
        setTitle("Phần mềm hỗ trợ tập bài bảo đảm hậu cần, kỹ thuật tiểu đoàn bộ binh chiến đấu");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            private Image bgImage;
            {
                try {
                    URL bgUrl = getClass().getResource("/images/bg1.jpg");
                    if (bgUrl != null) {
                        bgImage = new ImageIcon(bgUrl).getImage();
                    }
                } catch (Exception e) {
                    System.err.println("Không tìm thấy ảnh nền!");
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), getWidth(), getHeight(), new Color(109, 213, 250));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(30, 50, 40, 50)
        ));

        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(0, 0, -10, 0));
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon(Objects.requireNonNull(getClass().getResource("/images/logo1.svg")));
            svgIcon = svgIcon.derive(LOGO_SIZE, LOGO_SIZE);
            lblLogo.setIcon(svgIcon);
        } catch (Exception ex) {
            lblLogo.setText("[ LOGO ]");
        }

        JLabel lblTitle = new JLabel("PHẦN MỀM HỖ TRỢ TẬP BÀI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setForeground(new Color(41, 128, 185));

        JLabel lblSubTitle = new JLabel("Bảo đảm hậu cần, kỹ thuật tiểu đoàn bộ binh chiến đấu");
        lblSubTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubTitle.setForeground(new Color(100, 116, 139));

        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "Tên đăng nhập (vd: admin)");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsername.setPreferredSize(new Dimension(350, 40));
        txtUsername.setMaximumSize(new Dimension(350, 40));
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Mật khẩu");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword.setPreferredSize(new Dimension(350, 40));
        txtPassword.setMaximumSize(new Dimension(350, 40));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword.addActionListener(e -> xuLyDangNhap());

        chkShowPass = new JCheckBox("Hiển thị mật khẩu");
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkShowPass.setForeground(new Color(100, 100, 100));
        chkShowPass.setFocusPainted(false);
        chkShowPass.setOpaque(false);

        chkShowPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chkPanel.setOpaque(false);
        chkPanel.setMaximumSize(new Dimension(350, 25));
        chkPanel.add(chkShowPass);
        chkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(350, 45));
        btnLogin.setMaximumSize(new Dimension(350, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> xuLyDangNhap());

        loginCard.add(lblLogo);
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(lblTitle);
        loginCard.add(Box.createVerticalStrut(3));
        loginCard.add(lblSubTitle);
        loginCard.add(Box.createVerticalStrut(25));
        loginCard.add(txtUsername);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(txtPassword);
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(chkPanel);
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(btnLogin);

        bgPanel.add(loginCard);
        setContentPane(bgPanel);
    }

    private void xuLyDangNhap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoginFormService.LoginResult result = loginService.authenticate(username, password);

        switch (result.getStatus()) {
            case SUCCESS -> {
                LoginFormService.AuthenticatedUser u = result.getUser();
                hienThiBangChonHinhThuc(u.getUserId(), u.getUsername(), u.getRole());
            }
            case INVALID_CREDENTIALS ->
                    JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Thất bại", JOptionPane.ERROR_MESSAGE);
            case DATABASE_UNAVAILABLE ->
                    JOptionPane.showMessageDialog(this, "Lỗi nghiêm trọng: Không thể kết nối tới cơ sở dữ liệu (cả Online và Offline)!", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            case ERROR ->
                    JOptionPane.showMessageDialog(this, "Lỗi khi xử lý đăng nhập:\n" + result.getErrorDetail(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThiBangChonHinhThuc(int userId, String username, String role) {
        JDialog dialog = new JDialog(this, "Lựa chọn Hình thức Tập bài", true);
        dialog.setSize(650, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("BẠN HÃY CHỌN HÌNH THỨC TẬP BÀI", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(41, 128, 185));
        lblHeader.setBorder(new EmptyBorder(25, 10, 15, 10));
        dialog.add(lblHeader, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 40, 40, 40));

        JButton btnTienCong = new JButton("<html><center><b style='font-size:16px;'>1. TIẾN CÔNG</b><br><span style='font-size:13px; font-weight:normal;'>Bảo đảm hậu cần, kỹ thuật tiểu đoàn bộ binh chiến đấu tiến công</span></center></html>");
        btnTienCong.setBackground(new Color(231, 76, 60));
        btnTienCong.setForeground(Color.WHITE);
        btnTienCong.setFocusPainted(false);
        btnTienCong.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnPhongNgu = new JButton("<html><center><b style='font-size:16px;'>2. PHÒNG NGỰ</b><br><span style='font-size:13px; font-weight:normal;'>Bảo đảm hậu cần, kỹ thuật tiểu đoàn bộ binh chiến đấu phòng ngự</span></center></html>");
        btnPhongNgu.setBackground(new Color(46, 204, 113));
        btnPhongNgu.setForeground(Color.WHITE);
        btnPhongNgu.setFocusPainted(false);
        btnPhongNgu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTienCong.addActionListener(e -> {
            dialog.dispose();
            moDashboard(userId, username, role, "Tiến công");
        });

        btnPhongNgu.addActionListener(e -> {
            dialog.dispose();
            moDashboard(userId, username, role, "Phòng ngự");
        });

        btnPanel.add(btnTienCong);
        btnPanel.add(btnPhongNgu);

        dialog.add(btnPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void moDashboard(int userId, String username, String role, String hinhThucDaChon) {
        new DashboardFormUI(userId, username, role, hinhThucDaChon).setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf");
        }
        SwingUtilities.invokeLater(() -> new LoginFormUI().setVisible(true));
    }
}
