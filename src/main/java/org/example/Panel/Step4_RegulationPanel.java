package org.example.Panel;

import org.example.Tab.Step4_Regulation.CasualtyRegulationTab;
import org.example.Tab.Step4_Regulation.DamageRegulationTab;
import org.example.Tab.Step4_Regulation.MaterialRegulationTab;
import org.example.Utils.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Step4_RegulationPanel extends JPanel {
    private DataDeclarationPanel parent;

    private MaterialRegulationTab tab1;
    private CasualtyRegulationTab tab2;
    private DamageRegulationTab tab3;

    public Step4_RegulationPanel(DataDeclarationPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("KHAI BÁO QUY ĐỊNH DỰ TRỮ VÀ TỈ LỆ THIỆT HẠI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185));
        lblTitle.setBorder(new EmptyBorder(10, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 2. Khởi tạo các Tab
        tab1 = new MaterialRegulationTab();
        tab2 = new CasualtyRegulationTab();
        tab3 = new DamageRegulationTab();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.addTab("1. Dự trữ & Tiêu thụ", tab1);
        tabbedPane.addTab("2. Tỉ lệ Thương binh", tab2);
        tabbedPane.addTab("3. Tỉ lệ Hư hỏng VKTB", tab3);

        add(tabbedPane, BorderLayout.CENTER);

        // --- BÍ QUYẾT: TỰ ĐỘNG LOAD KHI HIỂN THỊ ---
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData(); // Đảm bảo vừa hiện lên là lấy SessionId và Load ngay
            }
        });

        // 3. Bottom Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnBack = UIUtils.createNavButtonWithIcon("Quay lại Bước 3", new Color(149, 165, 166), "/images/return.png", false);
        JButton btnFinish = UIUtils.createNavButtonWithIcon("Lưu & Hoàn tất", new Color(46, 204, 113), "/images/check.png", true);

        btnBack.addActionListener(e -> parent.navigateStep(3));

        btnFinish.addActionListener(e -> {
            int sessionId = parent.getCurrentSessionId();
            if (sessionId <= 0) {
                JOptionPane.showMessageDialog(this, "Phiên làm việc không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lưu cả 3 Tab
            boolean t1 = tab1.saveToDatabase(sessionId);
            boolean t2 = tab2.saveToDatabase(sessionId);
            boolean t3 = tab3.saveToDatabase(sessionId);

            // Kiểm tra đủ 3 tab mới báo thành công
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
        // In ra để kiểm tra xem đã nhận được ID thật (ví dụ: 2) hay vẫn là -1
        System.out.println("DEBUG: Step 4 đang nhận SessionID = " + sessionId);

        if (sessionId > 0) {
            tab1.loadDataFromDatabase(sessionId);
            tab2.loadDataFromDatabase(sessionId);
            tab3.loadDataFromDatabase(sessionId);
        } else {
            System.err.println("CẢNH BÁO: Step 4 chưa nhận được SessionID hợp lệ!");
        }
    }
}