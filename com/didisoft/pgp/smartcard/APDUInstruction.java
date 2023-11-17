/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.smartcard;

class APDUInstruction {
    static final byte CLA_ISO7816 = 0;
    static final byte CLA_CHAINING = 16;
    static final byte CLA_GP = -128;
    static final byte CLA_SECURE = -124;
    static final byte Delete = -28;
    static final byte GetData = -54;
    static final byte GetStatus = -14;
    static final byte Install = -26;
    static final byte Load = -24;
    static final byte ManageChannel = 112;
    static final byte PutKey = -40;
    static final byte Select = -92;
    static final byte SetStatus = -16;
    static final byte StoreData = -30;
    static final byte InitializeUpdate = 80;
    static final byte ExternalAuthenticate = -126;
    static final byte P1_INSTALL_AND_MAKE_SELECTABLE = 12;
    static final byte P1_INSTALL_FOR_INSTALL = 4;
    static final byte P1_INSTALL_FOR_LOAD = 2;
    static final byte P1_MORE_BLOCKS = 0;
    static final byte P1_LAST_BLOCK = -128;

    APDUInstruction() {
    }
}

