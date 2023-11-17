/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import java.io.IOException;
import java.util.Properties;

public class LoggingProvider {
    public static Object log_lock = new Object();
    public String newTags = "(ERROR)(START)(STOP)(QUIT_SERVER)(RUN_SERVER)(SERVER),(ALERT),(GENERAL)";

    public void checkForLogRoll() {
    }

    public void append_log(String log_data, String check_data, boolean file_only) {
    }

    public Properties getLogSegment(long start, long len, String log_file) throws IOException {
        return null;
    }

    public void shutdown() throws IOException {
    }

    public void checkLogPath() {
    }

    public static boolean checkFilters(String filtersStr, String log_data) {
        if (filtersStr.trim().equals("")) {
            return true;
        }
        String[] filters = filtersStr.split(",");
        int x = 0;
        while (x < filters.length) {
            if (Common.do_search(filters[x], log_data, false, 0)) {
                return false;
            }
            ++x;
        }
        return true;
    }
}

