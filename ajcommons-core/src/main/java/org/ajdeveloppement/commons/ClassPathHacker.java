/**
 * 
 */
package org.ajdeveloppement.commons;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * !! Include in ajcommons for test only !!
 * 
 * The system classloader (ClassLoader.getSystemClassLoader()) is a subclass of
 * URLClassLoader. It can therefore be casted into a URLClassLoader and used as
 * one. URLClassLoader has a protected method addURL(URL url), which you can use
 * to add files, jars, web addresses - any valid URL in fact.
 * 
 * Since the method is protected you need to use reflection to invoke it.
 * 
 * @author Antony Miguel (see
 *         http://forum.java.sun.com/thread.jspa?threadID=300557)
 * @version 0.1
 */
@Beta
public class ClassPathHacker {
	private static final Class<?>[] parameters = new Class<?>[] { URL.class };

	/**
	 * Add file to classpath
	 * 
	 * @param s the path of file
	 */
	public static void addFile(String s) {
		File f = new File(s);
		addFile(f);
	}

	/**
	 * Add file to classpath
	 * 
	 * @param f the path of file
	 */
	public static void addFile(File f) {
		try {
			addURL(f.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add file to classpath
	 * 
	 * @param u the url of file
	 */
	public static void addURL(URL u) {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		try {
			/* Class was uncheched, so used URLClassLoader.class instead */
			Method method = URLClassLoader.class.getDeclaredMethod(
					"addURL", parameters); //$NON-NLS-1$
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
			System.out
					.println("Dynamically added " + u.toString() + " to classLoader"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
