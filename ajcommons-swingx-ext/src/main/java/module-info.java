module org.ajdeveloppement.commons.swingx {
	requires org.ajdeveloppement.commons.core;
	requires org.ajdeveloppement.commons.swing;
	requires java.desktop;
	requires java.xml.bind;
	requires java.logging;
	requires swingx;
	
	exports org.ajdeveloppement.swingxext.error;
	exports org.ajdeveloppement.swingxext.error.ui;
	exports org.ajdeveloppement.swingxext.localisation;
}