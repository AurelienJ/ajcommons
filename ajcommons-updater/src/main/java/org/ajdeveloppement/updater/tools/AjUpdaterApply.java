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
package org.ajdeveloppement.updater.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.io.FileUtils;

/**
 * <p>Outils autonome et multiplateforme d'application de fichiers de mise à jour.</p>
 * <p>Sur la plupart des systèmes, le processus doit disposer des privilèges administrateurs</p>
 *
 * <p>S'éxecute en lancant la commande <code>java -cp ajcommons.jar org.ajdeveloppement.updater.AjUpdaterApply [sourcePath] [destPath]</code><br>
 * Les chemins sourcePath et destPath sont obligatoires<br>
 * <i>sourcePath</i> doit contenir un sous-dossier <i>files</i> contenant les fichiers à déplacer ainsi
 * qu'un ou plusieurs fichiers <i>[nomdepot]-filelist.txt</i> contenant les chemins relatif de chacun des fichiers de
 * l'application géré par le ou les dépôts.
 * </p>
 *
 * <p>
 * L'utilisateur peut interdire l'application de certain fichier de mise à jour contenue dans sourcePath
 * en définissant un fichier <i>overwrite-blacklist.txt</i> à la racine de l'application et contenant les
 * chemins relatifs des fichiers à ne pas écraser à raison de 1 par ligne.
 * </p>
 *
 * <p>
 * Un script javascript peut être executer avant la copie de fichier en plancant le fichier .js
 * dans le répertoire [sourcePath]/scripts/pre
 * <br>
 * De même, un script peut être executé après la copie en plancant le fichier .js
 * dans le répertoire [sourcePath]/scripts/post
 * </p>
 *
 *
 * @author Aurélien JEOFFRAY
 *
 */
public class AjUpdaterApply {

	private static AjResourcesReader labels = new AjResourcesReader("org.ajdeveloppement.updater.labels"); //$NON-NLS-1$

	/**
	 * 
	 * @param args first arg: source path for update, second: destination path
	 */
	public static void main(String[] args) {
		if(args.length >= 2) {
			String updatePath = args[0];
			String basePath = args[1];

			System.out.println("source: " + updatePath); //$NON-NLS-1$
			System.out.println("dest: " + basePath); //$NON-NLS-1$

			applyUpdate(updatePath, basePath);
		} else {
			System.err.println("bad params length"); //$NON-NLS-1$
		}
	}

