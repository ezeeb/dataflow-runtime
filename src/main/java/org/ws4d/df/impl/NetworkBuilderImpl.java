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
package org.ws4d.df.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ws4d.df.api.comp.Component;
import org.ws4d.df.api.comp.InPort;
import org.ws4d.df.api.comp.OutPort;
import org.ws4d.df.api.comp.type.ComponentType;
import org.ws4d.df.api.comp.type.ComponentTypeRegistry;
import org.ws4d.df.api.comp.type.InPortType;
import org.ws4d.df.api.comp.type.OutPortType;
import org.ws4d.df.api.data.DataProvider;
import org.ws4d.df.api.network.ConnectionValidator;
import org.ws4d.df.api.network.Network;
import org.ws4d.df.api.network.NetworkBuilder;
import org.ws4d.df.impl.networkdef.ComponentDef;
import org.ws4d.df.impl.networkdef.InPortDef;
import org.ws4d.df.impl.networkdef.NetworkDef;
import org.ws4d.df.impl.networkdef.OutPortDef;
import org.ws4d.df.impl.runtime.RtComponent;
import org.ws4d.df.impl.runtime.RtInPort;
import org.ws4d.df.impl.runtime.RtInitPort;
import org.ws4d.df.impl.runtime.RtNetwork;
import org.ws4d.df.impl.runtime.RtNullPort;
import org.ws4d.df.impl.runtime.RtOutPort;

public class NetworkBuilderImpl implements NetworkBuilder {

    // -------------------------------------------------------------------------
    // static members
    // -------------------------------------------------------------------------

    public static final int DEFAULT_CAPACITY = 10;

    // -------------------------------------------------------------------------
    // instance members
    // -------------------------------------------------------------------------

    private final NetworkDef networkDef;
    private DataProvider dataProvider = null;
    private ConnectionValidator connectionValidator = null;
    private ComponentTypeRegistry componentTypeRegistry = null;

    // This belongs to the final network that will be started.
    private Map<String, ComponentDef> componentDefs;
    private Map<String, RtComponent> rtComponents;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------

    public NetworkBuilderImpl() {
        networkDef = new NetworkDef(this);
    }

    // -------------------------------------------------------------------------
    // accessors
    // -------------------------------------------------------------------------

    public void setDataProvider(DataProvider provider) {
        if ((this.dataProvider == null) && (provider != null)) {
            this.dataProvider = provider;
            provider.setNetworlBuilder(this);
        }
    }

    public DataProvider getDataProvider() {
        return this.dataProvider;
    }

    public void setComponentTypeRegistry(ComponentTypeRegistry registry) {
        if ((this.componentTypeRegistry == null) && (registry != null)) {
            this.componentTypeRegistry = registry;
            registry.setNetworlBuilder(this);
        }
    }

    public ComponentTypeRegistry getComponentTypeRegistry() {
        return this.componentTypeRegistry;
    }

    public void setConnectionValidator(ConnectionValidator connectionValidator) {
        if ((this.connectionValidator == null) && (connectionValidator != null)) {
            this.connectionValidator = connectionValidator;
            connectionValidator.setNetworlBuilder(this);
        }
    }

    public ConnectionValidator getConnectionValidator() {
        return this.connectionValidator;
    }

    // -------------------------------------------------------------------------
    // NetworkBuilder interface
    // -------------------------------------------------------------------------

    public void addComponent(String componentName, Class<?> componentClass)
            throws Exception {
        networkDef.addComponent(componentName, componentClass);
    }

    public void setCapacity(String receiverName, String inPortName, int capacity)
            throws Exception {
        this.setCapacity(receiverName, inPortName, -1, capacity);
    }

    public void setCapacity(String receiverName, String inPortName, int index,
            int capacity) throws Exception {
        networkDef.setCapacity(receiverName, inPortName, index, capacity);
    }

    public void initialize(String receiverName, String inPortName, Object packet)
            throws Exception {
        this.initialize(receiverName, inPortName, -1, packet, false);
    }

    public void initialize(String receiverName, String inPortName, int index,
            Object packet) throws Exception {
        this.initialize(receiverName, inPortName, index, packet, false);
    }

    public void initialize(String receiverName, String inPortName,
            Object packet, boolean isStatic) throws Exception {
        this.initialize(receiverName, inPortName, -1, packet, isStatic);
    }

    public void initialize(String receiverName, String inPortName, int index,
            Object packet, boolean isStatic) throws Exception {
        networkDef
                .initialize(receiverName, inPortName, index, packet, isStatic);
    }

