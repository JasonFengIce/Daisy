package tv.ismar.daisy.models;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Attribute implements Serializable {
	
	private static final long serialVersionUID = 3409800758940535030L;
	@SuppressWarnings("rawtypes")
	public LinkedHashMap map;
	
	public static class Info {
		public Integer id;
		public String name;
	}
}
