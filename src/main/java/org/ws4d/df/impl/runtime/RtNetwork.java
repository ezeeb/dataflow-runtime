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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.df.api.comp.ComponentState;
import org.ws4d.df.api.comp.ComponentTracer;
import org.ws4d.df.api.comp.InPort;
import org.ws4d.df.api.comp.OutPort;
import org.ws4d.df.api.comp.PortTracer;
import org.ws4d.df.api.network.Network;
import org.ws4d.df.api.network.NetworkContext;
import org.ws4d.df.api.network.NetworkState;
import org.ws4d.df.api.network.NetworkTracer;

public class RtNetwork implements Network, NetworkContext {

	// -------------------------------------------------------------------------
	// instance members
	// -------------------------------------------------------------------------

	private final Logger logger;
	private final String networkId;

	private PortTracer portTracer = null;
	private ComponentTracer componentTracer = null;
	private NetworkTracer networkTracer = null;

	private Map<String, RtComponent> rtComponents = null;
	private List<RtComponent> nonFinishedRtComponents = null;
	private List<RtComponent> closedRtComponents = null;

	private final Map<String, Object> runtimeProperties = new HashMap<String, Object>();
	private final Map<String, Object> networkProperties = new HashMap<String, Object>();

	private NetworkState state = NetworkState.INITIALIZED;
	private boolean isOneTime;

	// -------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------

	public RtNetwork(String networkId) {
		this.networkId = networkId;
		logger = Logger.getLogger(networkId);
	}

	// -------------------------------------------------------------------------
	// accessors
	// -------------------------------------------------------------------------

	public void setRtComponents(Map<String, RtComponent> rtComponents) {
		this.rtComponents = rtComponents;
	}

	public boolean isOneTimeMode() {
		return isOneTime;
	}

	public Map<String, Object> getRuntimeProperties() {
		return runtimeProperties;
	}

	public Map<String, Object> getNetworkProperties() {
		return networkProperties;
	}

	public Logger getLogger() {
		return logger;
	}

	public PortTracer getPortTracer() {
		return portTracer;
	}

	public ComponentTracer getComponentTracer() {
		return componentTracer;
	}

	private void setState(NetworkState state) {
		this.state = state;

		if (networkTracer != null) {
			networkTracer.changeState(this, this.state);
		}
	}

	// -------------------------------------------------------------------------
	// methods
	// -------------------------------------------------------------------------

	public synchronized void componentClosed(RtComponent rtComponent) {
		closedRtComponents.add(rtComponent);
		notify();
	}

	public void runNetwork() throws Exception {
		nonFinishedRtComponents = new ArrayList<RtComponent>(
				rtComponents.values());
		closedRtComponents = new ArrayList<RtComponent>();

		setState(NetworkState.STARTED);

		synchronized (this) {
			for (RtComponent rtComponent : rtComponents.values()) {
				boolean startComponent = true;
				if (!rtComponent.isActiveComponent()) {
					for (InPort inPort : rtComponent.getAllInPorts()) {
						if (inPort instanceof RtInPort) {
							startComponent = false;
							break;
						}
					}
				}
				if (startComponent) {
					rtComponent.runComponent();
				}
			}
			try {
				boolean checkFurther = true;
				while (checkFurther) {
					wait();
					checkFurther = checkNetwork();
				}
			} catch (Exception e) {
				logException(Level.SEVERE, "???", e);
			}
		}
	}

	public void reset() {
		for (RtComponent rtComponent : rtComponents.values()) {
			rtComponent.reset();
		}
	}

	// -------------------------------------------------------------------------
	// Network interface
	// -------------------------------------------------------------------------

	public void startAndTerminate() throws Exception {
		if (this.state == NetworkState.INITIALIZED) {
			this.isOneTime = true;
			runNetwork();
			setState(NetworkState.TERMINATED);
		} else {
			throw new IllegalStateException(
					"A terminated network cannot be started again.");
		}
	}

	public void start() throws Exception {
		if (this.state == NetworkState.INITIALIZED) {
			this.isOneTime = false;
		} else {
			if (this.isOneTimeMode()) {
				throw new IllegalStateException(
						"A terminated network cannot be started again.");
			} else {
				reset();
			}
		}
		runNetwork();
		setState(NetworkState.RESETTED);
	}

	public void terminate() {
		for (RtComponent rtComponent : rtComponents.values()) {
			rtComponent.closeAllOutPorts();
		}
		for (RtComponent rtComponent : rtComponents.values()) {
			rtComponent.terminate();
		}
		setState(NetworkState.TERMINATED);
	}

	public Object getRuntimeProperty(String name) {
		return runtimeProperties.get(name);
	}

	public void setRuntimeProperty(String name, Object property) {
		runtimeProperties.put(name, property);
	}

	public void setPortTracer(PortTracer portTracer) {
		this.portTracer = portTracer;
	}

	public void setComponentTracer(ComponentTracer componentTracer) {
		this.componentTracer = componentTracer;
	}

	public void setNetworkTracer(NetworkTracer networkTracer) {
		this.networkTracer = networkTracer;
	}

	// -------------------------------------------------------------------------
	// NetworkContext interface
	// -------------------------------------------------------------------------

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkProperty(String key, Object value) {
		networkProperties.put(key, value);
	}

	public Object getNetworkProperty(String key) {
		return networkProperties.get(key);
	}

    // -------------------------------------------------------------------------
	// private
    // -------------------------------------------------------------------------
	
	private void log(Level level, String msg) {
		if (logger != null) {
			logger.log(level, msg);
		} else {
			System.out.println(msg);
		}
	}
	
	private void logException(Level level, String msg, Exception e) {
		if (logger != null) {
			logger.log(level, msg, e);
		} else {
			System.out.println(msg);
			e.printStackTrace();
		}
	}

	private boolean checkNetwork() {
		while (closedRtComponents.size() > 0) {

			RtComponent rtComponent = closedRtComponents.remove(0);
			if (!nonFinishedRtComponents.contains(rtComponent)) {
				log(Level.WARNING, "Problem Component: " + rtComponent.getName());
				break;
			}

			rtComponent.finish();
			if (isOneTimeMode()) {
				rtComponent.terminate();
			}
			nonFinishedRtComponents.remove(rtComponent);

			for (OutPort outPort : rtComponent.getAllOutPorts()) {
				if (outPort instanceof RtOutPort) { // todo dirty
					RtComponent neighbour = ((RtOutPort) outPort).getRtInPort()
							.getReceiver();
					if (nonFinishedRtComponents.contains(neighbour)
							&& neighbour.getComponentState() != ComponentState.ACTIVE) {

						boolean closeComponent = true;
						for (InPort inPort : neighbour.getAllInPorts()) {
							if (inPort instanceof RtInPort) {
								RtInPort rtInPort = (RtInPort) inPort;
								if (!rtInPort.getRtOutPort().isClosed()
										|| (!rtInPort.isClosed() && rtInPort
												.getPacketCount() > 0)) {
									closeComponent = false;
									break;
								}
							}
						}
						if (closeComponent) {
							if (neighbour.getComponentState() == ComponentState.INITIALIZED
									|| neighbour.getComponentState() == ComponentState.RESETTED
									|| neighbour.getComponentState() == ComponentState.TERMINATED) {
								neighbour.closeAllPorts();
								componentClosed(neighbour);
							} else {
								logger.log(Level.WARNING, "DEAD LOCK ?!?");
								// state is INACTIVE
								// todo deadlock
							}
						}

					}
				}
			}
		}

		return nonFinishedRtComponents.size() != 0;
	}
}
