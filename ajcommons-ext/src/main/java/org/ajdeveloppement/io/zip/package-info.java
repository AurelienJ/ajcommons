/**
 * <p>
 * Permet la manipulation étendu des archives zip, notament en supportant les archives
 * crypté avec l'algorithme de cryptage traditionnel de PKZIP.
 * </p>
 * <p>
 * Le support des archives protégés est implémenté exclusivement en lecture car l'algorithme est réputé non fiable (mois de 30 sec pour casser
 * le mot de passe avec l'utilitaire pkcrack - (http://www.unix-ag.uni-kl.de/~conrad/krypto/pkcrack.html)). Il n'est donc pas conseillé en création.
 * </p>
 */
package org.ajdeveloppement.io.zip;