/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import com.crushftp.tunnel3.StreamController;
import com.crushftp.tunnel3.StreamReader;
import com.crushftp.tunnel3.StreamWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class StreamTuner
implements Runnable {
    StreamController sc = null;
    public Vector speedHistory = new Vector();
    public Properties speedHistoryIp = new Properties();
    int stableSeconds = 5;
    int channelRampUp = 1;
    int minFastSpeed = 100;
    int minSlowSpeed = 10;
    double speedThreshold = 0.6;
    int closeInRequests = 0;
    long baseSpeedInIntervals = 0L;
    long baseSpeedOutIntervals = 0L;
    float baseSpeedOut = 0.0f;
    float baseSpeedIn = 0.0f;
    boolean active = true;
    int noIncomingCount = 0;

    public StreamTuner(StreamController sc) {
        this.sc = sc;
    }

    @Override
    public void run() {
        this.active = true;
        Thread.currentThread().setName("Tunnel Connection Tunner:" + this.sc);
        this.noIncomingCount = 0;
        while (this.sc.isActive()) {
            this.stableSeconds = Integer.parseInt(this.sc.tunnel.getProperty("stableSeconds", "5"));
            this.channelRampUp = Integer.parseInt(this.sc.tunnel.getProperty("channelRampUp", "1"));
            this.minFastSpeed = Integer.parseInt(this.sc.tunnel.getProperty("minFastSpeed", "100"));
            this.minSlowSpeed = Integer.parseInt(this.sc.tunnel.getProperty("minSlowSpeed", "10"));
            this.speedThreshold = (double)Integer.parseInt(this.sc.tunnel.getProperty("speedThreshold", "60")) / 100.0;
            try {
                this.addOut();
                this.addIn();
                Thread.sleep(1000L);
                long intervals = 0L;
                while (this.sc.isActive()) {
                    Properties p;
                    int x;
                    ++intervals;
                    this.calcSpeed();
                    if (this.baseSpeedInIntervals++ == (long)this.stableSeconds) {
                        float speedIn = 0.0f;
                        x = 0;
                        while (x < this.speedHistory.size()) {
                            p = (Properties)this.speedHistory.elementAt(x);
                            speedIn += Float.parseFloat(p.getProperty("speedIn"));
                            ++x;
                        }
                        this.baseSpeedIn = speedIn / (float)this.speedHistory.size();
                        this.sc.msg("Tunnel3:stableSeconds=" + this.stableSeconds + ":channelRampUp=" + this.channelRampUp + ":minFastSpeed=" + this.minFastSpeed + ":minSlowSpeed=" + this.minSlowSpeed + ":speedThreshold=" + this.speedThreshold + ":Base Speed In=" + this.baseSpeedIn / 1024.0f + "K/sec  Out=" + this.baseSpeedOut / 1024.0f + "K/sec");
                        if (this.baseSpeedIn < 5.0f) {
                            this.baseSpeedInIntervals = 0L;
                        }
                    }
                    if (this.baseSpeedOutIntervals++ == (long)this.stableSeconds) {
                        float speedOut = 0.0f;
                        x = 0;
                        while (x < this.speedHistory.size()) {
                            p = (Properties)this.speedHistory.elementAt(x);
                            speedOut += Float.parseFloat(p.getProperty("speedOut"));
                            ++x;
                        }
                        this.baseSpeedOut = speedOut / (float)this.speedHistory.size();
                        this.sc.msg("Tunnel3:stableSeconds=" + this.stableSeconds + ":channelRampUp=" + this.channelRampUp + ":minFastSpeed=" + this.minFastSpeed + ":minSlowSpeed=" + this.minSlowSpeed + ":speedThreshold=" + this.speedThreshold + ":Base Speed In=" + this.baseSpeedIn / 1024.0f + "K/sec  Out=" + this.baseSpeedOut / 1024.0f + "K/sec");
                        if (this.baseSpeedOut < 5.0f) {
                            this.baseSpeedOutIntervals = 0L;
                        }
                    }
                    int loops = this.channelRampUp;
                    if (intervals == (long)(this.stableSeconds + 1)) {
                        loops *= 2;
                    }
                    if (this.baseSpeedIn > 0.0f) {
                        float speed = this.getSpeedAverage(this.speedHistory, "speedIn", "countIn", 10);
                        if (speed / 1024.0f > (float)this.minFastSpeed && (double)speed > (double)this.baseSpeedIn * this.speedThreshold) {
                            if (this.sc.incoming.size() < Math.abs(Integer.parseInt(this.sc.tunnel.getProperty("channelsInMax", "1")))) {
                                this.sc.msg("Tunnel3:Fast incoming speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedIn / 1024.0f + "K/sec, adding " + loops + " channels.");
                                int x2 = 0;
                                while (x2 < loops) {
                                    this.addIn();
                                    ++x2;
                                }
                            }
                        } else if (speed / 1024.0f < (float)this.minSlowSpeed && this.sc.incoming.size() > 1 && Integer.parseInt(this.sc.tunnel.getProperty("channelsInMax", "1")) >= 0) {
                            this.sc.msg("Tunnel3:Slow incoming speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedIn / 1024.0f + "K/sec, removing channel.");
                            int x3 = 0;
                            while (x3 < loops) {
                                this.removeIn(null);
                                ++x3;
                            }
                        }
                        if (this.sc.incoming.size() < Integer.parseInt(this.sc.tunnel.getProperty("channelsInMax", "1")) * -1) {
                            this.sc.msg("Tunnel3:Fast incoming speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedIn / 1024.0f + "K/sec, adding " + loops + " channels.");
                            int x4 = 0;
                            while (x4 < loops) {
                                this.addIn();
                                ++x4;
                            }
                        }
                        if (this.speedHistory.size() > 10 && speed > this.baseSpeedIn) {
                            this.baseSpeedIn = speed;
                        }
                    }
                    if (this.baseSpeedOut > 0.0f) {
                        float speed = this.getSpeedAverage(this.speedHistory, "speedOut", "countOut", 10);
                        if (speed / 1024.0f > (float)this.minFastSpeed && (double)speed > (double)this.baseSpeedOut * this.speedThreshold) {
                            if (this.sc.outgoing.size() < Math.abs(Integer.parseInt(this.sc.tunnel.getProperty("channelsOutMax", "1")))) {
                                this.sc.msg("Tunnel3:Fast outgoing speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedOut / 1024.0f + "K/sec, adding " + loops + " channels.");
                                int x5 = 0;
                                while (x5 < loops) {
                                    this.addOut();
                                    ++x5;
                                }
                            }
                        } else if (speed / 1024.0f < (float)this.minSlowSpeed && this.sc.outgoing.size() > 1 && Integer.parseInt(this.sc.tunnel.getProperty("channelsOutMax", "1")) >= 0) {
                            this.sc.msg("Tunnel3:Slow outgoing speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedOut / 1024.0f + "K/sec, removing channel.");
                            int x6 = 0;
                            while (x6 < loops) {
                                this.removeOut(null);
                                ++x6;
                            }
                        }
                        if (this.sc.outgoing.size() < Integer.parseInt(this.sc.tunnel.getProperty("channelsOutMax", "1")) * -1) {
                            this.sc.msg("Tunnel3:Fast outgoing speed:" + speed / 1024.0f + "K/sec versus base:" + this.baseSpeedOut / 1024.0f + "K/sec, adding " + loops + " channels.");
                            int x7 = 0;
                            while (x7 < loops) {
                                this.addOut();
                                ++x7;
                            }
                        }
                        if (this.speedHistory.size() > 10 && speed > this.baseSpeedOut) {
                            this.baseSpeedOut = speed;
                        }
                    }
                    if (this.sc.incoming.size() == 0) {
                        this.addIn();
                        if (this.noIncomingCount++ > 10) {
                            Worker.startWorker(new Runnable(){

                                @Override
                                public void run() {
                                    StreamTuner.this.sc.msg("Tunnel3:Tunnel on server appears to be disconnecting us, resetting tunnel.");
                                    StreamTuner.this.sc.reset();
                                }
                            });
                            Thread.sleep(1000L);
                            this.noIncomingCount = 0;
                        }
                    } else {
                        this.noIncomingCount = 0;
                    }
                    if (this.sc.outgoing.size() == 0) {
                        this.addOut();
                    }
                    Thread.sleep(1000L);
                }
            }
            catch (Exception e) {
                this.sc.msg(e);
            }
            try {
                Thread.sleep(5000L);
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        while (this.sc.outgoing.size() > 1) {
            StreamWriter sw = (StreamWriter)this.sc.outgoing.remove(this.sc.outgoing.size() - 1);
            sw.close();
        }
        int loops = 0;
        while (this.sc.incoming.size() > 0 && loops++ < 10) {
            this.removeIn(null);
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.sc.outgoing.size() != 0) continue;
            this.addOut();
        }
        while (this.sc.outgoing.size() > 0) {
            StreamWriter sw = (StreamWriter)this.sc.outgoing.remove(this.sc.outgoing.size() - 1);
            sw.close();
        }
        try {
            this.sc.startStopTunnel(false);
        }
        catch (Exception e) {
            this.sc.msg(e);
        }
        this.active = false;
        this.sc.msg("Tunnel3:Tunnel shutdown.");
    }

    public boolean isActive() {
        return this.active;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public float getSpeedAverage(Vector v, String type1, String type2, int loops_max) {
        float average = 0.0f;
        int loops = 0;
        Vector vector = v;
        synchronized (vector) {
            int x = v.size() - 1;
            while (x >= 0 && loops++ < loops_max) {
                Properties p = (Properties)v.elementAt(x);
                try {
                    average += Float.parseFloat(p.getProperty(type1)) / Float.parseFloat(p.getProperty(type2));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                --x;
            }
        }
        return average /= (float)(loops - 1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void calcSpeed() {
        Vector by_ip;
        long temp;
        long bOut = 0L;
        long bIn = 0L;
        Properties bOut_ip = new Properties();
        Properties bIn_ip = new Properties();
        int x = 0;
        while (x < this.sc.outgoing.size()) {
            StreamWriter sw = (StreamWriter)this.sc.outgoing.elementAt(x);
            temp = sw.getTransferred();
            bOut += temp;
            bOut_ip.put(sw.getBindIp(), String.valueOf(Long.parseLong(bOut_ip.getProperty(sw.getBindIp(), "0")) + temp));
            bOut_ip.put(String.valueOf(sw.getBindIp()) + "_count", String.valueOf(Long.parseLong(bOut_ip.getProperty(String.valueOf(sw.getBindIp()) + "_count", "0")) + 1L));
            by_ip = (Vector)this.speedHistoryIp.get(sw.getBindIp());
            if (by_ip == null) {
                this.speedHistoryIp.put(sw.getBindIp(), new Vector());
            }
            ++x;
        }
        x = 0;
        while (x < this.sc.incoming.size()) {
            StreamReader sr = (StreamReader)this.sc.incoming.elementAt(x);
            temp = sr.getTransferred();
            bIn += temp;
            bIn_ip.put(sr.getBindIp(), String.valueOf(Long.parseLong(bIn_ip.getProperty(sr.getBindIp(), "0")) + temp));
            bIn_ip.put(String.valueOf(sr.getBindIp()) + "_count", String.valueOf(Long.parseLong(bIn_ip.getProperty(String.valueOf(sr.getBindIp()) + "_count", "0")) + 1L));
            by_ip = (Vector)this.speedHistoryIp.get(sr.getBindIp());
            if (by_ip == null) {
                this.speedHistoryIp.put(sr.getBindIp(), new Vector());
            }
            ++x;
        }
        Properties p = new Properties();
        p.put("speedOut", String.valueOf(bOut));
        p.put("speedIn", String.valueOf(bIn));
        p.put("countOut", String.valueOf(this.sc.outgoing.size()));
        p.put("countIn", String.valueOf(this.sc.incoming.size()));
        this.speedHistory.addElement(p);
        Cloneable cloneable = this.speedHistory;
        synchronized (cloneable) {
            while (this.speedHistory.size() > 60) {
                this.speedHistory.remove(0);
            }
        }
        cloneable = this.speedHistoryIp;
        synchronized (cloneable) {
            Enumeration<Object> keys = this.speedHistoryIp.keys();
            while (keys.hasMoreElements()) {
                String bind_ip = keys.nextElement().toString();
                by_ip = (Vector)this.speedHistoryIp.get(bind_ip);
                p = new Properties();
                p.put("speedOut", bOut_ip.getProperty(bind_ip, "0"));
                p.put("speedIn", bIn_ip.getProperty(bind_ip, "0"));
                p.put("countOut", bOut_ip.getProperty(String.valueOf(bind_ip) + "_count", "0"));
                p.put("countIn", bIn_ip.getProperty(String.valueOf(bind_ip) + "_count", "0"));
                by_ip.addElement(p);
                while (by_ip.size() > 60) {
                    by_ip.remove(0);
                }
            }
        }
    }

    public void addOut() {
        if (this.sc.outgoing.size() >= Math.abs(Integer.parseInt(this.sc.tunnel.getProperty("channelsOutMax", "1")))) {
            return;
        }
        StreamWriter sw = new StreamWriter(this.sc, null, Common.makeBoundary(11));
        this.sc.outgoing.addElement(sw);
        this.sc.msg("Tunnel3:Adding outgoing channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        try {
            Worker.startWorker(sw);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void addIn() {
        if (this.sc.incoming.size() >= Math.abs(Integer.parseInt(this.sc.tunnel.getProperty("channelsInMax", "1")))) {
            return;
        }
        StreamReader sr = new StreamReader(this.sc, null, Common.makeBoundary(11));
        this.sc.incoming.addElement(sr);
        this.sc.msg("Tunnel3:Adding incoming channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        try {
            Worker.startWorker(sr);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void removeOut(StreamWriter sw) {
        if (sw != null) {
            this.sc.outgoing.removeElement(sw);
            this.sc.msg("Tunnel3:Removing outgoing channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        } else if (this.sc.outgoing.size() > 0) {
            sw = (StreamWriter)this.sc.outgoing.get(this.sc.outgoing.size() - 1);
            sw.close();
            this.sc.outgoing.removeElement(sw);
            this.sc.msg("Tunnel3:Request remove outgoing channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        }
    }

    public void removeIn(StreamReader sr) {
        if (sr != null) {
            sr.close();
            this.sc.incoming.removeElement(sr);
            this.sc.msg("Tunnel3:Removing incoming channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        } else {
            sr = (StreamReader)this.sc.incoming.get(this.sc.incoming.size() - 1);
            sr.close();
            this.sc.incoming.removeElement(sr);
            this.sc.msg("Tunnel3:Request remove incoming channel. incoming:" + this.sc.incoming.size() + " outgoing:" + this.sc.outgoing.size());
        }
    }
}

