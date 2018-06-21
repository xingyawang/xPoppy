package toolkits.poppy;

public class Block {

	int 	start_line;
	
	int 	start_line_java;
	
	int 	length;
	
	String 	tag_pred;
	
	String 	tag_succ;
	
	public Block(
			int 	start_line, 
			int 	start_line_java, 
			int 	length, 
			String 	tag_pred, 
			String 	tag_succ) {
		this.start_line 		= start_line;
		this.start_line_java 	= start_line_java;
		this.length 			= length;
		this.tag_pred 			= tag_pred;
		this.tag_succ 			= tag_succ;
	}
	
	public void setStartLine(int start_line) {
		this.start_line = start_line;
	}
	
	public void setStartLineJava(int start_line_java) {
		this.start_line_java = start_line_java;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public void setTagPred(String tag_pred) {
		this.tag_pred = tag_pred;
	}
	
	public void setTagSucc(String tag_succ) {
		this.tag_succ = tag_succ;
	}
	
	public int getStartLine() {
		return this.start_line;
	}
	
	public int getEndLine() {
		return this.start_line + this.length - 1;
	}
	
	public int getStartLineJava() {
		return this.start_line_java;
	}
	
	public int getEndLineJava() {
		return this.start_line_java + this.length - 1;
	}
	
	public int getBlockLength() {
		return this.length;
	}
	
	public String getTagPred() {
		return this.tag_pred;
	}
	
	public String getTagSucc() {
		return this.tag_succ;
	}
	
	public String toDump() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.start_line + "\t" + getEndLine() + "\t" + this.start_line_java + "\t" + getEndLineJava() + "\t"
				+ this.length + "\t" + this.tag_pred + "\t" + this.tag_succ);
		return sb.toString();
	}
}
