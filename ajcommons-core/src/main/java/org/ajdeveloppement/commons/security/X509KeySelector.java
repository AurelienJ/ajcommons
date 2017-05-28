/*
 * Créé le 05/04/2009 à 17:03 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * Selectionne le certificat nécessaire à la vérification d'une signature cryptographique
 * 
 * @author Aurélien JEOFFRAY
 * 
 */
public class X509KeySelector extends KeySelector {
	
	/**
	 * Identifie les types de certificats valide possible
	 * 
	 * @author Aurélien JEOFFRAY
	 */
	public enum CertType {
		/**
		 * Le certificat emis à été auto signé
		 */
		SELF_SIGNED,
		/**
		 * Le certificat emis à été signé par une autorité de certification
		 */
		TRUSTED,
		/**
		 * Il n'y a pas de certificat
		 */
		NO_CERT
	}
	
	/**
	 * Liste des raisons d'échec de selection de certificat
	 * @author Aurélien JEOFFRAY
	 */
	public enum FailedRaison {
		/**
		 * Il n'y a pas de raison d'echec, le certificat est valide
		 */
		NONE,
		/**
		 * Le certificat n'est pas lisible
		 */
		CERTIFICATE_ERROR,
		/**
		 * Le certificat est en dehors de sa date de validité
		 */
		VALIDITY_ERROR,
		/**
		 * Le certificat à été révoqué par son autorité de certification
		 */
		REVOCATION_ERROR,
		/**
		 * l'authorité de certification du certificat n'a pas été trouvé
		 */
		CA_NOT_FOUND_ERROR,
		/**
		 * Erreur d'origine inconnue
		 */
		OTHER
	}
	
	private KeyStore appKeyStore;
	private CertType certType;
	private FailedRaison failedReason = FailedRaison.NONE;
	private Certificate certificate;

