/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.mail.MailSessionDefinition;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface MailSessionDefinitions {
    public MailSessionDefinition[] value();
}

