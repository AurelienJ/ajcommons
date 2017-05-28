/*
 * Créé le 9 oct. 2009 à 17:42:04 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.apps;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ajdeveloppement.commons.io.XMLSerializer;

/**
 * <p>Data context of the application. Name, Version, Host system, JVM.</p>
 * <p>Object is serializable est serializable using JAXB</p>
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
@XmlRootElement(name="context")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationContext {
	@XmlElement(name="name")
	private String applicationName;
	@XmlElement(name="version")
	private String applicationVersion;
	
	@XmlElement(name="jvmversion")
	private String jvmVersion;
	@XmlElement(name="jvmvendor")
	private String jvmVendor;
	@XmlElement(name="OS")
	private String osName;
	@XmlElement(name="architecture")
	private String architecture;
	
	@XmlElement(name="systemType")
	private String systemType;
	
	
	
	
	@XmlElement(name="UUID",required=false)
	private String applicationUUID;
	
	private transient AppRessources applicationRessources;
	
	/**
	 * Référence vers l'instance unique du singleton
	 */
	private static ApplicationContext singleton = new ApplicationContext();
	
	/**
	 * Constructeur privée pour initialisé le singleton
	 */
	private ApplicationContext() {
		
	}
	
	/**
	 * Retourne le contexte de l'application
	 * 
	 * @return le contexte de l'application
	 */
	public static ApplicationContext getContext() {
		return singleton;
	}
	
	/**
	 * Retourne le nom de l'application
	 * 
	 * @return le nom de l'application
	 */
	public String getApplicationName() {
		return applicationName;
	}
	
	/**
	 * Définit le nom de l'application représenté par le contexte
	 * 
	 * @param applicationName le nom de l'application
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
		
		applicationRessources =  new AppRessources(applicationName);
	}
	
	/**
	 * Retourne le numéro de version de l'application
	 * 
	 * @return le numéro de version de l'application
	 */
	public String getApplicationVersion() {
		return applicationVersion;
	}
	
	/**
	 * Définit le numéro de version de l'application
	 * 
	 * @param applicationVersion le numéro de version de l'application
	 */
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	
	/**
	 * Retourne l'identifiant unique (serial) de l'installation. Si le nom
	 * de l'application n'est pas définit, retourne null.
	 * 
	 * @return l'identifiant unique de l'installation au format UUID
	 */
	public String getApplicationUUID() {
		if(applicationUUID != null)
			return applicationUUID;
		if(applicationRessources != null) {
			applicationUUID = AppUtilities.getAppUID(applicationRessources);
			return applicationUUID;
		}
		
		return null;
	}
	
	/**
	 * Retourne le type de système exécuté.
	 * @see AppUtilities
	 * 
	 * @return le type de système exécuté
	 */
	
	public String getSystemType() {
		if(systemType == null)
			systemType = AppUtilities.getCurrentSystem();
		return systemType;
	}
	
	/**
	 * Retourne le nom de l'os exécutant la jvm. La valeur retourné est celle renvoyé par la propriété java "os.name"
	 * 
	 * @return le nom de l'os exécutant la jvm
	 */
	public String getOSName() {
		if(osName == null)
			osName = System.getProperty("os.name"); //$NON-NLS-1$
		return osName;
	}
	
	/**
	 * Retourne l'architecture matérielle sur laquelle s'éxécute la jvm.
	 * La valeur retourné est celle renvoyé par la propriété java "os.arch"
	 *  
	 * @return l'architecture matérielle sur laquelle s'éxécute la jvm.
	 */
	public String getArchitecture() {
		if(architecture == null)
			architecture = System.getProperty("os.arch"); //$NON-NLS-1$
		return architecture;
	}
	
	/**
	 * Retourne le fournisseur de la jvm utilisé
	 * 
	 * @return le fournisseur de la jvm utilisé
	 */
	
	public String getJVMVendor() {
		if(jvmVendor == null)
			jvmVendor =System.getProperty("java.vendor"); //$NON-NLS-1$ 
		return jvmVendor;
	}
	
	/**
	 * Retourne la version de la jvm utilisé
	 * 
	 * @return la version de la jvm utilisé
	 */
	public String getJVMVersion() {
		if(jvmVersion == null)
			jvmVersion = System.getProperty("java.version"); //$NON-NLS-1$
		return jvmVersion;
	}
	
	/**
	 * Retourne le contexte sérialisé au format XML
	 * 
	 * @return le contexte sérialisé au format XML
	 * @throws JAXBException 
	 */
	public String serializeContext() throws JAXBException {
		return XMLSerializer.createMarshallStructure(this);
	}
}
