package process.seed;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import toolkits.poppy.Block;
import toolkits.poppy.FaultData;

import center.DateCenter;
import center.TagCenter;

public class SeederOrig {
	
	DateCenter dc;
	
	String 	path_source;
	
	String 	path_fault_seeds;
	
	String 	path_fault_mul;
	
	int 	fault_version;
	
	Map<Integer, FaultData> 		map_index_fds;
	
	Map<String, List<FaultData>> 	map_cpp_fds;
	
	public SeederOrig(DateCenter dc) {
		this.dc = dc;
	}
	
/*	public Seeder(String[] args) {
		this.path_source 		= args[0];
		this.path_fault_seeds 	= args[1];
		this.path_fault_mul 	= args[2];
		this.fault_version 		= Integer.parseInt(args[3]);
		this.map_index_fds 		= new HashMap<Integer, FaultData>();
		this.map_cpp_fds 		= new HashMap<String, List<FaultData>>();
	}*/
    
    public void seed() {
    	try {
			generateFaultDatas();
			seedFaults();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@SuppressWarnings("resource")
	public void generateFaultDatas() throws IOException {
		// load file of FaultSeeds.h
		{
			File file_fault_seeds = loadFile(this.path_fault_seeds);
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file_fault_seeds));
			String str_fault 	= null;;
			while ((str_fault 	= reader.readLine()) != null) {
				String[] tags 	= str_fault.split(" ");
				FaultData fd 	= new FaultData(Integer.parseInt(tags[1]), tags[0], tags[2]);
				List<FaultData> fds = map_cpp_fds.get(tags[2]);
				if (fds == null) {
					fds = new ArrayList<FaultData>();
				}
				fds.add(fd);
				
				this.map_index_fds.put(Integer.parseInt(tags[1]), fd);
				this.map_cpp_fds.put(tags[2], fds);
			}
			
		}
		
		// load file of fault multiple
		{
			File file_fault_mul 	= loadFile(this.path_fault_mul);
			BufferedReader reader 	= null;
			reader = new BufferedReader(new FileReader(file_fault_mul));
			String[] fault_indexs 	= getLine(reader, this.fault_version).split("\t");
			for (String fault_index : fault_indexs) {
				FaultData fd = map_index_fds.get(Integer.parseInt(fault_index));
				fd.setActive(true);
			}
			
		}
		
		// analyze cpp files
		{
			Set<String> cpp_files = this.map_cpp_fds.keySet();
			for (String cpp_file : cpp_files) {
				List<FaultData> fds = this.map_cpp_fds.get(cpp_file);
				analyzeCppFile(cpp_file, fds);
			}
		}
	}
	
