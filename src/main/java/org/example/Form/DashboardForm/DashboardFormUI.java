package org.example.Form.DashboardForm;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.example.Form.LoginForm.LoginFormUI;
import org.example.Panel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DashboardFormUI extends JFrame {
    private final DashboardFormService dashboardService = new DashboardFormService();

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sidebar;

    private int currentUserId = -1;
    private int currentSessionId = -1;

    private String currentUser;
    private String currentRole;
    private String hinhThucTapBai;

    private PlanEstimationPanel currentPlanPanel;

    public DashboardFormUI(int userId, String username, String role, String hinhThuc) {
        this.currentUserId = userId;
        this.currentUser = dashboardService.resolveDisplayUsername(username);
        this.currentRole = dashboardService.normalizeRole(role);
        this.hinhThucTapBai = hinhThuc;

        setTitle("Phần mềm hỗ trợ tập bài bảo đảm hậu cần, kỹ thuật tiểu đoàn bộ binh chiến đấu");
        setSize(1280, 760);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(240, 244, 248));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(31, 110, 160));
        topBar.setPreferredSize(new Dimension(0, 55));
        topBar.setBorder(new EmptyBorder(0, 10, 0, 20));

        JButton btnToggle = new JButton();
        btnToggle.setBackground(new Color(31, 110, 160));
        btnToggle.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            URL menuIconUrl = getClass().getResource("/images/menu.png");
            if (menuIconUrl != null) {
                Image img = new ImageIcon(menuIconUrl).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                btnToggle.setIcon(new ImageIcon(img));
            } else {
                btnToggle.setText("☰");
                btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 24));
                btnToggle.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            btnToggle.setText("☰");
            btnToggle.setForeground(Color.WHITE);
        }

        btnToggle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnToggle.setBackground(new Color(21, 88, 130)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnToggle.setBackground(new Color(31, 110, 160)); }
        });

        JLabel lblTopTitle = new JLabel("  PHẦN MỀM HỖ TRỢ TẬP BÀI BẢO ĐẢM HẬU CẦN, KỸ THUẬT TIỂU ĐOÀN BỘ BINH CHIẾN ĐẤU");
        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTopTitle.setForeground(Color.WHITE);

        topBar.add(btnToggle, BorderLayout.WEST);
        topBar.add(lblTopTitle, BorderLayout.CENTER);
        rootPanel.add(topBar, BorderLayout.NORTH);

        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 225, 230)));

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(new EmptyBorder(20, 10, 15, 10));

        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            FlatSVGIcon svgIcon = new FlatSVGIcon(Objects.requireNonNull(getClass().getResource("/images/logo1.svg")));
            lblLogo.setIcon(svgIcon.derive(130, 130));
        } catch (Exception e) {
            lblLogo.setText("[ LỖI LOGO ]");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        JLabel lblAppTitle = new JLabel("<html><div style='text-align: center;'>PHẦN MỀM<br>HỖ TRỢ TẬP BÀI</div></html>", SwingConstants.CENTER);
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblAppTitle.setForeground(new Color(44, 62, 80));
        lblAppTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRole = new JLabel("Vai trò: " + currentRole + " | Bài: " + hinhThucTapBai);
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRole.setForeground(new Color(41, 128, 185));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(15));
        logoPanel.add(lblAppTitle);
        logoPanel.add(Box.createVerticalStrut(8));
        logoPanel.add(lblRole);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(Color.WHITE);
        menuContainer.setBorder(new EmptyBorder(10, 0, 0, 0));

        menuContainer.add(createMenuButton("Thông tin cá nhân", "ThongTin", null));
        menuContainer.add(createMenuButton("Khai báo dữ liệu", "KhaiBao", null));
        menuContainer.add(createMenuButton("Dự kiến kế hoạch", "DuKien", null));
        menuContainer.add(createMenuButton("Kế hoạch bảo đảm", "KeHoach", null));
        menuContainer.add(createMenuButton("Lịch sử phiên báo cáo", "LichSu", null));

        if (dashboardService.isAdminRole(currentRole)) {
            JPanel separator = new JPanel();
            separator.setMaximumSize(new Dimension(200, 1));
            separator.setBackground(new Color(230, 235, 240));
            menuContainer.add(Box.createVerticalStrut(5));
            menuContainer.add(separator);
            menuContainer.add(Box.createVerticalStrut(5));

            menuContainer.add(createMenuButton("Quản lý quy ước tính", "TinhToan", null));
            menuContainer.add(createMenuButton("Quản lý tài khoản", "NguoiDung", null));
        }

        sidebar.add(menuContainer, BorderLayout.CENTER);

        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(Color.WHITE);

        JPanel bottomInfoPanel = new JPanel();
        bottomInfoPanel.setLayout(new BoxLayout(bottomInfoPanel, BoxLayout.Y_AXIS));
        bottomInfoPanel.setBackground(new Color(248, 250, 252));
        bottomInfoPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        Font infoFont = new Font("Segoe UI", Font.PLAIN, 12);
        Color infoColor = new Color(100, 116, 139);

        JLabel lblVersion = new JLabel("Phiên bản: 1.0 (2026)");
        JLabel lblAuthor = new JLabel("Sáng kiến: ");
        lblVersion.setFont(infoFont); lblVersion.setForeground(infoColor);
        lblAuthor.setFont(infoFont); lblAuthor.setForeground(infoColor);

        bottomInfoPanel.add(lblVersion);
        bottomInfoPanel.add(Box.createVerticalStrut(5));
        bottomInfoPanel.add(lblAuthor);

        bottomWrapper.add(bottomInfoPanel, BorderLayout.CENTER);

        JButton btnLogout = createMenuButton("Đăng xuất", "Logout", "/images/logout.png");
        btnLogout.setBackground(new Color(254, 242, 242));
        btnLogout.setForeground(new Color(220, 38, 38));
        bottomWrapper.add(btnLogout, BorderLayout.SOUTH);

        sidebar.add(bottomWrapper, BorderLayout.SOUTH);

        btnToggle.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            rootPanel.revalidate();
        });

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        SessionHistoryPanel historyPanel = new SessionHistoryPanel(this.currentUserId, new SessionHistoryPanel.SessionActionListener() {
            @Override
            public void onCreateNewSession() {
                updateDeclarationPanel(-1);
            }

            @Override
            public void onContinueSession(int sessionId, String sessionName) {
                updateDeclarationPanel(sessionId);
            }
        });

        mainContentPanel.add(new UserProfilePanel(currentUser), "ThongTin");
        mainContentPanel.add(new PlanEstimationPanel(currentSessionId), "DuKien");
        mainContentPanel.add(new AssurancePlanPanel("", "", new HashMap<>(), new HashMap<>(), new HashMap<>(), currentSessionId), "KeHoach");
        mainContentPanel.add(historyPanel, "LichSu");

        if (dashboardService.isAdminRole(currentRole)) {
            mainContentPanel.add(new UserManagementPanel(), "NguoiDung");
            mainContentPanel.add(new CalculationConventionPanel(), "TinhToan");
        }

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);

        cardLayout.show(mainContentPanel, "LichSu");
    }

    private void updateDeclarationPanel(int sessionId) {
        this.currentSessionId = sessionId;

        for (Component comp : mainContentPanel.getComponents()) {
            if (comp instanceof DataDeclarationPanel || comp instanceof PlanEstimationPanel) {
                mainContentPanel.remove(comp);
            }
        }

        DataDeclarationPanel declarationPanel = new DataDeclarationPanel(hinhThucTapBai, currentUserId, currentSessionId);
        mainContentPanel.add(declarationPanel, "KhaiBao");

        PlanEstimationPanel planPanel = new PlanEstimationPanel(currentSessionId);
        mainContentPanel.add(planPanel, "DuKien");
        this.currentPlanPanel = planPanel;

        mainContentPanel.revalidate();
        mainContentPanel.repaint();

        cardLayout.show(mainContentPanel, "KhaiBao");
    }

    private JButton createMenuButton(String text, String cardName, String iconPath) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(12, 35, 12, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (iconPath != null) {
            try {
                URL iconUrl = getClass().getResource(iconPath);
                if (iconUrl != null) {
                    Image img = new ImageIcon(iconUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(img));
                    btn.setIconTextGap(15);
                    btn.setBorder(new EmptyBorder(12, 20, 12, 10));
                }
            } catch (Exception ignored) {}
        }

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (cardName.equals("Logout")) {
                    btn.setBackground(new Color(254, 226, 226));
                } else {
                    btn.setBackground(new Color(241, 245, 249));
                    btn.setForeground(new Color(41, 128, 185));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (cardName.equals("Logout")) {
                    btn.setBackground(new Color(254, 242, 242));
                    btn.setForeground(new Color(220, 38, 38));
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(71, 85, 105));
                }
            }
        });

        btn.addActionListener(e -> {
            if (cardName.equals("Logout")) {
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    this.dispose();
                    new LoginFormUI().setVisible(true);
                }
            } else {
                switch (cardName) {
                    case "ThongTin": refreshUserProfile(); break;
                    case "LichSu": refreshSessionHistory(); break;
                    case "KeHoach": refreshAssurancePlan(); break;
                }
                cardLayout.show(mainContentPanel, cardName);
            }
        });

        return btn;
    }

    private void refreshAssurancePlan() {
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp instanceof AssurancePlanPanel) {
                mainContentPanel.remove(comp);
                break;
            }
        }

        String danhGia = "";
        String nhiemVu = "";
        Map<String, String> dataTab6 = dashboardService.emptyStringMap();
        Map<String, String> dataTab10 = dashboardService.emptyStringMap();
        Map<String, String> dataTab11 = dashboardService.emptyStringMap();

        if (currentPlanPanel != null) {
            danhGia = currentPlanPanel.getTab1().getDanhGia();
            nhiemVu = currentPlanPanel.getTab2().getNhiemVu();
            dataTab6 = dashboardService.buildLivingDataForAssurance(
                    currentPlanPanel.getTab6().getAnUong(),
                    currentPlanPanel.getTab6().getMac(),
                    currentPlanPanel.getTab6().getONguNghi());
            dataTab10 = dashboardService.copyStringMap(currentPlanPanel.getTab10().getProtectionData());
            dataTab11 = dashboardService.copyStringMap(currentPlanPanel.getTab11().getCommandData());
        }

        AssurancePlanPanel assurancePanel = new AssurancePlanPanel(danhGia, nhiemVu, dataTab6, dataTab10, dataTab11, this.currentSessionId);
        mainContentPanel.add(assurancePanel, "KeHoach");

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void refreshUserProfile() {
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp instanceof UserProfilePanel) {
                mainContentPanel.remove(comp);
                break;
            }
        }
        mainContentPanel.add(new UserProfilePanel(currentUser), "ThongTin");
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void refreshSessionHistory() {
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp instanceof SessionHistoryPanel) {
                mainContentPanel.remove(comp);
                break;
            }
        }
        SessionHistoryPanel historyPanel = new SessionHistoryPanel(this.currentUserId, new SessionHistoryPanel.SessionActionListener() {
            @Override
            public void onCreateNewSession() {
                updateDeclarationPanel(-1);
            }

            @Override
            public void onContinueSession(int sessionId, String sessionName) {
                updateDeclarationPanel(sessionId);
            }
        });
        mainContentPanel.add(historyPanel, "LichSu");
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
}
