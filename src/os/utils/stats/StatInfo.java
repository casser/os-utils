package os.utils.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatInfo {
	
	private final static Map<String, StatInfo> list = new ConcurrentHashMap<String, StatInfo>();
	public static StatInfo get(String name){
		if(!list.containsKey(name)){
			list.put(name,new StatInfo(name));
		}
		return list.get(name);
	}
	
	public static void clear() {
		list.clear();
	}
	
	public static Map<String, StatInfo> list() {
		return list;
	}
	
	private String name;
	
	private Long	time;
	private Long	count;
	private Double	hps;
	private Long 	hits;
	private Long 	fails;
	private Double 	sum;
	private Double 	min;
	private Double 	max;
	
	public StatInfo(String name){
		this.name 	= name;
		this.min 	= Double.MAX_VALUE;
		this.max 	= Double.MIN_VALUE;
		this.sum 	= 0.0;
		this.hits 	= 0L;
		this.fails 	= 0L;
		this.count 	= 0L;
		this.hps 	= 0.0;
		this.time 	= Long.valueOf(System.currentTimeMillis()/1000);
	}
	
	public void hit(Double value, Boolean isFail){
		if(isFail){
			fails		++;
		}
		
		hits	++;
		sum		+= value;
		max 	=  (max==Double.MIN_VALUE)?value:Math.max(max, value);
		min 	=  (min==Double.MAX_VALUE)?value:Math.min(min, value);
		
		Long now	= Long.valueOf(System.currentTimeMillis()/1000);
		Long pTime  = now-time;
		Long pCount = hits-count;
		if(pTime>1){
			hps 	= (double)pCount/(double)pTime;
			time 	= now;
			count 	= hits;
		}
	}
			
	public String getName(){
		return name;
	}
	
	public Long getHps(){
		return Math.round(hps);
	}
	
	public Long getHits(){
		return hits;
	}
	
	public Long getFails(){
		return fails;
	}
	
	public Double getSum(){
		return sum;
	}
			
	public Double getMax(){
		return max;
	}
	
	public Double getMin(){
		return min;
	}

	public Object getAvg() {
		return sum/hits;
	}

}