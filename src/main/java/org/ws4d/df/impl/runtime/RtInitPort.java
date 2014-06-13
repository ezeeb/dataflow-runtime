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
import org.ws4d.df.api.comp.PortTracer;

public class RtInitPort extends AbstractPort implements InPort, RuntimeInPort {

    // -------------------------------------------------------------------------
	// instance members
    // -------------------------------------------------------------------------
	
    private final Object packet;
    private final boolean isStatic;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------
    
    public RtInitPort(String name, RtComponent component, int index, Object packet, boolean isStatic)
            throws Exception {
        super(name, component, index);

        if (packet == null) {
            throw new Exception("packet is null");
        }

        this.packet = packet;
        this.isStatic = isStatic;
    }

    // -------------------------------------------------------------------------
    // InPort interface
    // -------------------------------------------------------------------------
    
    public void close() {
        super.close();
    }

    public Object receive() {
        if (isClosed()) {
            return null;
        }
        if (!isStatic) {
            close();
        }

        PortTracer tracer = component.getRtNetwork().getPortTracer();
        if (tracer != null) {
            tracer.port_recv(this, packet);
        }

        return packet;
    }
    
    public boolean receiveWouldBlock() {
    	return false;
    }

    public int getPacketCount() {
        return isClosed() ? 0 : 1;
    }

    public int getCapacity() {
        return 1;
    }

    public void append(Object packet) {
    }
}
