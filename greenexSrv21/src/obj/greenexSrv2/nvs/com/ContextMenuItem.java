package obj.greenexSrv2.nvs.com;

public class ContextMenuItem {
	public String id;
	public String caption = "";
	public String onFunctionCode = "";
	
	public ContextMenuItem(String id, String caption, String onFunctionCode ) {
		this.id = id;
		this.caption = caption;
		this.onFunctionCode = onFunctionCode;
	}
}
