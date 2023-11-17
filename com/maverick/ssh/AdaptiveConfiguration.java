/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.Utils;
import com.maverick.util.IOUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveConfiguration {
    public static final String KEY_EXCHANGE = "Kex";
    public static final String PUBLIC_KEYS = "Publickeys";
    public static final String CIPHERS = "Ciphers";
    public static final String MACS = "Macs";
    public static final String COMPRESSION = "Compressions";
    static Logger log = LoggerFactory.getLogger(AdaptiveConfiguration.class);
    private static Map<String, String> globalConfig = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    private static Map<String, Map<String, String>> patternConfigs = new TreeMap<String, Map<String, String>>(String.CASE_INSENSITIVE_ORDER);
    private static File configFile = new File(System.getProperty("maverick.configFile", "maverick.cfg"));

    public static void resetConfiguration() throws IOException {
        globalConfig = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        patternConfigs = new TreeMap<String, Map<String, String>>(String.CASE_INSENSITIVE_ORDER);
        if (configFile.exists()) {
            FileInputStream in = new FileInputStream(configFile);
            try {
                AdaptiveConfiguration.loadConfiguration(in);
            }
            finally {
                IOUtil.closeStream(in);
            }
        }
    }

    public static void saveMatchingConfiguration(String match, String keyexchange, String publickey, String cipher, String mac, String compression) throws IOException {
        if (AdaptiveConfiguration.getBoolean("LastKnownGoodConfiguration", false, match)) {
            AdaptiveConfiguration.setPatternConfig(match, KEY_EXCHANGE, keyexchange);
            AdaptiveConfiguration.setPatternConfig(match, PUBLIC_KEYS, publickey);
            AdaptiveConfiguration.setPatternConfig(match, CIPHERS, cipher);
            AdaptiveConfiguration.setPatternConfig(match, MACS, mac);
            AdaptiveConfiguration.setPatternConfig(match, COMPRESSION, compression);
            AdaptiveConfiguration.saveConfig();
        }
    }

    public static void saveConfig() throws IOException {
        StringWriter writer = new StringWriter();
        for (String key : globalConfig.keySet()) {
            writer.write(key);
            writer.write(" ");
            writer.write(globalConfig.get(key));
            writer.write(System.lineSeparator());
        }
        writer.write(System.lineSeparator());
        for (String key : patternConfigs.keySet()) {
            writer.write("Match ");
            writer.write(key);
            writer.write(System.lineSeparator());
            Map<String, String> pattern = patternConfigs.get(key);
            for (String k : pattern.keySet()) {
                writer.write(" ");
                writer.write(k);
                writer.write(" ");
                writer.write(pattern.get(k));
                writer.write(System.lineSeparator());
            }
            writer.write(System.lineSeparator());
        }
        IOUtil.writeStringToFile(configFile, writer.toString(), "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadConfiguration(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));){
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).length() <= 0) continue;
                if (line.toLowerCase().startsWith("match ")) break;
                String key = AdaptiveConfiguration.before(line);
                if (key.startsWith("#") || !Utils.isNotBlank(key)) continue;
                String value = AdaptiveConfiguration.after(line);
                AdaptiveConfiguration.setGlobalConfig(key, value);
            }
            while (line != null && line.toLowerCase().startsWith("match ")) {
                String matchValue = AdaptiveConfiguration.after(line);
                while ((line = reader.readLine()) != null && !line.toLowerCase().startsWith("match ")) {
                    String key = AdaptiveConfiguration.before(line = line.trim());
                    if (key.startsWith("#") || !Utils.isNotBlank(key)) continue;
                    String value = AdaptiveConfiguration.after(line);
                    AdaptiveConfiguration.setPatternConfig(matchValue, key, value);
                }
            }
        }
    }

    private static String before(String str) {
        String[] vals = str.trim().split("\\s+");
        if (vals.length > 0) {
            return vals[0];
        }
        throw new IllegalArgumentException(str + " does not contain elements separated by whitespace");
    }

    private static String after(String str) {
        String[] vals = str.trim().split("\\s+");
        if (vals.length > 1) {
            return vals[1];
        }
        throw new IllegalArgumentException(str + " does not contain elements separated by whitespace");
    }

    public static String createAlgorithmList(String supportedList, String key, String ident, String hostname, String ... ignores) {
        List<String> supported = Arrays.asList(supportedList.split(","));
        String locallist = AdaptiveConfiguration.getPatternConfig(key, hostname, hostname);
        if (Utils.isBlank(locallist)) {
            locallist = AdaptiveConfiguration.getGlobalConfig(key);
        }
        if (Utils.isBlank(locallist)) {
            locallist = supportedList;
        }
        List<String> ignoreAlgs = Arrays.asList(ignores);
        ArrayList<String> results = new ArrayList<String>();
        for (String algorithm : locallist.split(",")) {
            if (!supported.contains(algorithm) || ignoreAlgs.contains(algorithm)) continue;
            results.add(algorithm);
        }
        return Utils.csv(results);
    }

    public static String createAlgorithmList(ComponentFactory<?> factory, String key, String contextPreference, String ident, String hostname, String ... ignores) {
        String locallist = factory.filter(AdaptiveConfiguration.getPatternConfig(key, hostname, ident), new String[0]);
        if (Utils.isBlank(locallist)) {
            locallist = factory.filter(AdaptiveConfiguration.getGlobalConfig(key), new String[0]);
        }
        if (Utils.isBlank(locallist)) {
            locallist = factory.list(contextPreference);
        }
        List<String> ignoreAlgs = Arrays.asList(ignores);
        ArrayList<String> results = new ArrayList<String>();
        for (String algorithm : locallist.split(",")) {
            if (ignoreAlgs.contains(algorithm)) continue;
            results.add(algorithm);
        }
        return Utils.csv(results);
    }

    public static String getPatternConfig(String key, String ... values) {
        for (String value : values) {
            for (String pattern : patternConfigs.keySet()) {
                String result;
                if (!value.matches(pattern) || (result = patternConfigs.get(pattern).get(key)) == null) continue;
                if (log.isDebugEnabled()) {
                    log.debug("Matched {} from pattern configuration {} [{}] with value {}", new Object[]{key, value, pattern, result});
                }
                return result;
            }
            String result = AdaptiveConfiguration.getSystemProperty(AdaptiveConfiguration.formatKey(value, key));
            if (result == null) continue;
            return result;
        }
        return AdaptiveConfiguration.getGlobalConfig(key);
    }

    private static String formatKey(String key1, String key2) {
        StringBuilder str = new StringBuilder();
        str.append(key1);
        str.append(".");
        str.append(key2);
        return str.toString();
    }

    private static String getSystemProperty(String key) {
        String result = System.getProperty(key);
        if (result != null && log.isDebugEnabled()) {
            log.debug("Matched {} from system property with value {}", (Object)key, (Object)result);
        }
        return result;
    }

    public static void setPatternConfig(String pattern, String key, String val) {
        if (!patternConfigs.containsKey(pattern)) {
            patternConfigs.put(pattern, new TreeMap(String.CASE_INSENSITIVE_ORDER));
        }
        patternConfigs.get(pattern).put(key, val);
    }

    public static void setPatternConfig(String pattern, String key, boolean val) {
        AdaptiveConfiguration.setPatternConfig(pattern, key, String.valueOf(val));
    }

    public static void setPatternConfig(String pattern, String key, int val) {
        AdaptiveConfiguration.setPatternConfig(pattern, key, String.valueOf(val));
    }

    public static void setPatternConfig(String pattern, String key, long val) {
        AdaptiveConfiguration.setPatternConfig(pattern, key, String.valueOf(val));
    }

    public static String getGlobalConfig(String key) {
        String result = globalConfig.get(key);
        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("Matched {} from global configuration with value {}", (Object)key, (Object)result);
            }
            return result;
        }
        return AdaptiveConfiguration.getSystemProperty(AdaptiveConfiguration.formatKey("maverick", key));
    }

    public static void setGlobalConfig(String key, String val) {
        globalConfig.put(key, val);
    }

    public static void setGlobalConfig(String key, int val) {
        globalConfig.put(key, String.valueOf(val));
    }

    public static void setGlobalConfig(String key, long val) {
        globalConfig.put(key, String.valueOf(val));
    }

    public static void setGlobalConfig(String key, boolean value) {
        AdaptiveConfiguration.setGlobalConfig(key, String.valueOf(value));
    }

    public static String getIdent(String remoteIdentification) {
        if (remoteIdentification.startsWith("SSH")) {
            String[] elements = remoteIdentification.split("-");
            if (elements.length == 3) {
                return elements[2].trim();
            }
            if (elements.length > 3) {
                String ident = elements[2];
                int idx = ident.indexOf(32);
                if (idx > -1) {
                    ident = ident.substring(0, idx);
                }
                return ident;
            }
        }
        log.error("Remote identification cannot be parsed to capture the remote nodes identity [{}]", (Object)remoteIdentification);
        return "<unknown>";
    }

    public static boolean getBoolean(String key, boolean defaultValue, String ... match) {
        String result = AdaptiveConfiguration.getPatternConfig(key, match);
        if (result == null) {
            return AdaptiveConfiguration.getBooleanOrDefault(key, defaultValue);
        }
        return AdaptiveConfiguration.parseBoolean(result);
    }

    private static boolean parseBoolean(String val) {
        switch (val.toUpperCase()) {
            case "YES": 
            case "Y": 
            case "TRUE": {
                return true;
            }
        }
        return false;
    }

    public static void setBoolean(String key, String pattern) {
        AdaptiveConfiguration.setPatternConfig(pattern, key, Boolean.TRUE.toString());
    }

    public static void setBoolean(String key, String pattern, Boolean val) {
        AdaptiveConfiguration.setPatternConfig(pattern, key, val.toString());
    }

    public static boolean getBooleanOrDefault(String key, boolean defaultValue) {
        String result = AdaptiveConfiguration.getGlobalConfig(key);
        if (result != null) {
            return AdaptiveConfiguration.parseBoolean(result);
        }
        return defaultValue;
    }

    public static long getLong(String key, Long defaultValue, String ... match) {
        String result = AdaptiveConfiguration.getPatternConfig(key, match);
        if (result == null) {
            return AdaptiveConfiguration.getLongOrDefault(key, defaultValue);
        }
        return Long.parseLong(result);
    }

    private static long getLongOrDefault(String key, long defaultValue) {
        String result = AdaptiveConfiguration.getGlobalConfig(key);
        if (result != null) {
            return Long.parseLong(result);
        }
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue, String ... match) {
        String result = AdaptiveConfiguration.getPatternConfig(key, match);
        if (result == null) {
            return AdaptiveConfiguration.getIntOrDefault(key, defaultValue);
        }
        return Integer.parseInt(result);
    }

    private static int getIntOrDefault(String key, int defaultValue) {
        String result = AdaptiveConfiguration.getGlobalConfig(key);
        if (result != null) {
            return Integer.parseInt(result);
        }
        return defaultValue;
    }

    public static long getByteSize(String key, String defaultValue, String ... match) {
        String result = AdaptiveConfiguration.getPatternConfig(key, match);
        if (result != null) {
            return IOUtil.fromByteSize(result);
        }
        return AdaptiveConfiguration.getByteSizeOrDefault(key, defaultValue);
    }

    private static long getByteSizeOrDefault(String key, String defaultValue) {
        String result = AdaptiveConfiguration.getGlobalConfig(key);
        if (result != null) {
            return IOUtil.fromByteSize(result);
        }
        return IOUtil.fromByteSize(defaultValue);
    }

    public static String getProperty(String key, String defaultValue, String ... match) {
        String result = AdaptiveConfiguration.getPatternConfig(key, match);
        if (result != null) {
            return result;
        }
        result = AdaptiveConfiguration.getGlobalConfig(key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    static {
        try {
            AdaptiveConfiguration.resetConfiguration();
        }
        catch (IOException e) {
            log.error("Failed to initialize AdaptiveConfiguration", (Throwable)e);
        }
    }
}

