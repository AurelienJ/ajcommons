/*
 * Créé le 21/02/2006 à 14:01 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.ajdeveloppement.commons.UncheckedException;

/**
 * Initialise et retourne les chemins des ressources applicative pour le programme en
 * fonction du système Hôte.
 * 
 * @author  Aurélien JEOFFRAY
 * @version 1.1
 */
public class AppRessources {
	
    private String userPath;
    private String allusersDataPath;
    
    private char[] appKeyStorePassword;
    private File appKeyStoreFile;
    private KeyStore appKeyStore;
    private char[] userKeyStorePassword;
    private File userKeyStoreFile;
    private KeyStore userKeyStore;
    
    /**
     * Construit le répertoire utilisateur selon le systeme
     * 
     * @param progname nom du programme
     */
    public AppRessources(String progname) {
    	// Chemins de ressources standard Windows
        if(System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
            userPath = System.getenv("APPDATA") + File.separator //$NON-NLS-1$
                    + progname;
            if(Integer.parseInt(System.getProperty("os.version").substring(0,1)) >= 6) //$NON-NLS-1$
            	allusersDataPath = System.getenv("ALLUSERSPROFILE") + File.separator + progname; //$NON-NLS-1$
            else
            	allusersDataPath = System.getenv("ALLUSERSPROFILE") + File.separator  //$NON-NLS-1$
            			+ "Application Data" + File.separator + progname; //$NON-NLS-1$
        
        // Chemins des ressources standard par défaut
        } else {
            userPath = System.getProperty("user.home") + File.separator //$NON-NLS-1$ 
            	+ "." + progname; //$NON-NLS-1$
            
            // Chemins de ressources commune Linux
            if(System.getProperty("os.name").startsWith("Linux")) {  //$NON-NLS-1$//$NON-NLS-2$
            	allusersDataPath = "/var/lib/" + progname; //$NON-NLS-1$
            	new File(allusersDataPath).mkdirs();
            	File f = new File(allusersDataPath);
            	if(!f.exists() || !f.canExecute())
            		allusersDataPath = userPath;
            } else
            	allusersDataPath = userPath;
        }

        new File(allusersDataPath).mkdirs();
        new File(userPath).mkdirs();
    }
    
    private KeyStore checkKeyStore(File keyStoreFile, char[] password)
    		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	KeyStore keyStore;
    	
    	KeyStore newKeyStore = KeyStore.getInstance("JCEKS"); //$NON-NLS-1$

