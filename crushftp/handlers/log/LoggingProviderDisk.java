/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers.log;

import com.crushftp.client.File_S;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.LoggingProvider;
import crushftp.server.ServerStatus;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class LoggingProviderDisk
extends LoggingProvider {
    public static Object log_lock = new Object();
    public static RandomAccessFile log_file_stream = null;
    public String lastLogPath = "";
    long currentSyslogXmlTime = 0L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void checkForLogRoll() {
        block16: {
            try {
                block18: {
                    block17: {
                        logLen = 0L;
                        if (LoggingProviderDisk.log_file_stream != null) {
                            var3_3 = LoggingProviderDisk.log_lock;
                            synchronized (var3_3) {
                                logLen = LoggingProviderDisk.log_file_stream.length();
                            }
                        }
                        Log.log("SERVER", 3, "Log Rolling:Checking if its time to roll logs either daily or size.");
                        if (LoggingProviderDisk.log_file_stream == null) break block17;
                        if (ServerStatus.BG("roll_daily_logs")) break block17;
                        if (logLen <= ServerStatus.LG("roll_log_size") * 1024L * 1024L) break block17;
                        Log.log("SERVER", 0, "roll_size_logs:Performing Size Log Rolling");
                        renamer1 = new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                        Common.updateOSXInfo(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                        logFiles = new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/logs/");
                        logFiles.mkdirs();
                        Common.updateOSXInfo(String.valueOf(logFiles.getPath()) + "/");
                        sdf = new SimpleDateFormat(ServerStatus.SG("log_roll_date_format"), Locale.US);
                        cal = new GregorianCalendar();
                        cal.setTime(new Date());
                        cal.add(5, ServerStatus.IG("log_roll_rename_hours"));
                        renamer2 = new File_S(String.valueOf(logFiles.getPath()) + "/" + renamer1.getName() + "_" + sdf.format(cal.getTime()) + ".log");
                        Log.log("SERVER", 0, "roll_size_logs:" + renamer1 + "   to   " + renamer2);
                        this.do_roll(renamer1, renamer2);
                        Common.updateOSXInfo(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                        Log.log("SERVER", 3, "roll_size_logs:Looking for old rolled logs to delete.");
                        retry_loops = 0;
                        if (true) ** GOTO lbl65
                        do {
                            items = logFiles.list();
                            minDate = new Date().getTime();
                            minIndex = items.length - 1;
                            logCount = 0;
                            x = 0;
                            while (x < items.length) {
                                if (items[x].toUpperCase().endsWith(".LOG")) {
                                    ++logCount;
                                    modified = new File_S(String.valueOf(logFiles.getPath()) + "/" + items[x]).lastModified();
                                    if (modified < minDate) {
                                        minIndex = x;
                                        minDate = modified;
                                    }
                                }
                                ++x;
                            }
                            if (logCount <= ServerStatus.IG("roll_log_count")) break;
                            ++retry_loops;
                            if (!new File_S(String.valueOf(logFiles.getPath()) + "/" + items[minIndex]).delete()) continue;
                            retry_loops = 0;
lbl65:
                            // 3 sources

                        } while (ServerStatus.IG("roll_log_count") > 0 && retry_loops < 100);
                        Log.log("SERVER", 3, "roll_size_logs:Done.");
                        break block16;
                    }
                    if (LoggingProviderDisk.log_file_stream == null) break block16;
                    if (!ServerStatus.BG("roll_daily_logs")) break block16;
                    bad_names = new Vector<String>();
                    sdfHHmm = new SimpleDateFormat("HH:mm", Locale.US);
                    Log.log("SERVER", 3, "roll_daily_logs:Checking log roll time:" + sdfHHmm.format(new Date()) + "  compared to  " + ServerStatus.SG("log_roll_time"));
                    if (!sdfHHmm.format(new Date()).equals(ServerStatus.SG("log_roll_time"))) break block18;
                    Log.log("SERVER", 0, "roll_daily_logs:Performing Daily Log Rolling");
                    renamer1 = new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                    Common.updateOSXInfo(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                    logFiles = new File_S(String.valueOf(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)).getCanonicalFile().getParentFile().getPath()) + "/logs/");
                    logFiles.mkdir();
                    Common.updateOSXInfo(String.valueOf(logFiles.getPath()) + "/");
                    sdf = new SimpleDateFormat(ServerStatus.SG("log_roll_date_format"), Locale.US);
                    cal = new GregorianCalendar();
                    cal.setTime(new Date());
                    cal.add(5, ServerStatus.IG("log_roll_rename_hours"));
                    renamer2 = new File_S(String.valueOf(logFiles.getPath()) + "/" + renamer1.getName() + "_" + sdf.format(new Date(cal.getTime().getTime() - 50000L)) + ".log");
                    Log.log("SERVER", 0, "roll_daily_logs:Closing current log.");
                    Log.log("SERVER", 0, "roll_daily_logs:Moving " + renamer1.getPath() + " to " + renamer2.getPath());
                    this.do_roll(renamer1, renamer2);
                    Log.log("SERVER", 0, "roll_daily_logs:Opening new log file.");
                    Common.updateOSXInfo(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null));
                    Log.log("SERVER", 3, "roll_daily_logs:Looking for old rolled logs to delete.");
                    if (true) ** GOTO lbl129
                    do {
                        items = logFiles.list();
                        minDate = new Date().getTime();
                        minIndex = items.length - 1;
                        logCount = 0;
                        x = 0;
                        while (x < items.length) {
                            if (bad_names.indexOf(items[x]) < 0) {
                                if (items[x].toUpperCase().endsWith(".LOG")) {
                                    ++logCount;
                                }
                                if ((modified = new File_S(String.valueOf(logFiles.getPath()) + "/" + items[x]).lastModified()) < minDate) {
                                    minIndex = x;
                                    minDate = modified;
                                }
                            }
                            ++x;
                        }
                        if (logCount <= ServerStatus.IG("roll_log_count")) break;
                        if (new File_S(String.valueOf(logFiles.getPath()) + "/" + items[minIndex]).delete()) continue;
                        bad_names.addElement(items[minIndex]);
lbl129:
                        // 3 sources

                    } while (ServerStatus.IG("roll_log_count") > 0);
                    Thread.sleep(60000L);
                }
                if (bad_names.size() > 0) {
                    Log.log("SERVER", 0, "Failed to delete these old log files:" + bad_names);
                }
                Log.log("SERVER", 3, "roll_daily_logs:Done.");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, "Log rolling error!:" + e.toString());
                Log.log("SERVER", 0, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void do_roll(File_S renamer1, File_S renamer2) throws Exception {
        boolean roll_success = false;
        long start_roll = System.currentTimeMillis();
        Object object = log_lock;
        synchronized (object) {
            log_file_stream.close();
            roll_success = renamer1.renameTo(renamer2);
            if (!roll_success) {
                Common.streamCopier(new FileInputStream(renamer1), new FileOutputStream(renamer2), false, true, true);
                renamer1.delete();
                RandomAccessFile tmp = new RandomAccessFile(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)), "rw");
                tmp.setLength(0L);
                tmp.close();
            }
            log_file_stream = new RandomAccessFile(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)), "rw");
            log_file_stream.seek(log_file_stream.length());
        }
        Log.log("SERVER", 0, "Log rolling complete. Rename roll:" + roll_success + " Copy/Delete roll:" + !roll_success + " Time taken (ms):" + (System.currentTimeMillis() - start_roll));
    }

    @Override
    public void append_log(String log_data, String tag, boolean file_only) {
        block9: {
            block8: {
                block7: {
                    if (!ServerStatus.BG("write_to_log")) {
                        return;
                    }
                    log_data = String.valueOf(log_data.trim()) + "\r\n";
                    boolean ok = true;
                    if (log_data.indexOf("/WebInterface/") >= 0 && log_data.indexOf("/WebInterface/function/") < 0) {
                        if (ServerStatus.SG("log_allow_str").indexOf("(WEBINTERFACE)") < 0) {
                            ok = false;
                        }
                    }
                    if (!ok) break block7;
                    if (ServerStatus.SG("log_allow_str").indexOf("(" + tag + ")") >= 0) break block8;
                }
                if (this.newTags.indexOf("(" + tag + ")") < 0) break block9;
            }
            if (LoggingProviderDisk.checkFilters(ServerStatus.SG("filter_log_text"), log_data)) {
                this.logFile(String.valueOf(tag) + "|" + log_data);
            }
        }
    }

    @Override
    public Properties getLogSegment(long start, long len, String log_file) throws IOException {
        return LoggingProviderDisk.getLogSegmentStatic(start, len, log_file);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties getLogSegmentStatic(long start, long len, String log_file) throws IOException {
        Properties log = new Properties();
        log.put("log_start_date", "");
        log.put("log_end_date", "");
        log.put("log_start", "0");
        log.put("log_end", "0");
        log.put("log_max", "0");
        log.put("log_data", "");
        String startDate = "";
        String endDate = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (len <= 0L || len > 0x3200000L) {
            len = 0x100000L;
        }
        RandomAccessFile log_raf = null;
        boolean need_close = false;
        if (log_file != null && !log_file.trim().equals("")) {
            need_close = true;
            if (!new File_S(log_file).exists()) {
                log.put("log_start_date", "" + new Date());
                log.put("log_end_date", "" + new Date());
                log.put("log_segment", "No log is available for this session.");
                log.put("log_data", "");
                return log;
            }
            log_raf = new RandomAccessFile(new File_S(log_file), "r");
        } else if (ServerStatus.BG("write_to_log")) {
            log_raf = log_file_stream;
        }
        try {
            Object object = log_lock;
            synchronized (object) {
                if (log_raf != null) {
                    if (start > log_raf.length()) {
                        start = 0L;
                    }
                    if (start < log_raf.length()) {
                        int off;
                        log_raf.seek(start);
                        int bytes_read = 0;
                        byte[] b = new byte[32768];
                        long total_size = 0L;
                        while ((long)baos.size() < len && bytes_read >= 0) {
                            String s;
                            if ((long)b.length > len - total_size) {
                                b = new byte[(int)(len - total_size)];
                            }
                            bytes_read = log_raf.read(b);
                            if (startDate.equals("") && bytes_read > 0 && (s = new String(b, 0, bytes_read)).indexOf("|") > 0) {
                                off = s.indexOf("|");
                                while (off > 0 && s.charAt(off) != '\n') {
                                    --off;
                                }
                                try {
                                    if (off >= 0) {
                                        startDate = s.substring(s.indexOf("|") + 1, s.indexOf("|", s.indexOf("|") + 1)).trim();
                                    }
                                    if (startDate.length() > 30) {
                                        startDate = "";
                                    }
                                }
                                catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                                    // empty catch block
                                }
                            }
                            if (bytes_read < 0) continue;
                            baos.write(b, 0, bytes_read);
                            total_size += (long)bytes_read;
                        }
                        bytes_read = 0;
                        b = new byte[1];
                        while (bytes_read >= 0) {
                            bytes_read = log_raf.read(b);
                            if (bytes_read >= 0) {
                                baos.write(b, 0, bytes_read);
                            }
                            if (b[0] == 10) break;
                        }
                        String lastChunk = new String(baos.toByteArray(), baos.size() > 1000 ? baos.size() - 1000 : 0, baos.size() > 1000 ? 1000 : baos.size());
                        try {
                            if (lastChunk.lastIndexOf("|") > 0) {
                                off = lastChunk.lastIndexOf("|");
                                while (off >= 0 && lastChunk.charAt(off) != '\n') {
                                    --off;
                                }
                                if (off >= 0) {
                                    endDate = lastChunk.substring(lastChunk.indexOf("|", off), lastChunk.indexOf("|", lastChunk.indexOf("|", off) + 1)).trim();
                                }
                                if (endDate.length() > 30) {
                                    endDate = "";
                                }
                            }
                        }
                        catch (Exception e) {
                            System.out.println(lastChunk);
                            e.printStackTrace();
                        }
                        log.put("log_start", String.valueOf(start));
                        log.put("log_end", String.valueOf(log_raf.getFilePointer()));
                        log.put("log_max", String.valueOf(log_raf.length()));
                        log.put("log_segment", new String(baos.toByteArray(), "UTF8"));
                        if (log.getProperty("log_segment").indexOf("\n") == log.getProperty("log_segment").lastIndexOf("\n") && endDate.equals("")) {
                            endDate = startDate;
                        }
                        log.put("log_start_date", String.valueOf(startDate));
                        log.put("log_end_date", String.valueOf(endDate));
                    }
                }
            }
        }
        finally {
            if (need_close && log_raf != null) {
                log_raf.close();
            }
        }
        return log;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void logFile(String log_data) {
        Object object = log_lock;
        synchronized (object) {
            if (ServerStatus.BG("write_to_log") && log_file_stream != null) {
                try {
                    log_file_stream.seek(log_file_stream.length());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    log_file_stream.write(log_data.getBytes("UTF8"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (ServerStatus.BG("plugin_log_call")) {
            Properties p = new Properties();
            p.put("action", "log");
            p.put("data", log_data);
            p.put("server_settings", ServerStatus.server_settings);
            ServerStatus.thisObj.runPlugins(p);
        }
    }

    @Override
    public void shutdown() throws IOException {
        if (log_file_stream != null) {
            log_file_stream.close();
        }
        log_file_stream = null;
    }

    private void fixLogLocation() {
        String logLocation = ServerStatus.SG("log_location").replace('\\', '/');
        if (logLocation.equals("")) {
            logLocation = "./CrushFTP.log";
        }
        if (logLocation.equals("./") && System.getProperty("crushftp.home").equals("../")) {
            logLocation = "../CrushFTP.log";
        }
        if (logLocation.equals("./CrushFTP.log") && System.getProperty("crushftp.home").equals("../")) {
            logLocation = "../CrushFTP.log";
        }
        if (logLocation.equals("./")) {
            logLocation = "./CrushFTP.log";
        }
        if (logLocation.equals("./../../../../")) {
            logLocation = "./../../../../CrushFTP.log";
        }
        if (!logLocation.toUpperCase().endsWith(".LOG")) {
            logLocation = String.valueOf(logLocation) + "CrushFTP.log";
        }
        ServerStatus.server_settings.put("log_location", logLocation);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void checkLogPath() {
        if (!this.lastLogPath.equals(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null))) {
            try {
                Object object = log_lock;
                synchronized (object) {
                    if (log_file_stream != null) {
                        log_file_stream.close();
                    }
                    this.fixLogLocation();
                    log_file_stream = new RandomAccessFile(new File_S(ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null)), "rw");
                    log_file_stream.seek(log_file_stream.length());
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        this.lastLogPath = ServerStatus.change_vars_to_values_static(ServerStatus.SG("log_location"), null, null, null);
    }
}

