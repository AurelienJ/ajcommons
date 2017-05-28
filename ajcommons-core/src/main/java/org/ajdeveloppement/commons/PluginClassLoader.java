/*
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL-C Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL-C telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 
 * 
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pri connaissance de la licence CeCILL-C, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package org.ajdeveloppement.commons;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Améliore le classLoader <code>URLClassLoader</code> pour permettre de ajouter
 * l'ensemble des jar/zip d'un répertoire dans le classpath.<br>
 * Permet ainsi l'intégration d'extension (Plugin) dans une application sans modifier
 * le classpath.
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class PluginClassLoader extends URLClassLoader {
	
	/**
	 * Crée un classLoader chargeant l'ensemble des librairies (zip et jar) d'un répertoire
	 * 
	 * @param parent le classLoader parent dont dépend le présent classLoader
	 * @param libDir Le répertoire contenant l'ensemble des fichier jar/zip a considérer
	 * @throws MalformedURLException
	 */
	public PluginClassLoader(ClassLoader parent, File libDir) throws MalformedURLException {
		super(libDir != null ? new URL[] { libDir.toURI().toURL() } : new URL[] {}, parent);

		addDirectoryJars(libDir);
	}
	
	public void addDirectoryJars(File libDir) throws MalformedURLException {
		if(libDir == null)
			return;
		
		File[] jars = libDir.listFiles((File dir, String name) -> {
				boolean accept = false;
				String smallName = name.toLowerCase();
				if (smallName.endsWith(".jar")) { //$NON-NLS-1$
					accept = true;
				} else if (smallName.endsWith(".zip")) { //$NON-NLS-1$
					accept = true;
				}
				return accept;
			});

		// Do nothing if no jar or zip files were found
		if (jars == null) {
			return;
		}

		for (int i = 0; i < jars.length; i++) {
			if (jars[i].isFile()) {
				addURL(jars[i].toURI().toURL());
			}
		}
	}
}
