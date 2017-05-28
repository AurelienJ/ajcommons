/*
 * Créé le 25 juil. 2009 à 12:26:11 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA 
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
 * pri connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 * 
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 *
 *  *** GNU GPL Terms *** 
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.ajdeveloppement.apps.localisation;

import javax.swing.JTabbedPane;

import org.ajdeveloppement.commons.AjResourcesReader;

/**
 * <p>Handler de localisation dédié aux objets {@link JTabbedPane} et dérivé. Permet la localisation des onglets.<br>
 * Dans le fichier de localisation, les différents onglets de l'objets doivent être référencé avec la même clé suivi
 * d'un point (.) puis du numéro d'index de l'onglet.</p>
 * <p>Ex:</p>
 * <pre>
 * monecran.ongletgenral.0=Mon Premier onglet
 * monecran.ongletgenral.1=Mon Second onglet
 * monecran.ongletgenral.2=Mon Troisième onglet
 * </pre>
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class JTabbedPaneLocalizationHandler implements LocalizationHandler {

    /**
     * @see LocalizationHandler#processLocalisation(java.lang.Object, java.lang.String, org.ajdeveloppement.commons.AjResourcesReader, java.lang.String) 
     */
	@Override
	public void processLocalisation(Object localisableObject,
			String localisationMethod, AjResourcesReader localisableReader,
			String localisationKey) {
		
		assert localisableObject instanceof JTabbedPane : "Le Handler ne supporte la localisation que des class JTabbedPane et dérivé"; //$NON-NLS-1$
		
		JTabbedPane tabbedPaneFieldObj = (JTabbedPane)localisableObject;
        switch (localisationMethod) {
            case "setText": //$NON-NLS-1$
                for(int i = 0; i < tabbedPaneFieldObj.getTabCount(); i++) {
                    tabbedPaneFieldObj.setTitleAt(i, localisableReader.getResourceString(localisationKey + "." + i)); //$NON-NLS-1$
                }
                break;
            case "setToolTipText": //$NON-NLS-1$
                for(int i = 0; i < tabbedPaneFieldObj.getTabCount(); i++) {
                    tabbedPaneFieldObj.setToolTipTextAt(i, localisableReader.getResourceString(localisationKey + "." + i)); //$NON-NLS-1$
                }
                break;
        }
	}

}
