package tv.ismar.daisy.models;

public class PrivilegeItem {
  private String title;
  private String duration;
  private int item_pk;
  private String type;
  private String buydate;
  private String exceeddate;
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
public String getBuydate() {
	return buydate;
}
public void setBuydate(String buydate) {
	this.buydate = buydate;
}
public String getExceeddate() {
	return exceeddate;
}
public void setExceeddate(String exceeddate) {
	this.exceeddate = exceeddate;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
public String getDuration() {
	return duration;
}
public void setDuration(String duration) {
	this.duration = duration;
}
public int getItem_pk() {
	return item_pk;
}
public void setItem_pk(int item_pk) {
	this.item_pk = item_pk;
}
}
