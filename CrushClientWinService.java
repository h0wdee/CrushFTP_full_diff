/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.boris.winrun4j.AbstractService
 *  org.boris.winrun4j.ServiceException
 */
import com.crushftp.client.AgentUI;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushClientWinService
extends AbstractService {
    public int serviceMain(String[] args) throws ServiceException {
        AgentUI.main(args);
        while (!this.shutdown) {
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        System.exit(0);
        return 0;
    }
}

