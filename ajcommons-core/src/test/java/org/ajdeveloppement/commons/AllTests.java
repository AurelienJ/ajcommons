package org.ajdeveloppement.commons;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A JUnit test suite for org.ajdeveloppement.commons package
 * @author Aurelien JEOFFRAY
 *
 */
public class AllTests {

	/**
	 * 
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.ajdeveloppement.commons"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(ConvertersTest.class);
		suite.addTestSuite(StringFormattersTest.class);
		suite.addTestSuite(AjResourcesReaderTest.class);
		suite.addTestSuite(ArraysUtilsTest.class);
		//$JUnit-END$
		return suite;
	}

}
