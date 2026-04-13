package org.example.Popup.Tab5_DanPanel;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.util.Map;

public class Tab5_DanPanelUI extends JPanel {

    private final Tab5_DanPanelService service;
    private DefaultTableModel danModel;
    private JTable table;
    private JPanel headerPanel;
    private JScrollPane tableScroll;
    private JViewport headerViewport;

    /**
     * Độ rộng cột gốc (scale = 1). Dùng chung cho JTable và header tuyệt đối — đồng bộ pixel khi zoom.
     */
    private static final int[] BASE_COL_WIDTHS = {
            150, // 0: Loại đạn
            70,  // 1: Số lượng VK
            60, 60, // 2-3: Nhu cầu (Cơ số, TL)
            60, 60, // 4-5: Tiêu thụ -> GĐCB (Cơ số, TL)
            60, 60, // 6-7: Tiêu thụ -> GĐCĐ (Cơ số, TL)
            60, 60, 60, // 8-10: PC SCĐ (ĐV, Kho, TL)
            60, 60, 60, // 11-13: Hiện có -> ĐV (d, PT, TL)
            60, 60, 60, // 14-16: Hiện có -> Kho (d, PT, TL)
            60, 60, 60, // 17-19: PC TQĐ (ĐV, Kho, TL)
            50, 50, 50, // 20-22: KHTN -> Trước nổ -> ĐV (d, PT, TL)
            50, 50, 50, // 23-25: KHTN -> Trước nổ -> Kho (d, PT, TL)
            50, 50, 50  // 26-28: KHTN -> Thực hành (ĐV, Kho, TL)
    };

    private final int BASE_ROW_HEIGHT = 30;
    private final int BASE_FONT_SIZE = 12;
    private double currentScale = 1.0;
    private MouseWheelListener zoomListener;

    public Tab5_DanPanelUI() {
        this(new Tab5_DanPanelService());
    }

    public Tab5_DanPanelUI(Tab5_DanPanelService service) {
        this.service = service != null ? service : new Tab5_DanPanelService();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = new String[29];
        for (int i = 0; i < 29; i++) {
            cols[i] = String.valueOf(i);
        }

        danModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(danModel);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        setupDanRenderers();

        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(tableScroll);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        headerPanel = new JPanel(null);
        headerPanel.setBackground(Color.WHITE);
        headerViewport = new JViewport();
        headerViewport.setView(headerPanel);

        tableScroll.getHorizontalScrollBar().addAdjustmentListener(
                e -> headerViewport.setViewPosition(new Point(e.getValue(), 0))
        );

        JPanel combined = new JPanel(new BorderLayout());
        combined.setBackground(Color.WHITE);
        combined.add(headerViewport, BorderLayout.NORTH);
        combined.add(tableScroll, BorderLayout.CENTER);
        add(combined, BorderLayout.CENTER);

        initZoomListener();
        applyZoom(currentScale);
    }

    private void initZoomListener() {
        zoomListener = e -> {
            if (!e.isControlDown()) {
                return;
            }
            if (e.getWheelRotation() < 0) {
                currentScale = Math.min(2.5, currentScale + 0.1);
            } else {
                currentScale = Math.max(0.6, currentScale - 0.1);
            }
            applyZoom(currentScale);
            e.consume();
        };
        tableScroll.addMouseWheelListener(zoomListener);
        headerPanel.addMouseWheelListener(zoomListener);
    }

    private void applyZoom(double scale) {
        int newFontSize = Math.max(9, (int) Math.round(BASE_FONT_SIZE * scale));
        Font tableFont = new Font("SansSerif", Font.PLAIN, newFontSize);
        table.setFont(tableFont);
        table.setRowHeight(Math.max(16, (int) Math.round(BASE_ROW_HEIGHT * scale)));

        int n = Tab5_DanPanelService.COL_COUNT;
        int[] currentWidths = new int[n];
        int totalZoomWidth = 0;
        for (int i = 0; i < n; i++) {
            currentWidths[i] = (int) Math.round(BASE_COL_WIDTHS[i] * scale);
            totalZoomWidth += currentWidths[i];
        }

        for (int i = 0; i < n; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            int w = currentWidths[i];
            col.setPreferredWidth(w);
            col.setMinWidth(w);
            col.setMaxWidth(w);
        }

        rebuildAbsoluteHeader(scale, currentWidths, newFontSize, totalZoomWidth);
        table.revalidate();
        table.repaint();
    }

