package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.ManyToAny;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Reviewer extends Model {

	public String email;
	public String password;
	public boolean isAdmin;
	public boolean coolTheme;

	public Reviewer(String email, String password) {
		this.email = email;
		this.password = password;
		this.coolTheme = false;
	}

	public static Reviewer connect(String email, String password) {
		return find("byEmailAndPassword", email, password).first();
	}

	public String toString() {
		return email;
	}

	public static Reviewer randomReviewer() {
		return Reviewer.find("email != 'super.cool.bot@gmail.com' order by rand()").first();
	}

}
