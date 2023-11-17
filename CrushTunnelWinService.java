/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.tunnel2.Tunnel2;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushTunnelWinService
extends AbstractService {
    @Override
    public int serviceMain(String[] args) throws ServiceException {
        Tunnel2.main(args);
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

