package org.example.Tab.PlanEstimation;

/**
 * Tab XII — Kết luận & đề nghị: lưu/tải văn bản trong {@code pn_plan_estimation}.
 */
public class Tab12_ConclusionPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadInto(Tab12Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.ketLuan = r.tab12KetLuan;
        target.deNghi = r.tab12DeNghi;
    }

    public void save(int sessionId, String ketLuan, String deNghi) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab12KetLuan = ketLuan != null ? ketLuan : "";
        r.tab12DeNghi = deNghi != null ? deNghi : "";
        store.save(r);
    }

    public static final class Tab12Fields {
        public String ketLuan = "";
        public String deNghi = "";
    }
}
