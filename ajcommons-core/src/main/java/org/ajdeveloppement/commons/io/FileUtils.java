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
package org.ajdeveloppement.commons.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utilitaire de manipulation et traitement de fichiers
 * (Copie, Compression, Listage, Suppression, Checksum, ...)
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class FileUtils {
	private static Pattern rgxFilenameValidator = Pattern.compile("[^<>:\"/\\\\\\|\\?\\*%]*", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
	/**
	 * Copie un fichier vers une destination donné<br>
	 * La date de modification du fichier est préservé
	 * La copie preserve les attribues (lecture, ecriture, execution)
	 * 
	 * @param srcPath le fichier à copier
	 * @param destPath emplacement de destination du fichier copié. Si destPath représente un répertoire,
	 * le nom du fichier d'origine est récupéré, sinon le fichier est renommé
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	//@Deprecated
	public static void copyFile(File srcPath, File destPath) throws 
			FileNotFoundException, IOException {
    	copyFile(srcPath, destPath, true);
    }
	
	/**
	 * Copie un fichier vers une destination donné<br>
	 * La date de modification du fichier est préservé.
	 * La copie preserve les attribues (lecture, ecriture, execution)
	 * 
	 * @param srcPath le fichier à copier
	 * @param destPath emplacement de destination du fichier copié. Si destPath représente un répertoire,
	 * le nom du fichier d'origine est récupéré, sinon le fichier est renommé
	 * @param overwrite si true, et qu'il existe déjà un fichier sous la destination, alors l'écraser.
	 * si false, invoquer une erreur de type IOException
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void copyFile(File srcPath, File destPath, boolean overwrite)
			throws FileNotFoundException, IOException {
		File newPath;
		if (destPath.isDirectory())
			newPath = new File(destPath, srcPath.getName());
		else
			newPath = destPath;

		if (newPath.exists()) {
			if(overwrite)
				newPath.delete();
			else
				throw new IOException("File already exists"); //$NON-NLS-1$
		}
		
		newPath.getParentFile().mkdirs();


		try (FileInputStream fis = new FileInputStream(srcPath); FileOutputStream fos = new FileOutputStream(newPath)) {
			FileChannel fcin = fis.getChannel();
			FileChannel fcout = fos.getChannel();
			
			fcin.transferTo(0, fcin.size(), fcout);
		}

		newPath.setLastModified(srcPath.lastModified());
		newPath.setExecutable(srcPath.canExecute());
		newPath.setReadable(srcPath.canRead());
		newPath.setWritable(srcPath.canWrite());
	}
	
	/**
	 * Enregistre un flux dans un fichier, puis ferme le flux concerné
	 * 
	 * @param in le flux à sauvegarder
	 * @param outputFile le fichier dans lequel enregstrer le flux
	 * @throws IOException
	 */
	public static void dumpStreamToFile(InputStream in, File outputFile) throws IOException {
		dumpStreamToFile(in, outputFile, true);
	}
	
	/**
	 * Enregistre un flux dans un fichier
	 * 
	 * @param in le flux à sauvegarder
	 * @param outputFile le fichier dans lequel enregstrer le flux
	 * @param closeSourceStream si <code>true</code>, ferme le flux d'origine sinon le laisse ouvert
	 * @throws IOException
	 */
	public static void dumpStreamToFile(InputStream in, File outputFile, boolean closeSourceStream) throws IOException {
		dumpStreamToFile(in, outputFile, closeSourceStream, -1);
	}
	
	public static void dumpStreamToFile(InputStream in, File outputFile, boolean closeSourceStream, long totalFileLength) throws IOException {
		if(outputFile.getParentFile() != null)
			outputFile.getParentFile().mkdirs();
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		
		try {
			byte[] buffer = new byte[1024];
			long readedLength = 0;
			int nbReadByte = 0;
			while((nbReadByte = in.read(buffer)) > -1) {
				out.write(buffer, 0, nbReadByte);
				readedLength +=nbReadByte;
				if(totalFileLength > -1 && readedLength >= totalFileLength)
					break;
			}
			out.flush();
		} finally {
			try { out.close(); } catch(IOException e) {}
			if(closeSourceStream)
				try { in.close(); } catch(IOException e) {}
		}
	}
	
	/**
	 * Compresse un fichier en gz (GNU Zip)
	 *  
	 * @param srcPath le fichier à compresser
	 * @param destPath le fichier compresser
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void compressFile(File srcPath, File destPath)
			throws FileNotFoundException, IOException {
		File newPath;
		if(destPath.isDirectory()) {
			newPath = new File(destPath, srcPath.getName() + ".gz"); //$NON-NLS-1$
			if (newPath.exists()) {
				newPath.delete();
			}
		} else {
			newPath = destPath;
		}
		newPath.getParentFile().mkdirs();
		FileInputStream fis = null;
		GZIPOutputStream gzos = null;
		try {
			fis = new FileInputStream(srcPath);
			gzos = new GZIPOutputStream(new FileOutputStream(newPath));

			byte[] buffer = new byte[512 * 1024];
			int nbLecture;
			while ((nbLecture = fis.read(buffer)) != -1) {
				gzos.write(buffer, 0, nbLecture);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (fis != null)
					fis.close();
				if (gzos != null)
					gzos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Décompresse un fichier compressé en gz (GNU Zip)
	 * 
	 * @param srcPath le fichier compresser
	 * @param destPath le chemin du  fichier décompressé
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void uncompressFile(File srcPath, File destPath)
			throws FileNotFoundException, IOException {
		File newPath;
		if(destPath.isDirectory()) {
			newPath = new File(destPath, srcPath.getName().substring(0, srcPath.getName().lastIndexOf('.')));
			if (newPath.exists()) {
				newPath.delete();
			}
		} else {
			newPath = destPath;
		}
		newPath.getParentFile().mkdirs();
		GZIPInputStream gzis = null;
		FileOutputStream fos = null;
		try {
			gzis = new GZIPInputStream(new FileInputStream(srcPath));
			fos = new FileOutputStream(newPath);

			byte[] buffer = new byte[512 * 1024];
			int nbLecture;
			while ((nbLecture = gzis.read(buffer)) != -1) {
				fos.write(buffer, 0, nbLecture);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (gzis != null)
					gzis.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
	/**
     * Liste tous le fichier dans la sous arborescence correspondant au masque
     * 
     * @param path - le chemin de base de recherche
     * @param mask - le masque de selection
     * @return - List<File> - les fichiers correspondant au critere
     */
	public static List<File> listAllFiles(File path, String mask) {
		return listAllFiles(path, mask, false);
	}
	
	/**
     * Liste tous le fichier dans la sous arborescence correspondant au masque
     * 
     * @param path - le chemin de base de recherche
     * @param mask - le masque de selection
     * @param includefolder - est ce que les dossier doivent être inclu dans la liste?
     * 
     * @return - List<File> - les fichiers correspondant au critere
     */
    public static List<File> listAllFiles(File path, String mask, boolean includefolder) {
        List<File> lisdocs = new ArrayList<File>();
        
        if(path != null) {
            //liste le contenu du repertoire donné en parametre
            File[] subpath = path.listFiles();
            if(subpath != null) {
                for(File filepath : subpath) {
                    //si c'est un repertoire, procédure recursive
                    if(filepath.isDirectory()) {
                        lisdocs.addAll(listAllFiles(filepath, mask, includefolder));
                        if(includefolder)
                    		lisdocs.add(filepath);
                    } else {
                        //sinon ajout du fichier à la liste
                        if(mask == null || filepath.getName().matches(mask))
                            lisdocs.add(filepath);
                    }
                }
            }
        }
        
        return lisdocs;
    }
    
    /**
     * Supprime toutes l'arborescence d'un chemin donnée
     * 
     * @param path - Le chemin à supprimé
     * @return <code>true</code> if delete tree path success, <code>false</code> else
     * @throws IOException invoqué en cas d'echec de suppression de l'arborescence
     */
    public static boolean deleteFilesPath(File path) 
    		throws IOException {
    	boolean resultat = true; 
        
        if (path.exists() && path.isDirectory()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					resultat &= deleteFilesPath(files[i]);
				} else {
					resultat &= files[i].delete();
				}
			}
		}
		resultat &= path.delete(); 
        return(resultat);
    }
    
    /**
	 * Calcul la somme de controle CRC32 du fichier donné en paramêtre
	 * 
	 * @param file - la fichier pour lequel calculer la somme de controle
	 * @return la somme de controle du fichier
	 */
	public static long calculChecksum(File file) {
		long checksum = 0;
		try(CheckedInputStream cis = new CheckedInputStream(new FileInputStream(file), new Adler32())) {
			byte[] tempBuf = new byte[128];
			while (cis.read(tempBuf) >= 0) {
			}
			checksum = cis.getChecksum().getValue();
		} catch (IOException e) {
			System.err.println("Error occuring during the Checksum calcul"); //$NON-NLS-1$	
		}
		return checksum;
	}
	
	/**
	 * Retourne l'emprunte SHA-256 du fichier fournit en paramètre
	 * 
	 * @param file le fichier pour lequel calculer l'emprunte SHA-256 
	 * @return tableau d'octet représentant l'emprunte SHA-256 du fichier ou null si le calcul
	 * échoue. En cas d'échec, la méthode renvoie silencieusement sur la sortie standard d'erreur
	 * la raison de l'échec.
	 */
	public static byte[] getSHA256Hash(File file) {

		byte[] hash = null;
		try(DigestInputStream dis = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("SHA-256"))){ //$NON-NLS-1$
			byte[] tempBuf = new byte[128];

			while (dis.read(tempBuf) >= 0) {
			}
			hash = dis.getMessageDigest().digest();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Error occuring during the Checksum calcul"); //$NON-NLS-1$	
		} catch (Exception e) {
			System.err.println("Error occuring during the Checksum calcul, No SHA-256 digester found"); //$NON-NLS-1$
			e.printStackTrace();
		}
		return hash;
	}

	
	/**
	 * Ordonne une liste de fichier fournit en parametre par
	 * ordre de date
	 * 
	 * @param unorderedListFile la liste de fichier à ordonner
	 * @param asc l'ordre de tri, true pour croissant, false pour decroissant
	 */
	public static void orderByDate(List<File> unorderedListFile, boolean asc) {
		Collections.sort(unorderedListFile, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o2.lastModified() > o1.lastModified() ? -1 : 1;
			}
			
		});
		if(!asc)
			Collections.reverse(unorderedListFile);
	}
	
	/**
	 * Ordonne une liste de fichier sous forme de tableau fournit en parametre par
	 * ordre de date
	 * 
	 * @param unorderedListFile la liste de fichier à ordonner
	 * @param asc l'ordre de tri, true pour croissant, false pour decroissant
	 */
	public static void orderByDate(File[] unorderedListFile, final boolean asc) {
		Arrays.sort(unorderedListFile, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int i = o2.lastModified() > o1.lastModified() ? -1 : 1;
				
				if(!asc)
					i = -i;
				
				return i;
			}
			
		});
	}
	
	/**
	 * Retourne <code>true</code> si le nom de fichier est valide et <code>false</code>
	 * si le nom contient des caractères invalide
	 * 
	 * @param filename le nom du fichier à tester
	 * @return la validité du nom
	 */
	public static boolean isValidFilename(String filename) {
		return rgxFilenameValidator.matcher(filename).matches();
	}
	
	/**
	 * Unzip an archive to specified directory
	 * 
	 * @param archive the archive to unzip
	 * 
	 * @param destinationDirectory the destination directory
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void unzipToDirectory(File archive, File destinationDirectory) throws MalformedURLException, IOException {
		try(ZipInputStream zipStream = new ZipInputStream(archive.toURI().toURL().openStream())) {
			ZipEntry entry;
			while((entry = zipStream.getNextEntry())!=null) {
				 File outpath = new File(destinationDirectory, entry.getName());
				 if(entry.isDirectory())
					 outpath.mkdirs();
				 else
					 FileUtils.dumpStreamToFile(zipStream, outpath, false);
			}
		}
	}
}
