package manage;

import process.dump.DumpManager;
import process.generate.Generator;
import process.load.LoadManager;
import process.seed.Seeder;
import center.DateCenter;
import center.TagCenter;

public class Manager {

	DateCenter 	dc;
	
	LoadManager loadMgr; 
	
	DumpManager dumpMgr;
	
	Generator 	generator;

	Seeder 		seeder;
	
	public Manager(
			String 	loc_project,
			String 	process_type, 
			String 	object_name, 
			int 	version,
			int 	num_mul_min, 
			int 	num_mul_max
			) {
		this.dc = new DateCenter(
				loc_project, 
				process_type, 
				object_name, 
				version, 
				num_mul_min, 
				num_mul_max
				);
		
		this.loadMgr 	= new LoadManager(dc);
		this.dumpMgr 	= new DumpManager(dc);
		this.generator 	= new Generator(dc);
		this.seeder 	= new Seeder(dc);
	}
	
	public void generate() {
		this.loadMgr.load(TagCenter.PROCESS_TYPE);
		this.generator.generate();
		this.dumpMgr.dump(TagCenter.PROCESS_TYPE);
	}
	
	public void seed() {
		this.loadMgr.load(TagCenter.PROCESS_TYPE);
		this.seeder.seed();
		this.dumpMgr.dump(TagCenter.PROCESS_TYPE);
	}
	
}
