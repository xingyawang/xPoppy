package process.generate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import toolkits.poppy.PoppyMultipleVersion;
import toolkits.poppy.PoppySingleVersion;

import center.DateCenter;
import center.TagCenter;

public class Generator {

	DateCenter dc;
	
	public Generator(DateCenter dc) {
		this.dc = dc;
	}
	
	public void generate() {
		System.out.println(TagCenter.PROCESS_GENERATE + "\t" + TagCenter.START);
		
		List<PoppySingleVersion> psVersions = this.dc.getPoppySingleVersions();
		Map<Integer, List<PoppyMultipleVersion>> map_pmVersions = this.dc.getMapPoppyMultipleVersions();
		
		for (int num_mul=1; num_mul<=psVersions.size(); ++num_mul) {
			if (num_mul < this.dc.getMinNumOfMultiple() || num_mul > this.dc.getMaxNumOfMultiple()) {
				continue;
			}
			
			List<PoppyMultipleVersion> 	pmVersions 	= new ArrayList<PoppyMultipleVersion>();
			List<List<Integer>> cs = generateCombinations(psVersions.size(), num_mul);
			for (List<Integer> c : cs) {
				PoppyMultipleVersion pmVersion = new PoppyMultipleVersion(psVersions.get(0).getObjectName(), psVersions.get(0).getVersion(), c);
				pmVersions.add(pmVersion);
			}
			map_pmVersions.put(num_mul, pmVersions);
			
			System.out.println(TagCenter.FAULT_MULTIPLE + "\t" + num_mul + "\t" + pmVersions.size());
//			for (PoppyMultipleVersion pmv : pmVersions) {
//				System.out.println(pmv.toDump(false));
//			}
		}
		
		System.out.println(TagCenter.PROCESS_GENERATE + "\t" + TagCenter.END);
	}
	
	public List<List<Integer>> generateCombinations(int n, int m) {
		BigInteger num_comb = computeNumOfCombinations(n, m);
		BigInteger v_1000 	= new BigInteger("1000");
		BigInteger v_100000 = new BigInteger("100000");
		
		List<List<Integer>> cs = new ArrayList<List<Integer>>();
		if (num_comb.compareTo(v_1000) < 1) {
			cs = generateCombinations_binary(n, m);
		} else if (num_comb.compareTo(v_100000) < 1) {
			cs = generateCombinations_binary_1000(n, m);
		} else {
			cs = generateCombinations_random_1000(n, m);
		}
		
		return cs;
	}
	
	public BigInteger computeNumOfCombinations(int n, int m) {
		BigInteger v1 = computeFactorial(n);
		BigInteger v2 = computeFactorial(m);
		BigInteger v3 = computeFactorial(n-m);
		BigInteger v4 = v1.divide(v2.multiply(v3));
		return v4;
	}
	
	public BigInteger computeFactorial(int v) {
		BigInteger v0 = new BigInteger(String.valueOf(v));
		BigInteger v1 = BigInteger.ONE;
		for (BigInteger index = BigInteger.ONE; index.compareTo(v0) < 1; index = index.add(BigInteger.ONE)) {
			v1 = v1.multiply(index);
		}
		return v1;
	}
	
	public List<List<Integer>> generateCombinations_random_1000(int n, int m) {
		Set<Set<Integer>> cs_set = new HashSet<Set<Integer>>();
		while (cs_set.size() < 1000) {
			HashSet<Integer> c_set = randomSet(1, n+1, m);
			cs_set.add(c_set);
			cs_set = conductConflictDetection(cs_set);
		}
		
		List<List<Integer>> cs = new ArrayList<List<Integer>>();
		for (Set<Integer> c_set : cs_set) {
			cs.add(convertSet2List(c_set));
		}
		
		return cs;
	}
	
	public List<Integer> convertSet2List(Set<Integer> set) {
		List<Integer> list = new ArrayList<Integer>();
		for (Integer i : set) {
			list.add(i);
		}
		return list;
	}
	
	public List<List<Integer>> generateCombinations_binary(int n, int m) {
		
		List<List<Integer>> cs = new ArrayList<List<Integer>>();
		
		if (n < m) {
			System.err.println("n<m");
			return cs;
			
		} else if (n == m) {
			List<Integer> c = new ArrayList<Integer>();
			for (int i=0; i<n; i++) {
				c.add(i+1);
			}
			cs.add(c);
//			return cs;
			
		} else if (n > m) {
			int[] a = new int[n];
			for (int i=0; i<m-0; i++) { a[i] = 1; }
			
			while (true) {
				List<Integer> c = new ArrayList<Integer>();
				for (int i=0; i<n-1; i++) {
					if (a[i]==0 || a[i+1]==1) continue;
					
					for (int j=0; j<n; j++) {
						if (a[j] == 1) {
							c.add(j+1);
						}
					}
					
					{
						a[i]	= 0;
						a[i+1]	= 1;
					}
					
					if (i > 0) {
						int k = 0;
						for (int j=0; j<i; j++) {
							k = k + a[j];
						}
						
						for (int j=0; j<k; j++) {
							a[j] = 1;
						}
						
						for (int j=k; j<i; j++) {
							a[j] = 0;
						}
					}
					
					break;
				}
				cs.add(c);
				
				boolean b_stop = true;
				for (int i=n-m; i<=n-1; i++) {
					if (a[i]==0) {
						b_stop = false;
					}
				}
				if (true == b_stop) {
					c = new ArrayList<Integer>();
					for (int i=n-m; i<=n-1; i++) {
						c.add(i+1);
					}
					cs.add(c);
					break;
				}
			}
		}
		
//		System.out.println(cs.size());
		List<List<Integer>> cs_new = conductConflictDetection(cs);
//		System.out.println(cs_new.size());
		
		return cs_new;
	}
	
