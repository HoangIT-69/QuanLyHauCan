package org.example.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Tiện ích UI dùng chung cho phân hệ Kế hoạch bảo đảm (bảng + vùng nhập văn bản).
 */
public final class AssurancePlanUiUtils {

    public static final Color SLATE_TEXT = new Color(30, 41, 59);
    public static final Color SLATE_BORDER = new Color(203, 213, 225);

    private AssurancePlanUiUtils() {
    }

    public static JTextArea createModernTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(SLATE_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return area;
    }

    public static JScrollPane scrollBorderedTextArea(JTextArea textArea, int preferredHeight) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(new LineBorder(SLATE_BORDER, 1));
        scroll.setPreferredSize(new Dimension(800, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.makeScrollPassThrough(scroll);
        return scroll;
    }

    public static JScrollPane wrapVerticalScroll(JComponent content) {
        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        return mainScroll;
    }
}
