/*
 * Créé le 23 juil. 2012 à 11:52:22 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2012 - Aurélien JEOFFRAY
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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import org.ajdeveloppement.commons.UncheckedException;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlSubTables;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlTable;

/**
 * Permet d'instancier et filtrer des éléments ou liste d'élément à partir de la base de données
 * 
 * @author Aurélien JEOFFRAY
 * 
 * @param <T> le type des éléments retourné par le jeux de résultat
 * @param <K> le type de la ressource permettant la construction des éléments de la collection.
 * <code>Void</code> si la ressource n'est pas nécessaire
 */
public class QResults<T,K> implements Iterable<T>, Selectable {
	
	private static ResourceBundle sqlRequestProperties = ResourceBundle.getBundle(
			SqlStoreHandler.class.getPackage().getName() + ".SqlRequest"); //$NON-NLS-1$
	private static String[] limitGrammar;
	private static String[] limitOffsetGrammar;
	
	private static WeakHashMap<Connection, Map<String, PreparedStatement>> pstmts = new WeakHashMap<>();
	
	// [start] Attributs de class
	private QFilter filter;
	private Class<?> persistentClass;
	private Class<T> returnType;
	private QField<?>[] orderFields = null;
	private QField<?>[] groupByFields = null;
	private QField<?>[] selectFields = null;
	private boolean distinct = false;
	private long nbRowsLimit = -1;
	private long startOffsetLimit = -1;
	private String tableName = null;
	private String tableAlias = null;
	private List<JoinParams> joinClause = null;
	private K binderRessourcesMap;
	private SqlLoadingSessionCache sessionCache;
	private SqlContext context;
	
	private ResultSetRowToObjectBinder<T,K> binder = null;

	private int nbRows = -1;
	// [end]
	
	// [start] Constructeurs
	/**
	 * Private constructor to force usage of {#link {@link #from(Class)} method call for instanciation.
	 * 
	 * @param filter An sql filter to apply on current QResults instance
	 * @param returnType 
	 * @param orderFields
	 * @param groupByFields
	 * @param selectFields
	 * @param distinct
	 * @param nbRowsLimit
	 * @param tableAlias
	 * @param joinClause
	 * @param binder
	 * @param binderRessourcesMap
	 * @param sessionCache cache de session
	 */
	private QResults(QFilter filter, 
			Class<T> returnType, 
			Class<?> persistentType,
			QField<?>[] orderFields, 
			QField<?>[] groupByFields, 
			QField<?>[] selectFields, 
			boolean distinct,
			long nbRowsLimit,
			long startOffsetLimit,
			String tableAlias, 
			List<JoinParams> joinClause,
			ResultSetRowToObjectBinder<T,K> binder,
			K binderRessourcesMap,
			SqlLoadingSessionCache sessionCache) {
		this.persistentClass = ReflectionTools.getFirstPersistentClass(persistentType);
		
		this.filter = filter;
		this.returnType = returnType;
		if(this.persistentClass != null)
			this.tableName = ReflectionTools.getTableName(persistentClass);
		this.tableAlias = tableAlias;
		this.orderFields = orderFields;
		this.groupByFields = groupByFields;
		this.selectFields = selectFields;
		this.distinct = distinct;
		this.nbRowsLimit = nbRowsLimit;
		this.startOffsetLimit = startOffsetLimit;
		this.joinClause = joinClause;
		this.binderRessourcesMap = binderRessourcesMap;
		this.sessionCache = sessionCache;
		this.context = SqlContext.getDefaultContext();
		
		if(binder == null)
			binder = ResultSetRowToObjectBinderFactory.<T,K>getBinder(returnType);
		
		this.binder = binder;
		
		if(joinClause == null) {
			discoverInheritJoinClause(persistentType, tableAlias);
			discoverSubTypeJoinClause(persistentType, tableAlias);
		}
	}
	// [end]

	// [start] Méthodes privées
	
