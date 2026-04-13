package org.example.Form.DashboardForm;

import java.util.HashMap;
import java.util.Map;

/**
 * Logic nghiệp vụ cấp dashboard (không Swing). Hiện không có truy vấn JDBC tại lớp này;
 * các thao tác CSDL nằm trong các Panel con — có thể bổ sung method DB tại đây sau này.
 */
public class DashboardFormService {

    private static final String DEFAULT_ROLE = "USER";
    private static final String GUEST_USERNAME = "Guest";

    public String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return DEFAULT_ROLE;
        }
        return role.trim().toUpperCase();
    }

    public String resolveDisplayUsername(String username) {
        if (username == null || username.isBlank()) {
            return GUEST_USERNAME;
        }
        return username.trim();
    }

    public boolean isAdminRole(String normalizedRole) {
        return "ADMIN".equals(normalizedRole);
    }

    /**
     * Gói ba trường sinh hoạt từ "Dự kiến" sang placeholder map dùng cho {@code PN_AssurancePlanPanelUI}.
     */
    public Map<String, String> buildLivingDataForAssurance(String anUong, String mac, String oNguNghi) {
        Map<String, String> m = new HashMap<>();
        m.put("<<bd_an_uong>>", nullToEmpty(anUong));
        m.put("<<bd_mac>>", nullToEmpty(mac));
        m.put("<<bd_o_ngunghi>>", nullToEmpty(oNguNghi));
        return m;
    }

    /**
     * Bản sao an toàn (tránh null) cho map từ tab bảo vệ / chỉ huy.
     */
    public Map<String, String> copyStringMap(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return new HashMap<>();
        }
        return new HashMap<>(source);
    }

    public Map<String, String> emptyStringMap() {
        return new HashMap<>();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
