/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.boris.winrun4j.AbstractService
 *  org.boris.winrun4j.ServiceException
 */
import com.crushftp.client.CrushSyncUI;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushSyncWinService
extends AbstractService {
    public int serviceMain(String[] args) throws ServiceException {
        CrushSyncUI.main(new String[]{"./", "-d"});
        CrushSyncUI.ignore_quit = true;
        while (!this.shutdown) {
            try {
                Thread.sleep(1000L);
                CrushSyncUI.pong = System.currentTimeMillis();
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        System.exit(0);
        return 0;
    }
}

