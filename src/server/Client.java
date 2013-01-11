package server;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;

import server.net.ReceivedPacket;
import server.net.util.ISAACCipher;
import server.net.util.StreamBuffer;

/**
 * The class behind a Player that handles all networking-related things.
 * 
 * @author blakeman8192
 */
public abstract class Client {
	
	private final Channel channel;
	private final Queue<ReceivedPacket> queuedPackets = new ConcurrentLinkedQueue<ReceivedPacket>();
	
	private final Player player = (Player) this;
	private final Misc.Stopwatch timeoutStopwatch = new Misc.Stopwatch();

	private String username;
	private String password;
	private ISAACCipher encryptor;

	/**
	 * Creates a new Client.
	 * 
	 * @param key
	 *            the SelectionKey of the client
	 */
	public Client(Channel channel) {
		this.channel = channel;
	}

	/**
	 * Called after the player finishes logging in.
	 * 
	 * @throws Exception
	 */
	public abstract void login() throws Exception;

	/**
	 * Called before the player disconnects.
	 * 
	 * @throws Exception
	 */
	public abstract void logout() throws Exception;

	/**
	 * Adds a packet to the queue
	 * @param packet
	 */
	public void queuePacket(ReceivedPacket packet) {
		queuedPackets.add(packet);
	}
	
	/**
	 * Handles packets we have received
	 */
	public void processQueuedPackets() {
		ReceivedPacket packet = null;
		while((packet = queuedPackets.poll()) != null) {
			handlePacket(packet.getOpcode(), packet.getSize(), StreamBuffer.OutBuffer.newInBuffer(packet.getPayload()));
		}
	}
	
	/**
	 * Sends all skills to the client.
	 */
	public void sendSkills() {
		for (int i = 0; i < player.getSkills().length; i++) {
			sendSkill(i, player.getSkills()[i], player.getExperience()[i]);
		}
	}

