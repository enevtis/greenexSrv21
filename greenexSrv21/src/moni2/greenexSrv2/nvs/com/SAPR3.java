package moni2.greenexSrv2.nvs.com;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

import greenexSrv2.nvs.com.globalData;

public class SAPR3 {
	public globalData gData = null;
	public Map<String, String> params = null;
	static JCoDestination destination = null;

	public SAPR3(globalData gData, Map<String, String> params) {
		this.gData = gData;
		this.params = params;
	}

	public String readTable(String tableName, String fields, String filter, String delim) {
		String out = "";

		createAbapDestination(); 			


		try {

			out = rfcReadTable(tableName, fields, filter, delim);

		} catch (JCoException e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.info(errors.toString());
			out = "CONNECT_ERROR";
			
			
		}

		return out;
	}

	public void createAbapDestination() {

		String dest_name = "currAbap";

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
			gData.logger.info(errors.toString());

		}

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

		String out = "";

		String FUNCTION_NAME = "RFC_READ_TABLE";
		JCoContext.begin(destination);

		JCoFunction function = destination.getRepository().getFunction(FUNCTION_NAME);
		if (function == null) {
			throw new RuntimeException(FUNCTION_NAME + " not found in SAP.");
		}

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

		try {
			function.execute(destination);
		} catch (AbapException e) {
			System.out.println(e.toString());
			return "error " + e.getMessage();
		}

		JCoTable table = function.getTableParameterList().getTable("DATA");
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);

			out += table.getString("WA") + "::";

		}

		JCoContext.end(destination);

		return out;

	}

}
