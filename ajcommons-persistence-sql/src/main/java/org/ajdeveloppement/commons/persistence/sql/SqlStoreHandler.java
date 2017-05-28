/*
 * Créé le 20 nov. 2009 à 21:11:15 pour AjCommons (Bibliothèque de composant communs)
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
 * pris connaissance de la licence CeCILL-C, et que vous en avez accepté les
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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.WeakHashMap;

import org.ajdeveloppement.commons.persistence.AbstractStoreHandler;
import org.ajdeveloppement.commons.persistence.ObjectData;
import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlField;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlForeignKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlPrimaryKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlUnmappedFields;


/**
 * Implémentation permettant d'établir le binding de sauvegarde/suppression entre la couche objet
 * et la base de données
 *
 * @author Aurélien JEOFFRAY
 *
 * @param <T> the type of stored object
 */
public class SqlStoreHandler<T extends ObjectPersistence> extends AbstractStoreHandler<T> {

	private static ResourceBundle sqlRequestProperties = ResourceBundle.getBundle(
			SqlStoreHandler.class.getPackage().getName() + ".SqlRequest"); //$NON-NLS-1$

	private SqlContext context;
	private Class<T> persistantClass;
	
	private String insertQuery;
	private String updateQuery;
	private String deleteQuery;

	private WeakHashMap<Connection, PreparedStatement> pstmtsInsert = new WeakHashMap<>();
	private WeakHashMap<Connection, PreparedStatement> pstmtsUpdate = new WeakHashMap<>();
	private WeakHashMap<Connection, PreparedStatement> pstmtsDelete = new WeakHashMap<>();

	private String tableName;
	private String domain;
	private ContextDomain contextDomain;
	private List<String> tableFields = new ArrayList<String>();
	private List<String> primaryKeyFields = new ArrayList<String>();
	private String generatedFieldName = null;
	private int generatedFieldType;
	
	private boolean updateOnlyIfModified = true;

	/**
	 * Construct a new store handler for specified persitent class
	 * 
	 * @param cnx the SQL connexion to linked database
	 * @param persistantClass the persistent type
	 * @throws SQLException
	 */
	public SqlStoreHandler(SqlContext sqlContext, Class<T> persistantClass) throws SQLException {
		this.context = sqlContext;
		this.persistantClass = persistantClass;

		tableName = ReflectionTools.getTableName(persistantClass);
		domain = ReflectionTools.getTableDomain(persistantClass);
		contextDomain = SqlContext.getContextDomain(domain);

		identifyPrimaryKey();

		prepareUpdateQueries();

		prepareDeleteQuery();
	}

	/**
	 * Identifie les champs de la clé primaire
	 *
	 * @param persistantClass la class à partir de laquelle identifier la clé primaire
	 */
	private void identifyPrimaryKey() {
		if(persistantClass.isAnnotationPresent(SqlPrimaryKey.class)) {
			SqlPrimaryKey pk = persistantClass.getAnnotation(SqlPrimaryKey.class);
			primaryKeyFields.addAll(Arrays.asList(pk.fields()));
			if(!pk.generatedidField().name().isEmpty()) {
				generatedFieldName = pk.generatedidField().name();
				generatedFieldType = pk.generatedidField().type();
			}
		}
	}

	/**
	 * Prépare la requête de mise à jour
	 *
	 * @throws SQLException
	 */
	private void prepareUpdateQueries() {
		StringBuilder sqlField = new StringBuilder();
		StringBuilder sqlValues = new StringBuilder();
		StringBuilder sqlUpdateKeysValues = new StringBuilder();
		StringBuilder sqlUpdateWhereClause = new StringBuilder();

		for(Field field : persistantClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(SqlForeignKey.class)) {
				String[] fieldsName = ReflectionTools.getForeignKeyFieldsName(field);

				for(String fieldName : fieldsName) {
					if(!tableFields.contains(fieldName)) {
						sqlField.append(", ").append(fieldName); //$NON-NLS-1$
						sqlValues.append(", ?"); //$NON-NLS-1$
					
						sqlUpdateKeysValues.append(", " + fieldName + "=?"); //$NON-NLS-1$ //$NON-NLS-2$
	
						tableFields.add(fieldName);
					}
				}
			}
		}
		
