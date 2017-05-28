/*
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
package org.ajdeveloppement.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.crypto.SecretKey;
import javax.swing.event.EventListenerList;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.ajdeveloppement.apps.AppUtilities;
import org.ajdeveloppement.commons.Converters;
import org.ajdeveloppement.commons.StringFormatters;
import org.ajdeveloppement.commons.io.FileUtils;
import org.ajdeveloppement.commons.io.XMLSerializer;
import org.ajdeveloppement.commons.security.KeyUtil;
import org.ajdeveloppement.commons.security.SecurityImporter;
import org.ajdeveloppement.commons.security.XMLSignatureValidator;
import org.ajdeveloppement.commons.ui.DataDownloadProgressDialog;
import org.ajdeveloppement.updater.Repository.Status;
import org.ajdeveloppement.updater.tools.AjUpdaterApply;
import org.xml.sax.SAXException;

/**
 * <p>Service de mise à jour automatique de l'application</p>
 * <p>Utilisation:
 * <code><pre>
 * //Initialise le service de mise à jour
 * AjUpdater updater = new AjUpdater("chemin/stockage/temporaire/fichier/téléchargé",
 * 		"chemin/de/l/application/a/mettre/a/jour");
 * //se met à l'écoute du service 
 * updater.addAjUpdaterListener(l);
 * 
 * //définit le keyStore dans lequel consulter ou stocker les clés et certificats
 * updater.setAppKeyStore(keyStore);
 * 
 * //définit le mot de passe de chiffrement des clés symétriques dans le keyStore
 * updater.setSecretKeyPassword("monmotdepasse".toCharArray());
 * 
 * //ajoute un dépôt de mise à jour
 * updater.addRepository(repos);
 * 
 * //déclenche la recherche de mise à jour
 * Map&lt;Repository, List&lt;FileMetaData&gt;&gt; fichieratelecharger = updater.checkUpdate();
 * 
 * //télécharge les fichiers de mise à jour
 * //une fois le téléchargement terminé, déclenche un événement AjUpdaterEvent FILES_DOWNLOADED
 * //qui doit être capturé pour lancer l'utilitaire {@link AjUpdaterApply} dans un environnement
 * //privilégié
 * updater.downloadFiles(fichieratelecharger);
 * </pre></code>
 * </p>
 * 
 * @see AjUpdaterEvent
 * @see AjUpdaterListener
 * @see Repository
 * 
 * @author Aurélien JEOFFRAY
 * @version 0.90.5
 */
public class AjUpdater {

	private final List<Repository> repositories = new ArrayList<Repository>();
	private KeyStore appKeyStore;
	private char[] secretKeyPassword;
	private String tempPath = ""; //$NON-NLS-1$
	private String hashPath = ""; //$NON-NLS-1$
	private long downloadSize = 0l;
	private Map<Repository, List<Version>> infoVersions = new HashMap<Repository, List<Version>>();
	private Map<Repository, List<FileMetaData>> filesPath = new HashMap<Repository, List<FileMetaData>>();
	private Map<Repository, List<FileMetaData>> securitiesPath = new HashMap<Repository, List<FileMetaData>>();

	private String userAgent = "AjUpdater Tools API 1.0"; //$NON-NLS-1$

	private final EventListenerList listeners = new EventListenerList();

	/**
	 * initialise le système de mise à jour
	 * 
	 * @param tempPath le fichiers ou stocké provisoirement les ressources téléchargé
	 * @param hashPath le chemin de base de l'application
	 */
	public AjUpdater(String tempPath, String hashPath) {
		this.tempPath = tempPath;
		this.hashPath = hashPath;
	}

	/**
	 * Permet l'ajout d'un auditeur au service de mise à jour.
	 * 
	 * @param listener l'auditeur à l'écoute du service de mise à jour.
	 */
	public void addAjUpdaterListener(AjUpdaterListener listener) {
		listeners.add(AjUpdaterListener.class, listener);
	}

	/**
	 * Permet à un auditeur de se retirer de l'écoute du service
	 * 
	 * @param listener
	 */
	public void removeAjUpdaterListener(AjUpdaterListener listener) {
		listeners.remove(AjUpdaterListener.class, listener);
	}

