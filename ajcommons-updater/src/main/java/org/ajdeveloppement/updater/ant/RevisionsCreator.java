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
package org.ajdeveloppement.updater.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.ajdeveloppement.commons.Converters;
import org.ajdeveloppement.commons.io.FileUtils;
import org.ajdeveloppement.commons.io.XMLSerializer;
import org.ajdeveloppement.commons.security.CertUtil;
import org.ajdeveloppement.commons.security.XMLSignatureHelper;
import org.ajdeveloppement.updater.FileMetaData;
import org.ajdeveloppement.updater.Revision;
import org.ajdeveloppement.updater.Version;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;

/**
 * Tache Ant permettant de concevoir un fichier de révision utilisé par le système
 * de mise à jour afin de détérminer les fichiers à télécharger si c'est nécessaire
 * 
 *
 * @author Aurélien JEOFFRAY
 * @version 2.0
 *
 */
public class RevisionsCreator extends Task {

	private final List<FileSet> filesets = new ArrayList<FileSet>();
	private List<System> osfiles = new ArrayList<System>();
	private final List<FileSet> keysAndCerts = new ArrayList<FileSet>();
	private String revisionPath = ""; //$NON-NLS-1$
	private String reposName = ""; //$NON-NLS-1$
	private String changelogXML = ""; //$NON-NLS-1$
	private String changelogTXT = ""; //$NON-NLS-1$
	private String preUpdateScript = ""; //$NON-NLS-1$
	private String postUpdateScript = "";  //$NON-NLS-1$
	
	private String keyStorePath = ""; //$NON-NLS-1$
	private String keyStorePassword = ""; //$NON-NLS-1$
	private String signKeysAlias = ""; //$NON-NLS-1$
	private String signKeysAliasPassword = ""; //$NON-NLS-1$

