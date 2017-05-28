/*
 * Créé le 12 juin 2010 à 17:08:14 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2010 - Aurélien JEOFFRAY
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
package org.ajdeveloppement.commons.persistence.sql;

import java.sql.SQLException;

import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * Utilitaire permettant la gestion d'une session de sauvegarde/suppression sur SQL.
 * Initialise une transaction et execute l'ensemble de la session de sauvegarde dans la
 * transaction
 *
 * @author Aurelien JEOFFRAY
 *
 */
public class SessionHelper {

	private static void startSaveOrDeleteSession(SqlContext context, ObjectPersistence object, boolean save)
			throws ObjectPersistenceException {
		synchronized (context) {
			try {
				try(SqlSession session = new SqlSession(context)) {
					if(save)
						object.save(session);
					else
						object.delete(session);
				}

				context.commitOpenTransactions(); 
			} catch (Exception e) {
				try { 
					context.rollbackAllOpenTransactions();
				} catch(SQLException e2) { e.addSuppressed(e2); }

				if(e instanceof ObjectPersistenceException)
					throw (ObjectPersistenceException)e;

				throw new ObjectPersistenceException(e);
			}
		}
	}
	
	/**
	 * Démarre une session de sauvegarde
	 *
	 * @param object l'objet à sauvegarder
	 * @throws ObjectPersistenceException
	 */
	public static void startSaveSession(ObjectPersistence object) throws ObjectPersistenceException {
		startSaveOrDeleteSession(SqlContext.getDefaultContext(), object, true);
	}

	/**
	 * Démarre une session de suppression
	 * 
	 * @param object l'objet à supprimer de la base de données
	 * @throws ObjectPersistenceException
	 */
	public static void startDeleteSession(ObjectPersistence object) throws ObjectPersistenceException {
		startSaveOrDeleteSession(SqlContext.getDefaultContext(), object, false);
	}

	/**
	 * Démarre une session de sauvegarde
	 *
	 * @param context le context dans lequel effectuer l'operation de sauvergarde
	 * @param object l'objet à sauvegarder
	 * @throws ObjectPersistenceException
	 */
	public static void startSaveSession(SqlContext context, ObjectPersistence object) throws ObjectPersistenceException {
		startSaveOrDeleteSession(context, object, true);
	}

	/**
	 * Démarre une session de suppression
	 * 
	 * @param context le context dans lequel effectuer l'operation de suppression
	 * @param object l'objet à supprimer de la base de données
	 * @throws ObjectPersistenceException
	 */
	public static void startDeleteSession(SqlContext context, ObjectPersistence object) throws ObjectPersistenceException {
		startSaveOrDeleteSession(context, object, false);
	}
}
