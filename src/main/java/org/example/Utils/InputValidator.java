package org.example.Utils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputValidator {

    // Khai báo public static để gọi từ mọi nơi: InputValidator.restrictToNumbers(...)
    public static void restrictToNumbers(JTextField textField, boolean allowDecimal) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                // Nếu cho phép nhập số thập phân (Float)
                if (allowDecimal) {
                    if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
                        e.consume();
                    }
                    if (c == '.' && textField.getText().contains(".")) {
                        e.consume();
                    }
                }
                // Nếu chỉ cho phép số nguyên (Int)
                else {
                    if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                        e.consume();
                    }
                }
            }
        });
    }

    public static boolean isValidMilitaryTime(String time) {
        // Định dạng chuẩn: "15.00 N-3", "08.30 N", "14.00 N+1"
        // Giờ từ 00-23, phút 00-59, dấu chấm, dấu cách, chữ N, có thể có +/- và số ngày
        String regex = "^([0-1]?[0-9]|2[0-3])\\.[0-5][0-9]\\sN([+-]\\d+)?$";
        return time != null && time.trim().matches(regex);
    }

    public static boolean isValidMapScale(String scale) {
        // Định dạng chuẩn: "1:50.000", "1:100.000", "1:25000"
        String regex = "^1:\\d{1,3}([.,]\\d{3})*$";
        return scale != null && scale.trim().matches(regex);
    }

    public static boolean isValidYear(String year) {
        // Định dạng: 4 chữ số, bắt đầu bằng 19 hoặc 20 (VD: 2026)
        String regex = "^(19|20)\\d{2}$";
        return year != null && year.trim().matches(regex);
    }

    // Thêm 2 hàm này vào InputValidator.java
    public static int parseIntSafe(Object obj) {
        if (obj == null || obj.toString().trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(obj.toString().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double parseDoubleSafe(Object obj) {
        if (obj == null || obj.toString().trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(obj.toString().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}