package org.example.Tab.PlanEstimation.Tab1_EvaluationPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;

/**
 * Lưu/tải nội dung Tab I — Đánh giá (bảng {@code pn_plan_estimation.danh_gia}).
 */
public class Tab1_EvaluationPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public String loadDanhGia(int sessionId) {
        return store.load(sessionId).danhGia;
    }

    public void saveDanhGia(int sessionId, String danhGia) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.danhGia = danhGia != null ? danhGia : "";
        store.save(r);
    }
}