	/**
	 * If current persistent type have a superclass that already persistent, add to query the
	 * "inner join" clause necessary to build object.
	 * 
	 * @param persistentType
	 * @param alias
	 */
	private void discoverInheritJoinClause(Class<?> persistentType, String alias) {
		if(persistentType.getSuperclass() != Object.class) {
			//Get, if exists, the parent persistent type
			Class<?> parent = ReflectionTools.getFirstPersistentClass(persistentType.getSuperclass());
			if(parent != null) {
				String parentTableName = ReflectionTools.getTableName(parent);
				String currentTableName = ReflectionTools.getTableName(persistentType);
				String[] pkParent = ReflectionTools.getPrimaryKeyFieldsName(parent);
				String[] pkCurrent = ReflectionTools.getPrimaryKeyFieldsName(persistentType);
				
				if(pkParent != null && pkCurrent != null && pkParent.length == pkCurrent.length) {
					QFilter joinFilter = null;
					for(int i = 0; i < pkParent.length; i++) {
						QField<?> qFieldParent = new QField<T>(parentTableName, pkParent[i]);
						QField<?> qFieldCurrent = new QField<T>(currentTableName, pkCurrent[i], alias);
						
						QFilter expression = qFieldCurrent.equalTo(qFieldParent);
						if(joinFilter == null)
							joinFilter = expression;
						else
							joinFilter.and(expression);
					}
					
					JoinParams joinParam = new JoinParams(ReflectionTools.getTableName(parent), null, joinFilter, parent);
					if(joinClause == null)
						joinClause = new ArrayList<>();
					joinClause.add(joinParam);
					
					discoverInheritJoinClause(parent, null);
				}
			}
		}
	}
	
	private void discoverSubTypeJoinClause(Class<?> persistentType, String alias) {
		SqlSubTables subTables = persistentType.getAnnotation(SqlSubTables.class);
		if(subTables != null) {
			//Get, child persistent types
			for(Class<?> subType : subTables.value()) {
				Class<?> child = ReflectionTools.getFirstPersistentClass(subType);
				if(child != null && child != persistentType) {
					String childTableName = ReflectionTools.getTableName(child);
					String currentTableName = ReflectionTools.getTableName(persistentType);
					String[] pkChild = ReflectionTools.getPrimaryKeyFieldsName(child);
					String[] pkCurrent = ReflectionTools.getPrimaryKeyFieldsName(persistentType);
					
					if(pkChild != null && pkCurrent != null && pkChild.length == pkCurrent.length) {
						QFilter joinFilter = null;
						
						for(int i = 0; i < pkChild.length; i++) {
							QField<?> qFieldChild = new QField<T>(childTableName, pkChild[i]);
							QField<?> qFieldCurrent = new QField<T>(currentTableName, pkCurrent[i], alias);
							
							QFilter expression = qFieldCurrent.equalTo(qFieldChild);
							if(joinFilter == null)
								joinFilter = expression;
							else
								joinFilter.and(expression);
						}
						
						JoinParams joinParam = new JoinParams(ReflectionTools.getTableName(child), null, joinFilter, child);
						joinParam.setType("left"); //$NON-NLS-1$
						if(joinClause == null)
							joinClause = new ArrayList<>();
						joinClause.add(joinParam);
						
						discoverSubTypeJoinClause(child, null);
					}
				}
			}
		}
	}
	
	private List<Object> getQueryParams() {
		List<Object> queryParams = new ArrayList<>();
		int paramIndex = 1;
		if(joinClause != null && joinClause.size() > 0) {
			for(JoinParams joinParams : joinClause) {
				for(Object param : joinParams.getJoinFilter().getParams()) {
					queryParams.add(param);
				}
			}
		}
		if(filter != null && filter.getParams().size() > 0) {
			for(Object param : filter.getParams()) {
				queryParams.add(param);
			}
		}
		
		return queryParams;
	}
	
	private String[] getlimitRequest() {
		if(limitGrammar == null) {
			String limitProperty= ""; //$NON-NLS-1$
			try {
				String domain = ReflectionTools.getTableDomain(persistentClass);
				ContextDomain contextDomain = SqlContext.getContextDomains().get(domain);
				if(contextDomain == null)
					contextDomain = SqlContext.getContextDomains().get(SqlContext.DEFAULT_DOMAIN);
				
				limitProperty = sqlRequestProperties.getString("limit." + contextDomain.getPersistenceDialect().toLowerCase()); //$NON-NLS-1$
			} catch(MissingResourceException e) { }
	
			if(limitProperty.isEmpty())
				limitProperty = sqlRequestProperties.getString("limit.default"); //$NON-NLS-1$
			
			limitGrammar = limitProperty.split(";"); //$NON-NLS-1$
		}
		return limitGrammar;
	}
	
