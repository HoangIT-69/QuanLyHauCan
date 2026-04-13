package org.example.Tab.PlanEstimation.Tab2_MissionPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;

/**
 * Lưu/tải nội dung Tab II — Nhiệm vụ (bảng {@code pn_plan_estimation.nhiem_vu}).
 */
public class Tab2_MissionPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public String loadNhiemVu(int sessionId) {
        return store.load(sessionId).nhiemVu;
    }

    public void saveNhiemVu(int sessionId, String nhiemVu) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.nhiemVu = nhiemVu != null ? nhiemVu : "";
        store.save(r);
    }
}
