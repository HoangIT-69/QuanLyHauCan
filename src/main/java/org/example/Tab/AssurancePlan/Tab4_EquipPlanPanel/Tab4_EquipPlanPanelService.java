package org.example.Tab.AssurancePlan.Tab4_EquipPlanPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Nạp dữ liệu Step 4 vào bảng chỉ tiêu trang bị (Assurance Plan).
 */
public class Tab4_EquipPlanPanelService {

    public static final String[] GROUPS = {"dBB1+PT", "Hướng PN chủ yếu", "Hướng PN thứ yếu", "Hướng PN phía sau", "Lực lượng còn lại"};
    public static final String[] WEAPONS = {
            "SMPK 12,7mm", "Cối 100mm/e", "Cối 82mm/d", "Cối 60mm", "Súng SPG-9",
            "Súng B41", "Súng đại liên", "Súng trung liên", "Súng tiểu liên", "Súng ngắn", "Lựu đạn"
    };
    public static final String[] UNITS = {
            "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "Khẩu", "quả"
    };

    public void loadFromDatabase(int sessionId, DefaultTableModel model) {
        if (sessionId <= 0) return;

        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 3; c < model.getColumnCount(); c++) {
                model.setValueAt("", r, c);
            }
        }

        String sql = "SELECT vat_chat, dt_chitiet, pc04_chitiet FROM step4_quy_dinh_du_tru WHERE session_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String vatChat = rs.getString("vat_chat");
                if (vatChat == null) continue;
                String vcLower = vatChat.toLowerCase();

                int weaponIndex = -1;
                if (vcLower.contains("12,7")) weaponIndex = 0;
                else if (vcLower.contains("100mm")) weaponIndex = 1;
                else if (vcLower.contains("82mm")) weaponIndex = 2;
                else if (vcLower.contains("60mm")) weaponIndex = 3;
                else if (vcLower.contains("spg")) weaponIndex = 4;
                else if (vcLower.contains("b41")) weaponIndex = 5;
                else if (vcLower.contains("đại liên")) weaponIndex = 6;
                else if (vcLower.contains("trung liên")) weaponIndex = 7;
                else if (vcLower.contains("tiểu liên") || vcLower.contains("ak")) weaponIndex = 8;
                else if (vcLower.contains("ngắn")) weaponIndex = 9;
                else if (vcLower.contains("lựu đạn")) weaponIndex = 10;

                if (weaponIndex == -1) continue;

                String[] dtArr = rs.getString("dt_chitiet") != null ? rs.getString("dt_chitiet").split(",") : new String[0];
                String[] pcArr = rs.getString("pc04_chitiet") != null ? rs.getString("pc04_chitiet").split(",") : new String[0];
                double[] dt = parseArray(dtArr);
                double[] pc = parseArray(pcArr);

                fillRowData(model, 0 * 11 + weaponIndex, dt[2] + dt[3] + dt[4] + dt[5], pc[2] + pc[3] + pc[4] + pc[5]);
                fillRowData(model, 1 * 11 + weaponIndex, dt[2], pc[2]);
                fillRowData(model, 2 * 11 + weaponIndex, dt[3], pc[3]);
                fillRowData(model, 3 * 11 + weaponIndex, dt[4], pc[4]);
                fillRowData(model, 4 * 11 + weaponIndex, dt[5], pc[5]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillRowData(DefaultTableModel model, int row, double hienCo, double phaiCo) {
        if (hienCo == 0 && phaiCo == 0) return;

        String name = model.getValueAt(row, 1).toString().toLowerCase();
        String kb = "1,00";
        String kt = "1,00";
        if (name.contains("tiểu liên") || name.contains("ak")) kt = "0,98";
        else if (name.contains("b41")) kt = "0,97";

        double boSung = phaiCo - hienCo;
        String slBoSung = (boSung > 0) ? formatDouble(boSung) : "";
        String tg = (boSung > 0) ? "14.00 N-4" : "";
        String dd = (boSung > 0) ? "VTCH/đv" : "";
        String pt = (boSung > 0) ? "Tay ba" : "";

        model.setValueAt(formatDouble(hienCo), row, 4);
        model.setValueAt(formatDouble(hienCo), row, 5);
        model.setValueAt(kb, row, 6);
        model.setValueAt(kt, row, 7);
        model.setValueAt(formatDouble(phaiCo), row, 8);

        model.setValueAt(slBoSung, row, 9);
        model.setValueAt(tg, row, 10);
        model.setValueAt(dd, row, 11);
        model.setValueAt(pt, row, 12);
    }

    private double[] parseArray(String[] arr) {
        double[] res = new double[6];
        for (int i = 0; i < 6; i++) {
            if (i < arr.length) res[i] = InputValidator.parseDoubleSafe(arr[i]);
        }
        return res;
    }

    private String formatDouble(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        return String.format("%s", d).replace('.', ',');
    }
}
