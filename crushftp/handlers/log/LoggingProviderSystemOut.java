/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers.log;

import crushftp.handlers.LoggingProvider;
import crushftp.server.ServerStatus;
import java.io.IOException;
import java.util.Properties;

public class LoggingProviderSystemOut
extends LoggingProvider {
    @Override
    public void checkForLogRoll() {
    }

    @Override
    public void append_log(String log_data, String check_data, boolean file_only) {
        block9: {
            block8: {
                block7: {
                    if (!ServerStatus.BG("write_to_log")) {
                        return;
                    }
                    log_data = String.valueOf(log_data.trim()) + "\r\n";
                    boolean ok = true;
                    if (log_data.indexOf("/WebInterface/") >= 0) {
                        if (ServerStatus.SG("log_allow_str").indexOf("(WEBINTERFACE)") < 0) {
                            ok = false;
                        }
                    }
                    if (!ok) break block7;
                    if (ServerStatus.SG("log_allow_str").indexOf("(" + check_data + ")") >= 0) break block8;
                }
                if (this.newTags.indexOf("(" + check_data + ")") < 0) break block9;
            }
            if (LoggingProviderSystemOut.checkFilters(ServerStatus.SG("filter_log_text"), log_data)) {
                System.out.print(String.valueOf(check_data) + "|" + log_data);
            }
        }
    }

    @Override
    public Properties getLogSegment(long start, long len, String log_file) throws IOException {
        Properties log = new Properties();
        log.put("log_start_date", "");
        log.put("log_end_date", "");
        log.put("log_start", "0");
        log.put("log_end", "0");
        log.put("log_max", "0");
        log.put("log_data", "");
        log.put("log_segment", "");
        return log;
    }

    @Override
    public void shutdown() throws IOException {
    }
}

