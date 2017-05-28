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
package org.ajdeveloppement.commons.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.StringFormatters;
/**
 * Fenêtre de progression de téléchargement d'une collection de données
 * 
 * @author Aurélien JEOFFRAY
 */
public class DataDownloadProgressDialog extends JDialog implements ActionListener {
	
	private static AjResourcesReader labels = new AjResourcesReader("org.ajdeveloppement.commons.ui.labels"); //$NON-NLS-1$
	
	private JLabel progressFileName = new JLabel();
	private JLabel progressSizeFile = new JLabel();
	private JLabel progressNumberFile = new JLabel();
	private JProgressBar downloadProgressBar = new JProgressBar();
	
	private JButton jbAnnuler = new JButton(labels.getResourceString("action.annuler")); //$NON-NLS-1$
	
	private long downloadTotalSize = 0;
	private long downloadCurrentSize = 0;
	private int totalFile = 0;
	
	/**
	 * Construit la fenetre de progression
	 * 
	 * @param downloadTotalSize la taille totale des données à télécharger
	 * @param totalFile le nombre totale des données à télécharger
	 */
	public DataDownloadProgressDialog(long downloadTotalSize, int totalFile) {
		this.downloadTotalSize = downloadTotalSize;
		this.totalFile = totalFile;
		
		init();
	}
	
	private void init() {
		JPanel panel = new JPanel();
		JPanel jpAction = new JPanel();
		
		GridBagConstraints c    = new GridBagConstraints();
		
		GridbagComposer gridbagComposer = new GridbagComposer();
		
		downloadProgressBar.setMaximum(100);
		downloadProgressBar.setValue(0);
		downloadProgressBar.setStringPainted(true);
		
		jbAnnuler.addActionListener(this);
		
		gridbagComposer.setParentPanel(panel);
		
		c.gridy = 0; c.anchor = GridBagConstraints.WEST;
		gridbagComposer.addComponentIntoGrid(progressFileName, c);
		gridbagComposer.addComponentIntoGrid(progressSizeFile, c);
		gridbagComposer.addComponentIntoGrid(progressNumberFile, c);
		
		c.gridy++; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
		gridbagComposer.addComponentIntoGrid(downloadProgressBar, c);
		
		jpAction.setLayout(new FlowLayout(FlowLayout.RIGHT));
		jpAction.add(jbAnnuler);
		
		setModal(false);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(jpAction, BorderLayout.SOUTH);
	}
	
	/**
	 * Affiche la fenêtre de progression 
	 */
	public void showProgressDialog() {
		setSize(400, 100);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Définit le nom de la donnée en cours de téléchargement
	 * 
	 * @param filename le nom de la donnée en cours de téléchargement
	 */
	public void setCurrentFile(final String filename) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressFileName.setText(filename);
			}
		});
	}
	
	/**
	 * Définit l'indice de la donnée en cours de téléchargement
	 * @param indice l'indice de la donnée en cours de téléchargement
	 */
	public void setIndiceFile(final int indice) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressNumberFile.setText(" - " + indice + "/" + totalFile); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}
	
	/**
	 * Définit la chaine représentant la taille des données formaté (Kilo,Mega,Giga) et localisé<br>
	 * Utlisé {@link StringFormatters#formatFileSize(double)} pour retourner la chaine de taille correctement formaté
	 * 
	 * @param fileSize la chaine de taille correctement formaté
	 */
	public void setDisplayFileSize(final String fileSize) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressSizeFile.setText(" - " + fileSize); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Ajoute le nombre d'octet récupéré à la dernière lecture du flux pour suivre la progression
	 * du téléchargement.
	 * 
	 * @param dSize le nombre d'octet téléchargé afin d'en rprésenté la progression
	 */
	public void addDownloadedSize(long dSize) {
		downloadCurrentSize += dSize;
		
		final double percent = ((double)downloadCurrentSize / (double)downloadTotalSize) * 100.0;
		
		assert percent <= 100.0 : "Un poucentage de progression ne doit pas être supérieur à 100"; //$NON-NLS-1$

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				downloadProgressBar.setValue(new BigDecimal(Math.round(percent)).intValue());
				repaint();
				
			}
		});
		
		Thread.yield();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
	}
}
