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
package org.ajdeveloppement.commons.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.ajdeveloppement.commons.AJTemplate;

/**
 * Analyse un fichier de script SQL et le transforme en batch JDBC<br>
 * Les lignes commençant par -- son considéré comme des commentaires<br>
 * Toutes requête sql doit se terminer par ;<br>
 * <br>
 * Un tag "-- @release [numRelease]" peut être précisé en commentaire du script afin
 * de préciser la version de la base à laquelle celui ci s'applique.<br>Ce commentaire
 * peut se placer n'import ou dans le script, mais toujours en début de section à marqué.
 * Un fichier peut contenir plusieurs section avec un numéro de révision différent<br>
 * Les sections doivent être dans l'ordre logique<br>
 * ex:<br>
 * <pre>
 * -- @release 0
 * INSERT INTO MATABLE VALUES(1);
 * -- @release 1
 * INSERT INTO MATABLE VALUES(2);
 * -- @release 0 <- Ne marche pas
 * INSERT INTO MATABLE VALUES(3);
 * </pre>
 *
 * @author Aurélien JEOFFRAY
 *
 */
public class SqlParser {

	/**
	 * <p>Crée un batch JDBC à partir d'un fichier de script SQL</p>
	 * <p>Le script SQL doit être encodé en UTF-8</p>
	 *
	 * @param sqlfile - Le fichier de script sql
	 * @param statement - le statement de connexion à la base à utiliser pour créer le batch
	 * @param vars - les variable de substitution à exploiter dans le script
	 *
	 * @return le numéro de la revision de base correspondant au script compilé
	 */
	public static int createBatch(File sqlfile, Statement statement, Map<String, String> vars) {
		return createBatch(sqlfile, statement, vars, 0);
	}

	/**
	 * <p>Crée un batch JDBC à partir d'un fichier de script SQL à la seul condition
	 * que la révision du script soit >= à la révision de la base.</p>
	 * <p>Le script SQL doit être encodé en UTF-8</p>
	 *
	 * @param sqlfile - Le fichier de script sql
	 * @param statement - le statement de connexion à la base à utiliser pour créer le batch
	 * @param vars - les variable de substitution à exploiter dans le script
	 * @param minRelease - la révision minimal que doit avoir le fichier script pour
	 * être compilé
	 *
	 * @return le numéro de la revision de base correspondant au script compilé
	 */
	public static int createBatch(File sqlfile, Statement statement, Map<String, String> vars, int minRelease) {
		int sqlFileRelease = 1;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sqlfile), Charset.forName("UTF-8"))); //$NON-NLS-1$
			String line;
			String buff = ""; //$NON-NLS-1$

			AJTemplate template;

			while((line = bufferedReader.readLine()) != null) {
				if(!line.trim().startsWith("--")) { //$NON-NLS-1$
					if(sqlFileRelease >= minRelease) {
						buff += line;
						if(line.endsWith(";")) { //$NON-NLS-1$

							//substitue si nécessaire les variables transmisent en paramètre
							if(vars != null) {
								template = new AJTemplate(buff);

								for(Map.Entry<String, String> entry : vars.entrySet()) {
									template.parse(entry.getKey(), entry.getValue());
								}
								buff = template.output();
							}

							//ajoute la ligne de script au batch
							statement.addBatch(buff.trim());

							buff = ""; //$NON-NLS-1$
							template = null;
						}
					}
				} else if(line.indexOf("@release") > -1) { //$NON-NLS-1$
					String release = "@release"; //$NON-NLS-1$

					try {
						sqlFileRelease = Integer.parseInt(line.substring(line.indexOf(release) + release.length() + 1));
					} catch (NumberFormatException e) {
						//ignorer l'erreur, ne simplement pas prendre en compte la valeur
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { if(bufferedReader != null) bufferedReader.close(); } catch (Exception e) {}
		}

		return sqlFileRelease;
	}

	/**
	 * Formate une chaîne de caractère pour qu'elle soit valide dans une requête SQL
	 *
	 * @param unformattedString la chaîne à formater
	 * @param acceptWildcardChar si false, alors echape tous les caractères WildCard
	 *
	 * @return la chaîne formaté pour SQL
	 */
	@SuppressWarnings("nls")
	public static String formatStringValue(String unformattedString, boolean acceptWildcardChar) {
		String formattedString = unformattedString.replaceAll("'", "''");
		if(!acceptWildcardChar)
			formattedString = formattedString.replaceAll("%", "\\%").replaceAll("_", "\\_");
		return formattedString;

	}
}