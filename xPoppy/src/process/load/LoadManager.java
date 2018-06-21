package process.load;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import toolkits.poppy.PoppyMultipleVersion;
import toolkits.poppy.PoppySingleVersion;

import center.DateCenter;
import center.TagCenter;

public class LoadManager {
	
	DateCenter 	dc;
	
	String 		process_type;
	
	String 		object_name;
	
	int 		version;
	
	public LoadManager(DateCenter dc) {
		this.dc 			= dc;
		this.process_type 	= dc.getProcessType();
		this.object_name 	= dc.getObjectName();
		this.version 		= dc.getVersion();
	}
	
	public void load(String load_type) {
		if (TagCenter.PROCESS_TYPE.equals(load_type)) {
			if (TagCenter.PROCESS_GENERATE.equals(this.process_type)) {
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_SINGLE 	+ "\t" + TagCenter.START);
				load(TagCenter.FAULT_SINGLE);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_SINGLE 	+ "\t" + TagCenter.END);
				
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_MUTEX 	+ "\t" + TagCenter.START);
				load(TagCenter.FAULT_MUTEX);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_MUTEX 	+ "\t" + TagCenter.END);
			}
			else if (TagCenter.PROCESS_SEED.equals(this.process_type)) {
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_MULTIPLE + "\t" + TagCenter.START);
				load(TagCenter.FAULT_MULTIPLE);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_MULTIPLE + "\t" + TagCenter.END);
				
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_SEEDS 	+ "\t" + TagCenter.START);
				load(TagCenter.FAULT_SEEDS);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_SEEDS 	+ "\t" + TagCenter.END);
				
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.CONFIG_CPP 	+ "\t" + TagCenter.START);
				load(TagCenter.CONFIG_CPP);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.CONFIG_CPP 	+ "\t" + TagCenter.END);
				
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_CPP 		+ "\t" + TagCenter.START);
				load(TagCenter.FAULT_CPP);
				System.out.println(TagCenter.LOAD + "\t" + TagCenter.FAULT_CPP 		+ "\t" + TagCenter.END);
			}
		} 
		else {
			if (TagCenter.FAULT_SINGLE.equals(load_type)) {
				String loc_fault_single = this.dc.getLocation(TagCenter.FAULT_SINGLE);
				String file_name 		= this.object_name + "_" + this.version + TagCenter.DOT_TXT;
				
				List<PoppySingleVersion> psVersions = new Loader().loadFaultSingle(
						loc_fault_single, 
						file_name, 
						this.object_name, 
						this.version);
				this.dc.setPoppySingleVersions(psVersions);
			} 
			else if (TagCenter.FAULT_MULTIPLE.equals(load_type)) {
				String loc_fault_multiple 	= this.dc.getLocation(TagCenter.FAULT_MULTIPLE);
				for (int num_mul=this.dc.getMinNumOfMultiple(); num_mul<=this.dc.getMaxNumOfMultiple(); ++num_mul) {
					String file_name = this.object_name + "_" + this.version + "_" + num_mul + TagCenter.DOT_TXT;
					List<PoppyMultipleVersion> pmVersions = new Loader().loadFaultMultiple(
							loc_fault_multiple, 
							file_name, 
							this.object_name, 
							this.version);
					this.dc.putPoppyMultipleVersion(num_mul, pmVersions);
				}
			}
			else if (TagCenter.FAULT_MUTEX.equals(load_type)) {
				String loc_fault_single = this.dc.getLocation(TagCenter.FAULT_SINGLE);
				String file_name 		= this.object_name + "_" + this.version + "_" + TagCenter.MUTEX + TagCenter.DOT_XLSX;
				
				int[][] mutex = new Loader().loadMutex(
						loc_fault_single, 
						file_name, 
						this.dc.getSizeOfPoppySingleVersions());
				this.dc.setMutex(mutex);
			}
			else if (TagCenter.FAULT_SEEDS.equals(load_type)) {
				String loc_fault_seeds 	= this.dc.getLocation(TagCenter.FAULT_SEEDS);
				String file_name 		= TagCenter.FAULT_SEEDS + TagCenter.DOT_H;
				
				List<PoppySingleVersion> psVersions = new Loader().loadFaultSeeds(
						loc_fault_seeds, 
						file_name, 
						this.object_name, 
						this.version);
				this.dc.setPoppySingleVersions(psVersions);
				this.dc.updateMapIndexPSV();
				this.dc.updateMapCppPSVs();
				this.dc.updateMapPoppyMultipleVersions();
			}
			else if (TagCenter.CONFIG_CPP.equals(load_type)) {
				String loc_config_cpp 	= this.dc.getLocation(TagCenter.CONFIG_CPP);
				String file_name 		= TagCenter.CONFIG_CPP;
				
				Properties config_cpp 	= new Loader().loadConfig(loc_config_cpp, file_name);
				this.dc.setConfig(TagCenter.CONFIG_CPP, config_cpp);
			}
			else if (TagCenter.FAULT_CPP.equals(load_type)) {
				Properties config_cpp = this.dc.getConfig(TagCenter.CONFIG_CPP);
				Map<String, List<PoppySingleVersion>> map_cpp_psvs = this.dc.getMapCppPSVs();
				Set<String> cpps = map_cpp_psvs.keySet();
				for (String cpp_file : cpps) {
					List<PoppySingleVersion> cpp_psvs = map_cpp_psvs.get(cpp_file);
					String loc_fault_cpp = config_cpp.getProperty(cpp_file) + "\\" + TagCenter.PRE_VERSION + this.version;
					
					cpp_psvs = new Loader().loadFaultCpp(
							loc_fault_cpp, 
							cpp_file, 
							cpp_psvs);
				}
			}
		}
	}
}
