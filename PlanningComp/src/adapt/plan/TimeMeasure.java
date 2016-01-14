package adapt.plan;

public class TimeMeasure {

	    public long startTime;
	    public long endTime;

	    public long getDuration() {
	        return endTime - startTime;
	    }
	    // I  would add
	    public void start() {
	        startTime = System.currentTimeMillis();
	    }
	    public void stop() {
	         endTime = System.currentTimeMillis();
	     }
}
