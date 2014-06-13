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
import org.ws4d.df.api.comp.type.InPortType;
import org.ws4d.df.api.comp.type.OutPortType;
import org.ws4d.df.api.exception.PortNotFoundException;

public class ComponentDef {

	// -------------------------------------------------------------------------
	// instance members
	// -------------------------------------------------------------------------

	private final ComponentType componentType;
	private final String id;

	// All In and Out types as defined in component type (will be copied) and in
	// this component

	private final Map<String, InPortType> allSingleInTypes;
	private final Map<String, InPortType> allArrayInTypes;
	private final Map<String, OutPortType> allSingleOutTypes;
	private final Map<String, OutPortType> allArrayOutTypes;

	// Copy of all types

	private final Map<String, InPortType> freeSingleInTypes;
	private final Map<String, InPortType> freeArrayInTypes;
	private final Map<String, OutPortType> freeSingleOutTypes;
	private final Map<String, OutPortType> freeArrayOutTypes;

	// Created In and Out PortDefs while defining connections

	private Map<String, InPortDef> connectedSingleInDefs = null;
	private Map<String, Map<Integer, InPortDef>> connectedArrayInDefs = null;
	private Map<String, OutPortDef> connectedSingleOutDefs = null;
	private Map<String, Map<Integer, OutPortDef>> connectedArrayOutDefs = null;

	// -----------------------------------------------------------------------------------------------------------
	//
	// constructors
	//
	// -----------------------------------------------------------------------------------------------------------

	public ComponentDef(String id, ComponentType componentType) {
		this.componentType = componentType;
		this.id = id;

		// Copy in and out defs from component type
		allSingleInTypes = componentType.getSingleInPorts();
		allArrayInTypes = componentType.getArrayInPorts();
		allSingleOutTypes = componentType.getSingleOutPorts();
		allArrayOutTypes = componentType.getArrayOutPorts();

		freeSingleInTypes = new HashMap<String, InPortType>(allSingleInTypes);
		freeArrayInTypes = new HashMap<String, InPortType>(allArrayInTypes);
		freeSingleOutTypes = new HashMap<String, OutPortType>(allSingleOutTypes);
		freeArrayOutTypes = new HashMap<String, OutPortType>(allArrayOutTypes);
	}

