/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.JobFilesHandler;
import crushftp.handlers.JobScheduler;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class Events {
    public static transient Object pluginCountLock = new Object();
    public static transient Object thread_starter_lock = new Object();
    Vector eventRunQueue = new Vector();
    Properties fileTracker = new Properties();
    public static Properties eventPluginCache = new Properties();

    public synchronized Properties process(String event_type, Properties fileItem1, Properties fileItem2, Vector the_events, SessionCrush theSession) {
        Properties info = null;
        if (the_events == null || the_events.size() == 0 || Common.dmz_mode) {
            return info;
        }
        String id = theSession.getId();
        if (id.endsWith(String.valueOf(theSession.uiSG("user_port")) + theSession.uiSG("user_number"))) {
            id = id.substring(0, id.lastIndexOf(String.valueOf(theSession.uiSG("user_port")) + theSession.uiSG("user_number")));
        }
        if (event_type.equals("LOGOUT_ALL") || event_type.equals("BATCH_COMPLETE")) {
            int xx = this.eventRunQueue.size() - 1;
            while (xx >= 0) {
                Properties eventRun = (Properties)this.eventRunQueue.elementAt(xx);
                if (eventRun.getProperty("id", "-1").equals(id)) {
                    eventRun.put("wait", "disconnect_now");
                    Log.log("EVENT", 2, "Setting events for id:" + id + " to run now.");
                }
                --xx;
            }
            return info;
        }
        Log.log("EVENT", 2, "Checking event info:" + event_type + ":" + VRL.safe(fileItem1));
        int x = 0;
        while (x < the_events.size()) {
            block22: {
                Properties eventRun;
                Properties event;
                block23: {
                    block24: {
                        event = (Properties)the_events.elementAt(x);
                        if (!event.containsKey("id")) {
                            event.put("id", crushftp.handlers.Common.makeBoundary());
                        }
                        if (!(event.getProperty("event_user_action_list", "").indexOf("(connect)") >= 0 && event_type.equals("LOGIN") || event.getProperty("event_user_action_list", "").indexOf("(upload)") >= 0 && event_type.equals("UPLOAD") || event.getProperty("event_user_action_list", "").indexOf("(upload)") >= 0 && event.getProperty("event_user_action_list", "").indexOf("(rename)") >= 0 && event_type.equals("RENAME") || event.getProperty("event_user_action_list", "").indexOf("(upload)") >= 0 && event_type.equals("RENAME") && fileItem2 != null && fileItem2.getProperty("url", "").endsWith(".filepart") || event.getProperty("event_user_action_list", "").indexOf("(download)") >= 0 && event_type.equals("DOWNLOAD") || event.getProperty("event_user_action_list", "").indexOf("(pre_download)") >= 0 && event_type.equals("PRE_DOWNLOAD") || event.getProperty("event_user_action_list", "").indexOf("(delete)") >= 0 && event_type.equals("DELETE") || event.getProperty("event_user_action_list", "").indexOf("(site)") >= 0 && event_type.equals("SITE") || event.getProperty("event_user_action_list", "").indexOf("(share)") >= 0 && event_type.equals("SHARE") || event.getProperty("event_user_action_list", "").indexOf("(custom)") >= 0 && event_type.equals("CUSTOM") || event.getProperty("event_user_action_list", "").indexOf("(problem)") >= 0 && event_type.equals("PROBLEM") || event.getProperty("event_user_action_list", "").indexOf("(rename)") >= 0 && event_type.equals("RENAME") || event.getProperty("event_user_action_list", "").indexOf("(error)") >= 0 && event_type.equals("ERROR") || event.getProperty("event_user_action_list", "").indexOf("(welcome)") >= 0 && event_type.equals("WELCOME")) && (event.getProperty("event_user_action_list", "").indexOf("(makedir)") < 0 || !event_type.equals("MAKEDIR"))) break block22;
                        boolean criteria_met = false;
                        Properties config = new Properties();
                        config.put("event", event);
                        config.put("event_type", event_type);
                        if (theSession != null) {
                            config.put("theSession", theSession);
                        }
                        config.put("update_tracker", "true");
                        config.put("id", id);
                        if (fileItem1 != null) {
                            config.put("fileItem1", fileItem1);
                        }
                        if (fileItem2 != null) {
                            config.put("fileItem2", fileItem2);
                        }
                        String criteria_result = "";
                        try {
                            criteria_result = this.checkCriteriaOfEvents(config);
                        }
                        catch (Exception e) {
                            Log.log("EVENT", 1, e);
                        }
                        if (criteria_result.equals("")) break;
                        if (criteria_result.equals("true")) {
                            criteria_met = true;
                        }
                        Log.log("EVENT", 2, "Checking event info:criteria_met=" + criteria_met + ":" + event_type + ":" + VRL.safe(fileItem1));
                        if (!criteria_met) break block22;
                        Log.log("EVENT", 2, new Exception("Event trigger stack"));
                        if (!event.getProperty("event_now_cb", "").equals("true")) break block23;
                        Log.log("EVENT", 2, "Event is set to run immediately, running it in 2 seconds...");
                        eventRun = new Properties();
                        eventRun.put("time", String.valueOf(System.currentTimeMillis()));
                        eventRun.put("timeout", String.valueOf(System.currentTimeMillis() + 2000L));
                        eventRun.put("wait", "timeout");
                        eventRun.put("session", theSession);
                        eventRun.put("event", event);
                        eventRun.put("id", id);
                        eventRun.put("event_type", event_type);
                        if (fileItem1 == null) break block24;
                        eventRun.put("fileItem", fileItem1);
                        if (event_type.equals("SITE") && !fileItem1.getProperty("event_name").equals(event.getProperty("name", ""))) break block22;
                    }
                    Log.log("EVENT", 2, "Adding 2 second delayed event to the queue...");
                    this.eventRunQueue.addElement(eventRun);
                    if (event_type.equals("LOGIN") || event_type.equals("SITE") || event_type.equals("WELCOME") || event_type.equals("ERROR") || event_type.startsWith("PRE_") || event.getProperty("name", "").toUpperCase().endsWith("_NO_DELAY")) {
                        Log.log("EVENT", 2, "Skipping 2 second delay.");
                        eventRun.put("timeout", "0");
                        info = this.checkEventsNow();
                    }
                    break block22;
                }
                eventRun = new Properties();
                eventRun.put("time", String.valueOf(System.currentTimeMillis()));
                eventRun.put("session", theSession);
                eventRun.put("event", event);
                eventRun.put("id", id);
                eventRun.put("wait", "disconnect_all");
                eventRun.put("event_type", event_type);
                if (event.getProperty("event_after_list", "").indexOf("(disconnect") >= 0) {
                    String disconnectMsg = event.getProperty("event_after_list", "").substring(event.getProperty("event_after_list", "").indexOf("(disconnect") + "(disconnect".length());
                    disconnectMsg = disconnectMsg.substring(0, disconnectMsg.indexOf(")"));
                    eventRun.put("wait", "disconnect " + disconnectMsg);
                    Log.log("EVENT", 2, "Adding event to queue:" + id + ":" + event.getProperty("name", "") + "...will check to see if its already in the queue...");
                } else if (event.getProperty("event_after_list", "").indexOf("(idle_") >= 0) {
                    String secs = event.getProperty("event_after_list", "").substring(event.getProperty("event_after_list", "").indexOf("(idle_") + "(idle_".length());
                    secs = secs.substring(0, secs.indexOf(")"));
                    eventRun.put("wait", "idle " + secs);
                    Log.log("EVENT", 2, "Adding event to queue:" + id + ":" + event.getProperty("name", "") + "...will check to see if its already in the queue...");
                }
                boolean found = false;
                int xx = this.eventRunQueue.size() - 1;
                while (xx >= 0) {
                    Properties eventRun2 = (Properties)this.eventRunQueue.elementAt(xx);
                    Properties event2 = (Properties)eventRun2.get("event");
                    if (eventRun2.getProperty("id", "-1").equals(id) && event2.getProperty("id").equals(event.getProperty("id"))) {
                        Log.log("EVENT", 2, "Event is in queue..." + event2.getProperty("id", "-1") + ":" + id);
                        found = true;
                    }
                    --xx;
                }
                this.updateTracker(id, event_type, event, fileItem1, fileItem2, theSession);
                if (!found) {
                    Log.log("EVENT", 2, "Adding event to queue as " + event_type + ".");
                    this.eventRunQueue.addElement(eventRun);
                }
            }
            ++x;
        }
        return info;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void updateTracker(String id, String event_type, Properties event, Properties fileItem1, Properties fileItem2, SessionCrush theSession) {
        if (Common.dmz_mode) {
            return;
        }
        Properties tracker = (Properties)this.fileTracker.get(String.valueOf(id) + "_" + event.getProperty("id"));
        if (tracker == null) {
            tracker = new Properties();
            tracker.put("uploads", new Vector());
            tracker.put("downloads", new Vector());
            tracker.put("deletes", new Vector());
            tracker.put("renames", new Vector());
            tracker.put("shares", new Vector());
            tracker.put("customs", new Vector());
            tracker.put("problems", new Vector());
            tracker.put("makedirs", new Vector());
            tracker.put("event_type", event_type);
            this.fileTracker.put(String.valueOf(id) + "_" + event.getProperty("id"), tracker);
        }
        Vector uploads = (Vector)tracker.get("uploads");
        Vector downloads = (Vector)tracker.get("downloads");
        Vector deletes = (Vector)tracker.get("deletes");
        Vector renames = (Vector)tracker.get("renames");
        Vector shares = (Vector)tracker.get("shares");
        Vector customs = (Vector)tracker.get("customs");
        Vector problems = (Vector)tracker.get("problems");
        Vector makedirs = (Vector)tracker.get("makedirs");
        if (fileItem1 == null) return;
        fileItem1.put("event_type", event_type);
        if (event_type.equals("UPLOAD")) {
            boolean found = false;
            int x = uploads.size() - 1;
            while (x >= 0 && !found) {
                Properties p = (Properties)uploads.elementAt(x);
                if (fileItem1.getProperty("mark_error", "").equals("true")) {
                    Log.log("EVENT", 0, "Mark error event item:" + VRL.fileFix(new VRL(fileItem1.getProperty("url", "")).safe()) + "  versus  " + VRL.fileFix(new VRL(p.getProperty("url", "")).safe()));
                    if (VRL.fileFix(p.getProperty("url", "")).equalsIgnoreCase(VRL.fileFix(fileItem1.getProperty("url", "")))) {
                        p.put("the_file_error", fileItem1.getProperty("the_file_error"));
                        p.put("the_file_status", fileItem1.getProperty("the_file_status"));
                        found = true;
                    }
                } else if (VRL.fileFix(p.getProperty("url", "")).equalsIgnoreCase(VRL.fileFix(fileItem1.getProperty("url", "")))) {
                    Log.log("EVENT", 2, "Found existing event item:" + VRL.fileFix(new VRL(p.getProperty("url", "")).safe()) + " versus:" + VRL.fileFix(new VRL(fileItem1.getProperty("url", "")).safe()));
                    uploads.setElementAt(fileItem1, x);
                    found = true;
                }
                --x;
            }
            if (found || fileItem1.getProperty("mark_error", "").equals("true")) return;
            Log.log("EVENT", 2, "New event item:" + VRL.fileFix(new VRL(fileItem1.getProperty("url", "")).safe()));
            uploads.addElement(fileItem1);
        } else if (event_type.equals("DOWNLOAD")) {
            downloads.addElement(fileItem1);
        } else if (event_type.equals("SHARE")) {
            shares.addElement(fileItem1);
        } else if (event_type.equals("CUSTOM")) {
            customs.addElement(fileItem1);
        } else if (event_type.equals("PROBLEM")) {
            problems.addElement(fileItem1);
        } else if (event_type.equals("MAKEDIR")) {
            makedirs.addElement(fileItem1);
        } else if (event_type.equals("DELETE")) {
            deletes.addElement(fileItem1);
            int xx = uploads.size() - 1;
            while (xx >= 0) {
                Properties p = (Properties)uploads.elementAt(xx);
                if (VRL.fileFix(p.getProperty("url", "")).equalsIgnoreCase(VRL.fileFix(fileItem1.getProperty("url", "")))) {
                    uploads.removeElementAt(xx);
                }
                --xx;
            }
            if (ServerStatus.BG("recursive_delete_event") && fileItem1.getProperty("type", "FILE").equalsIgnoreCase("DIR")) {
                try {
                    Vector list = new Vector();
                    theSession.uVFS.getListing(list, fileItem1.getProperty("the_file_path"), 20, 50000, true);
                    list.removeElementAt(list.size() - 1);
                    int xx2 = 0;
                    while (xx2 < list.size() - 1) {
                        Properties p = (Properties)list.elementAt(xx2);
                        p.put("the_file_name", p.getProperty("name", ""));
                        p.put("the_file_path", p.getProperty("path", ""));
                        ++xx2;
                    }
                    deletes.addAll(list);
                }
                catch (Exception e) {
                    Log.log("EVENT", 1, e);
                }
            }
        } else if (event_type.equals("RENAME")) {
            Properties p;
            if (fileItem2 == null) {
                fileItem2 = fileItem1;
            }
            if (!fileItem2.getProperty("url", "").endsWith(".filepart")) {
                renames.addElement(fileItem1);
            }
            int xx = 0;
            while (xx < downloads.size()) {
                p = (Properties)downloads.elementAt(xx);
                if (VRL.fileFix(p.getProperty("url", "")).equalsIgnoreCase(VRL.fileFix(fileItem1.getProperty("url", "")))) {
                    p.put("url", fileItem1.getProperty("url", ""));
                    p.put("url_2", fileItem2.getProperty("url", ""));
                    p.put("the_file_name", fileItem1.getProperty("the_file_name", ""));
                    p.put("the_file_name_2", fileItem2.getProperty("the_file_name", ""));
                    p.put("the_file_path", fileItem1.getProperty("the_file_path", ""));
                    p.put("the_file_path_2", fileItem2.getProperty("the_file_path", ""));
                }
                ++xx;
            }
            xx = 0;
            while (xx < uploads.size()) {
                p = (Properties)uploads.elementAt(xx);
                if (VRL.fileFix(crushftp.handlers.Common.url_decode(p.getProperty("url", ""))).equalsIgnoreCase(VRL.fileFix(crushftp.handlers.Common.url_decode(fileItem2.getProperty("url", ""))))) {
                    p.put("url", fileItem1.getProperty("url", ""));
                    p.put("url_2", fileItem2.getProperty("url", ""));
                    p.put("the_file_name", fileItem1.getProperty("the_file_name", ""));
                    p.put("the_file_name_2", fileItem2.getProperty("the_file_name", ""));
                    p.put("the_file_path", fileItem1.getProperty("the_file_path", ""));
                    p.put("the_file_path_2", fileItem2.getProperty("the_file_path", ""));
                }
                ++xx;
            }
            if (ServerStatus.BG("recursive_rename_event") && fileItem2.getProperty("type", "FILE").equalsIgnoreCase("DIR")) {
                try {
                    Vector list = new Vector();
                    theSession.uVFS.getListing(list, fileItem2.getProperty("the_file_path"), 20, 50000, true);
                    list.removeElementAt(list.size() - 1);
                    int xx3 = 0;
                    while (xx3 < list.size() - 1) {
                        Properties p2 = (Properties)list.elementAt(xx3);
                        p2.put("the_file_name", p2.getProperty("name", ""));
                        p2.put("the_file_path", p2.getProperty("path", ""));
                        ++xx3;
                    }
                    renames.addAll(list);
                }
                catch (Exception e) {
                    Log.log("EVENT", 1, e);
                }
            }
        }
        theSession.add_log("[" + theSession.uiSG("user_number") + ":" + theSession.uiSG("user_name") + ":" + theSession.uiSG("user_ip") + "] WROTE: *Tracking event items (..." + id.substring(id.length() - 4) + ") uploads:" + uploads.size() + ",downloads:" + downloads.size() + ",deletes:" + deletes.size() + ",renames:" + renames.size() + ",lastType:" + event_type + "*", "STOR");
    }

    public synchronized Properties checkEventsNow() {
        Properties eventRun;
        Properties info = null;
        String lastName = "";
        int x = 0;
        while (x < this.eventRunQueue.size()) {
            try {
                eventRun = (Properties)this.eventRunQueue.elementAt(x);
                SessionCrush theSession = (SessionCrush)eventRun.get("session");
                String id = eventRun.getProperty("id");
                Properties event = (Properties)eventRun.get("event");
                if (!event.containsKey("id")) {
                    event.put("id", crushftp.handlers.Common.makeBoundary());
                }
                lastName = event.getProperty("name", "");
                if (theSession.user == null) {
                    Log.log("EVENT", 0, "Invalid user session found in event.");
                    this.eventRunQueue.remove(eventRun);
                    break;
                }
                theSession.user_info.put("root_dir", theSession.user.getProperty("root_dir"));
                long lastActivity = Long.parseLong(theSession.getProperty("last_activity", "0"));
                boolean notConnected = this.countConnectedUsers(theSession) == 0;
                boolean http = theSession.uiSG("user_protocol").startsWith("HTTP");
                String doEventType = null;
                if (eventRun.getProperty("wait").equals("timeout") || eventRun.getProperty("wait").startsWith("disconnect") && event.getProperty("event_now_cb", "").equals("true")) {
                    Log.log("EVENT", 2, "Checking time on timeout event...");
                    if (Long.parseLong(eventRun.getProperty("timeout", "0")) < System.currentTimeMillis()) {
                        Log.log("EVENT", 2, "Event has reached timeout, running...");
                        eventRun.put("delete", "true");
                        Properties p = (Properties)eventRun.get("fileItem");
                        if (p == null || !p.getProperty("usedByNow_" + event.getProperty("id"), "false").equals("true")) {
                            Properties tracker;
                            if (p != null) {
                                p.put("usedByNow_" + event.getProperty("id"), "true");
                            }
                            Vector<Properties> items = new Vector<Properties>();
                            if (p != null) {
                                items.addElement(p);
                            }
                            if (event.getProperty("event_user_action_list", "").indexOf("(pre_") < 0) {
                                this.cleanupItems(items);
                            }
                            if ((tracker = (Properties)this.fileTracker.get(String.valueOf(id) + "_" + event.getProperty("id"))) == null) {
                                tracker = new Properties();
                                tracker.put("event_type", eventRun.getProperty("event_type", ""));
                            }
                            if (items.size() > 0 || event.getProperty("event_user_action_list", "").indexOf("(connect)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(site)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(welcome)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(error)") >= 0) {
                                info = this.runEvent(event, theSession, items, ServerStatus.BG("reverse_events"), tracker.getProperty("event_type", ""));
                            }
                        }
                    }
                } else if (eventRun.getProperty("wait").startsWith("idle")) {
                    Log.log("EVENT", 2, "Checking time on idle event...");
                    long secs = Long.parseLong(eventRun.getProperty("wait").split(" ")[1]);
                    if (System.currentTimeMillis() - lastActivity > secs * 1000L || System.currentTimeMillis() - lastActivity > (long)((http ? 300 : 10) * 1000) && notConnected) {
                        Log.log("EVENT", 2, "Event has reached idle timeout, running...");
                        doEventType = "Idle";
                    }
                } else if (eventRun.getProperty("wait").startsWith("disconnect")) {
                    Log.log("EVENT", 2, "Checking disconnect event, lastActivity:" + lastActivity + " " + new Date(lastActivity) + "  notConnected:" + notConnected);
                    if (lastActivity == 0L || eventRun.getProperty("wait").equals("disconnect_now") || System.currentTimeMillis() - lastActivity > (long)((http ? 300 : 10) * 1000) && notConnected) {
                        doEventType = "Disconnect";
                    }
                }
                if (doEventType != null) {
                    Log.log("EVENT", 2, "Running event type:" + doEventType + " id=" + id.substring(id.length() - 4) + " eventid:" + event.getProperty("id"));
                    eventRun.put("delete", "true");
                    Vector items = new Vector();
                    Properties tracker = (Properties)this.fileTracker.get(String.valueOf(id) + "_" + event.getProperty("id"));
                    if (tracker == null) {
                        Log.log("SERVER", 0, "Event lost!:" + event + ":" + eventRun);
                    }
                    Vector uploads = (Vector)tracker.get("uploads");
                    Vector downloads = (Vector)tracker.get("downloads");
                    Vector deletes = (Vector)tracker.get("deletes");
                    Vector renames = (Vector)tracker.get("renames");
                    Vector shares = (Vector)tracker.get("shares");
                    Vector customs = (Vector)tracker.get("customs");
                    Vector problems = (Vector)tracker.get("problems");
                    Vector makedirs = (Vector)tracker.get("makedirs");
                    if (event.getProperty("event_user_action_list", "").indexOf("(upload)") >= 0) {
                        items.addAll(uploads);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(pre_download)") >= 0) {
                        items.addAll(downloads);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(download)") >= 0) {
                        items.addAll(downloads);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(delete)") >= 0) {
                        items.addAll(deletes);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(rename)") >= 0) {
                        items.addAll(renames);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(share)") >= 0) {
                        items.addAll(shares);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(custom)") >= 0) {
                        items.addAll(customs);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(problem)") >= 0) {
                        items.addAll(problems);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(makedir)") >= 0) {
                        items.addAll(makedirs);
                    }
                    if (event.getProperty("event_user_action_list", "").indexOf("(") != event.getProperty("event_user_action_list", "").lastIndexOf("(")) {
                        crushftp.handlers.Common.do_sort(items, "modified", "modified");
                    }
                    Log.log("EVENT", 2, "Checking event:" + event);
                    Log.log("EVENT", 2, "Checking event items:" + items.size());
                    int xx = items.size() - 1;
                    while (xx >= 0) {
                        Properties p = (Properties)items.elementAt(xx);
                        Properties p2 = (Properties)p.clone();
                        p2.put("url", new VRL(p2.getProperty("url")).safe());
                        Log.log("EVENT", 2, "Checking event item:" + p2);
                        if (p.getProperty("usedBy" + doEventType + "_" + event.getProperty("id"), "false").equals("true")) {
                            Log.log("EVENT", 0, "Event has already processed this item (removing...):" + p);
                            items.removeElementAt(xx);
                        }
                        p.put("usedBy" + doEventType + "_" + event.getProperty("id"), "true");
                        --xx;
                    }
                    this.cleanupItems(items);
                    theSession.uiPUT("session_upload_count", String.valueOf(items.size()));
                    theSession.uiPUT("session_download_count", String.valueOf(items.size()));
                    if (items.size() > 0 || event.getProperty("event_user_action_list", "").indexOf("(connect)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(site)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(welcome)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(error)") >= 0) {
                        info = this.runEvent(event, theSession, items, ServerStatus.BG("reverse_events"), tracker.getProperty("event_type", ""));
                    }
                }
            }
            catch (Exception e) {
                Log.log("EVENT", 0, "Event " + lastName + " failed due to an error");
                Log.log("EVENT", 0, e);
            }
            ++x;
        }
        x = this.eventRunQueue.size() - 1;
        while (x >= 0) {
            eventRun = (Properties)this.eventRunQueue.elementAt(x);
            String id = eventRun.getProperty("id");
            Properties event = (Properties)eventRun.get("event");
            if (eventRun.getProperty("delete", "false").equals("true")) {
                Log.log("EVENT", 2, "Removing event tracker for id=" + id.substring(id.length() - 4) + " eventid:" + event.getProperty("id"));
                this.eventRunQueue.remove(x);
                this.fileTracker.remove(String.valueOf(id) + "_" + event.getProperty("id"));
            }
            --x;
        }
        return info;
    }

    public int countConnectedUsers(SessionCrush this_user) {
        int num_users = 0;
        int x = ServerStatus.siVG("user_list").size() - 1;
        while (x >= 0) {
            try {
                Properties p = (Properties)ServerStatus.siVG("user_list").elementAt(x);
                if (((SessionCrush)p.get("session")).getId().equalsIgnoreCase(this_user.getId())) {
                    Log.log("EVENT", 2, "Found similar user:" + ++num_users + ":" + ((SessionCrush)p.get("session")).getId());
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            --x;
        }
        return num_users;
    }

    public void cleanupItems(Vector itemsAll) {
        int xx = itemsAll.size() - 1;
        while (xx >= 0) {
            Properties p = (Properties)itemsAll.elementAt(xx);
            if (!ServerStatus.BG("event_empty_files") && p.getProperty("the_file_size", "0").equals("0") || p.getProperty("the_file_name", "").indexOf(".DS_Store") >= 0 || p.getProperty("the_file_name", "").startsWith("._") || p.getProperty("the_file_name", "").equals("")) {
                itemsAll.remove(p);
            }
            --xx;
        }
    }

    public void cleanupItemsEmail(Vector itemsAll) {
        int xx = itemsAll.size() - 1;
        while (xx >= 0) {
            Properties p = (Properties)itemsAll.elementAt(xx);
            if (!ServerStatus.BG("event_reuse") && p.getProperty("usedByEvent", "").equals("true")) {
                itemsAll.remove(p);
            }
            p.put("usedByEvent", "true");
            --xx;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties runEvent(final Properties event, final SessionCrush the_user, final Vector items, final boolean reverse, final String event_type) throws Exception {
        boolean async;
        Object object;
        Log.log("EVENT", 2, "runEvent::" + event.getProperty("name", ""));
        Log.log("EVENT", 2, "runEvent:items size:" + items.size());
        Log.log("EVENT", 2, "runEvent:items:" + items);
        final Properties info = new Properties();
        info.put("event_status", "running");
        info.put("event_name", event.getProperty("name", ""));
        if (event_type != null) {
            info.put("event_type", event_type);
        }
        Thread t = new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                try {
                    Properties groupings = new Properties();
                    groupings.put("default", new Vector());
                    int x = 0;
                    while (x < items.size()) {
                        Vector<Properties> v;
                        String id = "default";
                        Properties item = (Properties)items.elementAt(x);
                        if (ServerStatus.BG("event_batching") && item.containsKey("metaInfo")) {
                            Properties metaInfo = (Properties)item.get("metaInfo");
                            id = metaInfo.getProperty("unique_upload_id", id);
                        }
                        if ((v = (Vector<Properties>)groupings.get(id)) == null) {
                            v = new Vector<Properties>();
                        }
                        groupings.put(id, v);
                        v.addElement(item);
                        ++x;
                    }
                    if (groupings.size() > 1) {
                        groupings.remove("default");
                    }
                    Vector users = null;
                    Enumeration<Object> keys = groupings.keys();
                    while (keys.hasMoreElements()) {
                        String groupId = keys.nextElement().toString();
                        Log.log("EVENT", 0, "Grouping event on id:" + groupId);
                        Vector groupedItems = (Vector)groupings.get(groupId);
                        if (event.getProperty("event_action_list", "").indexOf("(send_email)") >= 0 && (String.valueOf(event.getProperty("to", "").trim()) + event.getProperty("cc", "").trim() + event.getProperty("bcc", "").trim()).equals("")) {
                            event.put("event_action_list", "(reverse)");
                        }
                        if (event.getProperty("event_action_list", "").indexOf("(send_email)") >= 0) {
                            Events.this.cleanupItemsEmail(groupedItems);
                            if (groupedItems.size() > 0 || event.getProperty("event_user_action_list", "").indexOf("(connect)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(site)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(welcome)") >= 0 || event.getProperty("event_user_action_list", "").indexOf("(error)") >= 0) {
                                Events.this.doEventEmail(event, the_user.user, the_user.user_info, groupedItems, the_user.uiVG("lastUploadStats"));
                            }
                        }
                        if (event.getProperty("event_action_list", "").indexOf("(run_plugin)") >= 0) {
                            Events.this.doEventPlugin(info, event, the_user, groupedItems);
                        }
                        if (event.getProperty("event_action_list", "").indexOf("(reverse)") < 0 || !reverse) continue;
                        if (!ServerStatus.BG("v10_beta")) continue;
                        Properties event_types = new Properties();
                        int x2 = 0;
                        while (x2 < groupedItems.size()) {
                            Properties item = (Properties)groupedItems.get(x2);
                            if (!event_types.containsKey(item.getProperty("event_type", event_type))) {
                                event_types.put(item.getProperty("event_type", event_type), new Vector());
                            }
                            Vector event_items = (Vector)event_types.get(item.getProperty("event_type", event_type));
                            event_items.add(item);
                            ++x2;
                        }
                        Enumeration<Object> event_types_keys = event_types.keys();
                        while (event_types_keys.hasMoreElements()) {
                            String reverse_event_type = event_types_keys.nextElement().toString();
                            Events.this.doReverseEvent(users, (Vector)event_types.get(reverse_event_type), the_user, reverse_event_type);
                        }
                    }
                }
                catch (Throwable throwable) {
                    Object object = pluginCountLock;
                    synchronized (object) {
                        ServerStatus.siPUT("running_event_threads", String.valueOf(ServerStatus.siIG("running_event_threads") - 1));
                    }
                    info.put("event_status", "done");
                    throw throwable;
                }
                Object object = pluginCountLock;
                synchronized (object) {
                    ServerStatus.siPUT("running_event_threads", String.valueOf(ServerStatus.siIG("running_event_threads") - 1));
                }
                info.put("event_status", "done");
            }
        });
        while (true) {
            object = pluginCountLock;
            synchronized (object) {
                if (ServerStatus.siIG("running_event_threads") <= ServerStatus.IG("max_event_threads")) {
                    ServerStatus.siPUT("running_event_threads", String.valueOf(ServerStatus.siIG("running_event_threads") + 1));
                    break;
                }
            }
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {}
        }
        t.setName(String.valueOf(Thread.currentThread().getName()) + ":Running event:" + event.getProperty("name"));
        object = thread_starter_lock;
        synchronized (object) {
            while (true) {
                try {
                    t.start();
                }
                catch (Throwable e) {
                    Log.log("SERVER", 0, "ERROR starting event thread, will retry in 1 second.  Server needs additional open file handles or process threads from the OS:  " + e);
                    Log.log("SERVER", 0, e);
                    Thread.sleep(1000L);
                    continue;
                }
                break;
            }
        }
        boolean bl = async = event.getProperty("async", "").equalsIgnoreCase("") || event.getProperty("async", "").equalsIgnoreCase("auto") ? ServerStatus.BG("event_asynch") : event.getProperty("async", "").equalsIgnoreCase("yes");
        if (!async) {
            t.join(1000 * ServerStatus.IG("event_thread_timeout"));
            if (t.isAlive()) {
                Log.log("EVENT", 0, "Event didn't complete in 60 seconds, leaving thread running...items=" + items.size() + ":Event=" + event);
            }
        }
        return info;
    }

    public String doEventEmail(Properties event, Properties user, Properties user_info, Vector items, Vector lastUploadStats) {
        Log.log("EVENT", 0, "Event:EMAIL " + event.getProperty("name") + ":" + event.getProperty("to") + "\r\n");
        Log.log("EVENT", 2, "doEventEmail:items size:" + items.size());
        Log.log("EVENT", 2, "doEventEmail:items:" + items);
        String to = "";
        String cc = "";
        String bcc = "";
        String from = "";
        String body = "";
        String subject = "";
        try {
            String emailResult;
            body = crushftp.handlers.Common.replace_str(crushftp.handlers.Common.replace_str(event.getProperty("body"), "&lt;LINE&gt;", "<LINE>"), "&lt;/LINE&gt;", "</LINE>");
            String the_body_line = "";
            int loops = 0;
            while (body.toUpperCase().indexOf("<LINE>") >= 0 && body.toUpperCase().indexOf("</LINE>") >= 0) {
                if (++loops > 20) break;
                try {
                    the_body_line = body.substring(body.toUpperCase().indexOf("<LINE>") + "<LINE>".length(), body.toUpperCase().indexOf("</LINE>"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                String lineData = "";
                String user_time = ((SimpleDateFormat)ServerStatus.thisObj.logDateFormat.clone()).format(new Date());
                int x = 0;
                while (x < items.size()) {
                    Properties p = (Properties)items.elementAt(x);
                    String the_line = the_body_line;
                    the_line = this.replace_path_url_segments(p, the_line);
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%user_time%", user_time);
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_path%", p.getProperty("the_file_path", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_name%", p.getProperty("the_file_name", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_name_2%", p.getProperty("the_file_name_2", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size%", p.getProperty("the_file_size", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size_formatted%", Common.format_bytes_short2(Long.parseLong(p.getProperty("the_file_size", "0"))));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_speed%", p.getProperty("the_file_speed", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_error%", p.getProperty("the_file_error", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_start%", p.getProperty("the_file_start", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_end%", p.getProperty("the_file_end", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_md5%", p.getProperty("the_file_md5", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%url%", p.getProperty("url", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%url_2%", p.getProperty("url_2", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%display%", p.getProperty("display", ""));
                    the_line = crushftp.handlers.Common.replace_str(the_line, "%all%", p.toString());
                    if (!(the_line = ServerStatus.thisObj.change_vars_to_values(the_line, user, user_info, null)).trim().equals("")) {
                        if (event.getProperty("event_now_cb", "").equals("true") && (x == items.size() - 1 || event.getProperty("event_user_action_list").indexOf("(disconnect)") >= 0)) {
                            lineData = String.valueOf(lineData) + the_line + "\r\n";
                        } else if (!event.getProperty("event_now_cb", "").equals("true")) {
                            lineData = String.valueOf(lineData) + the_line + "\r\n";
                        }
                    }
                    ++x;
                }
                Log.log("EVENT", 2, "BODY:<LINE>" + lineData + "</LINE>");
                try {
                    body = crushftp.handlers.Common.replace_str(body, body.substring(body.toUpperCase().indexOf("<LINE>"), body.toUpperCase().indexOf("</LINE>") + "</LINE>".length()), lineData);
                }
                catch (Exception e) {
                    Log.log("EVENT", 1, e);
                }
            }
            body = ServerStatus.thisObj.change_vars_to_values(body, user, user_info, null);
            Properties form_email = crushftp.handlers.Common.buildFormEmail(ServerStatus.server_settings, lastUploadStats);
            String web_upload_form_all = "";
            Vector<String> names = new Vector<String>();
            Vector<String> forms = new Vector<String>();
            int xx = 0;
            while (xx < items.size()) {
                String web_upload_form = "";
                Properties item = (Properties)items.elementAt(xx);
                Properties uploadStat = (Properties)item.get("uploadStats");
                Properties metaInfo = null;
                metaInfo = uploadStat == null ? (Properties)item.get("metaInfo") : (Properties)uploadStat.get("metaInfo");
                if (metaInfo != null && metaInfo != null) {
                    String id = metaInfo.getProperty("UploadFormId", "");
                    Properties customForm = null;
                    Vector customForms = (Vector)ServerStatus.server_settings.get("CustomForms");
                    if (customForms != null) {
                        int x = 0;
                        while (x < customForms.size()) {
                            Properties p = (Properties)customForms.elementAt(x);
                            if (p.getProperty("id", "").equals(id)) {
                                customForm = p;
                                break;
                            }
                            ++x;
                        }
                        if (customForm != null) {
                            if (!customForm.containsKey("entries")) {
                                customForm.put("entries", new Vector());
                            }
                            Vector entries = (Vector)customForm.get("entries");
                            int x2 = 0;
                            while (x2 < entries.size()) {
                                Properties p = (Properties)entries.elementAt(x2);
                                web_upload_form = !p.getProperty("type").trim().equals("label") ? String.valueOf(web_upload_form) + p.getProperty("name", "").trim() + ":" + metaInfo.getProperty(p.getProperty("name", "").trim()) + "\r\n\r\n" : String.valueOf(web_upload_form) + p.getProperty("label", "").trim() + " " + p.getProperty("value", "") + "\r\n";
                                ++x2;
                            }
                        }
                        if (forms.indexOf(web_upload_form) < 0) {
                            names.addElement(item.getProperty("the_file_name"));
                            forms.addElement(web_upload_form);
                        } else {
                            names.setElementAt(String.valueOf(names.elementAt(forms.indexOf(web_upload_form)).toString()) + "," + (uploadStat == null ? item.getProperty("name", "") : uploadStat.getProperty("name", "")), forms.indexOf(web_upload_form));
                        }
                    }
                }
                ++xx;
            }
            int x = 0;
            while (x < names.size()) {
                web_upload_form_all = String.valueOf(web_upload_form_all) + "File Name(s): " + names.elementAt(x).toString() + "\r\n\r\n" + forms.elementAt(x).toString() + "\r\n";
                ++x;
            }
            body = crushftp.handlers.Common.replace_str(body, "%web_upload_form%", web_upload_form_all);
            subject = event.getProperty("subject");
            subject = this.replace_line_variables(items, subject, user, user_info);
            subject = ServerStatus.thisObj.change_vars_to_values(subject, user, user_info, null);
            subject = crushftp.handlers.Common.replaceFormVariables(form_email, subject);
            to = this.replace_line_variables(items, event.getProperty("to"), user, user_info).trim();
            to = ServerStatus.thisObj.change_vars_to_values(to, user, user_info, null);
            to = crushftp.handlers.Common.replaceFormVariables(form_email, to);
            cc = this.replace_line_variables(items, event.getProperty("cc"), user, user_info).trim();
            cc = ServerStatus.thisObj.change_vars_to_values(cc, user, user_info, null);
            cc = crushftp.handlers.Common.replaceFormVariables(form_email, cc);
            bcc = this.replace_line_variables(items, event.getProperty("bcc"), user, user_info).trim();
            bcc = ServerStatus.thisObj.change_vars_to_values(bcc, user, user_info, null);
            bcc = crushftp.handlers.Common.replaceFormVariables(form_email, bcc);
            from = this.replace_line_variables(items, event.getProperty("from"), user, user_info).trim();
            from = ServerStatus.thisObj.change_vars_to_values(from, user, user_info, null);
            from = crushftp.handlers.Common.replaceFormVariables(form_email, from);
            body = crushftp.handlers.Common.replaceFormVariables(form_email, body);
            if (ServerStatus.SG("smtp_server").equals("")) {
                ServerStatus.server_settings.put("smtp_server", event.getProperty("smtp_server", ""));
                ServerStatus.server_settings.put("smtp_user", event.getProperty("smtp_user", ""));
                ServerStatus.server_settings.put("smtp_pass", event.getProperty("smtp_pass", ""));
                ServerStatus.thisObj.save_server_settings(true);
            }
            if ((emailResult = Common.send_mail(ServerStatus.SG("discovered_ip"), to, cc, bcc, from, subject, body, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.BG("smtp_ssl"), ServerStatus.BG("smtp_html"), null)).toUpperCase().indexOf("SUCCESS") < 0) {
                Log.log("EVENT", 0, String.valueOf(LOC.G("FAILURE:")) + " " + emailResult + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("FROM:")) + " " + from + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("TO:")) + " " + to + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("CC:")) + " " + cc + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("BCC:")) + " " + bcc + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("SUBJECT:")) + " " + subject + "\r\n");
                Log.log("EVENT", 0, String.valueOf(LOC.G("BODY:")) + " " + body + "\r\n");
                Properties m = new Properties();
                m.put("result", emailResult);
                m.put("body", body);
                m.put("subject", subject);
                m.put("to", to);
                m.put("from", from);
                m.put("cc", cc);
                m.put("bcc", bcc);
                ServerStatus.thisObj.runAlerts("invalid_email", m, null, null);
                return "ERROR:" + emailResult;
            }
            Log.log("EVENT", 0, "Event:EMAIL SUCCESS " + event.getProperty("name") + ":" + to + "\r\n");
            return "SUCCESS";
        }
        catch (Exception e) {
            Log.log("EVENT", 0, String.valueOf(LOC.G("Event:EMAIL")) + " " + ServerStatus.thisObj.change_vars_to_values(event.getProperty("name"), user, user_info, null) + ":" + to + ":" + e + "\r\n");
            Log.log("EVENT", 1, e);
            Properties m = new Properties();
            m.put("result", e.toString());
            m.put("body", body);
            m.put("subject", subject);
            m.put("to", to);
            m.put("from", from);
            m.put("cc", cc);
            m.put("bcc", bcc);
            ServerStatus.thisObj.runAlerts("invalid_email", m, null, null);
            return "ERROR:" + e;
        }
    }

    public String replace_line_variables(Vector items, String s, Properties user, Properties user_info) {
        String s_line = "";
        try {
            s_line = s.substring(s.toUpperCase().indexOf("<LINE>") + "<LINE>".length(), s.toUpperCase().indexOf("</LINE>"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        String lineData = "";
        String user_time = ((SimpleDateFormat)ServerStatus.thisObj.logDateFormat.clone()).format(new Date());
        int x = 0;
        while (x < items.size()) {
            Properties p = (Properties)items.elementAt(x);
            String the_line = s_line;
            the_line = this.replace_path_url_segments(p, the_line);
            the_line = crushftp.handlers.Common.replace_str(the_line, "%user_time%", user_time);
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_path%", p.getProperty("the_file_path", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_name%", p.getProperty("the_file_name", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_name_2%", p.getProperty("the_file_name_2", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size%", p.getProperty("the_file_size", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_size_formatted%", Common.format_bytes_short2(Long.parseLong(p.getProperty("the_file_size", "0"))));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_speed%", p.getProperty("the_file_speed", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_error%", p.getProperty("the_file_error", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_start%", p.getProperty("the_file_start", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_end%", p.getProperty("the_file_end", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_md5%", p.getProperty("the_file_md5", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%url%", p.getProperty("url", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%url_2%", p.getProperty("url_2", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%display%", p.getProperty("display", ""));
            the_line = crushftp.handlers.Common.replace_str(the_line, "%all%", p.toString());
            Enumeration<Object> keys = p.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                String val = p.get(key).toString();
                the_line = crushftp.handlers.Common.replace_str(the_line, "%" + key + "%", val);
            }
            if (!(the_line = ServerStatus.thisObj.change_vars_to_values(the_line, user, user_info, null)).trim().equals("")) {
                lineData = String.valueOf(lineData) + the_line + "\r\n";
            }
            ++x;
        }
        Log.log("EVENT", 2, String.valueOf(s) + ":<LINE>" + lineData + "</LINE>");
        try {
            if (s.toUpperCase().indexOf("<LINE>") >= 0) {
                s = crushftp.handlers.Common.replace_str(s, s.substring(s.toUpperCase().indexOf("<LINE>"), s.toUpperCase().indexOf("</LINE>") + "</LINE>".length()), lineData);
            }
        }
        catch (Exception e) {
            Log.log("EVENT", 1, e);
        }
        return s;
    }

    public String replace_path_url_segments(Properties p, String the_line) {
        String token;
        String[] pathTokens = p.getProperty("the_file_path", "").split("/");
        int xx = 1;
        while (xx < 20) {
            token = "";
            if (pathTokens != null && xx - 1 < pathTokens.length) {
                token = pathTokens[xx - 1];
            }
            the_line = crushftp.handlers.Common.replace_str(the_line, "%the_file_path" + xx + "%", token);
            ++xx;
        }
        pathTokens = p.getProperty("url", "").split("/");
        xx = 1;
        while (xx < 20) {
            token = "";
            if (pathTokens != null && xx - 1 < pathTokens.length) {
                token = pathTokens[xx - 1];
            }
            the_line = crushftp.handlers.Common.replace_str(the_line, "%url" + xx + "%", token);
            ++xx;
        }
        return the_line;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    public Properties doEventPlugin(Properties info, Properties event, SessionCrush the_user, Vector items) {
        try {
            Vector<Properties> cache;
            Properties cachedPlugin;
            String subItem;
            String pluginName;
            block41: {
                block42: {
                    Thread.currentThread().setName(String.valueOf(LOC.G("Event:PLUGIN")) + " " + event.getProperty("name"));
                    StackTraceElement[] ste = Thread.currentThread().getStackTrace();
                    String horizontal_thread = "";
                    int x = 0;
                    while (x < ste.length && x < 4) {
                        horizontal_thread = String.valueOf(horizontal_thread) + "|" + ste[x].getFileName() + ":" + ste[x].getMethodName() + ":" + ste[x].getLineNumber();
                        ++x;
                    }
                    Log.log("EVENT", 0, String.valueOf(LOC.G("Event:PLUGIN")) + " " + event.getProperty("name") + ":" + horizontal_thread);
                    if (info == null) {
                        info = new Properties();
                    }
                    info.put("action", "event");
                    info.put("server_settings", ServerStatus.server_settings);
                    info.put("event", event);
                    info.put("trigger_name", crushftp.handlers.Common.replace_str(event.getProperty("name", ""), ":", "_"));
                    if (the_user != null) {
                        info.put("ServerSession", the_user);
                    }
                    if (the_user != null) {
                        info.put("ServerSessionObject", the_user);
                    }
                    if (the_user != null && the_user.user != null) {
                        info.put("user", the_user.user);
                    }
                    if (the_user != null && the_user.user_info != null) {
                        info.put("user_info", the_user.user_info);
                    }
                    info.put("items", items);
                    pluginName = event.getProperty("event_plugin_list", "");
                    subItem = "";
                    if (pluginName.indexOf(":") >= 0) {
                        subItem = pluginName.substring(pluginName.indexOf(":") + 1);
                        pluginName = pluginName.substring(0, pluginName.indexOf(":"));
                    }
                    cachedPlugin = null;
                    cache = null;
                    if (!pluginName.equalsIgnoreCase("Job")) break block42;
                    event = (Properties)event.clone();
                    Vector jobs = JobScheduler.getJobList(false);
                    File job = null;
                    int x2 = 0;
                    while (job == null && x2 < jobs.size()) {
                        block44: {
                            File_S f;
                            block43: {
                                f = (File_S)jobs.elementAt(x2);
                                String path = f.getAbsolutePath();
                                if (crushftp.handlers.Common.machine_is_windows()) {
                                    path = Common.winPath(path);
                                }
                                if (subItem.contains("/") && path.endsWith(subItem)) break block43;
                                if (!f.getName().equalsIgnoreCase(subItem)) break block44;
                                if (!f.getParentFile().getAbsolutePath().equals(new File_S(String.valueOf(ServerStatus.SG("jobs_location")) + "jobs/").getAbsolutePath())) break block44;
                            }
                            job = f;
                        }
                        ++x2;
                    }
                    Properties params = null;
                    int x3 = 0;
                    while (x3 < 30) {
                        try {
                            params = (Properties)JobFilesHandler.readXMLObject(String.valueOf(job.getPath()) + "/job.XML");
                            break;
                        }
                        catch (Exception path) {
                            Thread.sleep(1000L);
                            ++x3;
                        }
                    }
                    params.put("new_job_id", crushftp.handlers.Common.makeBoundary(20));
                    try {
                        Cloneable cloneable;
                        event.putAll((Map<?, ?>)params);
                        event.put("event_plugin_list", params.getProperty("plugin", params.getProperty("event_plugin_list")));
                        event.put("name", "ScheduledPluginEvent:" + params.getProperty("scheduleName"));
                        boolean override = false;
                        if (event.getProperty("async", "").equals("no")) {
                            int loops = 0;
                            while (loops++ < 600) {
                                cloneable = AdminControls.runningSchedules;
                                synchronized (cloneable) {
                                    if (AdminControls.runningSchedules.indexOf(params.getProperty("scheduleName")) < 0) {
                                        override = true;
                                        AdminControls.runningSchedules.addElement(params.getProperty("scheduleName"));
                                        break;
                                    }
                                }
                                Thread.sleep(1000L);
                            }
                        }
                        if (!event.getProperty("async", "").equalsIgnoreCase("no")) {
                            override = true;
                        }
                        if (AdminControls.runningSchedules.indexOf(params.getProperty("scheduleName")) < 0 || override) {
                            try {
                                if (!override) {
                                    AdminControls.runningSchedules.addElement(params.getProperty("scheduleName"));
                                }
                                cloneable = ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
                                return cloneable;
                            }
                            finally {
                                AdminControls.runningSchedules.remove(params.getProperty("scheduleName"));
                            }
                        }
                        break block41;
                    }
                    catch (Exception e) {
                        Log.log("HTTP_SERVER", 1, e);
                    }
                    break block41;
                }
                if (pluginName.endsWith(" (User Defined)")) {
                    pluginName = pluginName.substring(0, pluginName.indexOf(" (User Defined)"));
                    Object thePlugin = null;
                    cache = (Vector<Properties>)eventPluginCache.get(pluginName);
                    Properties job = eventPluginCache;
                    synchronized (job) {
                        if (cache == null) {
                            cache = new Vector<Properties>();
                        }
                        eventPluginCache.put(pluginName, cache);
                        if (cache.size() > 0) {
                            cachedPlugin = (Properties)cache.remove(0);
                            thePlugin = cachedPlugin.get("plugin");
                            subItem = cachedPlugin.getProperty("subItem");
                            Properties defaultPrefs = (Properties)cachedPlugin.get("defaultPrefs");
                            defaultPrefs = (Properties)Common.CLONE(defaultPrefs);
                            defaultPrefs.putAll((Map<?, ?>)event);
                            ServerStatus.thisObj.common_code.setPluginSettings(thePlugin, defaultPrefs);
                        }
                    }
                    if (thePlugin == null) {
                        subItem = crushftp.handlers.Common.makeBoundary(10);
                        thePlugin = crushftp.handlers.Common.getPlugin(pluginName, new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").toURI().toURL().toExternalForm(), subItem);
                        Properties defaultPrefs = ServerStatus.thisObj.common_code.getPluginDefaultPrefs(pluginName, subItem);
                        cachedPlugin = new Properties();
                        cachedPlugin.put("plugin", thePlugin);
                        cachedPlugin.put("defaultPrefs", Common.CLONE(defaultPrefs));
                        cachedPlugin.put("subItem", subItem);
                        defaultPrefs.putAll((Map<?, ?>)event);
                        ServerStatus.thisObj.common_code.setPluginSettings(thePlugin, defaultPrefs);
                    }
                }
            }
            info.put("job_max_runtime_hours", ServerStatus.SG("job_max_runtime_hours"));
            info.put("job_max_runtime_minutes", ServerStatus.SG("job_max_runtime_minutes"));
            info.put("task_max_runtime_hours", ServerStatus.SG("task_max_runtime_hours"));
            info.put("task_max_runtime_minutes", ServerStatus.SG("task_max_runtime_minutes"));
            Log.log("EVENT", 0, String.valueOf(LOC.G("Event:PLUGIN")) + " " + event.getProperty("name") + ":" + pluginName + ":" + subItem + ":");
            crushftp.handlers.Common.runPlugin(pluginName, info, subItem);
            if (cachedPlugin != null && cache != null) {
                cache.addElement(cachedPlugin);
            }
            return info;
        }
        catch (Exception e) {
            Log.log("EVENT", 1, e);
            Log.log("EVENT", 0, String.valueOf(LOC.G("FAILURE:")) + event.getProperty("name") + ":" + e + "\r\n");
            return info;
        }
    }

    public synchronized void reverseProcess(Properties fileItem1, SessionCrush theSession, Properties users_with_access, Vector users, String event_type) {
        if (fileItem1 == null || fileItem1.getProperty("url", "").equals("")) {
            return;
        }
        String test_url = fileItem1.getProperty("url", "");
        if (test_url.toLowerCase().startsWith("file:/") && !test_url.toLowerCase().startsWith("file://")) {
            test_url = "file:/" + test_url.substring(test_url.indexOf(":") + 1);
        }
        if (test_url.contains("%")) {
            test_url = crushftp.handlers.Common.url_decode3(test_url);
        }
        int x = users.size() - 1;
        while (x >= 0) {
            String username = users.elementAt(x).toString();
            boolean user_is_listening_for_reverse = false;
            Properties user = UserTools.ut.getUser(theSession.uiSG("listen_ip_port"), username, true);
            if (user == null) {
                users.removeElementAt(x);
            } else {
                Vector events = (Vector)user.get("events");
                if (events == null) {
                    users.removeElementAt(x);
                } else {
                    int xx = 0;
                    while (!user_is_listening_for_reverse && xx < events.size()) {
                        Properties event_tmp = (Properties)events.elementAt(xx);
                        try {
                            if (event_tmp.getProperty("event_user_action_list", "").indexOf("(r_upload)") >= 0 && event_type.equals("UPLOAD") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_download)") >= 0 && event_type.equals("DOWNLOAD") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_delete)") >= 0 && event_type.equals("DELETE") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_rename)") >= 0 && event_type.equals("RENAME") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_makedir)") >= 0 && event_type.equals("MAKEDIR")) {
                                user_is_listening_for_reverse = true;
                            }
                        }
                        catch (Exception e) {
                            Log.log("EVENT", 1, e);
                        }
                        ++xx;
                    }
                    if (user_is_listening_for_reverse) {
                        VFS vfs = UserTools.ut.get_full_VFS(theSession.uiSG("listen_ip_port"), username, user);
                        Properties virtual = vfs.getCombinedVFS();
                        Enumeration<Object> keys = virtual.keys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement().toString();
                            if (key.equals("vfs_permissions_object")) continue;
                            Properties p = (Properties)virtual.get(key);
                            Vector v = (Vector)p.get("vItems");
                            int xx2 = 0;
                            while (v != null && xx2 < v.size()) {
                                Properties pp = (Properties)v.elementAt(xx2);
                                String test_url2 = pp.getProperty("url", "");
                                if (test_url2.toLowerCase().startsWith("file:/") && !test_url2.toLowerCase().startsWith("file://")) {
                                    test_url2 = "file:/" + test_url2.substring(test_url.indexOf(":") + 1);
                                }
                                if (test_url.toLowerCase().startsWith(test_url2.toLowerCase())) {
                                    Vector<Properties> modified_items_list = (Vector<Properties>)users_with_access.get(username);
                                    if (modified_items_list == null) {
                                        modified_items_list = new Vector<Properties>();
                                    }
                                    users_with_access.put(username, modified_items_list);
                                    Properties fileItem2 = (Properties)fileItem1.clone();
                                    String partial_path = test_url.substring(test_url2.length());
                                    if (!partial_path.startsWith("/")) {
                                        partial_path = "/" + partial_path;
                                    }
                                    Properties vfs_item = null;
                                    try {
                                        vfs_item = vfs.get_item(String.valueOf(key) + partial_path);
                                    }
                                    catch (Exception e) {
                                        Log.log("EVENT", 1, e);
                                    }
                                    if (vfs_item != null) {
                                        fileItem2.put("path", String.valueOf(vfs_item.getProperty("root_dir", "")) + vfs_item.getProperty("name", ""));
                                        fileItem2.put("the_file_path", String.valueOf(vfs_item.getProperty("root_dir", "")) + vfs_item.getProperty("name", ""));
                                    } else {
                                        fileItem2.put("path", partial_path);
                                        fileItem2.put("the_file_path", partial_path);
                                    }
                                    boolean contains = false;
                                    int xxx = 0;
                                    while (xxx < modified_items_list.size()) {
                                        Properties item = (Properties)modified_items_list.get(xxx);
                                        if (item.getProperty("url", "").equals(fileItem2.getProperty("url", "")) && item.getProperty("url_2", "").equals(fileItem2.getProperty("url_2", "")) && item.getProperty("event_type", "").equals(fileItem2.getProperty("event_type", "")) && item.getProperty("the_command", "").equals(fileItem2.getProperty("the_command", ""))) {
                                            contains = true;
                                        }
                                        ++xxx;
                                    }
                                    if (!contains) {
                                        modified_items_list.addElement(fileItem2);
                                    }
                                }
                                ++xx2;
                            }
                        }
                    }
                }
            }
            --x;
        }
    }

    public void doReverseEvent(Vector users, Vector groupedItems, final SessionCrush the_user, final String event_type) {
        if (users == null) {
            users = new Vector();
            UserTools.refreshUserList(the_user.uiSG("listen_ip_port"), users);
        }
        final Properties users_with_access = new Properties();
        int x = 0;
        while (x < groupedItems.size()) {
            this.reverseProcess((Properties)groupedItems.elementAt(x), the_user, users_with_access, users, event_type);
            ++x;
        }
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Enumeration<Object> keys = users_with_access.keys();
                    while (keys.hasMoreElements()) {
                        Vector events;
                        String username = keys.nextElement().toString();
                        if (ServerStatus.BG("reverse_events_skip_sender") && the_user != null && the_user.user != null && username.equals(the_user.user.getProperty("user_name", ""))) continue;
                        Vector modified_items_list = (Vector)users_with_access.get(username);
                        Properties user = UserTools.ut.getUser(the_user.uiSG("listen_ip_port"), username, true);
                        if (user == null || (events = (Vector)user.get("events")) == null) continue;
                        int xx = 0;
                        while (xx < events.size()) {
                            Properties event_tmp = (Properties)Common.CLONE(events.elementAt(xx));
                            try {
                                if (event_tmp.getProperty("event_user_action_list", "").indexOf("(r_upload)") >= 0 && event_type.equals("UPLOAD") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_download)") >= 0 && event_type.equals("DOWNLOAD") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_delete)") >= 0 && event_type.equals("DELETE") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_rename)") >= 0 && event_type.equals("RENAME") || event_tmp.getProperty("event_user_action_list", "").indexOf("(r_makedir)") >= 0 && event_type.equals("MAKEDIR")) {
                                    SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", the_user.uiSG("listen_ip_port"), the_user.server_item);
                                    tempSession.verify_user(username, crushftp.handlers.Common.makeBoundary(), true, false);
                                    if (tempSession.user_info != null && the_user.user != null) {
                                        tempSession.user_info.put("reverse_event_trigger_username", the_user.user.getProperty("username", ""));
                                        tempSession.user_info.put("reverse_event_trigger_user_name", the_user.user.getProperty("user_name", ""));
                                    }
                                    if (tempSession.user != null) {
                                        Properties config = new Properties();
                                        config.put("event", event_tmp);
                                        config.put("event_type", event_type);
                                        config.put("theSession", tempSession);
                                        config.put("update_tracker", "false");
                                        Vector<Properties> temp_items = new Vector<Properties>();
                                        int xxx = 0;
                                        while (xxx < modified_items_list.size()) {
                                            Properties fileItem1 = (Properties)modified_items_list.get(xxx);
                                            config.put("fileItem1", fileItem1);
                                            if (!event_tmp.getProperty("event_if_list", "").equals("") && !event_tmp.getProperty("event_dir_data", "").equals("")) {
                                                String event_if_list = "(" + event_type.toLowerCase() + "_dir)";
                                                event_if_list = crushftp.handlers.Common.replace_str(event_if_list, "makedir", "make");
                                                event_tmp.put("event_if_list", event_if_list);
                                                if (event_tmp.getProperty("name", "").startsWith("subscribe_") && event_tmp.getProperty("event_always_cb", "").equals("true")) {
                                                    event_tmp.put("event_always_cb", "false");
                                                }
                                            }
                                            if (Events.this.checkCriteriaOfEvents(config).equals("true")) {
                                                temp_items.add(fileItem1);
                                            }
                                            ++xxx;
                                        }
                                        if (temp_items.size() > 0) {
                                            Events.this.runEvent(event_tmp, tempSession, modified_items_list, false, event_type);
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                Log.log("EVENT", 1, e);
                            }
                            ++xx;
                        }
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public String checkCriteriaOfEvents(Properties config) {
        Properties event = (Properties)config.get("event");
        String event_type = config.getProperty("event_type");
        SessionCrush theSession = config.get("theSession") != null ? (SessionCrush)config.get("theSession") : null;
        Properties fileItem1 = config.get("fileItem1") != null ? (Properties)config.get("fileItem1") : null;
        Properties fileItem2 = config.get("fileItem2") != null ? (Properties)config.get("fileItem2") : null;
        String id = config.getProperty("id", "");
        boolean update_tracker = config.getProperty("update_tracker", "false").equals("true");
        boolean criteria_met = false;
        if (fileItem1 != null) {
            fileItem1.put("event_type", event_type);
        }
        if (fileItem2 != null) {
            fileItem2.put("event_type", event_type);
        }
        if (event.getProperty("event_always_cb", "").equals("true")) {
            criteria_met = true;
        } else if (event.getProperty("event_if_list", "").equals("(upload)") && event_type.equals("UPLOAD")) {
            criteria_met = true;
        } else if (event.getProperty("event_if_list", "").equals("(download)") && event_type.equals("DOWNLOAD")) {
            criteria_met = true;
        } else if (event.getProperty("event_if_list", "").equals("(pre_download)") && event_type.equals("PRE_DOWNLOAD")) {
            criteria_met = true;
        } else if (event.getProperty("event_if_list", "").equals("(real_url)") && (event_type.equals("UPLOAD") || event_type.equals("DOWNLOAD") || event_type.equals("DELETE") || event_type.equals("RENAME") || event_type.equals("MAKEDIR"))) {
            String url = ServerStatus.thisObj.change_vars_to_values(event.getProperty("event_dir_data", ""), theSession);
            String url2 = fileItem1.getProperty("url", "");
            String url3 = url;
            if (url2.toUpperCase().startsWith("FILE:/") && !url2.toUpperCase().startsWith("FILE://")) {
                url2 = "file://" + url2.substring("file:/".length());
            }
            if (url3.toUpperCase().startsWith("FILE:/") && !url3.toUpperCase().startsWith("FILE://")) {
                url3 = "file://" + url3.substring("file:/".length());
            }
            if (!url.startsWith("REGEX:") && url.indexOf("*") < 0 && url.indexOf("?") < 0) {
                if (url2.toUpperCase().startsWith(url3.toUpperCase())) {
                    criteria_met = true;
                }
            } else if (Common.do_search(url3.toUpperCase(), url2.toUpperCase(), false, 0)) {
                criteria_met = true;
            }
            if (ServerStatus.siIG("enterprise_level") <= 0 && criteria_met) {
                Log.log("EVENT", 0, "Enterprise license is required for URL matching on events.  Event has been ignored.");
                criteria_met = false;
            }
            if (criteria_met) {
                Log.log("EVENT", 0, "Matched event url:" + fileItem1.getProperty("url", "") + "   starts with:" + url);
            }
        } else if (event.getProperty("event_if_list", "").contains("(upload_dir)") && (event_type.equals("UPLOAD") || event_type.equals("RENAME") || event_type.equals("DELETE"))) {
            String path = event.getProperty("event_dir_data", "");
            path = ServerStatus.thisObj.change_vars_to_values(path, theSession);
            String path2 = fileItem1.getProperty("the_command_data");
            if (!path.startsWith("REGEX:") && path.indexOf("*") < 0 && path.indexOf("?") < 0) {
                String path3 = path2;
                if (theSession != null && path3.startsWith(theSession.SG("root_dir"))) {
                    path3 = path3.substring(theSession.SG("root_dir").length() - 1);
                }
                if (path2.toUpperCase().startsWith(path.toUpperCase()) || path3.toUpperCase().startsWith(path.toUpperCase())) {
                    if (event_type.equals("RENAME") || event_type.equals("DELETE")) {
                        if (update_tracker) {
                            this.updateTracker(id, event_type, event, fileItem1, fileItem2, theSession);
                        }
                        return "";
                    }
                    Log.log("EVENT", 0, "Matched event dir:" + path2 + "   starts with:" + path);
                    criteria_met = true;
                }
            } else if (Common.do_search(path.toUpperCase(), path2.toUpperCase(), false, 0)) {
                if (event_type.equals("RENAME") || event_type.equals("DELETE")) {
                    if (update_tracker) {
                        this.updateTracker(id, event_type, event, fileItem1, fileItem2, theSession);
                    }
                } else {
                    criteria_met = true;
                }
            }
        } else if (event.getProperty("event_if_list", "").contains("(download_dir)") && (event_type.equals("DOWNLOAD") || event_type.equals("RENAME"))) {
            String path = event.getProperty("event_dir_data", "");
            path = ServerStatus.thisObj.change_vars_to_values(path, theSession);
            String path2 = fileItem1.getProperty("the_command_data");
            if (!path.startsWith("REGEX:") && path.indexOf("*") < 0 && path.indexOf("?") < 0) {
                String path3 = path2;
                if (theSession != null && path3.startsWith(theSession.SG("root_dir"))) {
                    path3 = path3.substring(theSession.SG("root_dir").length() - 1);
                }
                if (path2.toUpperCase().startsWith(path.toUpperCase()) || path3.toUpperCase().startsWith(path.toUpperCase())) {
                    if (event_type.equals("RENAME")) {
                        if (update_tracker) {
                            this.updateTracker(id, event_type, event, fileItem1, fileItem2, theSession);
                        }
                        return "";
                    }
                    Log.log("EVENT", 0, "Matched event dir:" + path2 + "   starts with:" + path);
                    criteria_met = true;
                }
            } else if (Common.do_search(path.toUpperCase(), path2.toUpperCase(), false, 0)) {
                if (event_type.equals("RENAME")) {
                    if (update_tracker) {
                        this.updateTracker(id, event_type, event, fileItem1, fileItem2, theSession);
                    }
                } else {
                    criteria_met = true;
                }
            }
        } else if (event.getProperty("event_if_list", "").contains("(delete_dir)") && event_type.equals("DELETE")) {
            String path = event.getProperty("event_dir_data", "");
            path = ServerStatus.thisObj.change_vars_to_values(path, theSession);
            String path2 = fileItem1.getProperty("the_command_data");
            if (!path.startsWith("REGEX:") && path.indexOf("*") < 0 && path.indexOf("?") < 0) {
                String path3 = path2;
                if (theSession != null && path3.startsWith(theSession.SG("root_dir"))) {
                    path3 = path3.substring(theSession.SG("root_dir").length() - 1);
                }
                if (path2.toUpperCase().startsWith(path.toUpperCase()) || path3.toUpperCase().startsWith(path.toUpperCase())) {
                    Log.log("EVENT", 0, "Matched event dir:" + path2 + "   starts with:" + path);
                    criteria_met = true;
                }
            } else if (Common.do_search(path.toUpperCase(), path2.toUpperCase(), false, 0)) {
                criteria_met = true;
            }
        } else if (event.getProperty("event_if_list", "").contains("(rename_dir)") && event_type.equals("RENAME")) {
            String path = event.getProperty("event_dir_data", "");
            path = ServerStatus.thisObj.change_vars_to_values(path, theSession);
            String path2 = fileItem1.getProperty("the_command_data");
            if (!path.startsWith("REGEX:") && path.indexOf("*") < 0 && path.indexOf("?") < 0) {
                String path3 = path2;
                if (theSession != null && path3.startsWith(theSession.SG("root_dir"))) {
                    path3 = path3.substring(theSession.SG("root_dir").length() - 1);
                }
                if (path2.toUpperCase().startsWith(path.toUpperCase()) || path3.toUpperCase().startsWith(path.toUpperCase())) {
                    Log.log("EVENT", 0, "Matched event dir:" + path2 + "   starts with:" + path);
                    criteria_met = true;
                }
            } else if (Common.do_search(path.toUpperCase(), path2.toUpperCase(), false, 0)) {
                criteria_met = true;
            }
        } else if (event.getProperty("event_if_list", "").contains("(make_dir)") && event_type.equals("MAKEDIR")) {
            String path = event.getProperty("event_dir_data", "");
            path = ServerStatus.thisObj.change_vars_to_values(path, theSession);
            String path2 = fileItem1.getProperty("the_command_data");
            if (!path.startsWith("REGEX:") && path.indexOf("*") < 0 && path.indexOf("?") < 0) {
                String path3 = path2;
                if (theSession != null && path3.startsWith(theSession.SG("root_dir"))) {
                    path3 = path3.substring(theSession.SG("root_dir").length() - 1);
                }
                if (path2.toUpperCase().startsWith(path.toUpperCase()) || path3.toUpperCase().startsWith(path.toUpperCase())) {
                    Log.log("EVENT", 0, "Matched event dir:" + path2 + "   starts with:" + path);
                    criteria_met = true;
                }
            } else if (Common.do_search(path.toUpperCase(), path2.toUpperCase(), false, 0)) {
                criteria_met = true;
            }
        }
        return String.valueOf(criteria_met);
    }
}

