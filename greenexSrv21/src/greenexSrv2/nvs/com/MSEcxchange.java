package greenexSrv2.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
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
	
}
