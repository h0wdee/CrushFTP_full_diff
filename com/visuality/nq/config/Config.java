/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.config;

import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

public final class Config {
    private static String ConfigFileName = "properties/config.properties";
    private final Properties prop = new Properties();
    private static FileInputStream input = null;
    private Map paramCach = new HashMap();
    private boolean addProperties = false;
    public static Config jnq;
    public static final String DO_NOT_USE_HOSTNAME_IN_AUTH = "DO_NOT_USE";
    public static final int DEFAULT_URI_PORT = 5025;
    private static boolean inDomain;
    private static boolean server;
    private static boolean client;
    private static String domainName;
    private static InetAddress[] winsServers;
    private static InetAddress[] dnsServers;
    private static boolean wasSet;

    public Config() {
    }

    public static Map getConfigInfo() {
        TreeMap<String, Object> map = new TreeMap<String, Object>();
        for (Param param : Config.jnq.paramCach.values()) {
            if (null == param) continue;
            map.put(param.paramsName, param.value);
        }
        return map;
    }

    public Config(String fileName) {
        try {
            Config.setInput(new FileInputStream(fileName));
            if (null != input) {
                this.prop.load(input);
            }
        }
        catch (IOException e) {
            TraceLog.get().error("file not found : ", fileName);
            input = null;
        }
    }

    private static void setInput(FileInputStream in) {
        input = in;
    }

