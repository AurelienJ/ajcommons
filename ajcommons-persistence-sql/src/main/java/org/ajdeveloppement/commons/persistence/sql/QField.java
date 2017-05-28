/*
 * Créé le 2 nov. 2011 à 15:17:32 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2011 - Aurélien JEOFFRAY
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.WeakHashMap;

import org.ajdeveloppement.commons.Beta;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * Reprensent meta-datas of a SQL Table field (column)
 * 
 * @author Aurelien JEOFFRAY
 *
 * @param <T> the type of the represented field
 */
@Beta
public class QField<T> {
	private String tableName;
	private String fieldName;
	private String alias;
	private String modifier;
	
	private WeakHashMap<ResultSet, Integer> resultSetsFieldIndexCache = new WeakHashMap<>();
	private int definedFieldIndex = -1;
	private int cacheFieldSqlType = -1;

	/**
	 * Conctruct a new field for a table
	 * 
	 * @param tableName the name of table that contains field (or alias of table if table is aliased in query)
	 * @param fieldName the name of the represented field
	 */
	public QField(String tableName, String fieldName) {
		this(tableName, fieldName, null, null, -1);
	}
	
	/**
	 * Conctruct a new field for a table
	 * 
	 * @param tableName the name of table that contains field (or alias of table if table is aliased in query)
	 * @param fieldName the name of the represented field
	 * @param alias the alias of field. Field alias can be used for select SQL query clause
	 */
	public QField(String tableName, String fieldName, String alias) {
		this(tableName, fieldName, alias, null, -1);
	}
	
	/**
	 * Conctruct a new field for a table
	 * 
	 * @param tableName the name of table that contains field (or alias of table if table is aliased in query)
	 * @param fieldName the name of the represented field
	 * @param alias the alias of field. Field alias can be used for select SQL query clause
	 * @param index index of the column in resultset
	 */
	public QField(String tableName, String fieldName, String alias, int index) {
		this(tableName, fieldName, alias, null, -1);
	}
	
	private QField(String tableName, String fieldName, String alias, String modifier, int index) {
		this.tableName = tableName;
		this.fieldName = fieldName;
		this.alias = alias;
		this.modifier = modifier;
		if(index != -1)
			this.definedFieldIndex = index;
	}
	
	public static <T> QField<T> custom(String fieldName) {
		return custom(null, fieldName, null, null);
	}
	
	public static <T> QField<T> custom(String tableName, String fieldName) {
		return custom(tableName, fieldName, null, null);
	}
	
	public static <T> QField<T> custom(String tableName, String fieldName, String alias) {
		return custom(tableName, fieldName, alias, null);
	}
	
	public static <T> QField<T> custom(String tableName, String fieldName, String alias, String modifier) {
		return new QField<>(tableName, fieldName, alias, modifier, -1);
	}
	
