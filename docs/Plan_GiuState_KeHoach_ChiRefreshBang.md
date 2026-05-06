# Kế hoạch: Giữ state Kế hoạch, chỉ refresh bảng dữ liệu

## 1. Mục tiêu

- Không làm mất dữ liệu người dùng đã nhập ở màn hình **Dự kiến kế hoạch** và **Kế hoạch bảo đảm** khi chuyển qua menu khác rồi quay lại.
- Chỉ cập nhật lại các bảng dữ liệu phụ thuộc dữ liệu khai báo (ví dụ thêm vật chất ở phần Khai báo dữ liệu), không recreate toàn bộ panel.
- Chỉ refresh dữ liệu trong `JTable` (table model), tuyệt đối không refresh `JComboBox`, không `remove/add` lại panel.
- Giữ UX mượt: bấm menu quay lại nhanh, không giật/đơ do load lại cả cây UI.

## 2. Hiện trạng và nguyên nhân

### 2.1. Dấu hiệu gây mất dữ liệu

- Dashboard đang remove/add lại card Kế hoạch trong `installKeHoachContent(...)`.
- Khi mở menu Kế hoạch (`openKeHoachCard()`), code tạo `PN_AssurancePlanPanelUI` mới.
- Với luồng sau khi tạo session mới (`syncPlanPanelsAfterWizardSessionCreated(...)`), card Kế hoạch cũng bị remove/add lại.

Hệ quả:
- Toàn bộ state nhập tay chưa lưu trong các tab Kế hoạch bị reset.
- Mỗi lần quay lại menu Kế hoạch giống như mở panel mới.

### 2.2. Điểm tích cực có thể tận dụng

Nhiều tab đã có hàm load theo session để refresh dữ liệu bảng:
- `Tab4_EquipPlanPanelUI.loadDataFromDatabase(sessionId)`
- `Tab5_MaterialPanelUI.loadSessionData(sessionId)`
- `Tab7_MedPlanPanelUI.loadDataFromDatabase(sessionId)`
- `Tab8_MaintPlanPanelUI.loadDataFromDatabase(sessionId)`
- `Tab9_TransportPanel.refreshData()`

=> Có thể chuyển sang chiến lược **giữ nguyên panel instance + refresh có chọn lọc**.

---

## 2.3. Chi tiết phần cần refresh trong `PN_PlanEstimationPanelUI` (Dự kiến kế hoạch)

> Đây là danh sách CỤ THỂ các tab và phần dữ liệu cần được refresh khi dữ liệu khai báo thay đổi.
> **Tuyệt đối không refresh JTextArea, JTextField nhập tay** — chỉ refresh các phần tự động tính hoặc lấy từ dữ liệu khai báo.

### Tab V — Bảo đảm đạn, vật chất hậu cần, vật tư kỹ thuật (`Tab5_MaterialPanelUI`)

| Phần | Loại | API refresh hiện có | Ghi chú |
|------|------|---------------------|---------|
| 1. Dự kiến khối lượng đạn, vật chất HC, vật tư KT | `JTable` (model1, 5 cột) | `refreshTable1()` | Phụ thuộc dữ liệu khai báo vật chất |
| 2. Dự trữ vật chất hậu cần chi tiết | `JTable` (model2, 9 cột) | `refreshTable2()` | Phụ thuộc Step 4 VCHC khai báo |
| 3. Ý định tổ chức tiếp nhận, bổ sung | `JTextArea` x3 | ❌ Không refresh | Dữ liệu nhập tay, giữ nguyên |

**Gọi:** `tab5.refreshTable1(); tab5.refreshTable2();`

---

### Tab VII — Bảo đảm quân y (`Tab7_MedicalPanelUI`)

| Phần | Loại | API refresh hiện có | Ghi chú |
|------|------|---------------------|---------|
| 1. Dự kiến tỷ lệ TBBB (tự động tính) | `JTextField` auto-calc (txtTBToanTran, txtBBToanTran, txtTBCaoNhat) | `applyTbbbFromDb()` — hiện là **private** | Cần expose thành `public` |
| 2. Cân đối (khả năng cấp cứu, vận chuyển) | `JTextField` nhập tay + `JLabel` tính tổng | ❌ Không refresh | Nhập tay, tự động tính lại khi user sửa |
| 3. Ý định bảo đảm | `JTextArea` x2 | ❌ Không refresh | Nhập tay |
| 4. Bảo đảm vệ sinh phòng bệnh | `JTextArea` | ❌ Không refresh | Nhập tay |