    public void connect(String senderName, String outPortName,
            String receiverName, String inPortName) throws Exception {
        this.connect(senderName, outPortName, -1, receiverName, inPortName, -1);
    }

    public void connect(String senderName, String outPortName, int outIndex,
            String receiverName, String inPortName) throws Exception {
        this.connect(senderName, outPortName, outIndex, receiverName,
                inPortName, -1);
    }

    public void connect(String senderName, String outPortName,
            String receiverName, String inPortName, int inIndex)
            throws Exception {
        this.connect(senderName, outPortName, -1, receiverName, inPortName,
                inIndex);
    }

    public void connect(String senderName, String outPortName, int outIndex,
            String receiverName, String inPortName, int inIndex)
            throws Exception {
        networkDef.connect(senderName, outPortName, outIndex, receiverName,
                inPortName, inIndex);
    }

    public Network buildNetwork() throws Exception {
        networkDef.validate();
        componentDefs = networkDef.getComponentDefs();
        RtNetwork rtNetwork = new RtNetwork(String.valueOf(System
                .currentTimeMillis()));
        createRtComponents(rtNetwork);
        rtNetwork.setRtComponents(rtComponents);
        createInPorts();
        createOutPorts();
        return rtNetwork;
    }

    // -------------------------------------------------------------------------
    // private
    // -------------------------------------------------------------------------

    private void createRtComponents(RtNetwork rtNetwork) throws Exception {
        rtComponents = new HashMap<String, RtComponent>();
        for (ComponentDef componentDef : componentDefs.values()) {
            String componentId = componentDef.getId();

            ComponentType componentType = componentDef.getComponentType();
            Component component = (Component) componentType.getComponentClass()
                    .getConstructor().newInstance();

            RtComponent rtComponent = new RtComponent(rtNetwork, componentId,
                    component, componentType);

            dataProvider.initComponent(componentType, component, rtComponent);

            rtComponents.put(componentId, rtComponent);
        }
    }

    private void createInPorts() throws Exception {
        for (ComponentDef componentDef : componentDefs.values()) {

            RtComponent rtComponent = rtComponents.get(componentDef.getId());

            // single in ports
            Map<String, InPortDef> connectedSingleIns = componentDef
                    .getConnectedSingleInDefs();
            if (connectedSingleIns != null) {
                for (InPortDef inPortDef : connectedSingleIns.values()) {

                    InPort inPort;
                    String inPortName = inPortDef.getName();

                    if (inPortDef.isInitializer()) {
                        inPort = new RtInitPort(inPortName, rtComponent, -1,
                                inPortDef.getInitPacket(), inPortDef.isStatic());
                    } else {
                        inPort = new RtInPort(inPortName, rtComponent, -1,
                                inPortDef.getCapacity());
                    }

                    dataProvider.initInPort(componentDef.getComponentType(),
                            rtComponent.getComponent(),
                            inPortDef.getPortType(), inPort);

                    rtComponent.addInPort(inPortName, inPort);
                }
            }

            // initialize free single in ports
            for (InPortType inPortType : componentDef.getFreeSingleInDefs()
                    .values()) {
                String inPortName = inPortType.getName();
                InPort inPort = new RtNullPort(inPortName, rtComponent, -1);

                dataProvider.initInPort(componentDef.getComponentType(),
                        rtComponent.getComponent(), inPortType, inPort);

                rtComponent.addInPort(inPortName, inPort);
            }

            // ArrayInPorts
            Map<String, Map<Integer, InPortDef>> connectedArrayIns = componentDef
                    .getConnectedArrayInDefs();
            if (connectedArrayIns != null) {
                for (Map<Integer, InPortDef> inPortDefs : connectedArrayIns
                        .values()) {
                    InPortType inPortType = null;
                    String inPortName = null;

                    List<InPort> inPorts = new ArrayList<InPort>();
                    for (int i = 0; i < inPortDefs.size(); i++) {
                        inPorts.add(i, null);
                    }

                    for (InPortDef inPortDef : inPortDefs.values()) {

                        int index = inPortDef.getIndex();
                        if (inPortType == null) {
                            inPortType = inPortDef.getPortType();
                            inPortName = inPortDef.getName();
                        }

                        InPort inPort;
                        if (inPortDef.isInitializer()) {
                            inPort = new RtInitPort(inPortName, rtComponent,
                                    index, inPortDef.getInitPacket(),
                                    inPortDef.isStatic());
                        } else {
                            inPort = new RtInPort(inPortName, rtComponent,
                                    index, inPortDef.getCapacity());
                        }
                        inPorts.set(index, inPort);
                    }

                    dataProvider.initInPortArray(
                            componentDef.getComponentType(),
                            rtComponent.getComponent(), inPortType, inPorts);
                    rtComponent.addInPorts(inPortName, inPorts);
                }
            }

            // initialize free inPortArrays
            for (InPortType inPort : componentDef.getFreeArrayInDefs().values()) {
                List<InPort> inPorts = Collections.emptyList();

                dataProvider.initInPortArray(componentDef.getComponentType(),
                        rtComponent.getComponent(), inPort, inPorts);

                rtComponent.addInPorts(inPort.getName(), inPorts);
            }
        }
    }

