package org.example.Panel.DataDeclarationPanel;

import org.example.Panel.DataDeclarationContext;
import org.example.Panel.Step1_DocumentInfoPanel.Step1_DocumentInfoPanelUI;
import org.example.Panel.Step2_OrganizationPanel.Step2_OrganizationPanelUI;
import org.example.Panel.Step3_MaterialPanel.Step3_MaterialPanelUI;
import org.example.Panel.Step4_RegulationPanel.Step4_RegulationPanelUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataDeclarationPanelUI extends JPanel implements DataDeclarationContext {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel lblStep1, lblStep2, lblStep3, lblStep4;

    private String hinhThucTapBai;
    private int currentUserId;
    private int currentSessionId;
    private final Step1_DocumentInfoPanelUI step1Panel;
    private final Step2_OrganizationPanelUI step2Panel;
    private final Step3_MaterialPanelUI step3Panel;
    private final Step4_RegulationPanelUI step4Panel;

    public DataDeclarationPanelUI(String hinhThucTapBai, int currentUserId, int sessionId) {
        this.hinhThucTapBai = hinhThucTapBai;
        this.currentUserId = currentUserId;
        this.currentSessionId = sessionId;

        setLayout(new BorderLayout());
        setOpaque(false);

        add(createStepperPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        step1Panel = new Step1_DocumentInfoPanelUI(this);
        step2Panel = new Step2_OrganizationPanelUI(this, hinhThucTapBai);
        step3Panel = new Step3_MaterialPanelUI(this, hinhThucTapBai);
        step4Panel = new Step4_RegulationPanelUI(this);

        cardPanel.add(step1Panel, "Step1");
        cardPanel.add(step2Panel, "Step2");
        cardPanel.add(step3Panel, "Step3");
        cardPanel.add(step4Panel, "Step4");

        add(cardPanel, BorderLayout.CENTER);

        navigateStep(1);
    }

    @Override
    public int getCurrentSessionId() {
        return currentSessionId;
    }

    @Override
    public void setCurrentSessionId(int sessionId) {
        this.currentSessionId = sessionId;
        System.out.println("DEBUG: DataDeclarationPanelUI vừa cập nhật SessionId mới = " + sessionId);
    }

    @Override
    public int getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public String getHinhThucTapBai() {
        return hinhThucTapBai;
    }

    @Override
    public void navigateStep(int step) {
        cardLayout.show(cardPanel, "Step" + step);
        updateStepperUI(step);

        Component currentComp = getVisibleCardComponent();
        if (currentComp instanceof Step4_RegulationPanelUI) {
            ((Step4_RegulationPanelUI) currentComp).refreshData();
        }
    }

    @Override
    public boolean saveStep1ToDatabase() {
        return step1Panel != null && step1Panel.saveToDatabase();
    }

    @Override
    public boolean saveStep2ToDatabase() {
        return step2Panel != null && step2Panel.saveToDatabase();
    }

    @Override
    public boolean saveStep3ToDatabase() {
        return step3Panel != null && step3Panel.saveToDatabasePublic();
    }

    private Component getVisibleCardComponent() {
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
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

        panel.add(lblStep1);
        panel.add(new JLabel(" ➔ "));
        panel.add(lblStep2);
        panel.add(new JLabel(" ➔ "));
        panel.add(lblStep3);
        panel.add(new JLabel(" ➔ "));
        panel.add(lblStep4);

        return panel;
    }

    private JLabel createStepLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }
}
