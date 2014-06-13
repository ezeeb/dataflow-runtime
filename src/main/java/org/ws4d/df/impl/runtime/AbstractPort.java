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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.df.api.comp.ComponentContext;
import org.ws4d.df.api.comp.Port;

public abstract class AbstractPort implements Port {

    // -------------------------------------------------------------------------
    // instance members
    // -------------------------------------------------------------------------
	
    private final String name;
    private final int index;
    protected final RtComponent component;
    private boolean isClosed = false;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------
    
    public AbstractPort(String name, RtComponent component, int index) {
        this.name = name;
        this.component = component;
        this.index = index;
    }

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------
    
    public void reset() {
        isClosed = false;
    }

    // -------------------------------------------------------------------------
    // Port interface
    // -------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public int getIndex() {
        return index;
    }
    
    public ComponentContext getComponent() {
    	return component;
    }

    public void close() {
        isClosed = true;
        if (component.getRtNetwork().getPortTracer() != null) {
        	component.getRtNetwork().getPortTracer().port_close(this);
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    // -------------------------------------------------------------------------
    // private
    // -------------------------------------------------------------------------
    
    protected void logException(Level level, String msg, Exception e) {
    	Logger logger = component.getLogger();
    	if (logger != null) {
    		logger.log(level, msg, e);
    	} else {
    		System.out.println(msg);
    		e.printStackTrace();
    	}
    }
}
