package org.example.Panel;

/**
 * Ngữ cảnh chung cho wizard khai báo dữ liệu (dùng bởi các Step).
 * Tách interface để các Step không phụ thuộc vào lớp UI cụ thể sau refactor.
 */
public interface DataDeclarationContext {

    int getCurrentSessionId();

    void setCurrentSessionId(int sessionId);

    int getCurrentUserId();

    String getHinhThucTapBai();

    void navigateStep(int step);

    boolean saveStep1ToDatabase();

    boolean saveStep2ToDatabase();

    boolean saveStep3ToDatabase();

    boolean isCacHuongEnabled();

    /**
     * Phát tín hiệu cho các panel Kế hoạch / Dự kiến biết rằng dữ liệu khai báo đã thay đổi,
     * để chúng chỉ refresh bảng dữ liệu (không recreate panel).
     */
    void notifyPlanningPanelsDeclarationChanged();
}
