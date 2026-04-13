package org.example.Tab.PlanEstimation.Tab7_MedicalPanel;

import org.example.Utils.DBConnection;
import org.example.Utils.InputValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Tab VII — Quân y: tỷ lệ TBBB từ biên chế (Step 2) và tỷ lệ thương binh (Step 4);
 * cân đối khả năng Min/Max (cấp cứu, vận chuyển) cho xuất Word.
 */
public class Tab7_MedicalPanelService {

    public static final class TbbbFromDb {
        public int tbToanTran;
        public int bbToanTran;
        public int tbCaoNhat;
    }

    /**
     * Tính số thương/bệnh binh theo quân số và tỷ lệ Step 4.
     */
    public TbbbFromDb loadTbbbRatios(int sessionId) {
        TbbbFromDb out = new TbbbFromDb();
        if (sessionId < 1) {
            return out;
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return out;
            }
            int tongQs = 0;
            String sqlQs = "SELECT SUM(q.quan_so) AS tong_qs FROM step2_bien_che s2 JOIN quyuoc_bienche q ON s2.quyuoc_id = q.id WHERE s2.session_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlQs)) {
                pst.setInt(1, sessionId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        tongQs = rs.getInt("tong_qs");
                    }
                }
            }

            double tlTB = 0;
            double tlBB = 0;
            double tlMax = 0;
            String sqlTiLe = "SELECT loai_thuong_binh, ti_le FROM step4_ti_le_thuong_binh WHERE session_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(sqlTiLe)) {
                pst.setInt(1, sessionId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String l = rs.getString("loai_thuong_binh");
                        if (l == null) {
                            continue;
                        }
                        l = l.toLowerCase();
                        double v = rs.getDouble("ti_le");
                        if (l.contains("thương binh toàn trận")) {
                            tlTB = v;
                        } else if (l.contains("bệnh binh toàn trận")) {
                            tlBB = v;
                        } else if (l.contains("ngày cao nhất")) {
                            tlMax = v;
                        }
                    }
                }
            }
            out.tbToanTran = (int) Math.round(tongQs * tlTB / 100.0);
            out.bbToanTran = (int) Math.round(tongQs * tlBB / 100.0);
            out.tbCaoNhat = (int) Math.round(tongQs * tlMax / 100.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Đổ keyword mẫu Word cho mục "2. Cân đối" (khoảng Min ÷ Max và tổng tự tính).
     * Giá trị rỗng / không phải số → 0.
     */
    public static void putCanDoiKeywords(
            Map<String, String> data,
            String ccDMin,
            String ccDMax,
            String ccEMin,
            String ccEMax,
            String ccXaMin,
            String ccXaMax,
            String vcDMin,
            String vcDMax,
            String vcEMin,
            String vcEMax,
            String vcXaMin,
            String vcXaMax) {
        int cc_d_min = InputValidator.parseIntSafe(ccDMin);
        int cc_d_max = InputValidator.parseIntSafe(ccDMax);
        int cc_e_min = InputValidator.parseIntSafe(ccEMin);
        int cc_e_max = InputValidator.parseIntSafe(ccEMax);
        int cc_xa_min = InputValidator.parseIntSafe(ccXaMin);
        int cc_xa_max = InputValidator.parseIntSafe(ccXaMax);
        int vc_d_min = InputValidator.parseIntSafe(vcDMin);
        int vc_d_max = InputValidator.parseIntSafe(vcDMax);
        int vc_e_min = InputValidator.parseIntSafe(vcEMin);
        int vc_e_max = InputValidator.parseIntSafe(vcEMax);
        int vc_xa_min = InputValidator.parseIntSafe(vcXaMin);
        int vc_xa_max = InputValidator.parseIntSafe(vcXaMax);

        int cc_tong_min = cc_d_min + cc_e_min + cc_xa_min;
        int cc_tong_max = cc_d_max + cc_e_max + cc_xa_max;
        int vc_tong_min = vc_d_min + vc_e_min + vc_xa_min;
        int vc_tong_max = vc_d_max + vc_e_max + vc_xa_max;

        data.put("<<cc_d_min>>", String.valueOf(cc_d_min));
        data.put("<<cc_d_max>>", String.valueOf(cc_d_max));
        data.put("<<cc_e_min>>", String.valueOf(cc_e_min));
        data.put("<<cc_e_max>>", String.valueOf(cc_e_max));
        data.put("<<cc_xa_min>>", String.valueOf(cc_xa_min));
        data.put("<<cc_xa_max>>", String.valueOf(cc_xa_max));
        data.put("<<cc_tong_min>>", String.valueOf(cc_tong_min));
        data.put("<<cc_tong_max>>", String.valueOf(cc_tong_max));

        data.put("<<vc_d_min>>", String.valueOf(vc_d_min));
        data.put("<<vc_d_max>>", String.valueOf(vc_d_max));
        data.put("<<vc_e_min>>", String.valueOf(vc_e_min));
        data.put("<<vc_e_max>>", String.valueOf(vc_e_max));
        data.put("<<vc_xa_min>>", String.valueOf(vc_xa_min));
        data.put("<<vc_xa_max>>", String.valueOf(vc_xa_max));
        data.put("<<vc_tong_min>>", String.valueOf(vc_tong_min));
        data.put("<<vc_tong_max>>", String.valueOf(vc_tong_max));
    }
}
