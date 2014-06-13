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

import org.ws4d.df.api.comp.type.OutPortType;

public class OutPortDef {

    // -------------------------------------------------------------------------
    // instance members
    // -------------------------------------------------------------------------

    private final String receiverName;
    private final OutPortType portType;
    private final InPortDef inPort;
    private final int index;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------

    public OutPortDef(OutPortType portType, int index, String receiverName, InPortDef inPort) {
        this.receiverName = receiverName;
        this.portType = portType;
        this.index = index;
        this.inPort = inPort;
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

    public String getReceiverName() {
        return receiverName;
    }

    public String getInPortName() {
        return inPort.getName();
    }

    public int getInIndex() {
        return inPort.getIndex();
    }

    public OutPortType getPortType() {
        return portType;
    }
}
