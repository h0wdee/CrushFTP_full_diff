/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.IncompatibleAlgorithm;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SecurityPolicy;
import com.maverick.ssh.components.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSecurityPolicy
implements SecurityPolicy {
    static Logger log = LoggerFactory.getLogger(DefaultSecurityPolicy.class);
    SecurityLevel minimumSecurity;
    boolean managedSecurity;
    boolean dropSecurityAsLastResort;

    public DefaultSecurityPolicy(SecurityLevel minimumSecurity, boolean managedSecurity) {
        this(minimumSecurity, managedSecurity, false);
    }

    public DefaultSecurityPolicy(SecurityLevel minimumSecurity, boolean managedSecurity, boolean dropSecurityAsLastResort) {
        this.minimumSecurity = minimumSecurity;
        this.managedSecurity = managedSecurity;
        this.dropSecurityAsLastResort = dropSecurityAsLastResort;
    }

    @Override
    public SecurityLevel getMinimumSecurityLevel() {
        return this.minimumSecurity;
    }

    @Override
    public boolean isManagedSecurity() {
        return this.managedSecurity;
    }

    @Override
    public boolean isDropSecurityAsLastResort() {
        return this.dropSecurityAsLastResort;
    }

    @Override
    public void onIncompatibleSecurity(String host, int port, String remoteIdentification, IncompatibleAlgorithm ... reports) {
        log.error("Connection to {}:{} could not be established due to incompatible security protocols", (Object)host, (Object)port);
        log.error("The remote host identified itself as {}", (Object)remoteIdentification);
        log.error("The following algorithms could not be negotiated:");
        for (IncompatibleAlgorithm report : reports) {
            log.error("{} could not be negotiated from remote algorithms {}", (Object)report.getType().name(), (Object)Utils.csv(report.getRemoteAlgorithms()));
        }
    }
}