**Cần làm:** Đổi `applyTbbbFromDb()` từ `private` → `public` rồi gọi `tab7.applyTbbbFromDb();`

---

### Tab VIII — Bảo dưỡng, sửa chữa (`Tab8_MaintenancePanelUI`)

| Phần | Loại | API refresh hiện có | Ghi chú |
|------|------|---------------------|---------|
| 2a. Dự kiến tỷ lệ vũ khí trang bị hư hỏng | `JTable` (5 cột: STT, Loại vũ khí, Số lượng, Tỉ lệ HH, Số HH) | `loadDataFromDatabase(sessionId)` — đã **public** | Phụ thuộc dữ liệu vũ khí khai báo |
| 1. Bảo dưỡng kỹ thuật | `JTextArea` x3 | ❌ Không refresh | Nhập tay |
| 2b. Ý định bảo đảm | `JTextArea` x3 | ❌ Không refresh | Nhập tay |
| Cân đối | `JTextArea` | ❌ Không refresh | Nhập tay |

**Gọi:** `tab8.loadDataFromDatabase(sessionId);`

---

### Tab IX — Công tác vận tải (`Tab9_TransportPanelUI`)

| Phần | Loại | API refresh hiện có | Ghi chú |
|------|------|---------------------|---------|
| 2. Dự tính khối lượng vận chuyển | `JLabel` tự động tính (lblKlGdcb, lblKlGdcd, lblKlToanTran) | `componentShown` tự reload snapshot | Phụ thuộc dữ liệu vật chất khai báo |
| 1. Đường vận tải | `JTextArea` | ❌ Không refresh | Nhập tay |
| 3. Cân đối khả năng | `JTextField` nhập tay + `JLabel` tính tổng | ❌ Không refresh | Nhập tay |
| 4. Ý định vận chuyển | `JTextArea` x3 | ❌ Không refresh | Nhập tay |

**Cần làm:** Thêm method `public void reloadSnapshot()` gọi lại `snapshot = service.loadTransportSnapshot(sessionId); applySnapshotLabels(); recalcTransport();`

---

### Tab IV — Trang bị kỹ thuật (`Tab4_EquipmentPanelUI`)

| Phần | Loại | Cần refresh? | Ghi chú |
|------|------|--------------|---------|
| 1. Chỉ tiêu | `JTextArea` (auto-gen hoặc nhập tay) | ❌ Không refresh | Auto-gen chỉ khi tạo lần đầu |
| 2. Tiếp nhận, chuẩn bị | `JTextArea` | ❌ Không refresh | Nhập tay |
| 3. Tiếp nhận trong chiến đấu | `JTextArea` | ❌ Không refresh | Nhập tay |

**Kết luận: Tab IV không có JTable, không cần refresh.**

---

### Tab XI — Chỉ huy hậu cần – kỹ thuật (`Tab11_CommandPanelUI`)

| Phần | Loại | Cần refresh? | Ghi chú |
|------|------|--------------|---------|
| Tất cả nội dung | `JTextArea` / `JTextField` nhập tay | ❌ Không refresh | Toàn bộ nhập tay |

**Kết luận: Tab XI không phụ thuộc dữ liệu khai báo, không cần refresh.**

---

### Tổng hợp: Danh sách gọi trong `refreshDataTablesOnly()` của `PN_PlanEstimationPanelUI`

```java
public void refreshDataTablesOnly() {
    // Tab V — bảng vật chất
    tab5.refreshTable1();
    tab5.refreshTable2();

    // Tab VII — dự kiến TBBB (cần đổi applyTbbbFromDb() thành public)
    tab7.applyTbbbFromDb();

    // Tab VIII — bảng vũ khí hư hỏng
    tab8.loadDataFromDatabase(sessionId);

    // Tab IX — nhãn khối lượng vận chuyển tự động tính
    tab9.reloadSnapshot(); // cần thêm method public này vào Tab9_TransportPanelUI
}
```

## 3. Thiết kế đề xuất

## 3.1. Nguyên tắc

- Card `KeHoach` và `DuKien` chỉ khởi tạo 1 lần cho mỗi `sessionId`.
- Chuyển menu chỉ `cardLayout.show(...)`, không remove/add card nếu `sessionId` không đổi.
- Khi dữ liệu khai báo thay đổi, phát tín hiệu `dataChanged` để tab dữ liệu tự reload bảng.
- Các tab text/ý định nhập tay (đánh giá, nhiệm vụ, chỉ huy, bảo vệ...) không tự reset.

