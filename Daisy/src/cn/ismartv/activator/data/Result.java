package cn.ismartv.activator.data;


public class Result {
    private String device_token;
    private String domain;
    private String ad_domain;
    private String packageInfo;
    private String expiry_date;
    private String sn_token;
    private String log_domain;
    private String upgrade_domain; 
    
    public String getUpgrade_domain() {
		return upgrade_domain;
	}
	public void setUpgrade_domain(String upgrade_domain) {
		this.upgrade_domain = upgrade_domain;
	}
	public String getSn_Token(){
    	return sn_token;
    }
    public void setSn_Token(String sn){
    	this.sn_token = sn;
    }
    public String getLog_Domain(){
    	return log_domain;
    }
    public void setLog_Domain(String log){
    	this.log_domain = log;
    }
    public String getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAd_domain() {
        return ad_domain;
    }

    public void setAd_domain(String ad_domain) {
        this.ad_domain = ad_domain;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("device_token ---> ").append(device_token).append(" \n")
                .append("packageInfo ---> ").append(packageInfo+" \n").append("domain ------>").append(domain);
        return stringBuffer.toString();
    }
}