	/**
	 * Retourne le User-Agent envoyé au serveur HTTP avec chaque requête et identifiant le
	 * service de mise à jour.
	 * 
	 * @return le User-Agent envoyé au serveur HTTP
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * Définit le User-Agent envoyé au serveur HTTP avec chaque requête et identifiant le
	 * service de mise à jour.
	 * 
	 * @param userAgent le User-Agent envoyé au serveur HTTP
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Retourne le KeyStore applicatif (espace de stockage de clé) servant
	 * à vérifier les signatures de dépôt ou contenant les clés symétrique mis
	 * à jour par le service de mise à jour.
	 * 
	 * @return le KeyStore Applicatif manipulé par le service
	 */
	public KeyStore getAppKeyStore() {
		return appKeyStore;
	}

	/**
	 * <p>Définit le KeyStore applicatif (espace de stockage de clé) servant
	 * à vérifier les signatures de dépôt ou contenant les clés symétrique
	 * pouvant être mis à jour par le service.</p>
	 * <p>Si le KeyStore est définit à null, la vérification des signatures ce
	 * fera exclusivement avec le KeyStore système de java. En outre, aucune clé
	 * ou certificat ne pourra être importé à partir du dépôt</p>
	 * 
	 * @param appKeyStore le KeyStore Applicatif manipulé par le service
	 */
	public void setAppKeyStore(KeyStore appKeyStore) {
		this.appKeyStore = appKeyStore;
	}
	
	/**
	 * Définit le mot de passe permettant de chiffrer les clés secrètes transmises
	 * par un dépôt. Afin d'assurer un niveau maximal de sécurité, le mot de passe
	 * devrait pouvoir être définit par l'utilisateur.
	 * 
	 * @param password le mot de passe servant à chiffrer les clés symétriques fournit
	 * par un dépôt.
	 */
	public void setSecretKeyPassword(char[] password) {
		this.secretKeyPassword = password;
	}

	/**
	 * Ajoute un dépôt de téléchargement au service de mise à jour.
	 * 
	 * @param repos le dépôt de téléchargement à ajouter au service
	 */
	public void addRepository(Repository repos) {
		if(!repositories.contains(repos))
			repositories.add(repos);
	}

	/**
	 * Retire un dépôts de téléchargement du service de mise à jour
	 * 
	 * @param repos le dépôt de téléchargement à retirer du service
	 */
	public void removeRepository(Repository repos) {
		repositories.remove(repos);
	}
	
	/**
	 * Retourne la liste des dépôts ajouter au service et contenant les données de mise
	 * à jour. Un dépôt peut représenter l'application principal ou une extension de celle ci.
	 * 
	 * @return la liste des dépôts interrogé par le service
	 */
	public List<Repository> getRepositories() {
		return repositories;
	}

	/**
	 * Retourne 0 si le service n'a pas contacté le ou les dépôts de mise à jour ou si il n'y
	 * a pas de mise à jour ou la taille total en octet des fichiers à télécharger si des mises
	 * à jour sont disponible.
	 * 
	 * @return la taille en octet des mises à jour disponible ou 0 si pas de mise à jour disponible
	 */
	public long getDownloadSize() {
		return downloadSize;
	}
	
	/**
	 * Retourne l'historique de version disponible pour le dépôt transmis en paramètre
	 * 
	 * @param repos le dépôt pour lequel retourner l'historique de version.
	 * @return l'historique de version pour le dépôt
	 */
	public List<Version> getInfoVersions(Repository repos) {
		return infoVersions.get(repos);
	}

	/**
	 * Retourne les métadonnées de des fichiers présent sur le dépôts données et
	 * devant être téléchargé pour la mise à jour
	 * 
	 * @param repos le dépôt pour lequel retourner les métadonnées.
	 * @return les métadonnées des fichier à télécharger pour la mise à jour pour
	 * le dépôt donnée
	 */
	public List<FileMetaData> getFilesPath(Repository repos) {
		return filesPath.get(repos);
	}

	/**
	 * Récupère le fichier xml contenant la liste des révisions
	 * 
	 */
	private Revision loadRevisionFile(Repository repos) throws UpdateException {
		Revision revision = null;
		BufferedInputStream sourceStream = null;

		String url = repos.getReposURLs()[repos.getCurrentMirror()] + "/revision.xml.gz"; //$NON-NLS-1$
		url = url.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			uc.setRequestProperty("User-Agent", userAgent); //$NON-NLS-1$
			
			InputStream in =  uc.getInputStream();
			
			sourceStream = new BufferedInputStream(new GZIPInputStream(in));
		} catch (InterruptedIOException e) {
			repos.setReposStatus(Status.CONNECTION_FAILED);
			throw new UpdateException("Socket Interrupted", e); //$NON-NLS-1$
		} catch (IOException e) {
			repos.setReposStatus(Status.CONNECTION_FAILED);
			if(e.getMessage().indexOf("HTTP response code: 407") > -1) { //$NON-NLS-1$
				throw new UpdateException("Proxy Authentification required for URL: " + url, e); //$NON-NLS-1$
			} else if(e.getMessage().indexOf("HTTP response code: 401") > -1) { //$NON-NLS-1$
				throw new UpdateException("WWW Authentification required for URL: " + url, e); //$NON-NLS-1$
			}
			throw new UpdateException("Socket Error for url " + url, e); //$NON-NLS-1$
		}
		