    /**
     * Cùng công thức toán học với {@link #createAbsoluteHeader()} nhưng dùng độ rộng đã scale và nhân y/height theo scale.
     */
    private void rebuildAbsoluteHeader(double scale, int[] w, int newFontSize, int totalWidth) {
        headerPanel.removeAll();

        int totalHeaderHeight = (int) Math.round(120 * scale);
        int y0 = (int) Math.round(0 * scale);
        int y30 = (int) Math.round(30 * scale);
        int y60 = (int) Math.round(60 * scale);
        int y90 = (int) Math.round(90 * scale);
        int h120 = (int) Math.round(120 * scale);
        int h90 = (int) Math.round(90 * scale);
        int h60 = (int) Math.round(60 * scale);
        int h30 = (int) Math.round(30 * scale);

        int[] x = new int[30];
        x[0] = 0;
        for (int i = 0; i < 29; i++) {
            x[i + 1] = x[i] + w[i];
        }

        // TẦNG 0 (y = 0)
        addHeaderCell("Loại đạn", x[0], y0, w[0], h120, newFontSize);
        addHeaderCell("<html><center>Số<br>lượng<br>VK</center></html>", x[1], y0, w[1], h120, newFontSize);
        addHeaderCell("Nhu cầu", x[2], y0, x[4] - x[2], h90, newFontSize);
        addHeaderCell("Tiêu thụ", x[4], y0, x[8] - x[4], h30, newFontSize);
        addHeaderCell("PC SCĐ", x[8], y0, x[11] - x[8], h60, newFontSize);
        addHeaderCell("Hiện có", x[11], y0, x[17] - x[11], h30, newFontSize);
        addHeaderCell("PC TQĐ", x[17], y0, x[20] - x[17], h60, newFontSize);
        addHeaderCell("Kế hoạch tiếp nhận, bảo đảm", x[20], y0, x[29] - x[20], h30, newFontSize);

        // TẦNG 1 (y = 30)
        addHeaderCell("GĐCB", x[4], y30, x[6] - x[4], h60, newFontSize);
        addHeaderCell("GĐCĐ", x[6], y30, x[8] - x[6], h60, newFontSize);
        addHeaderCell("ĐV", x[11], y30, x[14] - x[11], h30, newFontSize);
        addHeaderCell("Kho", x[14], y30, x[17] - x[14], h30, newFontSize);
        addHeaderCell("Trước nổ súng", x[20], y30, x[26] - x[20], h30, newFontSize);
        addHeaderCell("thực hành nổ súng", x[26], y30, x[29] - x[26], h60, newFontSize);

        // TẦNG 2 (y = 60)
        addHeaderCell("ĐV", x[8], y60, w[8], h30, newFontSize);
        addHeaderCell("Kho", x[9], y60, w[9], h30, newFontSize);
        addHeaderCell("TL", x[10], y60, w[10], h60, newFontSize);
        addHeaderCell("d", x[11], y60, w[11], h30, newFontSize);
        addHeaderCell("PT", x[12], y60, w[12], h30, newFontSize);
        addHeaderCell("TL", x[13], y60, w[13], h60, newFontSize);
        addHeaderCell("d", x[14], y60, w[14], h30, newFontSize);
        addHeaderCell("PT", x[15], y60, w[15], h30, newFontSize);
        addHeaderCell("TL", x[16], y60, w[16], h60, newFontSize);
        addHeaderCell("ĐV", x[17], y60, w[17], h30, newFontSize);
        addHeaderCell("Kho", x[18], y60, w[18], h30, newFontSize);
        addHeaderCell("TL", x[19], y60, w[19], h60, newFontSize);
        addHeaderCell("ĐV", x[20], y60, x[23] - x[20], h30, newFontSize);
        addHeaderCell("Kho", x[23], y60, x[26] - x[23], h30, newFontSize);

        // TẦNG 3 (y = 90)
        addHeaderCell("Cơ số", x[2], y90, w[2], h30, newFontSize);
        addHeaderCell("TL", x[3], y90, w[3], h30, newFontSize);
        addHeaderCell("Cơ số", x[4], y90, w[4], h30, newFontSize);
        addHeaderCell("TL", x[5], y90, w[5], h30, newFontSize);
        addHeaderCell("Cơ số", x[6], y90, w[6], h30, newFontSize);
        addHeaderCell("TL", x[7], y90, w[7], h30, newFontSize);
        addHeaderCell("Cơ số", x[8], y90, w[8], h30, newFontSize);
        addHeaderCell("Cơ số", x[9], y90, w[9], h30, newFontSize);
        addHeaderCell("Cơ số", x[11], y90, w[11], h30, newFontSize);
        addHeaderCell("Cơ số", x[12], y90, w[12], h30, newFontSize);
        addHeaderCell("Cơ số", x[14], y90, w[14], h30, newFontSize);
        addHeaderCell("Cơ số", x[15], y90, w[15], h30, newFontSize);
        addHeaderCell("Cơ số", x[17], y90, w[17], h30, newFontSize);
        addHeaderCell("Cơ số", x[18], y90, w[18], h30, newFontSize);
        addHeaderCell("d", x[20], y90, w[20], h30, newFontSize);
        addHeaderCell("PT", x[21], y90, w[21], h30, newFontSize);
        addHeaderCell("TL", x[22], y90, w[22], h30, newFontSize);
        addHeaderCell("d", x[23], y90, w[23], h30, newFontSize);
        addHeaderCell("PT", x[24], y90, w[24], h30, newFontSize);
        addHeaderCell("TL", x[25], y90, w[25], h30, newFontSize);
        addHeaderCell("ĐV", x[26], y90, w[26], h30, newFontSize);
        addHeaderCell("Kho", x[27], y90, w[27], h30, newFontSize);
        addHeaderCell("TL", x[28], y90, w[28], h30, newFontSize);

        headerPanel.setPreferredSize(new Dimension(totalWidth, totalHeaderHeight));
        headerViewport.setPreferredSize(new Dimension(Math.max(0, headerViewport.getWidth()), totalHeaderHeight));
        headerPanel.revalidate();
        headerPanel.repaint();
        headerViewport.revalidate();
        headerViewport.repaint();
    }

