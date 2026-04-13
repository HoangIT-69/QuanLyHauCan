package org.example.Tab.PlanEstimation.Tab4_EquipmentPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;

/**
 * Tab IV — Trang bị kỹ thuật (3 ô văn bản trong {@code pn_plan_estimation}).
 */
public class Tab4_EquipmentPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadInto(Tab4Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.chiTieu = r.tab4ChiTieu;
        target.chuanBi = r.tab4ChuanBi;
        target.chienDau = r.tab4ChienDau;
    }

    public void save(int sessionId, String chiTieu, String chuanBi, String chienDau) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab4ChiTieu = chiTieu != null ? chiTieu : "";
        r.tab4ChuanBi = chuanBi != null ? chuanBi : "";
        r.tab4ChienDau = chienDau != null ? chienDau : "";
        store.save(r);
    }

    public static final class Tab4Fields {
        public String chiTieu = "";
        public String chuanBi = "";
        public String chienDau = "";
    }
}
