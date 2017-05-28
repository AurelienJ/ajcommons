/*
 * Créer le 26 nov. 07 à 21:55:31 pour AjCommons (Bibliothèque de composant communs)
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
 * pris connaissance de la licence CeCILL-C, et que vous en avez accepté les
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.ajdeveloppement.commons.io.XMLSerializer;
import org.ajdeveloppement.commons.ui.GridbagComposer;
import org.ajdeveloppement.updater.Version;

/**
 * Fenetre permettant la rédaction d'un fichier XML structuré de changelog exploitable
 * par la tache Ant RevisionsCreator afin de produire les fichiers de révisions spécifique
 * à chaque moyen de distribution (pack windows, pack linux, mise à jour automatique, ...)
 * 
 * La fenetre peut s'executer en tant qu'application autonome. Le chemin du fichier XML à générer
 * doit être fournit en parametre 
 *
 * @author Aurélien JEOFFRAY
 * @version 0.1
 *
 */
@SuppressWarnings("nls")
public class ChangeLogEditor extends JFrame implements ActionListener {
	
	//private static AjResourcesReader ajrLibelle = new AjResourcesReader("ajinteractive.standard.utilities.updater.updater_libelle"); //$NON-NLS-1$
	
	private JButton jbNouvelleRevision = new JButton("Nouvelle Révision");
	private JButton jbEnregistrer = new JButton("Modifier Dernière Révision");
	private JButton jbFermer = new JButton("Fermer");
	
	private JTextField jtfNomApplication = new JTextField(15);
	private JTextField jtfNumVersion = new JTextField(8);
	private JComboBox<String> jcbEtat = new JComboBox<>();
	private JTextArea jtaInformation = new JTextArea(8, 50);
	private JTextField jtfAuteur = new JTextField(15);
	private JTextField jtfDate = new JTextField(8);
	
	private DefaultTableModel dtm = new DefaultTableModel();
	private JTable jtRevisions = new JTable();
	
	private ArrayList<Version> versions;
	private String pathRevisionsFile;

	/**
	 * Construit la fenetre d'édition du changelog
	 * 
	 * @param pathRevisionsFile le chemin du fichier XML à créer/modifier
	 */
	@SuppressWarnings("all")
	public ChangeLogEditor(String pathRevisionsFile) {
		this.pathRevisionsFile = pathRevisionsFile;
		
		try {
			versions = XMLSerializer.loadXMLStructure(new File(pathRevisionsFile), false);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(versions == null)
			versions = new ArrayList<Version>();
		init();
		completePanel();
	}
	
	private void init() {
		//GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		GridbagComposer gridbagComposer = new GridbagComposer();
		
		JPanel jpAction = new JPanel();
		jpAction.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JPanel jpRevision = new JPanel();
		jpRevision.setBorder(new TitledBorder("Détail Révision"));
		
		JPanel jpListRevisions = new JPanel();
		jpListRevisions.setBorder(new TitledBorder("Revisions"));
		jpListRevisions.setLayout(new BorderLayout());
		
		JLabel jlNomApplication = new JLabel("Nom de l'application:");
		JLabel jlNumVersion = new JLabel("Version:");
		JLabel jlInformation = new JLabel("ChangeLog:");
		JLabel jlAuteur = new JLabel("Auteur:");
		JLabel jlDate = new JLabel("Date:");
		
		jcbEtat.addItem("stable");
		jcbEtat.addItem("unstable");
		
		dtm.addColumn("Version");
		dtm.addColumn("Description");
		dtm.addColumn("Auteur");
		dtm.addColumn("Date");
		
		jtRevisions.setModel(dtm);
		jtRevisions.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		jtRevisions.setPreferredScrollableViewportSize(new Dimension(550, 100));
		
		jbNouvelleRevision.addActionListener(this);
		jbFermer.addActionListener(this);
		
		jpAction.add(jbNouvelleRevision);
		jpAction.add(jbEnregistrer);
		jpAction.add(jbFermer);
		
		gridbagComposer.setParentPanel(jpRevision);
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbagComposer.addComponentIntoGrid(jlNomApplication, c);
		gridbagComposer.addComponentIntoGrid(jtfNomApplication, c);
		gridbagComposer.addComponentIntoGrid(jlNumVersion, c);
		gridbagComposer.addComponentIntoGrid(jtfNumVersion, c);
		gridbagComposer.addComponentIntoGrid(jcbEtat, c);
		c.gridy++;
		c.gridwidth = 5;
		gridbagComposer.addComponentIntoGrid(jlInformation, c);
		c.gridy++;
		gridbagComposer.addComponentIntoGrid(new JScrollPane(jtaInformation), c);
		c.gridy++;
		c.gridwidth = 1;
		gridbagComposer.addComponentIntoGrid(jlAuteur, c);
		gridbagComposer.addComponentIntoGrid(jtfAuteur, c);
		gridbagComposer.addComponentIntoGrid(jlDate, c);
		gridbagComposer.addComponentIntoGrid(jtfDate, c);
		
		jpListRevisions.add(new JScrollPane(jtRevisions), BorderLayout.CENTER);
		jpListRevisions.add(jpAction, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(jpRevision, BorderLayout.CENTER);
		getContentPane().add(jpListRevisions, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void completePanel() {
		dtm.setRowCount(0);
		for(Version version : versions) {
			dtm.addRow(new Object[] { version.getVersion(), version.getChangeInfos(), version.getAuthor(), version.getDateVersion() });
		}
		jtRevisions.setModel(dtm);
		
		if(versions.size() > 0) {
			Version version = versions.get(versions.size() - 1);
			
			jtfNomApplication.setText(version.getAppname());
			jtfNumVersion.setText(version.getVersion());
			jcbEtat.setSelectedItem(version.getState());
			jtaInformation.setText(version.getChangeInfos());
			jtfAuteur.setText(version.getAuthor());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbNouvelleRevision) {
			Version version = new Version();
			version.setAppname(jtfNomApplication.getText());
			version.setVersion(jtfNumVersion.getText());
			version.setChangeInfos(jtaInformation.getText());
			version.setAuthor(jtfAuteur.getText());
			version.setDateVersion(new Date());
			
			versions.add(version);
			
			dtm.addRow(new Object[] { version.getVersion(), version.getChangeInfos(), version.getAuthor(), version.getDateVersion() });
			
			try {
				XMLSerializer.saveXMLStructure(new File(pathRevisionsFile), versions, false);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Impossible de sauvegarder la revision", "sauvegarde impossible", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else if(e.getSource() == jbFermer) {
			System.exit(0);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 0)
			args = new String[] {"changelog.xml"};
		new ChangeLogEditor(args[0]);
	}

}
