package moni2.greenexSrv2.nvs.com;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.SqlReturn;

public class SAPR3 {
	public globalData gData = null;
	public Map<String, String> params = null;
	static JCoDestination destination = null;
	public String destFileName = "";

	public SAPR3(globalData gData, Map<String, String> params) {
		this.gData = gData;
		this.params = params;
		createAbapDestination();
		
	}

	public String readTable(String tableName, String fields, String filter, String delim) {
		String out = "";

//		createAbapDestination();
		
		//removeAbapDistination();
		try {

			out = rfcReadTable(tableName, fields, filter, delim);

		} catch (JCoException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.info(errors.toString());
			out = "CONNECT_ERROR";
			gData.saveToLog("ERROR : " + params.get("sid") + " \n" + errors.toString() , params.get("job_name"));

		}

		return out;
	}
	public SqlReturn readBigTable(String tableName, String fields, String filter, String delim) {
		SqlReturn out = new SqlReturn();


//		createAbapDestination();
		
		//removeAbapDistination();
		try {

			out = rfcReadBigTable(tableName, fields, filter, delim);

		} catch (JCoException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.info(errors.toString());
			out.isOk = false;
			gData.saveToLog("ERROR : " + params.get("sid") + " \n" + errors.toString() , params.get("job_name"));

		}

		return out;
	}
	public String createAbapDestination() {

		String out = "";
		
		String dest_name = params.get("job_name") == null? "abap": params.get("job_name");

		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, params.get("ip"));
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, params.get("sysnr"));
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, params.get("clnt"));
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, params.get("user"));
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, params.get("password"));
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
		createDestinationDataFile(dest_name, connectProperties);

		try {
			destination = JCoDestinationManager.getDestination(dest_name);

		} catch (JCoException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			out = errors.toString();
			gData.logger.info(errors.toString());

		}

		return out;
	}

	public void createDestinationDataFile(String destinationName, Properties connectProperties) {
		File destCfg = new File(destinationName + ".jcoDestination");
		try {
			FileOutputStream fos = new FileOutputStream(destCfg, false);
			connectProperties.store(fos, "greenex");
			fos.close();
		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.info(errors.toString());

		}
	}

	public String rfcReadTable(String tablename, String fields, String filter, String delim) throws JCoException {

//		createAbapDestination();
		
		String out = "";

		String FUNCTION_NAME = "RFC_READ_TABLE";
		JCoContext.begin(destination);

		gData.saveToLog("rfcReadTable 1 ", params.get("job_name"));
		
		JCoFunction function = destination.getRepository().getFunction(FUNCTION_NAME);
		if (function == null) {
			throw new RuntimeException(FUNCTION_NAME + " not found in SAP.");
		}

		
		gData.saveToLog("rfcReadTable 2 ", params.get("job_name"));
		
		String strFields[] = fields.split("\\s*,\\s*");

		JCoTable inputTableParam = function.getTableParameterList().getTable("FIELDS");

		for (int i = 0; i < strFields.length; i++) {
			inputTableParam.appendRow();
			inputTableParam.setValue("FIELDNAME", strFields[i]);
		}

		function.getImportParameterList().setValue("QUERY_TABLE", tablename);

		function.getImportParameterList().setValue("DELIMITER", delim);

		if (!filter.isEmpty()) {
			JCoTable optionsTableParam = function.getTableParameterList().getTable("OPTIONS");
			optionsTableParam.appendRow();
			optionsTableParam.setValue("TEXT", filter);
//	    	optionsTableParam.setValue("TEXT", filter"BNAME EQ 'ENEVTIS' OR BNAME EQ 'SAP*'");
		}

		gData.saveToLog("rfcReadTable 3 ", params.get("job_name"));
		
		try {
			function.execute(destination);
		
			gData.saveToLog("rfcReadTable 4 ", params.get("job_name"));
			
		} catch (Exception e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog("ERROR : " + params.get("sid") + " \n" + errors.toString() , params.get("job_name"));
			
			return "error " + e.getMessage();
		}

		JCoTable table = function.getTableParameterList().getTable("DATA");
		
		gData.saveToLog("rfcReadTable 5 table.getNumRows()= : " + table.getNumRows() , params.get("job_name"));
		
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);

			out += table.getString("WA") + "::";

		}

		gData.saveToLog("rfcReadTable 6 table.getNumRows()= : " + table.getNumRows() , params.get("job_name"));
		
		JCoContext.end(destination);

		return out;

	}
	public SqlReturn rfcReadBigTable(String tablename, String fields, String filter, String delim) throws JCoException {

		
		SqlReturn out = new SqlReturn();
		out.records = new ArrayList<Map<String, String>>();

		String FUNCTION_NAME = "RFC_READ_TABLE";
		JCoContext.begin(destination);

		gData.saveToLog("rfcReadTable 1 ", params.get("job_name"));
		
		JCoFunction function = destination.getRepository().getFunction(FUNCTION_NAME);
		if (function == null) {
			throw new RuntimeException(FUNCTION_NAME + " not found in SAP.");
		}

		
		gData.saveToLog("rfcReadTable 2 ", params.get("job_name"));
		
		String strFields[] = fields.split("\\s*,\\s*");

		JCoTable inputTableParam = function.getTableParameterList().getTable("FIELDS");

		for (int i = 0; i < strFields.length; i++) {
			inputTableParam.appendRow();
			inputTableParam.setValue("FIELDNAME", strFields[i]);
		}

		function.getImportParameterList().setValue("QUERY_TABLE", tablename);

		function.getImportParameterList().setValue("DELIMITER", delim);

		if (!filter.isEmpty()) {
			JCoTable optionsTableParam = function.getTableParameterList().getTable("OPTIONS");
			optionsTableParam.appendRow();
			optionsTableParam.setValue("TEXT", filter);
//	    	optionsTableParam.setValue("TEXT", filter"BNAME EQ 'ENEVTIS' OR BNAME EQ 'SAP*'");
		}

		gData.saveToLog("rfcReadTable 3 ", params.get("job_name"));
		
		try {
			function.execute(destination);
		
			gData.saveToLog("rfcReadTable 4 ", params.get("job_name"));
			
		} catch (Exception e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.saveToLog("ERROR : " + params.get("sid") + " \n" + errors.toString() , params.get("job_name"));
			out.isOk = false;
			return out;
		}

		JCoTable table = function.getTableParameterList().getTable("DATA");
		
		gData.saveToLog("rfcReadTable 5 table.getNumRows()= : " + table.getNumRows() , params.get("job_name"));
		
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);
			Map<String, String> record = new HashMap<String, String>();
			
			String line = table.getString("WA");
			String lineParts[] = line.split(";");
			
			for(int y=0; y < lineParts.length; y ++) {
				record.put(strFields[y], lineParts[y]);	
			}
			out.records.add(record);

		}

		gData.saveToLog("7 out.records.size= : " + out.records.size() , params.get("job_name"));
		
		gData.saveToLog("rfcReadTable 6 table.getNumRows()= : " + table.getNumRows() , params.get("job_name"));
		
		JCoContext.end(destination);

		return out;

	}
	public String rfcGetSystemInfo() {
		String out = "";

		String FUNCTION_NAME = "RFC_GET_SYSTEM_INFO";

		out = FUNCTION_NAME;

//		createAbapDestination();

		JCoContext.begin(destination);

		JCoFunction function = null;
		try {

			function = destination.getRepository().getFunction(FUNCTION_NAME);

			function.getImportParameterList().setValue("DESTINATION", "NONE");
			function.execute(destination);

			JCoStructure struct = function.getExportParameterList().getStructure("RFCSI_EXPORT");

			JCoFieldIterator fieldIt = struct.getFieldIterator();

			while (fieldIt.hasNextField()) {
				JCoField field = fieldIt.nextField();
				out += field.getName() + "=" + field.getString() + "::";
			}

		} catch (JCoException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			out = errors.toString();
		} finally {

			try {
				JCoContext.end(destination);
			} catch (JCoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (function == null) {

			StringWriter errors = new StringWriter();
			gData.logger.severe(FUNCTION_NAME + " not found in SAP.");
		}

		return out;
	}

	public SqlReturn th_WPInfo(String srvName) {

		SqlReturn out = new SqlReturn();
		out.isOk = false;

		String FUNCTION_NAME = "TH_WPINFO";
		
//		createAbapDestination();

		JCoContext.begin(destination);

		JCoFunction function = null;
		try {

			function = destination.getRepository().getFunction(FUNCTION_NAME);

			if (!srvName.isEmpty())
				function.getImportParameterList().setValue("SRVNAME", srvName);

			function.execute(destination);

			JCoTable outTable = function.getTableParameterList().getTable("WPLIST");

			for (int i = 0; i < outTable.getNumRows(); i++) {
				outTable.setRow(i);
				Map<String, String> record = new HashMap<String, String>();

				JCoFieldIterator iter = outTable.getFieldIterator();
//			        LinkedHashMap m = new LinkedHashMap();
				while (iter.hasNextField()) {
					JCoField f = iter.nextField();
					record.put(f.getName(), outTable.getValue(f.getName()) + "");

//			            out += f.getName()+ "=" + outTable.getValue(f.getName()) + "<br>";
				}

				out.records.add(record);
				out.isOk = true;

			}

		} catch (JCoException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			
			out.isOk = false;

		} finally {

			try {
				JCoContext.end(destination);
			} catch (JCoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (function == null) {

			StringWriter errors = new StringWriter();
			gData.logger.severe(FUNCTION_NAME + " not found in SAP.");
		}

		return out;
	}
	public void	removeAbapDistination() {
		
	}
}
