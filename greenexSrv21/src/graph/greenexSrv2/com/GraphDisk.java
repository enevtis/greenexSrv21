package graph.greenexSrv2.com;

public class GraphDisk {
	public String path = "";
	public float maxSizeGb;
	public float usedSizeGb;
	public float minUsedSizeGb = 0;

	public GraphDisk(String path, float usedSizeGb, float maxSizeGb ) {
		this.path = path;
		this.maxSizeGb = maxSizeGb;
		this.usedSizeGb = usedSizeGb;
	}

	public GraphDisk(String path, float usedSizeGb, float maxSizeGb, float minUsedSizeGb) {
		this.path = path;
		this.maxSizeGb = maxSizeGb;
		this.usedSizeGb = usedSizeGb;
		this.minUsedSizeGb = minUsedSizeGb;
	}

	public GraphDisk() {
		// TODO Auto-generated constructor stub
	}

}
