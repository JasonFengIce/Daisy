package tv.ismar.daisy.models;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Attribute implements Serializable {
	
	private static final long serialVersionUID = 3409800758940535030L;
	@SuppressWarnings("rawtypes")
	public LinkedHashMap map;
	
<<<<<<< HEAD
	public static class Info {
=======
	public static class Info implements Serializable {
		private static final long serialVersionUID = 148464239713571723L;
		
>>>>>>> 15fbd302918113623af431d68ef3c106bd33b6fb
		public Integer id;
		public String name;
	}
}
