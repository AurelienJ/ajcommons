package org.ajdeveloppement.commons;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ArraysUtils class
 * @author Aurelien JEOFFRAY
 *
 */
public class ArraysUtilsTest extends TestCase {

	@Override
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test d'inversion d'élément
	 */
	@Test
	public void testSwap() {
		Integer[] elements = new Integer[] { 1, 2, 3, 4 };
		
		ArraysUtils.swap(elements, 1, 3);
		Assert.assertArrayEquals(new Integer[] { 1, 4, 3, 2 }, elements);
	}

	/**
	 * Test de contenue de tableau
	 */
	@SuppressWarnings("nls")
	@Test
	public void testContains() {
		String[] elements = { "un", "deux", "trois" };
		
		Assert.assertTrue(ArraysUtils.contains(elements, "deux"));
		Assert.assertFalse(ArraysUtils.contains(elements, "zero"));
	}

}
