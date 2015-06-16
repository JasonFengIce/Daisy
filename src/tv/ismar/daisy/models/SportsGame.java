package tv.ismar.daisy.models;

import java.io.Serializable;

public class SportsGame implements Serializable {

	private static final long serialVersionUID = 1L;
	public String start_time;
	public String expiry_date;
	public String name;
	public String poster_url;

	public int getGameType() {

		return 0;
	}
}
