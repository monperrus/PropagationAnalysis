package com.vmusco.smf.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class CollectionsTools {

	/**
	 * Returns set_one - set_two
	 * @param tests
	 * @return
	 */
	public static String[] setDifference(String[] set_one, String[] set_two){
		Set<String> set = new HashSet<String>();

		for(String s: set_one){
			set.add(s);
		}

		for(String s: set_two){
			if(set.contains(s))
				set.remove(s);
		}

		return (String[]) set.toArray(new String[0]);
	}

	/**
	 * Returns set_one inter set_two
	 * @param tests
	 * @return
	 */
	public static String[] setIntersection(String[] set_one, String[] set_two){
		Set<String> set = new HashSet<String>();
		List<String> ret = new ArrayList<String>();

		for(String s: set_one){
			set.add(s);
		}

		for(String s: set_two){
			if(set.contains(s))
				ret.add(s);
		}

		return (String[]) ret.toArray(new String[0]);
	}

	/**
	 * Returns set_one == set_two
	 * @param tests
	 * @return
	 */
	public static boolean areSetsSimilars(String[] set_one, String[] set_two){
		if(set_one.length != set_two.length)
			return false;

		Set<String> set = new HashSet<String>();

		for(String s: set_one){
			set.add(s);
		}

		for(String s: set_two){
			if(!set.remove(s))
				return false;
		}

		return set.size()==0;
	}

	public static boolean isMutantAlive(String[] failing, String[] hanging, String[] mutfailing, String[] muthanging){
		return areSetsSimilars(failing, mutfailing) && areSetsSimilars(hanging, muthanging);
	}

	public static String[] shuffle(String[] allMutations) {
		List<String> l = new ArrayList<String>();

		for(String m : allMutations){
			l.add(m);
		}

		Collections.shuffle(l);
		return (String[]) l.toArray(new String[0]);
	}

	public static String[] shuffleAndSlice(String[] allMutations, int nb) {
		String[] shuffled = shuffle(allMutations);
		return slice(shuffled, nb);
	}

	public static String[] slice(String[] allMutations, int nb) {
		String[] ret = new String[allMutations.length>nb?nb:allMutations.length];

		for(int i=0; i<ret.length; i++){
			ret[i] = allMutations[i];
		}

		return ret;
	}

	public static String[] setUnionWithoutDuplicates(String[] set1, String[] set2) {
		Set<String> unionset = new HashSet<>();

		for(String s : set1){
			unionset.add(s);
		}

		for(String s : set2){
			unionset.add(s);
		}

		return unionset.toArray(new String[0]);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V>  sortMapByValue( Map<K, V> map ){
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>(){
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ){
				return (o1.getValue()).compareTo( o2.getValue() );
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list){
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}
}
