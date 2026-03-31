package org.example.Panel;

import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataDeclarationPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel lblStep1, lblStep2, lblStep3, lblStep4;

    // --- CÁC BIẾN QUẢN LÝ DỮ LIỆU TỪ DASHBOARD TRUYỀN VÀO ---
    private String hinhThucTapBai;
    private int currentUserId;
    private int currentSessionId; // sessionId truyền từ Dashboard

    // KHÔI PHỤC CONSTRUCTOR 3 THAM SỐ CỦA HOÀNG
    public DataDeclarationPanel(String hinhThucTapBai, int currentUserId, int sessionId) {
        this.hinhThucTapBai = hinhThucTapBai;
        this.currentUserId = currentUserId;
        this.currentSessionId = sessionId; // Nhận ID từ Dashboard (có thể là -1 nếu tạo mới)

        setLayout(new BorderLayout());
        setOpaque(false);

        // 1. Phần Stepper (Tiến trình)
        add(createStepperPanel(), BorderLayout.NORTH);

        // 2. Phần nội dung chính dùng CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        // Khởi tạo các Step và truyền "this" để các Step con có thể gọi getCurrentSessionId()
        cardPanel.add(new Step1_DocumentInfoPanel(this), "Step1");
        cardPanel.add(new Step2_OrganizationPanel(this), "Step2");
        cardPanel.add(new Step3_MaterialPanel(this), "Step3");
        cardPanel.add(new Step4_RegulationPanel(this), "Step4");

        add(cardPanel, BorderLayout.CENTER);

        // Mặc định luôn bắt đầu từ Bước 1
        navigateStep(1);
    }

    // --- CÁC GETTER / SETTER QUAN TRỌNG ---
    public int getCurrentSessionId() {
        return currentSessionId;
    }

    public void setCurrentSessionId(int sessionId) {
        this.currentSessionId = sessionId;
        System.out.println("DEBUG: DataDeclarationPanel vừa cập nhật SessionId mới = " + sessionId);
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getHinhThucTapBai() {
        return hinhThucTapBai;
    }

    // --- HÀM CHUYỂN BƯỚC THÔNG MINH ---
    public void navigateStep(int step) {
        cardLayout.show(cardPanel, "Step" + step);
        updateStepperUI(step);

        // Tự động load lại dữ liệu cho Step tương ứng khi hiện lên
        Component currentComp = getComponentInCard("Step" + step);
        if (currentComp instanceof Step3_MaterialPanel) {
            ((Step3_MaterialPanel) currentComp).loadDataFromDatabase();
        } else if (currentComp instanceof Step4_RegulationPanel) {
            ((Step4_RegulationPanel) currentComp).refreshData();
        }
    }

    private Component getComponentInCard(String cardId) {
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible()) return comp;
        }
        return null;
    }

    private void updateStepperUI(int step) {
        Color activeColor = new Color(41, 128, 185);
        Color completedColor = new Color(46, 204, 113);
        Color inactiveColor = new Color(150, 150, 150);

        lblStep1.setForeground(step >= 1 ? (step == 1 ? activeColor : completedColor) : inactiveColor);
        lblStep2.setForeground(step >= 2 ? (step == 2 ? activeColor : completedColor) : inactiveColor);
        lblStep3.setForeground(step >= 3 ? (step == 3 ? activeColor : completedColor) : inactiveColor);
        lblStep4.setForeground(step >= 4 ? (step == 4 ? activeColor : completedColor) : inactiveColor);
    }

    private JPanel createStepperPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        lblStep1 = createStepLabel("1. Thông tin văn kiện");
        lblStep2 = createStepLabel("2. Khai báo Biên chế");
        lblStep3 = createStepLabel("3. Khai báo Vật chất");
        lblStep4 = createStepLabel("4. Quy định dự trữ");

        panel.add(lblStep1); panel.add(new JLabel(" ➔ "));
        panel.add(lblStep2); panel.add(new JLabel(" ➔ "));
        panel.add(lblStep3); panel.add(new JLabel(" ➔ "));
        panel.add(lblStep4);

        return panel;
    }

    private JLabel createStepLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }
}