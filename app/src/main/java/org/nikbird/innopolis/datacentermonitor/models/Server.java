package org.nikbird.innopolis.datacentermonitor.models;

import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServer;

/**
 * Created by nikbird on 14.07.17.
 */

public class Server implements IServer {

    public static final State STATE_DEFAULT = State.GOOD;
    private static int sId = 1;
    private static final int newId() {
        return sId++;
    }

    private final int mId;
    private State mState;
    private ServerPosition mPosition;

    public Server(State state) {
        mState = state;
        mPosition = null;
        mId = newId();
    }

    public Server(IServer server) { this(server.state()); }
    public Server() { this(STATE_DEFAULT); }

    @Override public boolean isInRack() { return mPosition != null; }

    @Override public State state() { return mState; }
    @Override public ServerPosition position() { return mPosition; }
    @Override public int hashCode() { return mId; }
    @Override public boolean equals(Object obj) { return this == obj; }

    @Override public IServer setState(State state) { mState = state; return this; }
    @Override  public IServer setPosition(ServerPosition position) { mPosition = position; return this; }
    @Override public IServer createCopy() { return new Server(mState); }

}