		for(Field field : persistantClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(SqlField.class)) {
				SqlField sqlFieldAnnotation = field.getAnnotation(SqlField.class);
				String fieldName = ReflectionTools.getFieldName(field);

				if(!tableFields.contains(fieldName)) {
					sqlField.append(", ").append(fieldName); //$NON-NLS-1$
					if(sqlFieldAnnotation.saveModifier().isEmpty()) {
						sqlValues.append(", ?"); //$NON-NLS-1$
						
						sqlUpdateKeysValues.append(", " + fieldName + "=?"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						sqlValues.append(", " + sqlFieldAnnotation.saveModifier()); //$NON-NLS-1$
						sqlUpdateKeysValues.append(", " + fieldName + " = " + sqlFieldAnnotation.saveModifier()); //$NON-NLS-1$ //$NON-NLS-2$
					}

					tableFields.add(fieldName);
				}
			}
		}

		String[] foreignKeysFields = null;
		if(persistantClass.isAnnotationPresent(SqlUnmappedFields.class)) {
			foreignKeysFields = persistantClass.getAnnotation(SqlUnmappedFields.class).fields();

			for(String fkField : foreignKeysFields) {
				if(!tableFields.contains(fkField)) {
					sqlField.append(", ").append(fkField); //$NON-NLS-1$
					sqlValues.append(", ?"); //$NON-NLS-1$

					sqlUpdateKeysValues.append(", " + fkField + "=?"); //$NON-NLS-1$ //$NON-NLS-2$

					tableFields.add(fkField);
				}
			}
		}

		sqlField.replace(0, 2, ""); //$NON-NLS-1$
		sqlValues.replace(0, 2, ""); //$NON-NLS-1$

		sqlUpdateKeysValues.replace(0, 2, ""); //$NON-NLS-1$

		if(primaryKeyFields != null) {
			for(String pkField : primaryKeyFields)
				sqlUpdateWhereClause.append(" and ").append(pkField).append("=?"); //$NON-NLS-1$ //$NON-NLS-2$
			
			sqlUpdateWhereClause.replace(0, 5, ""); //$NON-NLS-1$
		}


		//TODO A voir pour support SQL Server
		// Ex: SQL Server:
		//
		//		MERGE tableName AS target
		//	    USING (sqlValues) AS source (sqlField)
		//	    ON (jointure=PK_FIELD)
		//	    WHEN MATCHED THEN
		//	        UPDATE SET F2 = source.F2
		//		WHEN NOT MATCHED THEN
		//		    INSERT (sqlField)
		//		    VALUES (source.F1, source.F2)

		//update MaTable set a=x, b=y where c=z
		
		String updateRequest = getUpdateRequest();
		String option = ""; //$NON-NLS-1$
		int posSeparatorOption=updateRequest.indexOf(';');
		if(posSeparatorOption > 0) {
			option = updateRequest.substring(posSeparatorOption+1).toLowerCase();
			updateRequest = updateRequest.substring(0, posSeparatorOption);
		}
		
