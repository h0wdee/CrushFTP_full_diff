/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.tunnel2.QueueTransfer;
import com.crushftp.tunnel2.Tunnel2;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

public class ConnectionOptimizer
implements Runnable {
    Tunnel2 t = null;
    Vector outgoing = new Vector();
    Vector incoming = new Vector();
    Vector speedHistory = new Vector();
    int stableSeconds = 5;
    int channelRampUp = 1;
    int minFastSpeed = 100;
    int minSlowSpeed = 10;
    double speedThreshold = 0.6;
    int closeInRequests = 0;
    long baseSpeedInIntervals = 0L;
    long baseSpeedOutIntervals = 0L;

    public ConnectionOptimizer(Tunnel2 t) {
        this.t = t;
        this.stableSeconds = Integer.parseInt(t.tunnel.getProperty("stableSeconds", "5"));
        this.channelRampUp = Integer.parseInt(t.tunnel.getProperty("channelRampUp", "1"));
        this.minFastSpeed = Integer.parseInt(t.tunnel.getProperty("minFastSpeed", "100"));
        this.minSlowSpeed = Integer.parseInt(t.tunnel.getProperty("minSlowSpeed", "10"));
        this.speedThreshold = (double)Integer.parseInt(t.tunnel.getProperty("speedThreshold", "60")) / 100.0;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Tunnel Connection Optimizer");
        int noIncomingCount = 0;
        while (this.t.isActive()) {
            try {
                this.addOut();
                this.addIn();
                Thread.sleep(1000L);
                long intervals = 0L;
                while (this.t.isActive()) {
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
                        this.t.baseSpeedIn = speedIn / (float)this.speedHistory.size();
                        Tunnel2.msg("Tunnel2:stableSeconds=" + this.stableSeconds + ":channelRampUp=" + this.channelRampUp + ":minFastSpeed=" + this.minFastSpeed + ":minSlowSpeed=" + this.minSlowSpeed + ":speedThreshold=" + this.speedThreshold + ":Base Speed In=" + this.t.baseSpeedIn / 1024.0f + "K/sec  Out=" + this.t.baseSpeedOut / 1024.0f + "K/sec");
                        if (this.t.baseSpeedIn < 5.0f) {
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
                        this.t.baseSpeedOut = speedOut / (float)this.speedHistory.size();
                        Tunnel2.msg("Tunnel2:stableSeconds=" + this.stableSeconds + ":channelRampUp=" + this.channelRampUp + ":minFastSpeed=" + this.minFastSpeed + ":minSlowSpeed=" + this.minSlowSpeed + ":speedThreshold=" + this.speedThreshold + ":Base Speed In=" + this.t.baseSpeedIn / 1024.0f + "K/sec  Out=" + this.t.baseSpeedOut / 1024.0f + "K/sec");
                        if (this.t.baseSpeedOut < 5.0f) {
                            this.baseSpeedOutIntervals = 0L;
                        }
                    }
                    int loops = this.channelRampUp;
                    if (intervals == (long)(this.stableSeconds + 1)) {
                        loops *= 2;
                    }
                    if (this.t.baseSpeedIn > 0.0f) {
                        float speed = this.getLast10("speedIn", "countIn");
                        if (speed / 1024.0f > (float)this.minFastSpeed && (double)speed > (double)this.t.baseSpeedIn * this.speedThreshold) {
                            if (this.incoming.size() < Math.abs(Integer.parseInt(this.t.tunnel.getProperty("channelsInMax", "1")))) {
                                Tunnel2.msg("Tunnel2:Fast incoming speed:" + speed / 1024.0f + "K/sec versus base:" + this.t.baseSpeedIn / 1024.0f + "K/sec, adding " + loops + " channels.");
                                int x2 = 0;
                                while (x2 < loops) {
                                    this.addIn();
                                    ++x2;
                                }
                            }
                        } else if (speed / 1024.0f < (float)this.minSlowSpeed && this.incoming.size() > 1 && Integer.parseInt(this.t.tunnel.getProperty("channelsInMax", "1")) >= 0) {
                            Tunnel2.msg("Tunnel2:Slow incoming speed:" + speed / 1024.0f + "K/sec versus base:" + this.t.baseSpeedIn / 1024.0f + "K/sec, removing channel.");
                            this.removeIn(null);
                        }
                        if (this.speedHistory.size() > 10 && speed > this.t.baseSpeedIn) {
                            this.t.baseSpeedIn = speed;
                        }
                    }
                    if (this.t.baseSpeedOut > 0.0f) {
                        float speed = this.getLast10("speedOut", "countOut");
                        if (speed / 1024.0f > (float)this.minFastSpeed && (double)speed > (double)this.t.baseSpeedOut * this.speedThreshold) {
                            if (this.outgoing.size() < Math.abs(Integer.parseInt(this.t.tunnel.getProperty("channelsOutMax", "1")))) {
                                Tunnel2.msg("Tunnel2:Fast outgoing speed:" + speed / 1024.0f + "K/sec versus base:" + this.t.baseSpeedOut / 1024.0f + "K/sec, adding " + loops + " channels.");
                                int x3 = 0;
                                while (x3 < loops) {
                                    this.addOut();
                                    ++x3;
                                }
                            }
                        } else if (speed / 1024.0f < (float)this.minSlowSpeed && this.outgoing.size() > 1 && Integer.parseInt(this.t.tunnel.getProperty("channelsOutMax", "1")) >= 0) {
                            Tunnel2.msg("Tunnel2:Slow outgoing speed:" + speed / 1024.0f + "K/sec versus base:" + this.t.baseSpeedOut / 1024.0f + "K/sec, removing channel.");
                            this.removeOut(null);
                        }
                        if (this.speedHistory.size() > 10 && speed > this.t.baseSpeedOut) {
                            this.t.baseSpeedOut = speed;
                        }
                    }
                    if (this.incoming.size() == 0) {
                        this.addIn();
                        if (noIncomingCount++ > 10) {
                            if (this.t.username == null || this.t.username.equals("")) break;
                            new Thread(new Runnable(){

                                @Override
                                public void run() {
                                    Tunnel2.msg("Tunnel2:Tunnel on server appears to be disconnecting us, resetting tunnel.");
                                    ConnectionOptimizer.this.t.reset();
                                }
                            }).start();
                            noIncomingCount = 0;
                        }
                    } else {
                        noIncomingCount = 0;
                    }
                    if (this.outgoing.size() == 0) {
                        this.addOut();
                    }
                    Thread.sleep(1000L);
                }
            }
            catch (Exception e) {
                Tunnel2.msg(e);
            }
            try {
                Thread.sleep(5000L);
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        while (this.outgoing.size() > 1) {
            QueueTransfer qt = (QueueTransfer)this.outgoing.remove(this.outgoing.size() - 1);
            qt.close();
        }
        int loops = 0;
        while (this.incoming.size() > 0 && loops++ < 10) {
            this.removeIn(null);
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.outgoing.size() != 0) continue;
            this.addOut();
        }
        while (this.outgoing.size() > 0) {
            QueueTransfer qt = (QueueTransfer)this.outgoing.remove(this.outgoing.size() - 1);
            qt.close();
        }
        this.t.stopThisTunnel();
        this.t.setShutdown(true);
        Tunnel2.msg("Tunnel2:Tunnel shutdown.");
    }

    public float getLast10(String type1, String type2) {
        float average = 0.0f;
        int loops = 0;
        int x = this.speedHistory.size() - 1;
        while (x >= 0 && loops++ < 10) {
            Properties p = (Properties)this.speedHistory.elementAt(x);
            try {
                average += Float.parseFloat(p.getProperty(type1)) / Float.parseFloat(p.getProperty(type2));
            }
            catch (Exception exception) {
                // empty catch block
            }
            --x;
        }
        return average /= (float)loops;
    }

    public void calcSpeed() {
        long temp;
        QueueTransfer q;
        long bOut = 0L;
        long bIn = 0L;
        int x = 0;
        while (x < this.outgoing.size()) {
            q = (QueueTransfer)this.outgoing.elementAt(x);
            temp = q.getTransferred();
            bOut += temp;
            ++x;
        }
        x = 0;
        while (x < this.incoming.size()) {
            q = (QueueTransfer)this.incoming.elementAt(x);
            temp = q.getTransferred();
            bIn += temp;
            ++x;
        }
        Properties p = new Properties();
        p.put("speedOut", String.valueOf(bOut));
        p.put("speedIn", String.valueOf(bIn));
        p.put("countOut", String.valueOf(this.outgoing.size()));
        p.put("countIn", String.valueOf(this.incoming.size()));
        this.speedHistory.addElement(p);
        while (this.speedHistory.size() > 60) {
            this.speedHistory.remove(0);
        }
    }

    public void addOut() {
        if (this.outgoing.size() >= Math.abs(Integer.parseInt(this.t.tunnel.getProperty("channelsOutMax", "1")))) {
            return;
        }
        QueueTransfer qt = new QueueTransfer(this.t, "send", this.outgoing);
        this.outgoing.addElement(qt);
        Tunnel2.msg("Tunnel2:Adding outgoing channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size());
        new Thread(qt).start();
    }

    public void addIn() {
        if (this.incoming.size() >= Math.abs(Integer.parseInt(this.t.tunnel.getProperty("channelsInMax", "1")))) {
            return;
        }
        QueueTransfer qt = new QueueTransfer(this.t, "get", this.incoming);
        this.incoming.addElement(qt);
        Tunnel2.msg("Tunnel2:Adding incoming channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size());
        new Thread(qt).start();
    }

    public void removeOut(QueueTransfer qt) {
        if (qt != null) {
            this.outgoing.removeElement(qt);
            Tunnel2.msg("Tunnel2:Removing outgoing channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size());
        } else {
            qt = (QueueTransfer)this.outgoing.get(this.outgoing.size() - 1);
            qt.close();
            Tunnel2.msg("Tunnel2:Request remove outgoing channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size());
        }
    }

    public void removeIn(QueueTransfer qt) {
        if (this.closeInRequests > this.incoming.size() && this.closeInRequests > 10) {
            while (this.incoming.size() > 0) {
                QueueTransfer qt2 = (QueueTransfer)this.incoming.remove(0);
                qt2.close();
                try {
                    qt2.urlc.disconnect();
                }
                catch (IOException e) {
                    Tunnel2.msg(e);
                }
            }
            this.addIn();
            this.closeInRequests = 0;
        }
        if (qt != null) {
            this.incoming.removeElement(qt);
            Tunnel2.msg("Tunnel2:Removing incoming channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size());
            this.closeInRequests = 0;
        } else {
            try {
                this.t.writeLocal(this.t.makeCommand(0, "CLOSEIN:0"), 0);
                ++this.closeInRequests;
            }
            catch (Exception exception) {
                // empty catch block
            }
            Tunnel2.msg("Tunnel2:Request remove incoming channel. incoming:" + this.incoming.size() + " outgoing:" + this.outgoing.size() + " closeInRequests:" + this.closeInRequests);
        }
    }
}

