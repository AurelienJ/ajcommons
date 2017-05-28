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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

/**
 * Permet de filtrer les fichiers afficher dans une boite de dialogue
 * de sélection de fichier selon un Regex pattern definit
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class RegexPatternFileFilter extends FileFilter {
    FileFilterPattern filter;

    /**
     * Construit un filtre de fichier pour une expression régulière donné et avec la 
     * description fournit en paramètre.
     * 
     * @param pattern l'expression de filtrage de fichier
     * @param description la description associé au filtrage
     */
    public RegexPatternFileFilter(String pattern, String description) {
    	if(pattern != null)
    		this.filter = new FileFilterPattern(Pattern.compile(pattern), description);
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(File)
     */
    @Override
    public boolean accept(File f) {
		if(f != null) {
		    if(f.isDirectory())
		    	return true;
		    if(filter == null)
		        return true;
		    if(filter.matchFileName(f.getName()))
		    	return true;
		}
		return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription() {
    	if(filter != null) {
    		if(filter.getDescription() != null && !filter.getDescription().isEmpty())
    			return filter.getDescription();
    		return filter.getPattern().pattern();
    	}
    	return null;
    }

    /**
     * Définit une desciption pour le filtre courrant
     * 
     * @param description la description du filtre
     */
    public void setDescription(String description) {
    	if(filter != null)
    		filter.setDescription(description);
    }
    
    private class FileFilterPattern {
    	private Pattern pattern;
    	private String description;
    	
		public FileFilterPattern(Pattern pattern, String description) {
			this.pattern = pattern;
			this.description = description;
		}
		
		public boolean matchFileName(String filename) {
			Matcher m = pattern.matcher(filename);
			return m.matches();
		}

		/**
		 * @return the pattern
		 */
		public Pattern getPattern() {
			return pattern;
		}

		/**
		 * @param pattern the pattern to set
		 */
		@SuppressWarnings("unused")
		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
    }
}
