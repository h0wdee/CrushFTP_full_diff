/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.paths;

import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.paths.PathResolveException;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.StatusHandler;

public interface PathResolver {
    public static final PathResolver LOCAL = new PathResolver(){

        @Override
        public SmbPath resolve(Session session, SMB2Packet responsePacket, SmbPath smbPath) {
            return smbPath;
        }

        @Override
        public SmbPath resolve(Session session, SmbPath smbPath) {
            return smbPath;
        }

        @Override
        public StatusHandler statusHandler() {
            return StatusHandler.SUCCESS;
        }
    };

    public SmbPath resolve(Session var1, SMB2Packet var2, SmbPath var3) throws PathResolveException;

    public SmbPath resolve(Session var1, SmbPath var2) throws PathResolveException;

    public StatusHandler statusHandler();
}

