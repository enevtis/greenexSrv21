package graph.greenexSrv2.com;

import java.time.LocalDateTime;
import java.util.Date;

public class GraphTimeValue {
	public LocalDateTime dateTime;
	public float tValue = 0f;
	public String label = "";

	public GraphTimeValue(LocalDateTime dateTime, float tValue) {
		this.dateTime = dateTime;
		this.tValue = tValue;		
	}
	public GraphTimeValue(LocalDateTime dateTime, float tValue, String label) {
		this.dateTime = dateTime;
		this.tValue = tValue;
		this.label = label;
	}
	public GraphTimeValue() {
	}
}