		ContextDomain contextDomain = SqlContext.getContextDomain(domain);
		if(!option.equals("as insert")) { //$NON-NLS-1$
			updateQuery = String.format(updateRequest, tableName, sqlUpdateKeysValues.toString(), sqlUpdateWhereClause.toString());
			if(sqlUpdateWhereClause.toString().isEmpty()) //remove " where " pattern if there is no where clause 
				updateQuery = updateQuery.substring(0, updateQuery.length() - 7);
		}
		else
			updateQuery = String.format(updateRequest, tableName,  sqlField.toString(), sqlValues.toString());
		insertQuery = String.format(getInsertRequest(), tableName, sqlField.toString(), sqlValues.toString());
	}
	
	private PreparedStatement getUpdatePreparedStatement(Connection connection) throws SQLException {
		if(connection == null)
			return null;
		
		ContextDomain contextDomain = SqlContext.getContextDomain(domain);
		
		PreparedStatement pstmtUpdate = pstmtsUpdate.get(connection);
		
		if(pstmtUpdate == null || (contextDomain.isValidateConnectionBeforeUse()
				&& pstmtUpdate.getConnection().isValid(contextDomain.getTimoutValidation()))) {
			if(connection != null) {
				pstmtUpdate = connection.prepareStatement(updateQuery);
				
				pstmtsUpdate.put(connection, pstmtUpdate);
			}
		}
		
		return pstmtUpdate;
	}
	
	private PreparedStatement getInsertPreparedStatement(Connection connection) throws SQLException {
		if(connection == null)
			return null;
		
		ContextDomain contextDomain = SqlContext.getContextDomain(domain);
		
		PreparedStatement pstmtInsert = pstmtsInsert.get(connection);
		
		if(pstmtInsert == null || (contextDomain.isValidateConnectionBeforeUse()
				&& pstmtInsert.getConnection().isValid(contextDomain.getTimoutValidation()))) {
			if(connection != null) {
				int generatedKey = Statement.NO_GENERATED_KEYS;
				SqlPrimaryKey pkAnnotation = persistantClass.getAnnotation(SqlPrimaryKey.class);
				if(pkAnnotation != null && pkAnnotation.generatedidField() != null)
					generatedKey = Statement.RETURN_GENERATED_KEYS;
				pstmtInsert = connection.prepareStatement(insertQuery, generatedKey);
				
				pstmtsInsert.put(connection, pstmtInsert);
			}
		}
		
		return pstmtInsert;
	}

	/**
	 * prépare la requête de suppression
	 *
	 * @throws SQLException
	 */
	private void prepareDeleteQuery() throws SQLException {
		StringBuilder sqlFilter = new StringBuilder();

		if(primaryKeyFields != null) {
			for(String pkField : primaryKeyFields)
				sqlFilter.append(" and ").append(pkField).append("=?"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			sqlFilter.append("1 = 1"); //$NON-NLS-1$
		}

		sqlFilter.replace(0, 5, ""); //$NON-NLS-1$
		
		deleteQuery = String.format(getDeleteRequest(), tableName, sqlFilter.toString());
	}
	
	private PreparedStatement getDeletePreparedStatement(Connection connection) throws SQLException {
		if(connection == null)
			return null;
		
		ContextDomain contextDomain = SqlContext.getContextDomain(domain);
		
		PreparedStatement pstmtDelete = pstmtsDelete.get(connection);
		if(pstmtDelete == null || (contextDomain.isValidateConnectionBeforeUse()
				&& pstmtDelete.getConnection().isValid(contextDomain.getTimoutValidation()))) {
			if(connection != null) {
				pstmtDelete = connection.prepareStatement(deleteQuery);
				
				pstmtsDelete.put(connection, pstmtDelete);
			}
		}
		
		return pstmtDelete;
	}

	/**
	 * Retourne la requête utilisé pour l'insertion
	 *
	 * @return la requête utilisé pour l'insertion
	 */
	private String getInsertRequest() {
		try {
			if(contextDomain != null)
				return sqlRequestProperties.getString("insert." + contextDomain.getPersistenceDialect().toLowerCase()); //$NON-NLS-1$
		} catch(MissingResourceException e) { }

		return sqlRequestProperties.getString("insert.default"); //$NON-NLS-1$
	}

	/**
	 * Retourne la requête utilisé pour l'insertion/mise à jour
	 *
	 * @return la requête utilisé pour l'insertion/mise à jour
	 */
	private String getUpdateRequest() {
		try {
			if(contextDomain != null) {
				String updateTemplateQuery = sqlRequestProperties.getString("update." + contextDomain.getPersistenceDialect().toLowerCase());  //$NON-NLS-1$
				return updateTemplateQuery;
			}
		} catch(MissingResourceException e) { }

		return sqlRequestProperties.getString("update.default"); //$NON-NLS-1$
	}

	/**
	 * Retourne la requête utilisé pour la suppression
	 *
	 * @return la requête utilisé pour la suppression
	 */
	private String getDeleteRequest() {
		try {
			if(contextDomain != null)
				return sqlRequestProperties.getString("delete." + contextDomain.getPersistenceDialect().toLowerCase()); //$NON-NLS-1$
		} catch(MissingResourceException e) { }

		return sqlRequestProperties.getString("delete.default"); //$NON-NLS-1$
	}

	/**
	 * Le ResourceBundle définissant les requêtes utilisées pour les différentes opération.<br>
	 * Voir documentation de l'API de persistance pour plus d'information
	 *
	 * @param requestResourceBundle Le ResourceBundle définissant les requêtes utilisées
	 */
	public static void setRequestResourceBundle(ResourceBundle requestResourceBundle) {
		sqlRequestProperties = requestResourceBundle;
	}

	/**
	 * @param updateOnlyIfModified the updateOnlyIfModified to set
	 */
	public void setUpdateOnlyIfModified(boolean updateOnlyIfModified) {
		this.updateOnlyIfModified = updateOnlyIfModified;
	}

	/**
	 * @return the updateOnlyIfModified
	 */
	public boolean isUpdateOnlyIfModified() {
		return updateOnlyIfModified;
	}

	/* (non-Javadoc)
	 * @see org.ajdeveloppement.commons.persistance.AbstractStoreHandler#parseObject(java.lang.Object)
	 */
	@Override
	public ObjectData parseObject(T obj) throws ObjectPersistenceException{
		return new QueryData<T>(obj, persistantClass, context);
	}

	@Override
	public Object executeUpdate(ObjectData datas) throws ObjectPersistenceException {		
		Object generatedKey = null;
		if(!updateOnlyIfModified || datas.isModified()) {
			try {
				Connection connection = context.getConnectionForDomain(domain);
				
				context.openTransactionForConnection(connection);
				
				PreparedStatement pstmtUpdate = getUpdatePreparedStatement(connection);
				PreparedStatement pstmtInsert = getInsertPreparedStatement(connection);

				int parameterIndex = 1;
				for(String field : tableFields) {
					if(generatedFieldName != null && field.equals(generatedFieldName)
							&& datas.getValues().get(field) == null) {
						if(datas.getGeneratedField().getType().isAssignableFrom(UUID.class)) {
							UUID newUUID = UUID.randomUUID();
							
							pstmtUpdate.setObject(parameterIndex, newUUID);
							pstmtInsert.setObject(parameterIndex++, newUUID);
							
							generatedKey = newUUID;
						} else {
							pstmtUpdate.setNull(parameterIndex, Types.TINYINT);
							pstmtInsert.setNull(parameterIndex++, Types.TINYINT);
						}
					} else {
						pstmtUpdate.setObject(parameterIndex, datas.getValues().get(field));
						pstmtInsert.setObject(parameterIndex++, datas.getValues().get(field));
					}
				}
				String updateRequest = getUpdateRequest();
				String option = ""; //$NON-NLS-1$
				int posSeparatorOption=updateRequest.indexOf(';');
				if(posSeparatorOption > 0) {
					option = updateRequest.substring(posSeparatorOption+1).toLowerCase();
				}
				
				if((contextDomain == null || !option.contains("as insert")) //$NON-NLS-1$
						&& (generatedFieldName == null || datas.getValues().get(generatedFieldName) != null)) {
					if(primaryKeyFields != null) {
						for(String pkField : primaryKeyFields) {
							pstmtUpdate.setObject(parameterIndex++, datas.getValues().get(pkField));
						}
					}
				}
				
				boolean updateRequestExecuted = true;
				if(generatedFieldName != null && datas.getValues().get(generatedFieldName) == null) {
					updateRequestExecuted = false;
					pstmtInsert.executeUpdate();
				} else {
					if(pstmtUpdate.executeUpdate() == 0) {
						updateRequestExecuted = false;
						pstmtInsert.executeUpdate();
					}
				}
				
				ObjectPersitenceState.calculateHash(datas.getSourceObject(), persistantClass, datas.getValues(), true, context);
	
				if(generatedFieldName != null) {
					if(datas.getValues().get(generatedFieldName) == null && !datas.getGeneratedField().getDeclaringClass().equals(UUID.class)) {
						ResultSet clefs = null;
						if(updateRequestExecuted)
							clefs = pstmtUpdate.getGeneratedKeys();
						else
							clefs = pstmtInsert.getGeneratedKeys();
						try {
							if (clefs.first()) {
								generatedKey = clefs.getObject(1);
								if(generatedKey instanceof Long) {
									long id = (Long)generatedKey;
									if(id == 0)
										generatedKey = null;
									else {
										switch(generatedFieldType) {
											case Types.INTEGER:
												return (int)id;
											case Types.SMALLINT:
												return (short)id;
											case Types.TINYINT:
												return (byte)id;
										}
									}
								}
							}
						} finally {
							clefs.close();
						}
					}
				}
			} catch (SQLException e) {
				throw new ObjectPersistenceException(e);
			}
		}

		return generatedKey;
	}

	@Override
	public void executeDelete(ObjectData datas) throws ObjectPersistenceException {
		try {
			Connection connection = context.getConnectionForDomain(domain);
			
			context.openTransactionForConnection(connection);
			
			PreparedStatement pstmtDelete = getDeletePreparedStatement(connection);
			
			int parameterIndex = 1;
			
			if(primaryKeyFields != null) {
				for(String field : primaryKeyFields) {
					pstmtDelete.setObject(parameterIndex++, datas.getObjectIdValues().get(field));
				}
			}
			pstmtDelete.executeUpdate();
			
			ObjectPersitenceState.setDeleted(datas.getSourceObject(), true, context);
		} catch (SQLException e) {
			throw new ObjectPersistenceException(e);
		}
	}
}
