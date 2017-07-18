package org.nikbird.innopolis.datacentermonitor.interfaces;


import java.util.Iterator;

/**
 * Created by nikbird on 15.07.17.
 */

public interface IServerRoom extends Iterator<IRack>, Iterable<IRack> {
    int capacity();
    int countRacks();
    int availablePlace();
    int availablePlaceForServers();
    int countServers();
    IRack getRack(int rackIndex);
    IRack[] getRacks();
    IServer[] getServers();
    boolean removeRack(IRack rack);
    boolean insertRack(IRack rack, int rackIndex);
    IRack.RackPosition addRack(IRack rack);
}
