package org.ajdeveloppement.commons;

/**
 * Unchecked exceptions do not need to be declared in a method or constructor's
 * throws clause if they can be thrown by the execution of the method or constructor
 * and propagate outside the method or constructor boundary.
 * 
 * @author Aurélien JEOFFRAY
 */
public class UncheckedException extends RuntimeException {
	
	/**
	 * Construct new unchecked exceptions
	 * @param source the source of exception
	 */
	public UncheckedException(Throwable source) {
		super(source);
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
