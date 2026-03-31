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

            // Paragraph ngoài bảng
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceTextInParagraph(paragraph, dataMap);
            }

            // Paragraph trong bảng
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceTextInParagraph(paragraph, dataMap);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }

            System.out.println("Xuất file Word thành công: " + outputPath);
        }
    }

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

        // Tạo run mới, set style cố định để tránh lỗi disconnected XML
        XWPFRun newRun = paragraph.createRun();
        newRun.setFontFamily("Times New Roman");
        newRun.setFontSize(14);

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