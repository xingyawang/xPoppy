package center;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocCenter {

	String 	loc_project;
	
	String 	object_name;
	
	int 	version;
	
	public LocCenter(
			String loc_project, 
			String object_name, 
			int version) {
		this.loc_project 	= loc_project;
		this.object_name 	= object_name;
		this.version 		= version;
	}
	
	public String getLocation(String loc_type) {
		
		if (TagCenter.INPUT.equals(loc_type)) {
			return this.loc_project + "\\" + TagCenter.INPUT;
		} 
		else if (TagCenter.OUTPUT.equals(loc_type)) {
			return this.loc_project + "\\" + TagCenter.OUTPUT;
		} 
		else if (TagCenter.FAULT_SINGLE.equals(loc_type)) {
			return getLocation(TagCenter.INPUT) + "\\" + TagCenter.FAULT_SINGLE;
		} 
		else if (TagCenter.FAULT_MULTIPLE.equals(loc_type)) {
			File file = new File(getLocation(TagCenter.OUTPUT) + "\\" + TagCenter.FAULT_MULTIPLE);
			if (file.exists()) {
				return getLocation(TagCenter.OUTPUT) + "\\" + TagCenter.FAULT_MULTIPLE;
			} else {
				return createFolder(TagCenter.FAULT_MULTIPLE);
			}
		} 
		else if (TagCenter.FAULT_SEEDS.equals(loc_type)) {
			return getLocation(TagCenter.INPUT) + "\\" + TagCenter.SOURCE + "\\" + this.object_name + "\\" + TagCenter.PRE_VERSION+this.version;
		}
		else if (TagCenter.CONFIG.equals(loc_type)) {
			return this.loc_project + "\\" + TagCenter.CONFIG;
		}
		else if (TagCenter.CONFIG_CPP.equals(loc_type)) {
			return this.loc_project + "\\" + TagCenter.CONFIG;
		}
		else if (TagCenter.FAULT_CPP.equals(loc_type)) {
			return getLocation(TagCenter.INPUT) + "\\" + TagCenter.SOURCE + "\\" + this.object_name + "\\" + TagCenter.PRE_VERSION+this.version;
		}
		
		return "";
	}
	
	public String createFolder(String create_type) {
		if (TagCenter.FAULT_MULTIPLE.equals(create_type)) {
			return createFolder(getLocation(TagCenter.OUTPUT), TagCenter.FAULT_MULTIPLE);
		}
		
		return "";
	}
	
	public String createFolder(String location, String folder_name) {
		new File(location + "/" + folder_name).mkdir();
		return location + "/" + folder_name;
	}
	
	public void copyFolderFiles(String path_old, String path_new) {
		File folder = new File(path_old);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				copyFile(file.getName(), path_old, path_new);
			}
			else if (file.isDirectory()) {
				createFolder(path_new, File.separator+file.getName());
				copyFolderFiles(path_old+File.separator+file.getName(), path_new+File.separator+file.getName());
			}
		}
	}
	
	public void copyFile(String file_name, String path_old, String path_new) {
		File file = new File(path_old + "\\" + file_name);
		if (file.exists()) {
			try {
				InputStream inStream 	= new FileInputStream(path_old 	+ "\\" + file.getName());
				OutputStream outStream 	= new FileOutputStream(path_new + "\\" + file.getName());
				
				byte[] buffer = new byte[(int) file.length()];
				int byte_read = 0;
				while ((byte_read=inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, byte_read);
				}
				
				inStream.close();
				outStream.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	public static void main(String[] args) {
//		
//		LocCenter lc = new LocCenter(null, null, 0);
//		lc.copyFolderFiles(
//				"E:\\projects\\eclipse\\xPoppy\\input\\source\\jtcas_1", 
//				"E:\\projects\\eclipse\\xPoppy\\output\\source\\jtcas_1_2\\1");
//		
//		
//	}
	
	
	
	
	
	
	
	
	
	
}
