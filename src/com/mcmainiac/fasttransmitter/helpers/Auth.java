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
	private static List<Auth> auth = new ArrayList<>();
	
	private String username;
	@SuppressWarnings("unused")
	private String password;
	
	private AuthState state = AuthState.USERNAME_NEEDED;
	
	public Auth() {
		auth.add(this);
	}
	
	public AuthState getState() {
		return state;
	}
	
	public void setUsername(String username) throws AlreadyLoggedInException {
		for (Auth a : auth) {
			if (a != this && a.username != null && a.username.equals(username))
				throw new AlreadyLoggedInException("Username \"" + username + "\" is already logged in!");
		}
		this.username = username;
		this.state = AuthState.PASSWORD_NEEDED;
	}
	
	public void setPassword(String password) {
		this.password = password;
		this.state = AuthState.OK;
	}
	
	public boolean isAuthenticated() {
		return state.equals(AuthState.OK);
	}

	public void logout() {
		auth.remove(this);
	}

	public class AlreadyLoggedInException extends Throwable {
		AlreadyLoggedInException(String s) {
			super(s);
		}
	}
}
