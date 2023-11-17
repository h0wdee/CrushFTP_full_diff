/*
 * Decompiled with CFR 0.152.
 */
import crushftp.server.ServerStatus;

public class CrushFTPD {
    public static void main(String[] args) {
        CrushFTP.main(new String[]{"-d"});
    }

    public CrushFTPD() {
        try {
            new ServerStatus(true, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}

