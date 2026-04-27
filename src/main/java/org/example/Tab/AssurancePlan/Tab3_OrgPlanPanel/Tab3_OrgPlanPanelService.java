package org.example.Tab.AssurancePlan.Tab3_OrgPlanPanel;

import org.example.Utils.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xuất keyword Word / logic không phụ thuộc Swing (ngoài model bảng).
 */
public class Tab3_OrgPlanPanelService {

    public Map<String, String> buildExportData(DefaultTableModel model, String viTriChinhThuc, String viTriDuBi) {
        Map<String, String> data = new HashMap<>();
        int rowCount = model.getRowCount();
        
        // Xuất tối đa 10 dòng dữ liệu đầu tiên (theo placeholder trong template Word)
        for (int i = 0; i < 10; i++) {
            int r = i + 1;
            if (i < rowCount - 1) {
                data.put("<<tcll_name_" + r + ">>", cell(model, i, 0));
                data.put("<<tcll_sq_" + r + ">>", cell(model, i, 1));
                data.put("<<tcll_qn_" + r + ">>", cell(model, i, 2));
                data.put("<<tcll_cong_" + r + ">>", cell(model, i, 3));
                data.put("<<tcll_kn_" + r + ">>", cell(model, i, 4));
                data.put("<<tcll_hckt_" + r + ">>", cell(model, i, 5));
                data.put("<<tcll_db_" + r + ">>", cell(model, i, 6));
                data.put("<<tcll_tc_" + r + ">>", cell(model, i, 7));
            } else {
                // Nếu ít hơn 10 dòng thì để trống các placeholder còn lại
                data.put("<<tcll_name_" + r + ">>", "");
                data.put("<<tcll_sq_" + r + ">>", "");
                data.put("<<tcll_qn_" + r + ">>", "");
                data.put("<<tcll_cong_" + r + ">>", "");
                data.put("<<tcll_kn_" + r + ">>", "");
                data.put("<<tcll_hckt_" + r + ">>", "");
                data.put("<<tcll_db_" + r + ">>", "");
                data.put("<<tcll_tc_" + r + ">>", "");
            }
        }
        
        // Dòng tổng cộng luôn là dòng cuối cùng
        int lastRow = rowCount - 1;
        if (lastRow >= 0) {
            data.put("<<tcll_sq_sum>>", cell(model, lastRow, 1));
            data.put("<<tcll_qn_sum>>", cell(model, lastRow, 2));
            data.put("<<tcll_cong_sum>>", cell(model, lastRow, 3));
            data.put("<<tcll_hckt_sum>>", cell(model, lastRow, 5));
            data.put("<<tcll_db_sum>>", cell(model, lastRow, 6));
            data.put("<<tcll_tc_sum>>", cell(model, lastRow, 7));
        }
        
        data.put("{{vi_tri_chinh_thuc}}", viTriChinhThuc != null ? viTriChinhThuc.trim() : "");
        data.put("{{vi_tri_du_bi}}", viTriDuBi != null ? viTriDuBi.trim() : "");
        return data;
    }

    // loadTenLucLuongFromStep2 đã được xóa bỏ.

    private static String cell(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
