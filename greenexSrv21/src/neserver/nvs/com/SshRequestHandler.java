package neserver.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import greenexSrv2.nvs.com.ObjectParametersReader;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.FileSystemsSpaceDiagram;
import obj.greenexSrv2.nvs.com.ConnectionData;
import obj.greenexSrv2.nvs.com.PhisObjProperties;

public class SshRequestHandler extends HandlerTemplate {

	public SshRequestHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {
		String out = "", osType="";
		
		String action = params.get("action");	
		
		this.caption = (params.containsKey("caption")) ? params.get("caption") : "";

		if (!this.caption.isEmpty())
			caption = globalData.decode(this.caption);
		else
			caption = readFromTable_caption(this.getClass().getSimpleName(), action);

		ObjectParametersReader parReader = new ObjectParametersReader(gData);
		PhisObjProperties pr = parReader.getParametersPhysObject(params.get("guid"));
		ConnectionData conData = readConnectionParameters(pr);
		
		
		gData.saveToLog("pr.description= " + pr.description + " pr.shortCaption=" + pr.shortCaption, this.getClass().getSimpleName());
		
		this.caption += " " + pr.description;
		
		out += getBeginPage();
		out += strTopPanel(this.caption);

		
		
		switch (action) {

		case "disks":
			
			String remoteText = readFromTable_ssh_remote_check(this.getClass().getSimpleName(), action, pr.OS);
			
			if (gData.debugMode) gData.saveToLog("remoteText= " + remoteText + " pr.OS=" + pr.OS, this.getClass().getSimpleName());
			
				try {

 
					String response = gData.sshReq.getSsh(conData.ip, conData.user, conData.password, remoteText);
					
					FileSystemsSpaceDiagram dia= new FileSystemsSpaceDiagram();
					out += dia.buildFileSystemDiagram(response);

				} catch (Exception e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe("<p stype='color:red;'> ip=" + conData.ip + " user=" + conData.user + " hash="
						+ conData.hash + " gave an connect error" + errors.toString() + "<br>" + remoteText + "</p>");

				}		

			
			break;

		default:

			break;

		}

		out += "<p style='font-size:80%;'>" + gData.getCurrentTime() + "</p>";
		out += getEndPage();

		return out;
	}

}
// out += params.get("action");
//		out += params.get("guid");		
