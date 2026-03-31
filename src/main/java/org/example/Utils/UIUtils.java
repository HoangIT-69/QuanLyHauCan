package org.example.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class UIUtils {

    // ====================================================================
    // 1. CÁC HÀM TẠO NÚT BẤM (BUTTONS)
    // ====================================================================

    public static JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    public static JButton createNavButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createNavButtonWithIcon(String text, Color bg, String iconPath, boolean iconOnRight) {
        // Kế thừa các cài đặt nền, font, viền từ hàm cũ
        JButton btn = createNavButton(text, bg);

        if (iconPath != null) {
            try {
                java.net.URL iconUrl = UIUtils.class.getResource(iconPath);
                if (iconUrl != null) {
                    Image img = new ImageIcon(iconUrl).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    btn.setIcon(new ImageIcon(img));
                    btn.setIconTextGap(10); // Khoảng cách giữa chữ và icon

                    if (iconOnRight) {
                        // Nút Next: Ép chữ sang trái, icon sang phải
                        btn.setHorizontalTextPosition(SwingConstants.LEFT);
                    }
                }
            } catch (Exception ignored) {}
        }
        return btn;
    }

    public static JButton createLargeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 50));
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 35));
        return btn;
    }

    // ====================================================================
    // 2. CÁC HÀM TẠO KHUNG GIAO DIỆN (PANELS)
    // ====================================================================

    public static JPanel createModernPanel(String titleText, String subTitleText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 15");

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 20, 30));
        header.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel(subTitleText);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(100, 116, 139));

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(5));
        header.add(lblSub);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 230, 230));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        header.add(Box.createVerticalStrut(15));
        header.add(line);

        card.add(header, BorderLayout.NORTH);

        JPanel contentArea = new JPanel();
        contentArea.setBackground(Color.WHITE);
        contentArea.putClientProperty("FlatLaf.style", "arc: 15");
        card.add(contentArea, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ====================================================================
    // 3. CÁC HÀM THÊM TRƯỜNG NHẬP LIỆU (FORM FIELDS)
    // ====================================================================

    public static void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, String placeholder) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.2;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setPreferredSize(new Dimension(0, 38));
        txt.putClientProperty("JTextField.placeholderText", placeholder);
        panel.add(txt, gbc);
    }

    public static void addInput(JPanel p, GridBagConstraints g, int r, int c, String l, JComponent comp) {
        g.gridy = r; g.gridx = c; g.weightx = 0; p.add(new JLabel(l), g);
        g.gridx = c + 1; g.weightx = 1; p.add(comp, g);
    }

    public static JTextField addReadOnlyField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txt.setForeground(new Color(44, 62, 80));
        txt.setPreferredSize(new Dimension(300, 38));
        txt.setEditable(false);
        txt.setBackground(new Color(248, 249, 250));
        txt.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(txt, gbc);
        return txt;
    }

    public static JTextField createTextField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setPreferredSize(new Dimension(0, 38));
        txt.putClientProperty("JTextField.placeholderText", placeholder);
        return txt;
    }

    public static void addCustomRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.2;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        panel.add(comp, gbc);
    }


    // Thêm vào UIUtils.java

    // 1. HÀM TẠO Ô HEADER BẢNG DÙNG CHUNG
    public static JLabel createAbsoluteHeaderLabel(String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        l.setBounds(x, y, w, h);
        l.setOpaque(true);
        l.setBackground(new Color(241, 245, 249));
        return l;
    }

    // 2. HÀM TÔ MÀU BẢNG QUÂN SỰ DÙNG CHUNG
    public static DefaultTableCellRenderer getStandardTableRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String label = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString().trim() : "";

                // Nhận diện dòng TỔNG CỘNG (Tô màu Hồng/Đỏ)
                if (label.contains("TỔNG") || label.contains("Cộng")) {
                    c.setBackground(new Color(254, 226, 226));
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD, 14f));
                }
                // Nhận diện dòng TIÊU ĐỀ CHÍNH (I, II, 1., 2. hoặc IN HOA) (Tô màu Vàng)
                else if (label.matches("^(I|II|III|IV|V)$") || label.matches("^[A-ZÀ-Ỹ\\s]+$") || label.startsWith("1.") || label.startsWith("2.")) {
                    c.setBackground(new Color(255, 241, 118));
                    c.setFont(c.getFont().deriveFont(Font.BOLD, 14f));
                    c.setForeground(Color.BLACK);
                }
                // Nhận diện dòng PHỤ NHÓM (Bắt đầu bằng dấu trừ "-") (Tô màu Xám nhạt)
                else if (label.startsWith("-")) {
                    c.setBackground(new Color(241, 245, 249));
                    c.setFont(c.getFont().deriveFont(Font.ITALIC, 13f));
                    c.setForeground(Color.BLACK);
                }
                // Các dòng dữ liệu bình thường
                else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                // Căn lề: Cột đầu tiên (tên) căn trái, các cột số liệu căn giữa
                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

                if (isSelected && col > 0) c.setBackground(new Color(219, 234, 254)); // Bôi xanh khi click
                return c;
            }
        };
    }

    public static void makeScrollPassThrough(JScrollPane innerScroll) {
        innerScroll.setWheelScrollingEnabled(false);
        innerScroll.addMouseWheelListener(e -> {
            JScrollPane parentScroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, innerScroll.getParent());
            if (parentScroll != null) {
                java.awt.event.MouseWheelEvent parentEvent = new java.awt.event.MouseWheelEvent(
                        parentScroll, e.getID(), e.getWhen(), e.getModifiersEx(),
                        1, 1, e.getClickCount(), e.isPopupTrigger(),
                        e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
                parentScroll.dispatchEvent(parentEvent);
            }
        });
    }

    public static JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(10, 0, 10, 0));
        return lbl;
    }

    public static JLabel createSubSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 14));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    public static JTextArea createStandardTextArea() {
        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBorder(new EmptyBorder(10, 15, 10, 15));
        txt.setSelectionColor(new Color(219, 234, 254));
        return txt;
    }

    public static JScrollPane createTextAreaScroll(JTextArea textArea, int height) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        scroll.setPreferredSize(new Dimension(800, height));
        scroll.setMinimumSize(new Dimension(100, height));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        makeScrollPassThrough(scroll); // Tự động gọi xuyên lăn chuột
        return scroll;
    }

    public static JTextField createNumberField(int width) {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(width, 30));
        txt.setHorizontalAlignment(SwingConstants.CENTER);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txt.setForeground(new Color(41, 128, 185));

        // Đảm bảo Border được tạo đúng
        txt.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(203, 213, 225), 1),
                new javax.swing.border.EmptyBorder(2, 5, 2, 5)
        ));

        return txt; // <<< BẮT BUỘC PHẢI CÓ DÒNG NÀY
    }

    // Tạo ô hiển thị kết quả tính toán (Readonly)
    public static JTextField createAutoCalcField(int width) {
        JTextField txt = createNumberField(width); // Gọi hàm trên
        if (txt == null) return new JTextField(); // Phòng hờ lỗi rỗng

        txt.setForeground(new Color(192, 57, 43));
        txt.setBackground(new Color(241, 245, 249));
        txt.setEditable(false);

        return txt; // <<< BẮT BUỘC PHẢI CÓ DÒNG NÀY
    }

    // Nhãn hiển thị tổng cộng
    public static JLabel createSumLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(192, 57, 43));
        return lbl;
    }

    // Văn bản thường
    public static JLabel createNormalText(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    // Chuyển đổi an toàn từ TextField sang Int
    public static int safeInt(JTextField txt) {
        try {
            return Integer.parseInt(txt.getText().trim());
        } catch (Exception e) { return 0; }
    }

    public static JTextField createTimeField(int width) {
        JTextField txt = createNumberField(width); // Kế thừa style từ ô nhập số
        txt.setForeground(new Color(41, 128, 185)); // Màu xanh đặc trưng
        return txt;
    }
}
