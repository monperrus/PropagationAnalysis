package com.vmusco.softminer.tests.cases.testImplicitDependency;

// AUTOBOXING PHENOMENON
public class Foo {

	public static Boolean bar(boolean bool) {
		if(bool)
			return true;
		else
			return null;
	}

}
