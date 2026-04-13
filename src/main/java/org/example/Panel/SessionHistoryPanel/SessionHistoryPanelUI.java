package org.example.Panel.SessionHistoryPanel;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Lịch sử kế hoạch — dữ liệu do {@link SessionHistoryPanelService} tải (lọc theo hình thức tập bài).
 */
public class SessionHistoryPanelUI extends JPanel {

    public interface SessionActionListener {
        void onCreateNewSession();

        void onContinueSession(int sessionId, String sessionName);
    }

    private final int currentUserId;
    private final String hinhThucTapBai;
    private final SessionHistoryPanelService service = new SessionHistoryPanelService();
    private final SessionActionListener listener;

    private DefaultTableModel model;
    private JTable table;

    public SessionHistoryPanelUI(int currentUserId, String hinhThucTapBai, SessionActionListener listener) {
        this.currentUserId = currentUserId;
        this.hinhThucTapBai = hinhThucTapBai != null ? hinhThucTapBai : "";
        this.listener = listener;

        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ KẾ HOẠCH BẢO ĐẢM HC-KT", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnCreateNew = UIUtils.createStyledButton("➕ Tạo kế hoạch mới", new Color(46, 204, 113));
        btnCreateNew.setPreferredSize(new Dimension(200, 45));
        btnCreateNew.addActionListener(e -> {
            if (listener != null) {
                listener.onCreateNewSession();
            }
        });

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnCreateNew, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Tên Bài Tập / Kế Hoạch", "Ngày Tạo", "Trạng Thái"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setMaxWidth(200);

        setupRenderer();

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

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFooter.setOpaque(false);

        JButton btnDelete = UIUtils.createStyledButton("🗑 Xóa kế hoạch", new Color(231, 76, 60));
        btnDelete.setPreferredSize(new Dimension(180, 45));
        btnDelete.addActionListener(e -> deleteSelectedSession());

        JButton btnContinue = UIUtils.createStyledButton("Tiếp tục kế hoạch đã chọn ➔", new Color(41, 128, 185));
        btnContinue.setPreferredSize(new Dimension(250, 45));
        btnContinue.addActionListener(e -> openSelectedSession());

        pnlFooter.add(btnDelete);
        pnlFooter.add(btnContinue);
        add(pnlFooter, BorderLayout.SOUTH);

        loadSessionsFromDB();
    }

    public void reloadSessions() {
        loadSessionsFromDB();
    }

    private void loadSessionsFromDB() {
        model.setRowCount(0);
        try {
            List<SessionHistoryPanelService.SessionRow> rows = service.loadSessions(currentUserId, hinhThucTapBai);
            for (SessionHistoryPanelService.SessionRow r : rows) {
                String strTrangThai = (r.trangThai == 1) ? "Đã hoàn thành" : "Đang soạn thảo";
                model.addRow(new Object[]{r.id, r.tenBaiTap, r.ngayTao, strTrangThai});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một kế hoạch trong danh sách để tiếp tục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int sessionId = (int) model.getValueAt(modelRow, 0);
        String sessionName = (String) model.getValueAt(modelRow, 1);
        if (listener != null) {
            listener.onContinueSession(sessionId, sessionName);
        }
    }

    private void deleteSelectedSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một kế hoạch trong danh sách để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int sessionId = (int) model.getValueAt(modelRow, 0);
        String sessionName = (String) model.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa kế hoạch: [" + sessionName + "] không?\nToàn bộ dữ liệu của kế hoạch này sẽ bị xóa vĩnh viễn!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (service.deleteSession(sessionId)) {
                JOptionPane.showMessageDialog(this, "Đã xóa kế hoạch thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadSessionsFromDB();
            } else {
                JOptionPane.showMessageDialog(this, "Không xóa được kế hoạch!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupRenderer() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

                if (col == 3) {
                    String status = value != null ? value.toString() : "";
                    if (status.equals("Đã hoàn thành")) {
                        c.setForeground(new Color(22, 163, 74));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(217, 119, 6));
                        setFont(getFont().deriveFont(Font.ITALIC));
                    }
                } else {
                    c.setForeground(new Color(51, 65, 85));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                if (isSelected) {
                    c.setBackground(new Color(224, 242, 254));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(0, 15, 0, 0)
                ));
                setFont(getFont().deriveFont(Font.BOLD));
                c.setForeground(new Color(15, 23, 42));

                if (isSelected) {
                    c.setBackground(new Color(224, 242, 254));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
    }
}
