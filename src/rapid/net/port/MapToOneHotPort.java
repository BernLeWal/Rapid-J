package rapid.net.port;

import java.util.HashMap;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rapid.net.Gate;

public class MapToOneHotPort<K> extends OneHotPort {

    private static final Logger LOG = LogManager.getLogger(MapToOneHotPort.class);

    private final HashMap<K, Integer> mapKey2Index;
    private final HashMap<Integer, K> mapIndex2Key;

    public MapToOneHotPort(String name) {
        super(name);

        mapKey2Index = new HashMap<>();
        mapIndex2Key = new HashMap<>();
    }
    
    public MapToOneHotPort(String name, MapToOneHotPort prototype) {
        super(name);
        
        this.mapKey2Index = prototype.mapKey2Index;
        this.mapIndex2Key = prototype.mapIndex2Key;
    }

    public void setItem(K key, Queue<Gate> bfp, int cycle) {
        int index;
        if (key == null) {
            super.setValue(NO_VALUE, bfp, cycle);  // unset the old item
        } else {
            if (!mapKey2Index.containsKey(key)) {
                index = createItem(key, cycle);
            } else {
                index = mapKey2Index.get(key);
            }
            super.setValue(index, bfp, cycle);
        }
    }

    public int createItem(K key, int cycle) {
        int index = gates.size();
        gates.add(Gate.createOrGate(this, name + "." + key, cycle));
        mapKey2Index.put(key, index);
        mapIndex2Key.put(index, key);
        return index;
    }

    public K getItem(int cycle) {
        int index = super.getValue(cycle);
        if (mapIndex2Key.containsKey(index)) {
            return mapIndex2Key.get(index);
        } else {
            return null;
        }
    }

    public K estimateItem() {
        int index = super.estimateValue();
        if (mapIndex2Key.containsKey(index)) {
            return mapIndex2Key.get(index);
        } else {
            return null;
        }
    }
}
