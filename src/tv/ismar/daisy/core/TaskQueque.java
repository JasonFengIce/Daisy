package tv.ismar.daisy.core;

public class TaskQueque {
	private static final String TAG = "TaskQueque";
	
	private volatile static TaskQueque instance = null;
	
	protected TaskQueque() {
		
	}
	
	public static TaskQueque getInstance() {
		if(instance==null){
			synchronized (TaskQueque.class) {
				if(instance==null) {
					instance = new TaskQueque();
				}
			}
		}
		return instance;
	}
}
