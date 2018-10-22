module org.ajdeveloppement.commons.updater {
	requires org.ajdeveloppement.commons.core;
	requires org.ajdeveloppement.commons.swing;
	requires java.desktop;
	requires java.xml.bind;
	requires java.xml.crypto;
	requires java.scripting;
	//requires java.logging;
	requires ant;

	exports org.ajdeveloppement.updater;
	exports org.ajdeveloppement.updater.ant;
	exports org.ajdeveloppement.updater.tools;
	exports org.ajdeveloppement.updater.ui;
}