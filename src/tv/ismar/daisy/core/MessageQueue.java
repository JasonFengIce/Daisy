package tv.ismar.daisy.core;

import java.util.ArrayList;

import android.util.Log;

public class MessageQueue {

	static ArrayList<String> messageQueueList;
	//private static MessageQueue queue = new MessageQueue();
    public static final String async="sync";
	static {
		messageQueueList = new ArrayList<String>();
	}

	public static synchronized void addQueue(String item) {
		Log.i("mnbvcxz", "item="+item);
		messageQueueList.add(item);
	}

	public static synchronized void remove() {
		messageQueueList.clear();
	}

//	public static MessageQueue getInstance() {
//		return queue;
//	}
	
	public static synchronized ArrayList<String> getQueueList(){
		return messageQueueList;
	}
}
