package process.dump;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import toolkits.poppy.FaultData;
import toolkits.poppy.PoppyMultipleVersion;

import center.DateCenter;
import center.TagCenter;

public class DumpManager {

	DateCenter 	dc;
	
	String 		process_type; 
	
	String 		object_name;
	
	public DumpManager(DateCenter dc) {
		this.dc 			= dc; 
		this.process_type 	= dc.getProcessType();
		this.object_name 	= dc.getObjectName();
	}
	
	public void dump(String dump_type) {
		if (TagCenter.PROCESS_TYPE.equals(dump_type)) {
			if (TagCenter.PROCESS_GENERATE.equals(this.process_type)) {
				System.out.println(TagCenter.DUMP + "\t" + TagCenter.FAULT_MULTIPLE + "\t" + TagCenter.START);
				dump(TagCenter.POPPY_MULTIPLE_VERSION, dc.getMapPoppyMultipleVersions());
				System.out.println(TagCenter.DUMP + "\t" + TagCenter.FAULT_MULTIPLE + "\t" + TagCenter.END);
			}
			else if (TagCenter.PROCESS_SEED.equals(this.process_type)) {
				System.out.println(TagCenter.DUMP + "\t" + TagCenter.FAULT_SOURCE 	+ "\t" + TagCenter.START);
				dump(TagCenter.FAULT_SOURCE, dc.getMapPoppyMultipleVersions());
				System.out.println(TagCenter.DUMP + "\t" + TagCenter.FAULT_SOURCE 	+ "\t" + TagCenter.END);
			}
		}
	}
	
	public void dump(String dump_type, Object object) {
		if (TagCenter.POPPY_MULTIPLE_VERSION.equals(dump_type)) {
			if (object instanceof Map<?, ?>) {
				Map<?, ?> map 	= (Map<?, ?>) object;
				Set<?> keys 	= map.keySet();
				for (Object key : keys) {
					dump(TagCenter.POPPY_MULTIPLE_VERSION, key, map.get(key));
				}
			}
		}
		else if (TagCenter.FAULT_SOURCE.equals(dump_type)) {
			if (object instanceof Map<?, ?>) {
				Map<?, ?> map 	= (Map<?, ?>) object;
				Set<?> keys 	= map.keySet();
				for (Object key : keys) {
					dump(TagCenter.FAULT_SOURCE, key, map.get(key));
				}
			}
		}
	}
	
	public void dump(String dump_type, Object obj1, Object obj2) {
		if (obj1 instanceof Integer && obj2 instanceof List<?>) {
			if (TagCenter.POPPY_MULTIPLE_VERSION.equals(dump_type)) {
				int num_mul 		= (Integer) obj1;
				List<?> pmvs 		= (List<?>) obj2;
				String object_name 	= this.dc.getObjectName();
				int version 		= this.dc.getVersion();
				
				String location 	= this.dc.getLocation(TagCenter.FAULT_MULTIPLE);
				String file_name 	= object_name + "_" + version + "_" + num_mul + TagCenter.DOT_TXT;
				StringBuilder sb 	= new StringBuilder();
				for (Object object : pmvs) {
					if (object instanceof PoppyMultipleVersion) {
						PoppyMultipleVersion pmv = (PoppyMultipleVersion) object;
						sb.append(pmv.toDump(false) + "\r\n");
					}
				}
				
				new Dumper(location, file_name, sb.toString()).dump();
			}
			else if (TagCenter.FAULT_SOURCE.equals(dump_type)) {
				int num_mul 		= (Integer) obj1;
				List<?> pmvs 		= (List<?>) obj2;
				String object_name 	= this.dc.getObjectName();
				int version 		= this.dc.getVersion();
				System.out.println(TagCenter.DUMP + "\t" + TagCenter.FAULT_SOURCE + "\t" + num_mul);
				
				String loc_input 		= this.dc.getLocation(TagCenter.INPUT);
				String loc_ip_source 	= this.dc.createFolder(loc_input, 		TagCenter.SOURCE);
				String loc_ip_object 	= this.dc.createFolder(loc_ip_source, 	object_name);
				String loc_ip_version 	= this.dc.createFolder(loc_ip_object, 	TagCenter.PRE_VERSION+version);
				
				String loc_output 		= this.dc.getLocation(TagCenter.OUTPUT);
				String loc_op_source_alt= this.dc.createFolder(loc_output, 			TagCenter.SOURCE_ALT);
				String loc_op_object 	= this.dc.createFolder(loc_op_source_alt, 	object_name);
				String loc_op_version 	= this.dc.createFolder(loc_op_object, 		TagCenter.PRE_VERSION+version+"_"+num_mul);
				
				int folder_name=0;
				for (Object object : pmvs) {
					if (object instanceof PoppyMultipleVersion) {
						PoppyMultipleVersion pmv = (PoppyMultipleVersion) object;
						
						// create folder and copy files
						String loc_op_fault = "";
						{
							folder_name++;
							loc_op_fault = this.dc.createFolder(loc_op_version, TagCenter.PRE_FAULT+folder_name);
							this.dc.copyFolderFiles(loc_ip_version, loc_op_fault);
						}
						
						// seed faults
						{
							this.dc.setActiveOfPSVs(false);
							for (String cpp_file : pmv.getFaultDatas().keySet()) {
								for (FaultData fault_data : pmv.getFaultDatas().get(cpp_file)) {
									fault_data.setActive(true);
								}
							}
							
							for (String cpp_file : pmv.getFaultDatas().keySet()) {
//								System.out.println(cpp_file);
								
								new Dumper().writeEqualizeLine(
										loc_op_fault, 
										cpp_file, 
										pmv.getFaultDatas().get(cpp_file));
								
								deleteFile(
										loc_op_fault, 
										cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
								
								renameFile(
										loc_op_fault, 
										TagCenter.STR_TEMP + TagCenter.DOT_JAVA, 
										cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
							}
						}
					}
				}
			}
		}
	}
	
	public void deleteFile(String location, String file_name) {
		File file = new File(location + "/" + file_name);
		if (file.exists()) {
			file.delete();
		}
	}
	
	public void renameFile(String location, String file_name, String new_name) {
		File file 		= new File(location + "/" + file_name);
		File file_new 	= new File(location + "/" + new_name);
		if (file.exists()) {
			file.renameTo(file_new);
		}
	}
	
}
