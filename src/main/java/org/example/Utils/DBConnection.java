package org.example.Utils;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // --------------------------------------------------------
    // 1. CẤU HÌNH MYSQL (Ưu tiên số 1)
    // --------------------------------------------------------
    // Thêm connectTimeout=2000: Chỉ đợi 2 giây, nếu MySQL không phản hồi thì báo lỗi ngay
    private static final String MYSQL_URL = "jdbc:mysql://192.168.52.1:3306/db_haucan?connectTimeout=2000";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

    // --------------------------------------------------------
    // 2. CẤU HÌNH H2 OFFLINE (Dự phòng)
    // --------------------------------------------------------
    private static final String H2_URL = "jdbc:h2:file:./data/db_haucan;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    // --------------------------------------------------------
    // Biến ghi nhớ để không bị lag phần mềm
    // --------------------------------------------------------
    private static boolean isOfflineMode = false;
    private static boolean hasChecked = false; // Đánh dấu xem đã check mạng lần nào chưa

    public static Connection getConnection() {
        // Lần đầu tiên gọi DB: Thử kết nối MySQL trước
        if (!hasChecked) {
            try {
                System.out.println("Đang thử kết nối MySQL...");
                Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
                System.out.println("✅ Đã kết nối MySQL thành công!");

                hasChecked = true;
                isOfflineMode = false;
                return conn; // Trả về kết nối MySQL luôn cho lần đầu

            } catch (SQLException e) {
                System.out.println("⚠️ Không tìm thấy MySQL. Tự động chuyển sang chế độ H2 OFFLINE!");
                hasChecked = true;
                isOfflineMode = true; // Từ nay về sau phần mềm sẽ nhớ là đang offline
            }
        }

        // Từ lần thứ 2 trở đi hoặc sau khi đã biết là online hay offline
        try {
            if (!isOfflineMode) {
                // Kết nối MySQL bình thường
                return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            } else {
                // Kết nối H2 Offline
                Class.forName("org.h2.Driver");
                File dataDir = new File("./data");
                if (!dataDir.exists()) dataDir.mkdirs();
                Connection h2Conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);


                H2SchemaInitializer.initialize(h2Conn);

                return h2Conn;
            }
        } catch (Exception e) {
            // Thông báo cho UI
            JOptionPane.showMessageDialog(null, "Lỗi nghiêm trọng...");

            // Ghi lỗi thực sự ra file để debug
            try {
                java.io.FileWriter fw = new java.io.FileWriter("error_log.txt", true);
                java.io.PrintWriter pw = new java.io.PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
            } catch (Exception ex) {}
        }
        return null;
    }
}