	// -------------------------------------------------------------------------
	// accessors
	// -------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public Map<String, InPortType> getAllSingleInDefs() {
		return allSingleInTypes;
	}

	public Map<String, InPortType> getAllArrayInDefs() {
		return allArrayInTypes;
	}

	public Map<String, OutPortType> getAllSingleOutDefs() {
		return allSingleOutTypes;
	}

	public Map<String, OutPortType> getAllArrayOutDefs() {
		return allArrayOutTypes;
	}

	public Map<String, InPortType> getFreeSingleInDefs() {
		return freeSingleInTypes;
	}

	public Map<String, InPortType> getFreeArrayInDefs() {
		return freeArrayInTypes;
	}

	public Map<String, OutPortType> getFreeSingleOutDefs() {
		return freeSingleOutTypes;
	}

	public Map<String, OutPortType> getFreeArrayOutDefs() {
		return freeArrayOutTypes;
	}

	public Map<String, InPortDef> getConnectedSingleInDefs() {
		return connectedSingleInDefs;
	}

	public Map<String, Map<Integer, InPortDef>> getConnectedArrayInDefs() {
		return connectedArrayInDefs;
	}

	public Map<String, OutPortDef> getConnectedSingleOutDefs() {
		return connectedSingleOutDefs;
	}

	public Map<String, Map<Integer, OutPortDef>> getConnectedArrayOutDefs() {
		return connectedArrayOutDefs;
	}

	// -------------------------------------------------------------------------
	// public methods
	// -------------------------------------------------------------------------

	public void initialize(String inPortName, int index, Object packet,
			boolean isStatic) throws Exception {
		if (index == -1) {
			// (Validation) in port exists and is a single port.
			assertSingleInPortName(inPortName);

			// (Validation) in port must not have been connected.
			if ((connectedSingleInDefs != null)
					&& (connectedSingleInDefs.get(inPortName) != null)) {
				throw new Exception("already connected or initialized");
			}

			connectSingleInPort(new InPortDef(allSingleInTypes.get(inPortName),
					index, packet, isStatic));

		} else {
			// (Validation) in port exists and is an array port.
			assertArrayInPortName(inPortName);

			// (Validation) in port must not have been connected.
			if ((connectedArrayInDefs != null)
					&& (connectedArrayInDefs.get(inPortName) != null)
					&& (connectedArrayInDefs.get(inPortName).get(index) != null)) {
				throw new Exception("in port already connected or initialized");
			}

			connectArrayInPort(new InPortDef(allArrayInTypes.get(inPortName),
					index, packet, isStatic));
		}
	}

	public void connectInPort(String inPortName, int index) throws Exception {
		if (index == -1) {
			// (Validation) in port exists and is a single port.
			assertSingleInPortName(inPortName);

			// (Validation) in port must not have been connected.
			if ((connectedSingleInDefs != null)
					&& (connectedSingleInDefs.get(inPortName) != null)) {
				throw new Exception("in port already connected or initialized");
			}

			connectSingleInPort(new InPortDef(allSingleInTypes.get(inPortName)));

		} else {
			// (Validation) in port exists and is an array port.
			assertArrayInPortName(inPortName);

			// (Validation) in port must not have been connected.
			if ((connectedArrayInDefs != null)
					&& (connectedArrayInDefs.get(inPortName) != null)
					&& (connectedArrayInDefs.get(inPortName).get(index) != null)) {
				throw new Exception("in port already connected or initialized");
			}

			connectArrayInPort(new InPortDef(allArrayInTypes.get(inPortName),
					index));
		}
	}

	public void connectOutPort(String outPortName, int outIndex,
			ComponentDef receiver, String inPortName, int inIndex)
			throws Exception {
		if (outIndex == -1) {
			// (Validation) out port exists and is a single port.
			assertSingleOutPortName(outPortName);

			// (Validation) out port must not have been connected.
			if ((connectedSingleOutDefs != null)
					&& (connectedSingleOutDefs.get(outPortName) != null)) {
				throw new Exception("already connected");
			}

			InPortDef inPort = receiver.getInPort(inPortName, inIndex);
			connectSingleOutPort(new OutPortDef(
					allSingleOutTypes.get(outPortName), outIndex,
					receiver.getId(), inPort));

		} else {
			// (Validation) out port exists and is an array port.
			assertArrayOutPortName(outPortName);

			// (Validation) out port must not have been connected.
			if ((connectedArrayOutDefs != null)
					&& (connectedArrayOutDefs.get(outPortName) != null)
					&& (connectedArrayOutDefs.get(outPortName).get(outIndex) != null)) {
				throw new Exception("out port already connected or initialized");
			}

			InPortDef inPort = receiver.getInPort(inPortName, inIndex);
			connectArrayOutPort(new OutPortDef(
					allArrayOutTypes.get(outPortName), outIndex,
					receiver.getId(), inPort));
		}
	}

	public void validate() throws Exception {
		for (InPortType inDef : freeSingleInTypes.values()) {
			if (!inDef.isOptional()) {
				throw new Exception("In-port \"" + inDef.getName()
						+ "\" must be connected.");
			}
		}

		for (InPortType inDef : freeArrayInTypes.values()) {
			if (!inDef.isOptional()) {
				throw new Exception("in must be connected");
			}
		}

		if (connectedArrayInDefs != null) {
			for (Map<Integer, InPortDef> connectedArrayItemIns : connectedArrayInDefs
					.values()) {
				int maxIndex = 0;
				for (int index : connectedArrayItemIns.keySet()) {
					if (index > maxIndex) {
						maxIndex = index;
					}
				}
				if (maxIndex + 1 != connectedArrayItemIns.size()) {
					throw new Exception("gaps detected");
				}
			}
		}

		if (connectedArrayOutDefs != null) {
			for (Map<Integer, OutPortDef> connectedArrayItemOuts : connectedArrayOutDefs
					.values()) {
				int maxIndex = 0;
				for (int index : connectedArrayItemOuts.keySet()) {
					if (index > maxIndex) {
						maxIndex = index;
					}
				}
				if (maxIndex + 1 != connectedArrayItemOuts.size()) {
					throw new Exception("gaps detected");
				}
			}
		}
	}

	public InPortDef getInPort(String inPortName, int index) {
		if (index == -1) {
			return connectedSingleInDefs.get(inPortName);
		} else {
			if (connectedArrayInDefs != null) {
				return connectedArrayInDefs.get(inPortName).get(index);
			} else {
				return null;
			}
		}
	}

	// -------------------------------------------------------------------------
	// private
	// -------------------------------------------------------------------------

	private void assertSingleInPortName(String name) throws Exception {
		if (!allSingleInTypes.containsKey(name)) {
			throw new Exception("no single in port for name=" + name);
		}
	}

	private void assertArrayInPortName(String name) throws Exception {
		if (!allArrayInTypes.containsKey(name)) {
			throw new PortNotFoundException("no array in port for name=" + name);
		}
	}

	private void assertSingleOutPortName(String name) throws Exception {
		if (!allSingleOutTypes.containsKey(name)) {
			throw new PortNotFoundException("no single out port for name="
					+ name);
		}
	}

	private void assertArrayOutPortName(String name) throws Exception {
		if (!allArrayOutTypes.containsKey(name)) {
			throw new PortNotFoundException("no array out port for name="
					+ name);
		}
	}

	private void connectSingleInPort(InPortDef inPortDef) {
		String inPortName = inPortDef.getName();

		if (connectedSingleInDefs == null) {
			connectedSingleInDefs = new HashMap<String, InPortDef>();
		}

		connectedSingleInDefs.put(inPortName, inPortDef);
		freeSingleInTypes.remove(inPortName);
	}

	private void connectArrayInPort(InPortDef inPortDef) {
		String inPortName = inPortDef.getName();
		int index = inPortDef.getIndex();

		if (connectedArrayInDefs == null) {
			connectedArrayInDefs = new HashMap<String, Map<Integer, InPortDef>>();
		}

		Map<Integer, InPortDef> connectedArrayItemIns = connectedArrayInDefs
				.get(inPortName);

		if (connectedArrayItemIns == null) {
			connectedArrayItemIns = new HashMap<Integer, InPortDef>();
			connectedArrayInDefs.put(inPortName, connectedArrayItemIns);
		}

		connectedArrayItemIns.put(index, inPortDef);
		freeArrayInTypes.remove(inPortName);
	}

	private void connectSingleOutPort(OutPortDef outPortDef) {
		String outPortName = outPortDef.getName();

		if (connectedSingleOutDefs == null) {
			connectedSingleOutDefs = new HashMap<String, OutPortDef>();
		}

		connectedSingleOutDefs.put(outPortName, outPortDef);
		freeSingleOutTypes.remove(outPortName);
	}

	private void connectArrayOutPort(OutPortDef outPortDef) {
		String inPortName = outPortDef.getName();
		int index = outPortDef.getIndex();

		if (connectedArrayOutDefs == null) {
			connectedArrayOutDefs = new HashMap<String, Map<Integer, OutPortDef>>();
		}

		Map<Integer, OutPortDef> connectedArrayItemOuts = connectedArrayOutDefs
				.get(inPortName);

		if (connectedArrayItemOuts == null) {
			connectedArrayItemOuts = new HashMap<Integer, OutPortDef>();
			connectedArrayOutDefs.put(inPortName, connectedArrayItemOuts);
		}

		connectedArrayItemOuts.put(index, outPortDef);
		freeArrayOutTypes.remove(inPortName);
	}
}
