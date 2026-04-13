package org.example.Tab.AssurancePlan.Tab5_MaterialPlanPanel;

import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelUI;
import org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelUI;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class Tab5_MaterialPanelUI extends JPanel {

    private final Tab5_MaterialPanelService materialService;

    private Tab5_DanPanelUI panelDan;
    private Tab5_VatChatPanelUI panelVCHC;
    private Tab5_VatChatPanelUI panelVTKT;

    private JTextArea txtGiaiDoanChuanBi;
    private JTextArea txtGiaiDoanChienDau;
    private JTextArea txtSauChienDau;

    public Tab5_MaterialPanelUI() {
        this(new Tab5_MaterialPanelService());
    }

    public Tab5_MaterialPanelUI(Tab5_MaterialPanelService materialService) {
        this.materialService = materialService != null ? materialService : new Tab5_MaterialPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        panelDan = new Tab5_DanPanelUI();
        panelVCHC = new Tab5_VatChatPanelUI("b) Vật chất hậu cần", 1);
        panelVTKT = new Tab5_VatChatPanelUI("c) Vật tư kỹ thuật", 2);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel lblTitle = new JLabel("V. BẢO ĐẢM ĐẠN, VẬT CHẤT HẬU CẦN, VẬT TƯ KỸ THUẬT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createVerticalStrut(25));

        mainContainer.add(UIUtils.createSectionLabel("1. Chỉ tiêu"));
        mainContainer.add(Box.createVerticalStrut(15));

        mainContainer.add(createPopupRow("a. Đạn", "Bảng Chỉ tiêu Đạn", panelDan));
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createPopupRow("b. Vật chất hậu cần", "Bảng Chỉ tiêu Vật chất Hậu cần", panelVCHC));
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(createPopupRow("c. Vật tư kỹ thuật", "Bảng Chỉ tiêu Vật tư Kỹ thuật", panelVTKT));

        mainContainer.add(Box.createVerticalStrut(30));

        mainContainer.add(UIUtils.createSectionLabel("2. Biện pháp bảo đảm"));
        mainContainer.add(Box.createVerticalStrut(15));

        txtGiaiDoanChuanBi = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("* Giai đoạn chuẩn bị:"));
        mainContainer.add(UIUtils.createTextAreaScroll(txtGiaiDoanChuanBi, 100));
        mainContainer.add(Box.createVerticalStrut(15));

        txtGiaiDoanChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("- Giai đoạn chiến đấu:"));
        mainContainer.add(UIUtils.createTextAreaScroll(txtGiaiDoanChienDau, 100));
        mainContainer.add(Box.createVerticalStrut(15));

        txtSauChienDau = UIUtils.createStandardTextArea();
        mainContainer.add(UIUtils.createSubSectionLabel("- Sau chiến đấu:"));
        mainContainer.add(UIUtils.createTextAreaScroll(txtSauChienDau, 100));

        JScrollPane mainScroll = new JScrollPane(mainContainer);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createPopupRow(String title, String popupTitle, JPanel panelToOpen) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 20, 10, 15)
        ));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(800, 60));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(71, 85, 105));
        row.add(lbl, BorderLayout.WEST);

        JButton btnOpen = UIUtils.createStyledButton("👁️ Xem & Khai báo bảng", new Color(41, 128, 185));
        btnOpen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOpen.addActionListener(e -> showPopup(popupTitle, panelToOpen));

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.add(btnOpen);
        row.add(pnlBtn, BorderLayout.EAST);

        return row;
    }

    private void showPopup(String title, JPanel panel) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = owner == null
                ? new JDialog((Frame) null, title, true)
                : new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(panel);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screenSize.width, screenSize.height);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void loadSessionData(int sessionId) {
        if (sessionId > 0) {
            panelDan.loadDataFromDatabase(sessionId);
            panelVCHC.loadDataFromDatabase(sessionId);
            panelVTKT.loadDataFromDatabase(sessionId);
        }
    }

    public Map<String, String> getExportData() {
        return materialService.buildExportData(
                panelDan,
                panelVCHC,
                panelVTKT,
                txtGiaiDoanChuanBi.getText(),
                txtGiaiDoanChienDau.getText(),
                txtSauChienDau.getText()
        );
    }
}
