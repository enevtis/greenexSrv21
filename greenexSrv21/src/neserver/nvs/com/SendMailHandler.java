package neserver.nvs.com;

import java.util.List;
import greenexSrv2.nvs.com.MSEcxchange;
import greenexSrv2.nvs.com.globalData;
import obj.greenexSrv2.nvs.com.TblField;

public class SendMailHandler extends HandlerTemplate {

	private List<TblField> fields = null;
	private String screenName = "monitor_conn_data";
	private String tableName = screenName;
	private String SQL = "";
	public String caption = "";


	public SendMailHandler(globalData gData) {
		super(gData);

	}

	@Override
	public String getPage() {

		String out = "";
		
		out += getBeginPage();
		out += strTopPanel("Send mail");
		
		out += "MAIL";
		
		sendLetter();
		
		out += getEndPage();

		return out;
	}
	public void sendLetter() {
		
		MSEcxchange me = new MSEcxchange(gData);
		String recepientsAll ="enevtis@gmail.com";
		String SubjectLetter = "Test letter";
		String BodyLetter = "<h3>Это тест</h3><hr> Здесь тестовый текст";
		
		me.sendOneLetter(recepientsAll, SubjectLetter, BodyLetter);
		
		
	}
}
