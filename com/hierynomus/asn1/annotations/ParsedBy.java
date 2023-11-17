/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.annotations;

import com.hierynomus.asn1.ASN1Parser;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.ANNOTATION_TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface ParsedBy {
    public Class<? extends ASN1Parser> parser();
}

