/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.MessagingException;
import javax.mail.Quota;

public interface QuotaAwareStore {
    public Quota[] getQuota(String var1) throws MessagingException;

    public void setQuota(Quota var1) throws MessagingException;
}