	public List<List<Integer>> generateCombinations_binary_1000(int n, int m) {
		List<List<Integer>> cs = generateCombinations_binary(n, m);
		
		if (cs.size() < 1000) {
			return cs;
		} else {
			HashSet<Integer> random_set = randomSet(0, cs.size()-1, 1000);
			
			List<List<Integer>> cs_new = new ArrayList<List<Integer>>();
			for (Integer random : random_set) {
				cs_new.add(cs.get(random));
			}
			return cs_new;
		}
	}
	
	public HashSet<Integer> randomSet(int min, int max, int n) {
		if (n > (max-min+1) || max < min) {
			return null;
		}
		
		HashSet<Integer> set = new HashSet<Integer>();
		while (set.size() < n) {
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num);
		}
		return set;
	}
	
//	public List<List<Integer>> generateCombinations_recursion(int n, int m) {
//		List<List<Integer>> cs = new ArrayList<List<Integer>>();
//		combine(n, m, new ArrayList<Integer>(), cs);
//		return cs;
//	}
//	
//	public void combine(int n, int m, List<Integer> c, List<List<Integer>> cs) {
//		if (n < 0) {
//			return;
//		}
//		
//		if (m == 0) {
//			cs.add(c);
//		} else {
//			List<Integer> c1 = new ArrayList<Integer>();
//			c1.addAll(c);
//			c1.add(n-1);
//			combine(n-1, m-1, c1, cs);
//			
//			List<Integer> c2 = new ArrayList<Integer>();
//			c2.addAll(c);
//			combine(n-1, m, c2, cs);
//		}
//	}
	
	public List<List<Integer>> conductConflictDetection(List<List<Integer>> cs) {
		List<List<Integer>> cs_new = new ArrayList<List<Integer>>();
		cs_new.addAll(cs);
		
		{
			int[][] mutex = dc.getMutex();
			
			// no failed test cases
			for (int i=1; i<=mutex.length-1; ++i) {
				if (mutex[i][i] == 1) {
					for (List<Integer> c : cs) {
						if (c.contains(i)) {
							cs_new.remove(c);
						}
					}
				}
			}
			
			// faults of mutex
			for (int i=1; i<=mutex.length-1; ++i) {
				for (int j=1; j<=mutex.length-1; ++j) {
					if (mutex[i][j] == 1 && (i != j)) {
						for (List<Integer> c : cs) {
							if (c.contains(i) && c.contains(j)) {
								cs_new.remove(c);
							}
						}
					}
				}
			}
		}
		
		return cs_new;
	}
	
	public Set<Set<Integer>> conductConflictDetection(Set<Set<Integer>> cs_set) {
		Set<Set<Integer>> cs_set_new = new HashSet<Set<Integer>>();
		cs_set_new.addAll(cs_set);
		
		{
			int[][] mutex = dc.getMutex();
			
			// no failed test cases
			for (int i = 1; i <= mutex.length - 1; ++i) {
				if (mutex[i][i] == 1) {
					for (Set<Integer> c_set : cs_set) {
						if (c_set.contains(i)) {
							cs_set_new.remove(c_set);
						}
					}
				}
			}

			// faults of mutex
			for (int i = 1; i <= mutex.length - 1; ++i) {
				for (int j = 1; j <= mutex.length - 1; ++j) {
					if (mutex[i][j] == 1 && (i != j)) {
						for (Set<Integer> c_set : cs_set) {
							if (c_set.contains(i) && c_set.contains(j)) {
								cs_set_new.remove(c_set);
							}
						}
					}
				}
			}
		}
		
		return cs_set_new;
	}
	
	/*public static void main(String[] args) {
		System.out.println("000");
//		Generator g = new Generator(null);
//		
//		List<List<Integer>> cs = new ArrayList<List<Integer>>();
//		
//		cs = g.generateCombinations_binary(39, 7);
		
//		for (List<Integer> c : cs) {
//			System.out.println(c);
//		}
//		System.out.println(cs.size());
		
		Set<Integer> set1 = new HashSet<Integer>();
		set1.add(10);
		set1.add(20);
		set1.add(30);
		
		Set<Integer> set2 = new HashSet<Integer>();
		set2.add(10);
		set2.add(20);
		set2.add(30);
		
		if (set1 == set2) {
			System.out.println("==\t" + true);
		}
		
		if (set1.equals(set2)) {
			System.out.println("eq\t" + true);
		}
		
		System.out.println("111");
		
		Generator g = new Generator(null);
		
		BigInteger v = g.computeNumOfCombinations(39, 2);
		
		System.out.println(v);
		
		System.out.println("222");
		
		Set<Integer> set = g.randomSet(11, 20, 5);
		
		System.out.println(set);
		
		System.out.println("333");
	}*/
	
}
