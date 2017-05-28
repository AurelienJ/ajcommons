/*
 * Créer le 2 déc. 07 à 14:12:53 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.updater.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.html.HTMLEditorKit;

import org.ajdeveloppement.apps.AppUtilities;
import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.StringFormatters;
import org.ajdeveloppement.commons.security.PrincipalHelper;
import org.ajdeveloppement.commons.ui.DefaultDialogReturn;
import org.ajdeveloppement.updater.AjUpdater;
import org.ajdeveloppement.updater.FileMetaData;
import org.ajdeveloppement.updater.Repository;
import org.ajdeveloppement.updater.Repository.Status;
import org.ajdeveloppement.updater.Version;

/**
 * Fenêtre de proposition et d'information des mises à jours disponible.
 * La fenêtre permet d'informer l'utilisateur sur le contenu des mises à jour.
 * Elle affiche un changelog pour chaque dépôt ainsi que la liste des fichiers
 * à télécharger avec leurs tailles 
 *
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class AjUpdaterFrame extends JDialog implements ActionListener, ListSelectionListener {
	private JLabel jlSiteUpdate = new JLabel();
	private final JTable jtRepository = new JTable() {
		//  Returning the Class of each column will allow different
			//  renderers to be used based on Class
			@Override
	        public Class<?> getColumnClass(int column)	{
				return getValueAt(0, column).getClass();
			}
		};
	//private AJList siteUpdate = new AJList();
	private JLabel jlInfoSecurity = new JLabel();
	private JLabel jlNoteVersion = new JLabel();
	private JTabbedPane infoTabbedPane = new JTabbedPane();
	private JTextArea jtaChangeLog = new JTextArea();
	private JTextPane jtpFileList = new JTextPane();
	
	private JButton jbValider = new JButton();
	private JButton jbAnnuler = new JButton();
	
	private AjUpdater ajUpdater;
	private Hashtable<Repository, String> changeLogs = new Hashtable<Repository, String>();
	private Hashtable<Repository, String> fileslists = new Hashtable<Repository, String>();
	
	private DefaultDialogReturn returnCode = DefaultDialogReturn.CANCEL;
	
	private static AjResourcesReader labels = new AjResourcesReader("org.ajdeveloppement.updater.labels"); //$NON-NLS-1$
	
	/**
	 * Construit une nouvelle fenetre de proposition de mise à jour se basant sur le service 
	 * de mise à jour fournit en parametre
	 * 
	 * @param ajUpdater le service de mise à jour en backend de la fenetre
	 */
	public AjUpdaterFrame(AjUpdater ajUpdater) {
		super();
		this.ajUpdater = ajUpdater;
		
		init();
		affectLibelle();
	}

	private void init() {
		
		//siteUpdate.addListSelectionListener(this);
		jbValider.addActionListener(this);
		jbAnnuler.addActionListener(this);
		
		jtRepository.setModel(new RepositoryTableModel());
		jtRepository.getSelectionModel().addListSelectionListener(this);
		jtRepository.getColumnModel().getColumn(0).setMaxWidth(20);
		jtRepository.setDefaultRenderer(Repository.class,
                new ReposCellRenderer());
		jtRepository.setRowHeight(18);
		
		jlInfoSecurity.setPreferredSize(new Dimension(700, 100));
		jlInfoSecurity.setVerticalAlignment(JLabel.TOP);
		
		JScrollPane jspTRepository = new JScrollPane(jtRepository);
		jspTRepository.setPreferredSize(new Dimension(700, 400));
		
		jtaChangeLog.setEditable(false);
		jtpFileList.setEditorKit(new HTMLEditorKit());
		jtpFileList.setEditable(false);
		
		JPanel jpInfoSecurity = new JPanel();
		jpInfoSecurity.setLayout(new BorderLayout());
		jpInfoSecurity.add(jlInfoSecurity, BorderLayout.CENTER);
		
		JPanel jpList = new JPanel();
		jpList.setLayout(new BorderLayout());
		jpList.add(jlSiteUpdate, BorderLayout.NORTH);
		jpList.add(jspTRepository, BorderLayout.CENTER);
		jpList.add(jpInfoSecurity, BorderLayout.SOUTH);
		
		JPanel jpChangeLog = new JPanel();
		jpChangeLog.setLayout(new BorderLayout());
		jpChangeLog.add(jlNoteVersion, BorderLayout.NORTH);
		jpChangeLog.add(jtaChangeLog, BorderLayout.CENTER);
		
		JPanel jpFileList = new JPanel();
		jpFileList.setLayout(new BorderLayout());
		jpFileList.add(jtpFileList, BorderLayout.CENTER);
		
		infoTabbedPane.addTab("update.frame.changelog", new JScrollPane(jpChangeLog)); //$NON-NLS-1$
		infoTabbedPane.addTab("update.frame.file", new JScrollPane(jpFileList)); //$NON-NLS-1$
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, jpList, infoTabbedPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.25);
		
		JPanel jpAction = new JPanel();
		jpAction.setLayout(new FlowLayout(FlowLayout.RIGHT));
		jpAction.add(jbValider);
		jpAction.add(jbAnnuler);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);
		getContentPane().add(jpAction, BorderLayout.SOUTH);
		
		setSize(new Dimension(700, 700));
		setLocationRelativeTo(null);
		setModal(true);
	}
	
	private void affectLibelle() {
		setTitle(labels.getResourceString("update.frame.title")); //$NON-NLS-1$
		jlSiteUpdate.setText(labels.getResourceString("update.frame.siteupdate")); //$NON-NLS-1$
		infoTabbedPane.setTitleAt(0, labels.getResourceString("update.frame.changelog")); //$NON-NLS-1$
		infoTabbedPane.setTitleAt(1, labels.getResourceString("update.frame.file")); //$NON-NLS-1$
		jbValider.setText(labels.getResourceString("update.frame.action.valider")); //$NON-NLS-1$
		jbAnnuler.setText(labels.getResourceString("update.frame.action.annuler")); //$NON-NLS-1$
	}
	
	private void completePanel() {
		if(ajUpdater != null) {
			RepositoryTableModel rtm = new RepositoryTableModel();
			
			for(Repository repos : ajUpdater.getRepositories()) {
				if(ajUpdater.getFilesPath(repos) == null)
					continue;
				rtm.addRepository(repos);
				
				String info = ""; //$NON-NLS-1$
				if(ajUpdater.getInfoVersions(repos) != null) {
					for(Version version : ajUpdater.getInfoVersions(repos)) {
						if(AppUtilities.compareVersion(repos.getRefAppVersion(), version.getVersion()) < 0) {
							info += "v" + version.getVersion() + " - " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(version.getDateVersion()) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							info += version.getChangeInfos() + "\n"; //$NON-NLS-1$
						}
					}
				}
				if(info.isEmpty())
					info = labels.getResourceString("update.frame.noinfo"); //$NON-NLS-1$
				String files = "<html>"; //$NON-NLS-1$
				files += "<table cellspacing=\"0\" cellpadding=\"0\">"; //$NON-NLS-1$
				if( ajUpdater.getFilesPath(repos) != null) {
					for(FileMetaData filepath : ajUpdater.getFilesPath(repos)) {
						String filename = filepath.getPath();
						files += "<tr style=\"font-family: Arial; margin: 0px;\"><td>" + filename + "&nbsp;</td><td>" + StringFormatters.formatFileSize(filepath.getFileSize()) + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
				files += "</table></html>"; //$NON-NLS-1$
				changeLogs.put(repos, info);
				fileslists.put(repos, files);
			}
			jtRepository.setModel(rtm);
			jtRepository.getSelectionModel().setSelectionInterval(0, 0);
			jtRepository.getColumnModel().getColumn(0).setMaxWidth(30);
			jtRepository.getColumnModel().getColumn(1).setPreferredWidth(150);
			jtRepository.getColumnModel().getColumn(1).setMaxWidth(150);
		}
	}
	
	/**
	 * Affiche la fenetre de proposition de mise à jour
	 * 
	 * @return le code de retour correspondant à l'action de validation effectué dans la fenetre
	 */
	public DefaultDialogReturn showAjUpdaterFrame() {
		completePanel();
		
		setVisible(true);
		
		return returnCode;
	}
	
	/**
	 * Return the list of repositories validate for update
	 * 
	 * @return the list of repositories validate for update
	 */
	public List<Repository> getValidateRepositories() {
		List<Repository> repos = new ArrayList<Repository>();
		
		for(int i = 0; i < jtRepository.getModel().getRowCount(); i++) {
			if((Boolean)jtRepository.getModel().getValueAt(i, 0))
				repos.add(((RepositoryTableModel)jtRepository.getModel()).getRepositoryAt(i));
		}
		
		return repos;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbValider) {
			returnCode = DefaultDialogReturn.OK;
			setVisible(false);
		} else if(e.getSource() == jbAnnuler) {
			returnCode = DefaultDialogReturn.CANCEL;
			setVisible(false);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == jtRepository.getSelectionModel()) {
			RepositoryTableModel rtm = (RepositoryTableModel)jtRepository.getModel();
			Repository repository = rtm.getRepositoryAt(jtRepository.getSelectionModel().getMinSelectionIndex());
			String info = changeLogs.get(repository);
			if(info.isEmpty())
				info = labels.getResourceString("update.frame.noinfo"); //$NON-NLS-1$
			String files = fileslists.get(repository);
			jtaChangeLog.setText(info);
			jtpFileList.setText(files);
			
			X509Certificate certificate;
			PrincipalHelper pHelper;
			String signataire = ""; //$NON-NLS-1$
			switch(repository.getReposStatus()) {
				case REVISION_SIGN_VERIFY_WITH_TRUSTED_CERT:
					jlInfoSecurity.setIcon(new ImageIcon(getClass().getResource("trusted_16x16.png"))); //$NON-NLS-1$
					jlInfoSecurity.setOpaque(true);
					jlInfoSecurity.setBackground(new Color(0, 175, 0));
					
					certificate = (X509Certificate)repository.getCertificate(); 
					pHelper = new PrincipalHelper(certificate.getSubjectDN());
					PrincipalHelper pHelperCA = new PrincipalHelper(certificate.getIssuerDN());
					
					signataire = labels.getResourceString("update.frame.repos.signinfo", //$NON-NLS-1$
							"", pHelper.getPrincipalPropertyValue("CN"), //$NON-NLS-1$ //$NON-NLS-2$
							pHelper.getPrincipalPropertyValue("O"), //$NON-NLS-1$
							pHelper.getPrincipalPropertyValue("ST") + ", " + pHelper.getPrincipalPropertyValue("C"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							pHelper.getPrincipalPropertyValue("EMAILADDRESS"), //$NON-NLS-1$
							pHelperCA.getPrincipalPropertyValue("CN"), //$NON-NLS-1$
							pHelperCA.getPrincipalPropertyValue("O") + ", " + pHelperCA.getPrincipalPropertyValue("ST") + ", " +pHelperCA.getPrincipalPropertyValue("C")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					break;
				case REVISION_SIGN_VERIFY_WITH_SELFSIGNED_CERT:
					jlInfoSecurity.setIcon(new ImageIcon(getClass().getResource("warning_16x16.png"))); //$NON-NLS-1$
					jlInfoSecurity.setOpaque(true);
					jlInfoSecurity.setBackground(Color.ORANGE);
					
					certificate = (X509Certificate)repository.getCertificate(); 
					pHelper = new PrincipalHelper(certificate.getSubjectDN());
					
					signataire = labels.getResourceString("update.frame.repos.selfsigninfo", //$NON-NLS-1$
							pHelper.getPrincipalPropertyValue("CN"), //$NON-NLS-1$ 
							pHelper.getPrincipalPropertyValue("O"), //$NON-NLS-1$
							pHelper.getPrincipalPropertyValue("ST") + ", " + pHelper.getPrincipalPropertyValue("C"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							pHelper.getPrincipalPropertyValue("EMAILADDRESS")); //$NON-NLS-1$
					break;
				case REVISION_SIGN_VERIFY_WITH_EXPIRED_CERT:
					jlInfoSecurity.setIcon(new ImageIcon(getClass().getResource("warning_16x16.png"))); //$NON-NLS-1$
					jlInfoSecurity.setOpaque(true);
					jlInfoSecurity.setBackground(Color.ORANGE);
					
					certificate = (X509Certificate)repository.getCertificate(); 
					pHelper = new PrincipalHelper(certificate.getSubjectDN());
					
					signataire = labels.getResourceString("update.frame.repos.expiredcert", //$NON-NLS-1$
							pHelper.getPrincipalPropertyValue("CN"), //$NON-NLS-1$ 
							pHelper.getPrincipalPropertyValue("O"), //$NON-NLS-1$
							pHelper.getPrincipalPropertyValue("ST") + ", " + pHelper.getPrincipalPropertyValue("C"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							pHelper.getPrincipalPropertyValue("EMAILADDRESS"), //$NON-NLS-1$
							DateFormat.getDateInstance(DateFormat.MEDIUM).format(certificate.getNotAfter()));
					break;
				case CERTIFICATE_FAILED:
				case CERTIFICATE_VALIDATION_FAILED:
				case REVISION_SIGN_VERIFY_FAILED:
					jlInfoSecurity.setIcon(new ImageIcon(getClass().getResource("failed_16x16.png"))); //$NON-NLS-1$
					jlInfoSecurity.setOpaque(true);
					jlInfoSecurity.setBackground(Color.RED);
					
					signataire = labels.getResourceString("update.frame.repos.signfail"); //$NON-NLS-1$
					break;
				case CONNECTION_FAILED:
				case DISCONNECTED:
				case UNSERIALIZE_FAILED:
					jlInfoSecurity.setIcon(new ImageIcon(getClass().getResource("failed_16x16.png"))); //$NON-NLS-1$
					jlInfoSecurity.setOpaque(true);
					jlInfoSecurity.setBackground(Color.RED);
					
					signataire = labels.getResourceString("update.frame.repos.unknownerror"); //$NON-NLS-1$
					break;
				default:
					jlInfoSecurity.setIcon(null);
					jlInfoSecurity.setOpaque(false);
					signataire = labels.getResourceString("update.frame.repos.nosign"); //$NON-NLS-1$
			}
			jlInfoSecurity.setText(signataire);
		}
	}
	
	class RepositoryTableModel implements TableModel {
		
		private EventListenerList listenerList = new EventListenerList();
		private String[] colName = {"", labels.getResourceString("update.frame.repos.name"), labels.getResourceString("update.frame.repos.url")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		private List<Repository> repositories = new ArrayList<Repository>();
		private List<Boolean> selectedRepos = new ArrayList<Boolean>();
		private List<Boolean> selectableRepos = new ArrayList<Boolean>();
		
		public RepositoryTableModel() {
		}
		
		public void addRepository(Repository repository) {
			repositories.add(repository);
			if(repository.getReposStatus() == Status.REVISION_SIGN_VERIFY_WITH_TRUSTED_CERT) {
				selectedRepos.add(true);
				selectableRepos.add(true);
			} else {
				selectedRepos.add(false);
				if(repository.getReposStatus() == Status.REVISION_NOSIGN_FOUND
						|| repository.getReposStatus() == Status.REVISION_SIGN_VERIFY_WITH_EXPIRED_CERT
						|| repository.getReposStatus() == Status.REVISION_SIGN_VERIFY_WITH_SELFSIGNED_CERT)
					selectableRepos.add(true);
				else
					selectableRepos.add(false);
			}
		}
		
		public Repository getRepositoryAt(int index) {
			return repositories.get(index);
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			listenerList.add(TableModelListener.class, l);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 0)
				return Boolean.class;
			else if(columnIndex == 1)
				return Repository.class;
			return String.class;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return colName[columnIndex];
		}

		@Override
		public int getRowCount() {
			return repositories.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Repository repository = repositories.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return selectedRepos.get(rowIndex);
				case 1:
					return repository;
				case 2:
					return repository.getReposURLs()[repository.getCurrentMirror()];
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 0 && selectableRepos.get(rowIndex) && repositories.get(rowIndex).isCanDisable())
				return true;
			return false;
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listenerList.remove(TableModelListener.class, l);
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if(columnIndex == 0)
				selectedRepos.set(rowIndex, (Boolean)value);
		}
		
	}
}
