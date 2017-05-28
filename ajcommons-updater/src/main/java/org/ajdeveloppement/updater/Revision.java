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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Une revision est une représentation à la fois des fichiers d'une version donnée ainsi
 * que de l'historique de version depuis la première version incrémentale possible.
 * 
 * @author  Aurélien Jeoffray
 * @version 1.0
 */
@XmlRootElement
public class Revision {
    
	private FileMetaData preUpdateScript; 
	private FileMetaData postUpdateScript; 
	private List<FileMetaData> cryptoKeys = new ArrayList<FileMetaData>();
	private List<FileMetaData> filesMetaData = new ArrayList<FileMetaData>();
	private List<Version> versions = new ArrayList<Version>();
	
    public Revision() {
        
    }

	/**
	 * Retourne le script javascript à executer avant de réaliser la copie de fichier dans le répertoire définitif
	 * 
	 * @return le script à passer avant la mise à jour
	 */
	@XmlElement(required=false)
	public FileMetaData getPreUpdateScript() {
		return preUpdateScript;
	}

	/**
	 * <p>Définit le script javascript à executer avant d'éffectuer la mise à jour de l'application.</p>
	 * <p>Le processus de mise à jour bénéficiant des privilèges administrateur, les scripts bénéficie des mêmes privilèges,
	 * aussi il est nécessaire de s'assurer de la non-nocivité de ces scripts pour les utilisateurs.</p>
	 *  
	 * @param preUpdateScript le script à passer avant la mise à jour
	 */
	public void setPreUpdateScript(FileMetaData preUpdateScript) {
		this.preUpdateScript = preUpdateScript;
	}

	/**
	 * Retourne le script javascript à passer après l'opération de copie des fichiers. 
	 * 
	 * @return le script à passer après la mise à jour
	 */
	@XmlElement(required=false)
	public FileMetaData getPostUpdateScript() {
		return postUpdateScript;
	}

	/**
	 * Définit le script javascript à passer après l'opération de copie des fichiers.
	 * 
	 * @param postUpdateScript le script à passer après la mise à jour
	 */
	public void setPostUpdateScript(FileMetaData postUpdateScript) {
		this.postUpdateScript = postUpdateScript;
	}

	/**
	 * <p>Retourne les emplacement des clés symétrique nécessaire à l'exploitation de certains fichiers</p>
	 * <p>Pour dess raisons de sécurité évidente, les dépôts définissant des entrés pour ce champs devrait
	 * être accessible uniquement sur une connexion SSL avec authentification individualisé</p>
	 * 
	 * @return les emplacement des clés symétrique
	 */
	@XmlElementWrapper(name="cryptoKeys",required=true)
    @XmlElement(name="cryptoKeyURL")
	public List<FileMetaData> getCryptoKeys() {
		return cryptoKeys;
	}

	/**
	 * <p>Définit les emplacement des clés symétrique nécessaire à l'exploitation de certains fichiers</p>
	 * <p>Pour dess raisons de sécurité évidente, les dépôts définissant des entrés pour ce champs devrait
	 * être accessible uniquement sur une connexion SSL avec authentification individualisé</p>
	 * 
	 * @param cryptoKeys the cryptoKeys to set
	 */
	public void setCryptoKeys(List<FileMetaData> cryptoKeys) {
		this.cryptoKeys = cryptoKeys;
	}

	/**
	 * Retourne les métadonnées de l'ensemble des fichiers de la révision courante
	 * 
	 * @return les métadonnées de l'ensemble des fichiers de la révision courante
	 */
    @XmlElementWrapper(name="filesMetaData",required=true)
    @XmlElement(name="fileMetaData")
	public List<FileMetaData> getFilesMetaData() {
		return filesMetaData;
	}

	/**
	 * Définit les métadonnées de l'ensemble des fichiers de la révision courante
	 * 
	 * @param filesMetaData les métadonnées de l'ensemble des fichiers de la révision courante
	 */
	public void setFilesMetaData(List<FileMetaData> filesMetaData) {
		this.filesMetaData = filesMetaData;
	}

	/**
	 * Retourne l'historique de version se terminant par la version courante représenté par la révision.<br>
	 * La liste doit permettre la génération d'un changelog dynamique des changements survenue entre la version du client
	 * et la révision courante.
	 * 
	 * @return l'historique de version
	 */
	@XmlElementWrapper(name="versions", required=true)
	@XmlElement(name="version")
	public List<Version> getVersions() {
		return versions;
	}

	/**
	 * Définit l'historique de version se terminant par la version courante représenté par la révision.<br>
	 * La liste doit permettre la génération d'un changelog dynamique des changements survenue entre la version du client
	 * et la révision courante.
	 * 
	 * @param versions l'historique de version
	 */
	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}
	
	/**
	 * Retourne les métatdonnées associé au fichier dont le nom est fournit en paramètre
	 * 
	 * @param file le nom du fichier pour lequel retourner les métadonnées
	 * @return les métadonnées du fichier
	 */
	public FileMetaData getFileMetaData(String file) {
		for(FileMetaData fmd : filesMetaData) {
			if(fmd.getPath().equals(file))
				return fmd;
		}
		
		return null;
	}
}