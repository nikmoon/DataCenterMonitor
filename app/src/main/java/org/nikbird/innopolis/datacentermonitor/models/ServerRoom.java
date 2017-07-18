package org.nikbird.innopolis.datacentermonitor.models;

import org.nikbird.innopolis.datacentermonitor.interfaces.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServerRoom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nikbird on 15.07.17.
 */

public class ServerRoom implements IServerRoom {

    public static final int CAPACITY_DEFAULT = 10;
    private static int sId = 1;
    private static final int newId() {
        return sId++;
    }

    private final int mId;
    private final IRack[] mRacks;
    private int mCount;


    public ServerRoom(final int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException();
        mRacks = new IRack[capacity];
        mCount = 0;
        mId = newId();
    }

    public ServerRoom() { this(CAPACITY_DEFAULT); }

    @Override public int capacity() { return mRacks.length; }
    @Override public int countRacks() { return mCount; }
    @Override public int availablePlace() { return capacity() - mCount; }

    @Override public int availablePlaceForServers() {
        int available = 0;
        for(IRack rack: mRacks)
            if (rack != null)
                available += rack.availablePlace();
        return available;
    }

    @Override public int countServers() {
        int count = 0;
        for(IRack rack: mRacks)
            if (rack != null)
                count += rack.countServers();
        return count;
    }

    @Override public IRack getRack(int rackIndex) { return getRackUnchecked(rackIndex); }

    @Override public IRack[] getRacks() {
        return Arrays.copyOf(mRacks, mRacks.length);
    }

    @Override
    public IServer[] getServers() {
        List<IServer> servers = new LinkedList<>();
        for (IRack rack : getRacks()) {
            for (IServer server : rack.getServers()) {
                servers.add(server);
            }
        }
        return servers.toArray(new IServer[servers.size()]);
    }

    @Override public boolean removeRack(IRack rack) {
        if (rack != null) {
            IRack.RackPosition position = rack.position();
            if (position != null && position.serverRoom() == this) {
                removeRackUnchecked(rack);
                return true;
            }
        }
        return false;
    }

    @Override public boolean insertRack(IRack rack, int rackIndex) {
        if (rack != null && rack.position() == null && getRackUnchecked(rackIndex) == null) {
            insertRackUnchecked(rack, rackIndex);
            return true;
        }
        return false;
    }

    @Override public IRack.RackPosition addRack(IRack rack) {
        if (rack != null && rack.position() == null)
            for(int i = 0, count = capacity(); i < count; i++)
                if (getRackUnchecked(i) == null) {
                    insertRackUnchecked(rack, i);
                    return rack.position();
                }
        return null;
    }

    private IRack getRackUnchecked(int rackIndex) {
        return mRacks[rackIndex];
    }

    private void insertRackUnchecked(IRack rack, int rackIndex) {
        mRacks[rackIndex] = rack;
        rack.setPosition(new IRack.RackPosition(this, rackIndex));
        mCount++;
    }

    private IRack removeRackUnchecked(IRack rack) {
        mRacks[rack.position().rackIndex()] = null;
        rack.setPosition(null);
        mCount--;
        return rack;
    }

    private IRack removeRackUnchecked(int rackIndex) {
        return removeRackUnchecked(mRacks[rackIndex]);
    }

    private int mIterIndex;

    @Override
    public Iterator<IRack> iterator() {
        mIterIndex = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return mIterIndex < capacity();
    }

    @Override
    public IRack next() {
        return getRackUnchecked(mIterIndex++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