	private String[] getlimitOffsetRequest() {
		if(limitOffsetGrammar == null) {
			String limitOffsetProperty= ""; //$NON-NLS-1$
			try {
				String domain = ReflectionTools.getTableDomain(persistentClass);
				ContextDomain contextDomain = SqlContext.getContextDomains().get(domain);
				if(contextDomain == null)
					contextDomain = SqlContext.getContextDomains().get(SqlContext.DEFAULT_DOMAIN);
				
				limitOffsetProperty = sqlRequestProperties.getString("skip." + contextDomain.getPersistenceDialect().toLowerCase()); //$NON-NLS-1$
			} catch(MissingResourceException e) { }
	
			if(limitOffsetProperty.isEmpty())
				limitOffsetProperty = sqlRequestProperties.getString("skip.default"); //$NON-NLS-1$
			
			limitOffsetGrammar = limitOffsetProperty.split(";"); //$NON-NLS-1$
		}
		return limitOffsetGrammar;
	}
	
	@SuppressWarnings("nls")
	private String buildQueryString(boolean countQuery) {
		String[] limitRequest = getlimitRequest();
		String[] limitOffsetRequest = getlimitOffsetRequest();
		
		String query = "select ";  
		if(countQuery)
			query += "count("; 
		else if(nbRowsLimit > -1 && limitRequest[1].equals("before select")) {
			query += String.format(limitRequest[0], nbRowsLimit) + " ";
			
			if(startOffsetLimit > -1 && limitOffsetRequest[1].equals("before select"))
				query += String.format(limitOffsetRequest[0], nbRowsLimit) + " ";
		}
		
		if(distinct) {
			query += "distinct ";
		}
		
		if(selectFields != null) {
			StringBuilder sb = new StringBuilder();
			for(QField<?> value : selectFields) {
				if(sb.length() > 0)
					sb.append(",");
				sb.append(value.getFullyQualifiedNameWithAlias());
			}
			query += sb.toString();
		} else {
			if(countQuery)
				query += "*";
			else {
				List<String> tableColumns = ReflectionTools.getTableColumns(persistentClass); //Arrays.asList(ReflectionTools.getPrimaryKeyFieldsName(persistentClass));//
				List<String> tableColumnsWithTableName = tableColumns;
				if(joinClause != null && joinClause.size() > 0) {
					tableColumnsWithTableName = new ArrayList<>();
					for(String tableColumn : tableColumns) {
						tableColumnsWithTableName.add(tableName + "." +tableColumn);
					}
				}
					
				query += String.join(",", tableColumnsWithTableName.toArray(new String[tableColumnsWithTableName.size()]));
				
				if(joinClause != null && joinClause.size() > 0) {
					for(JoinParams joinParams : joinClause) {
						List<String> joinedTableColumns = ReflectionTools.getTableColumns(joinParams.joinType);
						List<String> joinedTableColumnsWithTableName = new ArrayList<>();
						for(String tableColumn : joinedTableColumns) {
							joinedTableColumnsWithTableName.add(joinParams.getJoinTableNameAliased() + "." +tableColumn);
						}
						query += "," + String.join(",", joinedTableColumnsWithTableName.toArray(new String[joinedTableColumnsWithTableName.size()]));
					}
				}
			}
		}
		
		if(countQuery)
			query += ")";
		query += " from " + tableName;

		if(tableAlias != null && !tableAlias.isEmpty()) {
			query += " as " + tableAlias;
		}
		
		if(joinClause != null && joinClause.size() > 0) {
			for(JoinParams joinParams : joinClause) {
				query += " " + joinParams.getType() + " join " + joinParams.getJoinTableNameWithAlias() + " on " + joinParams.getJoinFilter().getFilterClause();
			}
		}
		
		if(filter != null) {
			query += " where " + filter.getFilterClause();
		}
		
		if(groupByFields != null) {
			StringBuilder sb = new StringBuilder();
			for(QField<?> value : groupByFields) {
				if(sb.length() > 0)
					sb.append(",");
				sb.append(value.getFullyQualifiedName()); 
			}
			query += " group by " + sb.toString();
		}
		
		if(orderFields != null) {
			StringBuilder sb = new StringBuilder();
			for(QField<?> value : orderFields) {
				if((!countQuery && (groupByFields == null || groupByFields.length == 0)) 
						|| (groupByFields != null && Arrays.asList(groupByFields).contains(value))) {
					if(sb.length() > 0)
						sb.append(",");
					sb.append(value.getFullyQualifiedName()); 
				}
			}
			if(sb.length() > 0)
				query += " order by " + sb.toString();
		}
		
		if(!countQuery && nbRowsLimit > -1 && limitRequest[1].equals("end")) {
			query += " "+String.format(limitRequest[0], nbRowsLimit);
			
			if(startOffsetLimit > -1 && limitOffsetRequest[1].equals("end"))
				query += " " + String.format(limitOffsetRequest[0], startOffsetLimit);
		}

		return query;
	}
	
