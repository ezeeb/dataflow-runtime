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

import org.ws4d.df.api.comp.InPort;
import org.ws4d.df.api.comp.OutPort;

public class RtNullPort extends AbstractPort implements InPort, OutPort,
		RuntimeInPort {

	// -------------------------------------------------------------------------
	// constructors
	// -------------------------------------------------------------------------

	public RtNullPort(String name, RtComponent component, int index) {
		super(name, component, index);
	}

	// -------------------------------------------------------------------------
	// InPort, OutPort interfaces
	// -------------------------------------------------------------------------

	public int getCapacity() {
		return 0;
	}

	public int getPacketCount() {
		return 0;
	}

	public Object receive() {
		return null;
	}

	public boolean receiveWouldBlock() {
		return false;
	}

	public void close() {
	}

	public void send(Object packet) {
	}

	public boolean sendWouldBlock() {
		return false;
	}

	public boolean isClosed() {
		return true;
	}

	public void append(Object packet) {
	}
}
