package obj.greenexSrv2.nvs.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqlReturn {
	public List<Map<String , String>> records = new ArrayList<Map<String , String>>();
	public List<TblField> fields = new ArrayList<TblField>();
	public boolean isOk = true;
	public String message = "";

}
