package moni.greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class remoteSystem {
	public Map<String, String> params = new HashMap<String, String>();
	public List<remoteDisk> disks = new ArrayList<>();
	public Map<String, String> services = new HashMap<String, String>();
	public Map<String, String> folders = new HashMap<String, String>();
	public List<Map<String , String>> records = null;

	public String getShortDescr() {
	String out = "";
	
	out += " ip=" + params.get("def_ip");
	out +=  " sid=" + params.get("sid");
	out += " hostname=" + params.get("def_hostname") +"";
	out +=  " sysnr=" + params.get("sysnr");
	out +=  " clnt=" + params.get("clnt");  
	out +=  " user=" + params.get("user");
	return out;
  }
	
	

}
