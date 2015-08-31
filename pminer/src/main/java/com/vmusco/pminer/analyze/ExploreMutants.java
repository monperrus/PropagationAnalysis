package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantExecutionIfos;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;
import com.vmusco.softminer.graphs.Graph;

public class ExploreMutants {
	private Graph usegraph;
	private MutationStatistics<?> ms;
	private ArrayList<MutantTestAnalyzer> analyzeListeners = new ArrayList<MutantTestAnalyzer>();

	public ExploreMutants(MutationStatistics<?> ms, Graph usegraph) {
		this.ms = ms;
		this.usegraph = usegraph;
	}

	public void addMutantTestAnalyzeListener(MutantTestAnalyzer mta){
		this.analyzeListeners.add(mta);
	}

	public void removeMutantTestAnalyzeListener(MutantTestAnalyzer mta){
		this.analyzeListeners.remove(mta);
	}

	public void start(String[] allMutations) throws Exception {
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();

		// LOADING PHASE FINISHED !

		fireExecutionStarting();

		int cpt = 0;
		for(String mutation : allMutations){											// For each mutant...
			if(++cpt %50 == 0)
				System.out.println("\t"+(cpt)+" "+mutation);

			boolean forceStop = false;
			MutantIfos ifos = ms.getMutationStats(mutation);

			// relevant IS list of tests impacted by the introduced bug (determined using mutation)
			String[] relevantArray = purifyFailAndHangResultSetForMutant(ps, ifos);

			if(relevantArray == null){
				continue;
			}

			UseGraph propaGraph = new UseGraph(usegraph);
			long duration = usegraph.visitDirectedByGraphNodeVisitor(propaGraph, ifos.getMutationIn());

			// retrieved IS list of tests impacted by the introduced bug (determined use graph)
			String[] retrievedArray = getRetrievedTests(propaGraph, ps.getTestCases());

			forceStop = fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, duration);

			if(forceStop)
				break;
		}

		fireExecutionEnded();

	}

	public boolean fireIntersectionFound(ProcessStatistics ps, String mutation, MutantIfos ifos, String[] retrievedArray, UseGraph propaGraph, long propatime) throws MutationNotRunException {
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, propatime);

			if(aListerner.forceStop()){
				return true;
			}
		}

		return false;
	}

	public void fireExecutionStarting(){
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionStarting();
		}
	}

	private void fireExecutionEnded() {
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionEnded();
		}
	}


	/***
	 * This method return a list test impacted by the a bug inserted in this.mutations.get(mutationKey)
	 * @param bba
	 * @param mutationKey
	 * @return
	 */
	public static String[] getRetrievedTests(UseGraph basin, String[] tests){
		String[] bugs = basin.getBasinNodes();

		Set<String> retrieved = new HashSet<String>();

		for(String bug : bugs){																// We explore each bug determined using the basins technique
			for(String test : tests){														// We determine whether bug basin determined node is a test
				if(ProcessStatistics.areTestsEquivalents(bug, test)){						// If so...
					retrieved.add(test);
					//}else if(bug.indexOf("test") != -1){
					// Nothing to do ? Those cases are reported as test but not returned as so by smf !
				}
			}
		}

		return retrieved.toArray(new String[retrieved.size()]);
	}

	/*public static String[] purifyResultSetForMutant(String[] mutantSet, String[] globalSet){
		Set<String> al = new HashSet<String>();
		Set<String> al2 = new HashSet<String>();

		for(String s : mutantSet){
			al.add(s);
		}

		for(String s : globalSet){
			al2.add(s);
		}

		al.removeAll(al2);

		return al.toArray(new String[0]); 
	}*/

	private static String[] includeTestSuiteGlobalFailingCases(ProcessStatistics ps, String[] testsuites, String[] include){
		Set<String> cases = new HashSet<String>();

		if(include != null){
			for(String s : include){
				cases.add(s);
			}
		}

		for(String ts : testsuites){
			for(String s : ps.getTestCases()){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		return cases.toArray(new String[0]);
	}

	public static String[] purifyFailingResultSetForMutant(ProcessStatistics ps, MutantIfos mi) throws MutationNotRunException {
		MutantExecutionIfos mei = mi.getExecutedTestsResults();
		String[] mutset = includeTestSuiteGlobalFailingCases(ps, mei.getMutantErrorOnTestSuite(), mei.getMutantFailingTestCases());
		String[] glbset = includeTestSuiteGlobalFailingCases(ps, ps.getErrorOnTestSuite(), ps.getFailingTestCases());

		return MutationsSetTools.setDifference(mutset, glbset);
		//return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyIgnoredResultSetForMutant(ProcessStatistics ps, MutantIfos mi) throws MutationNotRunException {
		MutantExecutionIfos mei = mi.getExecutedTestsResults();
		String[] mutset = mei.getMutantIgnoredTestCases();
		String[] glbset = ps.getIgnoredTestCases();

		return MutationsSetTools.setDifference(mutset, glbset);
		//return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyHangingResultSetForMutant(ProcessStatistics ps, MutantIfos mi) throws MutationNotRunException {
		MutantExecutionIfos mei = mi.getExecutedTestsResults();
		String[] mutset = mei.getMutantHangingTestCases();
		String[] glbset = ps.getHangingTestCases();

		return MutationsSetTools.setDifference(mutset, glbset);
		//return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyFailAndHangResultSetForMutant(ProcessStatistics ps, MutantIfos mi) throws MutationNotRunException {
		MutantExecutionIfos mei = mi.getExecutedTestsResults();
		
		Set<String> cases = new HashSet<String>();

		for(String ts : mei.getMutantErrorOnTestSuite()){
			for(String s : ps.getTestCases()){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		for(String s:mei.getMutantHangingTestCases()){
			cases.add(s);
		}

		for(String s:mei.getMutantFailingTestCases()){
			cases.add(s);
		}

		for(String s : mei.getMutantErrorOnTestSuite()){
			for(String ss : ps.getTestCases()){
				if(ss.startsWith(s)){
					cases.add(ss);
				}
			}
		}

		return MutationsSetTools.setDifference(cases.toArray(new String[0]), ps.getUnmutatedFailAndHang());
		//return purifyResultSetForMutant(cases.toArray(new String[0]), ps.getUnmutatedFailAndHang());
	}
}
