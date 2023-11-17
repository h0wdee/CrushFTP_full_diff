/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components;

import com.maverick.ssh.SecureComponent;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentFactory<T>
implements Cloneable {
    static Logger log = LoggerFactory.getLogger(ComponentFactory.class);
    protected Hashtable<String, Class<? extends T>> supported = new Hashtable();
    protected Vector<String> order = new Vector();
    private boolean locked = false;
    private ComponentManager componentManager;
    private SecurityLevel securityLevel = SecurityLevel.WEAK;

    public ComponentFactory(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public SecurityLevel getSecurityLevel() {
        return this.securityLevel;
    }

    public synchronized String changePositionofAlgorithm(String name, int position) throws SshException {
        int currentLocation;
        if (position < 0) {
            throw new SshException("index out of bounds", 4);
        }
        if (!this.contains(name)) {
            throw new SshException(name + " is not a supported algorithm", 4);
        }
        if (position >= this.order.size()) {
            position = this.order.size();
        }
        if ((currentLocation = this.order.indexOf(name)) < position) {
            this.order.insertElementAt(name, position);
            if (currentLocation > 0) {
                this.order.removeElementAt(currentLocation);
            }
        } else {
            this.order.removeElementAt(currentLocation);
            this.order.insertElementAt(name, position);
        }
        if (log.isDebugEnabled()) {
            log.debug("Moved algorithm preferences {} to position {}", (Object)name, (Object)position);
        }
        return this.order.elementAt(0);
    }

    public Collection<String> names() {
        return this.supported.keySet();
    }

    public synchronized String order(String[] ordering) throws SshException {
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Components cannot be added");
        }
        Vector<String> newOrder = new Vector<String>();
        for (String alg : ordering) {
            if (!this.supported.containsKey(alg)) continue;
            newOrder.add(alg);
        }
        if (newOrder.size() == 0) {
            throw new SshException("No algorithms supported", 4);
        }
        this.order = newOrder;
        if (log.isDebugEnabled()) {
            log.debug("Ordered algorithm preferences to {}", (Object)Utils.csv(this.order));
        }
        return this.order.get(0);
    }

    public synchronized String createNewOrdering(int[] ordering) throws SshException {
        int i;
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Components cannot be added");
        }
        if (ordering.length > this.order.size()) {
            throw new SshException("too many indicies", 4);
        }
        for (i = 0; i < ordering.length; ++i) {
            if (ordering[i] < 0 || ordering[i] >= this.order.size()) {
                throw new SshException("index out of bounds", 4);
            }
            this.order.insertElementAt(this.order.elementAt(ordering[i]), this.order.size());
        }
        Arrays.sort(ordering);
        for (i = ordering.length - 1; i >= 0; --i) {
            this.order.removeElementAt(ordering[i]);
        }
        for (i = 0; i < ordering.length; ++i) {
            String element = this.order.elementAt(this.order.size() - 1);
            this.order.removeElementAt(this.order.size() - 1);
            this.order.insertElementAt(element, 0);
        }
        if (log.isDebugEnabled()) {
            log.debug("Ordered algorithm preferences to {}", (Object)Utils.csv(this.order));
        }
        return this.order.elementAt(0);
    }

    public boolean contains(String name) {
        return this.supported.containsKey(name);
    }

    public synchronized String list(String preferred, String ... ignores) {
        return this.createDelimitedList(preferred, ignores);
    }

    public synchronized String list(String preferred) {
        return this.createDelimitedList(preferred, new String[0]);
    }

    public synchronized void add(String name, Class<? extends T> cls) {
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Components cannot be added");
        }
        this.supported.put(name, cls);
        if (!this.order.contains(name)) {
            this.order.addElement(name);
        }
    }

    public Object getInstance(String name) throws SshException {
        if (this.supported.containsKey(name)) {
            try {
                return this.createInstance(name, this.supported.get(name));
            }
            catch (Throwable t) {
                throw new SshException(t.getMessage(), 5, t);
            }
        }
        throw new SshException(name + " is not supported", 7);
    }

    protected T createInstance(String name, Class<? extends T> cls) throws Throwable {
        return cls.newInstance();
    }

    private synchronized String createDelimitedList(String preferred, String ... ignores) {
        StringBuffer listBuf = new StringBuffer();
        int prefIndex = this.order.indexOf(preferred);
        if (prefIndex != -1 && !this.isDisabled(preferred)) {
            listBuf.append(preferred);
        }
        for (int i = 0; i < this.order.size(); ++i) {
            if (prefIndex == i || this.isDisabled(this.order.elementAt(i))) continue;
            boolean ignoreItem = false;
            for (String ignore : ignores) {
                if (!this.order.elementAt(i).equals(ignore)) continue;
                ignoreItem = true;
                break;
            }
            if (ignoreItem) continue;
            if (listBuf.length() > 0) {
                listBuf.append(",");
            }
            listBuf.append(this.order.elementAt(i));
        }
        return listBuf.toString();
    }

    private boolean isDisabled(String alg) {
        return this.componentManager != null && this.componentManager.isDisabled(alg);
    }

    public synchronized void remove(String name) {
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Components cannot be added");
        }
        this.order.removeElement(name);
        if (log.isDebugEnabled()) {
            log.debug("Removed algorithm {}", (Object)name);
        }
    }

    public synchronized void clear() {
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Removing all components renders it unusable");
        }
        this.supported.clear();
        this.order.removeAllElements();
        if (log.isDebugEnabled()) {
            log.debug("Cleared all algorithms");
        }
    }

    public Object clone() {
        ComponentFactory<T> clone = new ComponentFactory<T>(this.componentManager);
        clone.order = (Vector)this.order.clone();
        clone.supported = (Hashtable)this.supported.clone();
        return clone;
    }

    public String[] toArray() {
        return this.order.toArray(new String[this.order.size()]);
    }

    public synchronized void removeAllBut(String names) {
        if (this.locked) {
            throw new IllegalStateException("Component factory is locked. Components cannot be added");
        }
        StringTokenizer t = new StringTokenizer(names, ",");
        Vector<String> v = new Vector<String>();
        while (t.hasMoreTokens()) {
            String name = t.nextToken();
            if (!this.supported.containsKey(name)) continue;
            v.add(name);
        }
        Enumeration<String> e = this.supported.keys();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            if (v.contains(name)) continue;
            this.remove(name);
        }
        if (log.isDebugEnabled()) {
            log.debug("Reconfigured algorithm preferences to {}", (Object)Utils.csv(this.order));
        }
    }

    public void lockComponents() {
        this.locked = true;
    }

    public void configureSecurityLevel(SecurityLevel securityLevel) throws SshException {
        this.configureSecurityLevel(securityLevel, false);
    }

    public void configureSecurityLevel(SecurityLevel securityLevel, boolean locked) throws SshException {
        this.locked = false;
        ArrayList<SecureComponent> list = new ArrayList<SecureComponent>();
        Enumeration<String> e = this.supported.keys();
        while (e.hasMoreElements()) {
            String name = e.nextElement();
            SecureComponent o = (SecureComponent)this.getInstance(name);
            if (o.getSecurityLevel().ordinal() < securityLevel.ordinal()) {
                this.remove(name);
                continue;
            }
            list.add(o);
        }
        Collections.sort(list, new Comparator<SecureComponent>(){

            @Override
            public int compare(SecureComponent o1, SecureComponent o2) {
                return new Integer(o2.getPriority()).compareTo(o1.getPriority());
            }
        });
        Vector<String> newOrder = new Vector<String>();
        for (SecureComponent alg : list) {
            newOrder.add(alg.getAlgorithm());
        }
        if (newOrder.size() == 0) {
            throw new SshException("No algorithms supported", 4);
        }
        this.order = newOrder;
        this.securityLevel = securityLevel;
        this.locked = locked;
        if (log.isDebugEnabled()) {
            log.debug("Reconfigured security level {} to  preferences to {}", (Object)securityLevel.name(), (Object)Utils.csv(this.order));
        }
    }

    public String selectStrongestComponent(String[] remoteAlgs) throws SshException {
        HashMap<String, SecureComponent> list = new HashMap<String, SecureComponent>();
        for (String name : this.supported.keySet()) {
            SecureComponent o = (SecureComponent)this.getInstance(name);
            list.put(name, o);
        }
        SecureComponent strongest = null;
        for (String remoteAlg : remoteAlgs) {
            SecureComponent component = (SecureComponent)list.get(remoteAlg);
            if (component == null) continue;
            if (strongest == null) {
                strongest = component;
                continue;
            }
            if (new Integer(component.getPriority()).compareTo(strongest.getPriority()) <= 0) continue;
            strongest = component;
        }
        if (strongest == null) {
            throw new SshException("Failed to negotiate component", 9);
        }
        if (log.isDebugEnabled()) {
            log.debug("Selecting strongest component {}", (Object)strongest.getAlgorithm());
        }
        return strongest.getAlgorithm();
    }

    public boolean hasComponents() {
        return !this.supported.isEmpty();
    }

    public Collection<String> order() {
        return this.order;
    }

    public String list() {
        return this.list("");
    }

    public SecurityLevel getSecurityLevel(String algorithm) {
        try {
            return ((SecureComponent)this.getInstance(algorithm)).getSecurityLevel();
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public SecurityLevel getMaximumSecurity() {
        try {
            SecurityLevel level = null;
            for (String algorithm : this.order) {
                SecurityLevel tmp = ((SecureComponent)this.getInstance(algorithm)).getSecurityLevel();
                if (level == null) {
                    level = tmp;
                }
                if (tmp.ordinal() <= level.ordinal()) continue;
                level = tmp;
            }
            return level;
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public SecurityLevel getMinimumSecurity() {
        try {
            SecurityLevel level = null;
            for (String algorithm : this.order) {
                SecurityLevel tmp = ((SecureComponent)this.getInstance(algorithm)).getSecurityLevel();
                if (level == null) {
                    level = tmp;
                }
                if (tmp.ordinal() >= level.ordinal()) continue;
                level = tmp;
            }
            return level;
        }
        catch (SshException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String filter(String list, String ... ignores) {
        if (list == null) {
            return list;
        }
        Vector<String> newOrder = new Vector<String>();
        for (String alg : list.split(",")) {
            if (!this.supported.containsKey(alg)) continue;
            newOrder.add(alg);
        }
        return Utils.csv(newOrder);
    }

    public String listInclusive(String preference, Set<String> includes) {
        ArrayList<String> newOrder = new ArrayList<String>();
        if (includes.contains(preference) && this.supported.containsKey(preference)) {
            newOrder.add(preference);
        }
        for (String o : this.order) {
            if (!includes.contains(o) || o.equals(preference)) continue;
            newOrder.add(o);
        }
        return Utils.csv(newOrder);
    }
}

