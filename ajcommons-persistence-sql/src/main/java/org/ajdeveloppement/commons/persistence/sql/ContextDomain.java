/*
 * Créé le 14 oct. 2016 à 09:22:46 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2016 - Aurélien JEOFFRAY
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.ajdeveloppement.commons.UncheckedException;

/**
 * @author a.jeoffray
 *
 */
@XmlRootElement(name="domain")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContextDomain {
	private String driver;
	private String databaseUrl;
	private String user;
	private String password;
	private String persistenceDialect;
	
	private transient DataSource dataSource;
	private transient ConnectionPoolDataSource connectionPoolDataSource;
	private transient PooledConnection pooledConnection;
	private String jndiDataSourceServiceName;
	
	private boolean validateConnectionBeforeUse = false;
	private int timoutValidation = 10;
	
	public ContextDomain() {
		
	}
	
	/**
	 * Return the name of defined jdbc driver
	 * 
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Set the name of jdbc driver use for connections. If the driver jdbc 4 or more compliant, can be null.
	 * 
	 * @param driver the name of jdbc driver use for connections
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	/**
	 * The datasource use to return a jdbc connection. The connection can be defined in jndi container and declare with {@link #setJndiDataSourceServiceName(String)}.
	 * 
	 * @return the datasource use if defined or null if no datasource exists
	 * @throws NamingException 
	 */
	public DataSource getDataSource() throws NamingException {
		if(this.dataSource == null && jndiDataSourceServiceName != null && !jndiDataSourceServiceName.isEmpty()) {
			Context ctx = new InitialContext();
			this.dataSource = (DataSource) ctx.lookup(jndiDataSourceServiceName);
		}
		return this.dataSource;
	}
	
	/**
	 * Define an DataSource to return jdbc connection. If null, you must use
	 * a database url.
	 * 
	 * @param dataSource the DataSource use to return jdbc connection
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Get the PooledDataSource use to return jdbc connection. If null, 
	 * a DataSource or database url must be defined.
	 * 
	 * @return the PooledDataSource use to return jdbc connection
	 */
	public ConnectionPoolDataSource getConnectionPoolDataSource() {
		return connectionPoolDataSource;
	}

	/**
	 * Define an PooledDataSource to return jdbc connection. If null, you must use
	 * a DataSource or database url.
	 * 
	 * @param connectionPoolDataSource the PooledDataSource to return jdbc connection
	 */
	public void setConnectionPoolDataSource(ConnectionPoolDataSource connectionPoolDataSource) {
		this.connectionPoolDataSource = connectionPoolDataSource;
		
	}

	/**
	 * If DataSource is defined in jndi, get the name of jndi service
	 * 
	 * @return the name of jndi service DataSource
	 */
	public String getJndiDataSourceServiceName() {
		return jndiDataSourceServiceName;
	}

	/**
	 * If DataSource is defined in jndi, set the name of jndi service
	 * 
	 * @param jndiDataSourceServiceName the name of jndi service DataSource
	 */
	public void setJndiDataSourceServiceName(String jndiDataSourceServiceName) {
		this.jndiDataSourceServiceName = jndiDataSourceServiceName;
	}

	/**
	 * If DataSource is null, the jdbc database url use to create connection
	 * 
	 * @return the jdbc database url use to create connection
	 */
	public String getDatabaseUrl() {
		return databaseUrl;
	}

	/**
	 * Define the jdbc database url use to create connection in case of DataSource is null
	 * 
	 * @param databaseUrl the databaseUrl to set
	 */
	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	/**
	 * If no DataSource is defined, the user of jdbc connection
	 * 
	 * @return the user of jdbc connection
	 */
	public String getUser() {
		return user;
	}

	/**
	 * If no DataSource is defined, the user defined for jdbc connection
	 * 
	 * @param user the user defined for jdbc connection
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * If no DataSource is defined, the password of jdbc connection
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * If no DataSource is defined, the password defined for jdbc connection
	 * 
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return the persistenceDialect
	 */
	public String getPersistenceDialect() {
		return persistenceDialect;
	}

	/**
	 * @param persistenceDialect the persistenceDialect to set
	 */
	public void setPersistenceDialect(String persistenceDialect) {
		this.persistenceDialect = persistenceDialect;
	}

	/**
	 * If true, the validity of connection may be tested before each request and connection can be renew if invalid, else request can be
	 * send before validity testing and can return an error.
	 * 
	 * @return the validateConnectionBeforeUser
	 */
	public boolean isValidateConnectionBeforeUse() {
		return validateConnectionBeforeUse;
	}

	/**
	 * Set if the validity of connection must be tested before each request and connection can be renew if invalid, else request can be
	 * send before validity testing and can return an error.
	 * 
	 * @param validateConnectionBeforeUser the validateConnectionBeforeUser to set
	 */
	public void setValidateConnectionBeforeUse(boolean validateConnectionBeforeUser) {
		this.validateConnectionBeforeUse = validateConnectionBeforeUser;
	}

	/**
	 * If {{@link #isValidateConnectionBeforeUse()} is true, the timeout for connection validation
	 * 
	 * @return the timeout for connection validation
	 */
	public int getTimoutValidation() {
		return timoutValidation;
	}

	/**
	 * If {{@link #isValidateConnectionBeforeUse()} is true, set the timeout for connection validation
	 * 
	 * @param timoutValidation the timeout for connection validation
	 */
	public void setTimoutValidation(int timoutValidation) {
		this.timoutValidation = timoutValidation;
	}

	/**
	 * Return a new connection for the domain context
	 * 
	 * @return
	 * @throws SQLException
	 * @throws NamingException
	 */
	public Connection createConnection() throws SQLException, NamingException {
		if(driver != null && !driver.isEmpty()) {
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				throw new UncheckedException(e);
			}
		}
		
		if(connectionPoolDataSource != null) {
			if(pooledConnection == null) {
				synchronized (connectionPoolDataSource) {
					if(pooledConnection == null)
						pooledConnection = connectionPoolDataSource.getPooledConnection();
				}
			}
			
			if(pooledConnection != null) {
				return pooledConnection.getConnection();
			}
		}
		
		if(getDataSource() != null) 
			return getDataSource().getConnection();

		Connection cnx = DriverManager.getConnection(databaseUrl, user, password);
		
		return cnx;
	}
}