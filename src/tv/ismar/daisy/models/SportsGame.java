package tv.ismar.daisy.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SportsGame implements Serializable {

	private static final long serialVersionUID = 1L;
	public String start_time;
	public String expiry_date;
	public String name;
	public String poster_url;

	public int getGameType() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		try {
//			Date starttime = sdf.parse(start_time);
//			Date expirytime = sdf.parse(expiry_date);
//			if (System.currentTimeMillis() < starttime.getTime())
//				return 5;
//			if (System.currentTimeMillis() > starttime.getTime()
//					&& System.currentTimeMillis() < expirytime.getTime())
//				return 4;
//			if (System.currentTimeMillis() > expirytime.getTime())
//				return 6;
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		return 0;
	}
}
