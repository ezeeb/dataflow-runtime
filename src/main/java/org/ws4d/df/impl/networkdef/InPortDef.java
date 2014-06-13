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

import org.ws4d.df.api.comp.type.InPortType;
import org.ws4d.df.impl.NetworkBuilderImpl;

public class InPortDef {

    // -------------------------------------------------------------------------
	// instance members
    // -------------------------------------------------------------------------

    private final boolean isStatic;
    private final Object initPacket;
    private final InPortType portType;
    private final int index;
    private Integer capacity = NetworkBuilderImpl.DEFAULT_CAPACITY;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------

    public InPortDef(InPortType portType) {
        this(portType, -1, null, false);
    }

    public InPortDef(InPortType portType, int index) {
        this(portType, index, null, false);
    }

    public InPortDef(InPortType portType, int index, Object initPacket, boolean isStatic) {
        this.initPacket = initPacket;
        this.isStatic = isStatic;
        this.portType = portType;
        this.index = index;
    }

    // -------------------------------------------------------------------------
    // accessors
    // -------------------------------------------------------------------------

    public String getName() {
        return portType.getName();
    }

    public int getIndex() {
        return index;
    }

    public Object getInitPacket() {
        return initPacket;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public InPortType getPortType() {
        return portType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------

    public boolean isInitializer() {
        return initPacket != null;
    }
}
