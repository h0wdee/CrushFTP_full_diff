/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.boris.winrun4j.AbstractService
 *  org.boris.winrun4j.ServiceException
 */
import crushftp.server.ServerStatus;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushFTPWinService
extends AbstractService {
    /*
     * Unable to fully structure code
     */
    public int serviceMain(String[] args) throws ServiceException {
        CrushFTP.main(args);
        while (!this.shutdown) {
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException var2_2) {
                // empty catch block
            }
        }
        start = System.currentTimeMillis();
        if (true) ** GOTO lbl18
        do {
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException var4_4) {
                // empty catch block
            }
            if (ServerStatus.siVG("running_tasks").size() <= 0) break;
        } while (System.currentTimeMillis() - start < ServerStatus.LG("active_jobs_shutdown_wait_secs") * 1000L);
        System.exit(0);
        return 0;
    }
}

