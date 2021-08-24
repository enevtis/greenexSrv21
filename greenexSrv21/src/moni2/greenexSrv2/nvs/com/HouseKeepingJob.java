package moni2.greenexSrv2.nvs.com;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import greenexSrv2.nvs.com.globalData;
import moni.greenexSrv2.nvs.com.batchJob;

public class HouseKeepingJob extends BatchJobTemplate implements Runnable{

	private batchJob job = null;
	
	public HouseKeepingJob (globalData gData, Map<String, String> params) {
		super(gData,params);
		
	}
	
	@Override
	public void run() {

	try {
	
			setRunningFlag_regular();

			deleteOldCheckResults();
			
			deleteOldImageFiles();

			reSetRunningFlag_regular();
			

		} catch(Exception e) {
			gData.logger.info("Error - begin new time");
		}	
		
	}
	

	
	private void deleteOldCheckResults() {
		
		String SQL = "";
		
		List<String> sql_list = new ArrayList<String>();
		
		
		
		SQL = "DELETE FROM monitor_results WHERE id IN ( \n";
		SQL += " SELECT s1.id FROM ( \n";
		SQL += " select a.id from monitor_results a  \n";
		SQL += " left join monitor_schedule b on a.monitor_number = b.number \n"; 
		SQL += " where DATEDIFF(CURDATE(),a.check_date) > b.keep_days) s1 \n";
		SQL += ")  \n";

		sql_list.add(SQL);		
		
		
		SQL = "delete from monitor_disks where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=101)" ;
		sql_list.add(SQL);
		
		SQL = "delete from mon_top_memory where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=211)" ;
		sql_list.add(SQL);		

		SQL = "delete from mon_hana_alerts where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=212)" ;
		sql_list.add(SQL);		

		SQL = "delete from monitor_sql where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=218)" ;
		sql_list.add(SQL);		

		SQL = "delete from monitor_hana_metrics where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=216)" ;
		sql_list.add(SQL);	
		
		SQL = "delete from problems " ;
		SQL += "WHERE is_fixed='X' AND TIMESTAMPDIFF(HOUR,fixed,NOW()) > 24" ;
		sql_list.add(SQL);		
		
		SQL = "delete from monitor_abap_wp where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=303)" ;
		sql_list.add(SQL);		
		
		SQL = "delete from monitor_idocs where " ;
		SQL += "DATEDIFF(CURDATE(),check_date) > ( select max(keep_days) from monitor_schedule where number=305)" ;
		sql_list.add(SQL);			
		
		if ( gData.sqlReq.saveResult(sql_list) ) {
			gData.logger.info("Housekeeping job was executed successfully ");
		} else {
			gData.logger.severe("Housekeeping job has ERROR. ");			
			
		};
	}
	
	public void deleteOldImageFiles() {
		String checkDirectory = gData.mainPath + File.separator + "img" + File.separator;
		long daysAgeForDelete = 2;
		List<String> filesList = new ArrayList();

		File folder = new File(checkDirectory);

		listFilesForFolder(folder, filesList, daysAgeForDelete);

		for (String f : filesList) {

			File file = new File(f);
			if (file.delete()) {
				gData.logger.info(f + " has been deleted after "+ daysAgeForDelete + " days");
			} else {
				gData.logger.info(" error deleting : " + f);
			}
		}

	}
	
	public void listFilesForFolder(final File folder, List<String> filesList, long daysAge) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, filesList, daysAge);
			} else {

				String fullName = fileEntry.getAbsolutePath();
				Path file = Paths.get(fullName);

				try {
					BasicFileAttributes attr;
					attr = Files.readAttributes(file, BasicFileAttributes.class);

					LocalDateTime lastModified = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(),
							ZoneId.systemDefault());
					LocalDateTime now = LocalDateTime.now();

					long diff = ChronoUnit.DAYS.between(lastModified, now);
					if (diff > daysAge) {
						filesList.add(fullName);
					}

//    		System.out.println(fileEntry.getAbsolutePath() + " lastModifiedTime: " + lastModified + " past days=" + diff);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}
