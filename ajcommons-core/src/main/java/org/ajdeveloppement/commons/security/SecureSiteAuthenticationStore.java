/*
 * Créé le 28 mai 2009 pour ajcommons
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

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.ajdeveloppement.commons.io.XMLSerializer;

/**
 * Stockage sécurisé des couples login/mot de passe associé à un site
 * d'authentification.
 * 
 * @see CryptUtil
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class SecureSiteAuthenticationStore extends CryptUtil {

	private File storeFile;
	private Map<String, SecureLoginPassword> urlAuthenticationEntrys = new HashMap<String, SecureLoginPassword>();
	
	/**
	 * <p>Initialise un nouvel espace de stockage pour les clés d'authentification
	 * L'espace de stockage est initialisé avec un chiffrement par défaut.</p>
	 * <p>Voir {@link CryptUtil#CryptUtil()} pour plus d'information</p>
	 * 
	 * @see CryptUtil
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public SecureSiteAuthenticationStore() throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		super();
	}

	/**
	 * <p>Initialise un nouvel espace de stockage pour les clés d'authentification
	 * L'espace de stockage est initialisé avec le chiffrement fournit en paramètre.</p>
	 * <p>Voir {@link CryptUtil#CryptUtil(Cipher, SecretKey)} pour plus d'information</p>
	 * 
	 * @see CryptUtil
	 * 
	 * @param cipher l'algorithme de chiffrement
	 * @param key la clé de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public SecureSiteAuthenticationStore(Cipher cipher, SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException {
		super(cipher, key);
	}

	/**
	 * <p>Initialise un nouvel espace de stockage pour les clés d'authentification
	 * L'espace de stockage est initialisé avec le chiffrement fournit en paramètre.</p>
	 * <p>Voir {@link CryptUtil#CryptUtil(Cipher)} pour plus d'information</p>
	 * 
	 * @see CryptUtil
	 * 
	 * @param cipher l'algorithme de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public SecureSiteAuthenticationStore(Cipher cipher)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException {
		super(cipher);
	}

	/**
	 * <p>Initialise un nouvel espace de stockage pour les clés d'authentification
	 * L'espace de stockage est initialisé l'algorithme par défaut de CryptUtil et
	 * la clé fournit en paramètre.</p>
	 * <p>Voir {@link CryptUtil#CryptUtil(SecretKey)} pour plus d'information</p>
	 * 
	 * @see CryptUtil
	 * 
	 * @param key la clé de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public SecureSiteAuthenticationStore(SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException {
		super(key);
	}
	
	/**
	 * Retourne le fichier XML contenant les clés d'authentification enregistré.
	 * 
	 * @return le fichier contenant les entrées d'authentification chiffré
	 */
	public File getStoreFile() {
		return storeFile;
	}

	/**
	 * Définit le fichier XML stockant/devant stocker les clés d'authentification
	 * chiffré
	 * 
	 * @param storeFile le fichier XML stockant/devant stocker les clés d'authentification
	 * chiffré
	 */
	public void setStoreFile(File storeFile) {
		this.storeFile = storeFile;
	}

	/**
	 * Ajoute une authentification pour un site donnée au dépôt.
	 * 
	 * @param site le site associé à l'entrée d'authentification
	 * @param passwordAuthentication l'entrée d'authentification
	 * 
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void putSiteAuthentication(String site, PasswordAuthentication passwordAuthentication)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, JAXBException, IOException {
		urlAuthenticationEntrys.put(site, new SecureLoginPassword(this,
				passwordAuthentication.getUserName(),
				new String(passwordAuthentication.getPassword())));
		store();
	}
	
	/**
	 * Retire l'entrée d'authentification associé au site fournit en paramètre
	 * 
	 * @param site le site d'authentification à retirer
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void removeSiteAuthentication(String site) throws JAXBException, IOException {
		urlAuthenticationEntrys.remove(site);
		
		store();
	}
	
	/**
	 * Retourne l'entrée d'authentification associé au site fournit en paramètre
	 * 
	 * @param site le site pour lequel retourner une entrée d'authentification
	 * 
	 * @return l'entrée d'authentification associé au site
	 * 
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public PasswordAuthentication getPasswordAuthentication(String site)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecureLoginPassword slp = urlAuthenticationEntrys.get(site);
		if(slp != null)
			return new PasswordAuthentication(slp.getLogin(), slp.getPassword(this));
		return null;
	}
	
	/**
	 * Enregistre les entrées d'authentification dans le fichier définit
	 * avec la méthode {@link #setStoreFile(File)} avec les mots de passe
	 * sous forme chiffré. Si storeFile est à null alors ne fait rien.
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void store()
			throws JAXBException, IOException {
		if(storeFile != null) {
			URLsAuthentication xmlStore = new URLsAuthentication();
			xmlStore.setAuthentication(urlAuthenticationEntrys);
			XMLSerializer.saveMarshallStructure(storeFile, xmlStore);
		}
	}
	
	/**
	 * Charge les entrées d'authentification stocké dans le fichier
	 * définit par la méthode {@link #setStoreFile(File)}. Si storeFile est
	 * à null alors ne fait rien.
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void load()
			throws JAXBException, IOException {
		if(storeFile != null) {
			URLsAuthentication xmlStore = XMLSerializer.loadMarshallStructure(storeFile, URLsAuthentication.class);
			urlAuthenticationEntrys = xmlStore.getAuthentication();
		}
	}
	
	@XmlRootElement(name="urlauthenticationstore")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class URLsAuthentication {
		@XmlElement(name="authentication")
		private List<URLAuthentication> urlsAuthentication = new ArrayList<URLAuthentication>();
		
		public Map<String, SecureLoginPassword> getAuthentication() {
			Map<String, SecureLoginPassword> map = new HashMap<String, SecureLoginPassword>();
			for(URLAuthentication ua : urlsAuthentication) {
				SecureLoginPassword slp = new SecureLoginPassword();
				slp.login = ua.getLogin();
				slp.password = ua.getPassword();
				map.put(ua.getSite(), slp);
			}
			return map;
		}
		
		public void setAuthentication(Map<String, SecureLoginPassword> entries) {
			urlsAuthentication.clear();
			for(Entry<String, SecureLoginPassword> entry : entries.entrySet()) {
				urlsAuthentication.add(new URLAuthentication(
						entry.getKey(), entry.getValue().login, 
						entry.getValue().password));
			}
		}
	}
	
	private static class SecureLoginPassword {

		private String login;
		private String password;
		
		public SecureLoginPassword() {
		}
		
		/**
		 * @param cryptUtil instance of CryptUtil use for crypt password
		 * @param login
		 * @param password
		 * @throws BadPaddingException 
		 * @throws IllegalBlockSizeException 
		 * @throws InvalidKeyException 
		 */
		public SecureLoginPassword(CryptUtil cryptUtil, String login, String password) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			super();
			this.login = login;
			setPassword(cryptUtil, password);
		}
		
		/**
		 * @return the login
		 */
		public String getLogin() {
			return login;
		}
		/**
		 * @param login the login to set
		 */
		@SuppressWarnings("unused")
		public void setLogin(String login) {
			this.login = login;
		}
		/**
		 * @param cryptUtil instance of CryptUtil use for decrypt password
		 * @return the password
		 * @throws BadPaddingException 
		 * @throws IllegalBlockSizeException 
		 * @throws InvalidKeyException 
		 */
		public char[] getPassword(CryptUtil cryptUtil) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			return cryptUtil.uncrypt(password).toCharArray();
		}
		
		/**
		 * @param cryptUtil instance of CryptUtil use for crypt password
		 * @param password the password to set
		 * @throws BadPaddingException 
		 * @throws IllegalBlockSizeException 
		 * @throws InvalidKeyException 
		 */
		public void setPassword(CryptUtil cryptUtil, String password) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			this.password = cryptUtil.crypt(password);
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name="authentication")
	private static class URLAuthentication {
		@XmlAttribute
		private String site;
		private String login;
		private String password;
		
		@SuppressWarnings("unused")
		public URLAuthentication() {
		}
		
		/**
		 * @param site
		 * @param login
		 * @param password
		 */
		public URLAuthentication(String site, String login, String password) {
			this.site = site;
			this.login = login;
			this.password = password;
		}
		
		/**
		 * @return the url
		 */
		public String getSite() {
			return site;
		}
		/**
		 * @param site the url to set
		 */
		@SuppressWarnings("unused")
		public void setSite(String site) {
			this.site = site;
		}
		/**
		 * @return the login
		 */
		public String getLogin() {
			return login;
		}
		/**
		 * @param login the login to set
		 */
		@SuppressWarnings("unused")
		public void setLogin(String login) {
			this.login = login;
		}
		/**
		 * @return the password
		 */
		public String getPassword() {
			return password; 
		}
		/**
		 * @param password the password to set
		 */
		@SuppressWarnings("unused")
		public void setPassword(String password) {
			this.password = password;
		}
	}
}
