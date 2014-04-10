package models;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import play.Logger;
import play.libs.Mail;

public class Utils {

	public static void emailAdmin(String subject, String message) {

		SimpleEmail email = new SimpleEmail();
		try {
			email.setFrom("super.cool.bot@gmail.com");
			String address = (String) play.Play.configuration.get("mail.admin");
			email.addTo(address);
			email.setSubject(subject);
			email.setMsg(message);
			Mail.send(email); 
		} catch (EmailException e) {
			Logger.error(e.getLocalizedMessage());
		}
	}

}
