package siena;

import java.io.*;

public class Dejavu{ 

	public void write() throws IOException{
    	   File outputFile = new File("out.txt");
    	   FileWriter out = new FileWriter(outputFile);
	   out.write("MODIFICATION TRAVERSING\n");
	   out.close();
	}

	public void writeInit() throws IOException{
    	   File outputFile = new File("out.txt");
    	   FileWriter out = new FileWriter(outputFile);
	   out.write("NOT MOD-TRAVERSING\n");
	   out.close();
	}
}
	
