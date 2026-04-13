package org.example.Tab.AssurancePlan.Tab5_MaterialPlanPanel;

import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelUI;
import org.example.Popup.Tab5_VatChatPanel.Tab5_VatChatPanelUI;

import java.util.HashMap;
import java.util.Map;

/**
 * Gom dữ liệu xuất Word từ các popup Tab 5 (Đạn, VCHC, VTKT) và vùng biện pháp.
 */
public class Tab5_MaterialPanelService {

    public Map<String, String> buildExportData(
            Tab5_DanPanelUI panelDan,
            Tab5_VatChatPanelUI panelVCHC,
            Tab5_VatChatPanelUI panelVTKT,
            String giaiDoanChuanBi,
            String giaiDoanChienDau,
            String sauChienDau) {
        Map<String, String> data = new HashMap<>();
        if (panelDan != null) data.putAll(panelDan.getExportData());
        if (panelVCHC != null) data.putAll(panelVCHC.getExportData());
        if (panelVTKT != null) data.putAll(panelVTKT.getExportData());
        data.put("<<bp_cb_vc>>", giaiDoanChuanBi != null ? giaiDoanChuanBi.trim() : "");
        data.put("<<bp_cd_vc>>", giaiDoanChienDau != null ? giaiDoanChienDau.trim() : "");
        data.put("<<bp_scd_vc>>", sauChienDau != null ? sauChienDau.trim() : "");
        return data;
    }
}
