package org.ajdeveloppement.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Aurelien JEOFFRAY
 *
 */
public class AjResourcesReaderTest {
	
	
	@BeforeAll
	public static void setUp() throws Exception {
		Locale.setDefault(Locale.FRENCH);
	}

	/**
	 * Test que le constructeur charge bien le fichier de resource souhaité en fonction des paramètres
	 */
	@Test
	public void testAjResourcesReaderConstructeur() {
		//Test avec le classloader par défaut et la locale par défaut
		AjResourcesReader resourceReader = new AjResourcesReader("org.ajdeveloppement.commons.resourcetest"); //$NON-NLS-1$
		
		//On est paramétré avec une local fr par défaut
		assertEquals("Test ref 1 fr", resourceReader.getResourceString("reference1"));  //$NON-NLS-1$//$NON-NLS-2$
		
		//Test avec une locale spécifique
		resourceReader = new AjResourcesReader("org.ajdeveloppement.commons.resourcetest", Locale.ENGLISH); //$NON-NLS-1$
		
		assertEquals("Test ref 1 en", resourceReader.getResourceString("reference1"));  //$NON-NLS-1$//$NON-NLS-2$
		
		//Test avec un classloader spécifique
		URLClassLoader classLoader;
		try {
			classLoader = new URLClassLoader(new URL[] { new URL("file:src/test/resources/org/ajdeveloppement/commons/") }); //$NON-NLS-1$
			
			resourceReader = new AjResourcesReader("resourcetest", classLoader); //$NON-NLS-1$
			
			assertEquals("Test ref 1 fr", resourceReader.getResourceString("reference1"));  //$NON-NLS-1$//$NON-NLS-2$
			
			resourceReader = new AjResourcesReader("resourcetest", classLoader, Locale.ENGLISH); //$NON-NLS-1$
			
			assertEquals("Test ref 1 en", resourceReader.getResourceString("reference1"));  //$NON-NLS-1$//$NON-NLS-2$
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Vérfie qu'il prend bien les resources dans les bons fichiers
	 */
	@Test
	public void testGetResourceStringString() {
		AjResourcesReader resourceReader = new AjResourcesReader("org.ajdeveloppement.commons.resourcetest"); //$NON-NLS-1$
		
		assertEquals("Test ref 1 fr", resourceReader.getResourceString("reference1"));  //$NON-NLS-1$//$NON-NLS-2$
		assertEquals("Test réf 2 custom", resourceReader.getResourceString("reference2"));  //$NON-NLS-1$//$NON-NLS-2$
		assertEquals("Ref non localisé", resourceReader.getResourceString("reference3"));  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Test la prise en charge des variables de chaîne
	 */
	@Test
	public void testGetResourceStringStringObjectArray() {
		AjResourcesReader resourceReader = new AjResourcesReader("org.ajdeveloppement.commons.resourcetest"); //$NON-NLS-1$
		
		assertEquals("Ref avec la var x", resourceReader.getResourceString("reference4", "x"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Test de la gestion des références numérique entière
	 */
	@Test
	public void testGetResourceInteger() {
		AjResourcesReader resourceReader = new AjResourcesReader("org.ajdeveloppement.commons.resourcetest"); //$NON-NLS-1$
		
		assertEquals(58, resourceReader.getResourceInteger("reference5")); //$NON-NLS-1$
	}

}
