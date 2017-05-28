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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Représente l'évenement de mise à jour à transmettre aux auditeurs
 * à l'écoute du service de mise à jour {@link AjUpdater}
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 *
 */
public class AjUpdaterEvent {
	
	/**
	 * Status du service de mise à jour
	 */
	public enum Status {
		/**
		 * Le service est hors ligne, aucune connexion n'a put être établit
		 * avec le dépôt.
		 */
		OFFLINE,
		/**
		 * La connexion vers le dépôt passe par un serveur mandataire (proxy)
		 * qui nécessite une authentification.<br>
		 * Aucune connexion n'a put être établit avec le dépôt.
		 */
		PROXY_AUTH_REQUIRED,
		/**
		 * Le service est correctement connecté au dépôts, le fichier révision
		 * est en cours de téléchargement
		 */
		//CONNECTED,
		/**
		 * La connexion avec le dépôts à été interrompu en cours de route,
		 * la mise à jour ne s'est pas effectué correctement
		 */
		CONNECTION_INTERRUPTED,
		/**
		 * Des mises à jour sont disponible sur le dépôts et prête à être téléchargé
		 */
		UPDATE_AVAILABLE,
		/**
		 * L'application locale est déjà à jour, aucune mise à jour n'est à télécharger
		 */
		NO_UPDATE_AVAILABLE,
		/**
		 * Les fichiers de mises à jour ont tous été téléchargé avec succès. Il sont disponible
		 * pour intégration dans l'application
		 */
		FILES_DOWNLOADED,
		/**
		 * Une erreur s'est produite durant le téléchargement des fichiers de mises à jour.<br>
		 * Afin d'éviter une mise à jour partiel pouvant provoqué une instabilité, les fichiers
		 * qui ont été téléchargé ne doivent pas être mis en place dans ces conditions.
		 */
		FILE_ERROR
	}
	
	private Status status;
	private Map<Repository, List<FileMetaData>> updateFiles;
	

	/**
	 * Construit un événement avec le status de service fournit en paramètre
	 * 
	 * @param status le status de service constituant l'évenement
	 */
	public AjUpdaterEvent(Status status) {
		this.status = status;
	}
	
	/**
	 * Construit un événement avec le status de service fournit en paramètre, 
	 * ainsi que la liste des fichiers identifier comme devant être mise à jour
	 * 
	 * @param status le status de mise à jour
	 * @param updateFiles la collection de fichier de mise à jour
	 */
	public AjUpdaterEvent(Status status, Map<Repository, List<FileMetaData>> updateFiles) {
		this.status = status;
		this.updateFiles = updateFiles;
	}
	
	/**
	 * Retourne le status de l'évenement
	 * 
	 * @return le status de l'évenement
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Définit le status de l'évenement
	 * 
	 * @param status le status de l'évenement
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Renvoie la liste des fichiers disponible en mise à jour
	 * 
	 * @return the updateFiles la liste des fichiers disponible en mise à jour
	 */
	public Map<Repository, List<FileMetaData>> getUpdateFiles() {
		return updateFiles;
	}

	/**
	 * Définit la liste des fichiers disponible en mise à jour
	 * 
	 * @param updateFiles la liste des fichiers disponible en mise à jour
	 */
	public void setUpdateFiles(Hashtable<Repository, List<FileMetaData>> updateFiles) {
		this.updateFiles = updateFiles;
	}
	
	
}
