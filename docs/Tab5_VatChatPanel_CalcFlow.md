# Tài liệu: Flow tính toán Bảng Vật Chất (Tab 5 Popup)

> **Class:** `Tab5_VatChatPanelService`  
> **File:** `src/main/java/org/example/Popup/Tab5_VatChatPanel/Tab5_VatChatPanelService.java`  
> **Phân loại type:** `type = 1` → VCHC (Vật chất hậu cần) | `type = 2` → VTKT (Vật tư kỹ thuật)

---

## 1. Tổng quan cấu trúc bảng

### 1.1. Hai bảng song song

| Bảng | type | Dữ liệu nguồn (`loai_vat_chat`) | Nhóm hiển thị |
|------|------|--------------------------------|---------------|
| VCHC | 1 | `loai_vat_chat IN (2, 3)` | QN, QY, Doanh trại, VTKT |
| VTKT | 2 | `loai_vat_chat = 3` | Chỉ nhóm VTKT |

### 1.2. Cột bảng UI (27 cột, index 0..26)

| Index | Tên cột | Ý nghĩa |
|-------|---------|---------|
| 0 | Chi tiêu | Tên vật chất / header block |
| 1 | ĐVT | Đơn vị tính |
| 2 | TL ĐVT Toàn d | Trọng lượng ĐVT toàn đơn vị (d + PT) |
| 3 | TL ĐVT d | Trọng lượng ĐVT tiểu đoàn |
| 4 | TL ĐVT PT | Trọng lượng ĐVT phối thuộc |
| 5 | QDDT | Quy định dự trữ (hệ số) |
| 6 | TT GĐCB | Tiêu thụ giai đoạn chuẩn bị |
| 7 | TT GĐCĐ | Tiêu thụ giai đoạn chiến đấu |
| 8 | PC SCĐ Kho/d | Phải có sau chiến đấu - Kho |
| 9 | PC SCĐ ĐV | Phải có sau chiến đấu - Đơn vị |
| 10 | PC SCĐ + | Phải có sau chiến đấu - Tổng |
| 11 | HC Kho d | Hiện có Kho (tiểu đoàn) |
| 12 | HC Kho PT | Hiện có Kho (phối thuộc) |
| 13 | HC ĐV d | Hiện có Đơn vị (tiểu đoàn) |
| 14 | HC ĐV PT | Hiện có Đơn vị (phối thuộc) |
| 15 | HC + | Hiện có Tổng |
| 16 | BS TQĐ Kho/d | Bổ sung tổng quân đội - Kho/d |
| 17 | BS TQĐ ĐV | Bổ sung tổng quân đội - ĐV |
| 18 | BS GĐCB Kho/d | Bổ sung giai đoạn chuẩn bị - Kho/d |
| 19 | BS GĐCB ĐV | Bổ sung giai đoạn chuẩn bị - ĐV |
| 20 | BS GĐCĐ Kho/d | Bổ sung giai đoạn chiến đấu - Kho/d |
| 21 | BS GĐCĐ ĐV | Bổ sung giai đoạn chiến đấu - ĐV |
| 22 | BS + | Bổ sung Tổng = col18 + col19 + col20 + col21 |
| 23–26 | KH … | Kế hoạch (nhập tay): Thời gian, Địa điểm, Phương thức, Nhiệm vụ |

---

## 2. Luồng tải dữ liệu chính (`loadDataFromDatabase`)

```
loadDataFromDatabase(sessionId, type, vatChatModel)
│
├── PHASE 1: Tải quân số (step2_bien_che × quyuoc_bienche)
│   ├── Nhom "Tiểu đoàn"   → quanSo_d (tiểu đoàn)
│   └── Nhom "Trung đoàn" / "Sư đoàn"  → quanSo_pt (phối thuộc)
│       → Map: huong → (d, PT)
│       → Tổng: quanSo_d_ToanD, quanSo_pt_ToanD
│
├── PHASE 2: Tải vật chất & định mức (step4 × step3 × quyuoc_vchc)
│   → Phân loại thành 4 list: listQuanNhu, listQuanY, listDoanhTrai, listVTKT
│   → Tính baseHcKhoD, basePcScdKhoD, basePcTqdKhoD (từ chi tiết hoặc fallback 50/50)
│
├── PHASE 3: Tính quanSo_d_LLCL = quanSo_d_ToanD - Σ(quanSo_d từng Hướng)
│
└── PHASE 4: Render bảng theo từng block
    ├── Block "Toàn d"     (isToanD = true)
    ├── Block từng Hướng   (isDirection = true)
    └── Block "__LLCL__"   (isLlcl = true)
        → Mỗi block × 4 nhóm vật chất × từng mặt hàng → thêm row
        → Cập nhật dòng tổng nhóm và tổng block
```

