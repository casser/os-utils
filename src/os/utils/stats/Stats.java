package os.utils.stats;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

import os.utils.StringUtils;



public class Stats {
	
	private static Boolean enabled = false;
	
	public static void enable(){
		enabled = true;
	}
	
	public static void disable(){
		enabled = false;
	}
	
	public static void reset(){
		StatInfo.clear();
	}
	
	public static Boolean isEnabled(){
		return enabled;
	}
	
	
	public static String getTrackingString(){
		Map<String,Object> data = getTrackingMap();
		
		int keyMaxLength = 0;
		for(String key:data.keySet()){
			keyMaxLength = Math.max(keyMaxLength, key.length());
		}
		
		String res = "STATS"+StringUtils.repeat(" ", keyMaxLength-5)+"       HPS      HITS     FAILS       SUM       MIN       MAX       AVG\n";
		res += StringUtils.repeat("-", keyMaxLength)+"----------------------------------------------------------------------\n";
		for(Map.Entry<String, Object> entry:data.entrySet()){
			res+=formatRow(entry.getKey(),entry.getValue(),keyMaxLength)+"\n";
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private static String formatRow(String key, Object value, int keyMaxLength){
		String res = "";
		Map<String, Object> data = (Map<String, Object>)value;
		res += key+StringUtils.repeat(" ", keyMaxLength-key.length());
		for(Map.Entry<String, Object> entry:data.entrySet()){
			String item = "";
			if(entry.getValue() instanceof Long){
				item = entry.getValue().toString();
				item = item.substring(0,Math.min(item.length(), 9));
			}
			if(entry.getValue() instanceof Double){
				item = new DecimalFormat("#.####").format(entry.getValue());
				item = item.substring(0,Math.min(item.length(), 9));;
			}
			item = StringUtils.repeat(" ", 9-item.length())+item;
			res+=" "+item;
		}
		return res;
	}
	
	public static Map<String,Object> getTrackingMap(){
		Map<String,Object> events 	= new TreeMap<String,Object>();
		for(StatInfo info:StatInfo.list().values()){
			Map<String, Object> infoMap	= new TreeMap<String, Object>();
			String key = info.getName()
				.replace("T$", "Times.")
				.replace("I$", "Items.")
				.replace("B$", "Data.");
			infoMap.put("hps", 		info.getHps());
			infoMap.put("hits", 	info.getHits());
			infoMap.put("fails",	info.getFails());
			infoMap.put("sum", 		info.getSum());
			infoMap.put("min",		info.getMin());
			infoMap.put("max", 		info.getMax());
			infoMap.put("avg", 		info.getAvg());
			events.put(key,infoMap);
		}
		return events;
	}
		
	public static StatEvent track(){
		return track("UNKNOWN");
	}
	
	public static void track(String name, Integer value, String units){
		track(name,value.doubleValue(),units);
	}
	
	public static void track(String name, Double value, String units){
		track(units+"."+name).finish(value);
	}
	
	public static StatEvent track(String name){
		if(isEnabled()){
			return new StatEvent(name);
		}
		return new StatEvent(false);
	}
}
