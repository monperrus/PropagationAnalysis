package com.vmusco.smf.misc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.smf.utils.MutationsSetTools;

public class SetTesting {

	private static String[] set1 = new String[]{"a", "b", "c"};
	private static String[] set2 = new String[]{"c", "d", "e"};
	private static String[] set3 = new String[]{"a", "b", "c"};
	private static String[] set4 = new String[]{"c", "d", "e"};
	private static String[] set5 = new String[]{"c", "d"};
	
	@Test
	public void testDifference(){

		String[] res = MutationsSetTools.setDifference(set1, set2);
		Assert.assertEquals(res.length, 2);
		Assert.assertEquals(res[0], "a");
		Assert.assertEquals(res[1], "b");
		

		res = MutationsSetTools.setDifference(set2, set1);
		Assert.assertEquals(res.length, 2);
		Assert.assertEquals(res[0], "d");
		Assert.assertEquals(res[1], "e");
	}

	@Test
	public void testIntersection(){
		String[] res = MutationsSetTools.setIntersection(set1, set2);
		Assert.assertEquals(res.length, 1);
		Assert.assertEquals(res[0], "c");
	}

	@Test
	public void testEquals(){
		Assert.assertFalse(MutationsSetTools.areSetsSimilars(set1, set2));
		Assert.assertFalse(MutationsSetTools.areSetsSimilars(set2, set1));
		
		Assert.assertTrue(MutationsSetTools.areSetsSimilars(set1, set3));
	}
	
	@Test
	public void testMutantAlive(){
		Assert.assertTrue(MutationsSetTools.isMutantAlive(set1, set2, set3, set4));
		Assert.assertFalse(MutationsSetTools.isMutantAlive(set1, set2, set3, set5));
	}
	
	
}
