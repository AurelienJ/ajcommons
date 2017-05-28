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
package org.ajdeveloppement.swingxext.error.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ajdeveloppement.apps.localisation.Localizable;
import org.ajdeveloppement.apps.localisation.Localizator;
import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.ui.GridbagComposer;
import org.ajdeveloppement.swingxext.error.ErrorReportMessage;
import org.ajdeveloppement.swingxext.localisation.JXHeaderLocalisationHandler;
import org.jdesktop.swingx.JXHeader;

/**
 * Boite de dialogue de description utilisateur de l'erreur
 * 
 * @author Aurelien JEOFFRAY
 */
@Localizable(textMethod="setTitle",value="errorreporter.title")
public class ErrorReporterDialog extends JDialog implements ActionListener {
	
	/**
	 * Les actions de validation de la boite de dialogue
	 */
	public enum Action {
		/**
		 * Envoie du formulaire
		 */
		SEND,
		/**
		 * Annulation
		 */
		CANCEL
	}
	
	@Localizable("errorreporter.header")
	private JXHeader header = new JXHeader();
	
	@Localizable("errorreporter.identity")
	private JLabel jlIdentite = new JLabel();
	private JTextField jtfIdentite = new JTextField();
	@Localizable("errorreporter.description")
	private JLabel jlDescription = new JLabel();
	private JTextArea jtaDescription = new JTextArea();
	@Localizable("errorreporter.adddata")
	private JCheckBox jcbAddData = new JCheckBox();
	@Localizable("errorreporter.confidentiality")
	private JLabel jlConfidentialDeclaration = new JLabel(); // Déclaration de confidentialité
	
	@Localizable("errorreporter.send")
	private JButton jbSend = new JButton();
	@Localizable("errorreporter.cancel")
	private JButton jbCancel = new JButton();
	
	private AjResourcesReader localisation = new AjResourcesReader(ErrorReporterDialog.class.getPackage().getName() + ".libelle"); //$NON-NLS-1$
	
	private Action returnAction = Action.CANCEL;
	private ErrorReportMessage message;
	private boolean sendData = false;
	
	/**
	 * Initialise la boite de dialogue de description utilisateur de l'erreur
	 */
	public ErrorReporterDialog(Dialog parent) {
		super(parent, true);
		
		Localizator.addLocalisationHandler(JXHeader.class, new JXHeaderLocalisationHandler());
		
		init();
		affectLibelle();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setSize(new Dimension(600, 400));
	}
	
	private void init() {
		GridbagComposer composer = new GridbagComposer();
		GridBagConstraints c = new GridBagConstraints();
		
		jbSend.addActionListener(this);
		jbCancel.addActionListener(this);
		
		JPanel jpGeneral = new JPanel();
		
		composer.setParentPanel(jpGeneral);
		c.gridy = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		composer.addComponentIntoGrid(jlIdentite, c);
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		composer.addComponentIntoGrid(jtfIdentite, c);
		c.gridy++;
		composer.addComponentIntoGrid(jlDescription, c);
		c.gridy++;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		composer.addComponentIntoGrid(new JScrollPane(jtaDescription), c);
		c.gridy++;
		c.weighty = 0.0;
		composer.addComponentIntoGrid(jcbAddData, c);
		c.gridy++;
		composer.addComponentIntoGrid(jlConfidentialDeclaration, c);

		JPanel jpAction = new JPanel();
		jpAction.setLayout(new FlowLayout(FlowLayout.RIGHT));
		jpAction.add(jbSend);
		jpAction.add(jbCancel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(header, BorderLayout.NORTH);
		getContentPane().add(jpGeneral, BorderLayout.CENTER);
		getContentPane().add(jpAction, BorderLayout.SOUTH);
	}
	
	private void affectLibelle() {
		Localizator.localize(this, localisation);
	}
	
	private void completePanel() {
		
	}
	
	/**
	 * Affiche la boite de dialogue de description utilisateur sur l'erreur
	 * 
	 * @param message le rapport d'erreur auquel ajouter la description utilisateur
	 */
	public Action showErrorReporterDialog(ErrorReportMessage message) {
		this.message = message;
		
		completePanel();
		
		setVisible(true);
		
		return returnAction;
	}
	
	/**
	 * Retourne le message de rapport d'erreur
	 * 
	 * @return le message de raport d'erreur
	 */
	public ErrorReportMessage getMessage() {
		return message;
	}
	
	/**
	 * Indique si des données de travail doivent ou non être transmise avec le rapport
	 * 
	 * @return <code>true</code> si des données de travail doivent être transmise avec le rapport
	 */
	public boolean isSendData() {
		return sendData;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbSend) {
			String userMessage = "Rapporteur: " + jtfIdentite.getText() + "\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
			userMessage += jtaDescription.getText();
			
			message.setUserErrorDescription(userMessage);
			
			returnAction = Action.SEND;
			
			setVisible(false);
		} else if(e.getSource() == jbCancel) {
			returnAction = Action.CANCEL;
			
			setVisible(false);
		}
	}
}
