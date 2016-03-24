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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import com.mcmainiac.fasttransmitter.helpers.Auth;

public class ClientWorker implements Runnable {
	private Socket clientSocket;
	private BufferedReader reader;
	private PrintWriter writer;
	
	private Auth auth;
	private UUID id;
	
	public ClientWorker(Server server, UUID id, Socket clientSocket) {
		Server.log("Recieved connection from " + clientSocket.getInetAddress().toString().substring(1) + ":" + clientSocket.getPort() + "...", 0, id.toString());
		this.clientSocket = clientSocket;
		try {
			this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.writer = new PrintWriter(clientSocket.getOutputStream());
		} catch (Exception e) {
			Server.log("An error occured!", 2, getDisplayName());
			Server.log(e.getMessage(), 2, getDisplayName());
			e.printStackTrace(Server.consoleOut);
			try {
				CloseConnection();
			} catch (IOException ioe) {
				Server.log("Error while trying to close the connection!", 2, getDisplayName());
				ioe.printStackTrace(Server.consoleOut);
			}
		}
		this.id = id;
		server.addClientWorker(this);
	}

	@Override
	public void run() {
		try {
			write("100 Welcome to the FastTransmitter server!");
			write("301 Login needed");
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("304")) Server.log("[<-] 304 ****", 0, getDisplayName()); // hide password sent from client
				else Server.log("[<-] " + line, 0, getDisplayName());
				
				String[] split = line.split(" ");
				String cmd = split[0];
				
				String response = null;
				
				switch (cmd) {
				case "300": response = CheckConnection(); break; // 300: check connection
				case "302": response = Username(split[1]); break; // 302: set user name
				case "304": response = Password(split[1]); break; // 304: set user password
				case "400": response = "400 Disconnected"; break; // 400: disconnect
				default: response = "500 Command not implemented: " + cmd;
				}

				if (cmd.startsWith("400") || !clientSocket.isConnected()) { CloseConnection(); break; }
				write(response);
			}
		} catch (IOException e) {
			Server.log("IOException occured!", 2, getDisplayName());
			Server.log("Client disconnected!", 1, getDisplayName());
		}
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public String getDisplayName() {
		return clientSocket.getInetAddress().toString().substring(1) + ":" + clientSocket.getPort();
	}
	
	// FaTP Commands
	private String CheckConnection() throws IOException {
		if (clientSocket.isConnected())
			return "201 Connection OK";
		else {
			Server.log("501 Connection aborted!", 0, getDisplayName());
			return null;	
		}
	}
	
	private String Username(String username) throws IOException {
		if (clientSocket.isConnected()) {
			this.auth = new Auth();
			auth.setUsername(username);
			switch (auth.getState()) {
			case OK: return "200 Logged in";
			case PASSWORD_NEEDED: return "303 Username ok, password needed";
			default: return "305 Authentication failed";
			}
		} else {
			Server.log("501 Connection aborted!", 0, getDisplayName());
			return null;	
		}
	}
	
	private String Password(String password) throws IOException {
		if (clientSocket.isConnected()) {
			auth.setPassword(password);
			if (auth.isAuthenticated()) return "200 Logged in";
			else return "305 Authentication failed";
		} else {
			Server.log("501 Connection aborted!", 0, getDisplayName());
			return null;
		}
	}
	
	private void CloseConnection() throws IOException {
		clientSocket.close();
		Server.log("Client disconnected!", 0, getDisplayName());
	}
	// END
	
	private void write(String message) {
		writer.println(message);
		writer.flush();
		Server.log("[->] " + message, 0, getDisplayName());
	}
}
