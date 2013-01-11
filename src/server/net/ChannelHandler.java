package server.net;
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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import server.model.player.Client;
import server.model.player.Player;
import server.model.player.PlayerHandler;

/**
 * 
 * @author Stuart Murphy
 *
 */
public class ChannelHandler extends SimpleChannelHandler {

	private Client client = null;

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		ctx.getChannel().close();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!e.getChannel().isConnected())
			return;
		if(e.getMessage() instanceof Client) {
			client = (Client) e.getMessage();
			PlayerHandler.queueLogin((Player) client);
		} else if(e.getMessage() instanceof ReceivedPacket) {
			client.queuePacket((ReceivedPacket)e.getMessage());
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if(client != null) {
			client.disconnect();
			client = null;
		}
	}

}