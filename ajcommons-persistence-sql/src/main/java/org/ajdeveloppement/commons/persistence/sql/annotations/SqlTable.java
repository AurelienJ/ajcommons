/*
 * Créé le 1 mai 2009 à 20:08:16 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.persistence.sql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ajdeveloppement.commons.persistence.sql.DefaultSqlBuilder;
import org.ajdeveloppement.commons.persistence.sql.QResults;
import org.ajdeveloppement.commons.persistence.sql.ResultSetRowToObjectBinder;
import org.ajdeveloppement.commons.persistence.sql.processor.QueryableProcessor;

/**
 * <p>Use for bind class with an SQL table with ajcommons SQL Persistence API
 * This annotation triggers the Java Annotation Processor {@link QueryableProcessor}
 * that generates a new java class untitled "T_[AnnotedClassName]" that define static
 * fields who represents the binded table fields.</p>
 * <p>
 * exemple:<br>
 * the class:<br>
 * <pre><code>
 * &#064;SqlTable(name="ATable", loadBuilder=ABuilder.class)
 * public class A {
 * 	&#064;SqlField(name="idColumn")
 * 	private int id;
 * 	&#064;SqlField(name="valueColumn")
 * 	private String value; 
 * }
 * </code></pre>
 * will generate:
 * <pre><code>
 * public static T_A {
 * 	public static String TABLE_NAME = "ATable";
 * 	public static QField&lt;Integer&gt; IDCOLUMN = new QField&lt;&gt;(TABLE_NAME, "idColumn");
 * 	public static QField&lt;String&gt; VALUECOLUMN = new QField&lt;&gt;(TABLE_NAME, "valueColumn");
 * }
 * </code></pre>
 * This new generated class can be used in a {@link QResults} query. ex: <code>A myA = QResults.from(A.class).where(T_A.IDCOLUMN.equalTo(1)).first();</code>
 * </p>
 * 
 * @author Aurélien JEOFFRAY
 *
 * @version 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SqlTable {
	/**
	 * The name of the database table associate with annotate class
	 */
	String name() default "";
	
	/**
	 * A domain name for connection url. If not defined use the default url connection of context
	 */
	String domain() default "default";
	
	/**
	 * The builder use to bind database resultset row to an object instance
	 * 
	 * @since 2.0
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends ResultSetRowToObjectBinder> loadBuilder() default DefaultSqlBuilder.class;
	
	/**
	 * Allow disable persistence cache
	 * 
	 * @since 2.0
	 */
	boolean disableCache() default false;
}
