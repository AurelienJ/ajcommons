/*
 * Copyright 2002-2010 - Aurélien JEOFFRAY
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utilitaires pour manipuler les connections SSL 
 * 
 * @author Aurelien JEOFFRAY
 *
 */
public class SSLUtils {
	
	/**
	 * Retourne une SSLSocketFactory qui prend en charge un keystore personnel en plus du keystore par défaut
	 * de java
	 * 
	 * @param userKeyStore le keystore à ajouter pour la validation des certificats
	 * @return une SSLSocketFactory pour les connections SSL
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyManagementException
	 */
	public static SSLSocketFactory getSSLSocketFactory(KeyStore userKeyStore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException {
		
		KeyStore systemKeyStore = KeyStore.getInstance("JKS"); //$NON-NLS-1$
		FileInputStream systemKeyStoreInputStream = new FileInputStream(new File(System.getProperty("java.home"), "lib/security/cacerts")); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			systemKeyStore.load(systemKeyStoreInputStream, null);
		} finally{
			systemKeyStoreInputStream.close();
		}
		
		if(userKeyStore != null) {
		    for(String alias : Collections.list(userKeyStore.aliases())) {
		    	if(userKeyStore.isCertificateEntry(alias)) {
		    		systemKeyStore.setCertificateEntry(alias, userKeyStore.getCertificate(alias));
		    	}
		    }
		}
		
		// create custom trust manager to ignore trust paths
		/*TrustManager trm = new X509TrustManager() {
		    public X509Certificate[] getAcceptedIssuers() {
			return null;
		    }

		    public void checkClientTrusted(X509Certificate[] certs, String authType) {
		    }

	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
		};*/
		
		TrustManagerFactory tmf =  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(systemKeyStore);
		
		SSLContext ctx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
		ctx.init(null, /*new TrustManager[] { trm }*/ tmf.getTrustManagers(), null);
		
		return ctx.getSocketFactory();
	}
	
	public static SSLServerSocketFactory getSSLServerSocketFactory(KeyStore userKeyStore, String alias, char[] password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, CertificateException, IOException {
		KeyStore serverKeyStore = KeyStore.getInstance("JCEKS"); //$NON-NLS-1$
		serverKeyStore.load(null, password);
		serverKeyStore.setKeyEntry(alias, userKeyStore.getKey(alias, password), password, userKeyStore.getCertificateChain(alias));
	
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(serverKeyStore, password);
		
		SSLContext ctx = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
		ctx.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
		
		return ctx.getServerSocketFactory();
	}
}
