package tv.ismar.daisy.models;

import java.io.Serializable;

public class Expense implements Serializable {
 
	private static final long serialVersionUID = 8475391000819295987L;
	public float price;
	public float subprice;
	public int duration;
	public int cpid;
	public String cpname;
	public String cptitle;
	public int paytype;
	public String cplogo;
	public boolean sale_subitem;
	public int jump_to;
}
