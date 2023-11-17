/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  CrushTask.Start
 */
package com.crushftp.job;

import CrushTask.Start;
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import com.crushftp.job.JobReference;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class JobBroker {
    static String job_host = "127.0.0.1";
    static int job_broker_port = 0;
    static Process proc = null;
    static BufferedReader proc_br = null;
    public static Object proc_lock = new Object();
    public static Vector remote_running_tasks = null;
    static long last_remote_update = System.currentTimeMillis();

    public static void main(String[] args) {
        ServerSocket ss = null;
        try {
            try {
                ss = new ServerSocket(0);
                System.out.println(String.valueOf(ss.getLocalPort()));
                while (!ss.isClosed()) {
                    final Socket sock = ss.accept();
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            JobBroker.doActions(sock);
                        }
                    });
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    ss.close();
                }
                catch (Exception exception) {}
            }
        }
        finally {
            try {
                ss.close();
            }
            catch (Exception exception) {}
        }
    }

    public static void doActions(Socket sock) {
        block28: {
            ObjectInputStream ois = null;
            ObjectOutputStream oos = null;
            try {
                try {
                    ois = new ObjectInputStream(sock.getInputStream());
                    oos = new ObjectOutputStream(sock.getOutputStream());
                    while (!sock.isClosed()) {
                        Properties p = (Properties)ois.readObject();
                        if (p.getProperty("action", "").equals("runLocalJob")) {
                            Properties info = JobBroker.runLocalJob(p.getProperty("job_name"), p.getProperty("new_job_id_run"), oos);
                            oos.writeUnshared(info);
                            oos.close();
                            ois.close();
                            sock.close();
                            continue;
                        }
                        if (p.getProperty("action", "").equals("getRunningTasks")) {
                            oos.writeUnshared(JobBroker.getActiveJobs());
                            oos.flush();
                            oos.reset();
                            continue;
                        }
                        if (!p.getProperty("action", "").equals("putRemote")) continue;
                        Vector jobs = JobBroker.getActiveJobs();
                        int x = 0;
                        while (x < jobs.size()) {
                            Properties tracker = (Properties)jobs.elementAt(x);
                            if (tracker.getProperty("id", "").equals(p.getProperty("tracker_id"))) {
                                tracker.put(p.get("key"), p.get("val"));
                                break block28;
                            }
                            ++x;
                        }
                        break;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    try {
                        ois.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    try {
                        oos.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    try {
                        sock.close();
                    }
                    catch (IOException iOException) {}
                }
            }
            finally {
                try {
                    ois.close();
                }
                catch (IOException iOException) {}
                try {
                    oos.close();
                }
                catch (IOException iOException) {}
                try {
                    sock.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    public static Properties runLocalJob(String job_name, String job_id, final ObjectOutputStream oos) throws Exception {
        File job = new File("./jobs/" + job_name);
        Properties params = (Properties)Common.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
        params.put("debug", "true");
        params.put("enabled", "true");
        Properties event = new Properties();
        event.put("event_plugin_list", "CrushTask");
        event.put("name", "ScheduledPluginEvent:" + params.getProperty("scheduleName"));
        params.put("new_job_id", job_id);
        params.put("new_job_id_run", job_id);
        event.putAll((Map<?, ?>)params);
        final Properties info = new Properties();
        info.put("action", "event");
        info.put("server_settings", new Properties());
        info.put("return_tracker", "true");
        info.put("event", event);
        info.put("items", new Vector());
        Start crush_task = new Start();
        crush_task.setSettings(params);
        final Properties status_obj = new Properties();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                while (!status_obj.containsKey("job_status")) {
                    try {
                        oos.writeUnshared(info);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
            }
        });
        crush_task.run(info);
        status_obj.put("job_status", "done");
        return info;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties runRemoteJob(final Properties info) throws Exception {
        Object object = proc_lock;
        synchronized (object) {
            if (job_broker_port == 0) {
                Vector<String> params = new Vector<String>();
                params.addElement(String.valueOf(System.getProperty("java.home")) + "/bin/java");
                if (new File_S("./jobs_debug").exists()) {
                    params.addElement("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=4002");
                }
                params.addElement("-jar");
                if (!System.getProperty("crushftp.jobs_memory_mb", "0").equals("0")) {
                    params.addElement("-Xmx");
                    params.addElement(String.valueOf(System.getProperty("crushftp.jobs_memory_mb", "0")) + "m");
                }
                params.addElement("plugins/lib/CrushFTPJarProxy.jar");
                params.addElement("-JOB_BROKER");
                String[] params_str = new String[params.size()];
                int x = 0;
                while (x < params.size()) {
                    params_str[x] = params.elementAt(x).toString();
                    ++x;
                }
                proc = Runtime.getRuntime().exec(params_str);
                proc_br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = "";
                line = proc_br.readLine();
                if (line.indexOf("Listening for transport") >= 0) {
                    line = proc_br.readLine();
                }
                job_broker_port = Integer.parseInt(line);
                Common.log("SERVER", 0, "JobBroker started on port:" + job_broker_port + (System.getProperty("crushftp.jobs_memory_mb", "0").equals("0") ? " using default JVM memory." : " using " + System.getProperty("crushftp.jobs_memory_mb", "0") + "MByte of memory."));
            }
        }
        final Properties p = new Properties();
        Worker.startWorker(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                sock = null;
                ois = null;
                oos = null;
                try {
                    try {
                        while (true) {
                            try {
                                sock = new Socket(JobBroker.job_host, JobBroker.job_broker_port);
                                oos = new ObjectOutputStream(sock.getOutputStream());
                                ois = new ObjectInputStream(sock.getInputStream());
                                p.put("action", "runLocalJob");
                                p.put("job_name", info.getProperty("scheduleName"));
                                event = (Properties)info.get("event");
                                if (event.containsKey("new_job_id_run")) {
                                    p.put("new_job_id_run", event.getProperty("new_job_id_run"));
                                }
                                oos.writeUnshared(p);
                                oos.flush();
                                if (true) ** GOTO lbl32
                            }
                            catch (Exception e) {
                                Common.log("SERVER", 0, "JobBroker error!:" + e);
                                Thread.sleep(100L);
                                continue;
                            }
                            break;
                        }
                        do {
                            p2 = (Properties)ois.readObject();
                            info.putAll((Map<?, ?>)p2);
                            p.put("info", info);
lbl32:
                            // 2 sources

                        } while (!sock.isClosed());
                    }
                    catch (EOFException p2) {
                        try {
                            if (ois != null) {
                                ois.close();
                            }
                        }
                        catch (IOException var6_8) {
                            // empty catch block
                        }
                        try {
                            if (oos != null) {
                                oos.close();
                            }
                        }
                        catch (IOException var6_9) {
                            // empty catch block
                        }
                        try {
                            if (sock != null) {
                                sock.close();
                            }
                        }
                        catch (IOException var6_10) {}
                    }
                    catch (Exception e) {
                        Common.log("SERVER", 0, e);
                        try {
                            if (ois != null) {
                                ois.close();
                            }
                        }
                        catch (IOException var6_11) {
                            // empty catch block
                        }
                        try {
                            if (oos != null) {
                                oos.close();
                            }
                        }
                        catch (IOException var6_12) {
                            // empty catch block
                        }
                        try {
                            if (sock != null) {
                                sock.close();
                            }
                        }
                        catch (IOException var6_13) {}
                    }
                }
                finally {
                    try {
                        if (ois != null) {
                            ois.close();
                        }
                    }
                    catch (IOException var6_17) {}
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                    }
                    catch (IOException var6_18) {}
                    try {
                        if (sock != null) {
                            sock.close();
                        }
                    }
                    catch (IOException var6_19) {}
                }
            }
        });
        long start = System.currentTimeMillis();
        while (!p.containsKey("info")) {
            Thread.sleep(100L);
            if (System.currentTimeMillis() - start <= 60000L) continue;
            throw new Exception("Timeout waiting for job to be started in Job engine...");
        }
        return info;
    }

    public static void addRunningTask(Properties tracker) {
        Common.log("JOB_SCHEDULER", 0, "addRunningTask:" + tracker.getProperty("scheduleName", "") + ":" + tracker.getProperty("log_file"));
        ((Vector)Common.System2.get("running_tasks")).addElement(tracker);
    }

    public static void removeRunningTask(Properties tracker) {
        Common.log("JOB_SCHEDULER", 0, "removeRunningTask:" + tracker.getProperty("scheduleName", "") + ":" + tracker.getProperty("log_file"));
        ((Vector)Common.System2.get("running_tasks")).remove(tracker);
    }

    public static int indexOfRunningTask(Properties tracker) {
        return ((Vector)Common.System2.get("running_tasks")).indexOf(tracker);
    }

    public static int getRunningTaskSize() {
        if (Common.V() >= 11) {
            return ((Vector)Common.System2.get("running_tasks")).size();
        }
        return ((Vector)Common.System2.get("running_tasks")).size();
    }

    public static Vector getActiveJobs() {
        Vector v = new Vector();
        Vector running_tasks = (Vector)Common.System2.get("running_tasks");
        int x = 0;
        while (x < running_tasks.size()) {
            Properties p = (Properties)running_tasks.elementAt(x);
            Vector active_items = (Vector)p.get("activbe_items");
            if (active_items != null && active_items.size() > 100) {
                p = (Properties)p.clone();
                p.remove("active_items");
            }
            running_tasks.setElementAt(p, x);
            ++x;
        }
        v.addAll(running_tasks);
        v.addAll(JobBroker.getRemoteActiveJobs());
        return v;
    }

    public static Vector getRemoteActiveJobs() {
        try {
            if (job_broker_port != 0 && remote_running_tasks == null) {
                remote_running_tasks = new Vector();
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Socket sock = null;
                        ObjectOutputStream oos = null;
                        ObjectInputStream ois = null;
                        try {
                            try {
                                sock = new Socket(job_host, job_broker_port);
                                oos = new ObjectOutputStream(sock.getOutputStream());
                                ois = new ObjectInputStream(sock.getInputStream());
                                while (!sock.isClosed()) {
                                    Properties p = new Properties();
                                    p.put("action", "getRunningTasks");
                                    oos.writeUnshared(p);
                                    oos.flush();
                                    Vector v = (Vector)ois.readObject();
                                    int x = 0;
                                    while (x < v.size()) {
                                        JobReference jr = new JobReference((Properties)v.elementAt(x), job_host, job_broker_port);
                                        v.setElementAt(jr, x);
                                        ++x;
                                    }
                                    remote_running_tasks = v;
                                    last_remote_update = System.currentTimeMillis();
                                    Thread.sleep(1000L);
                                }
                            }
                            catch (Exception e) {
                                Common.log("SERVER", 0, "JobBroker getRemoteActiveJobs error!:" + e);
                                remote_running_tasks = null;
                                try {
                                    if (ois != null) {
                                        ois.close();
                                    }
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                                try {
                                    if (oos != null) {
                                        oos.close();
                                    }
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                                try {
                                    if (sock != null) {
                                        sock.close();
                                    }
                                }
                                catch (IOException iOException) {}
                            }
                        }
                        finally {
                            remote_running_tasks = null;
                            try {
                                if (ois != null) {
                                    ois.close();
                                }
                            }
                            catch (IOException iOException) {}
                            try {
                                if (oos != null) {
                                    oos.close();
                                }
                            }
                            catch (IOException iOException) {}
                            try {
                                if (sock != null) {
                                    sock.close();
                                }
                            }
                            catch (IOException iOException) {}
                        }
                    }
                });
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        long current_update_time = last_remote_update;
        int loops = 0;
        while (remote_running_tasks != null && current_update_time == last_remote_update && loops++ < 50) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        return remote_running_tasks == null ? new Vector() : remote_running_tasks;
    }

    public static boolean isJobRunning(String scheduleName) {
        if (Common.V() >= 11) {
            boolean ok = true;
            int x = 0;
            while (ok && x < ((Vector)Common.System2.get("running_tasks")).size()) {
                Properties tracker = (Properties)((Vector)Common.System2.get("running_tasks")).elementAt(x);
                Properties settings = (Properties)tracker.get("settings");
                if (settings == null) {
                    settings = new Properties();
                }
                if (scheduleName.equalsIgnoreCase(settings.getProperty("scheduleName", ""))) {
                    ok = false;
                }
                ++x;
            }
            return !ok;
        }
        boolean ok = true;
        int x = 0;
        while (ok && x < ((Vector)Common.System2.get("running_tasks")).size()) {
            Properties tracker = (Properties)((Vector)Common.System2.get("running_tasks")).elementAt(x);
            Properties settings = (Properties)tracker.get("settings");
            if (settings == null) {
                settings = new Properties();
            }
            if (scheduleName.equalsIgnoreCase(settings.getProperty("scheduleName", ""))) {
                ok = false;
            }
            ++x;
        }
        return !ok;
    }

    public static int getJobSpeeds(String transfer_direction) {
        int speed = 0;
        if (Common.V() >= 11) {
            Vector running_tasks = (Vector)Common.System2.get("running_tasks");
            int x = 0;
            while (x < running_tasks.size()) {
                Properties tracker = (Properties)running_tasks.elementAt(x);
                Vector active_items = (Vector)tracker.get("active_items");
                if (active_items != null) {
                    int xx = 0;
                    while (xx < active_items.size()) {
                        Properties active_item = (Properties)active_items.elementAt(xx);
                        if (active_item.getProperty(transfer_direction, "false").equals("true") && active_item.containsKey("the_file_transfer_speed")) {
                            long the_file_transfer_speed = Long.parseLong(active_item.getProperty("the_file_transfer_speed", "0"));
                            speed = (int)((long)speed + the_file_transfer_speed / 1024L);
                        }
                        ++xx;
                    }
                }
                ++x;
            }
        } else {
            Vector running_tasks = (Vector)Common.System2.get("running_tasks");
            int x = 0;
            while (x < running_tasks.size()) {
                Properties tracker = (Properties)running_tasks.elementAt(x);
                Vector active_items = (Vector)tracker.get("active_items");
                if (active_items != null) {
                    int xx = 0;
                    while (xx < active_items.size()) {
                        Properties active_item = (Properties)active_items.elementAt(xx);
                        if (active_item.getProperty(transfer_direction, "false").equals("true") && active_item.containsKey("the_file_transfer_speed")) {
                            long the_file_transfer_speed = Long.parseLong(active_item.getProperty("the_file_transfer_speed", "0"));
                            speed = (int)((long)speed + the_file_transfer_speed / 1024L);
                        }
                        ++xx;
                    }
                }
                ++x;
            }
        }
        return speed;
    }

    public static String getJobResult(String task_key, String task_val, String job_id) throws Exception {
        String result = "Invalid task, or key.";
        if (Common.V() >= 11) {
            Vector running_tasks = (Vector)Common.System2.get("running_tasks");
            int x = running_tasks.size() - 1;
            while (x >= 0) {
                Properties tracker = (Properties)running_tasks.elementAt(x);
                if (tracker.getProperty("id").equals(job_id) && tracker.containsKey(task_key)) {
                    tracker.put(task_key, task_val);
                    Thread.sleep(2300L);
                    result = tracker.getProperty(String.valueOf(task_key) + "_result", "No result.");
                }
                --x;
            }
        } else {
            Vector running_tasks = (Vector)Common.System2.get("running_tasks");
            int x = running_tasks.size() - 1;
            while (x >= 0) {
                Properties tracker = (Properties)running_tasks.elementAt(x);
                if (tracker.getProperty("id").equals(job_id) && tracker.containsKey(task_key)) {
                    tracker.put(task_key, task_val);
                    Thread.sleep(2300L);
                    result = tracker.getProperty(String.valueOf(task_key) + "_result", "No result.");
                }
                --x;
            }
        }
        return result;
    }
}

