package process.load;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import center.TagCenter;

import toolkits.poppy.Block;
import toolkits.poppy.FaultData;
import toolkits.poppy.PoppyMultipleVersion;
import toolkits.poppy.PoppySingleVersion;

public class Loader {

	public File loadFile(
			String location, 
			String file_name) {
		File file = new File(location + "/" + file_name);
		return file;
	}
	
	public List<PoppySingleVersion> loadFaultSingle(
			String 	location, 
			String 	file_name, 
			String 	object_name,
			int 	version) {
		List<PoppySingleVersion> psVersions = new ArrayList<PoppySingleVersion>();
		
		try {
			File file_fault_single 	= loadFile(location, file_name);
			BufferedReader reader 	= new BufferedReader(new FileReader(file_fault_single));
			
			String line_fault_single;
			while ((line_fault_single = reader.readLine()) != null) {
				String[] tags = line_fault_single.split("\t");
				if (1 == tags.length) {
					int fault = Integer.parseInt(tags[0]);
					
					PoppySingleVersion psVersion = new PoppySingleVersion(object_name, version, fault);
					psVersions.add(psVersion);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return psVersions;
	}
	
	@SuppressWarnings("resource")
	public List<PoppyMultipleVersion> loadFaultMultiple(
			String 	location, 
			String 	file_name, 
			String 	object_name,
			int 	version) {
		List<PoppyMultipleVersion> pmVersions = new ArrayList<PoppyMultipleVersion>();
		
		try {
			File file_fault_mul 	= loadFile(location, file_name);
			BufferedReader reader 	= new BufferedReader(new FileReader(file_fault_mul));
			
			String line_fault_mul;
			while ((line_fault_mul = reader.readLine()) != null) {
				String[] tags = line_fault_mul.split("\t");
				if (tags.length <= 0) continue;
				
				PoppyMultipleVersion pmVersion = new PoppyMultipleVersion(object_name, version);
				for (int i_fault=0; i_fault<tags.length; ++i_fault) {
					pmVersion.addFault(Integer.parseInt(tags[i_fault]));
				}
				pmVersions.add(pmVersion);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pmVersions;
	}
	
	public int[][] loadMutex(
			String 	location, 
			String 	file_name, 
			int		num_psv) {
		int[][] mutex = new int[num_psv+1][num_psv+1];
		
		BufferedInputStream in 	= null;
		XSSFWorkbook workbook	= null;
		try {
			in 			= new BufferedInputStream(new FileInputStream(location + "/" + file_name));
			workbook 	= new XSSFWorkbook(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XSSFSheet sheet = workbook.getSheetAt(0);
		for (int i=0; i<=num_psv; ++i) {
			XSSFRow row = sheet.getRow(i);
			
			for (int j=0; j<=num_psv; ++j) {
				XSSFCell cell 	= row.getCell(j);
				String str 		= cell.getRawValue();
				if (str == null) {
					mutex[i][j] = 0;
				} else {
					mutex[i][j] = Integer.parseInt(str);
				}
			}
		}
		
		return mutex;
	}
	
	@SuppressWarnings("resource")
	public List<PoppySingleVersion> loadFaultSeeds(
			String 	location, 
			String 	file_name, 
			String 	object_name,
			int 	version) {
		List<PoppySingleVersion> psVersions = new ArrayList<PoppySingleVersion>();
		
		try {
			File file_fault_seeds = loadFile(location, file_name);
			BufferedReader reader = new BufferedReader(new FileReader(file_fault_seeds));
			
			String str_fault 	= null;
			while ((str_fault 	= reader.readLine()) != null) {
				String[] tags 	= str_fault.split(" ");
				FaultData fd 	= new FaultData(Integer.parseInt(tags[1]), tags[0], tags[2]);
				PoppySingleVersion psVersion = new PoppySingleVersion(object_name, version, Integer.parseInt(tags[1]), fd);
				psVersions.add(psVersion);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return psVersions;
	}
	
	public Properties loadConfig(
			String location, 
			String file_name) {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(location + "/" + file_name));
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
		return config;
	}
	
	public List<PoppySingleVersion> loadFaultCpp(
			String 	location, 
			String 	file_name, 
			List<PoppySingleVersion> psVersions) {
		
		try {
			File file_cpp = loadFile(location, file_name);
			BufferedReader reader = new BufferedReader(new FileReader(file_cpp));
			
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
	            		cur_fd = getFaultData(stok.sval, psVersions);
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
//	                	cur_fd = getFaultData(stok.sval, fds);
	                	cur_fd = getFaultData(stok.sval, psVersions);
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
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return psVersions;
	}
	
	/*public FaultData getFaultData(String fault_name, List<FaultData> fds) {
		for (FaultData fd : fds) {
			if (fd.getFaultName().equals(fault_name))
				return fd;
		}
		return null;
	}*/
	
	public FaultData getFaultData(String fault_name, List<PoppySingleVersion> psvs) {
		for (PoppySingleVersion psv : psvs) {
			if (psv.getFaultData().getFaultName().equals(fault_name)) {
				return psv.getFaultData();
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
