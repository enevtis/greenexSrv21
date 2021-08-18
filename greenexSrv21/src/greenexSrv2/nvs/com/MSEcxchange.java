package greenexSrv2.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class MSEcxchange {
	public globalData gData = new globalData();
	public MSEcxchange(globalData gData) {
		this.gData = gData;
	}

	public void sendOneLetter (String recepientsAll, String SubjectLetter, String BodyLetter ) {
		
		boolean result = false;
		
		String[] fn = recepientsAll.split("\\;"); 
		String komu = "";
		
		
		
		if (gData.commonParams.contains("mailSending")) {
			
				if (gData.commonParams.get("mailSending").equals("false")) {
					String message = "";
					message += " Address=" + recepientsAll;
					message += " Subject=" + SubjectLetter;
					message += " Body=" + BodyLetter;
					gData.logger.info("E-mailing is disallowed: " + message);
				return;
				}
			}
		
		   try {
		        
			   ExchangeService MSservice = null;
			   MSservice = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

			   MSservice.setUrl(new URI(gData.commonParams.get("MsExchangeWebLink")));
		         

		       String password = gData.getPasswordFromHash(gData.commonParams.get("MsExchangeHash")); 
			   ExchangeCredentials credentials = new WebCredentials(gData.commonParams.get("MsExchangeUser"), password, gData.commonParams.get("MsExchangeDomain"));
		        
		         
		         MSservice.setCredentials(credentials);

		    	
		        EmailMessage message = new EmailMessage(MSservice);
		        message.setSubject(SubjectLetter);
		        message.setBody(new MessageBody(BodyLetter));
		        
//		        message.getAttachments().addFileAttachment(fileName);

				for(int i=0; i < fn.length; i++) {
					komu += fn[i] + "!";
					message.getToRecipients().add(fn[i]);
						gData.logger.info("fn[" + i+"]=" + fn[i]);
			
				}

		        message.send();

		        gData.logger.info("Letter was send: address= " + komu + " subject=" + SubjectLetter + " body=" + BodyLetter );
		        

		   } catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
		    }
		
	
		
		
	}
	public void sendOneLetter2 (List<String> recepients, String SubjectLetter, String BodyLetter ) {
		
		sendOneLetter2 (recepients, SubjectLetter, BodyLetter, new ArrayList<String>() );
	}
	
	public void sendOneLetter2 (List<String> recepients, String SubjectLetter, String BodyLetter, List<String> guidFiles ) {
		
		boolean result = false;
		
		String komu = "";
		
		
		
		if (gData.commonParams.contains("mailSending")) {
			
				if (gData.commonParams.get("mailSending").equals("false")) {
					String message = "";
					message += " To=";
					for (String r: recepients) {
						message += r + ";";
					}
					message += " Subject=" + SubjectLetter;
					message += " Body=" + BodyLetter;
					gData.logger.info("E-mailing is disallowed: " + message);
				return;
				}
			}
		
		   try {
		        
			   ExchangeService MSservice = null;
			   MSservice = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

			   MSservice.setUrl(new URI(gData.commonParams.get("MsExchangeWebLink")));
		         

		       String password = gData.getPasswordFromHash(gData.commonParams.get("MsExchangeHash")); 
			   ExchangeCredentials credentials = new WebCredentials(gData.commonParams.get("MsExchangeUser"), password, gData.commonParams.get("MsExchangeDomain"));
		        
		         
		         MSservice.setCredentials(credentials);

		    	
		        EmailMessage message = new EmailMessage(MSservice);
		        message.setSubject(SubjectLetter);
		        message.setBody(new MessageBody(BodyLetter));
		        

		        for(String re: recepients) {
		        	message.getToRecipients().add(re);
		        	
		        	gData.logger.info("send to: " + re);
		        	
		        }
		        
	
		        if (guidFiles.size() > 0) {
		        	for(String guidFile: guidFiles) {
		        		String fullPath = gData.mainPath + File.separator + "img" +
		        				File.separator + guidFile +".png";
		        		
		        		FileAttachment f1 = message.getAttachments().addFileAttachment(fullPath);
		        		f1.setContentType("image");
		        		f1.setIsInline(true);
		        		f1.setContentId(guidFile +".png");
		        	}
		        }
		        
		        
		        message.send();

		        gData.logger.info("Letter was send: address= " + komu + " subject=" + SubjectLetter + " body=" + BodyLetter );
		        

		   } catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
		    }
		
	
		
		
	}	
}
