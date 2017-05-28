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
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.util.Collections;

/**
 * Permet de tester la chaine de certification d'un certificat X509 selon la norme PKI
 * 
 * @author Aurélien JEOFFRAY
 */
public class CertValidator {
	
	private KeyStore userKeyStore;
	
	/**
	 * Initialise le validateur avec un keyStore personnalisé pouvant stocké des 
	 * autorités de certification supplémentaire et/ou spécifique. Mettre <code>null</code>
	 * si l'on souhaite utilisé uniquement les certificats par défaut de java.
	 * 
	 * @param userKeyStore le keystore personnalisé stockant les certificats supplémentaire
	 */
	public CertValidator(KeyStore userKeyStore) {
		this.userKeyStore = userKeyStore;
	}
	
	/**
	 * Vérifie la chaine de certificat fournit en parametre selon la norme PKI
	 * 
	 * @param certPath la chaine de certificat à vérifier
	 * @return le résultat de la vérification
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException
	 * @throws CertPathValidatorException
	 */
	public PKIXCertPathValidatorResult verifyCertPath(CertPath certPath) throws KeyStoreException, NoSuchAlgorithmException, 
			CertificateException, IOException, InvalidAlgorithmParameterException, CertPathValidatorException {
		
		FileInputStream systemKeyStoreInputStream = new FileInputStream(new File(System.getProperty("java.home"), "lib/security/cacerts")); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			KeyStore systemKeyStore = KeyStore.getInstance("JKS"); //$NON-NLS-1$
			systemKeyStore.load(systemKeyStoreInputStream, null);
			
			//Ajoute au KeyStoreSystem, les certificats du keyStore utilisateur
			if(userKeyStore != null) {
			    for(String alias : Collections.list(userKeyStore.aliases())) {
			    	if(userKeyStore.isCertificateEntry(alias)) {
			    		systemKeyStore.setCertificateEntry(alias, userKeyStore.getCertificate(alias));
			    	}
			    }
			}
			
			//Vérifie la chaine de certification
			PKIXParameters params = new PKIXParameters(systemKeyStore);
		    params.setRevocationEnabled(false);
			
		    CertPathValidator cpv = CertPathValidator.getInstance("PKIX"); //$NON-NLS-1$
	
		    return (PKIXCertPathValidatorResult) cpv.validate(certPath, params);
		} finally {
			if(systemKeyStoreInputStream != null)
				systemKeyStoreInputStream.close();
		}
	}
}
