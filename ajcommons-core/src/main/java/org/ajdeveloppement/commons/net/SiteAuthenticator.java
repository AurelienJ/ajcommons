/*
 * Créé le 27 mai 2009 pour ajcommons
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
package org.ajdeveloppement.commons.net;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

/**
 * Fournit les informations d'authentification requises pour la connexion
 * à un site donnés
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class SiteAuthenticator extends Authenticator {
	
	protected Map<String, PasswordAuthentication> authentications = new HashMap<String, PasswordAuthentication>();

	/**
	 * Construit la chaîne représentant un site d'authentification à partir des informations
	 * fournit en paramètre.
	 * 
	 * @param protocol le protocole utilisé pour accéder à la ressource
	 * @param host la machine où se trouve la ressource
	 * @param port le port d'accès à la ressource
	 * @param realm le prompt de requête d'authentification 
	 * @return le site d'authentification sous la forme "protocol://host:port (realm)"
	 */
	@SuppressWarnings("nls")
	public static String getSiteForURL(String protocol, String host, int port, String realm) {
		return protocol + "://" + host + (port > -1 ? ":" + port : "") + " (" + realm + ")";
	}
	
	/**
	 * Ajoute un site à la liste des authentifications
	 * 
	 * @param site le site d'authentification
	 * @param passwordAuthentication les information d'authentifications
	 */
	public void addAuthentication(String site, PasswordAuthentication passwordAuthentication) {
		authentications.put(site, passwordAuthentication);
	}
	
	/**
	 * Retire un site des authentifications
	 * 
	 * @param site le site d'authentification à retirer
	 */
	public void removeAuthentication(String site) {
		authentications.remove(site);
	}
	
	/**
	 * <p>Retourne les informations d'authentification associé au site correspondant à la requête ou
	 * null si aucune authentification pour le site.</p>
	 * <p>Une authentification pour un site peut être ajouté avec {@link #addAuthentication(String, PasswordAuthentication)}</p>
	 */
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return authentications.get(getSiteForURL(getRequestingProtocol(), getRequestingHost(), getRequestingPort(), 
				getRequestorType() == RequestorType.PROXY ? "Proxy" : getRequestingPrompt())); //$NON-NLS-1$
	}
}
