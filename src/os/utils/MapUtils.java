package os.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapUtils {
	
	public static String checksum(Object value){
		return MD5.hash(uri(value));
	}
	
	public static String table(Object value){
		String result = "";
		Map<String,Object> map = flat(value);
		for(Map.Entry<String, Object>entry:map.entrySet()){
			result+="\n"+entry.getKey()+" "+entry.getValue();
		}
		return result.substring(1);
	}
	
	public static String uri(Object value){
		String result = "";
		Map<String,Object> map = flat(value);
		for(Map.Entry<String, Object>entry:map.entrySet()){
			result+="&"+entry.getKey()+"="+entry.getValue();
		}
		return result.substring(1);
	}
	
	public static Map<String,Object> flat(Object value){
		Map<String, Object> result = new TreeMap<String, Object>();
		result.putAll(flat(value,null));
		return result;
	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
	public static Map<String,Object> flat(Object value, String key){
		Map<String,Object> result = new HashMap<String, Object>();
		if(value instanceof List){
			List list = (List) value;
			for(int i=0;i<list.size();i++){
				result.putAll(flat(list.get(i),(key==null)?i+"":key+"."+i));
			}
		}else
		if(value instanceof Map){
			Map<String,Object> map = (Map<String,Object>) value;
			for(Map.Entry<String, Object>entry:map.entrySet()){
				result.putAll(flat(entry.getValue(),(key==null)?entry.getKey():key+"."+entry.getKey()));
			}
		}else{
			result.put((key==null)?"":key,value);
		}
		return result;
	}
}
