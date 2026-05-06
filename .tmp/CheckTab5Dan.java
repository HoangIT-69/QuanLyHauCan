import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService;
import javax.swing.table.DefaultTableModel;

public class CheckTab5Dan {
    private static double parse(Object o){
        if(o==null) return 0;
        String s=o.toString().trim().replace(",", ".");
        if(s.isEmpty()||"-".equals(s)) return 0;
        try { return Double.parseDouble(s); } catch(Exception e){ return 0; }
    }

    private static boolean approx(double a,double b,double eps){ return Math.abs(a-b)<=eps; }

    private static int findLikelySession() {
        Tab5_DanPanelService s = new Tab5_DanPanelService();
        for (int sessionId = 200; sessionId >= 1; sessionId--) {
            DefaultTableModel m;
            try { m = s.getDanTableModel(sessionId); } catch (Exception ex) { continue; }
            if (m == null || m.getRowCount() == 0) continue;

            int smpkRow = -1;
            for (int r = 0; r < m.getRowCount(); r++) {
                Object nameObj = m.getValueAt(r, 0);
                String name = nameObj == null ? "" : nameObj.toString().trim();
                if ("SMPK 12,7mm".equals(name)) { smpkRow = r; break; }
            }
            if (smpkRow < 0) continue;

            double vk = parse(m.getValueAt(smpkRow, 1));
            double cs = parse(m.getValueAt(smpkRow, 2));
            double tl = parse(m.getValueAt(smpkRow, 3));
            double toanDtl = parse(m.getValueAt(0, 3));

            if (approx(vk, 9, 0.01) && approx(cs, 2.3, 0.05) && approx(tl, 4.22, 0.1) && approx(toanDtl, 43.56, 0.3)) {
                return sessionId;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int sessionId = findLikelySession();
        if (sessionId < 0) {
            System.out.println("NO_MATCHING_SESSION");
            return;
        }

        Tab5_DanPanelService svc = new Tab5_DanPanelService();
        DefaultTableModel m = svc.getDanTableModel(sessionId);
        System.out.println("SESSION_ID=" + sessionId);
        System.out.println("COLUMNS=4..28 (co so/TL tu Tieu thu den KHTN)");

        StringBuilder header = new StringBuilder("LoaiDan");
        for (int c = 4; c <= 28; c++) header.append("\t").append(c);
        System.out.println(header);

        for (int r = 0; r < m.getRowCount(); r++) {
            String name = m.getValueAt(r, 0) == null ? "" : m.getValueAt(r, 0).toString();
            if (r > 0) {
                String trimmed = name.trim();
                boolean isHeader = !trimmed.isEmpty() && !name.startsWith("      ");
                if (isHeader) break;
            }
            StringBuilder line = new StringBuilder(name.replace("\t", " "));
            for (int c = 4; c <= 28; c++) {
                Object v = m.getValueAt(r, c);
                line.append("\t").append(v == null ? "" : v.toString());
            }
            System.out.println(line);
        }
    }
}
