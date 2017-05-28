/*
 * Créé le 28 sept. 2014 à 11:16:24 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Aurelien JEOFFRAY
 *
 */
public class WebSocketOutputStream extends OutputStream {
	
	private OutputStream innerOutputStream;
	private int maxFrameSize = 65535;
	private OpCode currentFrameOpCode = null;
	private boolean masked = false;
	private boolean endFrame = false;
	
	private ByteArrayOutputStream cache = new ByteArrayOutputStream();
	
	/**
	 * Wrap an OutputStream as Websocket message OutputStream
	 * @param out the wrapped OutputStream
	 */
	public WebSocketOutputStream(OutputStream out) {
		this.innerOutputStream = out;
	}
	
	private byte[] generateMask() {
		Random rnd = new Random();
		byte[] mask = new byte[4];
		
		rnd.nextBytes(mask);
		
		return mask;
	}
	
	private ByteBuffer createFrameHeader(boolean endFrame, OpCode opcode, byte[] mask, long datalength) {
		int headerlength = 2;
		
		boolean shortSizeFrame = false;
		
		if(datalength >= 126 && datalength <= 65535) {
			shortSizeFrame = true;
			headerlength += 2;
		} else if(datalength > 65535)
			headerlength += 8;
		
		if(mask != null)
			headerlength += 4;

		ByteBuffer header = ByteBuffer.allocate(headerlength);
		
		byte firstByte = 0;
		byte secondByte = 0;
		
		if(endFrame)
			firstByte |= FrameHeader.FIN_BIT;
		firstByte |= opcode.getCode();
		header.put(firstByte);
		
		if(mask != null) {
			secondByte |= FrameHeader.MASK_BIT;
		}
		
		if(datalength<126)
			secondByte = (byte)datalength;
		else {
			secondByte = (byte)(shortSizeFrame ? 126 : 127);
		}
		header.put(secondByte);
		
		if(datalength >= 126 && datalength <= 65535)
			header.putShort((short)datalength);
		else if(datalength > 65535)
			header.putLong(datalength);
		
		if(mask != null) {
			header.put(mask);
		}
		
		return header;
	}
	
	private synchronized void sendFrame(boolean endFrame, OpCode opcode) throws IOException {
		byte[] mask = null;
		if(masked && !opcode.isService())
			mask = generateMask();
		
		ByteBuffer headerFrame = createFrameHeader(endFrame, opcode, mask, cache.size());
		innerOutputStream.write(headerFrame.array());
		if(!opcode.isService())
			cache.writeTo(innerOutputStream);
		cache.reset();
		innerOutputStream.flush();
	}
	
	/**
	 * @return the maxFrameSize
	 */
	public int getMaxFrameSize() {
		return maxFrameSize;
	}

	/**
	 * @param maxFrameSize the maxFrameSize to set
	 */
	public void setMaxFrameSize(int maxFrameSize) {
		this.maxFrameSize = maxFrameSize;
	}

	/**
	 * @return the masked
	 */
	public boolean isMasked() {
		return masked;
	}

	/**
	 * @param masked the masked to set
	 */
	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public void startMessage(OpCode operationCode) throws IOException {
		if(!endFrame && this.currentFrameOpCode != null)
			endMessage();
		
		this.endFrame = false;
		this.currentFrameOpCode = operationCode;
	}
	
	public void endMessage() throws IOException {
		endFrame = true;
		flush();
	}
	
	public void sendServiceFrame(OpCode serviceOperationCode, StatusCode closeCode, String closeReason) throws IOException {
		if(!serviceOperationCode.isService())
			throw new IllegalArgumentException("arg must be a service OpCode"); //$NON-NLS-1$
		
		byte[] body = null;
		if(serviceOperationCode == OpCode.CLOSING && closeCode != null) {
			int bodyLength = 2;
			if(closeReason != null)
				bodyLength += closeReason.length();
			body = new byte[bodyLength];
			body[0] = (byte)(closeCode.getCode()>>>8);
			body[1] = (byte)(closeCode.getCode()&0xFF);
			if(closeReason != null)
				System.arraycopy(closeReason.getBytes(Charset.forName("UTF-8")), 0, body, 2, closeReason.length()); //$NON-NLS-1$
		}
		
		ByteBuffer headerFrame = createFrameHeader(endFrame, serviceOperationCode, null, body != null ? body.length : 0);
		innerOutputStream.write(headerFrame.array());
		if(body != null)
			innerOutputStream.write(body);
		innerOutputStream.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		cache.write(b);
		if(cache.size() >= maxFrameSize) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		if(currentFrameOpCode != null) {
			sendFrame(endFrame, currentFrameOpCode);
			currentFrameOpCode = OpCode.CONTINUOUS;
			super.flush();
		}
	}
}
