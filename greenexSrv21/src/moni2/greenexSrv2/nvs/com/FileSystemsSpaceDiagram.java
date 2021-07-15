package moni2.greenexSrv2.nvs.com;

public class FileSystemsSpaceDiagram {

	public String buildFileSystemDiagram(String sshResponse) {
		String out = "";
		
	     String lines[] = sshResponse.split("\\r?\\n");
	     
	     out+= "&nbsp;&nbsp;&nbsp;";
	     out+= "<table style='background-color:black;color:white;font-family:Courier New, monospace;font-size:12px;' border=0>";
	     out+= "<thead>";	     
	     out+= "<tr> ";
	     out+= "<th>&nbsp</th> ";
	     out+= "<th>file system </th> ";
	     out+= "<th>percent</th> ";
	     out+= "<th>total gb</th> ";
	     out+= "<th>used gb</th> ";	  
	     out+= "</tr> ";
	     out+= "</thead>";

	     out+= "<tbody>";
	     	     
	     for(String line: lines) {
			String parts[] = line.split("\\s+");
	    	Double totalVaue = Double.valueOf(parts[0]);
	    	Double usedValue = Double.valueOf(parts[1]); 
	    	 
	    	int leftPart = (int) (float) ((usedValue / totalVaue) * 200);
	    	int rightPart = 200 - leftPart; 
	    	
	     	out+= "<tr> ";
	     	out+= "<td> ";
	     	out+= "<div style='float:left;background-color:#0524aa;width:" + leftPart + "px;height:10px;'>&nbsp;</div> ";
	     	out+= "<div style='float:left;background-color:#A0A0A0;width:" + rightPart + "px;height:10px;'>&nbsp;</div> ";
	     	out+= "</td> ";

	     	out+= "<td>"  + parts[2] + "</td> ";
	     	out+= "<td>"  +  String.format("%.0f",(usedValue / totalVaue) * 100)  + "&percnt;</td> ";

	     	out+= "<td>"  + String.format("%.1f",totalVaue / 1024 / 1024) +  "</td> ";
	     	out+= "<td>"  + String.format("%.1f",usedValue / 1024 / 1024) +  "</td> ";
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