    private void createOutPorts() throws Exception {
        for (ComponentDef componentDef : componentDefs.values()) {

            RtComponent rtComponent = rtComponents.get(componentDef.getId());

            Map<String, OutPortDef> connectedSingleOuts = componentDef
                    .getConnectedSingleOutDefs();
            if (connectedSingleOuts != null) {
                for (OutPortDef outPortDef : connectedSingleOuts.values()) {

                    String outPortName = outPortDef.getName();
                    RtComponent receiver = rtComponents.get(outPortDef
                            .getReceiverName());

                    // TODO replace with method
                    RtInPort rtInPort;
                    if (outPortDef.getInIndex() == -1) {
                        rtInPort = (RtInPort) receiver.getInPort(outPortDef
                                .getInPortName());
                    } else {
                        rtInPort = (RtInPort) receiver.getInPorts(
                                outPortDef.getInPortName()).get(
                                outPortDef.getInIndex());
                    }

                    RtOutPort rtOutPort = new RtOutPort(outPortName,
                            rtComponent, -1, rtInPort);

                    rtInPort.setRtOutPort(rtOutPort);

                    dataProvider.initOutPort(componentDef.getComponentType(),
                            rtComponent.getComponent(),
                            outPortDef.getPortType(), rtOutPort);

                    rtComponent.addOutPort(outPortName, rtOutPort);
                }
            }

            // initialize free single out ports
            for (OutPortType outPortType : componentDef.getFreeSingleOutDefs()
                    .values()) {

                String outPortName = outPortType.getName();
                OutPort outPort = new RtNullPort(outPortName, rtComponent, -1);

                dataProvider.initOutPort(componentDef.getComponentType(),
                        rtComponent.getComponent(), outPortType, outPort);

                rtComponent.addOutPort(outPortName, outPort);
            }

            // ArrayOutPorts
            Map<String, Map<Integer, OutPortDef>> connectedArrayOuts = componentDef
                    .getConnectedArrayOutDefs();
            if (connectedArrayOuts != null) {
                for (Map<Integer, OutPortDef> outPortDefs : connectedArrayOuts
                        .values()) {
                    OutPortType outPortType = null;
                    String outPortName = null;

                    List<OutPort> outPorts = new ArrayList<OutPort>();
                    for (int i = 0; i < outPortDefs.size(); i++) {
                        outPorts.add(i, null);
                    }

                    for (OutPortDef outPortDef : outPortDefs.values()) {

                        int index = outPortDef.getIndex();
                        if (outPortType == null) {
                            outPortType = outPortDef.getPortType();
                            outPortName = outPortDef.getName();
                        }

                        RtComponent receiver = rtComponents.get(outPortDef
                                .getReceiverName());

                        // TODO replace with method
                        RtInPort rtInPort;
                        if (outPortDef.getInIndex() == -1) {
                            rtInPort = (RtInPort) receiver.getInPort(outPortDef
                                    .getInPortName());
                        } else {
                            rtInPort = (RtInPort) receiver.getInPorts(
                                    outPortDef.getInPortName()).get(
                                    outPortDef.getInIndex());
                        }

                        RtOutPort rtOutPort = new RtOutPort(outPortName,
                                rtComponent, index, rtInPort);

                        rtInPort.setRtOutPort(rtOutPort);
                        outPorts.set(index, rtOutPort);
                    }

                    dataProvider.initOutPortArray(
                            componentDef.getComponentType(),
                            rtComponent.getComponent(), outPortType, outPorts);

                    rtComponent.addOutPorts(outPortName, outPorts);
                }
            }

            // initialize all free outPortArrays
            for (OutPortType outPortType : componentDef.getFreeArrayOutDefs()
                    .values()) {
                List<OutPort> outPorts = Collections.emptyList();

                dataProvider.initOutPortArray(componentDef.getComponentType(),
                        rtComponent.getComponent(), outPortType, outPorts);

                rtComponent.addOutPorts(outPortType.getName(), outPorts);
            }
        }
    }
}
