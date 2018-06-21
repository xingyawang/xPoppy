package toolkits.poppy;

public class PoppySingleVersion {

	String 		object_name;
	
	int 		version;
	
	int 		fault;
	
	FaultData 	fault_data;
	
	public PoppySingleVersion(
			String 	object_name, 
			int 	version) {
		this.object_name 	= object_name;
		this.version 		= version;
	}
	
	public PoppySingleVersion(
			String 	object_name, 
			int 	version, 
			int 	fault) {
		this.object_name 	= object_name;
		this.version 		= version;
		this.fault 			= fault;
	}
	
	public PoppySingleVersion(
			String 		object_name, 
			int 		version, 
			int 		fault, 
			FaultData 	fault_data) {
		this.object_name 	= object_name;
		this.version 		= version;
		this.fault 			= fault;
		this.fault_data 	= fault_data;
	}
	
	public String getObjectName() {
		return this.object_name;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public int getFault() {
		return this.fault;
	}
	
	public FaultData getFaultData() {
		return this.fault_data;
	}
	
	public String toDump(boolean b_head) {
		StringBuilder sb = new StringBuilder();
		if (true == b_head) {
			sb.append(this.object_name + "\t" + this.version + "\t" + "\t");
		}
		sb.append(this.fault);
		return sb.toString();
	}
	
	
	
	
	
}
