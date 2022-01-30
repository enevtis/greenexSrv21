package health.greenexSrv2.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.greenexSrv2.com.GraphDisk;
import graph.greenexSrv2.com.PngDisksDiagramPainter;
import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.Utils;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.BatchJobTemplate;

public class RegularOpsbiHealthReport2 extends HealthReportTemplate implements Runnable {

	public String body = "";

	public List<String> attFiles = new ArrayList<String>();
	public RegularOpsbiHealthReport2(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_regular();
			doCheck();
			reSetRunningFlag_regular();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void doCheck() {
		
		gData.truncateLog(getJobName());
		List<String> guids = new ArrayList<>();

		attFiles.clear();
		
		//// проверка дисков

		this.body += "<h3>" + gData.tr("9c01211b-57d6-444f-a0e6-1d5f78ee9657") + ":</h3>";
		
		// prod
		guids.add("345bd525-44e0-4edc-b549-b29034a394b5");	//"mlk-opsbi-bw"
		guids.add("41f9e0fa-120b-4b4b-bfd0-34a6c6e2fbad");	//"svo-opsbi-bw"
		
		guids.add("c137434d-4ab4-4215-978a-8e9b7a027df8");	//"mlk-opsbi-bp"		
		guids.add("dc5869f8-977a-462a-ae49-715b44978bc2");	//"svo-opsbi-bp "		
		
		guids.add("447fd25a-5600-4cd8-924c-15e2691b0a4a");	//"mlk-opsbi-ds"
		guids.add("60d0f537-944d-4765-922d-19c266a51868");	//"svo-opsbi-ds"
		

		guids.add("153f00a6-e742-44fa-b6ef-e8abd8c81522");	//"mlk-opsbi-dsp"
		guids.add("253cd0f1-b841-4814-b859-8f6ee2a9949e");	//"mlk-opsbi-bwp"		
		guids.add("3f6032e3-2c23-409f-9b39-dabbeb5a58b8");	//"mlk-opsbi-bpp"
		

		guids.add("661e7fa8-9c2e-4d22-a04f-c81aba889b86");	//"mlk-tst-ots01"		
		guids.add("7230029a-208c-4d12-9cf6-f0fe042575ee");	//"mlk-tst-ots02"		
		guids.add("d3d96b05-dd51-417d-9324-0f048727b668");	//"mlk-tst-ots03"		
		guids.add("49b84470-5b35-4c7c-9716-6114ca4a289b");	//"mlk-tst-ots04"		
	
		
		
		
		this.body += getFreeSpaceOnDiskDiagram(guids, attFiles);
		

		
		/// проверка размера базы 203 -Hana , 210 -Oracle
		
		guids.clear();
		guids.add("5a2010b2-dfe4-4d27-9a04-4fb8513ab7fd");	//PBO
		guids.add("9c599e83-9084-45ef-9de8-80f40eabcf19");  //PBW
		guids.add("9178d683-0fad-4477-aac8-145099e7d467");	//PDS
		
		
		this.body += "<h3>"+gData.tr("8d73fe2e-dff8-4944-a658-68f583c00baa")+" " + Utils.timeConvert(growthDays * 24 * 60,gData.lang) + ":</h3>";
		this.body += getDatabaseGrowth(guids, attFiles);
		

		gData.saveToLog(" Files = " + attFiles.size(), getJobName());
		gData.saveToLog(body, getJobName());
		
		
		MSEcxchange me = new MSEcxchange(gData);

		List<String> recepients = readAllRecepients(params.get("job_name"));
		
		
		me.sendOneLetter2(recepients, gData.tr("e8187450-2468-420a-a105-39516931207a"), this.getClass().getSimpleName() + " " + body, attFiles);
		
		gData.saveToLog("Email is send", getJobName());
		
	}




protected String getJobName() {
	String out = "";
	out = params.containsKey("job_name") ? params.get("job_name"): this.getClass().getSimpleName();
	return out;
}


}
