package org.example.Utils;

import org.apache.poi.xwpf.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ExportWord {

    public static void exportDataToWord(InputStream is, String outputPath, Map<String, String> dataMap) throws Exception {
        try (XWPFDocument document = new XWPFDocument(is)) {

            // 1. Xử lý Paragraph ngoài bảng (Văn bản thường)
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceTextInParagraph(paragraph, dataMap);
            }

            // 2. Xử lý Paragraph trong bảng (CÓ NÂNG CẤP PARSE HTML THÀNH BẢNG WORD)
            for (XWPFTable table : document.getTables()) {
                processTable(table, dataMap);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }

            System.out.println("Xuất file Word thành công: " + outputPath);
        }
    }

    // --- HÀM MỚI: XỬ LÝ BẢNG ĐỂ DỊCH HTML ---
    private static void processTable(XWPFTable table, Map<String, String> dataMap) {
        // Duyệt ngược từ dưới lên để khi thêm/xóa dòng không bị lỗi lệch Index (IndexOutOfBounds)
        for (int r = table.getRows().size() - 1; r >= 0; r--) {
            XWPFTableRow row = table.getRow(r);
            boolean isHtmlRow = false;

            for (XWPFTableCell cell : row.getTableCells()) {
                String text = cell.getText();
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    // Nếu phát hiện thẻ placeholder chứa HTML (<tr>)
                    if (text.contains(entry.getKey()) && entry.getValue() != null && entry.getValue().contains("<tr>")) {
                        isHtmlRow = true;
                        // Chèn các dòng Word mới dựa trên mã HTML
                        insertHtmlRowsToTable(table, r, entry.getValue());
                        break;
                    }
                }
                if (isHtmlRow) break;
            }

            if (isHtmlRow) {
                // Xóa bỏ cái dòng chứa từ khóa <<rows_...>> đi
                table.removeRow(r);
            } else {
                // Nếu là dòng chữ bình thường, thay thế text như cũ
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceTextInParagraph(paragraph, dataMap);
                    }
                }
            }
        }
    }

    // --- HÀM MỚI: BIẾN MÃ HTML THÀNH DÒNG CỦA BẢNG WORD ---
    private static void insertHtmlRowsToTable(XWPFTable table, int insertPos, String htmlString) {
        String[] trs = htmlString.split("</tr>");
        int currentPos = insertPos; // Vị trí chèn dòng mới

        for (String tr : trs) {
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

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();

            if (replacedText.contains(key)) {
                replacedText = replacedText.replace(key, value);
                found = true;
            }
        }

        if (!found) return;

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