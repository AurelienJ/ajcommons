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
package org.ajdeveloppement.commons.net;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Beans stockant les informations spécifique à l'utilisation d'un serveur mandataire (proxy).
 * </p>
 * <p>
 * La configuration de proxy peut être activé dans l'application enfant en appelant la methode
 * {@link Proxy#activateProxyConfiguration()}
 * </p>
 * <p>
 * Actuellement, ne permet que la configuration d'un unique proxy pour tous les protocoles (http, https, ftp et socks)
 * </p>
 * @author Aurélien JEOFFRAY
 * @version 0.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={
		"proxyServerAddress",
		"proxyServerPort",
		"useProxyForLocalNetwork"})
public class Proxy {
	
	/**
	 * Types de proxy
	 *
	 */
	public enum ProxyType {
		/**
		 * Proxy HTTP
		 */
		HTTP,
		/**
		 * Proxy HTTPS
		 */
		HTTPS,
		/**
		 * Proxy FTP
		 */
		FTP,
		/**
		 * Proxy SOCKS
		 */
		SOCKS
	}
	
	@XmlElement(name="address")
	private String proxyServerAddress = ""; //$NON-NLS-1$
	@XmlElement(name="port")
	private int proxyServerPort = 8080;

	@XmlElement(name="localNetworkProxy")
	private boolean useProxyForLocalNetwork = false;

	/**
	 * 
	 */
	public Proxy() {
	}
	
	/**
	 * 
	 * @param proxyServerAddress
	 * @param proxyServerPort
	 */
	public Proxy(String proxyServerAddress, int proxyServerPort) {
		this(proxyServerAddress, proxyServerPort, false);
	}
	
	/**
	 * Crée une nouvelle configuration de proxy
	 *  
	 * @param proxyServerAddress l'adresse du serveur mandataire
	 * @param proxyServerPort le port du serveur mandataire
	 * @param useProxyForLocalNetwork <code>true</code> si l'application doit utiliser le serveur proxy y compris pour les adresses du réseau local, <code>false</code> sinon
	 */
	public Proxy(String proxyServerAddress, int proxyServerPort, boolean useProxyForLocalNetwork) {
		super();
		this.proxyServerAddress = proxyServerAddress;
		this.proxyServerPort = proxyServerPort;
		this.useProxyForLocalNetwork = useProxyForLocalNetwork;
	}

	/**
	 * Retourne l'adresse du serveur proxy
	 * 
	 * @return l'adresse du serveur proxy
	 */
	public String getProxyServerAddress() {
		return proxyServerAddress;
	}

	/**
	 * Définit l'adresse du serveur proxy
	 * 
	 * @param proxyServerAddress l'adresse du serveur proxy
	 */
	public void setProxyServerAddress(String proxyServerAddress) {
		this.proxyServerAddress = proxyServerAddress;
	}

	/**
	 * Retourne le port TCP du serveur proxy
	 * 
	 * @return le port TCP du serveur proxy
	 */
	public int getProxyServerPort() {
		return proxyServerPort;
	}

	/**
	 * Définit le port TCP du serveur proxy
	 * 
	 * @param proxyServerPort le port TCP du serveur proxy
	 */
	public void setProxyServerPort(int proxyServerPort) {
		this.proxyServerPort = proxyServerPort;
	}

	/**
	 * Indique si le serveur proxy doit ou non être utilisé pour le réseau local
	 * 
	 * @return <code>true</code> pour une utilisation du proxy pour le réseau local, <code>false</code>
	 * dans le cas contraire
	 */
	public boolean isUseProxyForLocalNetwork() {
		return useProxyForLocalNetwork;
	}

	/**
	 * Définit si l'on utilise le serveur proxy pour les adresses local ou non
	 * 
	 * @param useProxyForLocalNetwork <code>true pour une utilisation du serveur proxy dans le réseau local</code>
	 */
	public void setUseProxyForLocalNetwork(boolean useProxyForLocalNetwork) {
		this.useProxyForLocalNetwork = useProxyForLocalNetwork;
	}
	
	/**
	 * Active les paramètres de proxy pour l'application courante 
	 */
	public void activateProxyConfiguration() {
		System.setProperty("http.proxyHost", proxyServerAddress); //$NON-NLS-1$
		System.setProperty("http.proxyPort",Integer.toString(proxyServerPort)); //$NON-NLS-1$
		System.setProperty("https.proxyHost", proxyServerAddress); //$NON-NLS-1$
		System.setProperty("https.proxyPort",Integer.toString(proxyServerPort)); //$NON-NLS-1$
		System.setProperty("ftp.proxyHost", proxyServerAddress); //$NON-NLS-1$
		System.setProperty("ftp.proxyPort",Integer.toString(proxyServerPort)); //$NON-NLS-1$
		System.setProperty("socksProxyHost", proxyServerAddress); //$NON-NLS-1$
		System.setProperty("socksProxyHost",Integer.toString(proxyServerPort)); //$NON-NLS-1$
	}
}
