package org.example.Tab.PlanEstimation;

/**
 * Tab X — Bảo vệ HC-KT: lưu/tải văn bản trong {@code pn_plan_estimation}.
 */
public class Tab10_ProtectionPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadInto(Tab10Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.tinhHuong = r.tab10TinhHuong;
        target.bienPhap = r.tab10BienPhap;
    }

    public void save(int sessionId, String tinhHuong, String bienPhap) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab10TinhHuong = tinhHuong != null ? tinhHuong : "";
        r.tab10BienPhap = bienPhap != null ? bienPhap : "";
        store.save(r);
    }

    public static final class Tab10Fields {
        public String tinhHuong = "";
        public String bienPhap = "";
    }
}
