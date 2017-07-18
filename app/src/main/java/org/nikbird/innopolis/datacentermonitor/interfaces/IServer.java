package org.nikbird.innopolis.datacentermonitor.interfaces;

/**
 * Created by nikbird on 14.07.17.
 */

public interface IServer {

    enum State { GOOD, FAIL }

    class ServerPosition {
        private final IRack mRack;
        private final int mServerIndex;

        public ServerPosition(final IRack rack, final int serverIndex) {
            if (rack == null)
                throw new IllegalArgumentException();
            if (serverIndex < 0 || serverIndex >= rack.capacity())
                throw new ArrayIndexOutOfBoundsException();
            mRack = rack;
            mServerIndex = serverIndex;
        }

        public IRack rack() { return mRack; }
        public int index() { return mServerIndex; }
    }

    State state();
    ServerPosition position();
    boolean isInRack();
    IServer setState(State state);
    IServer setPosition(ServerPosition position);
    IServer createCopy();
}