    /**
     * Bố cục tuyệt đối scale = 1 (29 cột, 4 tầng × 30px) — đúng công thức Phase 2.
     * Phần hiển thị thời gian chạy dùng {@link #rebuildAbsoluteHeader} để đồng bộ zoom.
     */
    private JPanel createAbsoluteHeader() {
        int totalWidth = 0;
        for (int w : BASE_COL_WIDTHS) {
            totalWidth += w;
        }

        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(totalWidth, 120));

        int[] x = new int[30];
        x[0] = 0;
        for (int i = 0; i < 29; i++) {
            x[i + 1] = x[i] + BASE_COL_WIDTHS[i];
        }

        p.add(UIUtils.createAbsoluteHeaderLabel("Loại đạn", x[0], 0, BASE_COL_WIDTHS[0], 120));
        p.add(UIUtils.createAbsoluteHeaderLabel("<html><center>Số<br>lượng<br>VK</center></html>", x[1], 0, BASE_COL_WIDTHS[1], 120));
        p.add(UIUtils.createAbsoluteHeaderLabel("Nhu cầu", x[2], 0, x[4] - x[2], 90));
        p.add(UIUtils.createAbsoluteHeaderLabel("Tiêu thụ", x[4], 0, x[8] - x[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PC SCĐ", x[8], 0, x[11] - x[8], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Hiện có", x[11], 0, x[17] - x[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PC TQĐ", x[17], 0, x[20] - x[17], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kế hoạch tiếp nhận, bảo đảm", x[20], 0, x[29] - x[20], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCB", x[4], 30, x[6] - x[4], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("GĐCĐ", x[6], 30, x[8] - x[6], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[11], 30, x[14] - x[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[14], 30, x[17] - x[14], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Trước nổ súng", x[20], 30, x[26] - x[20], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("thực hành nổ súng", x[26], 30, x[29] - x[26], 60));

        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[8], 60, BASE_COL_WIDTHS[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[9], 60, BASE_COL_WIDTHS[9], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[10], 60, BASE_COL_WIDTHS[10], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[11], 60, BASE_COL_WIDTHS[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[12], 60, BASE_COL_WIDTHS[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[13], 60, BASE_COL_WIDTHS[13], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[14], 60, BASE_COL_WIDTHS[14], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[15], 60, BASE_COL_WIDTHS[15], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[16], 60, BASE_COL_WIDTHS[16], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[17], 60, BASE_COL_WIDTHS[17], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[18], 60, BASE_COL_WIDTHS[18], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[19], 60, BASE_COL_WIDTHS[19], 60));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[20], 60, x[23] - x[20], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[23], 60, x[26] - x[23], 30));

        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[2], 90, BASE_COL_WIDTHS[2], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[3], 90, BASE_COL_WIDTHS[3], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[4], 90, BASE_COL_WIDTHS[4], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[5], 90, BASE_COL_WIDTHS[5], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[6], 90, BASE_COL_WIDTHS[6], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[7], 90, BASE_COL_WIDTHS[7], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[8], 90, BASE_COL_WIDTHS[8], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[9], 90, BASE_COL_WIDTHS[9], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[11], 90, BASE_COL_WIDTHS[11], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[12], 90, BASE_COL_WIDTHS[12], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[14], 90, BASE_COL_WIDTHS[14], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[15], 90, BASE_COL_WIDTHS[15], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[17], 90, BASE_COL_WIDTHS[17], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Cơ số", x[18], 90, BASE_COL_WIDTHS[18], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[20], 90, BASE_COL_WIDTHS[20], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[21], 90, BASE_COL_WIDTHS[21], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[22], 90, BASE_COL_WIDTHS[22], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("d", x[23], 90, BASE_COL_WIDTHS[23], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("PT", x[24], 90, BASE_COL_WIDTHS[24], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[25], 90, BASE_COL_WIDTHS[25], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("ĐV", x[26], 90, BASE_COL_WIDTHS[26], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("Kho", x[27], 90, BASE_COL_WIDTHS[27], 30));
        p.add(UIUtils.createAbsoluteHeaderLabel("TL", x[28], 90, BASE_COL_WIDTHS[28], 30));

        return p;
    }

    private void addHeaderCell(String text, int x, int y, int width, int height, int fontSize) {
        JLabel label = UIUtils.createAbsoluteHeaderLabel(text, x, y, width, height);
        label.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, fontSize)));
        label.addMouseWheelListener(zoomListener);
        headerPanel.add(label);
    }

    private void setupDanRenderers() {
        Color gridColor = new Color(203, 213, 225);
        DefaultTableCellRenderer standardRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text0 = table.getValueAt(row, 0) != null ? table.getValueAt(row, 0).toString() : "";
                String text0Trim = text0.trim();
                boolean isDirectionHeader = !text0Trim.isEmpty() && !text0.startsWith("      ");
                boolean isBbGroupRow = text0Trim.contains("Đạn BB nhóm");

                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, column == 0 ? 1 : 0, 1, 1, gridColor));

                Font base = table.getFont() != null ? table.getFont() : new Font("Segoe UI", Font.PLAIN, 12);
                String vs = value != null ? value.toString() : "";
                if (isDirectionHeader || isBbGroupRow) {
                    setFont(base.deriveFont(Font.BOLD));
                    c.setBackground(new Color(226, 232, 240));
                    if (isBbGroupRow && (column == 0 || !vs.isEmpty())) {
                        c.setForeground(new Color(192, 57, 43));
                    } else if (isDirectionHeader) {
                        c.setForeground(new Color(30, 41, 59));
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    setFont(base.deriveFont(Font.PLAIN));
                    c.setForeground(Color.BLACK);
                    if (column <= 22 && !isSelected) {
                        c.setBackground(new Color(250, 252, 255));
                    } else if (isSelected) {
                        c.setBackground(new Color(219, 234, 254));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < Tab5_DanPanelService.COL_COUNT; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(standardRenderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        DefaultTableModel m = service.getDanTableModel(sessionId);
        table.setModel(m);
        danModel = m;
        setupDanRenderers();
        applyZoom(currentScale);
    }

    public Map<String, String> getExportData() {
        return service.getExportData(danModel);
    }
}