---

## 3. Tính TL ĐVT (`calculateTL` / `calcTlHauCan`)

Hàm cốt lõi: tính trọng lượng theo đơn vị tính (kg hoặc tấn ĐVT), **không** đổi ra tấn tại bước này.

```
calculateTL(tenVatChat, quyUoc, donViTinh, quanSo):
│
├── quanSo == 0                       → return 0
│
├── DVT bắt đầu bằng "túi" / "tui"   → return quyUoc       (không nhân QS)
│
├── Tên chứa "dầu thắp" / "dau thap" → return (quyUoc × quanSo) / 30
│       (quy đổi: 30 người/ngày/túi)
│
├── Tên là Đường sữa / ĐSTB           → return (quyUoc / 7) × quanSo × 0.01
│       (quy đổi: 7 ngày, đơn vị %)
│
├── DVT chứa "%" hoặc "%QS"           → return quyUoc × quanSo × 0.01
│
├── DVT là "bộ" / "cái"               → return quyUoc       (không nhân QS)
│
└── Mặc định                          → return quyUoc × quanSo
```

> **Lưu ý:** `calcTlHauCan` (bảng 5 cột Hậu cần đơn giản) dùng cùng logic nhưng tham số `dvt` thay vì `donViTinh` từ `quyuoc_vchc`.

---

## 4. Tính từng cột cho từng dòng vật chất

### 4.1. Cột TL ĐVT (col 2, 3, 4)

Phụ thuộc vào loại block:

| Block | col 2 (Toàn d) | col 3 (d) | col 4 (PT) |
|-------|---------------|-----------|-----------|
| **Toàn d** | `tlDvtD + tlDvtPT` | `tlDvtD` | `tlDvtPT` hoặc `"-"` nếu ptExc |
| **Hướng** | `calculateTL(quanSoD_Huong)` | `"-"` | `"-"` |
| **LLCL** | `calculateTL(quanSoPT_LLCL)` | `"-"` | `"-"` |

Trong đó:
- `tlDvtD = calculateTL(ten, quyUoc, donViTinh, quanSo_d)` 
- `tlDvtPT = calculateTL(ten, quyUoc, donViTinh, quanSo_pt)`

### 4.2. Cột PC SCĐ (col 8, 9, 10)

```
scdKho = basePcScdKhoD  (từ scd_chitiet[0], hoặc phai_co_scd × 0.5)
scdDv  = basePcScdDv    (từ scd_chitiet[1], hoặc phai_co_scd × 0.5)
wPcScd = scdKho + scdDv

col 8  (PC SCĐ Kho/d):
  → Hướng: "-"
  → Toàn d / LLCL: scdKho

col 9  (PC SCĐ ĐV):  scdDv  (tất cả block)

col 10 (PC SCĐ +):
  → Hướng: scdDv  (chỉ đơn vị)
  → Toàn d / LLCL: wPcScd (kho + ĐV)
```

### 4.3. Cột Hiện có (col 11–15)

```
hasKho = (isToanD || isLlcl)   // Kho chỉ có ở Toàn d và LLCL

col 11 (HC Kho d):     hasKho ? wHcKhoD : "-"
col 12 (HC Kho PT):    hasKho && !ptExc ? khoPtForCols : "-"
col 13 (HC ĐV d):      wHcDonVi
col 14 (HC ĐV PT):     isDirection || ptExc ? "-" : wHcPhoiThuoc
col 15 (HC +):
  → Hướng:     wHcDonVi
  → Toàn d/LLC: hcKhoForBs + hcDvForBs
```

Trong đó:
```
hcKhoForBs = (hasKho ? wHcKhoD : 0) + (hasKho && !ptExc ? khoPtForCols : 0)
hcDvForBs  = isDirection ? wHcDonVi : wHcDonVi + (!ptExc ? wHcPhoiThuoc : 0)
```

### 4.4. Cột Bổ sung (col 16–22)

Đây là các **cột bổ sung** tính nhu cầu bổ sung từ kho về đơn vị chiến đấu.

