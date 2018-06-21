package center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import toolkits.poppy.FaultData;
import toolkits.poppy.PoppyMultipleVersion;
import toolkits.poppy.PoppySingleVersion;

public class DateCenter {

	String 		loc_project;
	
	String 		process_type;
	
	String 		object_name;
	
	int 		version;
	
	int 		num_mul_min;
	
	int 		num_mul_max;
	
	LocCenter 	locCenter;
	
	int[][] 	mutex;
	
	Properties 	config;
	
	Properties 	config_cpp;
	
	List<PoppySingleVersion> 					psVersions;
	
	Map<Integer, PoppySingleVersion> 			map_index_psv;
	
	Map<String, List<PoppySingleVersion>> 		map_cpp_psvs;

	Map<Integer, List<PoppyMultipleVersion>> 	map_num_mul_pmvs;
	
	public DateCenter(
			String 	loc_project,
			String 	process_type, 
			String 	object_name, 
			int 	version,
			int 	num_mul_min, 
			int 	num_mul_max) {
		this.loc_project 	= loc_project;
		this.process_type 	= process_type;
		this.object_name 	= object_name;
		this.version 		= version;
		this.num_mul_min 	= num_mul_min;
		this.num_mul_max 	= num_mul_max;
		
		this.locCenter 			= new LocCenter(loc_project, object_name, version);
		this.psVersions 		= new ArrayList<PoppySingleVersion>();
		this.map_index_psv 		= new HashMap<Integer, PoppySingleVersion>();
		this.map_cpp_psvs 		= new HashMap<String, List<PoppySingleVersion>>();
		this.map_num_mul_pmvs 	= new HashMap<Integer, List<PoppyMultipleVersion>>();
	}
	
	public String getProcessType() {
		return this.process_type;
	}
	
	public String getObjectName() {
		return this.object_name;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public int getMinNumOfMultiple() {
		return this.num_mul_min;
	}
	
	public int getMaxNumOfMultiple() {
		return this.num_mul_max;
	}
	
	public void setMutex(int[][] mutex) {
		this.mutex = mutex;
	}
	
	public int[][] getMutex() {
		return this.mutex;
	}
	
	public void setConfig(String config_type, Properties config) {
		if (TagCenter.CONFIG.equals(config_type)) {
			this.config = config;
		} 
		else if (TagCenter.CONFIG_CPP.equals(config_type)) {
			this.config_cpp = config;
		}
	}
	
	public Properties getConfig(String config_type) {
		if (TagCenter.CONFIG.equals(config_type)) {
			return this.config;
		} 
		else if (TagCenter.CONFIG_CPP.equals(config_type)) {
			return this.config_cpp;
		}
		return null;
	}
	
	public String getLocation(String loc_type) {
		return this.locCenter.getLocation(loc_type);
	}
	
	public String createFolder(String location, String folder_name) {
		return this.locCenter.createFolder(location, folder_name);
	}
	
	public void copyFile(String file_name, String path_old, String path_new) {
		this.locCenter.copyFile(file_name, path_old, path_new);
	}
	
	public void copyFolderFiles(String path_old, String path_new) {
		this.locCenter.copyFolderFiles(path_old, path_new);
	}
	
	public void setPoppySingleVersions(List<PoppySingleVersion> psVersions) {
		this.psVersions = psVersions;
	}
	
	public List<PoppySingleVersion> getPoppySingleVersions() {
		return this.psVersions;
	}
	
	public void updateMapIndexPSV() {
		for (PoppySingleVersion psVersion : this.psVersions) {
			this.map_index_psv.put(psVersion.getFault(), psVersion);
		}
	}
	
	public Map<Integer, PoppySingleVersion> getMapIndexPSV() {
		return this.map_index_psv;
	}
	
	public void setActiveOfPSVs(boolean is_active) {
		Set<Integer> faults = this.map_index_psv.keySet();
		for (Integer fault : faults) {
			PoppySingleVersion psv = this.map_index_psv.get(fault);
			psv.getFaultData().setActive(is_active);
		}
	}
	
	public void updateMapCppPSVs() {
		for (PoppySingleVersion psVersion : this.psVersions) {
			String cpp_name = psVersion.getFaultData().getCppFile();
			List<PoppySingleVersion> psvs = this.map_cpp_psvs.get(cpp_name);
			if (null == psvs) {
				psvs = new ArrayList<PoppySingleVersion>();
			} 
			psvs.add(psVersion);
			this.map_cpp_psvs.put(cpp_name, psvs);
		}
	}
	
	public Map<String, List<PoppySingleVersion>> getMapCppPSVs() {
		return this.map_cpp_psvs;
	}
	
	public int getSizeOfPoppySingleVersions() {
		return this.psVersions.size();
	}
	
	public void updateMapPoppyMultipleVersions() {
		Set<Integer> num_muls = map_num_mul_pmvs.keySet();
		for (Integer num_mul : num_muls) {
			List<PoppyMultipleVersion> pmvs = map_num_mul_pmvs.get(num_mul);
			for (PoppyMultipleVersion pmv : pmvs) {
				List<Integer> faults = pmv.getFaults();
				for (Integer fault : faults) {
					FaultData fault_data = this.map_index_psv.get(fault).getFaultData();
					pmv.addFaultData(fault_data.getCppFile(), fault_data);
				}
			}
		}
	}
	
	public void putPoppyMultipleVersion(int num_mul, List<PoppyMultipleVersion> pmVersions) {
		this.map_num_mul_pmvs.put(num_mul, pmVersions);
	}
	
	public Map<Integer, List<PoppyMultipleVersion>> getMapPoppyMultipleVersions() {
		return this.map_num_mul_pmvs;
	}
	
	public int getSizeOfPoppyMultipleVersions(int num_mul) {
		List<PoppyMultipleVersion> pmVersions = this.map_num_mul_pmvs.get(num_mul);
		if (null == pmVersions) {
			return 0;
		} else {
			return pmVersions.size();
		}
	}
	
}
