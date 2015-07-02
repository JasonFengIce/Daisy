package tv.ismar.daisy.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class OrderListItem {

	private String extra;
	private String title;
	private String expiry_date;
	private int total_fee;
	private String thumb_url;
	private String start_date;
	private String paysource;

	public String getPaysource() {
		return paysource;
	}

	public void setPaysource(String paysource) {
		this.paysource = paysource;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExpiry_date() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date date = df.parse(expiry_date);
			long time = date.getTime();
			long currenttime = System.currentTimeMillis();
			long diff = time - currenttime;
			long days = diff / (1000 * 60 * 60 * 24);
			return days + "";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return expiry_date;
	}

	public void setExpiry_date(String expiry_date) {
		this.expiry_date = expiry_date;
	}

	public int getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}

	public String getThumb_url() {
		return thumb_url;
	}

	public void setThumb_url(String thumb_url) {
		this.thumb_url = thumb_url;
	}

	public String getStart_date() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			java.util.Date date = formatter.parse(start_date);
			start_date = formatter.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

}
