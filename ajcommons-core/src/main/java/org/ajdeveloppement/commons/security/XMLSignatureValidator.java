/*
 * Créé le 6 avr. 2009 à 19:10:08 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ajdeveloppement.commons.security.X509KeySelector.CertType;
import org.ajdeveloppement.commons.security.X509KeySelector.FailedRaison;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class utilitaires permetant de validé une signature de fichier XML
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class XMLSignatureValidator {
	
	private KeyStore appKeyStore;
	private boolean noSignatureValidation = true;
	private Certificate certificate;
	private X509KeySelector.CertType certType;
	private FailedRaison failedRaison;
	
	/**
	 * Construit un nouveau validateur de signature basé sur le KeyStore fournit en paramètre
	 * 
	 * @param appKeyStore le keystore contenant les certificats permettant la validation de la
	 * signature
	 */
	public XMLSignatureValidator(KeyStore appKeyStore) {
		this.appKeyStore = appKeyStore;
	}
	
	/**
	 * informe si l'absence de signature doit être considéré comme un cas valide ou non
	 * 
	 * @return <code>true</code> si on doit validé en cas d'absence de signature et
	 * <code>false</code> dans le cas contraire.
	 */
	public boolean isNoSignatureValidation() {
		return noSignatureValidation;
	}

	/**
	 * Définit si on doit validé ou non l'absence de signature dans le fichier XML.
	 * Si la propriété n'est pas définit, la valeur par défaut est <code>true</code>
	 *  
	 * @param noSignatureValidation the noSignatureValidation to set
	 */
	public void setNoSignatureValidation(boolean noSignatureValidation) {
		this.noSignatureValidation = noSignatureValidation;
	}

	/**
	 * Retourne la nature du dernier certificat validé ou null si aucun certificat n'a été validé
	 * ou si la validation du dernier certificat à échoué
	 * 
	 * @return la nature du dernier certificat validé
	 */
	public X509KeySelector.CertType getCertType() {
		return certType;
	}
	
	/**
	 * Retourne la raison de l'échec de validation du certificat
	 * 
	 * @return keySelector.getFailedRaison();
	 */
	public FailedRaison getCertificateFailedRaison() {
		return failedRaison;
	}
	
	/**
	 * Retourne le certificat servant à valider une signature
	 * 
	 * @return le certificat servant à la validation de la signature
	 */
	public Certificate getCertificate() {
		return certificate;
	}
	
	/**
	 * <p>Vérifie la signature d'un fichier XML encode selon la norme RFC 2828.</p>
	 * <p>En cas d'absence de la balise Signature retourne <code>true</code> ou <code>false</code>
	 * en fonction de la valeur définit par {@link #setNoSignatureValidation(boolean)}. Par défaut retourne <code>true</code></p> 
	 * 
	 * @param xml le fichier xml signé à vérifier
	 * @return retourne <code>true</code> si la signature à été validé et <code>false</code> en cas d'échec de vérification de la signature.
	 * 
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 */
	public boolean verifyXMLSignature(File xml)
			throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, MarshalException,
			XMLSignatureException {
		return verifyXMLSignature(new FileInputStream(xml));
	}

	/**
	 * <p>Vérifie la signature d'un fichier XML encode selon la norme RFC 2828.</p>
	 * <p>En cas d'absence de la balise Signature retourne <code>true</code> ou <code>false</code>
	 * en fonction de la valeur définit par {@link #setNoSignatureValidation(boolean)}. Par défaut retourne <code>true</code></p> 
	 * 
	 * @param xmlStream le flux xml signé à vérifier
	 * @return retourne <code>true</code> si la signature à été validé et <code>false</code> en cas d'échec de vérification de la signature.
	 * 
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 */
	public boolean verifyXMLSignature(InputStream xmlStream)
			throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, MarshalException,
			XMLSignatureException {
		certType = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(xmlStream);
		
		// Find Signature element.
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature"); //$NON-NLS-1$
		if (nl.getLength() != 0) {
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM"); //$NON-NLS-1$
			// Create a DOMValidateContext and specify a KeySelector
			// and document context.
			X509KeySelector keySelector = new X509KeySelector(appKeyStore);
			DOMValidateContext valContext = new DOMValidateContext(keySelector, nl.item(0));

			// Unmarshal the XMLSignature.
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);

			// Validate the XMLSignature.
			boolean valid = signature.validate(valContext);
			failedRaison = keySelector.getFailedRaison();
			certType = keySelector.getCertType();
			if(valid)
				certificate = keySelector.getCertificate();
			//else
			//	failedRaison = FailedRaison.
			
			return valid;
		}
		certType = CertType.NO_CERT;

		return noSignatureValidation;
	}
}
