/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.client.CrushDrive;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushDriveWinService
extends AbstractService {
    @Override
    public int serviceMain(String[] args) throws ServiceException {
        CrushDrive.main(args);
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

