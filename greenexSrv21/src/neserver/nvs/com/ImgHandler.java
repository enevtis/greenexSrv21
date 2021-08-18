package neserver.nvs.com;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.net.ssl.SSLSocket;

import greenexSrv2.nvs.com.globalData;



public class ImgHandler extends HandlerTemplate{
	
	
	public ImgHandler(globalData gData) {
		super(gData);
	}

	@Override
	public void getResponse(SSLSocket socket, String paramsString) {
		
		try {
			
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			String pathToFile="/resources" + paramsString;
			InputStream in = getClass().getResourceAsStream(pathToFile);
			
			if (in == null) {
				
				pathToFile=gData.mainPath + paramsString;
				
				File file = new File(pathToFile);
				in = new FileInputStream(file);
				
			}
			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
            int nRead;
            
            byte[] data = new byte[1024];
            
            while ((nRead = in.read(data, 0, data.length)) != -1) {
            	buffer.write(data, 0, nRead);
            }
         
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            
            
            out.writeBytes("HTTP/1.0 200 Document Follows\r\n");

            String ext = pathToFile.split("\\.")[1];
           	out.writeBytes("Content-Type: image/" + ext + "\r\n");
 
            out.writeBytes("Content-Length: " + byteArray.length + "\r\n");
            out.writeBytes("\r\n");

            out.write(byteArray, 0, byteArray.length);
   	   
   	   
            socket.close();           
            
			
		
		}catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());			
		}
		
		
	}
private String cutExtention(String fileName) {
	String out = "";
	return out;
}
}