	public void analyzeCppFile(String cpp_file, List<FaultData> fds) throws IOException {
		File file_cpp = loadFile(this.path_source, cpp_file);
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file_cpp));
		
		// Set up the tokenizer
        StreamTokenizer stok = new StreamTokenizer(reader);
        stok.resetSyntax();
        stok.eolIsSignificant(true);
        stok.wordChars(0x0000, 0xFFFF);
        stok.whitespaceChars(0x00, 0x20);
        
        int shift 		= 0;
        int line_ifdef 	= 0;
        int line_else 	= 0;
        int line_endif 	= 0;
        
        int tokenType;
        List<Block> 	list_block 	= null;
        FaultData 		cur_fd 		= null;
        List<FaultData> list_fd		= null;
        while ((tokenType = stok.nextToken()) != StreamTokenizer.TT_EOF) {
            if (tokenType == StreamTokenizer.TT_EOL) {
               continue;
            }
            
            if (stok.sval.equals(TagCenter._IFDEF)) {
            	line_ifdef = stok.lineno();
            	
            	stok.nextToken();
            	
            	// cur
            	{
                	cur_fd = getFaultData(stok.sval, fds);
                	Block fault_block = new Block(stok.lineno()+1, line_ifdef-shift, 1, TagCenter.IFDEF, null);
                	cur_fd.setFaultBlock(fault_block);
                	list_block = new ArrayList<Block>();
                	list_block.add(fault_block);
            	}
            	
            	list_fd = new ArrayList<FaultData>();
            	list_fd.add(cur_fd);
            	
            } else if (stok.sval.equals(TagCenter._ELIF)) {
            	stok.nextToken();
            	
            	// last
            	{
                	Block last_fault_block = list_block.get(list_block.size()-1);
                	last_fault_block.setLength(stok.lineno()-last_fault_block.getStartLine());
                	last_fault_block.setTagSucc(TagCenter.ELIF);
            	}
            	
            	// cur
            	{
                	cur_fd = getFaultData(stok.sval, fds);
                	Block fault_block = new Block(stok.lineno()+1, line_ifdef-shift, 1, TagCenter.ELIF, null);
                	cur_fd.setFaultBlock(fault_block);
                	list_block.add(fault_block);
            	}
            	
            	list_fd.add(cur_fd);

            } else if (stok.sval.equals(TagCenter._ELSE)) {
            	line_else = stok.lineno();
            	
            	// last
            	{
            		Block last_fault_block = list_block.get(list_block.size()-1);
            		last_fault_block.setLength(stok.lineno()-last_fault_block.getStartLine());
            		last_fault_block.setTagSucc(TagCenter.ELSE);
            	}
            	
            	// cur
            	{
            		Block right_block = new Block(stok.lineno()+1, line_ifdef-shift, 1, TagCenter.ELSE, null);
            		list_block.add(right_block);
            		
                	for (FaultData fd : list_fd) {
    					fd.setRightBlock(right_block);
    				}
            	}
            	
            } else if (stok.sval.equals(TagCenter._ENDIF)) {
            	line_endif = stok.lineno();
            	
            	// last
            	{
            		Block last_right_block = list_block.get(list_block.size()-1);
            		last_right_block.setLength(stok.lineno()-last_right_block.getStartLine());
            		last_right_block.setTagSucc(TagCenter.ENDIF);
            	}
            	
            	shift += (line_endif - line_ifdef + 1) - (line_endif - line_else - 1);
            }
        }
	}
	
	public void seedFaults() throws IOException {
		Set<String> cpp_files = map_cpp_fds.keySet();
		for (String cpp_file : cpp_files) {
			List<FaultData> fds = map_cpp_fds.get(cpp_file);
			writeEqualizeLine(cpp_file, fds);
			
			deleteFile(this.path_source, cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
			renameFile(this.path_source, TagCenter.STR_TEMP + TagCenter.DOT_JAVA, cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
		}
	}
	
	public void writeEqualizeLine(String cpp_file, List<FaultData> fds) throws IOException {
		File file_cpp 	= loadFile(this.path_source, cpp_file);
		File file_java 	= loadFile(this.path_source, cpp_file.split(TagCenter.DOT_CPP)[0]+TagCenter.DOT_JAVA);
		File file_temp 	= loadFile(this.path_source, TagCenter.STR_TEMP + TagCenter.DOT_JAVA);
		
		BufferedReader reader_cpp 	= new BufferedReader(new FileReader(file_cpp));
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
	}
	
	public File loadFile(String path) {
		File file = new File(path);
		return file;
	}
	
	public File loadFile(String location, String file_name) throws IOException {
		File file = new File(location + "/" + file_name);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	public void deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
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
	
	public String getLine(BufferedReader reader, int line_num) throws IOException {
		String line = "";
		for (; line_num>0; line_num--) {
			line = reader.readLine();
		}
		return line;
	}
	
	public FaultData getFaultData(String fault_name, List<FaultData> fds) {
		for (FaultData fd : fds) {
			if (fd.getFaultName().equals(fault_name))
				return fd;
		}
		return null;
	}
	
	public void print(String type, Object object) {
		if (TagCenter.FAULT_SEEDS.equals(type)) {
			Set<String> cpps = this.map_cpp_fds.keySet();
			StringBuilder sb = new StringBuilder();
			for (String cpp : cpps) {
				sb.append(cpp + "\n");
				for (FaultData fd : this.map_cpp_fds.get(cpp)) {
					sb.append(fd.toDump() + "\n");
				}
			}
			System.out.println(sb.toString());
		}
	}
	
	
/*	public static void main(String[] args) throws IOException {
		
		String[] arguments = {	
				"E:\\projects\\eclipse\\xPoppy\\input\\source\\test", 
				"E:\\projects\\eclipse\\xPoppy\\input\\source\\test\\FaultSeeds.h",
				"E:\\projects\\eclipse\\xPoppy\\input\\source\\jtcas_mul.txt", 
				"1"};
		
		Seeder seeder = new Seeder(arguments);
		seeder.generateFaultDatas();
		seeder.seedFaults();
		
	}*/
	
}
