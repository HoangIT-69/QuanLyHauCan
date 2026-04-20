package org.example.Popup.Tab5_VatChatPanel;

import org.example.Utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.util.Map;

public class Tab5_VatChatPanelUI extends JPanel {

    private final Tab5_VatChatPanelService service;
    private final int type;
    private DefaultTableModel vatChatModel;
    private JTable table;
    private JPanel headerPanel;
    private JScrollPane tableScroll;
    private JViewport headerViewport;

    private final int[] BASE_COL_WIDTHS = {
            150, 50, 70, 60, 60, 70, 60, 60, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 70, 80, 80, 80
    };
    private final int BASE_ROW_HEIGHT = 30;
    private final int BASE_FONT_SIZE = 12;
    private double currentScale = 1.0;
    private MouseWheelListener zoomListener;

    public Tab5_VatChatPanelUI(String title, int type) {
        this(title, type, new Tab5_VatChatPanelService());
    }

    public Tab5_VatChatPanelUI(String title, int type, Tab5_VatChatPanelService service) {
        this.service = service != null ? service : new Tab5_VatChatPanelService();
        this.type = type;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        if (title != null && !title.isBlank()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
            add(lbl, BorderLayout.NORTH);
        }

        String[] cols = {
                "Chi tieu", "DVT", "TL DVT Toan d", "TL d", "TL PT",
                "QDDT", "TT GDCB", "TT GDCD", "PC Kho/d", "PC DV", "PC +",
                "HC Kho d", "HC Kho PT", "HC DV d", "HC DV PT", "HC +",
                "BS TQD Kho/d", "BS TQD DV", "BS GDCB Kho/d", "BS GDCB DV",
                "BS GDCD Kho/d", "BS GDCD DV", "BS +",
                "KH Thoi gian", "KH Dia diem", "KH Phuong thuc", "KH Nhiem vu"
        };

        vatChatModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 23;
            }
        };

        table = new JTable(vatChatModel);
        table.setTableHeader(null);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        setupVatChatRenderers();

        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UIUtils.makeScrollPassThrough(tableScroll);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        headerPanel = new JPanel(null);
        headerPanel.setBackground(new Color(241, 245, 249));
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

        int[] scaledWidths = new int[BASE_COL_WIDTHS.length];
        int totalWidth = 0;
        for (int i = 0; i < BASE_COL_WIDTHS.length; i++) {
            int w = Math.max(28, (int) Math.round(BASE_COL_WIDTHS[i] * scale));
            scaledWidths[i] = w;
            table.getColumnModel().getColumn(i).setPreferredWidth(w);
            table.getColumnModel().getColumn(i).setMinWidth(w);
            totalWidth += w;
        }

        rebuildAbsoluteHeader(scale, scaledWidths, newFontSize, totalWidth);
        table.revalidate();
        table.repaint();
    }

    private void rebuildAbsoluteHeader(double scale, int[] scaledWidths, int newFontSize, int totalWidth) {
        headerPanel.removeAll();

        int hRow = Math.max(18, (int) Math.round(30 * scale));
        int totalHeaderHeight = hRow * 3;
        int y0 = 0;
        int y1 = hRow;
        int y2 = hRow * 2;

        int[] x = new int[scaledWidths.length + 1];
        x[0] = 0;
        for (int i = 0; i < scaledWidths.length; i++) {
            x[i + 1] = x[i] + scaledWidths[i];
        }

        int wQddt = scaledWidths[5] + scaledWidths[6] + scaledWidths[7] + scaledWidths[8] + scaledWidths[9] + scaledWidths[10];
        int wHienCo = scaledWidths[11] + scaledWidths[12] + scaledWidths[13] + scaledWidths[14] + scaledWidths[15];
        int wPcTqd  = scaledWidths[16] + scaledWidths[17];
        int wBoSung = scaledWidths[18] + scaledWidths[19] + scaledWidths[20] + scaledWidths[21] + scaledWidths[22];
        int wKeHoach = scaledWidths[23] + scaledWidths[24] + scaledWidths[25] + scaledWidths[26];

        addHeaderCell("Chi tieu", x[0], y0, scaledWidths[0], totalHeaderHeight, newFontSize);
        addHeaderCell("DVT", x[1], y0, scaledWidths[1], totalHeaderHeight, newFontSize);
        addHeaderCell("<html><center>TL DVT<br>(Toan d)</center></html>", x[2], y0, scaledWidths[2], totalHeaderHeight, newFontSize);
        addHeaderCell("<html><center>TL DVT<br>(d)</center></html>", x[3], y0, scaledWidths[3], totalHeaderHeight, newFontSize);
        addHeaderCell("<html><center>TL DVT<br>(PT)</center></html>", x[4], y0, scaledWidths[4], totalHeaderHeight, newFontSize);

        addHeaderCell("Quy dinh du tru, tieu thu", x[5], y0, wQddt, hRow, newFontSize);
        addHeaderCell("Hien co", x[11], y0, wHienCo, hRow, newFontSize);
        addHeaderCell("PC TQD", x[16], y0, wPcTqd, hRow, newFontSize);
        addHeaderCell("Bo sung", x[18], y0, wBoSung, hRow, newFontSize);
        addHeaderCell("Ke hoach tiep nhan, bao dam", x[23], y0, wKeHoach, hRow, newFontSize);

        addHeaderCell("Quy dinh du tru", x[5], y1, scaledWidths[5], hRow * 2, newFontSize);
        addHeaderCell("Tieu thu", x[6], y1, scaledWidths[6] + scaledWidths[7], hRow, newFontSize);
        addHeaderCell("PC SCD", x[8], y1, scaledWidths[8] + scaledWidths[9] + scaledWidths[10], hRow, newFontSize);

        addHeaderCell("Kho/d", x[11], y1, scaledWidths[11] + scaledWidths[12], hRow, newFontSize);
        addHeaderCell("DV", x[13], y1, scaledWidths[13] + scaledWidths[14], hRow, newFontSize);
        addHeaderCell("+", x[15], y1, scaledWidths[15], hRow * 2, newFontSize);

        addHeaderCell("Kho/d", x[16], y1, scaledWidths[16], hRow * 2, newFontSize);
        addHeaderCell("DV", x[17], y1, scaledWidths[17], hRow * 2, newFontSize);
        addHeaderCell("GDCB", x[18], y1, scaledWidths[18] + scaledWidths[19], hRow, newFontSize);
        addHeaderCell("GDCD", x[20], y1, scaledWidths[20] + scaledWidths[21], hRow, newFontSize);
        addHeaderCell("+", x[22], y1, scaledWidths[22], hRow * 2, newFontSize);

        addHeaderCell("Thoi gian", x[23], y1, scaledWidths[23], hRow * 2, newFontSize);
        addHeaderCell("Dia diem", x[24], y1, scaledWidths[24], hRow * 2, newFontSize);
        addHeaderCell("Phuong thuc", x[25], y1, scaledWidths[25], hRow * 2, newFontSize);
        addHeaderCell("Nhiem vu", x[26], y1, scaledWidths[26], hRow * 2, newFontSize);

        addHeaderCell("GDCB", x[6], y2, scaledWidths[6], hRow, newFontSize);
        addHeaderCell("GDCD", x[7], y2, scaledWidths[7], hRow, newFontSize);
        addHeaderCell("Kho/d", x[8], y2, scaledWidths[8], hRow, newFontSize);
        addHeaderCell("DV", x[9], y2, scaledWidths[9], hRow, newFontSize);
        addHeaderCell("+", x[10], y2, scaledWidths[10], hRow, newFontSize);

        addHeaderCell("d", x[11], y2, scaledWidths[11], hRow, newFontSize);
        addHeaderCell("PT", x[12], y2, scaledWidths[12], hRow, newFontSize);
        addHeaderCell("d", x[13], y2, scaledWidths[13], hRow, newFontSize);
        addHeaderCell("PT", x[14], y2, scaledWidths[14], hRow, newFontSize);

        addHeaderCell("Kho/d", x[18], y2, scaledWidths[18], hRow, newFontSize);
        addHeaderCell("DV", x[19], y2, scaledWidths[19], hRow, newFontSize);
        addHeaderCell("Kho/d", x[20], y2, scaledWidths[20], hRow, newFontSize);
        addHeaderCell("DV", x[21], y2, scaledWidths[21], hRow, newFontSize);

        headerPanel.setPreferredSize(new Dimension(totalWidth, totalHeaderHeight));
        headerViewport.setPreferredSize(new Dimension(Math.max(0, headerViewport.getWidth()), totalHeaderHeight));
        headerPanel.revalidate();
        headerPanel.repaint();
        headerViewport.revalidate();
        headerViewport.repaint();
    }

    private void addHeaderCell(String text, int x, int y, int width, int height, int fontSize) {
        JLabel label = UIUtils.createAbsoluteHeaderLabel(text, x, y, width, height);
        label.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, fontSize)));
        label.addMouseWheelListener(zoomListener);
        headerPanel.add(label);
    }

    private void setupVatChatRenderers() {
        Color gridColor = new Color(203, 213, 225);
        DefaultTableCellRenderer vatChatRenderer = new DefaultTableCellRenderer() {
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
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                Font base = table.getFont() != null ? table.getFont() : new Font("Segoe UI", Font.PLAIN, 12);
                setFont(base);
                ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, column == 26 ? 0 : 1, gridColor));

                if (column < 23 && !isSelected) {
                    c.setBackground(new Color(248, 250, 252));
                } else if (isSelected) {
                    c.setBackground(new Color(219, 234, 254));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < BASE_COL_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(vatChatRenderer);
        }
    }

    public void loadDataFromDatabase(int sessionId) {
        // UI này là bảng 27 cột nên luôn dùng luồng 27 cột trong service.
        service.loadDataFromDatabase(sessionId, type, vatChatModel);
    }

    public Map<String, String> getExportData() {
        return service.getExportData(type, vatChatModel);
    }
}
