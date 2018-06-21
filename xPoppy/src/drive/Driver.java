package drive;

import center.TagCenter;
import manage.Manager;

public class Driver {

	public static void main(String[] args) {
		Manager mgr = new Manager(
				"E:\\projects\\eclipse\\xPoppy",	// path of current project
//				TagCenter.PROCESS_GENERATE, 		// process generate
				TagCenter.PROCESS_SEED, 			// process seed
				TagCenter.OBJECT_SIENA, 			// object name
				5, 									// version
				1, 									// min num_mul
				2									// max num_mul
				);
		
//		mgr.generate();
		mgr.seed();
	}
	
}
