package org.ajdeveloppement.commons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test suite for ArraysUtils class
 * @author Aurelien JEOFFRAY
 *
 */
public class ArraysUtilsTest {

	/**
	 * Test d'inversion d'élément
	 */
	@Test
	public void testSwap() {
		Integer[] elements = new Integer[] { 1, 2, 3, 4 };
		
		ArraysUtils.swap(elements, 1, 3);
		assertArrayEquals(new Integer[] { 1, 4, 3, 2 }, elements);
	}

	/**
	 * Test de contenue de tableau
	 */
	@SuppressWarnings("nls")
	@Test
	public void testContains() {
		String[] elements = { "un", "deux", "trois" };
		
		assertTrue(ArraysUtils.contains(elements, "deux"));
		assertFalse(ArraysUtils.contains(elements, "zero"));
	}

}
