/*
 * Créé le 10 avr. 2014 à 22:30:02 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2014 - Aurélien JEOFFRAY
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.ajdeveloppement.commons.FinalizableWrapper;
import org.ajdeveloppement.commons.UncheckedException;
import org.ajdeveloppement.commons.io.FileUtils;
import org.ajdeveloppement.commons.io.XMLSerializer;
import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * SQL Persistence Context.
 * An SqlContext manage connection, transaction with dababase and also manage cache to loaded instances
 * 
 * @author "Aurélien JEOFFRAY"
 */
public class SqlContext {

	public static final String DEFAULT_DOMAIN = "default"; //$NON-NLS-1$
	
	private static Map<String, ContextDomain> contextDomains = new HashMap<>();
	//private static SqlContext defaultContext = new SqlContext();
	private static WeakHashMap<ClassLoader, SqlContext> defaultsContext = new WeakHashMap<>();
	
	private Map<String, ThreadLocal<FinalizableWrapper<Connection>>> domainsConnection = new HashMap<>();
	private WeakHashMap<Connection, Savepoint> domainsSavepoint = new WeakHashMap<>();
	
	private Cache cache = new Cache();

	static {
		try {
			loadFromConfigFile();
		} catch (IOException | URISyntaxException | JAXBException e) {
			throw new UncheckedException(e);
		}
	}
	
	/**
	 * Get the default context for the current ThreadContext classloader
	 * 
	 * @return the default SqlContext for the current ThreadContext classloader
	 */
	public static SqlContext getDefaultContext() {
		ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
		
		synchronized (threadContextClassLoader) {
			SqlContext defaultContext = defaultsContext.get(threadContextClassLoader);
			if(defaultContext == null) {
				defaultContext = new SqlContext();
				defaultsContext.put(threadContextClassLoader, defaultContext);
			}
			
			return defaultContext;
		}
	}
	
	/**
	 * @return the contextDomains
	 */
	public static Map<String, ContextDomain> getContextDomains() {
		return contextDomains;
	}
	
	/**
	 * Return the ContextDomain associate to domain
	 * 
	 * @param domain
	 * @return
	 */
	public static ContextDomain getContextDomain(String domain) {
		ContextDomain contextDomain = SqlContext.getContextDomains().get(domain);
		if(contextDomain == null)
			contextDomain = SqlContext.getContextDomains().get(SqlContext.DEFAULT_DOMAIN);
		
		return contextDomain;
	}

	/**
	 * @param contextDomains the contextDomains to set
	 */
	public static void setContextDomains(Map<String, ContextDomain> contextDomains) {
		SqlContext.contextDomains = contextDomains;
	}
	
	public static void addContextDomain(String domain, ContextDomain contextDomain) {
		contextDomains.put(domain, contextDomain);
	}
	
	public static void removeContextDomain(String domain) {
		contextDomains.remove(domain);
	}
	
	private static void loadFromConfigFile() throws IOException, URISyntaxException, JAXBException {
		Pattern rgxContextDomain = Pattern.compile("^META-INF/persistence/(?<name>.*)\\.xml$"); //$NON-NLS-1$
		
		ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
		synchronized (threadContextClassLoader) {
			Enumeration<URL> en = threadContextClassLoader.getResources("META-INF/persistence"); //$NON-NLS-1$
		    List<String> profiles = new ArrayList<>();
		    if (en.hasMoreElements()) {
		        URL url = en.nextElement();
		        if(url.getProtocol().equals("file")) { //$NON-NLS-1$
		        	File persistenceDirectory = new File(url.toURI());
		        	for(File xml : FileUtils.listAllFiles(persistenceDirectory, ".*\\.xml")) { //$NON-NLS-1$
		        		ContextDomain contextDomain = XMLSerializer.loadMarshallStructure(xml, ContextDomain.class);
		        		if(contextDomain != null)
		        			contextDomains.put(xml.getName().substring(0, xml.getName().length() - 4), contextDomain);
		        	}
		        } else {
			        URLConnection urlConnection = url.openConnection();
			        if(urlConnection instanceof JarURLConnection) {
				        JarURLConnection urlcon = (JarURLConnection)urlConnection;
				        try (JarFile jar = urlcon.getJarFile()) {
				            Enumeration<JarEntry> entries = jar.entries();
				            while (entries.hasMoreElements()) {
				            	JarEntry entry = entries.nextElement();
				                String entryName = entry.getName();
				                
				                Matcher m = rgxContextDomain.matcher(entryName);
				                if(m.matches()) {
				                	try(InputStream stream = threadContextClassLoader.getResourceAsStream(entryName)) {
				                		ContextDomain contextDomain = XMLSerializer.loadMarshallStructure(stream, ContextDomain.class, false, false);
				                		if(contextDomain != null)
				    	        			contextDomains.put(m.group("name"), contextDomain); //$NON-NLS-1$
				                	}
				                }
				            }
				        }
			        }
		        }
		    }
		}
	}

