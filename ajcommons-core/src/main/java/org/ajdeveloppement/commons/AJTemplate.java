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
package org.ajdeveloppement.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Moteur de template, permet à partir d'une chaine de template ou d'un fichier,
 * de generer une chaine de sortie formaté.</p>
 * 
 * <p>Un fichier de template est un fichier texte HTML ou XML auquel on ajoute:
 * <ul>
 * 	<li>Des constantes de localisation représenté par un champs entre signe '%'. ex: %template.title%<br>
 * Les chaînes localisé sont récupéré au travers d'une instance de la class {@link AjResourcesReader} </li>
 *  <li>Des variables remplacé en cours d'execution et représenté entre accolade. ex: {mavariable}</li>
 *  <li>Des balises &lt;bloc::monbloc&gt;&lt;/bloc::monbloc&gt; représentant une section optionnel ou
 *  sur laquelle boucler</li>
 *  <li>Si un signe % doit être représenté dans la chaine parser, celui ci doit être précédé d'un '\' (antislash)
 *  dans le template</li>
 * </ul>
 * </p>
 * <p>
 * Exemple de template:
 * <code><pre>
 * &lt;html&gt;
 * 	&lt;body&gt;
 *		&lt;table&gt;
 *			&lt;!-- Entete de tableau avec des constantes de localisation--&gt;
 *			&lt;th&gt;&lt;td&gt;%tableau.cellule1%&lt;/td&gt;&lt;td&gt;%tableau.cellule2%&lt;/td&gt;&lt;/th&gt;
 *			&lt;!-- boucle sur les lignes du tableau --&gt;
 *			&lt;bloc::ligne&gt;
 *			&lt;tr&gt;&lt;td&gt;{cell1}&lt;/td&gt;&lt;td&gt;{cell2}&lt;/td&gt;&lt;/tr&gt;
 *			&lt;/bloc::ligne&gt;
 *		&lt;/table&gt;
 * 	&lt;/body&gt;
 * &lt;/html&gt;
 * </pre></code>
 * </p>
 * <p>
 * Code de traitement du template:
 * <code><pre>
 * //charge le fichier de localisation lié
 * AjResourcesReader localisation = new AjResourcesReader("localisation.properties");
 * 
 * //initialise le moteur de template
 * AJTemplate template = new AJTemplate(localisation);
 * 
 * //charge le fichier template décrit plus haut
 * template.loadTemplate("chemin/vers/le/fichier.template");
 * 
 * for(int i = 0; i = 5; i++ {
 *  //remplace les variables cell1 et cell2 du bloc ligne par une valeur.
 *  //si plusieurs blocs sont imbriqué les référencer dans l'ordre ex: ligne.colonne.cell1
 * 	template.parse("ligne.cell1", i*5);
 * 	template.parse("ligne.cell2", (double)i/5.0);
 * 
 *  //duplique le bloc ligne
 *  template.loopBloc("ligne");
 * }
 * 
 * //retourne le résultat du parsing
 * String resultat = template.output();
 * </pre></code>
 * </p>
 * @author Aurélien Jeoffray
 * @version 2.0
 */
public class AJTemplate implements Cloneable {
	private AjResourcesReader l10n;
	
	private String template = new String();

	private Map<String, AJTemplate> blocs = new HashMap<String, AJTemplate>();
	private Map<String, StringBuilder> blocsLoopSubstitute = new HashMap<String, StringBuilder>();
	private Map<String, String> blocsSubstitute = new HashMap<String, String>();
	private Map<String, String> variables = new HashMap<String, String>();

	/**
	 * Creer un nouveau template vide
	 * 
	 */
	public AJTemplate() {

	}
	
	/**
	 * Créer un nouveau template gérant la localisation de celui ci
	 * 
	 * @param l10n le fichier de localisation associé
	 */
	public AJTemplate(AjResourcesReader l10n) {
		this.l10n = l10n;
	}

	/**
	 * Creer un nouveau template a partir d'une chaine de caractere
	 * 
	 * @param template -
	 *            la chaine à parser
	 */
	public AJTemplate(String template) {
		this(template, null);
	}
	
	/**
	 * Creer un nouveau template a partir d'une chaine de caractere. Le template
	 * soumis peut être localisé
	 * 
	 * @param template la chaine à parser
	 * @param l10n le fichier de localisation associé
	 */
	public AJTemplate(String template, AjResourcesReader l10n) {
		this.template = template;
		this.l10n = l10n;

		analyseTemplate();
	}
	
	private void pushInBlocsSubstitute(String blocName, String content) {
		if(!blocsLoopSubstitute.containsKey(blocName)){
			blocsLoopSubstitute.put(blocName, new StringBuilder(content));
		} else {
			blocsLoopSubstitute.get(blocName).append(content);
		}
	}
	
	/**
	 * Analyse un template
	 * 
	 */
	private void analyseTemplate() {
		//recherche des chaines localisable
		if(l10n != null) {
			Pattern pattern = Pattern.compile("[^\\\\]%([^%]+)%"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(template);
			while(matcher.find()) {
				String LocalizableString = matcher.group(1);

				String l10nString = l10n.getResourceString(LocalizableString ); 
				template = template.replace("%" + LocalizableString + "%", l10nString);  //$NON-NLS-1$//$NON-NLS-2$
			}
			template = template.replace("\\%", "%"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// recherche les blocs dans le template
		listBloc(template);
		// nettoie le template en supprimant le contenue des blocs
		purgeTemplate();
	}

	/**
	 * recherche et formate tous les blocs du template
	 * 
	 * @param searchDomain la chaine de template a analyser
	 */
	private void listBloc(String searchDomain) {
		int index = 0;
		String blocString = "<bloc::"; //$NON-NLS-1$

		while (index < searchDomain.length()) {
			// determine le nom du bloc
			int startBloc = searchDomain.indexOf(blocString, index);
			if (startBloc > -1) {
				int endStartBloc = searchDomain.indexOf(">", startBloc + blocString.length()); //$NON-NLS-1$
				String blocName = searchDomain.substring(startBloc + blocString.length(), endStartBloc);

				// determine la fin du bloc
				String blocCloseString = "</bloc::" + blocName + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				int endBloc = searchDomain.lastIndexOf(blocCloseString);

				if (endBloc > -1 && endStartBloc > -1 && endBloc >= endStartBloc) {
					// genere le bloc

					blocs.put(blocName, new AJTemplate(searchDomain.substring(endStartBloc + 1, endBloc)));

					index = endBloc + blocCloseString.length();
				} else {
					System.err.println("Fin du bloc " + blocName + " manquante"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
			} else
				break;
		}
	}

	/**
	 * Supprime pour accelerer le parsing tous le code inutile
	 * 
	 */
	private void purgeTemplate() {
		// supprimer les bloc content
		StringBuffer templateBuffer = new StringBuffer(template);
		for (String key : blocs.keySet()) {
			String blocString = "<bloc::" + key + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			int startBloc = templateBuffer.indexOf(blocString);
			int endBloc = templateBuffer.indexOf("</bloc::" + key + ">", startBloc + blocString.length()); //$NON-NLS-1$ //$NON-NLS-2$
			if (startBloc > -1) {
				templateBuffer.replace(startBloc + blocString.length(), endBloc, ""); //$NON-NLS-1$
			}
		}

		template = templateBuffer.toString();
	}
	
	/**
	 * Définit la resource de localisation du template
	 * 
	 * @param l10n la resource de localisation
	 */
	public void setLocalisationReader(AjResourcesReader l10n) {
		this.l10n = l10n;
	}

	/**
	 * Charge un fichier de template. Le fichier doit être dans un encodage UTF-8
	 * 
	 * @param path le chemin du fichier de template à charger
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void loadTemplate(String path) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		loadTemplate(new File(path).toURI().toURL());
	}
	
	/**
	 * Charge un template à partir d'un fichier. Le fichier doit être dans un encodage UTF-8
	 * 
	 * @param url le chemin du fichier de template à charger
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public void loadTemplate(URL url) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		try(BufferedReader xmlReader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) { //$NON-NLS-1$
			template = new String();
			while (xmlReader.ready()) {
				template += xmlReader.readLine() + "\n"; //$NON-NLS-1$
			}
		}

		analyseTemplate();
	}

	/**
	 * Réinitialise intégralement le template
	 * 
	 */
	public void reset() {
		for (AJTemplate bloc : blocs.values()) {
			bloc.reset();
		}
		
		variables.clear();
		blocsLoopSubstitute.clear();
		blocsSubstitute.clear();
	}

	/**
	 * Renvoie la chaine de template d'origine
	 * 
	 * @return la chaine d'origine
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Returns the subbloc map of template
	 * 
	 * @return the blocs
	 */
	public Map<String, AJTemplate> getBlocs() {
		return blocs;
	}

	/**
	 * remplace l'element 'search' par 'replace' dans le template
	 * 
	 * @param search la chaine à rechercher
	 * @param replace la chaine de remplacement
	 */
	public void parse(String search, String replace) {
		if(search != null && !search.isEmpty()) {
			String[] path = search.split("\\."); //$NON-NLS-1$
			if (path.length == 1) {
				variables.put(search, replace);
			} else if(blocs.containsKey(path[0])) {
				blocs.get(path[0]).parse(search.substring(path[0].length() + 1), replace);
			}
		}
	}

	/**
	 * remplace le contenu d'un bloc
	 * 
	 * @param searchBloc le bloc à remplacer
	 * @param replace la chaine de remplacement
	 */
	public void parseBloc(String searchBloc, String replace) {
		if(searchBloc != null && !searchBloc.isEmpty()) {
			String[] path = searchBloc.split("\\."); //$NON-NLS-1$
			if (path.length == 1) {
				blocsSubstitute.put(searchBloc, replace);
			} else if(blocs.containsKey(path[0])) {
				blocs.get(path[0]).parseBloc(searchBloc.substring(path[0].length() + 1), replace);
			}
		}
	}
	
	/**
	 * After usage of parseBloc, if you want reuse bloc template
	 * you must invoke this methodbefore
	 * 
	 * @param searchBloc
	 */
	public void useBlocTemplate(String searchBloc) {
		if(searchBloc != null && !searchBloc.isEmpty()) {
			String[] path = searchBloc.split("\\."); //$NON-NLS-1$
			if (path.length == 1) {
				blocsSubstitute.remove(searchBloc);
			} else if(blocs.containsKey(path[0])) {
				blocs.get(path[0]).useBlocTemplate(searchBloc.substring(path[0].length() + 1));
			}
		}
	}

	/**
	 * Repete le template d'un bloc
	 * 
	 * @param searchBloc le bloc à repeter
	 */
	public void loopBloc(String searchBloc) {
		if(searchBloc != null && !searchBloc.isEmpty()) {
			String[] path = searchBloc.split("\\."); //$NON-NLS-1$
			if(blocs.containsKey(path[0])) {
				if (path.length == 1) {
					if(blocsSubstitute.containsKey(path[0])) {
						pushInBlocsSubstitute(path[0],blocsSubstitute.get(path[0]));
						blocsSubstitute.remove(path[0]);
					} else {
						pushInBlocsSubstitute(path[0], blocs.get(path[0]).output());
						blocs.get(path[0]).reset();
					}
				} else {
					String subBloc = searchBloc.substring(path[0].length() + 1);
					blocs.get(path[0]).loopBloc(subBloc);
				}
			}
		}
	}

	/**
	 * genere la chaine de sortie
	 * 
	 * @return La chaine de sortie
	 */
	public String output() {
		String temp = template;
		
		for(String key : variables.keySet()) {
			if(variables.get(key) != null)
				temp = temp.replace("{" + key + "}", variables.get(key));  //$NON-NLS-1$//$NON-NLS-2$
		}
		
		for (String key : blocs.keySet()) {
			String blocContent = null;
			if(blocsLoopSubstitute.containsKey(key))
				blocContent = blocsLoopSubstitute.get(key).toString();
			else if(blocsSubstitute.containsKey(key)) {
				blocContent = blocsSubstitute.get(key);
			} else
				blocContent = blocs.get(key).output();
			temp = temp.replace("<bloc::" + key + "></bloc::" + key + ">", blocContent); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return temp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public AJTemplate clone() throws CloneNotSupportedException {
		AJTemplate clone = (AJTemplate)super.clone();
		
		Map<String, AJTemplate> clonedBlocs = new HashMap<String, AJTemplate>();
		for (Entry<String, AJTemplate> entry : clone.blocs.entrySet()) {
			clonedBlocs.put(entry.getKey(), entry.getValue().clone());
		}
		
		clone.blocs = clonedBlocs;
		clone.blocsLoopSubstitute = new HashMap<String, StringBuilder>(blocsLoopSubstitute);
		clone.blocsSubstitute = new HashMap<String, String>(blocsSubstitute);
		clone.variables = new HashMap<String, String>(variables);
		
		return clone;
	}
}