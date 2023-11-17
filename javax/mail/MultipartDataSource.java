/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataSource
 */
package javax.mail;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;

public interface MultipartDataSource
extends DataSource {
    public int getCount();

    public BodyPart getBodyPart(int var1) throws MessagingException;
}

