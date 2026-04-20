package org.example.Tab.AssurancePlan.Tab8_MaintPlanPanel;

import org.example.Utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Tab8_MaintPlanPanelService {

    public void loadFromDatabase(int sessionId, DefaultTableModel model, JComponent dialogParent) {
        for (int r = 2; r < 12; r++) {
            model.setValueAt("0", r, 2);
            model.setValueAt("0", r, 3);
        }

        String sql = "SELECT loai_vktb, so_luong_tham_gia, ti_le_hu_hong FROM step4_hu_hong_vktb WHERE session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String rawTen = rs.getString("loai_vktb");
                if (rawTen == null || rawTen.trim().isEmpty()) continue;

                String tenVK = rawTen.trim().toLowerCase();
                int soLuong = rs.getInt("so_luong_tham_gia");
                double tiLe = rs.getDouble("ti_le_hu_hong");

                int rowIndex = -1;
                if (tenVK.contains("12,7") || tenVK.contains("12.7")) rowIndex = 2;
                else if (tenVK.contains("100")) rowIndex = 3;
                else if (tenVK.contains("82")) rowIndex = 4;
                else if (tenVK.contains("60")) rowIndex = 5;
                else if (tenVK.contains("spg")) rowIndex = 6;
                else if (tenVK.contains("b41")) rowIndex = 7;
                else if (tenVK.contains("đại liên") || rawTen.contains("Đại liên")) rowIndex = 8;
                else if (tenVK.contains("trung liên") || rawTen.contains("Trung liên")) rowIndex = 9;
                else if (tenVK.contains("tiểu liên") || rawTen.contains("Tiểu liên")) rowIndex = 10;
                else if (tenVK.contains("ngắn") || rawTen.contains("Súng ngắn")) rowIndex = 11;

                if (rowIndex != -1) {
                    model.setValueAt(String.valueOf(soLuong), rowIndex, 2);
                    String strTiLe = (tiLe > 0) ? String.valueOf(tiLe).replace(".0", "").replace(".", ",") : "0";
                    model.setValueAt(strTiLe, rowIndex, 3);
                } else {
                    System.out.println("Cảnh báo: Không thể map vũ khí: " + rawTen + " vào dòng nào.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialogParent, "Lỗi lấy dữ liệu DB: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
