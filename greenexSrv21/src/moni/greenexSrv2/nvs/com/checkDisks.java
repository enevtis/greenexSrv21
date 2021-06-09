package moni.greenexSrv2.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import greenexSrv2.nvs.com.globalData;

public class checkDisks extends checkTemplate{
	String SQL_for_list = "";
	
	public checkDisks (globalData gData, batchJob job) {
		
		super(gData,job);
		
	}
	
	public void executeCheck() {
		
		SQL_for_list = getSqlForServers(job);
		
		
		
		List<remoteSystem> systems = getListForCheck(SQL_for_list);


		SaveToLog("check " + systems.size() + " systems", job.job_name);

		for(remoteSystem s: systems) {
				
			try {
				
				remoteCheck(s);
				
				gData.logger.fine("s.disks.size()=" + s.disks.size());
			
			}catch(Exception e) {
				
				gData.logger.info(e.getMessage());
				s.params.put("result_text",e.getMessage());
			}
		}
		
		
		
		
		saveCheckResult(systems);


		SaveToLog("written to database:", job.job_name);
		
		
	}
	private void remoteCheck(remoteSystem system) {
		
		String os_version = system.params.get("os_version").toUpperCase();
		
		if(os_version.contains("WINDOWS")) {
		
			checkDisksWindows(system);
		
		}else if (os_version.contains("LINUX")){
		
		    checkDisksLinux(system);
		
		}else if (os_version.contains("HP-UX")){ 
			
			checkDisksHPUX(system);
		
		}else {
		
			system.params.put("message", "Check canceled. Type OS " + os_version + " is unknown");
		
		}
		
	}
	private void checkDisksWindows(remoteSystem s) {
		
		
		disableSslVerification();
		
		String ip = s.params.get("def_ip");
		String port = "8443";
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		URL url = null;
		
		
		String password = gData.getPasswordFromHash(hash);
		
		
		try {

			
			
				url = new URL("https://" + ip + ":" + port + "/query/check_drivesize");		  
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Password", password);
				conn.setRequestProperty("Content-type", "application/json");
				conn.setRequestProperty("Accept", "application/json");		
				conn.setRequestMethod("GET");


				if (conn.getResponseCode() != 200) {
						s.params.put("result_text","Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
											

				String output;
				String outText = "";
		
					while ((output = br.readLine()) != null) {
						outText += output;
					}
		
		
		
				String message="";
				String result="";		

				JSONObject obj;
	
				try {
					obj = new JSONObject(outText);

						JSONObject res = obj.getJSONArray("payload").getJSONObject(0);
			
						JSONArray values = res.getJSONArray("perf");
			
			  
						for (int i = 0; i < values.length(); i++) {
				
							JSONObject current = values.getJSONObject(i); 			    
							String alias = current.getString("alias");
				  
								if (alias.contains(":") && !alias.contains("%")) {
				
									String[] buffer = alias.split(":");
									String disk_letter = buffer[0];
					
									JSONObject f1 = current.getJSONObject("float_value");
									String unit = f1.getString("unit");
										float used = (float)f1.getDouble("value");
										float maximum = (float)f1.getDouble("maximum");


								remoteDisk disk = new remoteDisk();
								disk.name = disk_letter;

								if (unit.equals("GB")){
									disk.maxSize = maximum;
									disk.usedSize = used;	
								}else if (unit.equals("TB")){
									disk.maxSize = maximum * 1024;
									disk.usedSize = used * 1024;
								}else {
									//disk.avail = -1;
								}
						
								
								s.disks.add(disk);


								}

						}
			  
			
		
				} catch (JSONException e) {
			
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					s.params.put("result_text", errors.toString());
					SaveToLog(url.toString() + "\n" + errors.toString(), job.job_name);
					gData.logger.severe(errors.toString());
		
				}		
				conn.disconnect();

		} catch (MalformedURLException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", errors.toString());
			SaveToLog(url.toString() + "\n" + errors.toString(), job.job_name);
			gData.logger.severe(errors.toString());


		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			s.params.put("result_text", errors.toString());			
			SaveToLog(url.toString() + "\n" + errors.toString(), job.job_name);
			
			gData.logger.severe(errors.toString());

		} 
		

		
	}
	
	private void checkDisksLinux(remoteSystem s) {
		
		String sshReply = "";
	
		String ip = s.params.get("def_ip");
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
	try {	
		sshReply = getSsh(ip, user, password, "df -Tkl");
		
		
//		gData.logger.info(sshReply);
		
		
		String lines[] = sshReply.split("\\r?\\n");
		
		// парсинг 
		for(int i=0; i < lines.length; i++) {
			
			String cLine[] = lines[i].split("\\s+");;
		
				// исключаем некоторые псевдо системы
				if(cLine[1].contains("tmpfs") 
					|| cLine[6].contains("/boot/")
					|| cLine[6].contains("/var/")
					|| cLine[0].contains("ilesystem")
				 ) { 	; } else {
	
					 
							remoteDisk disk = new remoteDisk();
							disk.name = cLine[6];
							disk.maxSize = Float.valueOf(cLine[2]) / 1024 / 1024;
							disk.usedSize = Float.valueOf(cLine[3]) / 1024 / 1024;	
							s.disks.add(disk);
	
		
				}


		}
	
	}catch (Exception e) {
		
		SaveToLog(ip + "\n" + e.toString(), job.job_name);
		
	}

		
/*
		Filesystem             Type      1K-blocks       Used  Available Use% Mounted on
		/dev/vda2              xfs       207517696   77039776  130477920  38% /
		//192.168.0.25/enevtis cifs     4840251488 1538767328 3301484160  32% /seagate		
*/		
		
	}
	private void checkDisksHPUX(remoteSystem s) {
		
		String sshReply = "";
	
		String ip = s.params.get("def_ip");
		String user = s.params.get("user");
		String hash = s.params.get("hash");
		
		String password = gData.getPasswordFromHash(hash);
		
		try {
		sshReply = getSsh(ip, user, password, "df -Plks");
		
		gData.logger.info(sshReply);
		
		String lines[] = sshReply.split("\\r?\\n");
		
		for(int i=1; i < lines.length; i++) {
			
			String buffer = lines[i].trim();
					
			String cLine[] = buffer.split("\\s+");
			
			String fsName = cLine[cLine.length -1];
			String totalKb = cLine[cLine.length -5];
			String usedKb = cLine[cLine.length -4];
	
			remoteDisk disk = new remoteDisk();
			disk.name = fsName;
			disk.maxSize = Float.valueOf(totalKb) / 1024 / 1024;
			disk.usedSize = Float.valueOf(usedKb) / 1024 / 1024;	
			s.disks.add(disk);
			
			
			gData.logger.info(i + " " + fsName + " " + totalKb + " " + usedKb);

				

	
	} 
		} catch (Exception e) {
			
			SaveToLog(ip + "\n" + e.toString(), job.job_name);
			
		}
		
/*
root@sap-bi-dev:/#bdf -l -s
Filesystem          kbytes    used   avail %used Mounted on
/dev/vg00/lvol3    2097152  320608 1762800   15% /
/dev/vg00/lvol1    2818048  512376 2287712   18% /stand
/dev/vg00/lvol8    10289152 6673272 3589232   65% /var
/dev/vg00/lvol7    20512768 16656448 3832448   81% /usr
/dev/vg00/lvol6    2097152  878792 1210448   42% /tmp
/dev/vg00/sapmnt   52428800 28457532 22720753   56% /sapmnt/BWD
/dev/vg01/sap      6144000000 4537981736 1593472192   74% /sap
/dev/vg00/lvol5    10223616 4797256 5384000   47% /opt
/dev/vg00/lvol4     851968   39024  806680    5% /home
root@sap-bi-dev:/#
	
*/		
		
	}	
	
	
}
