package moni.greenexSrv2.nvs.com;

import greenexSrv2.nvs.com.globalData;

public class MonitorService implements Runnable{
	public globalData gData = new globalData();
	private batchJob job = null;

	
	public MonitorService (globalData gData, batchJob job) {
		this.gData = gData;
		this.job = job;
		
	}
	
	@Override
	public void run() {
		

		try {		
 
			gData.logger.info(job.job_name);
			
			
			switch(job.job_name) 
			{ 
            	case "check_full_backup": 
            		checkFullBackup chkBackup = new checkFullBackup(gData,job);
            		chkBackup.executeCheck();
                break; 
 
            	case "check_disks":  
            		checkDisks chkDisks = new checkDisks(gData,job);	
            		chkDisks.executeCheck();
            	break;	
            		
            	case "check_cpu": 
            		checkCPU chkCpu = new checkCPU(gData,job);
            		chkCpu.executeCheck();
            	break; 

            	case "check_memory": 
            		checkMemory chkMemory = new checkMemory(gData,job);
            		chkMemory.executeCheck();
            	break;
            	
            	case "check_service": 
            		checkService chkService = new checkService(gData,job);
            		chkService.executeCheck();
            	break;            	

            	case "check_fresh_files": 
            		checkFreshFilesInFolder chkFreshFiles = new checkFreshFilesInFolder(gData,job);
            		chkFreshFiles.executeCheck();
            	break;

            	case "check_server_uptime": 
            		checkUpTime chkUptime = new checkUpTime(gData,job);
            		chkUptime.executeCheck();
            	break;   	
            	
            	case "check_oracle_free_space": 
            		checkOracleFreeSpace chkOraFreeSpace = new checkOracleFreeSpace(gData,job);
            		chkOraFreeSpace.executeCheck();
            	break;             	

            	case "check_database_size": 
            		checkDataBaseSize chkDBsize = new checkDataBaseSize(gData,job);
            		chkDBsize.executeCheck();
            	break;  
            	
            	case "check_logs_size": 
            		checkLogsVolume chkLogsVol = new checkLogsVolume(gData,job);
            		chkLogsVol.executeCheck();
            	break;

            	case "check_standby_status": 
            		checkStandbyStatus chkStbyStat = new checkStandbyStatus(gData,job);
            		chkStbyStat.executeCheck();
            	break;            	

            	case "check_saphana_license": 
            		checkSapHanaLicense chkSapHanaLic = new checkSapHanaLicense(gData,job);
            		chkSapHanaLic.executeCheck();
            	break;
            	
            	case "check_ds_status": 
            		checkDataServiceMsgStatus chkDsMsgStatus = new checkDataServiceMsgStatus(gData,job);
            		chkDsMsgStatus.executeCheck();
            	break;
            	case "check_abap_dumps": 
            		checkAbapDumps chkAbapDmp = new checkAbapDumps(gData,job);
            		chkAbapDmp.executeCheck();
            	break;           	
            	case "check_idocs": 
            		checkAbapIdocs chkAbapIdoc = new checkAbapIdocs(gData,job);
            		chkAbapIdoc.executeCheck();
            	break;           	
            	case "check_tables_on_disk": 
            		checkTopTablesOnDisk chkTblDsk = new checkTopTablesOnDisk(gData,job);
            		chkTblDsk.executeCheck();
            	break;       
            	
            	case "check_top_memory":
            		checkTopMemoryConsumers chkTopMem = new checkTopMemoryConsumers(gData,job);
            		chkTopMem.executeCheck();
            	break; 
 
            	case "check_saphana_alerts":
            		checkSAPHanaAlerts chkSAPalerts = new checkSAPHanaAlerts(gData,job);
            		chkSAPalerts.executeCheck();
            	break; 

            	case "check_tlocks":
            		checkLockTransCount chkTLock = new checkLockTransCount(gData,job);
            		chkTLock.executeCheck();
            	break; 
            	case "check_hana_threads":
            		checkActiveThreadsHana chkActThreads = new checkActiveThreadsHana(gData,job);
            		chkActThreads.executeCheck();
            	break;            	
            	case "check_hana_unloads":
            		checkUnloadsColumnsHana chkUnloadsCol = new checkUnloadsColumnsHana(gData,job);
            		chkUnloadsCol.executeCheck();
            	break;                	
            	case "check_hana_disk_stat":
            		checkSAPHanaDiskStatistics chkHanaDiskStat = new checkSAPHanaDiskStatistics(gData,job);
            		chkHanaDiskStat.executeCheck();
            	break; 
            	case "check_hana_net_stat":
            		checkSAPHanaNetStatistics chkHanaNetStat = new checkSAPHanaNetStatistics(gData,job);
            		chkHanaNetStat.executeCheck();
            	break; 
 
            	case "check_exp_sql":
            		checkSAPHanaExpenciveSQL chkHanaSQL = new checkSAPHanaExpenciveSQL(gData,job);
            		chkHanaSQL.executeCheck();
            	break; 
            	
            	default: 
                gData.logger.info("Job name " + job.job_name + " is unknown. Ommited"); 
        } 
		
  		String password = "";
        

		
		} catch(Exception e) {
				gData.logger.info("Error - begin new time");
		}
		
	}

	
	

}