		File tmpXML = null;
		try {
			tmpXML = File.createTempFile("rev", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
			FileUtils.dumpStreamToFile(sourceStream, tmpXML);
			
			revision = XMLSerializer.loadMarshallStructure(tmpXML, Revision.class, false);
			
			XMLSignatureValidator signatureValidator = new XMLSignatureValidator(appKeyStore);
			signatureValidator.setNoSignatureValidation(true);
			if(signatureValidator.verifyXMLSignature(tmpXML)) {
				repos.setCertificate(signatureValidator.getCertificate());
				
				switch(signatureValidator.getCertType()) {
					case SELF_SIGNED:
						repos.setReposStatus(Status.REVISION_SIGN_VERIFY_WITH_SELFSIGNED_CERT);
						break;
					case TRUSTED:
						repos.setReposStatus(Status.REVISION_SIGN_VERIFY_WITH_TRUSTED_CERT);
						break;
					default:
						repos.setReposStatus(Status.REVISION_NOSIGN_FOUND);
				}
			} else {
				switch(signatureValidator.getCertificateFailedRaison()) {
					case CERTIFICATE_ERROR:
					case OTHER:
						repos.setReposStatus(Status.CERTIFICATE_FAILED);
						break;
					case CA_NOT_FOUND_ERROR:
					case REVOCATION_ERROR:
						repos.setReposStatus(Status.CERTIFICATE_VALIDATION_FAILED);
						break;
					case VALIDITY_ERROR:
						repos.setReposStatus(Status.REVISION_SIGN_VERIFY_WITH_EXPIRED_CERT);
						break;
					default:
						repos.setReposStatus(Status.REVISION_SIGN_VERIFY_FAILED);
				}
			}
		} catch (JAXBException e) {
			repos.setReposStatus(Status.UNSERIALIZE_FAILED);
			throw new UpdateException("Unmarshall XML fail", e); //$NON-NLS-1$
		} catch (IOException e) {
			repos.setReposStatus(Status.UNSERIALIZE_FAILED);
			throw new UpdateException("Read or write revision XML fail", e); //$NON-NLS-1$
		} catch (SAXException e) {
			e.printStackTrace();
			repos.setReposStatus(Status.REVISION_SIGN_VERIFY_FAILED);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			repos.setReposStatus(Status.REVISION_SIGN_VERIFY_FAILED);
		} catch (MarshalException e) {
			e.printStackTrace();
			repos.setReposStatus(Status.CERTIFICATE_FAILED);
		} catch (XMLSignatureException e) {
			e.printStackTrace();
			repos.setReposStatus(Status.REVISION_SIGN_VERIFY_FAILED);
		} finally {
			if(tmpXML != null)
				tmpXML.delete();
		}

		return revision;
	}

