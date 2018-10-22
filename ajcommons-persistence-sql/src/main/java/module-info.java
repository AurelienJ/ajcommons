module org.ajdeveloppement.commons.persistence.sql {
	requires org.ajdeveloppement.commons.core;
	requires org.ajdeveloppement.commons.persistence;
	requires java.sql;
	requires java.compiler;
	requires java.xml.bind;
	requires java.naming;
	requires java.desktop;
	
	exports org.ajdeveloppement.commons.persistence.sql;
	exports org.ajdeveloppement.commons.persistence.sql.annotations;
}