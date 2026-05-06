package org.example.Tab.PlanEstimation;

import org.example.Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Tab XI — Chỉ huy HC-KT: văn bản {@code pn_plan_estimation}; chỉ huy/thay thế mặc định từ {@code step1_thong_tin} khi trống.
 */
public class Tab11_CommandPanelService {

    private final PnPlanEstimationTextStore store = new PnPlanEstimationTextStore();

    public void loadInto(Tab11Fields target, int sessionId) {
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        target.trienKhai = r.tab11TrienKhai;
        target.ttChuanBi = r.tab11TtCb;
        target.ttChienDau = r.tab11TtCd;
        target.baoCaoChuanBi = r.tab11BcCb;
        target.baoCaoChienDau1 = r.tab11BcCd1;
        target.baoCaoChienDau2 = r.tab11BcCd2;

        String ch = r.tab11ChiHuy;
        String th = r.tab11NguoiThayThe;
        if (ch == null || ch.isEmpty() || th == null || th.isEmpty()) {
            Step1ChiHuy s1 = fetchChiHuyFromStep1(sessionId);
            if (ch == null || ch.isEmpty()) {
                ch = s1.chiHuy;
            }
            if (th == null || th.isEmpty()) {
                th = s1.nguoiThayThe;
            }
        }
        target.chiHuy = ch != null ? ch : "";
        target.nguoiThayThe = th != null ? th : "";
    }

    public void save(int sessionId, Tab11Fields f) {
        if (f == null) {
            return;
        }
        PnPlanEstimationTextStore.PlanEstimationRow r = store.load(sessionId);
        r.tab11TrienKhai = nz(f.trienKhai);
        r.tab11ChiHuy = nz(f.chiHuy);
        r.tab11NguoiThayThe = nz(f.nguoiThayThe);
        r.tab11TtCb = nz(f.ttChuanBi);
        r.tab11TtCd = nz(f.ttChienDau);
        r.tab11BcCb = nz(f.baoCaoChuanBi);
        r.tab11BcCd1 = nz(f.baoCaoChienDau1);
        r.tab11BcCd2 = nz(f.baoCaoChienDau2);
        store.save(r);
    }

    public Tab11Fields fetchOnlyCommanderFieldsFromStep1(int sessionId) {
        Tab11Fields out = new Tab11Fields();
        Step1ChiHuy s1 = fetchChiHuyFromStep1(sessionId);
        out.chiHuy = s1.chiHuy;
        out.nguoiThayThe = s1.nguoiThayThe;
        return out;
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    private static final class Step1ChiHuy {
        String chiHuy = "";
        String nguoiThayThe = "";
    }

    private Step1ChiHuy fetchChiHuyFromStep1(int sessionId) {
        Step1ChiHuy out = new Step1ChiHuy();
        if (sessionId < 1) {
            return out;
        }
        String sql = "SELECT chi_huy, nguoi_thay_the FROM step1_thong_tin WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, sessionId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        out.chiHuy = nz(rs.getString("chi_huy"));
                        out.nguoiThayThe = nz(rs.getString("nguoi_thay_the"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static final class Tab11Fields {
        public String trienKhai = "";
        public String chiHuy = "";
        public String nguoiThayThe = "";
        public String ttChuanBi = "";
        public String ttChienDau = "";
        public String baoCaoChuanBi = "";
        public String baoCaoChienDau1 = "";
        public String baoCaoChienDau2 = "";
    }
}
