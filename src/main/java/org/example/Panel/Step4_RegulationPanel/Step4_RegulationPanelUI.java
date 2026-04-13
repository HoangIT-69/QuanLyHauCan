package org.example.Panel.Step4_RegulationPanel;

import org.example.Panel.DataDeclarationContext;
import org.example.Tab.Step4_Regulation.CasualtyRegulationTab.CasualtyRegulationTabUI;
import org.example.Tab.Step4_Regulation.DamageRegulationTab.DamageRegulationTabUI;
import org.example.Tab.Step4_Regulation.MaterialRegulationTab.MaterialRegulationTabUI;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Step4_RegulationPanelUI extends JPanel {
    private final DataDeclarationContext parent;
    private final Step4_RegulationPanelService service = new Step4_RegulationPanelService();

    private MaterialRegulationTabUI tab1;
    private CasualtyRegulationTabUI tab2;
    private DamageRegulationTabUI tab3;

    public Step4_RegulationPanelUI(DataDeclarationContext parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("KHAI BÁO QUY ĐỊNH DỰ TRỮ VÀ TỈ LỆ THIỆT HẠI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185));
        lblTitle.setBorder(new EmptyBorder(10, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        tab1 = new MaterialRegulationTabUI(parent.getHinhThucTapBai());
        tab2 = new CasualtyRegulationTabUI();
        tab3 = new DamageRegulationTabUI();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.addTab("1. Dự trữ & Tiêu thụ", tab1);
        tabbedPane.addTab("2. Tỉ lệ Thương binh", tab2);
        tabbedPane.addTab("3. Tỉ lệ Hư hỏng VKTB", tab3);

        add(tabbedPane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnBack = UIUtils.createNavButtonWithIcon("Quay lại Bước 3", new Color(149, 165, 166), "/images/return.png", false);
        JButton btnFinish = UIUtils.createNavButtonWithIcon("Lưu & Hoàn tất", new Color(46, 204, 113), "/images/check.png", true);

        btnBack.addActionListener(e -> parent.navigateStep(3));

        btnFinish.addActionListener(e -> {
            int sessionId = parent.getCurrentSessionId();
            if (!service.isValidSessionId(sessionId)) {
                JOptionPane.showMessageDialog(this, "Phiên làm việc không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean t1 = tab1.saveToDatabase(sessionId);
            boolean t2 = tab2.saveToDatabase(sessionId);
            boolean t3 = tab3.saveToDatabase(sessionId);

            if (t1 && t2 && t3) {
                JOptionPane.showMessageDialog(this, "Đã lưu toàn bộ dữ liệu Quy định thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu dữ liệu, vui lòng kiểm tra Console!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnBack, BorderLayout.WEST);
        bottomPanel.add(btnFinish, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        int sessionId = parent.getCurrentSessionId();
        System.out.println("DEBUG: Step 4 đang nhận SessionID = " + sessionId);

        if (service.isValidSessionId(sessionId)) {
            tab1.loadDataFromDatabase(sessionId);
            tab2.loadDataFromDatabase(sessionId);
            tab3.loadDataFromDatabase(sessionId);
        } else {
            System.err.println("CẢNH BÁO: Step 4 chưa nhận được SessionID hợp lệ!");
        }
    }
}
