/*
 * Créé le 28 mars 2009 à 12:09:06 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA 
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
 * pri connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 * 
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 *
 *  *** GNU GPL Terms *** 
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.ajdeveloppement.commons.security;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Permet la manipulation d'un fichier properties dont les proriétés sont stocké
 * de manière sécurisé par le biais d'un chiffrement symétrique.
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class SecureProperties extends CryptUtil {

	private Properties properties;
	
	/**
	 * Initialise un fichier de propriété chiffré avec l'algorithme AES et une clé
	 * aléatoire. Voir {@link CryptUtil#CryptUtil()} pour plus d'unformation
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public SecureProperties() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(null, null);
	}

	/**
	 * Initialise un fichier de propriété chiffré avec l'algorithme AES et la clé fournit en parametre
	 * 
	 * @param key la clé AES permettant le chiffrement/déchiffrement des propriétés
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public SecureProperties(SecretKey key) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(null, key);
	}

	/**
	 * Initialise un fichier de propriété chiffré avec l'algorithme symétrique fournit en paramètre
	 * et une clé généré aléatoirement.
	 * 
	 * @param cipher
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public SecureProperties(Cipher cipher) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(cipher, null);
	}

	/** 
	 * Initialise un fichier de propriété chiffré avec l'algorithme symétrique
	 * ainsi que la clé associé fournit en parametre.
	 * 
	 * @param cipher l'algorithme de chiffrement
	 * @param key la clé de chiffrement
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public SecureProperties(Cipher cipher, SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		super(cipher, key);
		this.properties = new Properties();
	}
	
	/**
	 * Charge le fichier de proprété associé à l'objet
	 * 
	 * @param reader le reader correspondant au fichier de propriété lié à l'objet
	 * @throws IOException
	 */
	public void load(Reader reader) throws IOException {
		properties.load(reader);
	}
	
	/**
	 * Enregistre le fichier de proprété géré par l'objet
	 * 
	 * @param writer le writer correspondant au fichier de propriété manipulé.
	 * @throws IOException
	 */
	public void store(Writer writer) throws IOException {
		properties.store(writer, "Generate With SecureProperties, do not edit"); //$NON-NLS-1$
	}

	/**
	 * Ajoute ou met à jour une propriété
	 * 
	 * @param property la propriété à ajouter/mettre à jour
	 * @param value la valeur de la propriété en clair.
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public void put(String property, String value) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		properties.setProperty(property, crypt(value));
	}
	
	/**
	 * Retourne la valeur d'une propriété
	 * 
	 * @param property la propriété pour laquelle retourner une valeur
	 * @return la valeur en clair de la propriété
	 * 
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	public String get(String property) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		return uncrypt(properties.getProperty(property));
	}
	
	/**
	 * Supprime une propriété de l'objet
	 * 
	 * @param property la propriété à supprimer
	 */
	public void remove(String property) {
		properties.remove(property);
	}
}
