package health.greenexSrv2.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.greenexSrv2.com.GraphDisk;
import graph.greenexSrv2.com.PngDisksDiagramPainter;
import greenexSrv2.nvs.com.globalData;
import moni2.greenexSrv2.nvs.com.BatchJobTemplate;

public class RegularOpsbiHealthReport extends BatchJobTemplate implements Runnable {

	public String body = "";
	public List<String> files = new ArrayList<>();
	public RegularOpsbiHealthReport(globalData gData, Map<String, String> params) {
		super(gData, params);
	}

	@Override
	public void run() {

		try {

			setRunningFlag_shedule();
			doCheck();
			reSetRunningFlag_shedule();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}

	}

	protected void doCheck() {
		getDisksInfo();
		gData.saveToLog(body, getJobName());
	}
	protected void getDisksInfo() {
		List<GraphDisk> disks = initDisks();
		
		PngDisksDiagramPainter dp = new PngDisksDiagramPainter();
		dp.imgPath = gData.mainPath + File.separator + "img";
		
		String fileName = dp.paintDisksDiagram(disks, "OPSBI") + ".png";
		files.add(fileName);
		body += "<img src='" + fileName + "'>";
		
		
	}

	public List<GraphDisk> initDisks() {
		List<GraphDisk> out = new ArrayList();

		GraphDisk d = new GraphDisk();
		d.maxSizeGb = 60;
		d.usedSizeGb = 55;
//		d.minUsedSizeGb = 1;
		d.path = "/";
		out.add(d);
		
		d = new GraphDisk();
		d.maxSizeGb = 1024;
		d.usedSizeGb = 235;
		d.path = "/usr/sap/";
		out.add(d);

		d = new GraphDisk();
		d.maxSizeGb = 512;
		d.usedSizeGb = 67;		
		d.path = "/sapmnt/";
		out.add(d);		

		d = new GraphDisk();
		d.maxSizeGb = 512;
		d.usedSizeGb = 370;		
		d.path = "/oracle/";
		out.add(d);
		
		d = new GraphDisk();
		d.maxSizeGb = 450;
		d.usedSizeGb = 123;		
		d.path = "/disk1/";
		out.add(d);
		
		
		d = new GraphDisk();
		d.maxSizeGb = 220;
		d.usedSizeGb = 1.5f;		
		d.path = "/disk2/";
		out.add(d);
		return out;
	}

protected String getJobName() {
	String out = "";
	out = params.containsKey("job_name") ? params.get("job_name"): this.getClass().getSimpleName();
	return out;
}
}
