package tv.ismar.daisy.models;

import java.io.Serializable;

public class Channel implements Serializable {
  
	private static final long serialVersionUID = -413039642927192094L;
	
	public String channel;
    public String name;
    public int template;
    public String url;
    public String icon_focus_url;
    public String icon_url;
    public boolean chargeable;
}
