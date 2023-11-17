/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.asn1.annotations;

import com.hierynomus.asn1.annotations.ParsedBy;
import com.hierynomus.asn1.types.primitive.ASN1Integer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ASN1 {

    @Target(value={ElementType.FIELD, ElementType.METHOD})
    @Retention(value=RetentionPolicy.RUNTIME)
    @ParsedBy(parser=ASN1Integer.Parser.class)
    public static @interface ASN1Integer {
        public String defaultValue() default "0";
    }
}

