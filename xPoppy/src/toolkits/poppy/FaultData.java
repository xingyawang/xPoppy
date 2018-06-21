package toolkits.poppy;

import center.TagCenter;

public class FaultData {

	int 	fault_index;
	
	String 	fault_name;
	
	String 	cpp_file;
	
	boolean is_active;
	
	Block 	fault_block;
	
	Block 	right_block;
	
	public FaultData(
			int 	fault_index, 
			String 	fault_name, 
			String 	cpp_file) {
		this.fault_index 	= fault_index;
		this.fault_name 	= fault_name;
		this.cpp_file 		= cpp_file;
		this.is_active 		= false;
		this.fault_block 	= new Block(0, 0, 0, null, null);
		this.right_block 	= new Block(0, 0, 0, null, null);
	}
	
	public void setFaultBlock(Block block) {
		this.fault_block = block;
	}
	
	public void setRightBlock(Block block) {
		this.right_block = block;
	}
	
	public void setActive(boolean is_active) {
		this.is_active = is_active;
	}
	
	public int getFaultIndex() {
		return this.fault_index;
	}
	
	public String getFaultName() {
		return this.fault_name;
	}
	
	public String getCppFile() {
		return this.cpp_file;
	}
	
	public Block getFaultBlock() {
		return this.fault_block;
	}
	
	public Block getRightBlock() {
		return this.right_block;
	}
	
	public boolean isActive() {
		return this.is_active;
	}
	
	public String toDump() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.fault_index 	+ "\t");
		sb.append(this.fault_name 	+ "\t");
		sb.append(this.cpp_file 	+ "\t");
		sb.append(this.is_active 	+ "\n");
		sb.append(TagCenter.FAULT_BLOCK + "\t" + this.fault_block.toDump() + "\n");
		sb.append(TagCenter.RIGHT_BLOCK + "\t" + this.right_block.toDump() + "\n");
		return sb.toString();
	}
	
}
