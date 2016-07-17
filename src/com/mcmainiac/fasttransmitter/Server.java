/* FastTransmitterServer - Provides a socket for FastTransmitterClients to connect to
 * Copyright (C) 2016 Ricardo Boss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmainiac.fasttransmitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

public class Server {
	private ServerSocket server;
	
	private static HashMap<UUID, ClientWorker> clients = new HashMap<UUID, ClientWorker>();
	public static PrintWriter consoleOut = new PrintWriter(System.out);
	
	public static void main(String[] args) {
		log("=================================", 0);
		log("| FastTransmitterServer V1.0.0  |", 0);
		log("| Copyright (C) 2016            |", 0);
		log("| Ricardo Boss                  |", 0);
		log("=================================", 0);
		log("", 0);
		
		HashMap<String, Object> arguments = new HashMap<String, Object>();
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-port": arguments.put("port", Integer.parseInt(args[i+1])); break;
				default: continue;
			}
		}
		
		if (!arguments.containsKey("port"))
			arguments.put("port", 1100);
		
		new Server(arguments);
	}
	
	private Server(HashMap<String, Object> args) {
		Integer port = (Integer) args.get("port");
		try {
			log("Opening socket on " + port, 0);
			server = new ServerSocket(port);
		} catch(IOException e) {
			log("Unable to bind port! Make sure no other services are running on port " + port, 2);
			exit(-1);
		}
		
		try {
			log("Waiting for clients to connect", 0);
			Thread t;
			while (!server.isClosed()) {
				t = new Thread(new ClientWorker(UUID.randomUUID(), server.accept()));
				t.start();
			}
		} catch (Exception e) {
			log("An error occured: " + e.getMessage(), 2);
			e.printStackTrace();
			exit(-1);
		} finally {
			try {
				server.close();
				exit(0);
			} catch (IOException e) {
				log("Unable to close socket!", 2);
				log(e.getMessage(), 2);
				e.printStackTrace();
				exit(-1);
			}
		}
	}
	
	private static void log(String log, int level) {
		String time = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "]";
		String pre;
		switch (level) {
			case 1: pre = "[WARNING]"; break;
			case 2: pre = "[ERROR]"; break;
			default: pre = "[INFO]"; break;
		}
		consoleOut.println(time + " [Server] " + pre + " " + log);
		consoleOut.flush();
	}
	
	public static void log(String log, int level, String id) {
		String time = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "]";
		String pre;
		switch (level) {
			case 1:  pre = "[WARNING]"; break;
			case 2:  pre = "[ERROR]"; break;
			default: pre = "[INFO]"; break;
		}
		consoleOut.println(time + " " + id + " " + pre + " " + log);
		consoleOut.flush();
	}
	
	public static void addClientWorker(ClientWorker cw) {
		clients.put(cw.getId(), cw);
	}
	
	public static void removeClientWorker(UUID id) {
		clients.remove(id);
	}
	
	private void exit(int code) {
		log("Server stopped!", 0);
		consoleOut.close();
		System.exit(code);
	}
}
