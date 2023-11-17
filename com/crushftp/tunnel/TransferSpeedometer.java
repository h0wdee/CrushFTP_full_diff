/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel;

import java.util.Date;

public class TransferSpeedometer
implements Runnable {
    int sleep_interval = 100;
    int samples = 5 * (1000 / this.sleep_interval);
    long[] rollingAmounts = new long[this.samples];
    long[] rollingAmountsTime = new long[this.samples];
    int rollingAmountsIndex = 0;
    long startByte = 0L;
    long startTime = 0L;
    String directionLabel = "";
    long max_speed = 0L;
    long size = 0L;
    long current_transfer_speed = 0L;
    long overall_transfer_speed = 0L;
    long seconds_remaining = 0L;
    public long current_loc = 0L;
    String the_dir = "";
    StringBuffer pause = null;

    public TransferSpeedometer(long max_speed, long size, StringBuffer pause) {
        this.max_speed = max_speed;
        this.size = size;
        this.pause = pause;
        this.startTime = new Date().getTime();
        this.startByte = 0L;
        int x = 0;
        while (x < this.rollingAmounts.length) {
            this.rollingAmounts[x] = this.startByte;
            this.rollingAmountsTime[x] = this.startTime;
            ++x;
        }
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("TransferSpeedometer");
            while (true) {
                Thread.sleep(this.sleep_interval);
                this.calcOverall();
                this.calcCurrent();
                if (this.max_speed > 0L && (double)this.current_transfer_speed >= (double)this.max_speed * 1.0) {
                    this.pause.setLength(0);
                    this.pause.append("true");
                    continue;
                }
                this.pause.setLength(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void calcCurrent() throws Exception {
        block4: {
            int rollingAmountsIndexStart = this.rollingAmountsIndex - (this.samples - 1);
            int rollingAmountsIndexEnd = this.rollingAmountsIndex - 1;
            if (rollingAmountsIndexStart < 0) {
                rollingAmountsIndexStart = this.samples + rollingAmountsIndexStart;
            }
            if (rollingAmountsIndexEnd < 0) {
                rollingAmountsIndexEnd = this.samples + rollingAmountsIndexEnd;
            }
            long bytes = this.rollingAmounts[rollingAmountsIndexEnd] - this.rollingAmounts[rollingAmountsIndexStart];
            long time = this.rollingAmountsTime[rollingAmountsIndexEnd] - this.rollingAmountsTime[rollingAmountsIndexStart];
            float speed = (float)bytes / (float)time;
            try {
                this.current_transfer_speed = (int)speed;
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block4;
                throw e;
            }
        }
    }

    public void calcOverall() throws Exception {
        block6: {
            block5: {
                long now = new Date().getTime();
                this.rollingAmounts[this.rollingAmountsIndex] = this.current_loc;
                this.rollingAmountsTime[this.rollingAmountsIndex] = now;
                if (++this.rollingAmountsIndex == this.samples) {
                    this.rollingAmountsIndex = 0;
                }
                try {
                    this.overall_transfer_speed = (this.current_loc - this.startByte) / ((now - this.startTime) / 1000L) / 1024L;
                }
                catch (Exception e) {
                    if (("" + e).indexOf("Interrupted") < 0) break block5;
                    throw e;
                }
            }
            try {
                this.seconds_remaining = (this.size - this.current_loc) / 1024L / this.current_transfer_speed;
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") < 0) break block6;
                throw e;
            }
        }
    }

    public static float getDelayAmount(int data_read, long startLoop, long endLoop, float slow_transfer, float speed_limit) {
        if (speed_limit == 0.0f || data_read == 0) {
            return 0.0f;
        }
        slow_transfer = (double)(endLoop - startLoop) < 1000.0 / (double)(speed_limit / (float)data_read) ? (float)((double)slow_transfer + (1000.0 / (double)(speed_limit / (float)data_read) - (double)(endLoop - startLoop))) : (float)((double)slow_transfer - (double)slow_transfer * 0.1);
        if (slow_transfer < 0.0f) {
            slow_transfer = 0.0f;
        }
        if (slow_transfer > 10000.0f) {
            slow_transfer = 500.0f;
        }
        return slow_transfer;
    }
}

