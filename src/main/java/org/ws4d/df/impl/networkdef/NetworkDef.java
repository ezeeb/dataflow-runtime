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
package org.ws4d.df.impl.networkdef;

import java.util.HashMap;
import java.util.Map;

import org.ws4d.df.api.comp.type.ComponentType;
import org.ws4d.df.api.network.ConnectionValidator;
import org.ws4d.df.api.network.NetworkBuilder;

public class NetworkDef {

	// -------------------------------------------------------------------------
	// instance members
	// -------------------------------------------------------------------------

	private final NetworkBuilder networkBuilder;
	private final Map<String, ComponentDef> componentDefs = new HashMap<String, ComponentDef>();

	// -------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------

	public NetworkDef(NetworkBuilder networkBuilder) {
		this.networkBuilder = networkBuilder;
	}

    // -------------------------------------------------------------------------
	// accessors
    // -------------------------------------------------------------------------

	public Map<String, ComponentDef> getComponentDefs() {
		return componentDefs;
	}

    // -------------------------------------------------------------------------
	// methods
    // -------------------------------------------------------------------------

	public void addComponent(String componentName, Class<?> componentClass)
			throws Exception {

		// (Validation) Component name must be unique.
		if (componentDefs.get(componentName) != null) {
			throw new Exception("Component name must be unique.");
		}

		ComponentType type = networkBuilder.getComponentTypeRegistry().get(
				componentClass);

		// Check component type
		if ((type.getSingleInPorts() == null)
				|| (type.getSingleOutPorts() == null)
				|| (type.getArrayInPorts() == null)
				|| (type.getArrayOutPorts() == null)) {
			throw new Exception(
					"component types returns null for a get*Ports() method");
		}

		componentDefs.put(componentName, new ComponentDef(componentName, type));
	}

	public void setCapacity(String receiverName, String inPortName, int index,
			int capacity) throws Exception {
		InPortDef inPort = null;
		ComponentDef receiver = componentDefs.get(receiverName);
		if (receiver == null) {
			throw new Exception("component not found");
		}

		if (index == -1) {
			inPort = receiver.getConnectedSingleInDefs().get(inPortName);
			if (inPort == null) {
				throw new Exception("port not found or not connected");
			}
		} else {
			Map<Integer, InPortDef> inPorts = receiver
					.getConnectedArrayInDefs().get(inPortName);
			if (inPorts == null) {
				throw new Exception("arrayport not found or not connected");
			}
			inPort = inPorts.get(index);
			if (inPort == null) {
				throw new Exception(
						"index in arrayport not found or not connected");
			}
		}

		inPort.setCapacity(capacity);
	}

	public void initialize(String receiverName, String inPortName, int index,
			Object packet, boolean isStatic) throws Exception {

		ComponentDef receiver = componentDefs.get(receiverName);
		if (receiver == null) {
			throw new Exception("component is null");
		}

		if (networkBuilder.getConnectionValidator() != null) {
			ComponentType receiverType = receiver.getComponentType();
			networkBuilder.getConnectionValidator().checkInitialization(
					receiverType, inPortName, index, packet, isStatic);
		}

		receiver.initialize(inPortName, index, packet, isStatic);
	}

	public void connect(String senderName, String outPortName, int outIndex,
			String receiverName, String inPortName, int inIndex)
			throws Exception {

		// (Validation) Components must have been registered.
		ComponentDef sender = componentDefs.get(senderName);
		ComponentDef receiver = componentDefs.get(receiverName);
		if (sender == null || receiver == null) {
			throw new Exception("component is null, senderName=" + senderName
					+ ", outPortName=" + outPortName + ", receiverName="
					+ receiverName + ", inPortName=" + inPortName);
		}

		ConnectionValidator validator = networkBuilder.getConnectionValidator();
		if (validator != null) {
			ComponentType senderType = sender.getComponentType();
			ComponentType receiverType = receiver.getComponentType();
			validator.checkConnection(senderType, outPortName, outIndex,
					receiverType, inPortName, inIndex);
		}

		receiver.connectInPort(inPortName, inIndex);
		sender.connectOutPort(outPortName, outIndex, receiver, inPortName,
				inIndex);
	}

	public void validate() throws Exception {
		for (ComponentDef componentDef : componentDefs.values()) {
			componentDef.validate();
		}
	}
}
