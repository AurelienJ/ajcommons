/*
 * Créé le 28 mars 2009 à 12:03:21 pour AjCommons (Bibliothèque de composant communs)
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
 * sur le site "http://www.CeCILL-C.info".
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
 * "http://www.CeCILL.info". 
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
package org.ajdeveloppement.commons.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.ajdeveloppement.commons.Converters;

/**
 * Utilitaire permettant le chiffrement/déchiffrement de chaînes de caractères selon le Cipher
 * et la clé fournit en paramètre.
 *  
 * @author Aurélien JEOFFRAY
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CryptUtil {
	/**
	 * Format d'encodage de la chaîne chiffré
	 */
	public enum CryptEncoding {
		/**
		 * La chaîne chiffré sera encodé en Hexadécimal
		 */
		HEXADECIMAL,
		/**
		 * La chaîne chiffré sera encodé en base 64
		 */
		BASE64
	}
	
	/**
	 * Algorithme de chiffrement par défaut: AES
	 */
	public static final String DEFAULT_ALGORITHM = "AES"; //$NON-NLS-1$
	
	/**
	 * Format d'encodage par défaut du flux chiffré: HEXADECIMAL
	 */
	public static final CryptEncoding DEFAULT_CRYPT_ENCODING = CryptEncoding.HEXADECIMAL;
	
	@XmlTransient
	protected Cipher cipher;
	//s'assure que la clé n'est jamais sérialisé avec l'objet
	transient protected SecretKey key;
	
	private CryptEncoding cryptEncoding = DEFAULT_CRYPT_ENCODING;
	
	/**
	 * Initialise l'utilitaire de chiffrement avec l'algorithme AES et une clé de taille maximum
	 * admise sur le système et généré aléatoirement. La clé généré peut être récupéré par
	 * la méthode {@link #getSecretKey()}
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public CryptUtil()
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(null, null);
	}
	
	/**
	 * Initialise l'utilitaire de chiffrement avec l'algorithme AES et la clé symetrique
	 * fournit en parametre
	 * @param key la clé AES de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public CryptUtil(SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(null, key);
	}
	
	/**
	 * <p>Initialise l'utilitaire de chiffrement avec l'algorithme fournit en parametre et une
	 * clé de taille maximum admise sur le système et généré aléatoirement. La clé généré
	 * peut être récupéré par la méthode {@link #getSecretKey()}.</p>
	 * <p>Le cipher fournit doit correspondre à un chiffrement symétrique</p> 
	 * 
	 * @param cipher l'algorithme de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public CryptUtil(Cipher cipher)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		this(cipher, null);
	}
	
	/**
	 * <p>Initialise l'utilitaire de chiffrement avec l'algorithme fournit en parametre et la clé
	 * fournit en parametre</p>
	 * <p>Le cipher fournit doit correspondre à un chiffrement symétrique</p>
	 * 
	 * @param cipher l'algorithme de chiffrement
	 * @param key la clé de chiffrement
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public CryptUtil(Cipher cipher, SecretKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		if(cipher != null)
			this.cipher = cipher;
		else
			this.cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
		
		if(key != null) {
			if(!key.getAlgorithm().equals(this.cipher.getAlgorithm()))
				throw new InvalidAlgorithmParameterException("SecretKey algorithm is different to cipher algorithm"); //$NON-NLS-1$
			this.key = key;
		} else {
			KeyGenerator kgen = KeyGenerator.getInstance(this.cipher.getAlgorithm()); 
			kgen.init(new SecureRandom());
            this.key = kgen.generateKey();
		}
	}
	
	/**
	 * Retourne la clé secrete utilisé pour les opération  de chiffrement/déchiffrement
	 * 
	 * @return la clé secréte
	 */
	public SecretKey getSecretKey() {
		return key;
	}
	
	/**
	 * Définit la clé secrete utilisé pour les opération  de chiffrement/déchiffrement
	 * 
	 * @param key la clé secrete permettant les opérations de chiffrement 
	 * @throws InvalidAlgorithmParameterException 
	 */
	public void setSecretKey(SecretKey key) throws InvalidAlgorithmParameterException {
		if(!key.getAlgorithm().equals(cipher.getAlgorithm()))
			throw new InvalidAlgorithmParameterException("SecretKey algorithm is different to cipher algorithm"); //$NON-NLS-1$
		this.key = key;
	}
	
	/**
	 * Indique le format d'encodage des chaines chiffré (Hexa ou base 64)
	 * 
	 * @return the cryptEncoding le format d'encodage des chaines chiffré
	 */
	public CryptEncoding getCryptEncoding() {
		return cryptEncoding;
	}

	/**
	 * Définit le format selon lequel les chaînes chiffré doivent être retourné
	 * 
	 * @param cryptEncoding le format selon lequel les chaînes chiffré doivent être retourné
	 */
	public void setCryptEncoding(CryptEncoding cryptEncoding) {
		this.cryptEncoding = cryptEncoding;
	}

	/**
	 * Sauvegarde la clé dans un fichier
	 * 
	 * @param keyFile le fichier dans lequel sauvegarder la clé
	 * @throws IOException
	 */
	public void storeKey(File keyFile) throws IOException {
		Writer writer  = new FileWriter(keyFile);
		try {
			writer.write(KeyUtil.serializeKey(key));
			writer.flush();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * Charge la clé à partir d'un fichier. S'assurer que la clé utilisé correspond
	 * bien à l'algorithme de chiffrement définit par le cipher.
	 * 
	 * @param keyFile le fichier dans lequel sauvegarder la clé
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 */
	public void loadKey(File keyFile) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException {
		BufferedReader reader = new BufferedReader(new FileReader(keyFile));
		try {
			key = KeyUtil.unserializeKey(reader.readLine());
			
			if(!key.getAlgorithm().equals(cipher.getAlgorithm())) {
				key = null;
				throw new InvalidAlgorithmParameterException("SecretKey algorithm is different to cipher algorithm"); //$NON-NLS-1$
			}
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Chiffre la chaine fournit en parametre
	 *  
	 * @param value la chaine à chiffrer
	 * @return la chaine chiffré encodé en hexadécimal ou base 64
	 * 
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String crypt(String value) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		byte[] cryptedValue = cipher.doFinal(value.getBytes());
		
		if(cryptEncoding == CryptEncoding.BASE64)
			return Base64.getEncoder().encodeToString(cryptedValue);
		return Converters.byteArrayToHexString(cryptedValue);
	}

	/**
	 * Déchiffre la chaine fournit en parametre
	 * 
	 * @param value la chaine à déchiffrer encodé en hexadécimal ou base 64
	 * @return la chaine déchiffré
	 * 
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String uncrypt(String value) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		byte[] cryptedValue;
		if(cryptEncoding == CryptEncoding.BASE64)
			cryptedValue =java.util.Base64.getDecoder().decode(value);
		else
			cryptedValue = Converters.hexStringToByteArray(value);
		
		return new String(cipher.doFinal(cryptedValue));
	}
}