```
pcTqdKho = hasKho ? basePcTqdKhoD : 0
pcTqdDv  = basePcTqdDv

ttGdcdKho = hasKho ? (wTtGdcd × 0.5) : 0
ttGdcdDv  = wTtGdcd - ttGdcdKho

b16 (BS TQĐ Kho/d)  = pcTqdKho - hcKhoForBs
b17 (BS TQĐ ĐV)     = pcTqdDv  - hcDvForBs
b18 (BS GĐCB Kho/d) = b16   ← (= pcTqdKho - hcKhoForBs)
b19 (BS GĐCB ĐV)    = b17   ← (= pcTqdDv  - hcDvForBs)
b20 (BS GĐCĐ Kho/d) = scdKho + ttGdcdKho - pcTqdKho
b21 (BS GĐCĐ ĐV)    = scdDv  + ttGdcdDv  - pcTqdDv
b22 (BS +)          = (không tính dòng item, chỉ tổng nhóm)
```

> **Lưu ý LLCL:** b16, b18, b20 được lấy từ snapshot của **Toàn d** (`toanDKhoNumSnapByTen`) để giữ đúng giá trị kho.

---

## 5. Tính dòng tổng nhóm (`tongCat[]`)

Hàm `addTonWeighted(tongCat, col, heSo, tlDvt)`:
```
tongCat[col] += (heSo × tlDvt) / 1000.0   // đổi sang tấn
```

### Tích lũy theo loại block:

#### Block Hướng (isDirection):
```
col 5:  wQddt   × tlDvtD / 1000
col 6:  wTtGdcb × tlDvtD / 1000
col 7:  wTtGdcd × tlDvtD / 1000
col 9:  scdDv   × tlDvtD / 1000
col 10: scdDv   × tlDvtD / 1000
col 13: wHcDonVi× tlDvtD / 1000
col 15: wHcDonVi× tlDvtD / 1000
col 17: b17     × tlDvtD / 1000
col 19: b19     × tlDvtD / 1000
col 21: b21     × tlDvtD / 1000
col 22: (b19 × tlDvtD + b21 × tlDvtD) / 1000
```

#### Block Toàn d / LLCL:
```
col 5:  wQddt   × tlDvtToanD / 1000
col 6:  wTtGdcb × tlDvtToanD / 1000
col 7:  wTtGdcd × tlDvtToanD / 1000
col 8:  scdKho  × tlDvtToanD / 1000
col 9:  scdDv   × tlDvtToanD / 1000
col 10: wPcScd  × tlDvtToanD / 1000
col 11: wHcKhoD × tlDvtToanD / 1000   (nếu hasKho)
col 12: khoPtForCols × tlDvtToanD / 1000  (nếu hasKho && !ptExc)
col 13: wHcDonVi × tlDvtD / 1000
col 14: wHcPhoiThuoc × tlDvtPT / 1000    (nếu !ptExc)
col 15: (hcKhoForBs + hcDvForBs) × tlDvtToanD / 1000
col 16: b16 × tlDvtToanD / 1000
col 17: b17 × tlDvtD / 1000
col 18: b18 × tlDvtToanD / 1000
col 19: b19 × tlDvtD / 1000
col 20: b20 × tlDvtToanD / 1000
col 21: b21 × tlDvtD / 1000
col 22 = tongCat[18] + tongCat[19] + tongCat[20] + tongCat[21]
```

---

## 6. Cập nhật lại dòng Toàn d

Sau khi duyệt xong tất cả các block, dòng header "Toàn d" được **ghi đè lại**:

```
col 5..22 của rowHuong[Toàn d] = Σ(các Hướng) + LLCL
```

Điều này đảm bảo Toàn d hiển thị tổng thực sự bằng tổng của tất cả hướng + LLCL.

---

## 7. Ghi kết quả vào các Map toàn cục (type = 1 VCHC)

### 7.1. `miniTableVCHC` — TL Bổ sung từng vật chất (Toàn d)

Được ghi qua `recordMiniTableVchcRow()` cho mỗi mặt hàng thuộc khối Toàn d:

```
tenVatChat → {
  "CATEGORY"              : catIndex (0=QN, 1=QY, 2=DT, 3=VTKT)
  "TL_BoSung_GDCB_Kho"   : b18 × tlDvtToanD / 1000    (tấn)
  "TL_BoSung_GDCB_DV_d"  : b19 × tlDvtD     / 1000    (tấn)
  "TL_BoSung_GDCD_Kho"   : b20 × tlDvtToanD / 1000    (tấn)
  "TL_BoSung_GDCD_DV"    : b21 × tlDvtD     / 1000    (tấn)
  "TL_BoSung_Total"       : tonGdcbKho + tonGdcbDv + tonGdcdKho + tonGdcdDv
}
```