### Ràng buộc cứng (bắt buộc)

- Chỉ cho phép thao tác refresh ở mức dữ liệu bảng (`JTable` / `TableModel`).
- Không gọi `remove(...)`, `add(...)`, `revalidate()`, `repaint()` ở cấp panel để xử lý cập nhật dữ liệu khai báo.
- Không reset, không nạp lại, không đổi model của `JComboBox` khi nhận tín hiệu cập nhật từ Khai báo dữ liệu.
- Không khởi tạo lại instance `PN_PlanEstimationPanelUI` hoặc `PN_AssurancePlanPanelUI` chỉ vì dữ liệu khai báo thay đổi.

## 3.2. API mới cần bổ sung

### A. Tại `PN_AssurancePlanPanelUI`

Thêm các API:
- `public int getSessionId()`
- `public void onDeclarationDataChanged()`
- `public void refreshDataTablesOnly()`

`refreshDataTablesOnly()` chỉ gọi refresh cho tab bảng:
- tab4, tab5, tab7, tab8, tab9

Không đụng vào dữ liệu text đã nhập ở tab1, tab2, tab6, tab10, tab11.

### B. Tại `PN_PlanEstimationPanelUI`

Thêm API tương tự:
- `public int getSessionId()`
- `public void onDeclarationDataChanged()`
- `public void refreshDataTablesOnly()`

Trong `refreshDataTablesOnly()`, ưu tiên refresh các tab có dữ liệu phụ thuộc khai báo (đặc biệt tab4, tab5, tab7, tab8, tab9 nếu có API load).

### C. Tại `DashboardFormUI`

Thêm cache instance:
- `private PN_PlanEstimationPanelUI cachedDuKienPanel;`
- `private PN_AssurancePlanPanelUI cachedKeHoachPanel;`
- `private int keHoachPanelSessionId = -1;`
- `private int duKienPanelSessionId = -1;`

Thêm hàm điều phối:
- `ensureDuKienPanelForSession(int sessionId)`
- `ensureKeHoachPanelForSession(int sessionId)`
- `notifyPlanningPanelsDeclarationChanged()`

## 4. Kế hoạch sửa theo bước

### Bước 1: Chặn recreate panel khi chỉ chuyển menu

File: `DashboardFormUI`

- Sửa `openKeHoachCard()`:
  - Nếu `cachedKeHoachPanel != null` và `keHoachPanelSessionId == currentSessionId`:
    - Không gọi `installKeHoachContent(...)`.
    - Chỉ `cardLayout.show(mainContentPanel, "KeHoach")`.
  - Chỉ preload + tạo panel mới khi:
    - Chưa có panel cache, hoặc
    - `sessionId` đổi.

Kết quả mong muốn:
- Chuyển qua menu khác rồi quay lại Kế hoạch không mất dữ liệu đang nhập.

### Bước 2: Chặn remove/add card không cần thiết

File: `DashboardFormUI`

- Trong `syncPlanPanelsAfterWizardSessionCreated(...)`:
  - Không remove/add `KeHoach` nếu session không đổi.
  - Khi session đổi thật sự, mới rebuild panel.

- Trong `updateDeclarationPanel(...)`:
  - Giữ logic cập nhật `KhaiBao`.
  - Với `DuKien/KeHoach`: dùng hàm `ensure...ForSession` thay vì remove toàn bộ card.

Kết quả mong muốn:
- Tránh reset state ngoài ý muốn do thao tác điều hướng bình thường.

### Bước 3: Chỉ refresh bảng khi dữ liệu khai báo thay đổi

File: `DashboardFormUI`

- Gọi `notifyPlanningPanelsDeclarationChanged()` ở các điểm dữ liệu khai báo có thể đã đổi:
  - Sau khi lưu thành công ở Khai báo dữ liệu.
  - Sau callback `syncPlanPanelsAfterWizardSessionCreated(...)`.

- `notifyPlanningPanelsDeclarationChanged()`:
  - Nếu panel cache tồn tại và cùng session, gọi `onDeclarationDataChanged()`.

File: `PN_AssurancePlanPanelUI`, `PN_PlanEstimationPanelUI`
- Implement `onDeclarationDataChanged()` gọi `refreshDataTablesOnly()`.
- Nếu tab nào chưa có API refresh, bổ sung method `reloadFromSession(int sessionId)` cho tab đó.
- `refreshDataTablesOnly()` chỉ được phép:
  - clear dữ liệu hàng cũ trong bảng,
  - nạp lại dữ liệu hàng mới,
  - fire sự kiện model (`fireTableDataChanged` hoặc tương đương).
