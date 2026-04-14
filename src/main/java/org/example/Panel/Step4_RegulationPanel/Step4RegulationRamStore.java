package org.example.Panel.Step4_RegulationPanel;

import org.example.Tab.Step4_Regulation.CasualtyRegulationTab.CasualtyRegulationTabService;
import org.example.Tab.Step4_Regulation.DamageRegulationTab.DamageRegulationTabService;
import org.example.Tab.Step4_Regulation.MaterialRegulationTab.MaterialRegulationTabService;

import java.util.ArrayList;
import java.util.List;

/**
 * Bản nháp Step 4 khi chưa có {@code session_id} (khai báo mới, chưa mở từ lịch sử).
 * Khi {@code session_id > 0} (tiếp tục từ {@link org.example.Panel.SessionHistoryPanel.SessionHistoryPanelUI})
 * các tab chỉ đọc/ghi DB, không dùng store này.
 */
public final class Step4RegulationRamStore {

    private static List<MaterialRegulationTabService.SaveRow> materialDraft;
    private static List<CasualtyRegulationTabService.Row> casualtyDraft;
    private static List<DamageRegulationTabService.SaveRow> damageDraft;

    private Step4RegulationRamStore() {
    }

    public static void clear() {
        materialDraft = null;
        casualtyDraft = null;
        damageDraft = null;
    }

    public static List<MaterialRegulationTabService.SaveRow> getMaterialDraft() {
        return materialDraft;
    }

    public static void setMaterialDraft(List<MaterialRegulationTabService.SaveRow> rows) {
        materialDraft = rows != null ? new ArrayList<>(rows) : null;
    }

    public static List<CasualtyRegulationTabService.Row> getCasualtyDraft() {
        return casualtyDraft;
    }

    public static void setCasualtyDraft(List<CasualtyRegulationTabService.Row> rows) {
        casualtyDraft = rows != null ? new ArrayList<>(rows) : null;
    }

    public static List<DamageRegulationTabService.SaveRow> getDamageDraft() {
        return damageDraft;
    }

    public static void setDamageDraft(List<DamageRegulationTabService.SaveRow> rows) {
        damageDraft = rows != null ? new ArrayList<>(rows) : null;
    }
}
