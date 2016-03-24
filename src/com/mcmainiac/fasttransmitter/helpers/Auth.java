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
package com.mcmainiac.fasttransmitter.helpers;

import java.util.ArrayList;
import java.util.List;

public class Auth {
	private static List<Auth> auths = new ArrayList<Auth>();
	
	private String username;
	@SuppressWarnings("unused")
	private String password;
	
	private AuthState state = AuthState.USERNAME_NEEDED;
	
	public Auth() {
		auths.add(this);
	}
	
	public AuthState getState() {
		return this.state;
	}
	
	public void setUsername(String username) {
		for (Auth a : auths) {
			if (a != this && a.getUsername() == username) return;
		}
		this.username = username;
		this.state = AuthState.PASSWORD_NEEDED;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setPassword(String password) {
		this.password = password;
		this.state = AuthState.OK;
	}
	
	public boolean isAuthenticated() {
		if (this.getState().equals(AuthState.OK)) return true;
		else return false;
	}
}