	/**
	 * <p>Liste les fichiers à télécharger et importe les certificats et clés symétrique
	 * associé au différents dépôts.</p>
	 * <p>Les certificats et clés ne peuvent être importé
	 * qu'a la seul condition que le dépôt soit signé numériquement et la signature
	 * certifié par une autorité de certification et ceux afin de s'assurer de l'authenticité
	 * des clés.</p>
	 * <p>En outre, afin de garantir la confidentialité des clés symétriques, il est
	 * indispensable que tous dépôt proposant ce type de clé soit protégé en SSL avec
	 * une authentification utilisateur.</p>
	 * 
	 * @return la liste des chemins relatif des fichiers à mettre à jour par dépôt
	 * @throws UpdateException 
	 */
	public Map<Repository, List<FileMetaData>> checkUpdate() throws UpdateException {
		Map<String, Long> localHash = new HashMap<String, Long>();
		Revision revision;
		
		filesPath.clear();
		securitiesPath.clear();
		infoVersions.clear();
		downloadSize = 0;
		
		String system = AppUtilities.getCurrentSystem();
		
		//Établie la table CRC des fichiers locaux
		List<File> referenceFiles = FileUtils.listAllFiles(new File(hashPath), ".*"); //$NON-NLS-1$
		for(File referenceFile : referenceFiles) {
			long checksum = FileUtils.calculChecksum(referenceFile);
			String path = referenceFile.getPath().replace(hashPath + File.separator, ""); //$NON-NLS-1$
			path = path.replace('\\', '/');
			localHash.put(path, checksum);
		}

		for (Repository repos : repositories) {
			try {
				revision = loadRevisionFile(repos);

				if(revision != null) {
					File dir = new File(tempPath, "files"); //$NON-NLS-1$
					dir.mkdirs();
					
					PrintStream ps = null;
					try {
						ps = new PrintStream(new File(dir, repos.getReposName() + "-filelist.txt")); //$NON-NLS-1$ 
						for (FileMetaData fileHash : revision.getFilesMetaData()) {
							ps.println(fileHash.getPath());
							
							long localFileHash = 0;
							try {
								localFileHash = localHash.get(fileHash.getPath());
							} catch (NullPointerException e) {
							} // traite l'exception comme un cas normal
							
							if (fileHash.getHash() != localFileHash 
									&& (fileHash.getOs().equals("all") || system.startsWith(fileHash.getOs()))) { //$NON-NLS-1$
								if (!filesPath.containsKey(repos))
									filesPath.put(repos, new ArrayList<FileMetaData>());
								filesPath.get(repos).add(fileHash);
								downloadSize += fileHash.getFileSize();
							}
						}
						ps.flush();
					} catch(FileNotFoundException e) {
						throw new UpdateException(e);
					} finally {
						if(ps != null)
							ps.close();
					}
					
					
					//on autorise l'installation des clés uniquement pour les PKI valide
					//idem pour les scripts pre/post
					if(appKeyStore != null && repos.getReposStatus() == Status.REVISION_SIGN_VERIFY_WITH_TRUSTED_CERT) {
						for (FileMetaData certMetaData : revision.getCryptoKeys()) {
							String securityFile = certMetaData.getPath();
							
							try {
								if(securityFile.startsWith("certs")) { //$NON-NLS-1$
									String alias = securityFile.substring(securityFile.lastIndexOf("/")+1, securityFile.lastIndexOf(".pem")); //$NON-NLS-1$ //$NON-NLS-2$
									X509Certificate cert = (X509Certificate)appKeyStore.getCertificate(alias);
									
									String signature = ""; //$NON-NLS-1$
									if(cert != null)
										signature = Converters.byteArrayToHexString(cert.getSignature());
										
									if(!signature.equals(certMetaData.getSecurehash())) {
										if(!securitiesPath.containsKey(repos))
											securitiesPath.put(repos, new ArrayList<FileMetaData>());
										securitiesPath.get(repos).add(certMetaData);
									}
								} else if(securityFile.startsWith("keys")) { //$NON-NLS-1$
									String alias = securityFile.substring(securityFile.lastIndexOf("/")+1, securityFile.lastIndexOf(".key")); //$NON-NLS-1$ //$NON-NLS-2$
									
									SecretKey key = (SecretKey)appKeyStore.getKey(alias, secretKeyPassword);
									
									String hash = ""; //$NON-NLS-1$
									if(key != null) {
										try {
											byte[] b = KeyUtil.serializeKey(key).getBytes("ASCII"); //$NON-NLS-1$
											MessageDigest digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
											digest.reset();
											digest.update(b);
											hash = Converters.byteArrayToHexString(digest.digest());
											
											if(!hash.equals(certMetaData.getSecurehash())) {
												if(!securitiesPath.containsKey(repos))
													securitiesPath.put(repos, new ArrayList<FileMetaData>());
												securitiesPath.get(repos).add(certMetaData);
											}
										} catch (UnsupportedEncodingException e) {
											e.printStackTrace();
										}
									} else {
										if(!securitiesPath.containsKey(repos))
											securitiesPath.put(repos, new ArrayList<FileMetaData>());
										securitiesPath.get(repos).add(certMetaData);
									}
								}
							} catch (KeyStoreException e) {
								e.printStackTrace();
							} catch (UnrecoverableKeyException e) {
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							}
						}
						
						downloadSecuritiesFile(repos);
						
						
						try {
							if(revision.getPreUpdateScript() != null) {
								String url = repos.getReposURLs()[repos.getCurrentMirror()] + "/" + revision.getPreUpdateScript().getPath(); //$NON-NLS-1$
								url = url.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
								downloadFile(new URL(url), tempPath + File.separator + "scripts" + File.separator + "pre" + File.separator + revision.getPreUpdateScript().getPath(), revision.getPreUpdateScript().getSecurehash()); //$NON-NLS-1$ //$NON-NLS-2$
							}
							
							if(revision.getPostUpdateScript() != null) {
								String url = repos.getReposURLs()[repos.getCurrentMirror()] + "/" + revision.getPostUpdateScript().getPath(); //$NON-NLS-1$
								url = url.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
								downloadFile(new URL(url), tempPath + File.separator + "scripts" + File.separator + "post" + File.separator + revision.getPostUpdateScript().getPath(), revision.getPostUpdateScript().getSecurehash()); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					if(revision.getVersions() != null)
						infoVersions.put(repos, revision.getVersions());
				}
			} catch (UpdateException e) {
				e.printStackTrace();
			}
		}

		if (downloadSize > 0) {
			fireAjUpdaterEvent(AjUpdaterEvent.Status.UPDATE_AVAILABLE, filesPath);
		} else {
			fireAjUpdaterEvent(AjUpdaterEvent.Status.NO_UPDATE_AVAILABLE);
		}

		return filesPath;
	}

	/**
	 * Télécharge les fichiers à mettre à jour
	 * 
	 * @param files table représentant les fichiers à
	 * mettre à jour et généré par {@link AjUpdater#checkUpdate()}
	 */
	public void downloadFiles(Map<Repository, List<FileMetaData>> files) {
		final Map<Repository, List<FileMetaData>> updatefiles = files;
		int nbtotalfile = 0;
		for(Entry<Repository, List<FileMetaData>> tfiles : updatefiles.entrySet()) {
			if(tfiles.getValue() != null)
				nbtotalfile += tfiles.getValue().size();
		}
		
		final DataDownloadProgressDialog progressDialog = new DataDownloadProgressDialog(downloadSize, nbtotalfile);
		Thread downloadThread = new Thread() {
			@Override
			public void run() {
				BufferedInputStream sourceStream = null;

				int indice = 1;

				for (Entry<Repository, List<FileMetaData>> reposEntry : updatefiles.entrySet()) {
					if(reposEntry.getValue() == null)
						continue;
					for (FileMetaData file : reposEntry.getValue()) {
						progressDialog.setIndiceFile(indice++);
						
						String url = reposEntry.getKey().getReposURLs()[reposEntry.getKey().getCurrentMirror()] + "/" + file.getPath(); //$NON-NLS-1$
						url = url.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$

						String destination = tempPath + File.separator + "files" + File.separator + file.getPath(); //$NON-NLS-1$
						if (!(new File(destination).exists()) 
								|| !file.getSecurehash().equals(
										Converters.byteArrayToHexString(
												FileUtils.getSHA256Hash(new File(destination))))) {
							progressDialog.setCurrentFile(new File(destination).getName());
							try {
								// ouvre la connexion
								URL u = new URL(url);
								URLConnection uc = u.openConnection();
								uc.setRequestProperty("User-Agent", userAgent); //$NON-NLS-1$
								progressDialog.setDisplayFileSize(StringFormatters.formatFileSize(uc.getContentLength()));
								
								// ouvre le flux
								InputStream in = uc.getInputStream();
								DigestInputStream din = null;
								try {
									din = new DigestInputStream(in, MessageDigest.getInstance("SHA-256")); //$NON-NLS-1$
								} catch (NoSuchAlgorithmException e) {
									e.printStackTrace();
								}
								if(din != null)
									sourceStream = new BufferedInputStream(din);
								else
									sourceStream = new BufferedInputStream(in);

								new File(destination).getParentFile().mkdirs();

								FileUtils.dumpStreamToFile(sourceStream, new File(destination), false);
								progressDialog.addDownloadedSize(uc.getContentLength());
								
								if(din != null && !Converters.byteArrayToHexString(din.getMessageDigest().digest()).equals(file.getSecurehash())) {
									fireAjUpdaterEvent(AjUpdaterEvent.Status.FILE_ERROR);
									progressDialog.setVisible(false);
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
								fireAjUpdaterEvent(AjUpdaterEvent.Status.FILE_ERROR);
								progressDialog.setVisible(false);
								return;
							} finally {
								try {
									sourceStream.close();
								} catch (Exception e) {
								}
								sourceStream = null;
							}
						} else {
							progressDialog.addDownloadedSize(new File(destination).length());
							Thread.yield();
						}

					}
				}
				progressDialog.setVisible(false);
				fireAjUpdaterEvent(AjUpdaterEvent.Status.FILES_DOWNLOADED);
			}
		};
		progressDialog.showProgressDialog();

		downloadThread.start();
	}
	
	/**
	 * Télécharge les certificats numériques et clés de chiffrement associé au dépôts
	 * fournit en paramètre.
	 * 
	 * @param repos le dépôt pour lequel récupérer clés et certificats
	 */
	public void downloadSecuritiesFile(final Repository repos) {
		if(appKeyStore == null)
			return;
		
		Thread downloadThread = new Thread() {
			@Override
			public void run() {
				List<FileMetaData> files = securitiesPath.get(repos);
				if(files != null) {
					for(FileMetaData file : files) {
						String url = repos.getReposURLs()[repos.getCurrentMirror()] + "/security/" + file.getPath(); //$NON-NLS-1$
						url = url.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
						
						// ouvre la connexion
						URL u;
						try {
							u = new URL(url);
							URLConnection uc = u.openConnection();
							uc.setRequestProperty("User-Agent", userAgent); //$NON-NLS-1$
							
							BufferedInputStream sourceStream = null;
							
							// ouvre le flux
							InputStream in = uc.getInputStream();
							try {
								DigestInputStream din = null;
								try {
									din = new DigestInputStream(in, MessageDigest.getInstance("SHA-256")); //$NON-NLS-1$
								} catch (NoSuchAlgorithmException e) {
									e.printStackTrace();
								}
								if(din != null)
									sourceStream = new BufferedInputStream(din);
								else
									sourceStream = new BufferedInputStream(in);
								
								String securityFile = file.getPath();
								String alias;
								if(securityFile.startsWith("certs")) {//$NON-NLS-1$
									alias = securityFile.substring(securityFile.lastIndexOf("/")+1, securityFile.lastIndexOf(".pem")); //$NON-NLS-1$ //$NON-NLS-2$
									SecurityImporter.importCert(appKeyStore, sourceStream, alias);
								} else if(securityFile.startsWith("keys")) {//$NON-NLS-1$
									alias = securityFile.substring(securityFile.lastIndexOf("/")+1, securityFile.lastIndexOf(".key")); //$NON-NLS-1$ //$NON-NLS-2$
									SecurityImporter.importKey(appKeyStore, sourceStream, alias, secretKeyPassword);
								}
							} finally {
								if(in != null)
									in.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (CertificateException e) {
							e.printStackTrace();
						} catch (KeyStoreException e) {
							e.printStackTrace();
						} catch (InvalidKeyException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		};
		
		downloadThread.start();
	}
	
	private void downloadFile(URL url, String destination, String sha256) throws IOException, UpdateException {
		BufferedInputStream sourceStream = null;
		
		URLConnection uc = url.openConnection();
		uc.setRequestProperty("User-Agent", userAgent); //$NON-NLS-1$
		
		// ouvre le flux
		InputStream in = uc.getInputStream();
		try {
			DigestInputStream din = null;
			try {
				din = new DigestInputStream(in, MessageDigest.getInstance("SHA-256")); //$NON-NLS-1$
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			if(din != null)
				sourceStream = new BufferedInputStream(din);
			else
				sourceStream = new BufferedInputStream(in);
	
			new File(destination).getParentFile().mkdirs();
	
			FileUtils.dumpStreamToFile(sourceStream, new File(destination), false);
			
			if(din != null && !Converters.byteArrayToHexString(din.getMessageDigest().digest()).equals(sha256)) {
				throw new UpdateException("Invalid file SHA-256 Hash"); //$NON-NLS-1$
			}
		} finally {
			if(in != null)
				in.close();
		}
	}

	private void fireAjUpdaterEvent(AjUpdaterEvent.Status status) {
		for (AjUpdaterListener listener : listeners.getListeners(AjUpdaterListener.class)) {
			listener.updaterStatusChanged(new AjUpdaterEvent(status));
		}
	}

	private void fireAjUpdaterEvent(AjUpdaterEvent.Status status, Map<Repository, List<FileMetaData>> updateFiles) {
		for (AjUpdaterListener listener : listeners.getListeners(AjUpdaterListener.class)) {
			listener.updaterStatusChanged(new AjUpdaterEvent(status, updateFiles));
		}
	}
}