    public ArrayList getParametersList() {
        ArrayList<String> list = new ArrayList<String>();
        for (Param p : this.paramCach.values()) {
            if (null == p) continue;
            list.add(p.paramsName);
        }
        if (0 == list.size()) {
            list = null;
        }
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getInt(String key) throws NqException {
        Integer value;
        Map map = this.paramCach;
        synchronized (map) {
            Param param = (Param)this.paramCach.get(key);
            try {
                value = null != param ? Integer.valueOf((Integer)param.value) : (Integer)this.prop.get(key);
            }
            catch (ClassCastException e) {
                throw new NqException("Illegal value for property " + key + "; value = " + param.value, -20);
            }
        }
        if (null == value) {
            throw new NqException("Property " + key + " not found", -20);
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getString(String key) throws NqException {
        String value = null;
        Map map = this.paramCach;
        synchronized (map) {
            Param param = (Param)this.paramCach.get(key);
            value = null != param ? (String)param.value : (String)this.prop.get(key);
        }
        if (null == value) {
            throw new NqException("Property " + key + " not found", -20);
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean getBool(String key) throws NqException {
        Boolean value = null;
        Map map = this.paramCach;
        synchronized (map) {
            Param param = (Param)this.paramCach.get(key);
            try {
                value = null != param ? Boolean.valueOf((Boolean)param.value) : (Boolean)this.prop.get(key);
            }
            catch (ClassCastException e) {
                throw new NqException("Illegal value for property " + key + "; value = " + param.value, -20);
            }
        }
        if (null == value) {
            throw new NqException("Property " + key + " not found", -20);
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object getNE(String key) {
        Object value = null;
        Map map = this.paramCach;
        synchronized (map) {
            Param param = (Param)this.paramCach.get(key);
            if (null != param) {
                value = null == param.value ? param.getDefaultValue() : param.value;
            }
        }
        return value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setNE(String key, Object value) {
        Param param;
        Map map = this.paramCach;
        synchronized (map) {
            param = (Param)this.paramCach.get(key);
            if (null == param) {
                if (!this.addProperties) {
                    return;
                }
                param = new Param(key, value.getClass(), value);
            }
        }
        param.value = value;
        this.paramCach.put(key, param);
        if (this.addProperties) {
            this.prop.put(key, value.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(String key, Object value) throws NqException {
        Param param;
        Map map = this.paramCach;
        synchronized (map) {
            param = (Param)this.paramCach.get(key);
            if (null == param) {
                if (!this.addProperties) {
                    throw new NqException("parameter not found : " + key);
                }
                param = new Param(key, value.getClass(), value);
            }
        }
        param.value = value;
        this.paramCach.put(key, param);
        if (this.addProperties) {
            this.prop.put(key, value.toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        if (null != file && null != file.getParentFile()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            this.prop.store(fos, "Auto saved configuration");
        }
        finally {
            if (null != fos) {
                fos.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDefault(String key) throws NqException {
        Param param;
        Map map = this.paramCach;
        synchronized (map) {
            param = (Param)this.paramCach.get(key);
            if (null == param) {
                throw new NqException("parameter not found : " + key);
            }
        }
        param.value = param.defaultValue;
        this.paramCach.put(key, param);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDefaultAll() throws NqException {
        Map map = this.paramCach;
        synchronized (map) {
            for (Param param : this.paramCach.values()) {
                param.value = param.defaultValue;
            }
        }
    }

    public static boolean isServer() {
        return server;
    }

    public static boolean isClient() {
        return client;
    }

    private static InetAddress[] parseIpList(String ipList) throws NqException {
        int startIdx = 0;
        Vector<InetAddress> ips = new Vector<InetAddress>();
        while (startIdx < ipList.length()) {
            int endIdx = ipList.indexOf(";", startIdx);
            if (-1 == endIdx) {
                endIdx = ipList.length();
            }
            String nextIp = ipList.substring(startIdx, endIdx);
            try {
                if (IpAddressHelper.isIpAddress(nextIp)) {
                    ips.add(InetAddress.getByName(nextIp));
                }
            }
            catch (UnknownHostException e) {
                TraceLog.get().error("UnknownHostException = ", e);
            }
            startIdx = endIdx + 1;
        }
        if (ips.size() == 0) {
            return null;
        }
        InetAddress[] res = new InetAddress[ips.size()];
        return ips.toArray(res);
    }

    private static void setup() {
        wasSet = true;
        inDomain = false;
        try {
            domainName = jnq.getString("DEFAULTDOMAIN");
            winsServers = Config.parseIpList(jnq.getString("WINS"));
            dnsServers = Config.parseIpList(jnq.getString("DNS"));
        }
        catch (Exception e) {
            TraceLog.get().error("Internal error in Config::setup()");
        }
    }

    public static InetAddress[] getWins() {
        if (!wasSet) {
            Config.setup();
        }
        return winsServers;
    }

    public static InetAddress[] getDns() {
        if (!wasSet) {
            Config.setup();
        }
        return dnsServers;
    }

    public static boolean isInDomain() {
        if (!wasSet) {
            Config.setup();
        }
        return inDomain;
    }

    public static String getDomainName() {
        if (!wasSet) {
            Config.setup();
        }
        return domainName.length() == 0 ? "workgroup" : domainName;
    }

    public void setAddProperties(boolean addProperties) {
        this.addProperties = addProperties;
    }

    public Map getParamCache() {
        return this.paramCach;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static {
        Config config = jnq = new Config();
        synchronized (config) {
            Config.jnq.paramCach.put("ISSERVER", new Param("ISSERVER", Boolean.class, false));
            Config.jnq.paramCach.put("ISCLIENT", new Param("ISCLIENT", Boolean.class, true));
            Config.jnq.paramCach.put("DEFAULTUSER", new Param("DEFAULTUSER", String.class, "user"));
            Config.jnq.paramCach.put("DEFAULTPASS", new Param("DEFAULTPASS", String.class, "password"));
            Config.jnq.paramCach.put("DEFAULTDOMAIN", new Param("DEFAULTDOMAIN", String.class, "workgroup"));
            Config.jnq.paramCach.put("MAXSECURITYLEVEL", new Param("MAXSECURITYLEVEL", Integer.class, 4));
            Config.jnq.paramCach.put("MINSECURITYLEVEL", new Param("MINSECURITYLEVEL", Integer.class, 0));
            Config.jnq.paramCach.put("SIGNINGPOLICY", new Param("SIGNINGPOLICY", Boolean.class, true));
            Config.jnq.paramCach.put("ISUSEINTERNALONLY", new Param("ISUSEINTERNALONLY", Boolean.class, false));
            Config.jnq.paramCach.put("ISUSEEXTERNALONLY", new Param("ISUSEEXTERNALONLY", Boolean.class, false));
            Config.jnq.paramCach.put("BINDWELLKNOWNPORTS", new Param("BINDWELLKNOWNPORTS", Boolean.class, true));
            Config.jnq.paramCach.put("INTERNALNAMESERVICEPORT", new Param("INTERNALNAMESERVICEPORT", Integer.class, 0));
            Config.jnq.paramCach.put("INTERNALDATAGRAMSERVICEPORT", new Param("INTERNALDATAGRAMSERVICEPORT", Integer.class, 0));
            Config.jnq.paramCach.put("DISABLESESSIONSERVICE", new Param("DISABLESESSIONSERVICE", Boolean.class, false));
            Config.jnq.paramCach.put("DISABLENETBIOSTRANSPORT", new Param("DISABLENETBIOSTRANSPORT", Boolean.class, false));
            Config.jnq.paramCach.put("TRANSPORTTIMEOUT", new Param("TRANSPORTTIMEOUT", Integer.class, 5000));
            Config.jnq.paramCach.put("ENABLENONSECUREAUTHMETHODS", new Param("ENABLENONSECUREAUTHMETHODS", Boolean.class, false));
            Config.jnq.paramCach.put("REGISTERHOST", new Param("REGISTERHOST", Boolean.class, false));
            Config.jnq.paramCach.put("DFSCACHETTL", new Param("DFSCACHETTL", Integer.class, 0));
            Config.jnq.paramCach.put("DFSENABLE", new Param("DFSENABLE", Boolean.class, true));
            Config.jnq.paramCach.put("SIDCACHETTL", new Param("SIDCACHETTL", Integer.class, 1440));
            Config.jnq.paramCach.put("RETRYCOUNT", new Param("RETRYCOUNT", Integer.class, 3));
            Config.jnq.paramCach.put("RETRYTIMEOUT", new Param("RETRYTIMEOUT", Integer.class, 0));
            Config.jnq.paramCach.put("WINS", new Param("WINS", String.class, ""));
            Config.jnq.paramCach.put("DNS", new Param("DNS", String.class, ""));
            Config.jnq.paramCach.put("DNSDOMAIN", new Param("DNSDOMAIN", String.class, ""));
            Config.jnq.paramCach.put("HOSTNAME", new Param("HOSTNAME", String.class, ""));
            Config.jnq.paramCach.put("DNS_TIMEOUT", new Param("DNS_TIMEOUT", Integer.class, 2000));
            Config.jnq.paramCach.put("UNICAST_RETRY_COUNT", new Param("UNICAST_RETRY_COUNT", Integer.class, 2));
            Config.jnq.paramCach.put("MULTICAST_RESOLUTION_TIMEOUT", new Param("MULTICAST_RESOLUTION_TIMEOUT", Integer.class, 2000));
            Config.jnq.paramCach.put("UNICAST_RESOLUTION_TIMEOUT", new Param("UNICAST_RESOLUTION_TIMEOUT", Integer.class, 2000));
            Config.jnq.paramCach.put("MULTICAST_DC_RESOLUTION_TIMEOUT", new Param("MULTICAST_DC_RESOLUTION_TIMEOUT", Integer.class, 2500));
            Config.jnq.paramCach.put("UNICAST_DC_RESOLUTION_TIMEOUT", new Param("UNICAST_DC_RESOLUTION_TIMEOUT", Integer.class, 2000));
            Config.jnq.paramCach.put("OLDRESOLVINGSETTINGS", new Param("OLDRESOLVINGSETTINGS", Boolean.class, true));
            Config.jnq.paramCach.put("KDC", new Param("KDC", String.class, ""));
            Config.jnq.paramCach.put("REALM", new Param("REALM", String.class, ""));
            Config.jnq.paramCach.put("USECACHE", new Param("USECACHE", Boolean.class, false));
            Config.jnq.paramCach.put("STOREKEY", new Param("STOREKEY", Boolean.class, false));
            Config.jnq.paramCach.put("TICKETTTL", new Param("TICKETTTL", Integer.class, 0));
            Config.jnq.paramCach.put("WSDWAITTIME", new Param("WSDWAITTIME", Integer.class, 3000));
            Config.jnq.paramCach.put("SMBTIMEOUT", new Param("SMBTIMEOUT", Integer.class, 15000));
            Config.jnq.paramCach.put("CONNECTION_TIMEOUT", new Param("CONNECTION_TIMEOUT", Integer.class, 2000));
            Config.jnq.paramCach.put("CLEANUP_THREAD_SERVER_IDLE_PERIOD", new Param("CLEANUP_THREAD_SERVER_IDLE_PERIOD", Integer.class, 15));
            Config.jnq.paramCach.put("CLEANUP_THREAD_SERVER_ENABLED", new Param("CLEANUP_THREAD_SERVER_ENABLED", Boolean.class, false));
            Config.jnq.paramCach.put("LOGTHRESHOLD", new Param("LOGTHRESHOLD", Integer.class, 0));
            Config.jnq.paramCach.put("LOGFILE", new Param("LOGFILE", String.class, "default.log"));
            Config.jnq.paramCach.put("LOGTOCONSOLE", new Param("LOGTOCONSOLE", Boolean.class, false));
            Config.jnq.paramCach.put("LOGTOFILE", new Param("LOGTOFILE", Boolean.class, false));
            Config.jnq.paramCach.put("LOGMAXRECORDSINFILE", new Param("LOGMAXRECORDSINFILE", Integer.class, 10000));
            Config.jnq.paramCach.put("LOGMAXFILES", new Param("LOGMAXFILES", Integer.class, 20));
            Config.jnq.paramCach.put("ENABLECAPTUREPACKETS", new Param("ENABLECAPTUREPACKETS", Boolean.class, false));
            Config.jnq.paramCach.put("CAPTUREFILE", new Param("CAPTUREFILE", String.class, "capture.pcap"));
            Config.jnq.paramCach.put("CAPTUREMAXRECORDSINFILE", new Param("CAPTUREMAXRECORDSINFILE", Integer.class, 10000));
            Config.jnq.paramCach.put("CAPTUREMAXFILES", new Param("CAPTUREMAXFILES", Integer.class, 50));
            Config.jnq.paramCach.put("URIPORT", new Param("URIPORT", Integer.class, 5025));
            try {
                input = new FileInputStream(ConfigFileName);
            }
            catch (IOException e) {
                input = null;
            }
            if (null != input) {
                Iterator iteratorValues = Config.jnq.paramCach.values().iterator();
                try {
                    Config.jnq.prop.load(input);
                    while (iteratorValues.hasNext()) {
                        String paramName;
                        String paramValue;
                        Param p = (Param)iteratorValues.next();
                        if (null == p || null == (paramValue = Config.jnq.prop.getProperty(paramName = p.paramsName)) || paramValue.equals("")) continue;
                        p.updateParam(paramValue);
                        Config.jnq.paramCach.put(paramName, p);
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        wasSet = false;
    }

    public static final class Param {
        protected String paramsName;
        protected Class type;
        protected Object value;
        private Object defaultValue;

        protected Param(String paramName, Class type, Object value) {
            this.paramsName = paramName;
            this.type = type;
            this.value = value;
            this.defaultValue = value;
        }

        public String toString() {
            return "Param [paramsName=" + this.paramsName + ", value=" + this.value + "]";
        }

        protected void updateParam(String value) {
            try {
                if (String.class == this.type) {
                    this.value = value;
                } else if (Boolean.class == this.type) {
                    this.value = Boolean.valueOf(value);
                } else if (Integer.class == this.type) {
                    this.value = Integer.valueOf(value);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        public Object getDefaultValue() {
            return this.defaultValue;
        }
    }
}

