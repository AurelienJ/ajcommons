/*
 * Créer le 10 nov. 07 à 19:05:21 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.updater;

/**
 * Regroupe l'ensemble des métadonnées concernant un fichier de
 * l'application. Permet de transmettre les informations essentiel
 * sur un fichier via le réseau sans avoir besoin de télécharger
 * le fichier concerné.
 *
 * @author Aurélien JEOFFRAY
 * @version 1.0
 *
 */
public class FileMetaData {
	private String path = ""; //$NON-NLS-1$
	private long hash = 0;
	private String securehash = ""; //$NON-NLS-1$
	private long fileSize = 0;
	private String os = "all"; //$NON-NLS-1$
	
	public FileMetaData() {
		
	}
	
	/**
	 * Retourne le chemin relatif du fichier représenté.<br>
	 * Le chemin est toujours relatif au répertoire d'installation de l'application
	 * 
	 * @return the path le chemin relatif du fichier
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Définit le chemin relatif du fichier représenté<br>
	 * Le chemin doit toujours être relatif au répertoire d'installation de l'application
	 * 
	 * @param path le chemin relatif du fichier représenté
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Retourne la valeur Adler32 du fichier représenté
	 * 
	 * @return the hash
	 */
	public long getHash() {
		return hash;
	}
	
	/**
	 * Définit la valeur Adler32 du fichier représenté
	 * 
	 * @param hash a valeur Adler32 du fichier
	 */
	public void setHash(long hash) {
		this.hash = hash;
	}
	
	/**
	 * Retourne la signature SHA-256 du fichier encodé en hexadecimal. La signature est utilisé pour
	 * vérifier que le fichier est bien celui attendu.
	 * 
	 * @return le hash SHA-256
	 */
	public String getSecurehash() {
		return securehash;
	}

	/**
	 * Définit la signature SHA-256 du fichier encodé en hexadecimal. La signature est utilisé pour
	 * vérifier que le fichier est bien celui attendu. contrairement à la somme de contrôle CRC-32 qui ne sert qu'à vérifier
	 * qu'un fichier à été modifié ou non car pas assez sécurisé.
	 *  
	 * @param securehash le hash SHA-256 du fichier
	 */
	public void setSecurehash(String securehash) {
		this.securehash = securehash;
	}

	/**
	 * Retourne la taille en Octet (Byte) du fichier représenté
	 * 
	 * @return la taille en Octet (Byte) du fichier représenté
	 */
	public long getFileSize() {
		return fileSize;
	}
	
	/**
	 * Définit la taille en Octet (Byte) du fichier représenté
	 * 
	 * @param fileSize la taille en Octet (Byte) du fichier représenté
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * Retourne le système d'exploitation de destination du fichier si celui-ci est spécifique
	 * ou <code>all</code> dans le cas contraire.
	 *  
	 * @return le système d'exploitation de destination du fichier
	 */
	public String getOs() {
		return os;
	}

	/**
	 * <p>
	 * Définit le système d'exploitation de destination du fichier si celui-ci est spécifique
	 * ou <code>all</code> dans le cas contraire.<br>
	 * Attention, la version du système n'est pas considéré
	 * </p>
	 * <p>
	 * Par convention on utilisera:
	 * <ul>
	 * 	<li>all - tous système</li>
	 * 	<li>windows - Systèmes windows</li>
	 *	<li>windows32 - Systèmes windows 32 bits</li>
	 * 	<li>windows64 - Systèmes windows 64 bits</li>
	 * 	<li>linux - Systèmes linux</li>
	 * 	<li>linux32 - Systèmes linux 32 bits</li>
	 * 	<li>linux64 - Systèmes linux 64 bits</li>
	 * 	<li>macosx - Systèmes mac OS X (Seul les systèmes intel 64bits sont représenté)</li>
	 * </ul> 
	 * </p>
	 * D'autres valeur sont possible mais ne seront pas intéropérable.
	 * 
	 * @param os le système d'exploitation de destination du fichier
	 */
	public void setOs(String os) {
		this.os = os;
	}
}
