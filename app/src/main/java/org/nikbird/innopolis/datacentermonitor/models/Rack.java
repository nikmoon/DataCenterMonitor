package org.nikbird.innopolis.datacentermonitor.models;

import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServer;

import java.util.Iterator;


/**
 * Created by nikbird on 15.07.17.
 */

public class Rack implements IRack {

    public static final int CAPACITY_DEFAULT = 5;
    private static int sId = 1;
    private static final int newId() {
        return sId++;
    }

    private final int mId;
    private final IServer[] mServers;
    private int mCount;
    private RackPosition mPosition;

    public Rack(final int capacity, RackPosition position) {
        if (capacity < 1)
            throw new IllegalArgumentException();
        mServers = new IServer[capacity];
        mCount = 0;
        mPosition = position;
        mId = newId();
    }

    public Rack(final int capacity) {
        this(capacity, null);
    }
    public Rack() { this(CAPACITY_DEFAULT, null); }

    @Override public boolean isInRoom() { return mPosition != null; }
    @Override public boolean hasAvailablePlace() { return availablePlace() != 0; }
    @Override public RackPosition position() { return mPosition; }
    @Override public int capacity() {
        return mServers.length;
    }
    @Override public int countServers() {
        return mCount;
    }
    @Override public int availablePlace() { return capacity() - mCount; }

    @Override public int hashCode() { return mId; }
    @Override public boolean equals(Object obj) { return this == obj; }

    @Override public IRack setPosition(RackPosition position) {
        mPosition = position;
        return this;
    }

    @Override public IRack createCopy() {
        IRack rackCopy = new Rack(capacity());
        for(IServer server: mServers)
            if (server != null) {
                IServer serverCopy = server.createCopy();
                rackCopy.insertServer(serverCopy, server.position().index());
            }
        return rackCopy;
    }

    @Override public IServer getServer(int serverIndex) {
        return getServerUnchecked(serverIndex);
    }

    @Override public boolean insertServer(IServer server, int serverIndex) {
        if (server != null && server.position() == null && getServerUnchecked(serverIndex) == null) {
            insertServerUnchecked(server, serverIndex);
            return true;
        }
        return false;
    }

    @Override public IServer.ServerPosition addServer(IServer server) {
        if (server != null && server.position() == null)
            for(int i = 0, count = capacity(); i < count; i++)
                if (getServerUnchecked(i) == null) {
                    insertServerUnchecked(server, i);
                    return server.position();
                }
        return null;
    }

    @Override public boolean removeServer(IServer server) {
        if (server != null) {
            IServer.ServerPosition position = server.position();
            if (position != null && position.rack() == this) {
                removeServerUnchecked(server);
                return true;
            }
        }
        return false;
    }

    @Override public IServer[] getServers() {
        IServer[] servers = new IServer[mCount];
        int i = 0;
        for(IServer server: mServers)
            if(server != null)
                servers[i++] = server;
        return servers;
    }

    private IServer getServerUnchecked(int serverIndex) { return mServers[serverIndex]; }

    private void insertServerUnchecked(IServer server, int serverIndex) {
        mServers[serverIndex] = server;
        server.setPosition(new IServer.ServerPosition(this, serverIndex));
        mCount++;
    }

    private IServer removeServerUnchecked(int serverIndex) {
        return removeServerUnchecked(mServers[serverIndex]);
    }

    private IServer removeServerUnchecked(IServer server) {
        mServers[server.position().index()] = null;
        server.setPosition(null);
        mCount--;
        return server;
    }

    private int mIterIndex;

    @Override
    public Iterator<IServer> iterator() {
        mIterIndex = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return mIterIndex < capacity();
    }

    @Override
    public IServer next() {
        return getServerUnchecked(mIterIndex++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
