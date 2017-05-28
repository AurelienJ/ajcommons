/*
 * Créé le 2 déc. 07 à 15:31:26 pour AjCommons (Bibliothèque de composant communs)
 *
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
package org.ajdeveloppement.apps;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.ajdeveloppement.commons.UncheckedException;
import org.ajdeveloppement.commons.io.XMLSerializer;

/**
 * Utilitaires de gestion applicatif. Est inclue:
 * <ul>
 *  <li>Une fonction retournant un identifiant unique pour une installation de l'application</li>
 * 	<li>Un comparateur de version d'appliation</li>
 * 	<li>Une fonction de localisation d'une fenêtre</li>
 * </ul>
 *
 * @author Aurélien JEOFFRAY
 * @version 0.1
 *
 */
public class AppUtilities {
	
	/**
	 * <p>Retourne un identifiant unique pour l'application représenté par son système de resources
	 * en paramètre. Le numéro (au format UUID) est généré au premier appel de la méthode et stocké de manière permanente
	 * pour tous les appels suivant durant toute la durée de vie du logiciel.</p>
	 * <p>L'identifiant est conservé dans un fichier <code>appserial</code> sous le répertoire retourné par la
	 * Méthode {@link AppRessources#getAllusersDataPath()}, supprimer le fichier pour renouveler l'identifiant</p>
	 *  
	 * @param appRessources le système de resources de l'application pour lequel donner un numéro unique permanent
	 * @return Une chaîne UUID correspondant à un identifiant unique pour l'installation
	 */
	public static String getAppUID(AppRessources appRessources) {
		File serialFile = new File(appRessources.getAllusersDataPath(), "appserial"); //$NON-NLS-1$
		if(serialFile.exists()) {
			try {
				return (String)XMLSerializer.loadXMLStructure(serialFile, false); 
			} catch (IOException e) {
				throw new UncheckedException(e);
			}
		}
		
		try {
			return generateSerial(appRessources);
		} catch (IOException e) {
			throw new UncheckedException(e);
		}
	}
	
	private static String generateSerial(AppRessources appRessources) throws IOException {

		String serial = UUID.randomUUID().toString();

		XMLSerializer.saveXMLStructure(new File(appRessources.getAllusersDataPath(), "appserial"),  //$NON-NLS-1$
				serial, false);
		
		return serial;
	}
	/**
	 * Compare deux numéro de version
	 * 
	 * @param version1 la version de réference
	 * @param version2 la version de comparaison
	 * @return valeur &lt; 0 si version2 &lt; version1, &gt; 0 si version2 &gt; version1 et 0 si equivalente
	 */
	public static int compareVersion(String version1, String version2) {
		if(version2.equals(version1))
			return 0;

		return version1.compareToIgnoreCase(version2);
	}
	
	/**
	 * <p>Retourne le système sur lequel l'application s'execute.</p>
	 * <p>Les valeurs retournées peuvent être: windows32, windows64, linux32,
	 * linux64, macosx32, macosx64, other</p>
	 * @return le système hôte.
	 */
	public static String getCurrentSystem() {
		String system = ""; //$NON-NLS-1$
		if(System.getProperty("os.name").startsWith("Windows")) //$NON-NLS-1$ //$NON-NLS-2$
			system = "windows"; //$NON-NLS-1$
		else if(System.getProperty("os.name").startsWith("mac os x")) //$NON-NLS-1$ //$NON-NLS-2$
			system = "macosx"; //$NON-NLS-1$
		else if(System.getProperty("os.name").startsWith("Linux")) //$NON-NLS-1$ //$NON-NLS-2$
			system = "linux"; //$NON-NLS-1$
		else
			system = "other"; //$NON-NLS-1$
		
		if(!system.equals("other")) { //$NON-NLS-1$
			if(System.getProperty("os.arch").contains("x86_64") || System.getProperty("os.arch").contains("amd64")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				system += "64"; //$NON-NLS-1$
			else
				system += "32"; //$NON-NLS-1$
		}
		
		return system;
	}
}