	/**
	 * Applique la mise à jour en deplacant les fichiers du répertoire temporaires
	 * de mise à jour vers le répertoire de l'application.
	 *
	 * @param updatePath le répertoire temporaire contenant les fichiers de mise à jour
	 * @param basePath le répertoire de base dans lequel appliquer les fichiers
	 */
	public static void applyUpdate(String updatePath, String basePath) {
		boolean success = false;
		boolean copySuccess = true;
		boolean preScriptSuccess = true;

		List<File> preScript = FileUtils.listAllFiles(new File(updatePath, "scripts" + File.separator + "pre"), ".*\\.js"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(preScript.size() > 0) {
			try {
				execScript(preScript.get(0));

				FileUtils.deleteFilesPath(new File(updatePath, "scripts" + File.separator + "pre")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (ScriptException e) {
				preScriptSuccess = false;
				e.printStackTrace();
			} catch (IOException e) {
				preScriptSuccess = false;
				e.printStackTrace();
			}
		}

		if(preScriptSuccess) {

			//liste tous les fichiers à mettre à jour
			List<File> updateFiles = FileUtils.listAllFiles(new File(updatePath, "files"), ".*"); //$NON-NLS-1$ //$NON-NLS-2$
			List<String> blacklist = new ArrayList<String>();
			//recherche la présence d'une blacklist d'écrasement
			File blacklistFile = new File(basePath, "overwrite-blacklist.txt"); //$NON-NLS-1$
			Scanner scanner = null;
			Scanner updateScanner = null;

			if(blacklistFile.exists()) {
				try {
					scanner = new Scanner(blacklistFile);
					while(scanner.hasNextLine()) {
						try {
							String blackedfile = scanner.nextLine();
							blacklist.add(blackedfile);
						} catch (NoSuchElementException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if(scanner != null)
						scanner.close();
					scanner = null;
				}
			}

			try {
				PrintStream deletedFilesLog = new PrintStream(new File(basePath, "deletedFiles.log")); //$NON-NLS-1$
				try {
					//si présence de fichiers xxx-filelist.txt, comparer les entrées avec le fichier correspondant dans update
					//supprimer toutes les entrées qui ne sont pas dans la version update
					for(File fileListing : FileUtils.listAllFiles(new File(updatePath, "files"), ".*-filelist\\.txt")) { //$NON-NLS-1$ //$NON-NLS-2$
						if(new File(basePath, fileListing.getName()).exists()) {
							try {
								updateScanner = new Scanner(fileListing);
								scanner = new Scanner(new File(basePath, fileListing.getName()));

								List<String> newFileList = new ArrayList<String>();
								while(updateScanner.hasNextLine()) {
									newFileList.add(updateScanner.nextLine());
								}

								List<String> oldFileList = new ArrayList<String>();
								while(scanner.hasNextLine()) {
									oldFileList.add(scanner.nextLine());
								}

								//supprime les fichiers qui sont dans l'ancienne filelist
								//et pas dans la nouvelle
								for(String file : oldFileList) {
									if(!newFileList.contains(file) && !blacklist.contains(file)) {
										new File(basePath, file.replace('/', File.separatorChar)).delete();

										deletedFilesLog.println(file);
									}
								}

								//Interdit la mise à jour des fichier qui existe déjà
								//mais qui ne sont pas dans l'ancienne filelist.
								//Permet d'éviter la corruption des fichiers d'un dépôt par un autre.
								for(String file : newFileList) {
									if(!oldFileList.contains(file) && new File(basePath, file).exists()) {
										blacklist.add(file);
									}
								}
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} finally {
								if(updateScanner != null)
									updateScanner.close();
								if(scanner != null)
									scanner.close();
							}
						}
					}
				} finally {
					deletedFilesLog.flush();
					deletedFilesLog.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				//pour chacun des fichier
				for(File updateFile : updateFiles) {
					//transforme le chemin absolu en chemin relatif
					String relativePath = updateFile.getPath().replace(updatePath + File.separator + "files" + File.separator, ""); //$NON-NLS-1$ //$NON-NLS-2$
					//construit le chemin de destination du fichier
					File destDirectory = new File(basePath, relativePath).getParentFile();
					if(!destDirectory.exists())
						destDirectory.mkdirs();

					//copie le fichier si il n'est pas blacklisté
					if(!blacklist.contains(relativePath)) {
						Files.copy(updateFile.toPath(), destDirectory.toPath().resolve(updateFile.getName()), StandardCopyOption.REPLACE_EXISTING);
						//FileUtils.copyFile(updateFile, destDirectory);
					}
					//supprime la source
					updateFile.delete();
				}

				//supprime les fichiers résiduel du répertoire temporaire
				FileUtils.deleteFilesPath(new File(updatePath, "files")); //$NON-NLS-1$

				success = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				copySuccess = false;
			} catch (IOException e) {
				e.printStackTrace();
				copySuccess = false;
			}
		}

		if(success) {
			List<File> postScript = FileUtils.listAllFiles(new File(updatePath, "scripts" + File.separator + "post"), ".*\\.js"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(postScript.size() > 0) {
				try {
					execScript(postScript.get(0));

					FileUtils.deleteFilesPath(new File(updatePath, "scripts")); //$NON-NLS-1$
				} catch (ScriptException e) {
					success = false;
					e.printStackTrace();
				} catch (IOException e) {
					success = false;
					e.printStackTrace();
				}
			}
		}

		if(success) {
			JOptionPane.showMessageDialog(
					null,
					labels.getResourceString("update.finish.sucessfull"), //$NON-NLS-1$
					labels.getResourceString("update.finish.sucessfull.title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		} else if(!copySuccess) {
			JOptionPane.showMessageDialog(
					null,
					labels.getResourceString("update.error.copy"), //$NON-NLS-1$
					labels.getResourceString("update.error.copy.title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(
					null,
					labels.getResourceString("update.error.script"), //$NON-NLS-1$
					labels.getResourceString("update.error.script.title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static void execScript(File script) throws ScriptException, IOException {
		ScriptEngine scriptEngine = null;
		ScriptEngineManager se = new ScriptEngineManager();
		//for(ScriptEngineFactory fact : se.getEngineFactories())
		//	System.out.println(fact.getEngineName());
		scriptEngine = se.getEngineByName("JavaScript");   //$NON-NLS-1$
		if(scriptEngine != null) {
			FileReader scriptReader = new FileReader(script);
			scriptEngine.eval(scriptReader);
			scriptReader.close();
		} else
			JOptionPane.showMessageDialog(
					null,
					labels.getResourceString(labels.getResourceString("update.error.jssupport")), //$NON-NLS-1$
					"Error js", //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
	}
}
