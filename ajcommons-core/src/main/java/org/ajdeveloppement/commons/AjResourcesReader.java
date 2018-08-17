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

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * Permet une gestion amélioré des fichiers de ressources en fonction de la localisation.
 * Avec <i>AjResourcesReader</i>, il est possible d'utiliser un fichier _custom.properties
 * qui permet à l'utilisateur de personnaliser un ou plusieurs paramètres du fichier properties
 * original sans altérer le fichier d'origine.
 * </p>
 * <p>
 * Cette class est particulièrement adapté au chargement des fichiers de localisations. Les fichiers
 * properties destiné à être lu par <code>AjResourcesReader</code> doivent être encodé en UTF-8
 * </p>
 * 
 * @author Aurélien JEOFFFRAY
 * @version 1.1
 */
public class AjResourcesReader {

	private Locale locale = Locale.getDefault();
	
	private ResourceBundle resources;
	private ResourceBundle customResources;
	private ClassLoader classLoader;
	
	private String resourcesPath;

	/**
	 * Instancie un nouveau gestionnaire pour une resource nommée
	 * donnée et localisée
	 * 
	 * @param resourcesPath le chemin du fichier de resource à charger.
	 */
	public AjResourcesReader(String resourcesPath) {
		this(resourcesPath, null, null);
	}
	
	/**
	 * Instancie un nouveau gestionnaire pour une resource nommée
	 * donnée et localisée.
	 * 
	 * @param resourcesPath le chemin du fichier de resource à charger.
	 * @param l la localisation du fichier à charger
	 */
	public AjResourcesReader(String resourcesPath, Locale l) {
		this(resourcesPath, null, l);
	}
	
	/**
	 * Instancie un nouveau gestionnaire pour une resource nommée
	 * donnée et localisée.
	 * 
	 * @param resourcesPath le chemin du fichier de resource à charger.
	 * @param cl le classloader pour trouver le fichier de ressource
	 */
	public AjResourcesReader(String resourcesPath, ClassLoader cl) {
		this(resourcesPath, cl, null);
	}
	
	/**
	 * Instancie un nouveau gestionnaire pour une resource nommée
	 * donnée et localisée.
	 * 
	 * @param resourcesPath le chemin du fichier de resource à charger.
	 * @param cl le classloader pour trouver le fichier de ressource
	 * @param l la localisation du fichier à charger
	 */
	public AjResourcesReader(String resourcesPath, ClassLoader cl, Locale l) {
		this.resourcesPath = resourcesPath;
		this.classLoader = cl;
		if(l != null)
			this.locale = l;
		loadResourcesBundles();
	}
	

	/**
	 * Définit la locale des fichiers de resources à charger
	 * 
	 * @param l la locale des fichiers de resources.
	 */
	public void setLocale(Locale l) {
		locale = l;
		reloadResourcesBundles();
	}
	
	private void loadResourcesBundles() {
		if(classLoader == null)
			classLoader = Thread.currentThread().getContextClassLoader();
		
		resources = ResourceBundle.getBundle(resourcesPath, locale, classLoader);
		
		try {
			customResources = ResourceBundle.getBundle(resourcesPath + "_custom", locale, classLoader); //$NON-NLS-1$
		} catch (MissingResourceException mre) {}
	}
	
	/**
	 * Recharge les fichiers de resources. Permet de changer le classloader et/ou
	 * la locale sans avoir à créer une nouvelle instance.
	 */
	public void reloadResourcesBundles() {
		loadResourcesBundles();
	}
	
	/**
	 * Indique si la clé existe ou non dans les ressources
	 * 
	 * @param key la clé à tester
	 * @return <code>true</code> si la clé existe, <code>false</code> sinon
	 */
	public boolean containsKey(String key) {
		boolean contains =false;
		if(customResources != null) {
			contains = customResources.containsKey(key);
		}
		
		if(!contains) {
			contains = resources.containsKey(key);
		}
		
		return contains;
	}

	/**
	 * Retourne une chaîne de resource localisé en fonction de la
	 * clé fournit en paramètre
	 * 
	 * @param nm la clé de recherche pour laquelle récupérer une chaîne
	 * @return la chaîne localisé correspondant à la clé
	 */
	public String getResourceString(String nm) {
		return getResourceString(nm, new Object[] {});
	}

	/**
	 * retourne la valeur de la ressource qualifié par son nom donnée en parametre
	 * 
	 * @param nm le nom de la ressource à retourné
	 * @param replaces remplacement dans la valeur retourné des champs % par les valeurs fournit en parametre
	 * 
	 * @return String - la ressource à retourner
	 */
	public String getResourceString(String nm, Object... replaces) {
		String res = null;
		try {
			if(customResources != null) {
				try {
					res = customResources.getString(nm);
				} catch (MissingResourceException e) {}
			}
			if(res == null) {
				try {
					res = resources.getString(nm);
				} catch (MissingResourceException e) {
					e.printStackTrace(); // se contente d'afficher une erreur dans la console
				}
			}
			if(res != null && getJavaVersion() < 9) {
				try {
					res = new String(res.getBytes("ISO-8859-1"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace(); // se contente d'afficher une erreur dans la console
				}
			}
		} finally {
			if(res == null)
				res = nm;
		}

		if(replaces.length > 0)
			return String.format(res, replaces);
		return res;
	}

	/**
	 * retourne une valeur de type entier à partir d'un fichier de
	 * resource.
	 * 
	 * @param nm la clé de recherche pour la valeur
	 * @return la valeur numérique à retourné. -1 par défaut si non trouvé
	 */
	public int getResourceInteger(String nm) {
		int iVal = -1;
		boolean customExist = false;
		if(customResources != null) {
			try {
				iVal = Integer.parseInt(customResources.getString(nm));
				customExist = true;
			} catch (MissingResourceException e) { }
		}
		if(!customExist) {
			try {
				iVal = Integer.parseInt(resources.getString(nm));
			} catch (MissingResourceException mre) { }
		}
		return iVal;
	}
	
	/**
	 * Get the java runtime version
	 * 
	 * @return
	 */
	private static double getJavaVersion() {
	    String version = System.getProperty("java.version");
	    int pos = version.indexOf('.');
	    pos = version.indexOf('.', pos+1);
	    return Double.parseDouble (version.substring (0, pos));
	}
}
