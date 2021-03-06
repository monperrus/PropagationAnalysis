package com.vmusco.pminer.analyze;

import com.vmusco.smf.utils.CollectionsTools;

/**
 * This class implements the set described by Bohner.
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class CIAEstimationSets {

	String[] cis, ais;
	String[] fpis, dis;
	String[] commons;
	
	public CIAEstimationSets(String[] cis, String[] ais) {
		commons = CollectionsTools.setIntersection(cis, ais);
		fpis = CollectionsTools.setDifference(cis, ais);
		dis = CollectionsTools.setDifference(ais, cis);
	}
	
	/**
	 * @return the set of actual impacts (previously inputed to constructor)
	 */
	public String[] getActualsImpactSet() {
		return ais;
	}
	
	/**
	 * @return the set of impacts found using the CIA (previously inputed to constructor)
	 */
	public String[] getCandidatesImpactSet() {
		return cis;
	}
	
	/**
	 * @return the set of impacts only found using the CIA
	 */
	public String[] getFalsePositivesImpactedSet() {
		return fpis;
	}
	

	/**
	 * @return the set of impacts missed by the CIA
	 */
	public String[] getDiscoveredImpactedSet() {
		return dis;
	}
	

	/**
	 * @return the set of impacts found by CIA which are actual impacts
	 */
	public String[] getFoundImpactedSet(){
		return commons;
	}
}
