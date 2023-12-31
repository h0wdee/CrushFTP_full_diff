/*
 * Decompiled with CFR 0.152.
 */
package crushftp.gui;

import com.crushftp.client.File_S;
import crushftp.handlers.Common;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class LOC {
    public static Properties localization = null;

    static {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = new LOC().getClass().getResourceAsStream("/assets/English.xml");
            byte[] b = new byte[32768];
            int bytes = 0;
            while (bytes >= 0) {
                bytes = in.read(b);
                if (bytes <= 0) continue;
                out.write(b, 0, bytes);
            }
            out.close();
            in.close();
            localization = (Properties)Common.readXMLObject(new ByteArrayInputStream(out.toByteArray()));
            if (new File_S("server_messages.xml").exists()) {
                in = new FileInputStream(new File_S("server_messages.xml"));
                Properties localization2 = (Properties)Common.readXMLObject(in);
                in.close();
                localization.putAll((Map<?, ?>)localization2);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String G(String s) {
        if (localization == null) {
            return s;
        }
        Object obj = localization.get(s);
        if (obj == null) {
            localization.put(s, s);
            obj = s;
        }
        return obj.toString();
    }

    public static String G(String s, String s0) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        return s;
    }

    public static String G(String s, String s0, String s1) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        return s;
    }

    public static String G(String s, String s0, String s1, String s2) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        s = Common.replace_str(s, "$2", s2);
        return s;
    }

    public static String G(String s, String s0, String s1, String s2, String s3) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        s = Common.replace_str(s, "$2", s2);
        s = Common.replace_str(s, "$3", s3);
        return s;
    }

    public static String G(String s, String s0, String s1, String s2, String s3, String s4) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        s = Common.replace_str(s, "$2", s2);
        s = Common.replace_str(s, "$3", s3);
        s = Common.replace_str(s, "$4", s4);
        return s;
    }

    public static String G(String s, String s0, String s1, String s2, String s3, String s4, String s5) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        s = Common.replace_str(s, "$2", s2);
        s = Common.replace_str(s, "$3", s3);
        s = Common.replace_str(s, "$4", s4);
        s = Common.replace_str(s, "$5", s5);
        return s;
    }

    public static String G(String s, String s0, String s1, String s2, String s3, String s4, String s5, String s6) {
        s = LOC.G(s);
        s = Common.replace_str(s, "$0", s0);
        s = Common.replace_str(s, "$1", s1);
        s = Common.replace_str(s, "$2", s2);
        s = Common.replace_str(s, "$3", s3);
        s = Common.replace_str(s, "$4", s4);
        s = Common.replace_str(s, "$5", s5);
        s = Common.replace_str(s, "$6", s6);
        return s;
    }
}

