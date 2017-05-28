/*
 * Créé le 27 sept. 2014 à 20:07:07 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2014 - Aurélien JEOFFRAY
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
package org.ajdeveloppement.commons.net.http.websocket;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This class implements an input stream filter for reading websocket input message.</p>
 * 
 * <p>Before any read operation you must invoke {@link #getNextMessage()} and wait return</p>
 * 
 * @author Aurelien JEOFFRAY
 */
public class WebSocketInputStream extends InputStream {
	
	private InputStream parentStream;
	
	private long availableBytes = 0;
	private OpCode messageType = null;
	private boolean isEndMessageFrame = false;
	private boolean isContinuousFrame = false;
	private boolean nextMessageReady = false;
	private int[] maskKey = null;
	private int currentMaskKeyByteIndex = 0;
	private boolean waitNextMessage = true;
	
	private List<WebSocketInputStreamListener> listeners = new ArrayList<>();

	/**
	 * Wrap an inputstream as a WebsocketInputStream
	 * 
	 * @param inputStream the wrapped input stream
	 */
	public WebSocketInputStream(InputStream inputStream) {
		this.parentStream = inputStream;
	}
	
	/**
	 * Wait and decode an websocket header frame
	 * 
	 * @return the frame opcode
	 * @throws IOException
	 */
	private OpCode waitFrame() throws IOException {
		DataInputStream bin = new DataInputStream(parentStream);

		boolean FIN = false;
		boolean MASK = false;
		OpCode opcode = null;
		long payloadlength = 0;

		byte[] buff = new byte[14];
		int nbRead = 0;
		
		bin.readFully(buff, 0, 2);

		FIN = (buff[0] & FrameHeader.FIN_BIT) != 0;
		opcode = OpCode.fromOrdinal((byte) ( buff[0] & 15 ));
		MASK = ( buff[1] & FrameHeader.MASK_BIT) != 0;
		payloadlength = (byte) ( buff[1] & ~FrameHeader.MASK_BIT );
		
		FrameHeader frameHeader = new FrameHeader(opcode);
		frameHeader.setEndFrame(FIN);
		frameHeader.setMaskedFrame(MASK);

		if(payloadlength > 125) {
			if(payloadlength == 126) { //chercher un entier 16 bits
				payloadlength = bin.readUnsignedShort();
			} else { //chercher un entier 64 bits
				payloadlength = bin.readLong();
			}
		}
		frameHeader.setLength(payloadlength);
		
		if(MASK) {
			maskKey = new int[4];
			for(int i = 0; i < 4; i++)
				maskKey[i] = bin.read();
			frameHeader.setMaskKey(maskKey);
			currentMaskKeyByteIndex = 0;
		} else
			maskKey = null;
		
		if(opcode == OpCode.CLOSING && payloadlength >= 2) {
			waitNextMessage = false;
			availableBytes = payloadlength;
			DataInputStream contentStream = new DataInputStream(this);
			short closeCode = contentStream.readShort();
			frameHeader.setClosingCode(StatusCode.fromOrdinal(closeCode));

			String closeReason = ""; //$NON-NLS-1$
			if(payloadlength > 2) {
				byte[] closeReasonRaw = new byte[(int)payloadlength-2];
				contentStream.readFully(closeReasonRaw, 0, closeReasonRaw.length);
				closeReason = new String(closeReasonRaw,Charset.forName("UTF-8")); //$NON-NLS-1$
				frameHeader.setClosingReason(closeReason);
			}
		}
		
		isContinuousFrame = false;
		
		if(opcode != null && opcode.isService()) {
			fireFrameReceived(frameHeader);
			return opcode;
		} else if(opcode == null)
			return null;
		
		waitNextMessage = false;
		
		switch (opcode) {
			case TEXT:
			case BINARY:
				messageType = opcode;
				break;
				
			case CONTINUOUS:
				isContinuousFrame = true;
				break;
			default:
				break;
		}
		
		availableBytes = payloadlength;

		isEndMessageFrame = FIN;
		
		fireFrameReceived(frameHeader);
		
		return opcode;
	}
	
	private int decodeByte(int in) {
		if(maskKey !=null) {
			return in ^ maskKey[(currentMaskKeyByteIndex++) % 4];
		}
		return in;
	}
	
	private void fireFrameReceived(FrameHeader frameHeader) {
		for(WebSocketInputStreamListener listener : listeners) {
			listener.frameReceived(frameHeader);
		}
	}
	
	/**
	 * Listen frame received event
	 * 
	 * @param listener
	 */
	public void addWebSocketInputStreamListener(WebSocketInputStreamListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * stop listen of frame event
	 * 
	 * @param listener
	 */
	public void removeWebSocketInputStreamListener(WebSocketInputStreamListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Wait next websocket message and return his type when arrived
	 * 
	 * @return the type of received message
	 * @throws IOException
	 */
	public synchronized OpCode getNextMessage() throws IOException {
		if(!waitNextMessage && !nextMessageReady)
			throw new IOException("Previous message read is not finished"); //$NON-NLS-1$
		
		if(!nextMessageReady) {
			do {
				OpCode opCode = null;
				do {
					opCode = waitFrame();
				} while(opCode == null || (opCode.isService() && opCode != OpCode.CLOSING));
			
				if(isContinuousFrame) {
					skip(availableBytes);
				}
			} while(isContinuousFrame);
		} else
			nextMessageReady = false;
		
		return messageType;
	}
	
	/**
	 * Return available bytes on the current message frame.<br>
	 * If 0, frame is empty or end of frame is reached, If available() == Integer.MAX_VALUE, frame size is too big
	 * for return real size.
	 */
	@Override
	public int available() throws IOException {
		if(availableBytes <= Integer.MAX_VALUE)
			return (int)availableBytes;
		return Integer.MAX_VALUE;
	};
	
	/**
	 * Return a content message byte. If return -1, end of message is reached
	 * 
	 * @return the readed content byte
	 */
	@Override
	public int read() throws IOException {
		if(waitNextMessage)
			return -1;
		
		int inByte = -1;
		if(availableBytes == 0) {
			if(!isEndMessageFrame) {
				OpCode opCode;
				do {
					opCode = waitFrame();
				} while(opCode == null || (opCode.isService() && opCode != OpCode.CLOSING));
				
				if(!isContinuousFrame) {
					nextMessageReady = true;
					return -1;
				}
			}
		}
		
		if(availableBytes > 0) {
			inByte = parentStream.read();
			if(maskKey !=null) {
				inByte = decodeByte(inByte);
			}
			
			availableBytes--;
		}
		
		if(inByte == -1)
			waitNextMessage = true;
		
		return inByte;
	}

	/**
	 * close the websocket stream
	 */
	@Override
	public void close() throws IOException {
		parentStream.close();
	}
}
