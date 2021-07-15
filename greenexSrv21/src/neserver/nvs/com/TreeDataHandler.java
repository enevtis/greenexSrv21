package neserver.nvs.com;

import java.io.File;
import greenexSrv2.nvs.com.globalData;

public class TreeDataHandler extends HandlerTemplate {

	String postData = "";

	public TreeDataHandler(globalData gData) {
		super(gData);

	}
	@Override
	public String getPage() {
		String out = "";
	
		
		String path=params.get("node_id");
		
		out += readDirContent(path);
		
		return out;
	}
	
	protected String readDirContent(String path) {
		String out = "";

		out+=" [ \n";		
		
		File f = new File(path);
/*
		FilenameFilter filter = new FilenameFilter() {
	        @Override
	        public boolean accept(File f, String name) {
	            return name.endsWith(".sql");
	        }
	    };	
*/
	    File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
    		out+=" { ";
    		out +="\"id\":" ;          
    		out +="\"" + files[i].getAbsolutePath() + "\",";
    		out+="\"title\":" ;          
    		out +="\"" + files[i].getName() + "\",";
    		out+="\"isFolder\":" ;             
    		out += files[i].isDirectory()? "1":"0";    		
    		out+=" },";
        }
        out =  out.substring(0, out.length() - 1);
        out+=" ] \n";
		
		return out;
	}
}
//out+=" [ \n";
//out+=" { \"id\": 1, \"title\": \"Node 1\", \"isFolder\": 1},\n";
//out+=" { \"id\": 2, \"title\": \"Node 2\", \"isFolder\": 0},\n";
//out+=" { \"id\": 3, \"title\": \"Node 3\", \"isFolder\": 1}\n";
//out+=" ] \n";
