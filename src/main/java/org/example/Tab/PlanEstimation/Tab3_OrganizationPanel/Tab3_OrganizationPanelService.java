package org.example.Tab.PlanEstimation.Tab3_OrganizationPanel;

import org.example.Tab.PlanEstimation.PnPlanEstimationTextStore;

/**
 * Tab III — Tổ chức / bố trí (cột {@code to_chuc}, {@code bo_tri} trong {@code pn_plan_estimation}).
 */
public class Tab3_OrganizationPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public String loadToChuc(int sessionId) {
        return store.load(sessionId).toChuc;
    }

    public String loadBoTri(int sessionId) {
        return store.load(sessionId).boTri;
    }

    public void saveToChuc(int sessionId, String toChuc) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.toChuc = toChuc != null ? toChuc : "";
        store.save(r);
    }

    public void saveBoTri(int sessionId, String boTri) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.boTri = boTri != null ? boTri : "";
        store.save(r);
    }

    public void saveBoth(int sessionId, String toChuc, String boTri) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.toChuc = toChuc != null ? toChuc : "";
        r.boTri = boTri != null ? boTri : "";
        store.save(r);
    }
}
