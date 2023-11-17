/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers.log;

import com.crushftp.client.File_S;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.LoggingProvider;
import crushftp.server.ServerStatus;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
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
    public static boolean log_not_at_end = true;
    static ByteArrayOutputStream baos_log_buffer = new ByteArrayOutputStream();
    public static long last_log_flush = 0L;
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

    public static Properties getLogSnippet(String start_date, String end_date) throws Exception {
        Properties log_segment;
        block17: {
            if (!ServerStatus.BG("v11_beta")) {
                throw new Exception("Access Denied! This feature is only available on V11 or above.");
            }
            log_segment = null;
            try {
                SimpleDateFormat log_date_format = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
                long start = log_date_format.parse(start_date).getTime();
                long end = log_date_format.parse(end_date).getTime();
                if (end < start) {
                    throw new Exception("Invalid end date! Start date: " + new Date(start) + " End date: " + new Date(end));
                }
                if (end - start > 1800000L) {
                    throw new Exception("Invalid time length! Max permitted time length is 30 min.");
                }
                Properties log = new Properties();
                log.put("log_segment", "");
                long log_length = 1024L;
                long start_log_position = 0L;
                int x = 0;
                while (x < 5 && log_segment == null) {
                    log_segment = LoggingProviderDisk.load_log_segment(start_log_position);
                    Thread.sleep(100L);
                    ++x;
                }
                if (log_segment == null) {
                    throw new Exception("Could not load the log!");
                }
                long max_size = Long.parseLong(log_segment.getProperty("log_max", "0"));
                long current_log_end = Long.parseLong(log_segment.getProperty("log_end", "0"));
                long cureent_log_start_time = log_date_format.parse(log_segment.getProperty("log_start_date", "")).getTime();
                long current_log_end_time = log_date_format.parse(log_segment.getProperty("log_end_date", "").startsWith("|") ? log_segment.getProperty("log_end_date", "").substring(1) : log_segment.getProperty("log_end_date", "")).getTime();
                if (start < cureent_log_start_time) {
                    new Exception("No log available! Current log file start: " + log_segment.getProperty("log_start_date", ""));
                }
                if (start < current_log_end_time) {
                    start_log_position = LoggingProviderDisk.find_date_position_in_segment(start, log_segment.getProperty("log_segment", "0"));
                    if (end < current_log_end_time) {
                        log_length = LoggingProviderDisk.find_date_position_in_segment(end, log_segment.getProperty("log_segment", "0").substring((int)start_log_position));
                        return LoggingProviderDisk.getLogSegmentStatic(start_log_position, log_length, "");
                    }
                    log_length = LoggingProviderDisk.find_log_end_position(start_log_position, end);
                    if (log_length > 0xA00000L) {
                        log_length = 0xA00000L;
                    }
                    log_segment = LoggingProviderDisk.getLogSegmentStatic(start_log_position, log_length, "");
                    break block17;
                }
                long around = (start - cureent_log_start_time) * max_size / (System.currentTimeMillis() - cureent_log_start_time);
                log_segment = LoggingProviderDisk.load_log_segment(around);
                if (log_segment != null) {
                    cureent_log_start_time = log_date_format.parse(log_segment.getProperty("log_start_date", "")).getTime();
                    current_log_end_time = log_date_format.parse(log_segment.getProperty("log_end_date", "").startsWith("|") ? log_segment.getProperty("log_end_date", "").substring(1) : log_segment.getProperty("log_end_date", "")).getTime();
                } else {
                    around = max_size;
                }
                start_log_position = start > cureent_log_start_time && start < current_log_end_time ? LoggingProviderDisk.find_date_position_in_segment(start, log_segment.getProperty("log_segment", "0")) : (start < cureent_log_start_time ? LoggingProviderDisk.find_log_start_position(start, 0L, around, 0) : LoggingProviderDisk.find_log_start_position(start, around, max_size, 0));
                log_length = LoggingProviderDisk.find_log_end_position(start_log_position, end);
                if (log_length > 0xA00000L) {
                    log_length = 0xA00000L;
                }
                log_segment = LoggingProviderDisk.getLogSegmentStatic(start_log_position, log_length, "");
                current_log_end_time = 0L;
                try {
                    current_log_end_time = log_date_format.parse(log_segment.getProperty("log_end_date", "").startsWith("|") ? log_segment.getProperty("log_end_date", "").substring(1) : log_segment.getProperty("log_end_date", "")).getTime();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (current_log_end_time != 0L && current_log_end_time < start) {
                    throw new Exception("No available log snippet! Time start : " + start_date + " end : " + end_date);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 2, e);
                throw e;
            }
        }
        return log_segment;
    }

    private static Properties load_log_segment(long start) throws Exception {
        Properties log_segment = null;
        SimpleDateFormat log_date_format = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
        int x = 0;
        while (x < 5 && log_segment == null) {
            int xx = 50;
            while (xx < 4100) {
                log_segment = LoggingProviderDisk.getLogSegmentStatic(start, xx * 1024, "");
                if (!(log_segment == null || log_segment.getProperty("log_max", "0").equals("0") || log_segment.getProperty("log_start_date", "").equals("") || log_segment.getProperty("log_end_date", "").equals(""))) {
                    try {
                        long cureent_log_start_time = log_date_format.parse(log_segment.getProperty("log_start_date", "")).getTime();
                        long current_log_end_time = log_date_format.parse(log_segment.getProperty("log_end_date", "").startsWith("|") ? log_segment.getProperty("log_end_date", "").substring(1) : log_segment.getProperty("log_end_date", "")).getTime();
                        break;
                    }
                    catch (Exception e) {
                        log_segment = null;
                    }
                } else {
                    log_segment = null;
                }
                xx += 100;
            }
            ++x;
        }
        return log_segment;
    }

    private static long find_log_start_position(long start_date, long start, long end, int deep) throws Exception {
        if (deep > 100) {
            throw new Exception("Could not found start position!");
        }
        long pos = start + (end - start) / 2L;
        Properties log_segment = LoggingProviderDisk.load_log_segment(pos);
        if (log_segment == null) {
            int x = 0;
            while (x < 5 && log_segment == null) {
                Thread.sleep(100L);
                log_segment = LoggingProviderDisk.load_log_segment(pos += 100L);
                ++x;
            }
            if (log_segment == null) {
                throw new Exception("Could not load start position! Position: " + pos);
            }
        }
        SimpleDateFormat log_date_format = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
        long cureent_log_start_time = log_date_format.parse(log_segment.getProperty("log_start_date", "")).getTime();
        long current_log_end_time = log_date_format.parse(log_segment.getProperty("log_end_date", "").startsWith("|") ? log_segment.getProperty("log_end_date", "").substring(1) : log_segment.getProperty("log_end_date", "")).getTime();
        long start_pos = pos;
        start_pos = start_date > cureent_log_start_time && start_date < current_log_end_time ? (start_pos += LoggingProviderDisk.find_date_position_in_segment(start_date, log_segment.getProperty("log_segment", "0"))) : (start_date < cureent_log_start_time ? LoggingProviderDisk.find_log_start_position(start_date, start, pos, ++deep) : LoggingProviderDisk.find_log_start_position(start_date, pos, end, ++deep));
        return start_pos;
    }

    private static long find_log_end_position(long start_log_position, long end_date) throws Exception {
        long log_length = 0L;
        int x = 0;
        while (x < 10) {
            Properties log_segment = LoggingProviderDisk.getLogSegmentStatic(start_log_position + (long)(x * 1024 * 104), 0x100000L, "");
            long length = LoggingProviderDisk.find_date_position_in_segment(end_date, log_segment.getProperty("log_segment", "0"));
            if (length != (long)log_segment.getProperty("log_segment", "0").length()) {
                log_length = length;
                break;
            }
            log_length += length;
            ++x;
        }
        return log_length;
    }

    private static long find_date_position_in_segment(long date, String segment) throws Exception {
        SimpleDateFormat log_date_format = new SimpleDateFormat(ServerStatus.SG("log_date_format"), Locale.US);
        String data = "";
        long pos = 0L;
        BufferedReader reader = new BufferedReader(new StringReader(segment));
        while ((data = reader.readLine()) != null) {
            String current_date = LoggingProviderDisk.getLogDate(data);
            if (current_date.startsWith("|")) {
                current_date = current_date.substring(1);
            }
            if (data.contains("|") && !current_date.equals("")) {
                try {
                    if (log_date_format.parse(current_date).getTime() > date) {
                        break;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            pos += (long)data.getBytes().length;
        }
        return pos;
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
                        log_raf.seek(start);
                        log_not_at_end = true;
                        int bytes_read = 0;
                        byte[] b = new byte[32768];
                        long total_size = 0L;
                        while ((long)baos.size() < len && bytes_read >= 0) {
                            if ((long)b.length > len - total_size) {
                                b = new byte[(int)(len - total_size)];
                            }
                            bytes_read = log_raf.read(b);
                            if (startDate.equals("") && bytes_read > 0) {
                                String s = new String(b, 0, bytes_read);
                                startDate = LoggingProviderDisk.getLogDate(s);
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
                                int off = lastChunk.lastIndexOf("|");
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

    private static String getLogDate(String s) {
        String date = "";
        if (s.indexOf("|") > 0) {
            int off = s.indexOf("|");
            while (off > 0 && s.charAt(off) != '\n') {
                --off;
            }
            try {
                if (off >= 0) {
                    date = s.substring(s.indexOf("|") + 1, s.indexOf("|", s.indexOf("|") + 1)).trim();
                }
                if (date.length() > 30) {
                    date = "";
                }
            }
            catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                // empty catch block
            }
        }
        return date;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flushNow() {
        Object object = log_lock;
        synchronized (object) {
            if (ServerStatus.BG("write_to_log") && log_file_stream != null) {
                try {
                    if (log_not_at_end) {
                        log_file_stream.seek(log_file_stream.length());
                    }
                    log_not_at_end = false;
                    log_file_stream.write(baos_log_buffer.toByteArray());
                    baos_log_buffer.reset();
                    last_log_flush = System.currentTimeMillis();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void logFile(String log_data) {
        Object object = log_lock;
        synchronized (object) {
            if (ServerStatus.BG("write_to_log") && log_file_stream != null) {
                try {
                    baos_log_buffer.write(log_data.getBytes("UTF8"));
                    if (baos_log_buffer.size() > ServerStatus.IG("log_buffer_memory") || log_not_at_end || System.currentTimeMillis() - last_log_flush > 10000L) {
                        this.flushNow();
                    }
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
            logLocation = "./" + System.getProperty("appname", "CrushFTP") + ".log";
        }
        if (logLocation.equals("./") && System.getProperty("crushftp.home").equals("../")) {
            logLocation = "../" + System.getProperty("appname", "CrushFTP") + ".log";
        }
        if (logLocation.equals("./" + System.getProperty("appname", "CrushFTP") + ".log") && System.getProperty("crushftp.home").equals("../")) {
            logLocation = "../" + System.getProperty("appname", "CrushFTP") + ".log";
        }
        if (logLocation.equals("./")) {
            logLocation = "./" + System.getProperty("appname", "CrushFTP") + ".log";
        }
        if (logLocation.equals("./../../../../")) {
            logLocation = "./../../../../" + System.getProperty("appname", "CrushFTP") + ".log";
        }
        if (!logLocation.toUpperCase().endsWith(".LOG")) {
            logLocation = String.valueOf(logLocation) + System.getProperty("appname", "CrushFTP") + ".log";
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