	/**
	 * Construit un sélecteur de certificat utilisant le keyStore fournit en paramètre
	 * comme keyStore supplémentaire de recherche d'authorité de certification en plus
	 * du keyStore java par défaut.
	 *  
	 * @param appKeyStore keyStore supplémentaire utilisé pour validé un certificat ou <code>null</code>
	 * pour se contenter du keyStore standard java
	 */
	public X509KeySelector(KeyStore appKeyStore) {
		this.appKeyStore = appKeyStore;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.crypto.KeySelector#select(javax.xml.crypto.dsig.keyinfo.KeyInfo, 
	 * javax.xml.crypto.KeySelector.Purpose, javax.xml.crypto.AlgorithmMethod, javax.xml.crypto.XMLCryptoContext)
	 */
	@Override
	public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
			AlgorithmMethod method, XMLCryptoContext context)
			throws KeySelectorException {
		Throwable lastObjExc = null;
		try {
			for (Object infContent : keyInfo.getContent()) {
				lastObjExc = null;
				
				XMLStructure info = (XMLStructure) infContent;
				if (!(info instanceof X509Data))
					continue;
				
			    List<X509Certificate> certChain = new ArrayList<X509Certificate>();
				X509Data x509Data = (X509Data) info;
				for (Object x509DataContent : x509Data.getContent()) {
					if (x509DataContent instanceof X509Certificate)
						certChain.add((X509Certificate)x509DataContent);
				}
				
				//test si on est en présence d'un certificat auto-signé
				if(certChain.size() == 1) {
					X509Certificate cert = certChain.get(0);
					try {
						final PublicKey key = cert.getPublicKey();
						
						//Si il ce vérifie avec lui même il est auto-signé
						cert.verify(key);
						
						//On test sa période de validité
						cert.checkValidity();
						
						// Make sure the algorithm is compatible
						// with the method.
						if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
							certType = CertType.SELF_SIGNED;
							certificate = cert;
							
							return new KeySelectorResult() {
								@Override
								public Key getKey() {
									return key;
								}
							};
						}
						
						failedReason = FailedRaison.CERTIFICATE_ERROR;
						
						throw new KeySelectorException("Method algorithm is different of Key algorithm"); //$NON-NLS-1$
					} catch (CertificateExpiredException e) {
						failedReason = FailedRaison.VALIDITY_ERROR;
						
						throw new KeySelectorException("Certificate validity failed!", e); //$NON-NLS-1$
					} catch (CertificateNotYetValidException e) {
						failedReason = FailedRaison.VALIDITY_ERROR;
						
						throw new KeySelectorException("Certificate validity failed!", e); //$NON-NLS-1$
					} catch (InvalidKeyException e) {
					} catch (NoSuchAlgorithmException e) {
						throw new KeySelectorException(e); 
					} catch (NoSuchProviderException e) {
						throw new KeySelectorException(e); 
					} catch (SignatureException e) {
					}
				}
				

				//si le certificat n'est pas auto-signé, test si il à été validé auprès d'une autorité de certification
				try {
					CertificateFactory factory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
					CertPath certPath = factory.generateCertPath(certChain);
		
					CertValidator certValidator = new CertValidator(appKeyStore);
				    PKIXCertPathValidatorResult result = certValidator.verifyCertPath(certPath);
				    
				    final PublicKey key = result.getPublicKey();
					// Make sure the algorithm is compatible
					// with the method.
					if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
						certType = CertType.TRUSTED;
						certificate = certPath.getCertificates().get(0);
						
						return new KeySelectorResult() {
							@Override
							public Key getKey() {
								return key;
							}
						};
					}
					
					failedReason = FailedRaison.CERTIFICATE_ERROR;
					
					throw new KeySelectorException("Method algorithm is different of Key algorithm"); //$NON-NLS-1$
				} catch(CertPathValidatorException e) {
					//failedReason = FailedRaison.CERTIFICATE_ERROR;
					failedReason = FailedRaison.CA_NOT_FOUND_ERROR;
					//failedReason = FailedRaison.REVOCATION_ERROR;
					lastObjExc = e;
				} catch(InvalidAlgorithmParameterException e) {
					failedReason = FailedRaison.OTHER;
					lastObjExc = e;
				} catch(NoSuchAlgorithmException e) {
					failedReason = FailedRaison.OTHER;
					lastObjExc = e;
				} catch(KeyStoreException e) {
					failedReason = FailedRaison.OTHER;
					lastObjExc = e;
				} catch(IOException e) {
					failedReason = FailedRaison.OTHER;
					lastObjExc = e;
				}
			}
		} catch (CertificateException e) {
			if(e instanceof CertificateExpiredException || e instanceof CertificateNotYetValidException)
				failedReason = FailedRaison.VALIDITY_ERROR;
			else
				failedReason = FailedRaison.CERTIFICATE_ERROR;
			throw new KeySelectorException("Invalid Certificate", e); //$NON-NLS-1$
		}
		
		if(lastObjExc != null)
			throw new KeySelectorException("Certificate Validation Failed!", lastObjExc); //$NON-NLS-1$
		
		failedReason = FailedRaison.OTHER;
		throw new KeySelectorException("No key found!"); //$NON-NLS-1$
	}
	
	/**
	 * Retourne le type du certificat sélectionné
	 * 
	 * @return le type du certificat sélectionné
	 */
	public CertType getCertType() {
		return certType;
	}
	
	/**
	 * Retourne la raison de l'échec de validation du certificat si tel est le cas
	 * 
	 * @return la raison de l'échec de validation du certificat
	 */
	public FailedRaison getFailedRaison() {
		return failedReason;
	}
	
	/**
	 * Retourne le certificat sélectionné
	 * 
	 * @return le certificat sélectionné
	 */
	public Certificate getCertificate() {
		return certificate;
	}
	
	private static boolean algEquals(String algURI, String algName) {
		if ((algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) //$NON-NLS-1$
				|| (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) //$NON-NLS-1$
			return true;

		return false;
	}
}
