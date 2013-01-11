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
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import server.model.npc.Npc;
import server.model.player.PlayerHandler;
import server.net.PipelineFactory;
import server.util.Misc;

/**
 * The main core of RuneSource.
 * 
 * @author blakeman8192
 */
public class Server implements Runnable {

	private static Server singleton;
	private final String host;
	private final int port;
	private final int cycleRate;

	private InetSocketAddress address;
	private Misc.Stopwatch cycleTimer;

	/**
	 * Creates a new Server.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param cycleRate
	 *            the cycle rate
	 */
	private Server(String host, int port, int cycleRate) {
		this.host = host;
		this.port = port;
		this.cycleRate = cycleRate;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: Server <host> <port> <cycleRate>");
			return;
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		int cycleRate = Integer.parseInt(args[2]);

		setSingleton(new Server(host, port, cycleRate));
		new Thread(getSingleton()).start();
	}

	@Override
	public void run() {
		try {
			System.setOut(new Misc.TimestampLogger(System.out, "./logs/out.log"));
			System.setErr(new Misc.TimestampLogger(System.err, "./logs/err.log"));

			address = new InetSocketAddress(host, port);
			System.out.println("Starting RuneSource on " + address + "...");

			// Load configuration.
			Misc.sortEquipmentSlotDefinitions();
			Misc.loadStackableItems("./data/stackable.dat");

			// Start up and get a'rollin!
			startup();
			System.out.println("Online!");
			while (true) {
				cycle();
				sleep();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Starts the server up.
	 * 
	 * @throws IOException
	 */
	private void startup() throws IOException {
		// Initialize netty and begin listening for new clients
		ServerBootstrap serverBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		serverBootstrap.setPipelineFactory(new PipelineFactory());
		serverBootstrap.bind(address);

		// Finally, initialize whatever else we need.
		cycleTimer = new Misc.Stopwatch();
		
		PlayerHandler.register(new Npc(1));
		
		/*for(int i = 0; i < 1000; i++) {
			Player player = new Player(null);
			player.setUsername("b0t" + i);
			player.setEncryptor(new ISAACCipher(new int[10]));
			try {
				player.login();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		
	}

	/**
	 * Performs a server cycle.
	 */
	private void cycle() {
		// Next, perform game processing.
		try {
			PlayerHandler.process();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sleeps for the cycle delay.
	 * 
	 * @throws InterruptedException
	 */
	private void sleep() throws InterruptedException {
		long sleepTime = cycleRate - cycleTimer.elapsed();
		
		System.out.println("Cycle time: " + cycleTimer.elapsed());
		
		if (sleepTime > 0) {
			Thread.sleep(sleepTime);
		} else {
			// The server has reached maximum load, players may now lag.
			System.out.println("[WARNING]: Server load: " + (100 + (Math.abs(sleepTime) / (cycleRate / 100))) + "%!");
		}
		cycleTimer.reset();
	}

	/**
	 * Sets the server singleton object.
	 * 
	 * @param singleton
	 *            the singleton
	 */
	public static void setSingleton(Server singleton) {
		if (Server.singleton != null) {
			throw new IllegalStateException("Singleton already set!");
		}
		Server.singleton = singleton;
	}

	/**
	 * Gets the server singleton object.
	 * 
	 * @return the singleton
	 */
	public static Server getSingleton() {
		return singleton;
	}

}
