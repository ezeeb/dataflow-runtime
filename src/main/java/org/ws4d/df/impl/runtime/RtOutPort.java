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

import org.ws4d.df.api.comp.OutPort;
import org.ws4d.df.api.comp.PortTracer;

public class RtOutPort extends AbstractPort implements OutPort {

    // -------------------------------------------------------------------------
	// instance members
    // -------------------------------------------------------------------------
	
    private final RtInPort rtInPort;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------
    
    public RtOutPort(String name, RtComponent component, int index,
            RtInPort rtInPort) {
        super(name, component, index);
        this.rtInPort = rtInPort;
    }

    // -------------------------------------------------------------------------
    // accessors
    // -------------------------------------------------------------------------
    
    public RtInPort getRtInPort() {
        return rtInPort;
    }

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------
    
    public int getCapacity() {
        return rtInPort.getCapacity();
    }

    public int getPacketCount() {
        return rtInPort.getPacketCount();
    }

    // -------------------------------------------------------------------------
    // OutPort interface
    // -------------------------------------------------------------------------
    
    public void send(Object packet) {
        PortTracer tracer = component.getRtNetwork().getPortTracer();
        if (tracer != null) {
            tracer.port_send(this, packet);
        }

        rtInPort.append(packet);
    }
    
    public boolean sendWouldBlock() {
    	return rtInPort.getPacketCount() == rtInPort.getCapacity();
    }

    public void close() {
        if (!isClosed()) {
            super.close();
            rtInPort.outPortClosed();
        }
    }
}
