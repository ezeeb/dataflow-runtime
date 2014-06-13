/**
 * Copyright (C) 2014 PipesBox UG (haftungsbeschränkt) (elmar.zeeb@pipesbox.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ws4d.df.impl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.df.api.comp.Component;
import org.ws4d.df.api.comp.ComponentContext;
import org.ws4d.df.api.comp.ComponentState;
import org.ws4d.df.api.comp.InPort;
import org.ws4d.df.api.comp.OutPort;
import org.ws4d.df.api.comp.type.ComponentType;
import org.ws4d.df.api.network.NetworkContext;

public class RtComponent extends Thread implements ComponentContext {

    // -------------------------------------------------------------------------
    // instance members
    // -------------------------------------------------------------------------

    private final RtNetwork rtNetwork;
    private final String componentId;
    private final Component component;
    private final ComponentType componentType;
    private final boolean isActiveComponent;

    private ComponentState state = ComponentState.INITIALIZED;
    private Map<String, InPort> singleInPorts = null;
    private Map<String, List<InPort>> arrayInPorts = null;
    private Map<String, OutPort> singleOutPorts = null;
    private Map<String, List<OutPort>> arrayOutPorts = null;
    private List<InPort> allInPorts = null;
    private List<OutPort> allOutPorts = null;

    private final ReentrantLock lock;
    private final Condition runCondition;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------

    public RtComponent(RtNetwork rtNetwork, String componentId,
            Component component, ComponentType componentType) {
        this.rtNetwork = rtNetwork;
        this.componentId = componentId;
        this.component = component;
        this.componentType = componentType;
        this.setName("[" + componentType.getName() + "] " + componentId);
        this.isActiveComponent = componentType.isActive();
        lock = new ReentrantLock();
        runCondition = lock.newCondition();
    }

    // -------------------------------------------------------------------------
    // accessors
    // -------------------------------------------------------------------------

    public RtNetwork getRtNetwork() {
        return rtNetwork;
    }

    public Component getComponent() {
        return component;
    }

    public boolean isActiveComponent() {
        return isActiveComponent;
    }

    public ComponentState getComponentState() {
        return state;
    }

    private void setState(ComponentState state) {
        this.state = state;

        if (rtNetwork.getComponentTracer() != null) {
            rtNetwork.getComponentTracer().changeState(this, this.state);
        }
    }

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------

    public void addInPort(String inPortName, InPort inPort) {
        if (singleInPorts == null) {
            singleInPorts = new HashMap<String, InPort>();
        }

        singleInPorts.put(inPortName, inPort);
        allInPorts = null;
    }

    public void addInPorts(String inPortName, List<InPort> inPorts) {
        if (arrayInPorts == null) {
            arrayInPorts = new HashMap<String, List<InPort>>();
        }

        arrayInPorts.put(inPortName, inPorts);
        allInPorts = null;
    }

    public void addOutPort(String outPortName, OutPort outPort) {
        if (singleOutPorts == null) {
            singleOutPorts = new HashMap<String, OutPort>();
        }

        singleOutPorts.put(outPortName, outPort);
        allOutPorts = null;
    }

    public void addOutPorts(String outPortName, List<OutPort> outPorts) {
        if (arrayOutPorts == null) {
            arrayOutPorts = new HashMap<String, List<OutPort>>();
        }

        arrayOutPorts.put(outPortName, outPorts);
        allOutPorts = null;
    }

    public InPort getInPort(String name) {
        return singleInPorts != null ? singleInPorts.get(name) : null;
    }

    public List<InPort> getInPorts(String name) {
        return arrayInPorts != null ? arrayInPorts.get(name) : null;
    }

    public OutPort getOutPort(String name) {
        return singleOutPorts != null ? singleOutPorts.get(name) : null;
    }

    public List<OutPort> getOutPorts(String name) {
        return arrayOutPorts != null ? arrayOutPorts.get(name) : null;
    }

    public void closeAllInPorts() {
        for (InPort inPort : getAllInPorts()) {
            inPort.close();
        }
    }

    public void closeAllOutPorts() {
        for (OutPort outPort : getAllOutPorts()) {
            outPort.close();
        }
    }

    public void closeAllPorts() {
        closeAllInPorts();
        closeAllOutPorts();
    }

    // todo
    public void init() {
    }

    public void runComponent() {
        if (state == ComponentState.INITIALIZED) {
            // This thread hasn't been started yet.
            setState(ComponentState.ACTIVE);
            super.start();
        } else if (state == ComponentState.RESETTED) {
            lock.lock();
            setState(ComponentState.ACTIVE);
            runCondition.signal();
            lock.unlock();
        } else {
            throw new IllegalStateException("");
        }
    }

    public void finish() {
        component.finish();
        setState(ComponentState.FINISHED);
    }

    public void reset() {
        if (getComponentState() != ComponentState.INITIALIZED) {
            component.reset();
            setState(ComponentState.RESETTED);
        }
        for (InPort inPort : getAllInPorts()) {
            // todo that's dirty
            ((AbstractPort) inPort).reset();
        }
        for (OutPort outPort : getAllOutPorts()) {
            // todo that's dirty
            ((AbstractPort) outPort).reset();
        }
    }

    public void terminate() {
        component.terminate();
        setState(ComponentState.TERMINATED);
        lock.lock();
        runCondition.signal();
        lock.unlock();
    }

    // -------------------------------------------------------------------------
    // Context interface
    // -------------------------------------------------------------------------
    
    public String getComponentId() {
        return componentId;
    }

    public NetworkContext getNetwork() {
        return rtNetwork;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public Logger getLogger() {
        return rtNetwork.getLogger();
    }

    public Object getNetworkProperty(String name) {
        return rtNetwork.getNetworkProperties().get(name);
    }

    public void setNetworkProperty(String name, Object property) {
        rtNetwork.getNetworkProperties().put(name, property);
    }

    public Object getRuntimeProperty(String name) {
        return rtNetwork.getRuntimeProperties().get(name);
    }

    public List<InPort> getAllInPorts() {
        if (allInPorts != null) {
            return allInPorts;
        } else {
            List<InPort> inPortsList = new ArrayList<InPort>();

            if (singleInPorts != null) {
                for (InPort inPort : singleInPorts.values()) {
                    inPortsList.add(inPort);
                }
            }

            if (arrayInPorts != null) {
                for (List<InPort> inPorts : arrayInPorts.values()) {
                    for (InPort inPort : inPorts) {
                        inPortsList.add(inPort);
                    }
                }
            }

            allInPorts = Collections.unmodifiableList(inPortsList);
            return allInPorts;
        }
    }

    public List<OutPort> getAllOutPorts() {
        if (allOutPorts != null) {
            return allOutPorts;
        } else {
            List<OutPort> outPortsList = new ArrayList<OutPort>();

            if (singleOutPorts != null) {
                for (OutPort outPort : singleOutPorts.values()) {
                    outPortsList.add(outPort);
                }
            }

            if (arrayOutPorts != null) {
                for (List<OutPort> outPorts : arrayOutPorts.values()) {
                    for (OutPort outPort : outPorts) {
                        outPortsList.add(outPort);
                    }
                }
            }

            allOutPorts = Collections.unmodifiableList(outPortsList);
            return allOutPorts;
        }
    }

    // -------------------------------------------------------------------------
    // Thread
    // -------------------------------------------------------------------------

    @Override
    public void run() {
        while (true) {
            while (true) {
                setState(ComponentState.ACTIVE);
                try {
                    component.execute();
                } catch (Exception e) {
                    logException(Level.SEVERE,
                            "Uncaught Exception in component "
                                    + component.getClass().getName(), e);
                }
                if (allPortsClosed()) {
                    rtNetwork.componentClosed(this);
                    break;
                }
                synchronized (this) {
                    if (keepAlive()) {
                        setState(ComponentState.INACTIVE);
                        waitForData();
                    }
                }
            }
            if (rtNetwork.isOneTimeMode()) {
                break;
            } else {
                try {
                    lock.lock();
                    if (state != ComponentState.TERMINATED) {
                        runCondition.await();
                    }
                    lock.unlock();
                } catch (Exception e) {
                    logException(Level.SEVERE, "???", e);
                    return;
                }
                if (state == ComponentState.TERMINATED) {
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // private
    // -------------------------------------------------------------------------
    
    private void logException(Level level, String msg, Exception e) {
        Logger logger = rtNetwork.getLogger();
        if (logger != null) {
            logger.log(level, msg, e);
        } else {
            System.out.println(msg);
            e.printStackTrace();
        }
    }

    private boolean keepAlive() {
        // Keep alive if all of the follow conditions hold:
        // (1) At least one open connected in port.
        // (2) Queue of (1) is empty.
        // (3) Connected out port of (1) is open.
        boolean keepAlive = false;
        for (InPort inPort : getAllInPorts()) {
            if (inPort instanceof RtInPort) {
                RtInPort rtInPort = (RtInPort) inPort;
                if (!rtInPort.isClosed() && rtInPort.getPacketCount() == 0
                        && !rtInPort.getRtOutPort().isClosed()) {
                    keepAlive = true;
                    break;
                }
            }
        }
        return keepAlive;
    }

    private boolean allInPortsClosed() {
        boolean allClosed = true;
        for (InPort inPort : getAllInPorts()) {
            if (!inPort.isClosed()) {
                allClosed = false;
                break;
            }
        }
        return allClosed;
    }

    private boolean allPortsClosed() {
        boolean allClosed = allInPortsClosed();
        if (allClosed) {
            for (OutPort outPort : getAllOutPorts()) {
                if (!outPort.isClosed()) {
                    allClosed = false;
                    break;
                }
            }
        }
        return allClosed;
    }

    private void waitForData() {
        boolean hasData = false;
        for (InPort inPort : getAllInPorts()) {
            // todo dirty cast
            if (((RuntimeInPort) inPort).getPacketCount() > 0) {
                hasData = true;
                break;
            }
        }
        if (!hasData) {
            try {
                wait();
            } catch (InterruptedException e) {
                logException(Level.SEVERE, "???", e);
            }
        }
    }
}
