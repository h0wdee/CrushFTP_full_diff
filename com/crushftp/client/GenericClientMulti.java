/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class GenericClientMulti
extends GenericClient {
    public Vector clients = null;
    public Vector vItems = null;
    public Properties originalvItem = null;
    public static Object journal_lock = new Object();
    public static Object replication_lock = new Object();
    public static boolean replicating = false;
    public static Properties replication_status = new Properties();

    public GenericClientMulti(String header, Vector log, Properties originalvItem, Vector vItems, Vector clients, boolean play) {
        super(header, log);
        this.fields = new String[]{"username", "password", "timeout", "replicated_login_user", "replicated_login_pass", "error", "replicate_content", "skip_first_client", "async", "config", "x", "startPos", "path1", "path2", "path3", "url1", "url2", "truncate", "binary", "action", "item"};
        this.clients = clients;
        this.originalvItem = originalvItem;
        this.vItems = vItems;
        this.config.put("timeout", System.getProperty("crushftp.multi_journal_timeout", "20000"));
        if (play) {
            this.playJournal();
        }
    }

    @Override
    public void setCache(Properties statCache) {
        this.statCache = statCache;
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            c.setCache(statCache);
            ++x;
        }
    }

    @Override
    public void setConfigObj(Properties config) {
        config.putAll((Map<?, ?>)this.config);
        this.config = config;
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            c.setConfigObj(config);
            ++x;
        }
    }

    @Override
    public void setConfig(String key, Object o) {
        Properties p;
        Properties properties = p = key.startsWith("transfer_") ? this.transfer_info : this.config;
        if (o == null) {
            p.remove(key);
        } else {
            p.put(key, o);
        }
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            c.setConfig(key, o);
            ++x;
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        String result = "";
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            if (c.getConfig("replicated_login_user") != null) {
                username = c.getConfig("replicated_login_user").toString();
            }
            if (c.getConfig("replicated_login_pass") != null) {
                password = c.getConfig("replicated_login_pass").toString();
            }
            try {
                result = c.login(username, password, clientid);
            }
            catch (Exception e) {
                if (x == 0) {
                    throw e;
                }
                Common.log("REPLICATION", 1, e);
            }
            ++x;
        }
        return result;
    }

    @Override
    public void logout() throws Exception {
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            try {
                c.logout();
            }
            catch (Exception e) {
                if (x == 0) {
                    throw e;
                }
                Common.log("REPLICATION", 1, e);
            }
            ++x;
        }
    }

    @Override
    public void close() throws Exception {
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            try {
                c.close();
            }
            catch (Exception e) {
                if (x == 0) {
                    throw e;
                }
                Common.log("REPLICATION", 1, e);
            }
            ++x;
        }
    }

    public String fixPath(String path, Properties vItem) {
        return this.fixPath2(path, this.originalvItem.getProperty("url"), vItem.getProperty("url"));
    }

    public String fixPath2(String path, String url_vitem_original, String url_vitem_current) {
        VRL vrl_vitem_original = new VRL(url_vitem_original);
        VRL vrl_vitem_current = new VRL(url_vitem_current);
        return String.valueOf(vrl_vitem_current.getPath()) + path.substring(vrl_vitem_original.getPath().length());
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            String item_id = this.getItemId(c);
            if (x == 0 || !this.journaling(item_id) || System.getProperty("crushftp.multi_journal", "false").equals("false")) {
                Vector list2;
                block10: {
                    list2 = new Vector();
                    try {
                        list2 = c.list(this.fixPath(path, (Properties)this.vItems.get(x)), new Vector());
                    }
                    catch (FileNotFoundException e) {
                        Common.log("SERVER", 2, e);
                    }
                    catch (Exception e) {
                        c.setConfig("error", "" + e);
                        if (x > 0 && System.getProperty("crushftp.replicated_vfs", "false").equals("true")) {
                            this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, path, path, 0L, true, true, "list", item_id);
                        }
                        if (x != 0) break block10;
                        throw e;
                    }
                }
                if (System.getProperty("crushftp.replicated_vfs", "false").equals("true") && x == this.clients.size() - 1) break;
                int listSize = list.size();
                int x2 = 0;
                while (x2 < list2.size()) {
                    Properties p2 = (Properties)list2.elementAt(x2);
                    boolean found = false;
                    int x1 = 0;
                    while (!found && x1 < listSize) {
                        Properties p1 = (Properties)list.elementAt(x1);
                        if (p1.getProperty("name", "").equals(p2.getProperty("name", ""))) {
                            found = true;
                        }
                        ++x1;
                    }
                    if (!found) {
                        list.addElement(p2);
                    }
                    ++x2;
                }
            }
            ++x;
        }
        return list;
    }

    @Override
    public InputStream download(String path, long startPos, long endPos, boolean binary) throws Exception {
        Exception lastException = null;
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            try {
                InputStream inTemp = c.download(this.fixPath(path, (Properties)this.vItems.get(x)), startPos, endPos, binary);
                if (inTemp != null) {
                    return inTemp;
                }
            }
            catch (Exception e) {
                lastException = e;
            }
            ++x;
        }
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    @Override
    public OutputStream upload(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        Vector<OutputStream> outs = new Vector<OutputStream>();
        Vector<String> item_ids = new Vector<String>();
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    outs.addElement(c.upload(this.fixPath(path, (Properties)this.vItems.get(x)), startPos, truncate, binary));
                }
                catch (Exception e) {
                    outs.addElement(null);
                    Common.log("REPLICATION", 1, e);
                }
                item_ids.addElement(item_id);
            }
            ++x;
        }
        class OutputMulti
        extends OutputStream {
            Vector outs = null;
            boolean closed = false;
            long start_time = Common.uidg();
            Vector item_ids = null;
            private final /* synthetic */ String val$path;
            private final /* synthetic */ long val$startPos;
            private final /* synthetic */ boolean val$truncate;
            private final /* synthetic */ boolean val$binary;

            public OutputMulti(Vector outs, Vector item_ids, String string, long l, boolean bl, boolean bl2) {
                this.val$path = string;
                this.val$startPos = l;
                this.val$truncate = bl;
                this.val$binary = bl2;
                this.outs = outs;
                this.item_ids = item_ids;
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                int x = 0;
                while (x < this.outs.size()) {
                    OutputStream outTemp = (OutputStream)this.outs.elementAt(x);
                    try {
                        if (outTemp == null) {
                            throw new IOException("null outputstream, not openned...");
                        }
                        outTemp.write(b, off, len);
                        if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                            GenericClientMulti.this.writeJournal(b, off, len, (GenericClient)GenericClientMulti.this.clients.elementAt(x), this.start_time, x, this.val$path, this.val$path, this.val$path, this.val$startPos, this.val$truncate, this.val$binary, "upload", this.item_ids.elementAt(x).toString());
                        }
                    }
                    catch (IOException e) {
                        if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                            GenericClientMulti.this.writeJournal(b, off, len, (GenericClient)GenericClientMulti.this.clients.elementAt(x), this.start_time, x, this.val$path, this.val$path, this.val$path, this.val$startPos, this.val$truncate, this.val$binary, "upload", this.item_ids.elementAt(x).toString());
                        }
                        throw e;
                    }
                    ++x;
                }
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                int x = 0;
                while (x < this.outs.size()) {
                    block8: {
                        OutputStream outTemp = (OutputStream)this.outs.elementAt(x);
                        try {
                            if (outTemp == null) {
                                throw new IOException("null outputstream, not openned...");
                            }
                            outTemp.close();
                            if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                                GenericClientMulti.this.writeJournal(null, -1, -1, (GenericClient)GenericClientMulti.this.clients.elementAt(x), this.start_time, x, this.val$path, this.val$path, this.val$path, this.val$startPos, this.val$truncate, this.val$binary, "close", this.item_ids.elementAt(x).toString());
                            }
                        }
                        catch (IOException e) {
                            if (x == 0) {
                                throw e;
                            }
                            if (outTemp == null && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                                GenericClientMulti.this.writeJournal(new byte[0], 0, 0, (GenericClient)GenericClientMulti.this.clients.elementAt(x), this.start_time, x, this.val$path, this.val$path, this.val$path, this.val$startPos, this.val$truncate, this.val$binary, "upload", this.item_ids.elementAt(x).toString());
                            }
                            if (!System.getProperty("crushftp.multi_journal", "false").equals("true")) break block8;
                            GenericClientMulti.this.writeJournal(null, -1, -1, (GenericClient)GenericClientMulti.this.clients.elementAt(x), this.start_time, x, this.val$path, this.val$path, this.val$path, this.val$startPos, this.val$truncate, this.val$binary, "unlock", this.item_ids.elementAt(x).toString());
                        }
                    }
                    ++x;
                }
                this.closed = true;
            }
        }
        this.out = new OutputMulti(outs, item_ids, path, startPos, truncate, binary);
        return this.out;
    }

    @Override
    public boolean upload_0_byte(String path) throws Exception {
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    boolean ok2 = c.upload_0_byte(this.fixPath(path, (Properties)this.vItems.get(x)));
                    if (!ok2) {
                        ok = false;
                    }
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, path, path, 0L, true, true, "upload_0_byte", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean delete(String path) throws Exception {
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            if (!(x == 0 && this.config.getProperty("skip_first_client", "false").equals("true") || x > 0 && this.config.getProperty("replicate_content", "true").equals("false"))) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    boolean ok2 = c.delete(this.fixPath(path, (Properties)this.vItems.get(x)));
                    if (!ok2 && (this.journaling(item_id) || x > 0) && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        ok = false;
                        throw new IOException("Journaling...");
                    }
                    if (!ok2 && x == 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        ok = false;
                        break;
                    }
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, path, path, 0L, true, true, "delete", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            String item_id = this.getItemId(c);
            try {
                if (x > 0 && this.journaling(item_id)) {
                    throw new IOException("Journaling...");
                }
                boolean ok2 = c.makedir(this.fixPath(path, (Properties)this.vItems.get(x)));
                if (!ok2 && (this.journaling(item_id) || x > 0)) {
                    ok = false;
                    if (this.stat(path) == null) {
                        throw new IOException("Journaling...");
                    }
                } else if (!ok2 && x == 0) {
                    ok = false;
                    break;
                }
            }
            catch (Exception e) {
                if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                    this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, path, path, 0L, true, true, "makedir", item_id);
                }
                throw e;
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            String item_id = this.getItemId(c);
            try {
                if (x > 0 && this.journaling(item_id)) {
                    throw new IOException("Journaling...");
                }
                boolean ok2 = c.makedirs(this.fixPath(path, (Properties)this.vItems.get(x)));
                if (!ok2 && (this.journaling(item_id) || x > 0)) {
                    ok = false;
                    if (this.stat(path) == null) {
                        throw new IOException("Journaling...");
                    }
                } else if (!ok2 && x == 0) {
                    ok = false;
                    break;
                }
            }
            catch (Exception e) {
                if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                    this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, path, path, 0L, true, true, "makedirs", item_id);
                }
                throw e;
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean rename(String rnfr, String rnto) throws Exception {
        return this.rename(rnfr, rnto, false);
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        Properties p;
        boolean is_file = true;
        if (this.config.getProperty("replicate_content", "true").equals("true") && (p = this.stat(rnfr)) != null && p.getProperty("type", "FILE").equalsIgnoreCase("DIR")) {
            is_file = false;
        }
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false") || !is_file) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    boolean ok2 = c.rename(this.fixPath(rnfr, (Properties)this.vItems.get(x)), this.fixPath(rnto, (Properties)this.vItems.get(x)), overwrite);
                    if (!ok2 && (this.journaling(item_id) || x > 0) && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        ok = false;
                        throw new IOException("Journaling...");
                    }
                    if (!ok2 && x == 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        ok = false;
                        break;
                    }
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, rnfr, rnto, "", 0L, true, true, "rename", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
        return ok;
    }

    @Override
    public Properties stat(String path) throws Exception {
        int x = 0;
        while (x < this.clients.size()) {
            GenericClient c = (GenericClient)this.clients.elementAt(x);
            try {
                Properties p = c.stat(this.fixPath(path, (Properties)this.vItems.get(x)));
                if (p != null) {
                    return p;
                }
            }
            catch (Exception e) {
                if (x == 0) {
                    throw e;
                }
                Common.log("REPLICATION", 1, e);
            }
            ++x;
        }
        return null;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        boolean ok = true;
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    boolean ok2 = c.mdtm(this.fixPath(path, (Properties)this.vItems.get(x)), modified);
                    if (!ok2 && (this.journaling(item_id) || x > 0) && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        ok = false;
                        throw new IOException("Journaling...");
                    }
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, String.valueOf(modified), path, 0L, true, true, "mdtm", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
        return ok;
    }

    @Override
    public void setMod(String path, String val, String param) throws Exception {
        if (param == null || param.trim().equals("")) {
            return;
        }
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    c.setMod(path, val, param);
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, val, param, 0L, true, true, "setMod", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
    }

    @Override
    public void setOwner(String path, String val, String param) throws Exception {
        if (param == null || param.trim().equals("")) {
            return;
        }
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    c.setOwner(path, val, param);
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, val, param, 0L, true, true, "setOwner", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
    }

    @Override
    public void setGroup(String path, String val, String param) throws Exception {
        if (param == null || param.trim().equals("")) {
            return;
        }
        int x = 0;
        while (x < this.clients.size()) {
            if (x <= 0 || !this.config.getProperty("replicate_content", "true").equals("false")) {
                GenericClient c = (GenericClient)this.clients.elementAt(x);
                String item_id = this.getItemId(c);
                try {
                    if (x > 0 && this.journaling(item_id)) {
                        throw new IOException("Journaling...");
                    }
                    c.setGroup(path, val, param);
                }
                catch (Exception e) {
                    if (x > 0 && System.getProperty("crushftp.multi_journal", "false").equals("true")) {
                        this.writeJournal(null, 0, 0, (GenericClient)this.clients.elementAt(x), Common.uidg(), x, path, val, param, 0L, true, true, "setGroup", item_id);
                    }
                    throw e;
                }
            }
            ++x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeJournal(byte[] b, int off, int len, GenericClient c, long start_time, int x, String path1, String path2, String path3, long startPos, boolean truncate, boolean binary, String action, String item_id) throws IOException {
        Object object = journal_lock;
        synchronized (object) {
            String journal_path = "./multi_journal/" + item_id + "/" + start_time + "/" + c.toString() + "/";
            if (!new File_S(journal_path).exists()) {
                new File_S(journal_path).mkdirs();
                Properties config_wrapper = new Properties();
                config_wrapper.put("config", c.config);
                config_wrapper.put("x", String.valueOf(x));
                config_wrapper.put("path1", path1);
                config_wrapper.put("path2", path2);
                config_wrapper.put("path3", path3);
                config_wrapper.put("url1", this.originalvItem.getProperty("url"));
                config_wrapper.put("url2", ((Properties)this.vItems.get(x)).getProperty("url"));
                config_wrapper.put("startPos", String.valueOf(startPos));
                config_wrapper.put("truncate", String.valueOf(truncate));
                config_wrapper.put("binary", String.valueOf(binary));
                config_wrapper.put("action", String.valueOf(action));
                if (action.equals("upload")) {
                    Common.writeXMLObject(String.valueOf(journal_path) + "config.XML.locked", config_wrapper, "config");
                } else {
                    Common.writeXMLObject(String.valueOf(journal_path) + "config.XML", config_wrapper, "config");
                }
            }
            if (action.equals("unlock")) {
                new File_S(String.valueOf(journal_path) + "config.XML.locked").renameTo(new File_S(String.valueOf(journal_path) + "config.XML"));
            } else if (action.equals("close")) {
                Common.recurseDelete(journal_path, false);
                String journal_path_parent = "./multi_journal/" + item_id + "/";
                new File_S(String.valueOf(Common.all_but_last(journal_path)) + ".DS_Store").delete();
                new File_S(Common.all_but_last(journal_path)).delete();
                new File_S(String.valueOf(journal_path_parent) + ".DS_Store").delete();
                new File_S(journal_path_parent).delete();
                if (new File_S(String.valueOf(journal_path) + "upload").exists()) {
                    Common.log("REPLICATION", 0, "Journaling close error!!!  path1:" + path1 + " path2:" + path2 + " startPos:" + startPos + " truncate:" + truncate + " binary:" + binary + " action:" + action + " item_id:" + item_id + " off:" + off + " len:" + len);
                    Common.log("REPLICATION", 0, new Exception("Journaling issue."));
                }
            } else if (b != null) {
                RandomAccessFile raf = new RandomAccessFile(new File_S(String.valueOf(journal_path) + action), "rw");
                raf.seek(raf.length());
                raf.write(b, off, len);
                raf.close();
            }
        }
    }

    private String getItemId(GenericClient c) {
        String item_id = null;
        try {
            item_id = Common.getMD5(new ByteArrayInputStream(c.url.getBytes("UTF8")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        item_id = item_id.substring(item_id.length() - 6);
        return item_id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean journaling(String item_id) {
        Object object = journal_lock;
        synchronized (object) {
            String journal_path_parent = "./multi_journal/" + item_id + "/";
            File_S f = new File_S(journal_path_parent);
            if (!f.exists()) {
                return false;
            }
            File_S[] time_stamp_list = (File_S[])f.listFiles();
            if (time_stamp_list == null || time_stamp_list.length == 0) {
                return false;
            }
            int x = 0;
            while (true) {
                if (x >= time_stamp_list.length) {
                    return false;
                }
                if (time_stamp_list[x].isDirectory()) {
                    File_S time_stamp_item = new File_S(time_stamp_list[x]);
                    new File_S(String.valueOf(time_stamp_item.getPath()) + "/.DS_Store").delete();
                    if (time_stamp_item.exists()) {
                        File_S[] folders = (File_S[])time_stamp_item.listFiles();
                        if (folders == null || folders.length == 0) {
                            time_stamp_item.delete();
                            return false;
                        }
                        int xx = 0;
                        while (xx < folders.length) {
                            if (folders[xx].isDirectory()) {
                                try {
                                    Properties config_wrapper = null;
                                    config_wrapper = new File_S(String.valueOf(folders[xx].getPath()) + "/config.XML").exists() ? (Properties)Common.readXMLObject(String.valueOf(folders[xx].getPath()) + "/config.XML") : (Properties)Common.readXMLObject(String.valueOf(folders[xx].getPath()) + "/config.XML.locked");
                                    if (config_wrapper.containsKey("config")) {
                                        config_wrapper = (Properties)config_wrapper.get("config");
                                    }
                                    if (config_wrapper.getProperty("async", "false").equals("false")) {
                                        return true;
                                    }
                                }
                                catch (Exception e) {
                                    Common.log("SERVER", 2, e);
                                }
                            }
                            ++xx;
                        }
                    }
                }
                ++x;
            }
        }
    }

    /*
     * Exception decompiling
     */
    public void playJournal() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [1[TRYBLOCK], 3[TRYBLOCK]], but top level block is 52[WHILELOOP]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }
}

