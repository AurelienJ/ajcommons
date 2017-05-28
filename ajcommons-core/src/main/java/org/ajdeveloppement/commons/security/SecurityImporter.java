/*
 * Créé le 04/04/2009 à 18:47 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.SecretKey;

import org.ajdeveloppement.commons.io.FileUtils;

/**
 * Utilitaire facilitant l'import de certificat X509 et clé privé dans
 * un KeyStore
 * 
 * @author Aurélien JEOFFRAY
 */
public class SecurityImporter {
	
	/**
	 * <p>Importe les certificats présent dans le répertoire <code>tempCertPath</code>
	 * dans le keyStore fournit en parametre. Les certificats doivent être au format PEM (Base64 avec header footer)
	 * et posseder l'extension ".pem"</p>
	 * 
	 * <p><b>Une fois les certificats importé, ils son supprimé du répertoire de stockage</b></p>
	 * 
	 * @param keyStore le keyStore dans lequel importer les certificats
	 * @param tempCertPath le chemin dans lequel ce trouve les certificats à importer
	 * 
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	public static void importCerts(KeyStore keyStore, File tempCertPath)
			throws CertificateException, IOException, KeyStoreException {
		importCerts(keyStore, tempCertPath, true);
	}
	
	/**
	 * <p>Importe les certificats présent dans le répertoire <code>tempCertPath</code>
	 * dans le keyStore fournit en parametre. Les certificats doivent être au format PEM (Base64 avec header footer)
	 * et posseder l'extension ".pem"</p>
	 * 
	 * @param keyStore le keyStore dans lequel importer les certificats
	 * @param tempCertPath le chemin dans lequel ce trouve les certificats à importer
	 * @param deletePemFileAfterImport indique si les certificats à importé doivent ou non être supprimé après import
	 * 
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	public static void importCerts(KeyStore keyStore, File tempCertPath, boolean deletePemFileAfterImport)
			throws CertificateException, IOException, KeyStoreException {
		List<File> certsPEM = FileUtils.listAllFiles(tempCertPath, ".*\\.pem"); //$NON-NLS-1$
		for(File certPEM : certsPEM) {
			Certificate certificate = CertUtil.readCert(certPEM);

			String alias = certPEM.getName();
			alias = alias.substring(0, alias.indexOf(".pem")); //$NON-NLS-1$
			
			keyStore.setCertificateEntry(alias, certificate);

			if(deletePemFileAfterImport)
				certPEM.delete();
		}
	}
	
	/**
	 * <p>Importe les certificats ou chaine de certificat présent dans le flux
	 * dans le keyStore fournit en parametre à l'alias fournit.</p>
	 * 
	 * <p><b>Le flux esr fermé après l'import</b></p>
	 * 
	 * @param keyStore le keyStore dans lequel importer les certificats
	 * @param in le flux dans lequel ce trouve le certificat
	 * @param alias l'alias du keyStore correspondant au certificat
	 * 
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	public static void importCert(KeyStore keyStore, InputStream in, String alias)
			throws CertificateException, IOException, KeyStoreException {
		if(in == null)
			return;
		
		Certificate certificate = CertUtil.readCert(in);
		
		keyStore.setCertificateEntry(alias, certificate);
	}
	
	/**
	 * Importe une clé secrète dans le keyStore à partir d'un flux. La clé doit
	 * être au format suivant {ALGO}clé_en_hexa. Ex: {AES}a6428dc65...
	 * 
	 * @param keyStore le keyStore dans lequel importer la clé
	 * @param in le flux à partir duquel récupérer la clé
	 * @param alias l'alias de stockage de la clé dans le keyStore
	 * @param password le mot de passe de stockage de la clé dans le keyStore
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws InvalidKeyException 
	 */
	public static void importKey(KeyStore keyStore, InputStream in, String alias, char[] password)
			throws IOException, KeyStoreException, InvalidKeyException {
		if(in == null)
			return;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			byte[] buffer = new byte[1024];
			int nbLecture = 0;
			while((nbLecture = in.read(buffer)) > 0) {
				baos.write(buffer, 0, nbLecture);
			}
		} finally {
			in.close();
		}
		
		SecretKey key = KeyUtil.unserializeKey(baos.toString("ASCII")); //$NON-NLS-1$
			
		keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection(password));
	}
}
