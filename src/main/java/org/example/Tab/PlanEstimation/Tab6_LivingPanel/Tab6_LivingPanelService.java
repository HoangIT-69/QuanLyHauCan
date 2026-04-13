package org.example.Tab.PlanEstimation.Tab6_LivingPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;

/**
 * Tab VI — Sinh hoạt (ăn uống, mặc, ở ngủ) trong {@code pn_plan_estimation}.
 */
public class Tab6_LivingPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadInto(Tab6Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.anUong = r.tab6AnUong;
        target.mac = r.tab6Mac;
        target.oNguNghi = r.tab6ONguNghi;
    }

    public void save(int sessionId, String anUong, String mac, String oNguNghi) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab6AnUong = anUong != null ? anUong : "";
        r.tab6Mac = mac != null ? mac : "";
        r.tab6ONguNghi = oNguNghi != null ? oNguNghi : "";
        store.save(r);
    }

    public static final class Tab6Fields {
        public String anUong = "";
        public String mac = "";
        public String oNguNghi = "";
    }
}
