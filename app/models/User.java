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
public class User extends Model {

	public String email;
	public String password;
	public boolean isAdmin;
		
	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public static User connect(String email, String password) {
		return find("byEmailAndPassword", email, password).first();
	}

}
