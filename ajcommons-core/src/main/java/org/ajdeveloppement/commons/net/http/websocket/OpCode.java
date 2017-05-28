package org.ajdeveloppement.commons.net.http.websocket;

public enum OpCode {
	CONTINUOUS((byte)0x0, false),
	TEXT((byte)0x1, false),
	BINARY((byte)0x2, false),
	PING((byte)0x9, true),
	PONG((byte)0x10, true),
	CLOSING((byte)0x8, true);
	
	byte code = 0;
	boolean service = false;
	
	private OpCode(byte code, boolean service) {
		this.code = code;
		this.service = service;
	}
	
	public byte getCode() {
		return code;
	}
	
	public boolean isService() {
		return service;
	}
	
    public static OpCode fromOrdinal(byte n) {
    	for(OpCode value : values()) {
    		if(value.getCode() == n)
    			return value;
    	}
    	return null;
    }
}
