package server.net.codecs;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.security.SecureRandom;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import server.model.player.Client;
import server.model.player.Player;
import server.net.util.ISAACCipher;
import server.net.util.StreamBuffer;
import server.util.Misc;

/**
 * 
 * @author Stuart Murphy
 *
 */
public class LoginDecoder extends FrameDecoder {

	/** Connected login state */
	private static final int CONNECTED = 0;

	/** Logging in login state */
	private static final int LOGGING_IN = 1;

	/** Current login state */
	private int state = CONNECTED;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer in) throws Exception {
		if (!channel.isConnected()) {
			return null;
		}
		switch (state) {
		case CONNECTED:
			if (in.readableBytes() < 2)
				return null;
			
			// Validate the request
			int request = in.readUnsignedByte();
			if(request != 14) {
				System.out.println("Invalid login request: " + request);
				channel.close();
				return null;
			}
			
			// Name hash
			in.readUnsignedByte();
			
			// Write the response.
			StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(17);
			out.writeLong(0); // First 8 bytes are ignored by the client.
			out.writeByte(0); // The response opcode, 0 for logging in.
			out.writeLong(new SecureRandom().nextLong()); // SSK.
			channel.write(out.getBuffer());
			
			state = LOGGING_IN;
			break;
		case LOGGING_IN:
			if (in.readableBytes() < 2)
				return null;

			// Validate the login type
			int loginType = in.readUnsignedByte();
			if (loginType != 16 && loginType != 18) {
				System.out.println("Invalid login type: " + loginType);
				channel.close();
				return null;
			}

			// Make sure we have the complete login block
			int blockLength = in.readUnsignedByte();
			int loginEncryptSize = blockLength - (36 + 1 + 1 + 2);
			if (loginEncryptSize <= 0) {
				System.out.println("Encrypted packet size zero or negative: " + loginEncryptSize);
				channel.close();
				return null;
			}

			in.readUnsignedByte(); // Magic id

			// Validate the client version
			int clientVersion = in.readUnsignedShort();
			if (clientVersion != 317) {
				System.out.println("Invalid client version: " + clientVersion);
				channel.close();
				return null;
			}

			// High/low memory
			in.readByte();

			// Skip the CRC keys.
			for (int i = 0; i < 9; i++)
				in.readInt();
			
			// Skip RSA block length.
			in.readByte();

			// Validate that the RSA block was decoded properly.
			int rsaOpcode = in.readByte();
			if (rsaOpcode != 10) {
				System.err.println("Unable to decode RSA block properly!");
				channel.close();
				return null;
			}

			// Set up the ISAAC ciphers.
			long clientHalf = in.readLong();
			long serverHalf = in.readLong();
			int[] isaacSeed = { (int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf };
			ISAACCipher inCipher = new ISAACCipher(isaacSeed);
			for (int i = 0; i < isaacSeed.length; i++)
				isaacSeed[i] += 50;
			ISAACCipher outCipher = new ISAACCipher(isaacSeed);
			
			int version = in.readInt();
			
			String username = Misc.getRS2String(in);
			String password = Misc.getRS2String(in);
			
			return login(channel, inCipher, outCipher, version, username, password);
		}
		return null;
	}

	private static Client login(Channel channel, ISAACCipher inCipher, ISAACCipher outCipher, int version, String name, String pass) {

			// Validate name
			if (!name.matches("[A-Za-z0-9 ]+") || name.length() > 12 || name.length() <= 0) {
				sendReturnCode(channel, 16);
				return null;
			}
	        
			// Switch the packet decoder to the game decoder
			channel.getPipeline().remove("decoder");
			channel.getPipeline().addFirst("decoder", new Decoder(inCipher));
			
			Client client = new Player(channel);
			client.setUsername(name);
			client.setPassword(pass);
			client.setEncryptor(outCipher);
	        
			return client;
	}

	public static void sendReturnCode(Channel channel, int code) {
		// Write the response.
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(1);
		out.writeByte(code); // First 8 bytes are ignored by the client.
		
		channel.write(out.getBuffer()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture arg0) throws Exception {
				arg0.getChannel().close();
			}
		});
	}

}
