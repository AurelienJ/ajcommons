/*
 * Créé le 27 mai 2009 pour ajcommons
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
package org.ajdeveloppement.commons.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.bind.JAXBException;

import org.ajdeveloppement.apps.localisation.Localizable;
import org.ajdeveloppement.apps.localisation.Localizator;
import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.net.SiteAuthenticator;
import org.ajdeveloppement.commons.security.SecureSiteAuthenticationStore;

/**
 * @author Aurélien JEOFFRAY
 *
 */
public class SwingURLAuthenticator extends SiteAuthenticator {

	private LoginDialog loginDialog = new LoginDialog();
	private SecureSiteAuthenticationStore secureSiteAuthenticationStore;
	private Map<String, Boolean> firstCallUrl = Collections.synchronizedMap(new HashMap<String, Boolean>());
	
	/**
	 * 
	 */
	public SwingURLAuthenticator() {
		super();
	}
	
	/**
	 * @return the urlAuthenticationStore
	 */
	public SecureSiteAuthenticationStore getSecureSiteAuthenticationStore() {
		return secureSiteAuthenticationStore;
	}

	/**
	 * @param urlAuthenticationStore the urlAuthenticationStore to set
	 */
	public void setSecureSiteAuthenticationStore(
			SecureSiteAuthenticationStore urlAuthenticationStore) {
		this.secureSiteAuthenticationStore = urlAuthenticationStore;
	}
	
	public void storeAuthentication(String site) {
		
		if(secureSiteAuthenticationStore != null) {
			try {
				secureSiteAuthenticationStore.putSiteAuthentication(site, authentications.get(site));
			} catch (InvalidKeyException e) {
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
				e.printStackTrace();
			} catch (BadPaddingException e) {
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
				e.printStackTrace();
			} catch (JAXBException e) {
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
				e.printStackTrace();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
				e.printStackTrace();
			}
		}
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		
		PasswordAuthentication pa = null;
		String site = SiteAuthenticator.getSiteForURL(getRequestingProtocol(), getRequestingHost(), getRequestingPort(),
				getRequestorType() == RequestorType.PROXY ? "Proxy" : getRequestingPrompt()); //$NON-NLS-1$
		
		Boolean firsCall = firstCallUrl.get(site);
		
		if(firsCall == null || firsCall.booleanValue()) {
			if(secureSiteAuthenticationStore != null) {
				try {
					pa = secureSiteAuthenticationStore.getPasswordAuthentication(site);
				} catch (InvalidKeyException e) {
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				} catch (BadPaddingException e) {
					JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				}
			}
			if(pa == null)
				pa = super.getPasswordAuthentication();
			firstCallUrl.put(site, false);
		}
		if(pa == null) {
			pa = loginDialog.showLoginDialog(getRequestorType(), site);
		}
		
		return pa;
	}
	
	@Localizable(textMethod="setTitle",value="swinghttpauthenticator.logindialog.title")
	private class LoginDialog extends JDialog implements ActionListener {
		private AjResourcesReader localisation  = new AjResourcesReader("org.ajdeveloppement.commons.ui.labels"); //$NON-NLS-1$;
		
		private JLabel jlAuthTitle = new JLabel();
		@Localizable("swinghttpauthenticator.logindialog.addresslabel")
		private JLabel jlAddressLabel = new JLabel();
		private JLabel jlAddress = new JLabel();
		
		@Localizable("swinghttpauthenticator.logindialog.login")
		private JLabel jlLogin = new JLabel();
		private JTextField jtfLogin = new JTextField();
		@Localizable("swinghttpauthenticator.logindialog.password")
		private JLabel jlPassword = new JLabel();
		private JPasswordField jtfPassword = new JPasswordField();
		@Localizable("swinghttpauthenticator.logindialog.conserv")
		private JCheckBox jcbConserv = new JCheckBox();
		
		@Localizable("action.valider")
		private JButton jbValider = new JButton();
		@Localizable("action.annuler")
		private JButton jbAnnuler = new JButton();
		
