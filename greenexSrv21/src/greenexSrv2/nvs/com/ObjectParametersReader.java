package greenexSrv2.nvs.com;

import java.util.List;
import java.util.Map;

import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class ObjectParametersReader {
	protected globalData gData;
	
	public ObjectParametersReader(globalData gData) {
		this.gData = gData; 
	}
	
	public PhisObjProperties getParametersPhysObject(String Guid) {

		PhisObjProperties out = new PhisObjProperties();
		String SQL = "";
		String checkedPhysGuid = Guid;
		String checkedGraphGuid = "none";
		String buffer = "";
	
		int kvo = 0;
		SQL = "SELECT count(*) as kvo FROM v_objects_all WHERE guid = '" + Guid + "'"; 
		List<Map<String , String>> records_list  = gData.sqlReq.getSelect(SQL);
		
		if (records_list != null ) {
			if (records_list.size() > 0) {
				
				for (Map<String, String> rec : records_list) {
					
					kvo = Integer.valueOf(rec.get("kvo"));
				}
			}
		}
		
		
		
		if (kvo==1){
			checkedPhysGuid = Guid;
		}else {
		
	
			SQL = "SELECT physGuid FROM graph_objects WHERE guid = '" + Guid + "'"; 
			records_list  = gData.sqlReq.getSelect(SQL);
		
			if (records_list != null ) {
				if (records_list.size() > 0) {
				
					for (Map<String, String> rec : records_list) {
					
						checkedPhysGuid = rec.get("physGuid");
					}
				}
			}		
		
			if(records_list.size()!=1) {
			gData.logger.severe("There are " + records_list.size() + "records in graph_objects !  grapGuid= " + Guid + " checkedPhysGuid=" + checkedPhysGuid);
			}	
		
		}
		

		
		SQL = "";
		SQL += "SELECT a.*, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN b.def_ip \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN c.def_ip \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.def_ip \n";
		SQL += "END AS ip, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN '' \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN c.sid \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.sid \n";
		SQL += "END AS sid, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN '' \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN c.port \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.port \n";
		SQL += "END AS port, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN '' \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN c.sysnr \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.sysnr \n";
		SQL += "END AS sysnr, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN '' \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN '' \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.sap_scheme \n";
		SQL += "END AS dbuser, \n";
		SQL += "CASE \n";
		SQL += "WHEN a.obj_typ = 'servers' THEN CONCAT(b.def_hostname,' ',b.os_typ) \n";
		SQL += "WHEN a.obj_typ = 'db_systems' THEN c.db_type \n";
		SQL += "WHEN a.obj_typ = 'app_systems' THEN d.app_typ \n";
		SQL += "END AS description \n";
		SQL += " FROM v_objects_all a \n";
		SQL += " LEFT JOIN servers b ON a.guid=b.guid \n";
		SQL += " LEFT JOIN db_systems c ON a.guid=c.guid \n";
		SQL += " LEFT JOIN app_systems d ON a.guid=d.guid \n";
		SQL += " WHERE a.guid = '" + checkedPhysGuid + "' \n";

		
		records_list = gData.sqlReq.getSelect(SQL);
		String descr = "";
		
		if (records_list != null) {
			if (records_list.size() > 0) {

				for (Map<String, String> rec : records_list) {

					out.graphGuid = checkedGraphGuid;
					out.physGuid = checkedPhysGuid;

					out.shortCaption = rec.get("short");
					out.description = rec.get("description");
					out.obj_typ = rec.get("obj_typ");
					out.ip = rec.get("ip");
					out.sid = rec.get("sid");
					out.port = rec.get("port");
					out.sysnr = rec.get("sysnr");
					out.sysnr = rec.get("dbuser");
					
					descr = out.description.toUpperCase();
					descr += out.shortCaption.toUpperCase();
					
					
					switch (out.obj_typ) {
					case "servers":
						if(descr.contains("LINUX")) out.OS = "LINUX";
						if (descr.contains("HPUX") ||  descr.contains("HP-UX")) out.OS = "HPUX";
						if (descr.contains("AIX"))	out.OS = "AIX";			
						if (descr.contains("WIND")) out.OS = "WINDOWS";						
					break;
					
					case "db_systems":
						
						if(descr.contains("SAPHANA")) out.DB = "SAPHANA";
						else if (descr.contains("ORACLE"))  out.DB = "ORACLE";
						else if (descr.contains("MSSQL"))	out.DB = "MSSQL";			
						else if (descr.contains("MYSQL")) out.DB = "MYSQL";							
						
						break;
					case "app_systems":					
						if(descr.contains("SAP") && descr.contains("NW")) out.APP = "SAPNW";
						else if(descr.contains("SAP") && descr.contains("WEAVER")) out.APP = "SAPNW";					
					
						break;					
					
					}
					
				}
			} 
		}

		return out;
	}
}
