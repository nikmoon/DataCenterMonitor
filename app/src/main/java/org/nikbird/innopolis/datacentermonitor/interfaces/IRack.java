package org.nikbird.innopolis.datacentermonitor.interfaces;

import java.util.Iterator;

/**
 * Created by nikbird on 14.07.17.
 */

public interface IRack extends Iterator<IServer>, Iterable<IServer> {

    class RackPosition {
        private final IServerRoom mServerRoom;
        private final int mRackIndex;

        public RackPosition(final IServerRoom room, final int rackIndex) {
            if (room == null)
                throw new IllegalArgumentException();
            if (rackIndex < 0 || rackIndex >= room.capacity())
                throw new ArrayIndexOutOfBoundsException();
            mServerRoom = room;
            mRackIndex = rackIndex;
        }

        public IServerRoom serverRoom() { return mServerRoom; }
        public int rackIndex() { return mRackIndex; }
    }

    RackPosition position();
    int capacity();
    int countServers();
    int availablePlace();
    boolean isInRoom();
    boolean hasAvailablePlace();
    IRack setPosition(RackPosition position);
    IServer getServer(int serverIndex);
    IServer[] getServers();
    boolean removeServer(IServer server);
    boolean insertServer(IServer server, int serverIndex);
    IServer.ServerPosition addServer(IServer server);
    IRack createCopy();
}
