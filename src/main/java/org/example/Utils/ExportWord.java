package org.example.Utils;

import org.apache.poi.xwpf.usermodel.*;
import org.example.Tab.PlanEstimation.Tab9_TransportPanel.Tab9_TransportPanelService;
import org.example.Tab.PlanEstimation.Tab9_TransportPanel.Tab9_TransportPanelUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ExportWord {

    /**
     * Tab IX — Công tác vận tải: map placeholder Word (chuỗi khóa trùng nội dung trong template .docx).
     */
    public static void putTab9TransportExport(Map<String, String> map, Tab9_TransportPanelUI tab9) {
        if (map == null || tab9 == null) {
            return;
        }
        double n1 = InputValidator.parseDoubleSafe(tab9.getTdNguoiText());
        double x1 = InputValidator.parseDoubleSafe(tab9.getTdXeText());
        double n2 = InputValidator.parseDoubleSafe(tab9.getTrNguoiText());
        double x2 = InputValidator.parseDoubleSafe(tab9.getTrXeText());
        double n3 = InputValidator.parseDoubleSafe(tab9.getDqNguoiText());
        double x3 = InputValidator.parseDoubleSafe(tab9.getDqXeText());

        Tab9_TransportPanelService.CapacityBreakdown cap = Tab9_TransportPanelService.calculateTotalCapacity(
                n1, tab9.getTdKgMinText(), tab9.getTdKgMaxText(), x1,
                n2, tab9.getTrKgMinText(), tab9.getTrKgMaxText(), x2,
                n3, tab9.getDqKgMinText(), tab9.getDqKgMaxText(), x3);

        map.put("vt_d_min", Tab9_TransportPanelService.formatKgChuyen(cap.tdMin));
        map.put("vt_d_max", Tab9_TransportPanelService.formatKgChuyen(cap.tdMax));
        map.put("vt_d_xe", String.valueOf(InputValidator.parseIntSafe(tab9.getTdXeText())));

        map.put("vt_e_min", Tab9_TransportPanelService.formatKgChuyen(cap.trMin));
        map.put("vt_e_max", Tab9_TransportPanelService.formatKgChuyen(cap.trMax));
        map.put("vt_e_xe", String.valueOf(InputValidator.parseIntSafe(tab9.getTrXeText())));

        map.put("vt_dq_min", Tab9_TransportPanelService.formatKgChuyen(cap.dqMin));
        map.put("vt_dq_max", Tab9_TransportPanelService.formatKgChuyen(cap.dqMax));
        map.put("vt_dq_xe", String.valueOf(InputValidator.parseIntSafe(tab9.getDqXeText())));

        String tongMin = Tab9_TransportPanelService.formatKgChuyen(cap.tongMin);
        String tongMax = Tab9_TransportPanelService.formatKgChuyen(cap.tongMax);
        map.put("tong_vtb_kg", "Tổng " + tongMin + " ÷ " + tongMax);

        map.put("can_doi_vanchuyen", tab9.getKetLuanText());
    }

    public static void exportDataToWord(InputStream is, String outputPath, Map<String, String> dataMap) throws Exception {
        // === DEBUG: in ra tất cả key trong dataMap ===
        System.out.println("[ExportWord] dataMap keys (" + dataMap.size() + " entries):");
        dataMap.forEach((k, v) -> {
            if (k != null && (k.contains("bando") || k.contains("ten_ke") || k.contains("mat_do") || k.contains("nam}"))) {
                System.out.println("  KEY=" + k + "  VALUE=" + v);
            }
        });

        try (XWPFDocument document = new XWPFDocument(is)) {

            // 1. Xử lý Paragraph ngoài bảng (Văn bản thường)
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceTextInParagraph(paragraph, dataMap);
            }

            // 2. Xử lý Paragraph trong bảng TRƯỚC (phải làm TRƯỚC replaceInBodyXml)
            // QUAN TRỌNG: replaceInBodyXml gọi body.set(newBody) sẽ làm DISCONNECT tất cả XWPFTable objects.
            // Nếu processTable chạy sau body.set(), table.getRow() sẽ throw XmlValueDisconnectedException
            // và toàn bộ bảng bị bỏ qua → keyword còn nguyên trong file xuất.
            for (XWPFTable table : document.getTables()) {
                try {
                    processTable(table, dataMap);
                } catch (Exception ex) {
                    System.err.println("ExportWord: bỏ qua một bảng do lỗi: " + ex.getMessage());
                }
            }

            // 3. Xử lý TextBox/Shape và dọn sạch keyword còn sót (chạy SAU bảng)
            // replaceInBodyXml cũng scan lại toàn bộ body XML (bao gồm bảng) để đảm bảo
            // không còn keyword nào sót lại do run bị split hoặc các trường hợp đặc biệt.
            replaceInBodyXml(document, dataMap);


            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }

            System.out.println("Xuất file Word thành công: " + outputPath);
        }
    }

    /**
     * Duyệt XML body để xử lý text trong các Drawing/TextBox/Shape VÀ bảng.
     * Word lưu text box dưới dạng wps:txbx → w:txbxContent → w:p, không nằm trong document.getParagraphs().
     * Các keyword trong bảng cũng có thể bị split run, dùng XML replace để đảm bảo.
     */
    private static void replaceInBodyXml(XWPFDocument document, Map<String, String> dataMap) {
        // Fallback đáng tin cậy hơn: thay thế trực tiếp trong XML của body
        try {
            org.apache.xmlbeans.XmlObject body = document.getDocument().getBody();
            String xmlStr = body.xmlText();
            boolean changed = false;

            // Tối ưu hóa: Match cả dạng thường ({{...}}) VÀ dạng XML-escaped (&lt;&lt;...&gt;&gt;)
            // Pattern gộp: match <<...>> (escaped or not) và {{...}}
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(&lt;&lt;[^&<>]+&gt;&gt;|<<.*?>>|\\{\\{.*?\\}\\})"
            );
            java.util.regex.Matcher m = pattern.matcher(xmlStr);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String key = m.group(1);
                // Decode XML entities để lookup trong dataMap
                String decodedKey = key.replace("&lt;", "<").replace("&gt;", ">");
                String lookupKey = dataMap.containsKey(decodedKey) ? decodedKey
                                 : dataMap.containsKey(key)       ? key
                                 : null;
                if (lookupKey != null) {
                    String value = dataMap.get(lookupKey);
                    if (value == null) value = "";
                    // Re-encode value cho XML an toàn
                    String safeValue = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                    m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(safeValue));
                    changed = true;
                } else {
                    m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(key));
                }
            }
            m.appendTail(sb);

            if (changed) {
                org.apache.xmlbeans.XmlObject newBody = org.apache.xmlbeans.XmlObject.Factory.parse(sb.toString());
                body.set(newBody);
            }
        } catch (Exception e) {
            System.err.println("[ExportWord] replaceInBodyXml fallback error: " + e.getMessage());
        }
    }


    // --- HÀM MỚI: XỬ LÝ BẢNG ĐỂ DỊCH HTML ---
    private static void processTable(XWPFTable table, Map<String, String> dataMap) {
        // === PASS 1: Tìm tất cả dòng có HTML placeholder, ghi nhớ index và value ===
        // Phải dùng list snapshot vì sẽ modify bảng sau
        List<Integer> htmlRowIndices = new java.util.ArrayList<>();
        List<String> htmlRowValues = new java.util.ArrayList<>();

        int totalRows;
        try {
            totalRows = table.getRows().size();
        } catch (Exception e) {
            System.err.println("[ExportWord] processTable: không đọc được số dòng bảng: " + e.getMessage());
            return;
        }

        for (int r = 0; r < totalRows; r++) {
            XWPFTableRow row;
            try {
                row = table.getRow(r);
            } catch (Exception e) {
                continue;
            }
            if (row == null) continue;

            for (XWPFTableCell cell : row.getTableCells()) {
                String text = cell.getText();
                if (text == null) continue;

                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(<<.*?>>|\\{\\{.*?\\}\\})").matcher(text);
                while (m.find()) {
                    String key = m.group(1);
                    if (dataMap.containsKey(key)) {
                        String value = dataMap.get(key);
                        if (value != null && value.contains("<tr>")) {
                            htmlRowIndices.add(r);
                            htmlRowValues.add(value);
                            break;
                        }
                    }
                }
                if (!htmlRowIndices.isEmpty() && htmlRowIndices.get(htmlRowIndices.size() - 1) == r) break;
            }
        }

        // === PASS 2: Thay thế text bình thường trong tất cả dòng KHÔNG phải HTML ===
        // (trước khi modify structure)
        for (int r = 0; r < totalRows; r++) {
            if (htmlRowIndices.contains(r)) continue;
            XWPFTableRow row;
            try {
                row = table.getRow(r);
            } catch (Exception e) {
                continue;
            }
            if (row == null) continue;
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    replaceTextInParagraph(paragraph, dataMap);
                }
            }
        }

        // === PASS 3: Xử lý HTML rows - duyệt NGƯỢC để index không bị lệch khi xóa ===
        for (int i = htmlRowIndices.size() - 1; i >= 0; i--) {
            int rowIdx = htmlRowIndices.get(i);
            String htmlValue = htmlRowValues.get(i);
            try {
                insertHtmlRowsToTable(table, rowIdx, htmlValue);
                // Sau khi chèn, xóa dòng placeholder gốc (đã bị đẩy xuống 1 vị trí)
                int deletedRowIdx = rowIdx + countHtmlRows(htmlValue);
                table.removeRow(deletedRowIdx);
            } catch (Exception ex) {
                System.err.println("[ExportWord] processTable: bỏ qua HTML row " + rowIdx + ": " + ex.getMessage());
            }
        }
    }

    private static int countHtmlRows(String html) {
        if (html == null) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = html.indexOf("<tr>", idx)) != -1) {
            count++;
            idx += 4;
        }
        return count;
    }

    // --- HÀM MỚI: BIẾN MÃ HTML THÀNH DÒNG CỦA BẢNG WORD ---
    private static void insertHtmlRowsToTable(XWPFTable table, int insertPos, String htmlString) {
        if (htmlString == null || htmlString.isEmpty()) {
            return;
        }
        String[] trs = htmlString.split("</tr>");
        int currentPos = insertPos; // Vị trí chèn dòng mới

        for (String tr : trs) {
            try {
                if (!tr.contains("<tr>")) continue;
                String cleanTr = tr.substring(tr.indexOf("<tr>") + 4);
                String[] tds = cleanTr.split("</td>");

                // Tạo một dòng mới trong Word
                XWPFTableRow newRow = table.insertNewTableRow(currentPos++);

                for (int i = 0; i < tds.length; i++) {
                    String td = tds[i];
                    if (!td.contains("<td")) continue;

                    String content = td.substring(td.indexOf(">") + 1);
                    boolean isBold = content.contains("<b>") || content.contains("<strong>");
                    boolean isItalic = content.contains("<i>");

                    // Bắt Canh lề
                    ParagraphAlignment alignment = ParagraphAlignment.CENTER; // Mặc định canh giữa cho số liệu
                    if (td.contains("class='text-left'")) alignment = ParagraphAlignment.LEFT;
                    if (td.contains("class='text-right'")) alignment = ParagraphAlignment.RIGHT;

                    // Xóa sạch mọi thẻ HTML còn sót lại
                    content = content.replaceAll("<[^>]+>", "")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">");

                    // Tạo Cell mới
                    XWPFTableCell cell = newRow.getCell(i);
                    if (cell == null) cell = newRow.createCell();

                    // Dọn dẹp rác paragraph mặc định của POI
                    if (!cell.getParagraphs().isEmpty()) {
                        cell.removeParagraph(0);
                    }

                    // Ghi dữ liệu vào Cell
                    XWPFParagraph p = cell.addParagraph();
                    p.setAlignment(alignment);

                    XWPFRun run = p.createRun();
                    run.setText(content.trim());
                    run.setFontFamily("Times New Roman");
                    run.setFontSize(12); // Size font chuẩn trong bảng Word
                    if (isBold) run.setBold(true);
                    if (isItalic) run.setItalic(true);
                }
            } catch (Exception ex) {
                System.err.println("ExportWord: bỏ qua một dòng HTML: " + ex.getMessage());
            }
        }
    }

    // --- HÀM CŨ CỦA BẠN (Đã giữ nguyên logic rất tốt) ---
    private static void replaceTextInParagraph(XWPFParagraph paragraph, Map<String, String> dataMap) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText != null) {
                fullText.append(runText);
            }
        }

        String originalText = fullText.toString();
        if (originalText.isEmpty()) return;

        String replacedText = originalText;
        boolean found = false;

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(<<.*?>>|\\{\\{.*?\\}\\})").matcher(originalText);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            if (dataMap.containsKey(key)) {
                String value = dataMap.get(key) == null ? "" : dataMap.get(key);
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(value));
                found = true;
                System.out.println("[ExportWord] replaceTextInParagraph: '" + key + "' -> '" + value + "'");
            } else {
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(key));
            }
        }
        m.appendTail(sb);

        if (!found) return;
        replacedText = sb.toString();

        // Xóa toàn bộ run cũ
        for (int i = runs.size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Tạo run mới
        XWPFRun newRun = paragraph.createRun();
        newRun.setFontFamily("Times New Roman");
        newRun.setFontSize(14); // Size chuẩn ngoài văn bản

        String[] lines = replacedText.split("\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) newRun.addBreak();
            newRun.setText(lines[i]);
        }
    }

    public static String chooseSavePath(Component parent,
                                        String dialogTitle,
                                        String defaultFileName,
                                        String extensionDescription,
                                        String extension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter(extensionDescription, extension));

        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith("." + extension.toLowerCase())) {
            filePath += "." + extension;
        }

        return filePath;
    }

    public static String chooseSaveDocxPath(Component parent, String defaultFileName) {
        return chooseSavePath(
                parent,
                "Chọn nơi lưu file Word",
                defaultFileName,
                "Word Document (*.docx)",
                "docx"
        );
    }
}