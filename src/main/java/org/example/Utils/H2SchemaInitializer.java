package org.example.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class H2SchemaInitializer {

    // Cờ đánh dấu để chỉ chạy lệnh kiểm tra/tạo bảng 1 lần duy nhất khi khởi động phần mềm
    private static boolean isInitialized = false;

    public static void initialize(Connection conn) {
        if (isInitialized || conn == null) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            // 1. Bảng quyuoc_bienche
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `quyuoc_bienche` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `nhom_don_vi` VARCHAR(255)," +
                            "  `ten_don_vi` VARCHAR(255)," +
                            "  `quan_so` INT," +
                            "  `luu_dan` INT," +
                            "  `sung_ngan` INT," +
                            "  `tieu_lien` INT," +
                            "  `trung_lien` INT," +
                            "  `dai_lien` INT," +
                            "  `b41` INT," +
                            "  `co60mm` INT," +
                            "  `co82mm` INT," +
                            "  `co100mm` INT," +
                            "  `spg9` INT," +
                            "  `smpk_127mm` INT" +
                            ");"
            );

            // 2. Bảng quyuoc_dan
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `quyuoc_dan` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `danh_muc` VARCHAR(255)," +
                            "  `loai_dan` VARCHAR(255)," +
                            "  `so_vien_tren_coso` INT," +
                            "  `trong_luong_1_vien` FLOAT," +
                            "  `don_vi_tinh` VARCHAR(50)" +
                            ");"
            );

            // 3. Bảng quyuoc_vchc
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `quyuoc_vchc` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `danh_muc` VARCHAR(255)," +
                            "  `ten_vat_chat` VARCHAR(255)," +
                            "  `quy_uoc` FLOAT," +
                            "  `don_vi_quy_uoc` VARCHAR(50)," +
                            "  `don_vi_tinh` VARCHAR(50)" +
                            ");"
            );

            // 4. Bảng step2_bien_che
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step2_bien_che` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `huong` VARCHAR(255)," +
                            "  `quyuoc_id` INT," +
                            "  `phan_loai` TINYINT" +
                            ");"
            );

            // 5. Bảng sessions
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `sessions` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `user_id` INT," +
                            "  `ten_bai_tap` VARCHAR(255)," +
                            "  `ngay_tao` DATETIME," +
                            "  `trang_thai` TINYINT," +
                            "  `hinh_thuc_tap_bai` VARCHAR(255)" +
                            ");"
            );
            try {
                stmt.executeUpdate("ALTER TABLE sessions ADD COLUMN hinh_thuc_tap_bai VARCHAR(255)");
            } catch (SQLException ignored) {
            }

            // 6. Bảng step1_thong_tin
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step1_thong_tin` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `ten_van_kien` VARCHAR(255)," +
                            "  `vi_tri_chi_huy` VARCHAR(255)," +
                            "  `thoi_gian` VARCHAR(255)," +
                            "  `map_1` VARCHAR(255)," +
                            "  `map_2` VARCHAR(255)," +
                            "  `map_3` VARCHAR(255)," +
                            "  `map_4` VARCHAR(255)," +
                            "  `ty_le` VARCHAR(50)," +
                            "  `nam` INT," +
                            "  `chi_huy` VARCHAR(255)," +
                            "  `nguoi_thay_the` VARCHAR(255)," +
                            "  `don_vi_chu_quan` VARCHAR(255)," +
                            "  `don_vi_lap` VARCHAR(255)," +
                            "  `so_ke_hoach` VARCHAR(255)," +
                            "  `chuc_vu_phe_chuan` VARCHAR(255)," +
                            "  `mat_do` VARCHAR(255)" +
                            ");"
            );
            String[] step1Cols = {
                "don_vi_chu_quan VARCHAR(255)", "don_vi_lap VARCHAR(255)", "so_ke_hoach VARCHAR(255)", 
                "chuc_vu_phe_chuan VARCHAR(255)", "mat_do VARCHAR(255)"
            };
            for (String col : step1Cols) {
                try {
                    stmt.executeUpdate("ALTER TABLE step1_thong_tin ADD COLUMN " + col);
                } catch (SQLException ignored) {}
            }
            // Dọn duplicate rows, chỉ giữ lại row mới nhất theo id
            try {
                stmt.executeUpdate(
                    "DELETE FROM step1_thong_tin WHERE id NOT IN (" +
                    "  SELECT max_id FROM (SELECT MAX(id) AS max_id FROM step1_thong_tin GROUP BY session_id) AS t" +
                    ")"
                );
            } catch (SQLException ignored) {}
            // Đảm bảo session_id là UNIQUE
            try {
                stmt.executeUpdate("ALTER TABLE step1_thong_tin ADD CONSTRAINT uq_step1_session UNIQUE (session_id)");
            } catch (SQLException ignored) {}

            // 7. Bảng step3_vat_chat (phân cấp theo tên cột nghiệp vụ; giữ cột legacy để migrate)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step3_vat_chat` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `loai_vat_chat` TINYINT," +
                            "  `vat_chat` VARCHAR(255)," +
                            "  `dvt` VARCHAR(50)," +
                            "  `kho_d` FLOAT," +
                            "  `don_vi` FLOAT," +
                            "  `huong_cy` FLOAT," +
                            "  `huong_ty_1` FLOAT," +
                            "  `huong_ty_2` FLOAT," +
                            "  `ll_cd_vong_ngoai` FLOAT," +
                            "  `ll_db_co_dong` FLOAT," +
                            "  `db_bcht` FLOAT," +
                            "  `ll_con_lai` FLOAT," +
                            "  `ll_cd_tao_the` FLOAT," +
                            "  `phoi_thuoc` FLOAT," +
                            "  `huong_ty` FLOAT," +
                            "  `pn_sau` FLOAT," +
                            "  `phan_cap_json` TEXT," +
                            "  `ghi_chu` TEXT" +
                            ");"
            );

            migrateStep3VatChatColumns(stmt);

            // 7b. Bảng pn_plan_estimation — nội dung Tab I/II (Dự kiến kế hoạch Phòng ngự)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `pn_plan_estimation` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT NOT NULL UNIQUE," +
                            "  `danh_gia` TEXT," +
                            "  `nhiem_vu` TEXT," +
                            "  `to_chuc` TEXT," +
                            "  `bo_tri` TEXT," +
                            "  `tab4_chi_tieu` TEXT," +
                            "  `tab4_chuan_bi` TEXT," +
                            "  `tab4_chien_dau` TEXT," +
                            "  `tab5_txt_chuan_bi` TEXT," +
                            "  `tab5_txt_chien_dau` TEXT," +
                            "  `tab5_txt_sau_cd` TEXT," +
                            "  `tab6_an_uong` TEXT," +
                            "  `tab6_mac` TEXT," +
                            "  `tab6_o_ngu_nghi` TEXT" +
                            ");"
            );
            String[] pnCols = {
                    "to_chuc TEXT", "bo_tri TEXT",
                    "tab4_chi_tieu TEXT", "tab4_chuan_bi TEXT", "tab4_chien_dau TEXT",
                    "tab5_txt_chuan_bi TEXT", "tab5_txt_chien_dau TEXT", "tab5_txt_sau_cd TEXT",
                    "tab6_an_uong TEXT", "tab6_mac TEXT", "tab6_o_ngu_nghi TEXT"
            };
            for (String c : pnCols) {
                try {
                    stmt.executeUpdate("ALTER TABLE pn_plan_estimation ADD COLUMN " + c);
                } catch (SQLException ignored) {
                }
            }
            String[] pnTab10_12 = {
                    "tab10_tinh_huong TEXT", "tab10_bien_phap TEXT",
                    "tab11_trien_khai TEXT", "tab11_chi_huy TEXT", "tab11_nguoi_thay_the TEXT",
                    "tab11_tt_cb TEXT", "tab11_tt_cd TEXT",
                    "tab11_bc_cb TEXT", "tab11_bc_cd1 TEXT", "tab11_bc_cd2 TEXT",
                    "tab12_ket_luan TEXT", "tab12_de_nghi TEXT"
            };
            for (String c : pnTab10_12) {
                try {
                    stmt.executeUpdate("ALTER TABLE pn_plan_estimation ADD COLUMN " + c);
                } catch (SQLException ignored) {
                }
            }

            // 8. Bảng step4_hu_hong_vktb
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step4_hu_hong_vktb` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `loai_vktb` VARCHAR(255)," +
                            "  `so_luong_tham_gia` INT," +
                            "  `ti_le_hu_hong` FLOAT" +
                            ");"
            );

            // 9. Bảng step4_quy_dinh_du_tru
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step4_quy_dinh_du_tru` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `loai_vat_chat` TINYINT," +
                            "  `vat_chat` VARCHAR(255)," +
                            "  `dvt` VARCHAR(50)," +
                            "  `du_tru` FLOAT," +
                            "  `phai_co_0400` FLOAT," +
                            "  `phai_co_scd` FLOAT," +
                            "  `tieu_thu_gdcb` FLOAT," +
                            "  `tieu_thu_gdcd` FLOAT," +
                            "  `dt_chitiet` TEXT," +
                            "  `pc04_chitiet` TEXT," +
                            "  `scd_chitiet` TEXT," +
                            "  `gdcb_chitiet` TEXT," +
                            "  `gdcd_chitiet` TEXT" +
                            ");"
            );

            // 10. Bảng step4_ti_le_thuong_binh
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `step4_ti_le_thuong_binh` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `session_id` INT," +
                            "  `loai_thuong_binh` VARCHAR(255)," +
                            "  `ti_le` FLOAT" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                            "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                            "  `username` VARCHAR(255)," +
                            "  `password` VARCHAR(255)," +
                            "  `ma_quan_nhan` VARCHAR(100)," +
                            "  `full_name` VARCHAR(255)," +
                            "  `dob` VARCHAR(100)," +     // Ngày sinh (để VARCHAR cho an toàn giống các bảng trước)
                            "  `rank` VARCHAR(100)," +    // Bắt buộc phải có dấu ` ` vì rank là từ khóa hệ thống
                            "  `chuc_vu` VARCHAR(255)," +
                            "  `don_vi` VARCHAR(255)," +
                            "  `role` VARCHAR(50)" +      // Quyền (Admin, User...)
                            ");"
            );
            stmt.executeUpdate(
                    "INSERT INTO `users` (`username`, `password`, `full_name`, `rank`, `role`) " +
                            "SELECT 'admin', '1', 'Quản trị viên', 'Sĩ quan', 'admin' " +
                            "WHERE NOT EXISTS (SELECT 1 FROM `users` WHERE `username` = 'admin');"
            );
            seedDefaultConventions(stmt);

            isInitialized = true; // Đánh dấu là đã tạo xong để lần sau không chạy lại nữa

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * DB cũ có thể thiếu cột phân cấp mới — ALTER + copy từ cột legacy (một lần khi cột mới còn NULL).
     */
    private static void migrateStep3VatChatColumns(Statement stmt) {
        String[] addCols = {
                "huong_ty_1 FLOAT",
                "huong_ty_2 FLOAT",
                "ll_cd_vong_ngoai FLOAT",
                "ll_db_co_dong FLOAT",
                "db_bcht FLOAT",
                "ll_cd_tao_the FLOAT"
        };
        for (String c : addCols) {
            try {
                stmt.executeUpdate("ALTER TABLE step3_vat_chat ADD COLUMN " + c);
            } catch (SQLException ignored) {
            }
        }
        try {
            stmt.executeUpdate("ALTER TABLE step3_vat_chat ADD COLUMN phan_cap_json TEXT");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("UPDATE step3_vat_chat SET huong_ty_1 = huong_ty WHERE huong_ty_1 IS NULL");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("UPDATE step3_vat_chat SET ll_cd_vong_ngoai = phoi_thuoc WHERE ll_cd_vong_ngoai IS NULL");
        } catch (SQLException ignored) {
        }
        try {
            stmt.executeUpdate("UPDATE step3_vat_chat SET ll_db_co_dong = pn_sau WHERE ll_db_co_dong IS NULL");
        } catch (SQLException ignored) {
        }
    }

    /**
     * Seed dữ liệu quy ước mặc định (idempotent) để hệ thống có dữ liệu khởi tạo.
     */
    private static void seedDefaultConventions(Statement stmt) throws SQLException {
        // quyuoc_bienche (đủ nhóm: Tiểu đoàn / Trung đoàn / Sư đoàn)
        stmt.executeUpdate("INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) " +
                "SELECT 'Tiểu đoàn', 'Tiểu đoàn bộ binh', 500, 120, 80, 60, 24, 12, 18, 6, 6, 0, 0, 0 " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_bienche WHERE ten_don_vi = 'Tiểu đoàn bộ binh')");
        stmt.executeUpdate("INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) " +
                "SELECT 'Sư đoàn', 'Đại đội hỏa lực', 150, 30, 10, 6, 3, 1, 4, 4, 2, 0, 0, 0 " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_bienche WHERE ten_don_vi = 'Đại đội hỏa lực')");
        stmt.executeUpdate("INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) " +
                "SELECT 'Trung đoàn', 'Trung đoàn bộ binh', 300, 50, 20, 10, 4, 2, 4, 2, 2, 0, 0, 0 " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_bienche WHERE ten_don_vi = 'Trung đoàn bộ binh')");
        stmt.executeUpdate("INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) " +
                "SELECT 'Tiểu đoàn', 'Tiểu đoàn pháo binh', 420, 80, 20, 8, 4, 2, 6, 8, 6, 4, 2, 2 " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_bienche WHERE ten_don_vi = 'Tiểu đoàn pháo binh')");
        stmt.executeUpdate("INSERT INTO quyuoc_bienche (nhom_don_vi, ten_don_vi, quan_so, luu_dan, sung_ngan, tieu_lien, trung_lien, dai_lien, b41, co60mm, co82mm, co100mm, spg9, smpk_127mm) " +
                "SELECT 'Tiểu đoàn', 'Đơn vị bảo đảm', 90, 10, 10, 4, 2, 0, 0, 0, 0, 0, 0, 0 " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_bienche WHERE ten_don_vi = 'Đơn vị bảo đảm')");

        // quyuoc_dan (đủ danh mục)
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn phòng không', 'Đạn 12.7mm', 300, 0.115, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn 12.7mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn chống tăng', 'Đạn B41', 10, 2.40, 'Quả' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn B41')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn BB nhóm I', 'Đạn 7.62mm', 300, 0.010, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn 7.62mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn BB nhóm II', 'Đạn 5.56mm', 360, 0.004, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn 5.56mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Mìn, Lựu đạn', 'Lựu đạn mỏ vịt', 2, 0.450, 'Quả' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Lựu đạn mỏ vịt')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn cối', 'Đạn Cối 60mm', 20, 1.360, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn Cối 60mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn cối', 'Đạn Cối 82mm', 15, 3.080, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn Cối 82mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn cối', 'Đạn Cối 100mm', 10, 8.000, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn Cối 100mm')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn chống tăng', 'Đạn SPG-9', 6, 2.700, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn SPG-9')");
        stmt.executeUpdate("INSERT INTO quyuoc_dan (danh_muc, loai_dan, so_vien_tren_coso, trong_luong_1_vien, don_vi_tinh) " +
                "SELECT 'Đạn BB nhóm I', 'Đạn 9mm', 50, 0.012, 'Viên' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_dan WHERE loai_dan = 'Đạn 9mm')");

        // quyuoc_vchc (đủ danh mục + item đặc biệt để test nghiệp vụ)
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân lương', 'Gạo tẻ', 1.5, 'Kg/ngày', 'Ngày' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Gạo tẻ')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân lương', 'Thịt hộp', 0.25, 'Kg/ngày', 'Ngày' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Thịt hộp')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân lương', 'Đường sữa thương binh', 0.02, 'Kg/người/ngày', '%QS' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Đường sữa thương binh')");

        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân trang', 'Áo mưa', 1, 'Cái/người', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Áo mưa')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân trang', 'Giày vải', 1, 'Cái/người', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Giày vải')");

        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân y', 'Túi y sĩ', 1, 'Túi/đơn vị', 'Túi' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Túi y sĩ')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân y', 'Túi y tá', 1, 'Túi/đơn vị', 'Túi' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Túi y tá')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân y', 'Túi cứu thương', 1, 'Túi/đơn vị', 'Túi' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Túi cứu thương')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Quân y', 'Thuốc sát trùng', 0.03, 'Lít/người', '%QS' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Thuốc sát trùng')");

        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Doanh trại', 'Dầu thắp', 0.2, 'Lít/ngày', 'Ngày' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Dầu thắp')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Doanh trại', 'Bạt dã chiến', 0.015, 'Cái/người', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Bạt dã chiến')");

        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'VTKT', 'Mỡ bảo quản súng', 0.05, 'Kg/khẩu', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Mỡ bảo quản súng')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'VTKT', 'Dầu súng AK', 0.03, 'Lít/khẩu', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Dầu súng AK')");
        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'VTKT', 'Giẻ lau súng', 0.2, 'Kg/100 khẩu', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Giẻ lau súng')");

        stmt.executeUpdate("INSERT INTO quyuoc_vchc (danh_muc, ten_vat_chat, quy_uoc, don_vi_quy_uoc, don_vi_tinh) " +
                "SELECT 'Khác', 'Bao tải dã chiến', 0.1, 'Cái/người', 'Cái' " +
                "WHERE NOT EXISTS (SELECT 1 FROM quyuoc_vchc WHERE ten_vat_chat = 'Bao tải dã chiến')");
    }
}