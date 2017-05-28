package org.ajdeveloppement.commons.security;

import java.io.InputStream;
import java.net.URL;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ajdeveloppement.commons.Beta;

import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.X509CertImpl;

/**
 * <b>!! Ne pas utiliser en cours de réécriture</b>
 */

@SuppressWarnings("restriction")
@Beta
public class CRLManager {
	private static Map<String, X509CRL> crlMap = new HashMap<String, X509CRL>();

	private CertificateFactory certificateFactory;

	private Collection<CRL> crlCollection = new ArrayList<CRL>();

	/**
	 * 
	 * @param certificateFactory
	 */
	public CRLManager(CertificateFactory certificateFactory) {
		this.certificateFactory = certificateFactory;
	}

	/**
	 * 
	 * @return collection de certificat de revocation
	 */
	public Collection<CRL> getCRLCollection() {
		return crlCollection;
	}

	/**
	 * Charge la liste de révocation associé au certificat
	 * 
	 * @param certificate
	 * @throws CRLException
	 */
	public void loadCRL(X509Certificate certificate) throws CRLException {
		X509CertImpl certificateImpl = (X509CertImpl) certificate;
		CRLDistributionPointsExtension crlDistributionPointsExtension = certificateImpl.getCRLDistributionPointsExtension();
		
		if (crlDistributionPointsExtension != null) {
			try {
				for (DistributionPoint distributionPoint : crlDistributionPointsExtension.get(CRLDistributionPointsExtension.POINTS)) {
					for (GeneralName generalName : distributionPoint.getFullName().names()) {
						String generalNameString = generalName.toString();

						if (generalNameString.startsWith("URIName: ")) { //$NON-NLS-1$
							String crlURLString = generalNameString.substring(9);
							X509CRL crlImpl = null;

							synchronized (crlMap) {
								crlImpl = crlMap.get(crlURLString);

								// If CRL has been updated since the last check,
								// clear it from the map.
								if (crlImpl != null && crlImpl.getNextUpdate().before(new Date())) {
									crlMap.remove(crlURLString);
									crlImpl = null;
								}

								if (crlImpl == null) {
									InputStream crlInputStream = new URL(crlURLString).openConnection().getInputStream();
									try {
										crlImpl = (X509CRL)certificateFactory.generateCRL(crlInputStream);
									} finally {
										crlInputStream.close();
									}

									crlMap.put(crlURLString, crlImpl);
								}
							}

							crlCollection.add(crlImpl);
						}
					}
				}
			} catch (Exception ex) {
				throw new CRLException(ex);
			}
		}
	}
}