    	if(!keyStoreFile.exists()) {
    		newKeyStore.load(null, null);
    		
    		try(FileOutputStream newKeyStoreOut = new FileOutputStream(keyStoreFile)) {
	    		newKeyStore.store(newKeyStoreOut, password);
	    		newKeyStoreOut.flush();
    		}
    		
    		keyStore = newKeyStore;
    	} else {
    		boolean success = false;
    		if(keyStoreFile.exists()) {
	    		try(FileInputStream appKeyStoreIn = new FileInputStream(keyStoreFile)) {
	    			newKeyStore.load(appKeyStoreIn, password);
	    			
	    			success = true;
	    		} catch (Exception e) {
                }
    		}
    		
    		if(!success) {
    			newKeyStore.load(null, password);
    		}
    		
    		keyStore = newKeyStore;
    	}
    	return keyStore;
    }
    
    private void checkAppKeyStore(char[] password)
    		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	if(appKeyStore == null)
    		appKeyStore = checkKeyStore(getAppKeyStoreFile(), password); 
    }
    
    private void checkUserKeyStore(char[] password)
    		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	if(userKeyStore == null)
    		userKeyStore = checkKeyStore(getUserKeyStoreFile(), password);
    }

    /**
     * Retourne le répertoire de base de l'utilisateur
     * 
	 * @return  Renvoie le repertoire de base de l'utilisateur
	 */
    public String getUserPath() {
        return userPath;
    }

    /**
     * <p>Définit le répertoire de base de l'utilisateur.</p>
     * <p>Si aucun chemin n'est défini, utilise le chemin standard du système:</p>
     * <ul>
     * 	<li>%APPDATA%\[appname] sur Windows</li>
     *  <li>System.getProperty("user.home") + /Library/Application Support/[appname] sur Mac OS X</li>
     *  <li>System.getProperty("user.home") + .[appname] sur les autres sytèmes</li>
     * </ul>
     * 
	 * @param userPath le repertoire de base de l'utilisateur
	 */
    public void setUserPath(String userPath) {
        this.userPath = userPath;
    }

	/**
	 * <p>Retourne le chemin du répertoire applicatif partagé commun à l'ensemble des utilisateurs du poste.</p>
	 * <p>Le chemin retourné correspond au convention du système Hôte</p>
	 * 
	 * @return le chemin du répertoire partagé
	 */
	public String getAllusersDataPath() {
		return allusersDataPath;
	}

	/**
	 * <p>Spécfie explicitement le chemin dur répertoire applicatif partagé entre utilisateurs.</p>
	 * <p>Si aucun chemin n'est défini, utilise le chemin standard du système:</p>
	 * <ul>
	 * 	<li>%ALLUSERSPROFILE%\[appname] sur Windows Vista/Seven/Server 2008 (&gt;=NT 6.0)</li>
	 *  <li>%ALLUSERSPROFILE%\Application Data\[appname] sur Windows 2000/XP/Server 2003 (NT 5.0).
	 *  Pas de support officiel pour les version antérieur de Windows</li>
	 *  <li>/Library/Application Support/[appname] sur Mac OS X</li>
	 *  <li>/var/lib/[appname] sur Linux</li>
	 *  <li>Sur les autres systémes, correspond au résultat de la méthode {@link #getUserPath()}</li>
	 * </ul>
	 * 
	 * @param allusersDataPath le chemin dur répertoire applicatif partagé entre utilisateurs
	 */
	public void setAllusersDataPath(String allusersDataPath) {
		this.allusersDataPath = allusersDataPath;
	}
	
	/**
	 * Return path to java jvm executable
	 * 
	 * @since 1.1
	 * 
	 * @return the java executable path
	 */
	public String getJavaExecutablePath() {
        File pathToJava;
        
        File javaBinDirectory = new File(System.getProperty("java.home"), "bin") ;  //$NON-NLS-1$//$NON-NLS-2$
        
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) //$NON-NLS-1$ //$NON-NLS-2$
            pathToJava = new File(javaBinDirectory, "java.exe"); //$NON-NLS-1$
        else
            pathToJava = new File(javaBinDirectory, "java"); //$NON-NLS-1$
        
        if (!pathToJava.exists()) {
            // Fallback to old behaviour
            return "java"; //$NON-NLS-1$
        }
        
        return pathToJava.getPath();
    }

	/**
	 * Retourne le chemin du KeyStore de l'application. Si celui-ci n'a pas
	 * été défini manuellement, alors retourne le chemin de la méthode {@link #getAllusersDataPath()} + "app.jks"
	 * 
	 * @return the appKeyStorePath
	 */
	public File getAppKeyStoreFile() {
		if(appKeyStoreFile == null)
			appKeyStoreFile = new File(getAllusersDataPath(), "app.jks"); //$NON-NLS-1$
		return appKeyStoreFile;
	}

	/**
	 * Définit le chemin du fichier stockant le KeyStore de l'application.
	 * 
	 * @param appKeyStorePath le chemin du KeyStore de l'application
	 */
	public void setAppKeyStoreFile(File appKeyStorePath) {
		this.appKeyStoreFile = appKeyStorePath;
	}

	/**
	 * Retourne le mot de passe du KeyStore de l'application.
	 * Si aucun mot de passe n'a été défini, retourne le numéro
	 * de série généré au premier démarrage de l'application.
	 * 
	 * @return le mot de passe du KeyStore de l'application
	 */
	public char[] getAppKeyStorePassword() {
		if(appKeyStorePassword == null) {
			appKeyStorePassword = AppUtilities.getAppUID(this).toCharArray();
		}
		return appKeyStorePassword;
	}

	/**
	 * Définit le mot de passe du KeyStore de l'application
	 * 
	 * @param appKeyStorePassword e mot de passe du KeyStore de l'application
	 */
	public void setAppKeyStorePassword(char[] appKeyStorePassword) {
		this.appKeyStorePassword = appKeyStorePassword;
	}

	/**
	 * Retourne le KeyStore de l'application. Un KeyStore sert à stocké clés et certificats
	 * nécessaire aux opérations de signature et/ou chiffrages.
	 * 
	 * @return le KeyStore général de l'application
	 */
	public KeyStore getAppKeyStore() {
		try {
			checkAppKeyStore(getAppKeyStorePassword());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new UncheckedException(e);
		}
		return appKeyStore;
	}

	/**
	 * <p>Définit un keyStore global à utiliser pour les opérations cryptographique de
	 * l'application.</p>
	 * <p>Si aucun keyStore n'est définit ou si il est définit à null, un keyStore par défaut sera
	 * généré dans le repertoire retourné par la méthode {@link #getAllusersDataPath()} avec le
	 * nom app.jks</p>
	 * 
	 * @param appKeyStore le keyStore global de l'application
	 */
	public void setAppKeyStore(KeyStore appKeyStore) {
		this.appKeyStore = appKeyStore;
	}
	
	/**
	 * Sauvegarde le KeyStore de l'application dans le fichier retourné par la méthode
	 * {@link #getAppKeyStoreFile()}
	 * 
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	public void storeAppKeyStore()
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		try (FileOutputStream appKeyStoreOut = new FileOutputStream(getAppKeyStoreFile())) {
			appKeyStore.store(appKeyStoreOut, getAppKeyStorePassword());
			appKeyStoreOut.flush();
		}
	}
	
	/**
	 * Retourne le chemin du fichier stockant le KeyStore de l'utilisateur.
	 * Si le chemin n'ap pas été spécifiquement définit, alors retourne le chemin
	 * de la méthode {@link #getUserPath()} + "user.jks"
	 * 
	 * @return le chemin du fichier stockant le KeyStore de l'utilisateur
	 */
	public File getUserKeyStoreFile() {
		if(userKeyStoreFile == null)
			userKeyStoreFile = new File(getUserPath(), "user.jks"); //$NON-NLS-1$
		return userKeyStoreFile;
	}

	/**
	 * Définit le chemin du fichier stockant le KeyStore de l'utilisateur.
	 * 
	 * @param userKeyStoreFile du fichier stockant le KeyStore de l'utilisateur.
	 */
	public void setUserKeyStoreFile(File userKeyStoreFile) {
		this.userKeyStoreFile = userKeyStoreFile;
	}

	/**
	 * Retourne le mot de passe du KeyStore de l'utilisateur
	 * 
	 * @return le mot de passe du KeyStore de l'utilisateur
	 */
	public char[] getUserKeyStorePassword() {
		return userKeyStorePassword;
	}

	/**
	 * Définit le mot de passe du KeyStore de l'utilisateur
	 * 
	 * @param userKeyStorePassword le mot de passe du KeyStore de l'utilisateur
	 */
	public void setUserKeyStorePassword(char[] userKeyStorePassword) {
		this.userKeyStorePassword = userKeyStorePassword;
	}

	/**
	 * Retourne le KeyStore de l'utilisateur. Un KeyStore sert à stocké clés et certificats
	 * nécessaire aux opérations de signature et/ou chiffrages.
	 * 
	 * @return le KeyStore de l'utilisateur
	 */
	public KeyStore getUserKeyStore() {
		try {
			checkUserKeyStore(getUserKeyStorePassword());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new UncheckedException(e);
		}
		return userKeyStore;
	}

	/**
	 * <p>Définit un keyStore de l'utilisateur à utiliser pour les opérations cryptographique de
	 * l'application.</p>
	 * <p>Si aucun keyStore n'est définit ou si il est définit à null, un keyStore par défaut sera
	 * généré dans le repertoire retourné par la méthode {@link #getUserPath()} avec le
	 * nom user.jks</p>
	 * 
	 * @param userKeyStore the userKeyStore to set
	 */
	public void setUserKeyStore(KeyStore userKeyStore) {
		this.userKeyStore = userKeyStore;
	}
	
	/**
	 * Sauvegarde le KeyStore de l'utilisateur dans le fichier retourné par la méthode
	 * {@link #getUserKeyStoreFile()}
	 * 
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	public void storeUserKeyStore()
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		try  (FileOutputStream userKeyStoreOut = new FileOutputStream(getUserKeyStoreFile())) {
			userKeyStore.store(userKeyStoreOut, getUserKeyStorePassword());
			userKeyStoreOut.flush();
		}
	}
}