	private PreparedStatement getPreparedSelectQuery(boolean countQuery)
			throws SQLException {
		String query = buildQueryString(countQuery);
		
		ContextDomain contextDomain = SqlContext.getContextDomain(ReflectionTools.getTableDomain(persistentClass));
		Connection connection = context.getConnectionForPersistentType(persistentClass);
		
		Map<String, PreparedStatement> queryCache = pstmts.get(connection);
		
		if(queryCache == null) {
			queryCache = new HashMap<>();
			
			pstmts.put(connection, queryCache);
		}
		
		PreparedStatement pstmt = null;
		
		if(queryCache.containsKey(query))
			pstmt = queryCache.get(query);

		if(pstmt == null) {
			if(connection != null) {
				pstmt = connection.prepareStatement(
						query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				
				queryCache.put(query, pstmt);
			}
		}

		if(pstmt != null) {
			int paramIndex = 1;
			for(Object param : getQueryParams()) {
				pstmt.setObject(paramIndex++, param);
			}
		}
		
		return pstmt;
	}
	
	private QResults<T,K> selectForQueryFilter(QField<?>... fields) {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, fields, false, nbRowsLimit, startOffsetLimit, null, joinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	@SuppressWarnings("unchecked")
	private <C> Iterable<C> selectOneColumn() throws SQLException {
		if(selectFields.length > 0)
			return () -> new ColumnIterator<C>((QField<C>)selectFields[0]);
			
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <V> V singleValue() throws SQLException {
		try(ResultSet rs = getPreparedSelectQuery(false).executeQuery()) {
			if(rs.next() && selectFields.length > 0)
				return (V)rs.getObject(selectFields[0].getSelectableFieldName());
		}
		
		return null;
	}
	// [end]
	
	// [start] Méthodes publiques
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @return the typed QResults
	 */
	public static <T> QResults<T,Void> from(Class<T> returnType) {
		return from(returnType, null, null, null);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param sessionCache cache de session
	 * @return the typed QResults
	 */
	public static <T> QResults<T,Void> from(Class<T> returnType, SqlLoadingSessionCache sessionCache) {
		return from(returnType, null, sessionCache, null);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param binderRessourcesMap ressouces map necessary to realize binding
	 * @return the typed QResults
	 */
	public static <T ,K> QResults<T,K> from(Class<T> returnType, K binderRessourcesMap) {
		return from(returnType, null, null, binderRessourcesMap);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param sessionCache cache de session
	 * @param binderRessourcesMap ressouces map necessary to realize binding
	 * @return the typed QResults
	 */
	public static <T,K> QResults<T,K> from(Class<T> returnType, SqlLoadingSessionCache sessionCache, K binderRessourcesMap) {
		return from(returnType, null, sessionCache, binderRessourcesMap);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * The table name can be aliased by <code>tableAlias</code>
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param tableAlias alias of table
	 * @return the typed QResults
	 */
	public static <T> QResults<T,Void> from(Class<T> returnType, String tableAlias) {
		return from(returnType, tableAlias, null, null);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * The table name can be aliased by <code>tableAlias</code>
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param tableAlias alias of table
	 * @param sessionCache cache de session
	 * @return the typed QResults
	 */
	public static <T> QResults<T,Void> from(Class<T> returnType, String tableAlias, SqlLoadingSessionCache sessionCache) {
		return from(returnType, tableAlias, sessionCache, null);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * The table name can be aliased by <code>tableAlias</code>
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param tableAlias alias of table
	 * @param binderRessourcesMap ressouces map necessary to realize binding
	 * @return the typed QResults
	 */
	public static <T,K> QResults<T,K> from(Class<T> returnType, String tableAlias, K binderRessourcesMap) {
		return new QResults<>(null, returnType, returnType, null, null, null, false, -1, -1,
				tableAlias, null, null, binderRessourcesMap, null);
	}
	
	/**
	 * Return a QResults for a type with {@link SqlTable} annotation
	 * The table name can be aliased by <code>tableAlias</code>
	 * 
	 * @param returnType type with {@link SqlTable} annotation for start select query
	 * @param tableAlias alias of table
	 * @param sessionCache cache de session
	 * @param binderRessourcesMap ressouces map necessary to realize binding
	 * @return the typed QResults
	 */
	public static <T,K> QResults<T,K> from(Class<T> returnType, String tableAlias, SqlLoadingSessionCache sessionCache, K binderRessourcesMap) {
		return new QResults<>(null, returnType, returnType, null, null, null, false, -1, -1,
				tableAlias, null, null, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Use a specific database context for this QResults.
	 * 
	 * @param context the context for QResults
	 * @return
	 */
	public QResults<T, K> useContext(SqlContext context) {
		if(context != null)
			this.context = context;
		else
			this.context = SqlContext.getDefaultContext();
		
		return this;
	}
	
	public QResults<T, K> useBuilder(ResultSetRowToObjectBinder<T, K> builder) {
		if(builder != null)
			this.binder = builder;
		else
			this.binder = ResultSetRowToObjectBinderFactory.<T,K>getBinder(returnType);
		
		return this;
	}
	
	public Class<T> getReturnType() {
		return returnType;
	}
	
	/**
	 * Return number of rows concerning by query
	 * 
	 * @return the number of rows of query
	 */
	@Override
	public int count() {
		if(nbRows== -1) {
			try(ResultSet rs = getPreparedSelectQuery(true).executeQuery()) {
				if(rs.next()) {
					nbRows = rs.getInt(1);
				}
			} catch (SQLException e) {
				new UncheckedException(e);
			}
		}
		
		return nbRows;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		//if(selectFields != null)
		//	return null;
		
		return new ResultsIterator();
	}

	/**
	 * Return the first element of this QResults
	 * 
	 * @return the first element of this QResults
	 */
	public T first() {
		if(selectFields!= null)
			return null;
		
		return ((ResultsIterator)iterator()).first();
	}
	
	/**
	 * Return from database table a Map of Fields and associated values
	 * 
	 * @param fields array of fields whitch returns values
	 * @return a Map of Fields and associated values
	 * @throws SQLException
	 */
	@Override
	public Iterable<ResultRow> select(QField<?>... fields) throws SQLException {
		return new QResults<ResultRow,Void>(filter, ResultRow.class, persistentClass, orderFields, groupByFields, fields, false, nbRowsLimit, startOffsetLimit, null, 
				joinClause, new ResultRowBuilder(fields), null, sessionCache);
	}
	
	/**
	 * Return from database table a Map of Fields and associated values. If distint is <code>true</code>
	 * then a distinct filter is realise on database select Query
	 * 
	 * @param distinct if <code>true</code> realise au distinct on database query
	 * @param fields array of fields whitch returns values
	 * @return a Map of Fields and associated values
	 * @throws SQLException
	 */
	@Override
	public Iterable<ResultRow> select(boolean distinct, QField<?>... fields) throws SQLException {
		return new QResults<ResultRow,Void>(filter, ResultRow.class, persistentClass, orderFields, groupByFields, fields, distinct, nbRowsLimit, startOffsetLimit, null, 
				joinClause, new ResultRowBuilder(fields), null, sessionCache);
	}
	
	/**
	 * Group result with given filds (equivalent as sql "group by" clause)
	 * 
	 * @param fields
	 * @return
	 * @throws SQLException
	 */
	public Selectable groupBy(QField<?>... fields) throws SQLException {
		return new QResults<>(filter, returnType, persistentClass, orderFields, fields, selectFields, distinct, nbRowsLimit, startOffsetLimit,
				tableAlias, joinClause, binder, binderRessourcesMap, sessionCache);
	}

	/**
	 * Return a list of values corresponding of a select query on a single columns.
	 * 
	 * @param field the columns field of the values
	 * @return values of given table field
	 * @throws SQLException
	 */
	@Override
	public <C> Iterable<C> selectOneColumn(QField<C> field) throws SQLException {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, new QField[] { field }, false, nbRowsLimit, startOffsetLimit, null, joinClause, binder, binderRessourcesMap, sessionCache).selectOneColumn();
	}
	
	/**
	 * Return a list of values corresponding of a select query on a single columns.
	 * 
	 * @param distinct if <code>true</code> return only distinct elements
	 * @param field the columns field of the values
	 * @return values of given table field
	 * @throws SQLException
	 */
	@Override
	public <C> Iterable<C> selectOneColumn(boolean distinct, QField<C> field) throws SQLException {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, new QField[] { field }, distinct, nbRowsLimit, startOffsetLimit, null, joinClause, binder, binderRessourcesMap, sessionCache).selectOneColumn();
	}
	
	/**
	 * Return a single value for the given field of Qresults table(s)
	 * 
	 * @param field the field that contains the value
	 * @return the first value of field in qresults query
	 * @throws SQLException
	 */
	@Override
	public <V> V singleValue(QField<V> field) throws SQLException {
		return new QResults<>(filter, returnType, persistentClass, null, null, new QField[] { field }, false, nbRowsLimit, startOffsetLimit, null, joinClause, binder, binderRessourcesMap, sessionCache).singleValue();
	}
	
	/**
	 * Apply an inner join on Qresults query to complete results
	 * @param joinType the type of joined element. In normal case, a persistent superttype of initial QResults type.
	 * @param joinFilter the filter necessary to realise join
	 * @return a new QResults who apply the given join clause.
	 */
	public QResults<T,K> innerJoin(Class<?> joinType, QFilter joinFilter) {
		return innerJoin(joinType, null, joinFilter);
	}
	
	/**
	 * Apply an inner join on Qresults query to complete results
	 * @param joinType the type of joined element. In normal case, a persistent superttype of initial QResults type.
	 * @param jointTableAlias apply an alias on join table name
	 * @param joinFilter the filter necessary to realise join. The filter must consider the table alias.
	 * @return a new QResults who apply the given join clause.
	 */
	public QResults<T,K> innerJoin(Class<?> joinType, String jointTableAlias, QFilter joinFilter) {
		JoinParams joinParam = new JoinParams(ReflectionTools.getTableName(joinType), jointTableAlias, joinFilter, joinType);
		List<JoinParams> newJoinClause = new ArrayList<>();
		if(joinClause != null)
			newJoinClause.addAll(joinClause);
		newJoinClause.add(joinParam);
		
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, startOffsetLimit, tableAlias, newJoinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Apply a left join on Qresults query to complete results
	 * @param joinType the type of joined element. In normal case, a persistent superttype of initial QResults type.
	 * @param joinFilter the filter necessary to realise join
	 * @return a new QResults who apply the given join clause.
	 */
	public QResults<T,K> leftJoin(Class<?> joinType, QFilter joinFilter) {
		return leftJoin(joinType, null, joinFilter);
	}
	
	/**
	 * Apply a left join on Qresults query to complete results
	 * @param joinType the type of joined element. In normal case, a persistent superttype of initial QResults type.
	 * @param jointTableAlias apply an alias on join table name
	 * @param joinFilter the filter necessary to realise join. The filter must consider the table alias.
	 * @return a new QResults who apply the given join clause.
	 */
	@SuppressWarnings("nls")
	public QResults<T,K> leftJoin(Class<?> joinType, String jointTableAlias, QFilter joinFilter) {
		JoinParams joinParam = new JoinParams(ReflectionTools.getTableName(joinType), jointTableAlias, joinFilter, joinType);
		joinParam.setType("left");
		List<JoinParams> newJoinClause = new ArrayList<>();
		if(joinClause != null)
			newJoinClause.addAll(joinClause);
		newJoinClause.add(joinParam);
		
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, startOffsetLimit, tableAlias, newJoinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Filter elements return by this QResults
	 * 
	 * @param filter the filter clause for this QResults values
	 * @return a new QResults who apply the given filter clause.
	 */
	public QResults<T,K> where(QFilter filter) {
		QFilter newFilter = filter;
		if(this.filter != null)
			newFilter = this.filter.and(filter);
		return new QResults<>(newFilter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, startOffsetLimit, tableAlias, joinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Ordered results with this order by clause
	 * 
	 * @param orderFields array of order fields
	 * @return ordered results
	 */
	public QResults<T,K> orderBy(QField<?>... orderFields) {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, startOffsetLimit, tableAlias, joinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Limits the number of rows return by results
	 * 
	 * @param nbRowsLimit
	 * @return a new QResults that returns a limited number of rows into iterator
	 */
	public QResults<T,K> limit(int nbRowsLimit) {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, -1, tableAlias, joinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Limits the number of rows return by results and start at specified offset
	 * 
	 * @param nbRowsLimit
	 * @param startOffset
	 * @return a new QResults that returns a limited number of rows into iterator
	 */
	public QResults<T,K> limit(long nbRowsLimit, long startOffset) {
		return new QResults<>(filter, returnType, persistentClass, orderFields, groupByFields, selectFields, false, nbRowsLimit, startOffset, tableAlias, joinClause, binder, binderRessourcesMap, sessionCache);
	}
	
	/**
	 * Return query results as an Object list
	 * 
	 * @return query results as an Object list
	 */
	public List<T> asList() {
		if(selectFields != null)
			return null;
		
		List<T> liste = new ArrayList<>();
		for(T item : this)
			liste.add(item);
		
		return liste;
	}
	
	/**
	 * The current QResults is return as a subquery for another QResults by exemple for an "in" {@link QField} clause
	 * 
	 * @param fields array of field represented on select subquery
	 * @return a subquery
	 */
	@Override
	public QSubQuery asSubQuery(QField<?>... fields) {
		QResults<T,K> selectQResult = selectForQueryFilter(fields);
		return new QSubQuery(selectQResult.asSqlQuery(), selectQResult.getQueryParams());
	}
	
	/**
	 * For debugging, return inner sql query string from current QResult
	 * 
	 * @return the inner sql query string
	 */
	@Override
	public String asSqlQuery() {
		return buildQueryString(false);
	}
	
	/**
	 * The current QResults is return as ResultSet
	 * 
	 * @param fields array of field represented on ResultSet. If null or empty, return all field (select *)
	 * @return a subquery
	 * @throws SQLException 
	 */
	@Override
	public ResultSet asResultSet(QField<?>... fields) throws SQLException {
		if(fields != null && fields.length > 0)
			return selectForQueryFilter(fields).getPreparedSelectQuery(false).executeQuery();
		
		return getPreparedSelectQuery(false).executeQuery();
	}
	//[end]
	
	private static class JoinParams {
		private String joinTableName;
		private String joinTableAlias;
		private QFilter joinFilter;
		private Class<?> joinType;
		
		private String type = "inner"; //$NON-NLS-1$
		/**
		 * @param joinTableName
		 * @param joinTableAlias
		 * @param joinFilter
		 * @param joinType
		 */
		public JoinParams(String joinTableName, String joinTableAlias,
				QFilter joinFilter, Class<?> joinType) {
			this.joinTableName = joinTableName;
			this.joinTableAlias = joinTableAlias;
			this.joinFilter = joinFilter;
			this.joinType = ReflectionTools.getFirstPersistentClass(joinType);
		}
		
		public String getJoinTableNameWithAlias() {
			if(joinTableAlias != null && !joinTableAlias.isEmpty())
				return joinTableName + " as " + joinTableAlias; //$NON-NLS-1$
			return joinTableName;
		}
		
		public String getJoinTableNameAliased() {
			if(joinTableAlias != null && !joinTableAlias.isEmpty())
				return joinTableAlias;
			return joinTableName;
		}
		
		public QFilter getJoinFilter() {
			return joinFilter;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}
	}
	
	private class ResultsIterator implements Iterator<T>,Closeable {
		private T nextEntry = null;
		private ResultSet rs = null;
		private boolean isLastElement = false;
		//private SqlLoadingSessionCache sessionCache = new SqlLoadingSessionCache();
		
		public ResultsIterator() {
			
		}
		
		@Override
		public void remove() {
		}
		
		public T first() {
			try {
				close(); //close current cursor
			} catch (IOException e) {
				e.printStackTrace();
			}
			loadNextEntry(); //open new cursor and load first entry
			try {
				close(); //re-close cursor
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return nextEntry;
		}
		
		@Override
		public T next() {
			if(!isLastElement && nextEntry == null)
				loadNextEntry();
			
			T returnedEntry = nextEntry;
			nextEntry = null;
			
			return returnedEntry;
		}
		
		@Override
		public boolean hasNext() {
			if(!isLastElement && nextEntry == null) 
				loadNextEntry();
			
			return nextEntry != null;
		}
		
		private void executeQuery() throws SQLException {
			rs = getPreparedSelectQuery(false).executeQuery();
		}
		
		private void loadNextEntry() {
			try {
				if(rs == null) {
					executeQuery();
				}

				T entry = null;
//				
//				if(sessionCache == null)
//					sessionCache = new SqlLoadingSessionCache();
//				
				while(entry == null && !rs.isClosed() && rs.next()) {
					if(binder != null)
						entry = binder.get(rs, context, sessionCache, binderRessourcesMap);
				}
				
				if(entry == null) {
					close();
					isLastElement = true;
					nextEntry = null;
				} else {
					nextEntry = entry;
					isLastElement = false;
				}
			} catch (IOException | SQLException | ObjectPersistenceException e) {
				nextEntry = null;
				
				throw new UncheckedException(e);
			}
		}

		@Override
		public void close() throws IOException {
			try {
				if(rs != null && !rs.isClosed()) {
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}
	
	private class ColumnIterator<C> implements Iterator<C>,Closeable {
		private C nextEntry = null;
		private ResultSet rs = null;
		private boolean isLastElement = false;
		private QField<C> column;
		
		public ColumnIterator(QField<C> column) {
			this.column = column;
		}

		private void executeQuery() throws SQLException {
			rs = getPreparedSelectQuery(false).executeQuery();
		}

		private void loadNextEntry() {
			try {
				if(rs == null) {
					executeQuery();
				}

				C entry = null;
	
				while(entry == null && !rs.isClosed() && rs.next()) {
					entry = column.getValue(rs);
				}
				
				if(entry == null) {
					close();
					isLastElement = true;
					nextEntry = null;
				} else {
					nextEntry = entry;
					isLastElement = false;
				}
			} catch (IOException | SQLException | ObjectPersistenceException e) {
				nextEntry = null;
				
				throw new UncheckedException(e);
			}
		}
		
		/* (non-Javadoc)
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() throws IOException {
			try {
				if(rs != null && !rs.isClosed()) {
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if(!isLastElement && nextEntry == null) 
				loadNextEntry();
			
			return nextEntry != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public C next() {
			if(!isLastElement && nextEntry == null)
				loadNextEntry();
			
			C returnedEntry = nextEntry;
			nextEntry = null;
			
			return returnedEntry;
		}
	}
	
//	private class ResultsCollection implements Collection<T> {
//
//		private List<T> newElements = new ArrayList<>();
//		private int nbElementsInDatabase = 0;
//		
//		public ResultsCollection() {
//			nbElementsInDatabase = count();
//		}
//		
//		/* (non-Javadoc)
//		 * @see java.util.Collection#size()
//		 */
//		@Override
//		public int size() {
//			return nbElementsInDatabase + newElements.size();
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#isEmpty()
//		 */
//		@Override
//		public boolean isEmpty() {
//			return nbElementsInDatabase == 0 && newElements.isEmpty();
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#contains(java.lang.Object)
//		 */
//		@Override
//		public boolean contains(Object o) {
//			if(newElements.contains(o))
//				return true;
//			
//			if(!(o instanceof ObjectPersistence)) 
//				return false;
//			
//			for(T item : QResults.this) {
//				if(item.equals(o))
//					return true;
//			}
//			
//			return false;
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#iterator()
//		 */
//		@Override
//		public Iterator<T> iterator() {
//			return QResults.this.iterator();
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#toArray()
//		 */
//		@Override
//		public Object[] toArray() {
//			return asList().toArray();
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#toArray(java.lang.Object[])
//		 */
//		@Override
//		public <U> U[] toArray(U[] a) {
//			return asList().toArray(a);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#add(java.lang.Object)
//		 */
//		@Override
//		public boolean add(T e) {
//			return newElements.add(e);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#remove(java.lang.Object)
//		 */
//		@Override
//		public boolean remove(Object o) {
//			return newElements.remove(o);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#containsAll(java.util.Collection)
//		 */
//		@Override
//		public boolean containsAll(Collection<?> c) {
//			return false;
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#addAll(java.util.Collection)
//		 */
//		@Override
//		public boolean addAll(Collection<? extends T> c) {
//			return newElements.addAll(c);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#removeAll(java.util.Collection)
//		 */
//		@Override
//		public boolean removeAll(Collection<?> c) {
//			return newElements.removeAll(c);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#retainAll(java.util.Collection)
//		 */
//		@Override
//		public boolean retainAll(Collection<?> c) {
//			return newElements.retainAll(c);
//		}
//
//		/* (non-Javadoc)
//		 * @see java.util.Collection#clear()
//		 */
//		@Override
//		public void clear() {
//			newElements.clear();
//		}
//		
//	}
}
