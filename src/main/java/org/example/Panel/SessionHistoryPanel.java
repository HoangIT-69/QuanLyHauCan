package org.example.Panel;

import org.example.Utils.DBConnection;
import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SessionHistoryPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private int currentUserId;

    // Interface để giao tiếp với MainFrame (chuyển màn hình)
    public interface SessionActionListener {
        void onCreateNewSession();
        void onContinueSession(int sessionId, String sessionName);
    }
    private SessionActionListener listener;

    public SessionHistoryPanel(int currentUserId, SessionActionListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;

        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(245, 247, 250)); // Nền xám nhạt toàn hệ thống
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // --- 1. HEADER (TIÊU ĐỀ & NÚT TẠO MỚI) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ KẾ HOẠCH BẢO ĐẢM HC-KT", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnCreateNew = UIUtils.createStyledButton("➕ Tạo kế hoạch mới", new Color(46, 204, 113));
        btnCreateNew.setPreferredSize(new Dimension(200, 45));
        btnCreateNew.addActionListener(e -> {
            if (listener != null) listener.onCreateNewSession();
        });

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnCreateNew, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        String[] columns = {"ID", "Tên Bài Tập / Kế Hoạch", "Ngày Tạo", "Trạng Thái"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Chỉ đọc
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Format Header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));

        // Chỉnh độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(200);

        setupRenderer();

        // Thêm tính năng Double Click để mở bài
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedSession();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // --- 3. FOOTER (NÚT TIẾP TỤC) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFooter.setOpaque(false);

        JButton btnContinue = UIUtils.createStyledButton("Tiếp tục kế hoạch đã chọn ➔", new Color(41, 128, 185));
        btnContinue.setPreferredSize(new Dimension(250, 45));
        btnContinue.addActionListener(e -> openSelectedSession());

        pnlFooter.add(btnContinue);
        add(pnlFooter, BorderLayout.SOUTH);

        // --- 4. LOAD DỮ LIỆU ---
        loadSessionsFromDB();
    }

    private void loadSessionsFromDB() {
        model.setRowCount(0);
        String sql = "SELECT id, ten_bai_tap, ngay_tao, trang_thai FROM sessions WHERE user_id = ? ORDER BY ngay_tao DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String ten = rs.getString("ten_bai_tap");
                String ngayTao = rs.getString("ngay_tao");
                int trangThai = rs.getInt("trang_thai");

                String strTrangThai = (trangThai == 1) ? "Đã hoàn thành" : "Đang soạn thảo";

                model.addRow(new Object[]{id, ten, ngayTao, strTrangThai});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedSession() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một kế hoạch trong danh sách để tiếp tục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sessionId = (int) model.getValueAt(row, 0);
        String sessionName = (String) model.getValueAt(row, 1);

        if (listener != null) {
            listener.onContinueSession(sessionId, sessionName);
        }
    }

    private void setupRenderer() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

                if (col == 3) { // Cột trạng thái
                    String status = value.toString();
                    if (status.equals("Đã hoàn thành")) {
                        c.setForeground(new Color(22, 163, 74)); // Xanh lá
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(217, 119, 6)); // Cam
                        setFont(getFont().deriveFont(Font.ITALIC));
                    }
                } else {
                    c.setForeground(new Color(51, 65, 85));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                if (isSelected) {
                    c.setBackground(new Color(224, 242, 254)); // Xanh nhạt khi chọn
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(0, 15, 0, 0)
                ));
                setFont(getFont().deriveFont(Font.BOLD));
                c.setForeground(new Color(15, 23, 42));

                if (isSelected) c.setBackground(new Color(224, 242, 254));
                else c.setBackground(Color.WHITE);
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }
}