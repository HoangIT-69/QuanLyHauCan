import org.example.Popup.Tab5_DanPanel.Tab5_DanPanelService;
import org.example.Utils.DBConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.io.*;
import java.util.*;

public class FindDanSession {
    private static double parse(Object o){
        if(o==null) return 0;
        String s=o.toString().trim().replace(",", ".");
        if(s.isEmpty()||"-".equals(s)) return 0;
        try { return Double.parseDouble(s); } catch(Exception e){ return 0; }
    }

    private static int findRow(DefaultTableModel m, String label){
        for(int r=0;r<m.getRowCount();r++){
            Object o=m.getValueAt(r,0);
            String n=o==null?"":o.toString().trim();
            if(label.equals(n)) return r;
        }
        return -1;
    }

    public static void main(String[] args) throws Exception {
        List<Integer> sessions = new ArrayList<>();
        try(Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT id FROM sessions ORDER BY id DESC"); ResultSet rs = ps.executeQuery()){
            while(rs.next()) sessions.add(rs.getInt(1));
        }

        System.out.println("SESSION_COUNT=" + sessions.size());
        Tab5_DanPanelService svc = new Tab5_DanPanelService();

        PrintStream orig = System.out;
        for(int sid: sessions){
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            System.setOut(new PrintStream(sink));
            DefaultTableModel m;
            try { m = svc.getDanTableModel(sid); } catch(Exception ex){ System.setOut(orig); continue; }
            System.setOut(orig);

            if(m==null || m.getRowCount()==0) continue;
            int r = findRow(m, "SMPK 12,7mm");
            if(r<0) continue;

            double vk = parse(m.getValueAt(r,1));
            double cs = parse(m.getValueAt(r,2));
            double tl = parse(m.getValueAt(r,3));
            double toanDtl = parse(m.getValueAt(0,3));
            if(vk>0 || tl>0 || toanDtl>0){
                System.out.printf(Locale.US, "sid=%d\tvk=%.2f\tcs=%.2f\ttl=%.2f\ttoanD_TL=%.2f\n", sid, vk, cs, tl, toanDtl);
            }
        }
    }
}
