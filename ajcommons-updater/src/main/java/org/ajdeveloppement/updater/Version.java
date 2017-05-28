/*
 * Créer le 18 nov. 07 à 15:57:59 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.updater;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Représentation d'un version de l'application.<br>
 * Donne des informations sur le numéro de la version, son statut, ses auteurs, la date
 * ainsi que le changelog de la version.
 *
 * @author Aurélien JEOFFRAY
 * @version 0.1
 *
 */
public class Version {
	private String appname = ""; //$NON-NLS-1$
	private String version = "0.00.00"; //$NON-NLS-1$
	private String state = "stable"; //$NON-NLS-1$
	private String changeInfos = ""; //$NON-NLS-1$
	private String author = ""; //$NON-NLS-1$
	private Date dateVersion = new Date();
	
	/**
	 * 
	 */
	public Version() {
		
	}
	
	/**
	 * Construit une nouvelle version
	 * 
	 * @param version le numero de la version à construire
	 * @param changeInfos le changelog de la version
	 * @param dateVersion la date de la version
	 */
	public Version(String version, String changeInfos, Date dateVersion) {
		this.version = version;
		this.changeInfos = changeInfos;
		this.dateVersion = dateVersion;
	}

	/**
	 * Retourne le nom de l'application a laquelle la version appartient
	 * 
	 * @return le nom de l'application a laquelle la version appartient
	 */
	public String getAppname() {
		return appname;
	}

	/**
	 * Définit le nom de l'application a laquelle la version appartient
	 * 
	 * @param appname le nom de l'application a laquelle la version appartient
	 */
	public void setAppname(String appname) {
		this.appname = appname;
	}

	/**
	 * Retourne le numéro de la version
	 * 
	 * @return le numéro de la version
	 */
	@XmlAttribute(name="name")
	public String getVersion() {
		return version;
	}

	/**
	 * Définit le numéro de la version
	 * 
	 * @param version le numéro de la version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Retourne l'état de la version
	 * 
	 * @return l'état de la version
	 */
	public String getState() {
		return state;
	}

	/**
	 * <p>
	 * Définit l'état de la version.
	 * </p>
	 * <p>
	 * Par convention, et pour générer un changelog compatible debian, on utlise les distributions débian
	 * <code>stable, unstable, testing, frozen, experimental</code>. Pour plus de détail, consulter 
	 * la documentation debian <a href="http://www.debian.org/doc/debian-policy/footnotes.html#f38">Debian policy distribution</a></p>
	 * 
	 * @param state l'état de la version.
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Retourne la chaines de change log
	 * 
	 * @return lz chaines de change log
	 */
	public String getChangeInfos() {
		return changeInfos;
	}

	/**
	 * <p>
	 * Définit  la chaines de change log<br>
	 * On utilisera la convention débian défini dans le chapitre 4.4 de la debian policy;
	 * </p>
	 * <p>
	 * Chaque changement doit être identifier par deux espaces suivit d'une étoile,
	 * puis d'un nouvel espace, puis du détail du changement. Les lignes suivantes du changement doivent
	 * commencer au même niveau que la premières (soit 4 espaces avant).
	 * </p>
	 * <p>
	 * Si les changement sont regrouper en plusieurs groupe, une ligne blanche doit séparer les changements
	 * de chaque groupe.
	 * </p>
	 * Exemple:
	 * <pre>
	 *  * change details
     *    more change details
     *	    [blank line(s), included in output of dpkg-parsechangelog]
     *  * even more change details
     *	    [optional blank line(s), stripped]
	 * </pre>
	 * @param changeInfos the changeInfos to set
	 */
	public void setChangeInfos(String changeInfos) {
		this.changeInfos = changeInfos;
	}

	/**
	 * Retourne le nom de l'auteur en charge de la construction de la version
	 * 
	 * @return le nom de l'auteur en charge de la construction de la version
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * <p>
	 * Définit le nom de l'auteur en charge de la construction de la version
	 * </p>
	 * <p>
	 * Pour rester compatible avec le format débian, on utilise la convention:<br>
	 * <i>Nom Responsable &lt;adresse@email.com&gt;</i>
	 * </p>
	 * 
	 * @param author le nom de l'auteur en charge de la construction de la version
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * Retourne la date de construction de la version
	 * 
	 * @return la date de construction de la version
	 */
	public Date getDateVersion() {
		return dateVersion;
	}

	/**
	 * Définit la date de construction de la version
	 * @param dateVersion la date de construction de la version
	 */
	public void setDateVersion(Date dateVersion) {
		this.dateVersion = dateVersion;
	}
}