	/**
	 * Sends the skill to the client.
	 * 
	 * @param skillID
	 *            the skill ID
	 * @param level
	 *            the skill level
	 * @param exp
	 *            the skill experience
	 */
	public void sendSkill(int skillID, int level, int exp) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(8);
		out.writeHeader(getEncryptor(), 134);
		out.writeByte(skillID);
		out.writeInt(exp, StreamBuffer.ByteOrder.MIDDLE);
		out.writeByte(level);
		send(out.getBuffer());
	}

	/**
	 * Sends all equipment.
	 */
	public void sendEquipment() {
		for (int i = 0; i < player.getEquipment().length; i++) {
			sendEquipment(i, player.getEquipment()[i], player.getEquipmentN()[i]);
		}
	}

	/**
	 * Sends the equipment to the client.
	 * 
	 * @param slot
	 *            the equipment slot
	 * @param itemID
	 *            the item ID
	 * @param itemAmount
	 *            the item amount
	 */
	public void sendEquipment(int slot, int itemID, int itemAmount) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(32);
		out.writeVariableShortPacketHeader(getEncryptor(), 34);
		out.writeShort(1688);
		out.writeByte(slot);
		out.writeShort(itemID + 1);
		if (itemAmount > 254) {
			out.writeByte(255);
			out.writeShort(itemAmount);
		} else {
			out.writeByte(itemAmount);
		}
		out.finishVariableShortPacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends the current full inventory.
	 */
	public void sendInventory() {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(256);
		out.writeVariableShortPacketHeader(getEncryptor(), 53);
		out.writeShort(3214);
		out.writeShort(player.getInventory().length);
		for (int i = 0; i < player.getInventory().length; i++) {
			if (player.getInventoryN()[i] > 254) {
				out.writeByte(255);
				out.writeInt(player.getInventoryN()[i], StreamBuffer.ByteOrder.INVERSE_MIDDLE);
			} else {
				out.writeByte(player.getInventoryN()[i]);
			}
			out.writeShort(player.getInventory()[i] + 1, StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
		}
		out.finishVariableShortPacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends a message to the players chat box.
	 * 
	 * @param message
	 *            the message
	 */
	public void sendMessage(String message) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(message.length() + 3);
		out.writeVariablePacketHeader(getEncryptor(), 253);
		out.writeString(message);
		out.finishVariablePacketHeader();
		send(out.getBuffer());
	}

	/**
	 * Sends a sidebar interface.
	 * 
	 * @param menuId
	 *            the interface slot
	 * @param form
	 *            the interface ID
	 */
	public void sendSidebarInterface(int menuId, int form) {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(4);
		out.writeHeader(getEncryptor(), 71);
		out.writeShort(form);
		out.writeByte(menuId, StreamBuffer.ValueType.A);
		send(out.getBuffer());
	}

	/**
	 * Refreshes the map region.
	 */
	public void sendMapRegion() {
		player.getCurrentRegion().setAs(player.getPosition());
		player.setNeedsPlacement(true);
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(5);
		out.writeHeader(getEncryptor(), 73);
		out.writeShort(player.getPosition().getRegionX() + 6, StreamBuffer.ValueType.A);
		out.writeShort(player.getPosition().getRegionY() + 6);
		send(out.getBuffer());
	}

	/**
	 * Disconnects the client.
	 */
	public void disconnect() {
		System.out.println(this + " disconnecting.");
		try {
			logout();
			channel.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles a clicked button.
	 * 
	 * @param buttonId
	 *            the button ID
	 */
	private void handleButton(int buttonId) {
		switch (buttonId) {
		case 9154:
			sendLogout();
			break;
		case 153:
			player.getMovementHandler().setRunToggled(true);
			break;
		case 152:
			player.getMovementHandler().setRunToggled(false);
			break;
		default:
			System.out.println("Unhandled button: " + buttonId);
			break;
		}
	}

	/**
	 * Sends a packet that tells the client to log out.
	 */
	public void sendLogout() {
		StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(1);
		out.writeHeader(getEncryptor(), 109);
		send(out.getBuffer());
	}

	/**
	 * Handles the current packet.
	 */
	private void handlePacket(int packetOpcode, int packetLength, StreamBuffer.InBuffer in) {
		timeoutStopwatch.reset();
		// Handle the packet.
		try {
			switch (packetOpcode) {
			case 145: // Remove item.
				int interfaceID = in.readShort(StreamBuffer.ValueType.A);
				int slot = in.readShort(StreamBuffer.ValueType.A);
				in.readShort(StreamBuffer.ValueType.A); // Item ID.
				if (interfaceID == 1688) {
					player.unequip(slot);
				}
				break;
			case 41: // Equip item.
				in.readShort(); // Item ID.
				slot = in.readShort(StreamBuffer.ValueType.A);
				in.readShort(); // Interface ID.
				player.equip(slot);
				break;
			case 185: // Button clicking.
				handleButton(Misc.hexToInt(in.readBytes(2)));
				break;
			case 4: // Player chat.
				int effects = in.readByte(false, StreamBuffer.ValueType.S);
				int color = in.readByte(false, StreamBuffer.ValueType.S);
				int chatLength = (packetLength - 2);
				byte[] text = in.readBytesReverse(chatLength, StreamBuffer.ValueType.A);
				player.setChatEffects(effects);
				player.setChatColor(color);
				player.setChatText(text);
				player.setChatUpdateRequired(true);
				break;
			case 103: // Player command.
				String command = in.readString();
				String[] split = command.split(" ");
				player.handleCommand(split[0].toLowerCase(), Arrays.copyOfRange(split, 1, split.length));
				break;
			case 248: // Movement.
			case 164: // ^
			case 98: // ^
				int length = packetLength;
				if (packetOpcode == 248) {
					length -= 14;
				}
				int steps = (length - 5) / 2;
				int[][] path = new int[steps][2];
				int firstStepX = in.readShort(StreamBuffer.ValueType.A, StreamBuffer.ByteOrder.LITTLE);
				for (int i = 0; i < steps; i++) {
					path[i][0] = in.readByte();
					path[i][1] = in.readByte();
				}
				int firstStepY = in.readShort(StreamBuffer.ByteOrder.LITTLE);

				player.getMovementHandler().reset();
				player.getMovementHandler().setRunPath(in.readByte(StreamBuffer.ValueType.C) == 1);
				player.getMovementHandler().addToPath(new Position(firstStepX, firstStepY));
				for (int i = 0; i < steps; i++) {
					path[i][0] += firstStepX;
					path[i][1] += firstStepY;
					player.getMovementHandler().addToPath(new Position(path[i][0], path[i][1]));
				}
				player.getMovementHandler().finish();
				break;
			default:
				System.out.println(this + " unhandled packet received " + packetOpcode + " - " + packetLength);
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sends the buffer to the socket.
	 * 
	 * @param buffer
	 *            the buffer
	 * @throws IOException
	 */
	public void send(ChannelBuffer buffer) {
		if(channel == null || !channel.isConnected()) {
			return;
		}
		channel.write(buffer);
	}

	/**
	 * Gets the remote host of the client.
	 * 
	 * @return the host
	 */
	public String getHost() {
		if(channel == null) {
			return "unknown";
		}
		return ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
	}

	/**
	 * Sets the username.
	 * 
	 * @param username
	 *            the username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the encryptor.
	 * 
	 * @param encryptor
	 *            the encryptor
	 */
	public void setEncryptor(ISAACCipher encryptor) {
		this.encryptor = encryptor;
	}

	/**
	 * Gets the encryptor.
	 * 
	 * @return the encryptor
	 */
	public ISAACCipher getEncryptor() {
		return encryptor;
	}

	/**
	 * Gets the Player subclass implementation of this superclass.
	 * 
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	public Misc.Stopwatch getTimeoutStopwatch() {
		return timeoutStopwatch;
	}
	
}