		private RequestorType requestorType;
		private String site;
		private PasswordAuthentication passwordAuthentication;
		/**
		 * 
		 */
		public LoginDialog() {
			super((JFrame)null, true);
			
			init();
			affectLibelle();
		}
		
		private void init() {
			JPanel jpTitle = new JPanel();
			JPanel jpGeneral = new JPanel();
			JPanel jpAction = new JPanel();
			
			GridbagComposer gbc = new GridbagComposer();
			GridBagConstraints c = new GridBagConstraints();
			
			jlAuthTitle.setFont(jlAuthTitle.getFont().deriveFont(22.0f));
			
			jbValider.addActionListener(this);
			jbAnnuler.addActionListener(this);
			
			gbc.setParentPanel(jpTitle);
			c.gridy = 0;
			c.anchor = GridBagConstraints.WEST;
			c.gridwidth = 4;
			gbc.addComponentIntoGrid(jlAuthTitle, c);
			c.gridy++;
			c.gridwidth = 1;
			gbc.addComponentIntoGrid(Box.createHorizontalStrut(30), c);
			gbc.addComponentIntoGrid(jlAddressLabel, c);
			gbc.addComponentIntoGrid(jlAddress, c);
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets.bottom = 30;
			gbc.addComponentIntoGrid(Box.createGlue(), c);
			
			gbc.setParentPanel(jpGeneral);
			c.gridy = 0;
			c.insets.bottom = 0;
			gbc.addComponentIntoGrid(jlLogin, c);
			gbc.addComponentIntoGrid(jtfLogin, c);
			c.gridy++;
			gbc.addComponentIntoGrid(jlPassword, c);
			gbc.addComponentIntoGrid(jtfPassword, c);
			c.gridy++;
			c.gridwidth = 2;
			gbc.addComponentIntoGrid(jcbConserv, c);
			
			jpAction.setLayout(new FlowLayout(FlowLayout.RIGHT));
			jpAction.add(jbValider);
			jpAction.add(jbAnnuler);
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(jpTitle, BorderLayout.NORTH);
			getContentPane().add(jpGeneral, BorderLayout.CENTER);
			getContentPane().add(jpAction, BorderLayout.SOUTH);
			
			getRootPane().setDefaultButton(jbValider);
		}
		
		private void affectLibelle() {
			Localizator.localize(this, localisation);
		}
		
		private void completePanel() {
			jcbConserv.setEnabled(secureSiteAuthenticationStore != null);
				
			if(requestorType != null && requestorType == RequestorType.PROXY)
				jlAuthTitle.setText(localisation.getResourceString("swinghttpauthenticator.logindialog.proxyauth"));  //$NON-NLS-1$
			else
				jlAuthTitle.setText(localisation.getResourceString("swinghttpauthenticator.logindialog.serverauth"));  //$NON-NLS-1$
			
			if(site != null)
				jlAddress.setText(site);
			else
				jlAddress.setText(localisation.getResourceString("swinghttpauthenticator.logindialog.none")); //$NON-NLS-1$
			
			jtfLogin.setText("");  //$NON-NLS-1$
			jtfPassword.setText("");  //$NON-NLS-1$
			if(secureSiteAuthenticationStore != null) {
				try {
					PasswordAuthentication pa = secureSiteAuthenticationStore.getPasswordAuthentication(site);
					if(pa != null) {
						jtfLogin.setText(pa.getUserName());
						jtfPassword.setText(new String(pa.getPassword()));
					}
				} catch (InvalidKeyException e) {
					JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				} catch (BadPaddingException e) {
					JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$
					e.printStackTrace();
				}
			}
		}
		
		public PasswordAuthentication showLoginDialog(RequestorType requestorType, String site) {
			this.requestorType = requestorType;
			this.site = site;
			passwordAuthentication = null;
			
			completePanel();
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
			
			return passwordAuthentication;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == jbValider) {
				passwordAuthentication = new PasswordAuthentication(jtfLogin.getText(), jtfPassword.getPassword());
				
				addAuthentication(site, passwordAuthentication);
				if(jcbConserv.isSelected()) {
					storeAuthentication(site);
				}
				
				setVisible(false);
			} else {
				setVisible(false);
			}
		}
	}
}