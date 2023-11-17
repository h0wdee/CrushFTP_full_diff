/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.smartcard;

import java.io.IOException;

public class SmartcardException
extends IOException {
    private static final long serialVersionUID = -5662773894082020210L;

    public SmartcardException(String string) {
        super(string);
    }

    public SmartcardException(String string, int n) {
        super(SmartcardException.formatError(string, n));
    }

    public SmartcardException(String string, Exception exception) {
        super(string, exception);
    }

    public static String formatError(String string, int n) {
        return string + " " + SmartcardException.errorDescription(n);
    }

    private static String errorDescription(int n) {
        byte by = (byte)(n >> 8);
        byte by2 = (byte)(n | 0xFF);
        if (by == 61) {
            return String.format("Error %1$s: %2$s bytes still available!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25088) {
            return String.format("Error %1$s: No information given!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25217) {
            return String.format("Error %1$s: Returned data may be corrupted!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25218) {
            return String.format("Error %1$s: The end of the file has been reached before the end of reading!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25219) {
            return String.format("Error %1$s: Invalid DF!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25220) {
            return String.format("Error %1$s: Selected file is not valid. File descriptor error!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25344) {
            return String.format("Error %1$s: Authentification failed. Invalid secret code or forbidden value!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (n == 25473) {
            return String.format("Error %1$s: File filled up by the last write!", Integer.toHexString(n & 0xFFFF), by2);
        }
        if (by == 99 && (byte)(by2 | 0xF0) == 192) {
            return String.format("Error %1$s: Counter provided by %2$s (exact meaning depending on the command)", Integer.toHexString(n & 0xFFFF), (byte)(by2 & 0xF));
        }
        if (n == 25857) {
            return String.format("Error %1$s: Memory failure. There have been problems in writing or reading the EEPROM/Other hardware problems", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 25985) {
            return String.format("Error %1$s: Write problem / Memory failure / Unknown mode", Integer.toHexString(n & 0xFFFF));
        }
        if (by == 103) {
            return String.format("Error %1$s: Error, Incorrect length or address range error/incorrect parameter P3 (ISO code)", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 26624) {
            return String.format("Error %1$s: The request function is not supported by the card.", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 26753) {
            return String.format("Error %1$s: Logical channel not supported", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 26754) {
            return String.format("Error %1$s: Secure messaging not supported", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 26880) {
            return String.format("Error %1$s: No successful transaction executed during session", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27009) {
            return String.format("Error %1$s: Cannot select indicated file, command not compatible with file organization", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27010) {
            return String.format("Error %1$s: Access conditions not fulfilled", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27011) {
            return String.format("Error %1$s: Secret code locked", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27012) {
            return String.format("Error %1$s: Referenced data invalidated", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27013) {
            return String.format("Error %1$s: No currently selected EF, no command to monitor / no Transaction Manager File", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27014) {
            return String.format("Error %1$s: Command not allowed (no current EF)", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27015) {
            return String.format("Error %1$s: Expected SM data objects missing", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27016) {
            return String.format("Error %1$s: SM data objects incorrect", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27136) {
            return String.format("Error %1$s: Bytes P1 and/or P2 are incorrect.", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27264) {
            return String.format("Error %1$s: The parameters in the data field are incorrect", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27265) {
            return String.format("Error %1$s: Card is blocked or command not supported", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27266) {
            return String.format("Error %1$s: File not found", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27267) {
            return String.format("Error %1$s: Record not found", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27268) {
            return String.format("Error %1$s: There is insufficient memory space in record or file", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27269) {
            return String.format("Error %1$s: Lc inconsistent with TLV structure", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27270) {
            return String.format("Error %1$s: Incorrect parameters P1P2", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27271) {
            return String.format("Error %1$s: The P3 value is not consistent with the P1 and P2 values.", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27272) {
            return String.format("Error %1$s: Referenced data not found.", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27392) {
            return String.format("Error %1$s: Incorrect reference; illegal address; Invalid P1 or P2 parameter", Integer.toHexString(n & 0xFFFF));
        }
        if (by == 108) {
            return String.format("Error %1$s: Incorrect P3 length.", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 27904) {
            return String.format("Error %1$s: Command not allowed. Invalid instruction byte (INS)", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 28160) {
            return String.format("Error %1$s: Incorrect application (CLA parameter of a command)", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 28416) {
            return String.format("Error %1$s: Checking error", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 36864) {
            return String.format("Error %1$s: Command executed without error", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37120) {
            return String.format("Error %1$s: Purse Balance error cannot perform transaction", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37122) {
            return String.format("Error %1$s: Purse Balance error", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37378) {
            return String.format("Error %1$s: Write problem / Memory failure", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37440) {
            return String.format("Error %1$s: Error, memory problem", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37892) {
            return String.format("Error %1$s: Purse selection error or invalid purse", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37894) {
            return String.format("Error %1$s: Invalid purse detected during the replacement debit step", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 37896) {
            return String.format("Error %1$s: Key file selection error", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 38912) {
            return String.format("Error %1$s: Warning/Security error", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 38916) {
            return String.format("Error %1$s: Access authorization not fulfilled", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 38918) {
            return String.format("Error %1$s: Access authorization in Debit not fulfilled for the replacement debit step", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 38944) {
            return String.format("Error %1$s: No temporary transaction key established", Integer.toHexString(n & 0xFFFF));
        }
        if (n == 38964) {
            return String.format("Error %1$s: Error, Update SSD order sequence not respected", Integer.toHexString(n & 0xFFFF));
        }
        return String.format("Error %1$s", Integer.toHexString(n & 0xFFFF));
    }
}

