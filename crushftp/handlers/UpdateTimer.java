/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.util.Date;

public class UpdateTimer
implements Runnable {
    public int sleep_interval = 500;
    ServerStatus caller;
    String type = "";
    String message = "";

    public UpdateTimer(ServerStatus caller, int sleep_interval, String type, String message) {
        this.type = type;
        this.caller = caller;
        this.sleep_interval = sleep_interval;
        this.message = message;
    }

    @Override
    public void run() {
        Thread this_thread = Thread.currentThread();
        if (this_thread.getName().indexOf(":") < 0) {
            this_thread.setName(String.valueOf(this_thread.getName()) + ":");
        }
        while (true) {
            try {
                while (true) {
                    long start_time = new Date().getTime();
                    try {
                        this.caller.update_now(this.message);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                    this_thread = Thread.currentThread();
                    if (this_thread.getName().length() > 300) {
                        this_thread.setName(String.valueOf(this_thread.getName().substring(0, this_thread.getName().indexOf(":") + 1)) + (new Date().getTime() - start_time));
                    } else {
                        this_thread.setName(String.valueOf(this_thread.getName().substring(0, this_thread.getName().lastIndexOf(":") + 1)) + (new Date().getTime() - start_time));
                    }
                    Thread.sleep(this.sleep_interval);
                }
            }
            catch (InterruptedException e) {
                try {
                    Thread.sleep(5000L);
                    continue;
                }
                catch (InterruptedException ee) {
                    return;
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, String.valueOf(this_thread.getName()) + ":" + this.message);
                Log.log("SERVER", 0, e);
                continue;
            }
            break;
        }
    }
}

