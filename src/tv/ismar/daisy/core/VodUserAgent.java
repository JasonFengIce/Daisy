package tv.ismar.daisy.core;

import java.net.NetworkInterface;

public class VodUserAgent {
	
	
	public static final String deviceType = "A11";
	public static final String deviceVersion = "2.0";
	
	/**
	 * getMACAddress == getSn
	 * 
	 * @return Sn
	 */
	public static  String getMACAddress(){
		String mac = "00112233445566";
			try{
				byte addr[];
				addr=NetworkInterface.getByName("eth0").getHardwareAddress();
				mac="";
				for(int i=0; i<6; i++){
					mac+=String.format("%02X",addr[i]);
				}
			}catch(Exception e){
				return mac;
			}

		return mac;
	}
	/**
	 * getUserAgent
	 * 
	 * @param sn
	 * @return UserAgent
	 */
	public static String getUserAgent(String sn) {
		String userAgent = deviceType + "/"+ deviceVersion + " " + getMACAddress().toUpperCase();
		return userAgent;
	}
	
}
