/*
 * Decompiled with CFR 0.152.
 */
import crushftp.gui.MainFrame;

public class CrushFTPGUI {
    public CrushFTPGUI() {
        try {
            new MainFrame();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}