	/**
	 * Ajoute la collection de fichier à attacher à la révision
	 * 
	 * @param fileset la collection de fichier à attacher à la révision
	 */
	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}
	
	public void addKeysAndCerts(FileSet fileset) {
		keysAndCerts.add(fileset);
	}
	
	/**
	 * Définit le chemin ou placer le fichier de révision
	 * 
	 * @param revisionPath le chemin ou placer le fichier de révision
	 */
	public void setRevisionpath(String revisionPath) {
		this.revisionPath = revisionPath;
	}
	
	/**
	 * Définit le nom du dépots généré par la tâche
	 * 
	 * @param reposName le nom du dépôt généré par la tâche.
	 */
	public void setReposName(String reposName) {
		this.reposName = reposName;
	}
	
	/**
	 * Définit le chemin du fichier de changelog XML généré avec l'outil ChangeLogEditor
	 * 
	 * @param changelogXML le chemin du fichier de changelog XML
	 */
	public void setChangelogXML(String changelogXML) {
		this.changelogXML = changelogXML;
	}
	
	/**
	 * Définit le chemin du fichier changelog TXT qui va être généré à partir de la version XML
	 * 
	 * @param changelogTXT le chemin du fichier changelog TXT à générer
	 */
	public void setChangelogTXT(String changelogTXT) {
		this.changelogTXT = changelogTXT;
	}

	/**
	 * <p>Définit le script javascript à executer avant d'éffectuer la mise à jour de l'application.</p>
	 * <p>Le processus de mise à jour bénéficiant des privilèges administrateur, les scripts bénéficie des mêmes privilèges,
	 * aussi il est nécessaire de s'assurer de la non-nocivité de ces scripts pour les utilisateurs.</p>
	 * 
	 * @param preUpdateScript le script à passer avant la mise à jour
	 */
	public void setPreUpdateScript(String preUpdateScript) {
		this.preUpdateScript = preUpdateScript;
	}

	/**
	 * Définit le script javascript à passer après l'opération de copie des fichiers.
	 * 
	 * @param postUpdateScript le script à passer après la mise à jour
	 */
	public void setPostUpdateScript(String postUpdateScript) {
		this.postUpdateScript = postUpdateScript;
	}

	/**
	 * Définit le chemin keystore au format JCEKS permettant de signer les révisions
	 * 
	 * @param keyStorePath le chemin du keystore
	 */
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	/**
	 * @param keyStorePassword the keyStorePassword to set
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * @param signKeysAlias the signKeysAlias to set
	 */
	public void setSignKeysAlias(String signKeysAlias) {
		this.signKeysAlias = signKeysAlias;
	}

	/**
	 * @param signKeysAliasPassword the signKeysAliasPassword to set
	 */
	public void setSignKeysAliasPassword(String signKeysAliasPassword) {
		this.signKeysAliasPassword = signKeysAliasPassword;
	}
	
	public System createSystem() {
		System os = new System();
		osfiles.add(os);
		
		return os;
	}

	/**
	 * Crée la révision
	 */
	@Override
	public void execute() {
		Revision revision = new Revision();
		DirectoryScanner ds;	

		revision.setVersions(parseChangelog());
		
		if(preUpdateScript != null && !preUpdateScript.isEmpty()) {
			FileMetaData fmdPreScript = new FileMetaData();
			File fpre = new File(preUpdateScript);
			fmdPreScript.setPath(preUpdateScript);
			fmdPreScript.setSecurehash(Converters.byteArrayToHexString(FileUtils.getSHA256Hash(fpre)));
			fmdPreScript.setFileSize(fpre.length());
			revision.setPreUpdateScript(fmdPreScript);
		}
		
		if(postUpdateScript != null && !postUpdateScript.isEmpty()) {
			FileMetaData fmdPostScript = new FileMetaData();
			File fpost = new File(postUpdateScript);
			fmdPostScript.setPath(postUpdateScript);
			fmdPostScript.setSecurehash(Converters.byteArrayToHexString(FileUtils.getSHA256Hash(fpost)));
			fmdPostScript.setFileSize(fpost.length());
			revision.setPostUpdateScript(fmdPostScript);
		}
		
		File fFilelist = new File(new File(revisionPath).getParent(), reposName + "-filelist.txt"); //$NON-NLS-1$
		PrintStream ps = null;
		try {
			ps = new PrintStream(fFilelist);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			java.lang.System.out.println(fFilelist.getAbsolutePath());
		}
		
		// parcours le fichiers de l'application
		for (FileSet fileSet : filesets) {
			ds = fileSet.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			for (String filename : includedFiles) {
				File file = new File(ds.getBasedir().getPath() + File.separator + filename);
				// pour chacun des fichier calcul sa somme de contrôle et l'enregistre
				if (file.isFile()) {
					long checksum = FileUtils.calculChecksum(file);
					byte[] hash = FileUtils.getSHA256Hash(file);
					String path = file.getPath().replace(ds.getBasedir().getPath() + File.separator, ""); //$NON-NLS-1$
					path = path.replace('\\', '/');
					
					FileMetaData fileMetaData = new FileMetaData();
					fileMetaData.setPath(path);
					fileMetaData.setHash(checksum);
					fileMetaData.setSecurehash(Converters.byteArrayToHexString(hash));
					fileMetaData.setFileSize(file.length());
					
					if(ps != null)
						ps.println(path);

					revision.getFilesMetaData().add(fileMetaData);
				}
			}
		}
		
		//parcours les fichiers spécifique à un os
		for (System os : osfiles) {
			for (FileSet fileSet : os.filesets) {
				ds = fileSet.getDirectoryScanner(getProject());
				String[] includedFiles = ds.getIncludedFiles();
				for (String filename : includedFiles) {
					File file = new File(ds.getBasedir().getPath() + File.separator + filename);
					// pour chacun des fichier calcul sa somme de contrôle et l'enregistre
					if (file.isFile()) {
						long checksum = FileUtils.calculChecksum(file);
						byte[] hash = FileUtils.getSHA256Hash(file);
						String path = file.getPath().replace(ds.getBasedir().getPath() + File.separator, ""); //$NON-NLS-1$
						path = path.replace('\\', '/');
						
						FileMetaData fileMetaData = new FileMetaData();
						fileMetaData.setPath(path);
						fileMetaData.setHash(checksum);
						fileMetaData.setSecurehash(Converters.byteArrayToHexString(hash));
						fileMetaData.setFileSize(file.length());
						fileMetaData.setOs(os.getName());
						
						if(ps != null)
							ps.println(path);
	
						revision.getFilesMetaData().add(fileMetaData);
					}
				}
			}
		}
		
		if(ps != null) {
			ps.flush();
			ps.close();
		}
		
		for(FileSet fileSet : keysAndCerts) {
			ds = fileSet.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			for (String filename : includedFiles) {
				File file = new File(ds.getBasedir().getPath() + File.separator + filename);
				if (file.isFile()) {
					String securehash = ""; //$NON-NLS-1$
					if(file.getName().endsWith(".pem")) { //$NON-NLS-1$
						try {
							X509Certificate cert = (X509Certificate)CertUtil.readCert(file);
							
							securehash = Converters.byteArrayToHexString(cert.getSignature());
						} catch (CertificateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if(file.getName().endsWith(".key")) { //$NON-NLS-1$
						securehash = Converters.byteArrayToHexString(FileUtils.getSHA256Hash(file));
					}
					
					if(!securehash.isEmpty()) {
						String path = file.getPath().replace(ds.getBasedir().getPath() + File.separator, ""); //$NON-NLS-1$
						path = path.replace('\\', '/');
						
						FileMetaData fileMetaData = new FileMetaData();
						
						fileMetaData.setPath(path);
						fileMetaData.setSecurehash(securehash);
						fileMetaData.setFileSize(file.length());
						
						revision.getCryptoKeys().add(fileMetaData);
					}
				}
			}
		}

		try {
			if(keyStorePath != null && !keyStorePath.isEmpty()) {
				File tmpXML = File.createTempFile("rev", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					XMLSerializer.saveMarshallStructure(tmpXML, revision, false);
					
					// Load the KeyStore and get the signing key and certificate.
					KeyStore ks = KeyStore.getInstance("JCEKS"); //$NON-NLS-1$
					InputStream ksStream = new FileInputStream(keyStorePath);
					try {
						ks.load(ksStream, keyStorePassword.toCharArray());
					} finally {
						ksStream.close();
					}
					
					XMLSignatureHelper signatureHelper = new XMLSignatureHelper(ks);
					
					InputStream in = new FileInputStream(tmpXML);
					OutputStream out = new GZIPOutputStream(new FileOutputStream(new File(revisionPath)));
					try {
						signatureHelper.signXMLStream(in, out, signKeysAlias, signKeysAliasPassword.toCharArray());
					} finally {
						in.close();
						out.close();
					}
				} finally {
					//if(tmpXML != null)
					//	tmpXML.delete();
				}
			} else {
				XMLSerializer.saveMarshallStructure(new File(revisionPath), revision, true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (UnrecoverableEntryException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (MarshalException e) {
			e.printStackTrace();
		} catch (XMLSignatureException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	private List<Version> parseChangelog() {
		List<Version> versions = new ArrayList<Version>();
		if(!changelogXML.isEmpty()) {
			BufferedWriter bw = null;
			try {
				versions = XMLSerializer.loadXMLStructure(new File(changelogXML), false);
				
				bw = new BufferedWriter(new FileWriter(changelogTXT));
				for(int i = versions.size() - 1; i >= 0; i--) {
					bw.write(versions.get(i).getAppname() + " "); //$NON-NLS-1$
					bw.write("(" + versions.get(i).getVersion() + ") "); //$NON-NLS-1$ //$NON-NLS-2$
					bw.write(versions.get(i).getState() + "; urgency=low\n\n"); //$NON-NLS-1$
					bw.write(versions.get(i).getChangeInfos() + "\n\n"); //$NON-NLS-1$
					bw.write(" -- " + versions.get(i).getAuthor() + "  "); //$NON-NLS-1$ //$NON-NLS-2$
					Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
					calendar.setTime(versions.get(i).getDateVersion());
					
					String date = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH) + ", "; //$NON-NLS-1$
					date += calendar.get(Calendar.DAY_OF_MONTH) + " "; //$NON-NLS-1$
					date += calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH) + " "; //$NON-NLS-1$
					date += calendar.get(Calendar.YEAR) + " "; //$NON-NLS-1$
					date += new DecimalFormat("00").format(calendar.get(Calendar.HOUR_OF_DAY)) + ":"; //$NON-NLS-1$ //$NON-NLS-2$
					date += new DecimalFormat("00").format(calendar.get(Calendar.MINUTE)) + ":"; //$NON-NLS-1$ //$NON-NLS-2$
					date += new DecimalFormat("00").format(calendar.get(Calendar.SECOND)) + " "; //$NON-NLS-1$ //$NON-NLS-2$
					date += "+" + new DecimalFormat("00").format(calendar.get(Calendar.ZONE_OFFSET) / 1000 / 60 / 60) + "00";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					bw.write(date + "\n\n"); //$NON-NLS-1$
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try { bw.close(); } catch (IOException e) {}
			}
		}
		return versions;
	}
	
	public class System {
		private final List<FileSet> filesets = new ArrayList<FileSet>();
		private String name = "all"; //$NON-NLS-1$
		
		/**
		 * Ajoute la collection de fichier à attacher à la révision
		 * 
		 * @param fileset la collection de fichier à attacher à la révision
		 */
		public void addFileset(FileSet fileset) {
			filesets.add(fileset);
		}
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}		
	}
}