> **Điều kiện:** Không ghi cho `isDuongSuaThuongBinh(ten) == true`

### 7.2. `miniTableVCHCByDirection` — TL Bổ sung từng vật chất theo Hướng

Tương tự 7.1 nhưng cho từng Hướng cụ thể (không phải LLCL):

```
huong → tenVatChat → {
  "TL_BoSung_GDCB_Kho"   : b18 × tlDvtToanD / 1000
  "TL_BoSung_GDCB_DV_d"  : b19 × tlDvtD     / 1000
  "TL_BoSung_GDCD_Kho"   : b20 × tlDvtToanD / 1000
  "TL_BoSung_GDCD_DV"    : b21 × tlDvtD     / 1000
}
```

### 7.3. `globalTonnageVCHC` — Tổng 5 khóa bổ sung (tấn)

Cộng dồn từ tất cả các entry trong `miniTableVCHC`:

```
"TL_BoSung_GDCB_Kho"  = Σ miniTableVCHC[*]["TL_BoSung_GDCB_Kho"]
"TL_BoSung_GDCB_DV_d" = Σ miniTableVCHC[*]["TL_BoSung_GDCB_DV_d"]
"TL_BoSung_GDCD_Kho"  = Σ miniTableVCHC[*]["TL_BoSung_GDCD_Kho"]
"TL_BoSung_GDCD_DV"   = Σ miniTableVCHC[*]["TL_BoSung_GDCD_DV"]
"TL_BoSung_Total"      = Σ miniTableVCHC[*]["TL_BoSung_Total"]
```

### 7.4. `globalTonnageVCHC_ByCat` — TL vận chuyển theo nhóm (cho Tab9)

Từ dòng tổng nhóm tại khối Toàn d:

```
"QN"   → { GDCB: tongCat_QN[18]+[19],  GDCD: tongCat_QN[20]+[21],  ToanTran: GDCB+GDCD }
"QY"   → { GDCB: tongCat_QY[18]+[19],  GDCD: tongCat_QY[20]+[21],  ToanTran: GDCB+GDCD }
"DT"   → { GDCB: tongCat_DT[18]+[19],  GDCD: tongCat_DT[20]+[21],  ToanTran: GDCB+GDCD }
"VTKT" → { GDCB: tongCat_VTKT[18]+[19], GDCD: tongCat_VTKT[20]+[21], ToanTran: GDCB+GDCD }
```

> Tất cả giá trị đã loại âm: `Math.max(0.0, value)`

---

## 8. Bảng Hậu cần 5 cột (`getHauCanTableModel`)

Đây là bảng rút gọn **chỉ đọc** 5 cột, độc lập với bảng VCHC chính.

### 8.1. Nguồn dữ liệu

- **Quân số:** `loadPersonnelByHuong()` — cùng SQL như Phase 1 chính
- **Vật chất:** `loadAndGroupQuyUocVchcStrict()` — chỉ query `SELECT * FROM quyuoc_vchc`

### 8.2. Phân nhóm vật chất (4 nhóm cố định)

| Nhóm | danh_muc trong DB |
|------|------------------|
| 1. Quân nhu | "Quân lương" hoặc "Quân trang" |
| 2. Quân y | "Quân y" |
| 3. Doanh trại | "Doanh trại" |
| 4. VTKT | "Khác" (mặc định) |

### 8.3. Tính giá trị 5 cột

Dùng `calcTlHauCan(ten, quyUoc, dvt, quanSo)` — cùng logic với `calculateTL`.

| SectionType | col 2 (TL Toàn d) | col 3 (TL d) | col 4 (TL PT) |
|-------------|-------------------|-------------|-------------|
| TOAN_D | `tlD + tlPT` | `tlD` | `tlPT` |
| HUONG | `calcTL(quanSoD)` | `"-"` | `"-"` |
| LLC | `calcTL(quanSoPt)` | `"-"` | `"-"` |

---

## 9. Ngoại lệ PT (`isPtExcludedVatChat`)

Các vật chất **không mang vác được** sẽ có cột PT hiển thị `"-"`:

| Điều kiện | Kết quả |
|-----------|---------|
| `type == 2` (VTKT) | PT luôn bị gạch |
| Tên chứa "túi" | PT bị gạch |
| Tên chứa "đường sữa" | PT bị gạch |
| `catIndex == 3` (nhóm VTKT trong VCHC) | PT bị gạch |

