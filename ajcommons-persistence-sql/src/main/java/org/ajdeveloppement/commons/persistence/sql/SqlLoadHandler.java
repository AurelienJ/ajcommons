/*
 * Créé le 10 janv. 2010 à 14:23:33 pour AjCommons (Bibliothèque de composant communs)
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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import org.ajdeveloppement.commons.persistence.AbstractLoadHandler;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * Implémentation de désérilisation permettant d'extraire les données stocké en base de
 * données.
 * 
 * @author Aurélien JEOFFRAY
 *
 * @param <T> Le type de l'élément à charger
 */
@Deprecated
public class SqlLoadHandler<T> extends AbstractSqlLoadHandler<T, Map<String, Object>> {

	private static ResourceBundle sqlRequestProperties = ResourceBundle.getBundle(
			SqlLoadHandler.class.getPackage().getName() + ".SqlRequest"); //$NON-NLS-1$
	
	private SqlContext context;
	private String selectQuery;
	private WeakHashMap<Connection, PreparedStatement> pstmtsSelect = new WeakHashMap<>();
	private List<String> primaryKeyFields = new ArrayList<String>();
	
	/**
	 * Initialise le handler
	 * 
	 * @param cnx connexion sql vers les données
	 * @param persistentClass la class à binder
	 * @throws ObjectPersistenceException
	 */
	public SqlLoadHandler(SqlContext context, Class<?> persistentClass) throws ObjectPersistenceException {
		this.context = context;
		this.persistentClass = ReflectionTools.getFirstPersistentClass(persistentClass);
		if(this.persistentClass == null)
			throw new ObjectPersistenceException("there are no persistent class"); //$NON-NLS-1$
		
		analysePersistentClass();
		
		prepareSelectQuery();
		
		if(this.persistentClass.getSuperclass() != null && this.persistentClass.getSuperclass() != Object.class) {
			Class<?> parentPersistentClass = ReflectionTools.getFirstPersistentClass(this.persistentClass.getSuperclass());
			if(parentPersistentClass != null)
				childSqlLoadHandler = new SqlLoadHandler<T>(context, parentPersistentClass);
		}
	}
	
	private String getSelectRequest() {
		try {
			ContextDomain contextDomain = SqlContext.getContextDomain(domain);
			return sqlRequestProperties.getString("select." + contextDomain.getPersistenceDialect().toLowerCase()); //$NON-NLS-1$
		} catch(MissingResourceException e) { }
		
		return sqlRequestProperties.getString("select.default"); //$NON-NLS-1$
	}
	
	private void prepareSelectQuery() {
		String sql = getSelectRequest();

		StringBuilder sqlFields = new StringBuilder();
		StringBuilder sqlForeignFields = new StringBuilder();
		
		for(Field field : tableFields) {
			String fieldName = ReflectionTools.getFieldName(field);
			
			if(sqlFields.length() > 0)
				sqlFields.append(", "); //$NON-NLS-1$
			sqlFields.append(fieldName);
		}
		
		for(String fieldName : foreignFieldsNames) {
			if(sqlForeignFields.length() > 0)
				sqlForeignFields.append(", "); //$NON-NLS-1$
			sqlForeignFields.append(fieldName);
		}
		
		if(sqlForeignFields.length() > 0) {
			if(sqlFields.length() > 0)
				sqlFields.append(", "); //$NON-NLS-1$
			sqlFields.append(sqlForeignFields.toString());
		}
		
		StringBuilder sqlPKFields = new StringBuilder();
		
		String[] pkFields = ReflectionTools.getPrimaryKeyFieldsName(persistentClass);
		if(pkFields != null) {
			primaryKeyFields.addAll(Arrays.asList(pkFields));
			
			for(String pkField : pkFields) {
				if(sqlPKFields.length() > 0)
					sqlPKFields.append(" and "); //$NON-NLS-1$
				else
					sqlPKFields.append("where "); //$NON-NLS-1$
				sqlPKFields.append(pkField);
				sqlPKFields.append(" = ?"); //$NON-NLS-1$
			}
		}
		
		selectQuery = String.format(sql, sqlFields.toString(), tableName, sqlPKFields.toString());
	}
	
	private PreparedStatement getSelectPreparedStatement(Connection connection) throws SQLException {
		ContextDomain contextDomain = SqlContext.getContextDomain(domain);
		
		PreparedStatement pstmtSelect = pstmtsSelect.get(connection);
		if(pstmtSelect == null || (contextDomain.isValidateConnectionBeforeUse() 
				&& !pstmtSelect.getConnection().isValid(contextDomain.getTimoutValidation()))) {
			
			pstmtSelect = connection.prepareStatement(selectQuery,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			
			pstmtsSelect.put(connection, pstmtSelect);
		}
		
		return pstmtSelect;
	}

	/**
	 * @see AbstractLoadHandler#fill(Object, Object)
	 */
	@Override
	public Map<Class<?>, Map<String, Object>> fill(T obj,
			Map<String, Object> keyValues) throws ObjectPersistenceException {
		//Analyse de l'objet
		QueryData<T> qdata = new QueryData<T>(obj, persistentClass, true, true, context);
		//Récupération des valeurs des propriétés perstante de l'objet
		Map<String, Object> objectIdValues =  new HashMap<String, Object>(qdata.getObjectIdValues());
		try {
			//injection des valeurs définit par l'utilisateur au fill (écrase éventuellement les valeurs issue de l'objet) 
			if(keyValues != null)
				objectIdValues.putAll(keyValues);
		} catch (Exception e) {
			throw new ObjectPersistenceException(e);
		}
		
		try {
			Connection connection = context.getConnectionForDomain(domain);
			synchronized (connection) {
				PreparedStatement pstmt = getSelectPreparedStatement(connection);
				
				//Avec les valeurs issue de l'objet ou fournit en paramêtre, initialise la requete avec les champs de la clé primaire
				for(int i = 0; i < primaryKeyFields.size(); i++) {
					if(objectIdValues.containsKey(primaryKeyFields.get(i))) {
						Object value = objectIdValues.get(primaryKeyFields.get(i));
						pstmt.setObject(i+1, value);
					} else {
						throw new ObjectPersistenceException(primaryKeyFields.get(i) + " identifier field value is missing"); //$NON-NLS-1$
					}
				}
				
				try(ResultSet rs = pstmt.executeQuery()) {
					if(!rs.isClosed() && rs.next()) {
						return super.fill(obj, keyValues, rs, context);
					}
				}
			}
		} catch(SQLException e) {
			throw new ObjectPersistenceException(e);
		}
		
		return null;
	}
}
