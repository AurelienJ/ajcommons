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
package org.ajdeveloppement.updater;

import java.security.cert.Certificate;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Beans représantant un dépôt pour l'application ou une extention et contenant 
 * l'ensemble des resources de celle ci dans leurs dernières versions.
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository {
	@XmlAttribute(name="name")
	private String reposName = ""; //$NON-NLS-1$
	@XmlAttribute(name="moduleVersion")
	private String refAppVersion = ""; //$NON-NLS-1$
	@XmlElement(name="url")
	private String[] reposURLs = {""}; //$NON-NLS-1$
	@XmlElement(name="mirror")
	private int currentMirror = 0;
	private boolean canDisable = true;
	private boolean mustBeSigned = false;
	@XmlTransient
	private Certificate certificate;
	@XmlTransient
	private Status reposStatus = Status.DISCONNECTED;

	public Repository() {
	}

	/**
	 * Construit un nouveau dépôt enrenseignant l'ensemble des informations le concernant
	 * 
	 * @param reposName le nom du dépôt
	 * @param reposURLs la ou les URLs du dépôt
	 * @param refAppVersion la version de la référence
	 */
	public Repository(String reposName, String[] reposURLs, String refAppVersion) {
		this(reposName, reposURLs, refAppVersion, true);
	}
	
	/**
	 * Construit un nouveau dépôt enrenseignant l'ensemble des informations le concernant
	 * 
	 * @param reposName le nom du dépôt
	 * @param reposURLs la ou les URLs du dépôt
	 * @param refAppVersion la version de la référence
	 * @param canDisable true si le dépôt peut être désactivé, false sinon
	 */
	public Repository(String reposName, String[] reposURLs, String refAppVersion, boolean canDisable) {
		this.reposName = reposName;
		this.reposURLs = reposURLs;
		this.canDisable = canDisable;
	}

	/**
	 * Retourne l'état courrant du dépôt
	 * 
	 * @see Status
	 * @return l'état du dépôt
	 */
	public Status getReposStatus() {
		return reposStatus;
	}

	/**
	 * Définit l'état du dépôt
	 * 
	 * @see Status
	 * @param reposStatus l'état du dépôt à définir
	 */
	public void setReposStatus(Status reposStatus) {
		this.reposStatus = reposStatus;
	}

	/**
	 * Si le dépôt est signé numériquement, retourne le certificat permettant d'en
	 * vérifier la signature. Dans le cas contraire, retourne null.
	 * 
	 * @see Certificate
	 * @return le certificat permettant de vérifier la signature
	 */
	public Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Définit le certificat numérique permettant de vérifier la signature du dépôt
	 * si celui ci est signé.
	 * 
	 * @param certificate the certificate to set
	 */
	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}

	/**
	 * <p>
	 * Donne le nom logique représantant le dépôt. Généralement le nom logique 
	 * de l'application ou l'extention rattaché.
	 * </p>
	 * 
	 * @return le nom du dépôt
	 */
	public String getReposName() {
		return reposName;
	}
	
	/**
	 * <p>
	 * Définit le nom logique représantant le dépôt. Prendre par convention le nom logique 
	 * de l'application ou l'extention rattaché.
	 * </p>
	 * <p>
	 * Le nom du dépôt va permettre l'établissement d'un fichier d'index référencant
	 * l'ensemble des fichiers du dépôts. Il est donc nécéssaire de ne pas utiliser
	 * de caractères non autorisé dans les noms de fichiers sur l'ensemble des plateformes
	 * supporté.<br>
	 * De préférence, se limiter aux caractères [a-zA-Z0-9_-]
	 * </p>
	 * 
	 * @param reposName le nom du dépôt
	 */
	public void setReposName(String reposName) {
		this.reposName = reposName;
	}

	/**
	 * Retourne la ou les URLs pointant vers le dépôts. Si plusieurs
	 * URLs sont fournit, les URLs supplémentaires seront utilisé comme
	 * mirroir si la première adresse est inaccessible.
	 * 
	 * @return la ou les URLs du dépôt
	 */
	public String[] getReposURLs() {
		return reposURLs;
	}
	
	/**
	 * Définit l'URL du dépôt ainsi que ses éventuels mirror.
	 * 
	 * @param reposURLs la ou les URLs du dépôt
	 */
	public void setReposURLs(String[] reposURLs) {
		this.reposURLs = reposURLs;
	}
	
	/**
	 * Retourne la version de l'application ou extention concerné par le dépôt
	 * 
	 * @return la version de la référence
	 */
	public String getRefAppVersion() {
		return refAppVersion;
	}
	
	/**
	 * <p>
	 * Définit la version de l'application ou extention concerné par le dépôt
	 * </p>
	 * <p>
	 * le numero de version va permettre d'afficher un chagelog pertinent pour l'utilisateur
	 * </p>
	 * 
	 * @param refAppVersion la version de la référence
	 */
	public void setRefAppVersion(String refAppVersion) {
		this.refAppVersion = refAppVersion;
	}
	
	/**
	 * Retourne l'index du mirroir courant utilisé
	 * 
	 * @return l'index du mirroir courant utilisé
	 */
	public int getCurrentMirror() {
		return currentMirror;
	}

	/**
	 * Définit l'index du mirroir à utiliser
	 * 
	 * @param currentMirror l'index du mirroir à utiliser
	 */
	public void setCurrentMirror(int currentMirror) {
		this.currentMirror = currentMirror;
	}

	/**
	 * Retourne <code>true</code> si le dépôt peut être désactivé et <code>false</code> si
	 * l'on souhaite que l'utilisateur ne puisse pas désactivé le dépôt.
	 * 
	 * @return the canDisable
	 */
	public boolean isCanDisable() {
		return canDisable;
	}

	/**
	 * Défini si le dépôt peut ou non être désactivé par l'utilisateur. Si l'on interdit la désactivation
	 * par l'utilisateur, alors le dépôt doit absolument être signé numériquement. La signature doit
	 * aussi être validé par une autorité de certification reconnue soit par java soit par votre application<br>
	 * Pour cela, placé la méthode {@link #setMustBeSigned(boolean)} à <code>true</code>
	 * si <code>canDisable</code> est placé à false
	 * 
	 * @param canDisable <code>true</code> si l'utilisateur peut désativé le dépôt, <code>false</code> sinon
	 */
	public void setCanDisable(boolean canDisable) {
		this.canDisable = canDisable;
		if(!canDisable)
			setMustBeSigned(true);
	}

	/**
	 * Informe si le dépôt doit être obligatoirement signé ou non.
	 * 
	 * @return <code>true</code> si le dépôt doit être signé, <code>false</code> sinon
	 */
	public boolean isMustBeSigned() {
		return mustBeSigned;
	}

	/**
	 * <p>Définit si l'on attend de ce dépôt qu'il soit systèmatiquement signé ou non. Devrait être placé à true
	 * pour tout dépôt signé.</p>
	 * <p>Si {@link #isCanDisable()} retourne false, le dépôt doit toujours être signé, la valeur passé en paramètre
	 * n'est alors pas prise en compte</p>
	 * 
	 * @param mustBeSigned <code>true</code> si on attend un dépôt signé, <code>false</code> sinon
	 */
	public void setMustBeSigned(boolean mustBeSigned) {
		if(canDisable)
			this.mustBeSigned = mustBeSigned;
	}

	@Override
	public String toString() {
		return reposURLs[0];
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((refAppVersion == null) ? 0 : refAppVersion.hashCode());
		result = prime * result
				+ ((reposName == null) ? 0 : reposName.hashCode());
		result = prime * result + Arrays.hashCode(reposURLs);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Repository other = (Repository) obj;
		if (refAppVersion == null) {
			if (other.refAppVersion != null)
				return false;
		} else if (!refAppVersion.equals(other.refAppVersion))
			return false;
		if (reposName == null) {
			if (other.reposName != null)
				return false;
		} else if (!reposName.equals(other.reposName))
			return false;
		if (!Arrays.equals(reposURLs, other.reposURLs))
			return false;
		return true;
	}
	
	public enum Status {
		/**
		 * L'application n'est pas connecté au dépôt
		 */
		DISCONNECTED,
		/**
		 * La connection au dépôt à échoué
		 */
		CONNECTION_FAILED,
		/**
		 * La désérialisation du fichier XML à échoué
		 */
		UNSERIALIZE_FAILED,
		/**
		 * La lecture du certificat à échoué 
		 */
		CERTIFICATE_FAILED,
		/**
		 * La vérification du certificat à échoué
		 */
		CERTIFICATE_VALIDATION_FAILED,
		/**
		 * Le contrôle de la validité du fichier révision à échoué.
		 */
		REVISION_SIGN_VERIFY_FAILED,
		/**
		 * La révision à été vérifié par un certificat est autosigné
		 */
		REVISION_SIGN_VERIFY_WITH_SELFSIGNED_CERT,
		/**
		 * La révision à été vérifié par un certificat expiré
		 */
		REVISION_SIGN_VERIFY_WITH_EXPIRED_CERT,
		/**
		 * La révision à été vérifié par un certificat signé par une autorité de certification
		 */
		REVISION_SIGN_VERIFY_WITH_TRUSTED_CERT,
		/**
		 * Le fichier révision n'est pas signé
		 */
		REVISION_NOSIGN_FOUND
	}


}