> **Ngoại lệ trong LLCL:** `isDuongSuaOrDstbTen` tại block LLCL **không** bị gạch PT (hiển thị đủ như Toàn d).

---

## 10. Ngoại lệ Đường sữa / ĐSTB tại Hướng

```
isHuongDstbFullDash(ten, isDirection):
  → isDirection && isDuongSuaOrDstbTen(ten)
  → Khi true: toàn bộ dòng số hiển thị "-", chỉ giữ tên và ĐVT
```

---

## 11. Snapshot Kho cho LLCL (`toanDKhoNumSnapByTen`)

Để LLCL dùng đúng giá trị Kho của **Toàn d** (không tính theo quân số hướng), khi render Toàn d mỗi item được lưu:

```
toanDKhoNumSnapByTen[ten] = [
  scdKho,       // index 0 → dùng làm scdKho cho LLCL
  wHcKhoD,      // index 1 → dùng làm wHcKhoD cho LLCL
  khoPtForCols, // index 2 → dùng làm khoPtForCols cho LLCL
  b16,          // index 3 → dùng làm b16 cho LLCL
  b18,          // index 4 → dùng làm b18 cho LLCL
  b20           // index 5 → dùng làm b20 cho LLCL
]
```

---

## 12. Export sang Word (`getExportData`)

### Mapping keyword → ô trong template:

```
{{vc_r{row}_c{col}}}   (VCHC, type=1)
{{vtkt_r{row}_c{col}}} (VTKT, type=2)
```

| `wordCell` | Giá trị |
|-----------|---------|
| 0 | STT (số thứ tự dòng) |
| 1..27 | UI col 0..26 |

- Tối đa 80 dòng dữ liệu × 28 ô/dòng
- HTML tags được strip, giá trị `"-"` được xuất thành `""`

---

## 13. Sơ đồ luồng tổng thể (tóm tắt)

```
DB: step2_bien_che × quyuoc_bienche
        ↓
   quanSo_d / quanSo_pt (theo Hướng + Toàn d)
        ↓
DB: step4_quy_dinh_du_tru × step3_vat_chat × quyuoc_vchc
        ↓
   ItemData[] (quyUoc, heSo, hiệnCó, chiTiet[])
        ↓
   for each block [Toàn d, Hướng1..N, LLCL]:
     for each nhóm [QN, QY, DT, VTKT]:
       for each item:
         ├─ calculateTL() → tlDvtD, tlDvtPT
         ├─ col 5..7:  hệ số (qddT, ttGĐCB, ttGĐCĐ)
         ├─ col 8..10: PC SCĐ (kho, ĐV, tổng)
         ├─ col 11..15: Hiện có (kho, ĐV, tổng)
         ├─ col 16..22: Bổ sung = PhảiCó - HiệnCó
         └─ addTonWeighted() → dòng tổng nhóm
     ← cập nhật dòng tổng hướng
   ← ghi đè dòng Toàn d = Σ hướng + LLCL
        ↓
   [type=1 VCHC only]
   miniTableVCHC       → Tab9 vận tải bổ sung
   miniTableVCHCByDirection → Tab9 chi tiết theo hướng
   globalTonnageVCHC   → tổng 5 TL bổ sung
   globalTonnageVCHC_ByCat → vận chuyển theo QN/QY/DT/VTKT
```

---

## 14. Các hàm tiện ích quan trọng

| Hàm | Mô tả |
|-----|-------|
| `addTonWeighted(arr, col, heSo, tlDvt)` | `arr[col] += (heSo × tlDvt) / 1000` — tích lũy tấn |
| `fmtCoeff(v)` | Format hệ số: bỏ `.00` (10 → "10", 1.5 → "1.5") |
| `fmtTonBold(v)` | Format tấn tổng: luôn 1 chữ số thập phân |
| `fmt2(v)` | Format 1 chữ số thập phân (bảng 5 cột) |
| `isDuongSuaOrDstbTen(ten)` | Nhận diện Đường sữa / ĐSTB |
| `isDuongSuaThuongBinh(ten)` | Nhận diện "Đường sữa thương binh" (không ghi miniTable) |
| `isPtExcludedVatChat(ten, cat, type)` | Kiểm tra vật chất không mang vác (PT = "-") |
| `parse6(s)` | Parse chuỗi "a,b,c,d,e,f" thành double[6] |
| `huongDetailIndex(huong)` | Map tên Hướng → index (0=ToànD, 2=chủyếu, 3=thứyếu, 4=phíasau, 5=LLCL) |
