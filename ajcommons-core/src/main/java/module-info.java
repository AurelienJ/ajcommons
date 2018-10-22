module org.ajdeveloppement.commons.core {
	requires java.xml.crypto;
	requires java.sql;
	requires java.xml.bind;
	requires java.desktop;
	requires orange.extensions;
    
	exports org.ajdeveloppement.apps;
	exports org.ajdeveloppement.apps.annotations;
	exports org.ajdeveloppement.apps.localisation;
	exports org.ajdeveloppement.commons;
	exports org.ajdeveloppement.commons.io;
	exports org.ajdeveloppement.commons.net;
	exports org.ajdeveloppement.commons.net.http;
	exports org.ajdeveloppement.commons.net.http.websocket;
	exports org.ajdeveloppement.commons.security;
	exports org.ajdeveloppement.commons.sql;
	exports org.ajdeveloppement.macosx;
}