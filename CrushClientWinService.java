/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.client.AgentUI;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushClientWinService
extends AbstractService {
    @Override
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

