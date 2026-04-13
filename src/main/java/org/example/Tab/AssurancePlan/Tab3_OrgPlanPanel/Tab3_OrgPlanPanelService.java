package org.example.Tab.AssurancePlan.Tab3_OrgPlanPanel;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Xuất keyword Word / logic không phụ thuộc Swing (ngoài model bảng).
 */
public class Tab3_OrgPlanPanelService {

    public Map<String, String> buildExportData(DefaultTableModel model, String viTriChinhThuc, String viTriDuBi) {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            int r = i + 1;
            data.put("<<tcll_name_" + r + ">>", cell(model, i, 0));
            data.put("<<tcll_sq_" + r + ">>", cell(model, i, 1));
            data.put("<<tcll_qn_" + r + ">>", cell(model, i, 2));
            data.put("<<tcll_cong_" + r + ">>", cell(model, i, 3));
            data.put("<<tcll_kn_" + r + ">>", cell(model, i, 4));
            data.put("<<tcll_hckt_" + r + ">>", cell(model, i, 5));
            data.put("<<tcll_db_" + r + ">>", cell(model, i, 6));
            data.put("<<tcll_tc_" + r + ">>", cell(model, i, 7));
        }
        data.put("<<tcll_sq_sum>>", cell(model, 10, 1));
        data.put("<<tcll_qn_sum>>", cell(model, 10, 2));
        data.put("<<tcll_cong_sum>>", cell(model, 10, 3));
        data.put("<<tcll_hckt_sum>>", cell(model, 10, 5));
        data.put("<<tcll_db_sum>>", cell(model, 10, 6));
        data.put("<<tcll_tc_sum>>", cell(model, 10, 7));
        data.put("<<vi_tri_chinh_thuc>>", viTriChinhThuc != null ? viTriChinhThuc.trim() : "");
        data.put("<<vi_tri_du_bi>>", viTriDuBi != null ? viTriDuBi.trim() : "");
        return data;
    }

    private static String cell(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
