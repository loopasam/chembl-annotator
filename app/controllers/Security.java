package controllers;

import models.Reviewer;

public class Security extends Secure.Security {

	static boolean authenticate(String email, String password) {
		return Reviewer.connect(email, password) != null;
	}

	static void onDisconnected() {
		Application.index();
	}

	static void onAuthenticated() {
		Application.index();
	}

	static boolean check(String profile) {
		if("admin".equals(profile)) {
			return Reviewer.find("byEmail", connected()).<Reviewer>first().isAdmin;
		}
		return false;
	}

}
