package process.dump;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import toolkits.poppy.FaultData;
import center.TagCenter;

public class Dumper {

	String location; 
	
	String file_name; 
	
	Object object;
	
	public Dumper() {
		
	}
	
	public Dumper(
			String location, 
			String file_name, 
			Object object) {
		this.location 	= location;
		this.file_name 	= file_name;
		this.object 	= object;
	}
	
	public void dump() {
		if (this.object instanceof String) {
			dumpFile((String) this.object);
		}
	}
	
	public void dumpFile(String str) {
		File file = new File(this.location + "/" + this.file_name);
		FileWriter fileWriter = null;

		try {
			file.createNewFile();
			fileWriter = new FileWriter(file);
			fileWriter.write(str);
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public File loadFile(String location, String file_name) {
		File file = new File(location, file_name);
		return file;
	}
	
	public void writeEqualizeLine(String location, String cpp_file, List<FaultData> fds) {
		File file_cpp 	= loadFile(location, cpp_file);
		File file_java 	= loadFile(location, cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
		File file_temp 	= loadFile(location, TagCenter.STR_TEMP + TagCenter.DOT_JAVA);
		
		try {
			BufferedReader reader_cpp = new BufferedReader(new FileReader(file_cpp));
			BufferedReader reader_java 	= new BufferedReader(new FileReader(file_java));
			BufferedWriter writer_temp 	= new BufferedWriter(new FileWriter(file_temp));
			
			int line_java 	= 0;
			int line_cpp 	= 0;
			String str_line_java = null;
			while ((str_line_java = reader_java.readLine()) != null) {
				line_java++;
				
				boolean b_fd = false;
				for (FaultData fd : fds) {
					if (fd.isActive() == false) continue;
					
					if (fd.getFaultBlock().getStartLineJava() == line_java) {
						int start_line_cpp 	= fd.getFaultBlock().getStartLine();
						int length_cpp 		= fd.getFaultBlock().getBlockLength();
						
						String str_line_cpp = null;
						while((str_line_cpp = reader_cpp.readLine()) != null) {
							line_cpp++;
							
							if (line_cpp == start_line_cpp) {
								writer_temp.write(str_line_cpp);
								writer_temp.newLine();
								
								for (int i=1; i<length_cpp; i++) {
									str_line_cpp 	= reader_cpp.readLine();
									str_line_java	= reader_java.readLine();
									
									line_cpp++;
									line_java++;
									
									writer_temp.write(str_line_cpp);
									writer_temp.newLine();
								}
								break;
							}
						}
						b_fd = true;
					}
				}
				
				if (false == b_fd) {
					writer_temp.write(str_line_java);
					writer_temp.newLine();
				}
			}
			
			reader_cpp.close();
			reader_java.close();
			writer_temp.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public int lineNumOfFile(File file) {
		int num_line = 0;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while (reader.readLine() != null) {
				num_line++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num_line;
	}
}
