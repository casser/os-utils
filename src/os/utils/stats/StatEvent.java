package os.utils.stats;


public class StatEvent{
	
	private String 	name;
	private Long 	time;
	private Boolean enabled;
	
	public StatEvent(String name) {
		this.time 		= System.currentTimeMillis();
		this.enabled 	= true;
		name(name);
	}
	
	public StatEvent(Boolean enabled) {
		this.time 		= System.currentTimeMillis();
		this.enabled 	= enabled;
	}

	private StatInfo info(){
		return StatInfo.get(name);
	}
	
	public StatEvent name(String name){
		int dp = name.indexOf('$');
		String prefix;
		String event;
		if(dp>0){
			prefix 	=  name.substring(0, dp);
			event	=  name.substring(dp+1);
			if(!prefix.equals("T") && !prefix.equals("B") && !prefix.equals("I")){
				prefix = "I";
			}
		}else{
			prefix 	=  "T";
			event	=  name;
		}
		this.name=prefix+"$"+event;
		return this;
	}
	
	public Double getDuration(){
		return ((double)System.currentTimeMillis()-this.time)/1000;
	}
	public void finish(){
		finish(false);
	}
	
	public void finish(Boolean isFail){
		finish(getDuration(),isFail);
	}
	
	public void finish(Long value){
		finish((double)value,false);
	}
	
	public void finish(Integer value){
		finish((double)value,false);
	}
	
	public void finish(Double value){
		finish(value,false);
	}
	
	public void finish(Integer value, Boolean isFail){
		finish((double)value,false);
	}
	
	public void finish(Double value, Boolean isFail){
		if(this.enabled){
			info().hit(value,isFail);
		}
	}
	
	public void success(){
		name(this.name);
		finish(false);
	}
	
	public void fail(){
		name(this.name);
		finish(true);
	}
}
