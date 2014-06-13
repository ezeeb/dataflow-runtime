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

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.ws4d.df.api.comp.ComponentState;
import org.ws4d.df.api.comp.InPort;
import org.ws4d.df.api.comp.PortTracer;

public class RtInPort extends AbstractPort implements InPort, RuntimeInPort {

    // -------------------------------------------------------------------------
	// instance members
    // -------------------------------------------------------------------------
	
    private final int capacity;

    private RtOutPort rtOutPort = null;
    private boolean connectedOutPortIsClosed = false;

    private LinkedList<Object> queue;
    private ReentrantLock lock;
    private Condition notEmpty;
    private Condition notFull;

    // -------------------------------------------------------------------------
    // constructors
    // -------------------------------------------------------------------------
    
    public RtInPort(String inPortName, RtComponent receiver, int index, int capacity) {
        super(inPortName, receiver, index);
        this.capacity = capacity;
        this.queue = new LinkedList<Object>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    // -------------------------------------------------------------------------
    // accessors
    // -------------------------------------------------------------------------
    
    public RtComponent getReceiver() {
        return component;
    }

    public RtOutPort getRtOutPort() {
        return rtOutPort;
    }

    // -------------------------------------------------------------------------
    // methods
    // -------------------------------------------------------------------------
    
    @Override
    public void reset() {
        super.reset();
        connectedOutPortIsClosed = false;
        queue.clear();
    }

    public void setRtOutPort(RtOutPort rtOutPort) {
        this.rtOutPort = rtOutPort;
    }

    public void append(Object packet) {
        try {
            lock.lockInterruptibly();
            while (queue.size() == capacity) {
                notFull.await();
            }
        } catch (InterruptedException e) {
            logException(Level.SEVERE, "???", e);
            return;
        }
        if (isClosed()) {
            return;
        }
        queue.offer(packet);
        if (getReceiver().getComponentState() == ComponentState.INITIALIZED
                || getReceiver().getComponentState() == ComponentState.RESETTED) {
            getReceiver().runComponent();
        } else if (getReceiver().getComponentState() == ComponentState.INACTIVE) {
            synchronized (getReceiver()) {
                getReceiver().notify();
            }
        } else {
            notEmpty.signal();
        }
        lock.unlock();
    }

    public void outPortClosed() {
        try {
            lock.lockInterruptibly();
            if (!isClosed()) {
                connectedOutPortIsClosed = true;
                // Notify receiver if it currently does a receive
                notEmpty.signal();
                // ... or if it waits for data
                synchronized (getReceiver()) {
                    getReceiver().notify();
                }
                // notFull.signalAll();
            }
            lock.unlock();

        } catch (InterruptedException e) {
            logException(Level.SEVERE, "???", e);
        }
    }

    // -------------------------------------------------------------------------
    // InPort interface
    // -------------------------------------------------------------------------
    
    public int getCapacity() {
        return capacity;
    }

    public int getPacketCount() {
        return queue.size();
    }

    public Object receive() {
        try {
            lock.lockInterruptibly();
            if (connectedOutPortIsClosed && queue.isEmpty()) {
                lock.unlock();
                close();
                return null;
            }

            if (queue.isEmpty()) {
                notEmpty.await();
            }
        } catch (InterruptedException e) {
            logException(Level.SEVERE, "???", e);
            return null;
        }

        Object packet = null;
        if (!queue.isEmpty()) {
            packet = queue.poll();
            notFull.signal(); // todo oder signalAll()?
        }

        if (connectedOutPortIsClosed && queue.isEmpty()) {
            close();
        }

        lock.unlock();

        PortTracer tracer = component.getRtNetwork().getPortTracer();
        if (tracer != null) {
            tracer.port_recv(this, packet);
        }

        return packet;
    }
    
    public boolean receiveWouldBlock() {
    	return queue.isEmpty();
    }

    public void close() {
        if (!isClosed()) {
            try {
                lock.lockInterruptibly();
                super.close();
                queue.clear();
                notFull.signalAll();
                notEmpty.signal(); // todo eigentlich nicht notwendig, da nur
                                    // die komponente close() aufrufen darf
                lock.unlock();
            } catch (InterruptedException e) {
                logException(Level.SEVERE, "???", e);
            }
            rtOutPort.close();
        }
    }
}
