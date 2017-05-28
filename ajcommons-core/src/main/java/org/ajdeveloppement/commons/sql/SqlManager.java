/*
 * Créé le 29 oct. 07 à 12:25:27 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.sql;

import java.io.File;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestionnaire simplifiant l'éxecution de scripts et requêtes SQL
 * sur une connection active
 *
 * @author Aurélien JEOFFRAY
 * @version 0.1
 *
 */
public class SqlManager {
	
	private Connection connection;
	private File scriptpath;
	
	/**
	 * Construit un gestionnaire SQL sur la connexion fourni en paramètre
	 * 
	 * @param connection la connexion SQL servant de base à l'execution de requêtes
	 * @param scriptpath répertoire par défaut stockant les fichiers de scripts SQL. Peut être null
	 */
	public SqlManager(Connection connection, File scriptpath) {
		this.connection = connection;
		this.scriptpath = scriptpath;
	}
	
	/**
	 * <p>
	 * Execute un script SQL stocké dans un fichier externe.<br>
	 * Le chemin peut être absolue ou relatif au répertoire de script définit à la construction.
	 * Si aucun répertoire de script n'est défini à la construction, alors le répertoire courant sera utilisé.
	 * </p>
	 * <p>
	 * Attention. Si la requête réussi (aucune erreur SQL), la méthode ne retourne aucun résultat.
	 * </p>
	 * @param filename le nom du fichier de scripts à executer
	 * @throws SQLException
	 */
	public void executeScript(String filename) throws SQLException {
		executeScript(filename, false);
	}
	
	/**
	 * <p>
	 * Execute un script SQL stocké dans un fichier externe.<br>
	 * Le chemin peut être absolue ou relatif au répertoire de script définit à la construction.
	 * Si aucun répertoire de script n'est défini à la construction, alors le répertoire courant sera utilisé.
	 * </p>
	 * <p>
	 * Si <code>inTransaction</code> est à true,le script est éxécuté dans une transaction et est annulé en
	 * totalité en cas d'exception.
	 * </p>
	 * <p>
	 * Attention. Si la requête réussi (aucune erreur SQL), la méthode ne retourne aucun résultat.
	 * </p>
	 * @param filename le nom du fichier de scripts à executer
	 * @param inTransaction si <code>true</code>, execute le script dans une transaction
	 * @throws SQLException
	 */
	public void executeScript(String filename, boolean inTransaction) throws SQLException {
		Statement statement = connection.createStatement();
		File sqlfile = new File(scriptpath, filename);
		
		boolean inititialAutoCommitConnectionMode = connection.getAutoCommit();
		
		if(inTransaction)
			connection.setAutoCommit(false);
		
		try {
			statement.clearBatch();
			
			SqlParser.createBatch(sqlfile, statement, null);
			
			statement.executeBatch();
			statement.clearBatch();
			
			if(inTransaction)
				connection.commit();
		} catch(BatchUpdateException e) {
			if(inTransaction)
				connection.rollback();
			
			e.printStackTrace();
			SQLException sqle;
			while((sqle = e.getNextException()) != null) {
				sqle.printStackTrace();
			}
			
			throw e;
		} catch(SQLException e) {
			if(inTransaction)
				connection.rollback();
			
			throw e;
		} finally {	
			if(inTransaction)
				connection.setAutoCommit(inititialAutoCommitConnectionMode);
		}
	}
	
	/**
	 * Exécute une requête SQL retournant un résultat
	 * 
	 * @param sql la requête de type SELECT à executer
	 * @return le resultset contenant les résultats de la requête
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		Statement statement = connection.createStatement();
		return statement.executeQuery(sql);
	}
	
	/**
	 * Exécute une requête de mise à jour (UPDATE, DELETE, CREATE, ALTER, ...) de la base.
	 * 
	 * @param sql la requête à executer
	 * @throws SQLException
	 */
	public void executeUpdate(String sql) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
	}
}
