package os.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BiMap<KeyType, ValueType> {
	
    private final Map<KeyType, ValueType> keyToValueMap;
    private final Map<ValueType, KeyType> valueToKeyMap;
    
    public BiMap(){
    	keyToValueMap = new ConcurrentHashMap<KeyType, ValueType>();
    	valueToKeyMap = new ConcurrentHashMap<ValueType, KeyType>();
    }
    
    synchronized public void put(KeyType key, ValueType value){
    	keyToValueMap.put(key, value);
        valueToKeyMap.put(value, key);	
    }

    synchronized public ValueType removeByKey(KeyType key){
        ValueType removedValue = keyToValueMap.remove(key);
        valueToKeyMap.remove(removedValue);
        return removedValue;
    }

    synchronized public KeyType removeByValue(ValueType value){
        KeyType removedKey = valueToKeyMap.remove(value);
        keyToValueMap.remove(removedKey);
        return removedKey;
    }

    public boolean containsKey(KeyType key){
        return keyToValueMap.containsKey(key);
    }

    public boolean containsValue(ValueType value){
        return keyToValueMap.containsValue(value);
    }

    public KeyType getKey(ValueType value){
        return valueToKeyMap.get(value);
    }

    public ValueType getValue(KeyType key){
        return keyToValueMap.get(key);
    }

	public Set<Map.Entry<KeyType, ValueType>> keySet() {
		return keyToValueMap.entrySet();
	}
	
	public Set<Map.Entry<KeyType, ValueType>> valueSet() {
		return keyToValueMap.entrySet();
	}
    
}
