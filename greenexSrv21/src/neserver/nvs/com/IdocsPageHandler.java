package neserver.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class IdocsPageHandler extends HandlerTemplate {
	public PhisObjProperties pr; 
	public IdocsPageHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		String curStyle = "";

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		pr = parReader.getParametersPhysObject(params.get("guid"));

		this.caption = gData.tr("IDOC statisics for:");



		out += getBeginPage();
		out += strTopPanel(caption);
		out += getIdocStatisticForOneSystem();

		out += getEndPage();

		return out;
	}

	public String getIdocStatisticForOneSystem() {
		String out = "";
		String SQL = "";

		SQL += "SELECT s1.*, ";
		SQL += "TIMESTAMPDIFF(HOUR,s1.check_date_old,s1.check_date_new) AS 'past_hours'  ";
		SQL += "FROM ( ";
		SQL += "SELECT object_guid, MAX(check_date) AS check_date_new, MIN(check_date) AS check_date_old FROM monitor_idocs ";
		SQL += "GROUP BY object_guid ) s1 ";
		SQL += "WHERE object_guid = '" + params.get("guid") + "' ";




		List<Map<String, String>> records = gData.sqlReq.getSelect(SQL);

		for (Map<String, String> rec : records) {
			
			String objectGuid = params.get("guid");
			String newCheckDate = rec.get("check_date_new");
			String oldCheckDate = rec.get("check_date_old");
	
			out += " Проект:" + pr.project + "<br>";
			out += " SAP система:" + pr.shortCaption + "<br>";
			
			int minutes = Integer.valueOf(rec.get("past_hours")) * 60;	
			out += "<br> интервал наблюдения " + Utils.timeConvert(minutes,gData.lang) + "<br>";
			out += "с " + rec.get("check_date_old") + " по " + rec.get("check_date_new") + "<br><br>";
		
			
			

			SQL = "";
			SQL += "SELECT s1.`year`, s1.`month`, monthname(STR_TO_DATE(s1.`month`,'%m')) AS 'месяц',s1.`sender`,s1.`reciever`,s1.`idoc_type`,s1.`status`,  "; 
			SQL += "FORMAT(s1.total_new,0) AS 'количество_idoc', ";
			SQL += "FORMAT(s2.total_old,0) AS 'предыдущее значение', ";
			SQL += "s1.total_new - s2.total_old AS 'рост'   ";
			SQL += "FROM   ";
			SQL += "(SELECT DISTINCT CONCAT(`month`,`year`,sender,reciever,idoc_type,`status`) AS `key`,  ";
			SQL += "object_guid,`month`,`year`,sender,reciever,idoc_type,STATUS,total AS total_new FROM monitor_idocs  ";
			SQL += "WHERE object_guid = '" + objectGuid + "' AND check_date = '" + newCheckDate + "' ) s1  ";
			SQL += "LEFT JOIN   ";
			SQL += "(SELECT DISTINCT CONCAT(`month`,`year`,sender,reciever,idoc_type,`status`) AS `key`,  ";
			SQL += "object_guid,`month`,`year`,sender,reciever,idoc_type,STATUS,total AS total_old FROM monitor_idocs  ";
			SQL += "WHERE object_guid = '" + objectGuid + "' AND check_date = '" + oldCheckDate + "' ) s2  ";
			SQL += "ON s1.`key` = s2.`key`  ";
			SQL += "ORDER BY ABS(рост) DESC, `year`,`month`  ";
			
			out += Utils.getHtmlTablePageFromSqlreturn(gData, SQL);

		}

		return out;
	}
}
