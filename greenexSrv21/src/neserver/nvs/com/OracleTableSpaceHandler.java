package neserver.nvs.com;

import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class OracleTableSpaceHandler extends HandlerTemplate {

	public OracleTableSpaceHandler(globalData gData) {
		super(gData);

	}


	@Override
	public String getPage() {
		String out = "", osType="";
		
		String curStyle = "";
		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));

		this.caption = gData.tr("TABLESPACE for ") ;
		this.caption += pr.description + " " + pr.sid + " " + pr.sysnr;
		
		out += getBeginPage();
		out += strTopPanel(caption);

		String remoteSQL = readFromTable_sql_remote_check(this.getClass().getSimpleName(),"tablespace",pr.DB);
		
		ConnectionData conData = readConnectionParameters(pr);
		String connString = "jdbc:oracle:thin:@" + conData.ip + ":" + conData.port + ":"
				+ conData.sid;
		SqlReturn ret = gData.sqlReq.getSqlFromOracle(connString, conData.user, conData.password, remoteSQL);
		
		if (ret.isOk) 
			out += buildOracleTableSpaceDiagram(ret.records);
		 else 

			out+= "<p style='color:red;'>" + ret.message + "</p>";
		
		out += getEndPage();
		
		return out;	
	}
	public String buildOracleTableSpaceDiagram(List<Map<String, String>> records) {
		String out = "";
	     
	     out+= "&nbsp;&nbsp;&nbsp;";

	     String style = gData.diagramStyle ;
	     out+= "<table style='" + style + "' border=0>";
	     out+= "<thead>";	     
	     out+= "<tr> ";
	     out+= "<th>&nbsp</th> ";
	     out+= "<th>tablespace </th> ";
	     out+= "<th>data_file</th> ";
	     out+= "<th>autoextend &gt;&gt;&gt;</th> ";
	     out+= "<th>max mb</th> ";	  
	     out+= "<th>size mb</th> ";
	     out+= "<th>free mb</th> ";	     
	     out+= "</tr> ";
	     out+= "</thead>";

	     out+= "<tbody>";
	     	     
		for (Map<String, String> rec : records) {

	    	
	     	out+= "<tr> ";
	     	out+= "<td> ";
	     	
	     	float max_mb = 0, size_mb =0 , free_mb = 0;
	     	
	     	try {
	     		max_mb = Float.valueOf(rec.get("MAX_MB"));
	     	}catch(Exception e) {
	     		max_mb = 0;
	     	}
	     	
	     	try {
	     		size_mb = Float.valueOf(rec.get("SIZE_MB"));
	     	}catch(Exception e) {
	     		size_mb = 0;
	     	}
	     	
	     	try {
	     		free_mb = Float.valueOf(rec.get("FREE_MB"));
	     	}catch(Exception e) {
	     		free_mb = 0;
	     	}	     	
	     	
	     	int leftPart = 0,rightPart = 0; 
	     	
	     	
	     	if(rec.get(">>>").equals("YES")){
	     		
	     		leftPart = (int) (((size_mb - free_mb) / max_mb) * 200);
 	     		
	     	}else {
	     		
	     		leftPart = (int) (((size_mb - free_mb)  / size_mb) * 200);
	     		
	     	}
	     	
	     	rightPart = (200 - leftPart) > 0 ? 200 - leftPart : 0;
    		
	     		out+= "<div style='float:left;background-color:" + gData.clrDiagramFill + ";width:" + leftPart + "px;height:10px;'>&nbsp;</div> ";
	     		out+= "<div style='float:left;background-color:" + gData.clrDiagramBackground +";width:" + rightPart + "px;height:10px;'>&nbsp;</div> ";

	     	out+= "</td> ";

			out += "<td>" + rec.get("TABLESPACE") + "</td>";
			out += "<td>" + rec.get("DATA_FILE") + "</td>";
			out += "<td>" + rec.get(">>>") + "</td>";
			out += "<td>" + rec.get("MAX_MB") + "</td>";				
			out += "<td>" + rec.get("SIZE_MB") + "</td>";				
			out += "<td>" + rec.get("FREE_MB") + "</td>";	

	    	 out+= "</tr> ";

	    	 
	     }

	     out+= "</tbody>";		
	     out+= "</table>";		
		
		
		return out;
	}
	
	private String getCssStyle(String elementName) {
		String out = "";
		switch(elementName) {
		case "header":
			out += "background-color: darkBlue; ";
			out += "";			
			out += "color: white; ";
			out += "text-align: center; ";
			out += "vertical-align: bottom; ";
			out += "height: 100px; ";
			out += "padding-bottom: 3px; ";
			out += "padding-left: 5px; ";
			out += "padding-right: 5px; ";

			break;
		case "row":
			out += "background-color: white; ";
			out += "color: blue;";
			out += "text-align: center;";
			out += "vertical-align: bottom;";
			out += "height: 600px;";
			out += "padding-bottom: 5px;";
			out += "padding-left: 5px;";
			out += "padding-right: 5px;";

			break;		
		
		
		}
		
		
		return out;
	}
}