	private int getColumnIndex(ResultSet rs, String columnLabel) throws SQLException {
		int indexColumnInResultSet = -1;
		
		if(resultSetsFieldIndexCache.containsKey(rs)) {
			indexColumnInResultSet = resultSetsFieldIndexCache.get(rs);
		} else {
				long nbPart = columnLabel.chars().filter(c -> c == '.').count();
			//if(columnLabel.chars().filter(c -> c == '.').count() == 2) {
				int nbColumn = rs.getMetaData().getColumnCount();
				
				int sepSchema = -1;
				if(nbPart > 1)
					sepSchema = columnLabel.indexOf('.');
				int sepTable = columnLabel.indexOf('.', sepSchema+1);
				
				String schema = null;
				if(sepSchema > -1)
					schema = columnLabel.substring(0, sepSchema).toUpperCase().replaceAll("[\\[\\]`]", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				String tableName = null;
				if(sepTable > -1)
					tableName = columnLabel.substring(sepSchema + 1, sepTable).toUpperCase().replaceAll("[\\[\\]`]", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				String columnName = columnLabel.substring(sepTable + 1).toUpperCase().replaceAll("[\\[\\]`]", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				for(int i = 1 ; i <= nbColumn; i++) {
					if((schema == null || rs.getMetaData().getSchemaName(i).toUpperCase().equals(schema))
							&& (tableName == null || rs.getMetaData().getTableName(i).toUpperCase().equals(tableName))
							&& rs.getMetaData().getColumnName(i).toUpperCase().equals(columnName)) {
						indexColumnInResultSet = i;
						break;
					}
				}
				
				if(indexColumnInResultSet < 0)
					indexColumnInResultSet = rs.findColumn(columnLabel);
			//} else {
			//	indexColumnInResultSet = rs.findColumn(columnLabel);
			//}
			
			resultSetsFieldIndexCache.put(rs, indexColumnInResultSet);
		}
		
		return indexColumnInResultSet;
	}
	
	/**
	 * Return a derived current QField with an new alias for field name
	 * 
	 * @param alias the new alias for field name
	 * @return the derived QField with alias
	 */
	public QField<T> alias(String alias) {
		return new QField<T>(tableName, fieldName, alias, modifier, definedFieldIndex);
	}
	
	/**
	 * Return a derived current QField with an new alias for table name
	 * 
	 * @param tableAlias the new alias for table name
	 * @return the derived QField with table alias
	 */
	public QField<T> tableAlias(String tableAlias) {
		return new QField<T>(tableAlias, fieldName, alias, modifier, definedFieldIndex);
	}
	
	/**
	 * Return a derived current QField where fully qualified field name is encapsuled
	 * with <code>UPPER()</code> SQL function with method {@link #getFullyQualifiedName()}
	 * 
	 * Attention! Use this only for <code>QField&lt;String&gt;</code> because SQL support
	 * <code>UPPER()</code> only for chars fields (SQL type char, varchar, nvarchar, text, ...)
	 * 
	 * @return the derived QField with UPPER modifier
	 */
	public QField<T> upper() {
		return modifier("UPPER"); //$NON-NLS-1$
	}
	
	/**
	 * Return a derived current QField where fully qualified field name is encapsuled
	 * with <code>LOWER()</code> SQL function with method {@link #getFullyQualifiedName()}
	 * 
	 * Attention! Use this only for <code>QField&lt;String&gt;</code> because SQL support
	 * <code>LOWER()</code> only for chars fields (SQL type char, varchar, nvarchar, text, ...)
	 * 
	 * @return the derived QField with LOWER modifier
	 */
	public QField<T> lower() {
		return modifier("LOWER"); //$NON-NLS-1$
	}
	
	/**
	 * Return a derived current QField where fully qualified field name is encapsuled
	 * with modifier SQL function with method {@link #getFullyQualifiedName()}
	 * 
	 * @return the derived QField with LOWER modifier
	 */
	public QField<T> modifier(String modifier) {
		return new QField<T>(tableName, fieldName, alias, modifier, definedFieldIndex);
	}
	
	/**
	 * Return a derived current QField subfixed with "DESC" keyword for descending order by clause.
	 * !! Use this only for an order by clause
	 * @return Return a derived current QField subfixed with "DESC"
	 */
	public QField<T> toOrderByDesc() {
		return new QField<T>(tableName, fieldName + " DESC", alias); //$NON-NLS-1$
	}
	
	/**
	 * Return name or alias of table associate with this field
	 * 
	 * @return the name of SQL table
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Return the name of SQL table field
	 * 
	 * @return the name of SQL table field
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * Return the alias of field
	 * 
	 * @return the alias of field
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Return the alias name of field if exists or fieldname if not
	 * 
	 * @return the alias or field name
	 */
	public String getSelectableFieldName() {
		return getAlias() != null && !getAlias().isEmpty() ? getAlias() : getFieldName();
	}
	
	/**
	 * Return the name of SQL table field with his alias if exists with 
	 * the syntax:<br>
	 * <code>[fieldName] as [alias]</code>
	 * 
	 * @return the name of SQL table field with his alias (<code>[fieldName] as [alias]</code>)
	 */
	public String getFieldNameWithAlias() {
		if(alias != null && !alias.isEmpty())
			return fieldName + " as " + alias; //$NON-NLS-1$
		return fieldName;
	}
	
	/**
	 * Return the fully qualified name of field. A fully qualified name is
	 * concatenation of table or table alias name and field name and eventually a SQL function modifier:<br>
	 * <code>([modifier(])[tableName].[fieldName]([)])</code><br>
	 * this is used for "where" and "join" clause
	 * 
	 * @return the fully qualified name of field
	 */
	public String getFullyQualifiedName() {
		String name = fieldName;
		if(tableName != null && !tableName.isEmpty())
			name = tableName + "." + fieldName; //$NON-NLS-1$
		
		if(modifier != null && !modifier.isEmpty()) {
			return modifier + "(" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return name;
	}
	
	/**
	 * Same as {@link #getFullyQualifiedName()} with append of field alias if exists for "select" clause:<br>
	 * code>([modifier(])[tableName].[fieldName]([)]) as [alias]</code><br>
	 * 
	 * @return the fully qualified name with eventual alias for "select" clause
	 */
	public String getFullyQualifiedNameWithAlias() {
		if(alias != null && !alias.isEmpty())
			return getFullyQualifiedName() + " as " + alias; //$NON-NLS-1$
		return getFullyQualifiedName();
	}
	
	/**
	 * Filter elements that are lower than value
	 * 
	 * @param value the comparable element
	 * @return the filter
	 */
	public QFilter lowerThan(T value) {
		return new QFilter(getFullyQualifiedName() + " < ?", Collections.singletonList((Object)value));  //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that are lower than or equal to value
	 * 
	 * @param value the comparable element
	 * @return the filter
	 */
	public QFilter lowerOrEqualTo(T value) {
		return new QFilter(getFullyQualifiedName() + " <= ?", Collections.singletonList((Object)value));  //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that are upper than or equal to value
	 * 
	 * @param value the comparable element
	 * @return the filter
	 */
	public QFilter upperOrEqualTo(T value) {
		return new QFilter(getFullyQualifiedName() + " >= ?", Collections.singletonList((Object)value));  //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that are upper than value
	 * 
	 * @param value the comparable element
	 * @return the filter
	 */
	public QFilter upperThan(T value) {
		return new QFilter(getFullyQualifiedName() + " > ?", Collections.singletonList((Object)value));  //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that equal to value
	 * 
	 * @param value  the comparable element
	 * @return the filter
	 */
	public QFilter equalTo(T value) {
		return new QFilter(getFullyQualifiedName() + " = ?", Collections.singletonList((Object)value)); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that equal to another field
	 * 
	 * @param value the other field
	 * @return the filter
	 */
	public QFilter equalTo(QField<?> value) {
		return new QFilter(getFullyQualifiedName() + " = " + value.getFullyQualifiedName(), null); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that different of value
	 * 
	 * @param value  the comparable element
	 * @return the filter
	 */
	public QFilter differentOf(T value) {
		return new QFilter(getFullyQualifiedName() + " <> ?", Collections.singletonList((Object)value)); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements that different of another field
	 * 
	 * @param value the other field
	 * @return the filter
	 */
	public QFilter differentOf(QField<?> value) {
		return new QFilter(getFullyQualifiedName() + " <> " + value.getFullyQualifiedName(), null); //$NON-NLS-1$
	}
	
	/**
	 * For <code>QField&lt;String&gt;</code> only, filter elements with "like" SQL keyword
	 * 
	 * @param value the search like string. Can contains SQL wildcard % and _
	 * @return the filter
	 */
	public QFilter like(String value) {
		return new QFilter(getFullyQualifiedName() + " like ?", Collections.singletonList((Object)value)); //$NON-NLS-1$
	}
	
	/**
	 * For <code>QField&lt;String&gt;</code> only, filter elements with "not like" SQL keyword
	 * 
	 * @param value the search not like string. Can contains SQL wildcard % and _
	 * @return the filter
	 */
	public QFilter notLike(String value) {
		return new QFilter(getFullyQualifiedName() + " not like ?", Collections.singletonList((Object)value)); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements where value is null
	 * 
	 * @return the filter
	 */
	public QFilter isNull() {
		return new QFilter(getFullyQualifiedName() + " is null", null); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements where value is not null
	 * 
	 * @return the filter
	 */
	public QFilter isNotNull() {
		return new QFilter(getFullyQualifiedName() + " is not null", null); //$NON-NLS-1$
	}
	
	/**
	 * Filter elements where field value is in values
	 * 
	 * @param values list of include values for filter
	 * @return the filter
	 */
	public QFilter in(Iterable<T> values) {
		StringBuilder sb = new StringBuilder();
		for(T value : values) {
			if(sb.length() > 0)
				sb.append(","); //$NON-NLS-1$
			sb.append(value.toString()); 
		}
		String collection = sb.toString();
		
		return in(collection);
	}
	
	/**
	 * Filter elements where field value is in subquery filter clause
	 * 
	 * @param innerQuery a sub query obtain with {@link QResults#asSubQuery(QField...)}
	 * @return the filter
	 */
	public QFilter in(QSubQuery innerQuery) {
		return new QFilter(getFullyQualifiedName() + " in ("+ innerQuery.getFilterClause() + ")", innerQuery.getParams()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Filter elements where field value is in subquery filter clause
	 * 
	 * @param subQuery the subquery string.
	 * @return the filter
	 */
	public QFilter in(String subQuery) {
		return new QFilter(getFullyQualifiedName() + " in ("+ subQuery + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Filter elements where field value is not in values
	 * 
	 * @param values list of include values for filter
	 * @return the filter
	 */
	public QFilter notIn(Iterable<T> values) {
		StringBuilder sb = new StringBuilder();
		for(T value : values) {
			if(sb.length() > 0)
				sb.append(","); //$NON-NLS-1$
			sb.append(value.toString()); 
		}
		String collection = sb.toString();
		
		return notIn(collection);
	}
	
	/**
	 * Filter elements where field value is not in subquery filter clause
	 * 
	 * @param innerQuery a sub query obtain with {@link QResults#asSubQuery(QField...)}
	 * @return the filter
	 */
	public QFilter notIn(QSubQuery innerQuery) {
		return new QFilter(getFullyQualifiedName() + " not in ("+ innerQuery.getFilterClause() + ")", innerQuery.getParams()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Filter elements where field value is not in subquery filter clause
	 * 
	 * @param subQuery the subquery string.
	 * @return the filter
	 */
	public QFilter notIn(String subQuery) {
		return new QFilter(getFullyQualifiedName() + " not in ("+ subQuery + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Return the current ResultSet row value for the current field
	 * 
	 * @param rs the resultset row that contains the field value to extract
	 * @return the field value for the resultset row
	 * @throws SQLException
	 * @throws ObjectPersistenceException 
	 */
	@SuppressWarnings("unchecked")
	public T getValue(ResultSet rs) throws SQLException, ObjectPersistenceException {
		String columnLabel = ""; //$NON-NLS-1$
		if(alias != null && !alias.isEmpty())
			columnLabel = alias;
		else
			columnLabel = getFullyQualifiedName();
		
		synchronized (rs) {
			int indexColumnInResultSet = getColumnIndex(rs, columnLabel);
			
			if(cacheFieldSqlType == -1)
				cacheFieldSqlType = rs.getMetaData().getColumnType(indexColumnInResultSet);
			
			switch (cacheFieldSqlType) {
				case Types.BLOB:
					Blob blob = rs.getBlob(indexColumnInResultSet);
					if(blob != null) {
						long dataLength = blob.length();
						if(dataLength < Integer.MAX_VALUE)
							return (T)blob.getBytes(0, (int)dataLength);
						
						throw new ObjectPersistenceException("The value of " + columnLabel + " is too long for a Byte Array cast"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					break;
				case Types.CLOB:
					Clob clob = rs.getClob(indexColumnInResultSet);
					if(clob != null) {
						long dataLength = clob.length();
						if(dataLength > 0 && dataLength < Integer.MAX_VALUE) {
							return (T)clob.getSubString(1, (int)clob.length());
						} else if(dataLength == 0)
							return (T)""; //$NON-NLS-1$

						throw new ObjectPersistenceException("The value of " + columnLabel + " is too long for a String cast"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					break;
				case Types.LONGVARBINARY:
					
					try(BufferedInputStream stream = new BufferedInputStream(rs.getBinaryStream(indexColumnInResultSet))) {
						if(!rs.wasNull()) {
							try(ByteArrayOutputStream baOut = new ByteArrayOutputStream()) {
								byte[] buf = new byte[2048];
								int nbRead = 0;
								while((nbRead = stream.read(buf)) != -1) {
									baOut.write(buf, 0, nbRead);
								}
								
								baOut.flush();
								
								return (T)baOut.toByteArray();
							}
						}
						
						return null;
					} catch (IOException e) {
						throw new ObjectPersistenceException(e);
					}
				case Types.LONGVARCHAR:
					Reader reader = rs.getCharacterStream(indexColumnInResultSet);
					if(reader != null) {
						try(BufferedReader breader = new BufferedReader(reader)) {
							StringBuilder texte = new StringBuilder();
							String ligne = null;
							while((ligne = breader.readLine()) != null){
								texte.append(ligne).append("\n"); //$NON-NLS-1$
					        }
							return (T)texte.toString();
						} catch (IOException e) {
							throw new ObjectPersistenceException(e);
						}
					}
					break;
				case Types.LONGNVARCHAR:
					Reader nreader = rs.getNCharacterStream(indexColumnInResultSet);
					if(nreader != null) {
						try(BufferedReader longNVarcharReader = new BufferedReader(nreader)) {
							StringBuilder texte = new StringBuilder();
							String ligne = null;
							while((ligne = longNVarcharReader.readLine()) != null){
								texte.append(ligne).append("\n"); //$NON-NLS-1$
					        }
							return (T)texte.toString();
						} catch (IOException e) {
							throw new ObjectPersistenceException(e);
						}
					}
				case Types.SMALLINT:
					return (T)(Short)rs.getShort(indexColumnInResultSet);
				case Types.TINYINT:
					return (T)(Byte)rs.getByte(indexColumnInResultSet);
			}

			return (T)rs.getObject(indexColumnInResultSet);
		}
	}
	
	/**
	 * Return the current ResultSet row stream for BLOB, CLOB and LONGVARBINARY.
	 * 
	 * @param rs the resultset row that contains the field stream
	 * @return the field stream for the resultset row
	 * @throws SQLException
	 */
	public InputStream getValueAsStream(ResultSet rs) throws SQLException {
		String columnLabel = ""; //$NON-NLS-1$
		if(alias != null && !alias.isEmpty())
			columnLabel = alias;
		else
			columnLabel = getFullyQualifiedName();
		
		int indexColumnInResultSet = getColumnIndex(rs, columnLabel);
		
		if(cacheFieldSqlType == -1)
			cacheFieldSqlType = rs.getMetaData().getColumnType(indexColumnInResultSet);
		
		switch (cacheFieldSqlType) {
			case Types.BLOB:
				Blob blob = rs.getBlob(indexColumnInResultSet);
				if(blob != null) {
					return blob.getBinaryStream();
				}
				break;
			case Types.CLOB:
				Clob clob = rs.getClob(indexColumnInResultSet);
				if(clob != null) {
					return clob.getAsciiStream();
				}
				break;
			case Types.LONGVARBINARY:
				return rs.getBinaryStream(indexColumnInResultSet);
		}
		return null;
	}

	/**
	 * Return the current ResultSet row Reader for CLOB and LONGVARCHAR and LONGNVARCHAR.
	 * 
	 * @param rs the resultset row that contains the field Reader
	 * @return the field Reader for the resultset row
	 * @throws SQLException
	 */
	public Reader getValueAsReader(ResultSet rs) throws SQLException {
		String columnLabel = ""; //$NON-NLS-1$
		if(alias != null && !alias.isEmpty())
			columnLabel = (tableName != null ? tableName + "." : "") + alias; //$NON-NLS-1$ //$NON-NLS-2$
		else
			columnLabel = getFullyQualifiedName();
		
		int indexColumnInResultSet = getColumnIndex(rs, columnLabel);
		
		if(cacheFieldSqlType == -1)
			cacheFieldSqlType = rs.getMetaData().getColumnType(indexColumnInResultSet);
		
		switch (cacheFieldSqlType) {
			case Types.CLOB:
				Clob clob = rs.getClob(indexColumnInResultSet);
				if(clob != null) {
					return clob.getCharacterStream();
				}
				break;
				
			case Types.LONGVARCHAR:
				return rs.getCharacterStream(indexColumnInResultSet);
			case Types.LONGNVARCHAR:
				return rs.getNCharacterStream(indexColumnInResultSet);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fieldName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result
				+ ((modifier == null) ? 0 : modifier.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
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
		QField<?> other = (QField<?>) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (modifier == null) {
			if (other.modifier != null)
				return false;
		} else if (!modifier.equals(other.modifier))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
	
	
}