- `refreshDataTablesOnly()` không được phép:
  - tạo lại tab/panel,
  - thay đổi `JComboBox`,
  - gọi hàm reset form text.

Kết quả mong muốn:
- Ví dụ thêm 1 vật chất ở Khai báo dữ liệu, quay lại Kế hoạch thấy bảng cập nhật.
- Nội dung text người dùng đang nhập tay không bị mất.

### Bước 4: Chuẩn hóa contract refresh cho các tab bảng

File mục tiêu (nếu thiếu API load):
- `Tab4_EquipPlanPanelUI`
- `Tab5_MaterialPanelUI`
- `Tab7_MedPlanPanelUI`
- `Tab8_MaintPlanPanelUI`
- `Tab9_TransportPanel`

Nguyên tắc:
- Method refresh phải idempotent: gọi nhiều lần không nhân bản dòng.
- Luôn clear model trước khi nạp lại.
- Không tự reset các text area nhập tay ngoài bảng.
- Không đụng vào `JComboBox` hiện có (giữ nguyên selected item và model).
- Chỉ cập nhật thành phần bảng cần thiết, không refresh panel cha.

## 5. Acceptance Criteria (tiêu chí nghiệm thu)

1. Nhập dữ liệu ở Kế hoạch (tab text + bảng), chuyển sang Thông tin cá nhân/Lịch sử/Khai báo rồi quay lại: dữ liệu vẫn còn.
2. Thêm mới vật chất ở Khai báo dữ liệu, quay lại Kế hoạch:
   - Các bảng liên quan cập nhật đúng dữ liệu mới.
   - Tab text (đánh giá, nhiệm vụ, chỉ huy...) không bị reset.
  - Mọi `JComboBox` giữ nguyên giá trị đang chọn, không bị nạp lại danh sách.
3. Chuyển giữa menu `Dự kiến kế hoạch` và `Kế hoạch bảo đảm` nhiều lần:
   - Không lag tăng dần.
   - Không tạo panel trùng trong `mainContentPanel`.
4. Xuất Word vẫn lấy đúng dữ liệu sau các lần refresh bảng.
5. Trong log debug không xuất hiện luồng remove/add card vì lý do cập nhật dữ liệu khai báo.

## 6. Rủi ro và cách giảm thiểu

- Rủi ro 1: Refresh bảng ghi đè nhầm dữ liệu user đang sửa dở trong bảng.
  - Giảm thiểu: chỉ refresh khi có tín hiệu dữ liệu khai báo đã lưu; tránh refresh tự động khi user đang focus bảng.

- Rủi ro 2: Session đổi nhưng cache panel cũ chưa hủy.
  - Giảm thiểu: so sánh `panel.getSessionId()` với `currentSessionId` trước mọi lần dùng cache.

- Rủi ro 3: Một số tab chưa có API reload chuẩn.
  - Giảm thiểu: thống nhất interface nội bộ `reloadFromSession(int sessionId)` cho các tab dạng bảng.

## 7. Gợi ý thứ tự triển khai thực tế

1. Sửa `DashboardFormUI` để không recreate card khi chỉ đổi menu.
2. Thêm API `refreshDataTablesOnly()` vào `PN_AssurancePlanPanelUI` và test manual.
3. Áp dụng tương tự cho `PN_PlanEstimationPanelUI`.
4. Bổ sung signal `notifyPlanningPanelsDeclarationChanged()` từ luồng Khai báo dữ liệu.
5. Chạy regression test: lưu nháp, chuyển menu, xuất Word.

## 8. Phạm vi không làm trong đợt này

- Không thay đổi cấu trúc DB.
- Không thay đổi giao diện/UX ngoài hành vi refresh.
- Không refactor toàn bộ luồng DataDeclaration, chỉ chèn hook thông báo thay đổi dữ liệu.

## 9. Danh sách thao tác cấm khi implement

- Cấm recreate card `DuKien` hoặc `KeHoach` để làm mới dữ liệu.
- Cấm gọi lại constructor của panel/tab chỉ để cập nhật dữ liệu mới.
- Cấm reset `JComboBox` (setModel, removeAllItems, setSelectedIndex mặc định).
- Cấm refresh toàn panel cha khi chỉ có thay đổi dữ liệu bảng.
