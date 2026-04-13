package org.example.Panel.Step4_RegulationPanel;

/**
 * Bước 4 không gọi JDBC trực tiếp; từng tab con tự load/save.
 * Lớp này giữ quy tắc nghiệp vụ chung (session hợp lệ) tách khỏi UI.
 */
public class Step4_RegulationPanelService {

    public boolean isValidSessionId(int sessionId) {
        return sessionId > 0;
    }
}