	public SqlContext() {
	}
	
	private ThreadLocal<FinalizableWrapper<Connection>> createConnectionForDomain(String domain) throws SQLException {
		ThreadLocal<FinalizableWrapper<Connection>> connection = null;
		if(contextDomains.containsKey(domain)) {
			synchronized (domainsConnection) {
				connection = new ThreadLocal<FinalizableWrapper<Connection>>() {
					@Override
					protected FinalizableWrapper<Connection> initialValue() {
						try {
							return new FinalizableWrapper<Connection>(contextDomains.get(domain).createConnection(), cnx -> {
								try {
									cnx.close();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
						} catch (SQLException | NamingException e) {
							throw new UncheckedException(e);
						}
					}
				};
				domainsConnection.put(domain, connection);
			}
		}
		
		return connection;
	}
	
	public Cache getCache() {
		return cache;
	}
	
	public Connection getConnectionForPersistentType(Class<?> persistentType) throws SQLException {
		String domain = ReflectionTools.getTableDomain(persistentType);
		
		return getConnectionForDomain(domain);
	}
	
	public Connection getConnectionForDomain(String domain) throws SQLException {
		ThreadLocal<FinalizableWrapper<Connection>> threadConnection = domainsConnection.get(domain);
		if(threadConnection == null) {
			threadConnection = createConnectionForDomain(domain);
			if(threadConnection == null)
				return getDefaultConnection();
		}
		
		ContextDomain context = contextDomains.get(domain);
		
		if(context != null
				&& context.isValidateConnectionBeforeUse() 
				&& !threadConnection.get().get().isValid(context.getTimoutValidation())) {
			threadConnection.remove();
		}
		
		return threadConnection.get().get();
	}
	
	/**
	 * @return the defaultConnection
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public Connection getDefaultConnection() throws SQLException {
		return getConnectionForDomain(DEFAULT_DOMAIN);
	}

	/**
	 * open a new transaction for a connection. If a transaction is already open
	 * do nothing.
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	public synchronized void openTransactionForConnection(Connection connection) throws SQLException {
		if(!domainsSavepoint.containsKey(connection)) {
			connection.setAutoCommit(false);
			Savepoint savepoint = connection.setSavepoint();
			
			domainsSavepoint.put(connection, savepoint);
		}
	}
	
	/**
	 * Commit all opened transactions in context
	 * 
	 * @throws SQLException
	 */
	public synchronized void commitOpenTransactions() throws SQLException {
		Throwable errors = null;
		
		List<Connection> transactionsCommited = new ArrayList<Connection>();
		
		for(Entry<Connection, Savepoint> entry : domainsSavepoint.entrySet())
		{
			try {
				Connection connection = entry.getKey();
				if(connection != null) {
					try {
						connection.commit();
					} finally {
						connection.releaseSavepoint(entry.getValue());
						connection.setAutoCommit(true);
					}
					
					transactionsCommited.add(entry.getKey());
				}
			} catch (Exception e) {
				if(errors == null)
					errors = e;
				else
					errors.addSuppressed(e);
			}
		}
		
		transactionsCommited.forEach(d -> domainsSavepoint.remove(d));
		
		//Rollback sur les trancations qui n'on pas pu se terminer correctement
		rollbackAllOpenTransactions();

		if(errors != null)
			throw new SQLException(errors);
	}
	
	/**
	 * Rollback all opened transactions in context
	 * 
	 * @throws SQLException
	 */
	public synchronized void rollbackAllOpenTransactions() throws SQLException {
		Throwable errors = null;
		for(Entry<Connection, Savepoint> entry : domainsSavepoint.entrySet())
		{
			try {
				Connection connection = entry.getKey();
				
				if(connection != null) {
					try {
						connection.rollback(entry.getValue());
					} finally {
						connection.releaseSavepoint(entry.getValue());
						connection.setAutoCommit(true);
					}
				}
			} catch (Exception e) {
				if(errors == null)
					errors = e;
				else
					errors.addSuppressed(e);
			}
		}
		
		if(errors != null)
			throw new SQLException(errors);
	}
	
	public void save(ObjectPersistence object) throws ObjectPersistenceException {
		SessionHelper.startSaveSession(this, object);
	}
	
	public void delete(ObjectPersistence object) throws ObjectPersistenceException {
		SessionHelper.startDeleteSession(this, object);
	}
}
