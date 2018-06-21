package toolkits.poppy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoppyMultipleVersion {
	
	String 			object_name;
	
	int 			version;
	
	List<Integer> 	faults;
	
//	List<FaultData> fault_datas;
	
	Map<String, List<FaultData>> map_cpp_fds;
	
	public PoppyMultipleVersion(
			String 	object_name, 
			int 	version) {
		this.object_name 	= object_name;
		this.version 		= version;
		this.faults 		= new ArrayList<Integer>();
		this.map_cpp_fds 	= new HashMap<String, List<FaultData>>();
	}
	
	public PoppyMultipleVersion(
			String 			object_name, 
			int 			version, 
			List<Integer> 	faults) {
		this.object_name 	= object_name;
		this.version 		= version;
		this.faults 		= faults;
		this.map_cpp_fds 	= new HashMap<String, List<FaultData>>();
	}
	
	public String getObjectName() {
		return this.object_name;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public void addFault(int fault) {
		this.faults.add(fault);
	}
	
	public List<Integer> getFaults() {
		return this.faults;
	}
	
	public int getSizeOfFaults() {
		return this.faults.size();
	}
	
//	public void addFaultData(FaultData fault_data) {
//		this.fault_datas.add(fault_data);
//	}
	
//	public List<FaultData> getFaultDatas() {
//		return this.fault_datas;
//	}
	
	public void addFaultData(String cpp_file, FaultData fault_data) {
		if (this.map_cpp_fds.containsKey(cpp_file)) {
			List<FaultData> fds = this.map_cpp_fds.get(cpp_file);
			fds.add(fault_data);
		} else {
			List<FaultData> fds = new ArrayList<FaultData>();
			fds.add(fault_data);
			this.map_cpp_fds.put(cpp_file, fds);
		}
	}
	
	public Map<String, List<FaultData>> getFaultDatas() {
		return this.map_cpp_fds;
	}
	
	public String toDump(boolean b_head) {
		StringBuilder sb = new StringBuilder();
		
		if (true == b_head) {
			sb.append(this.object_name + "\t" + this.version + "\t");
		}
		for (int fault : this.faults) {
			sb.append(fault + "\t");
		}
		
		return sb.toString();
	}
}
