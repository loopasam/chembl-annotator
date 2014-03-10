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
	
	//BAd idea
	@OneToMany(mappedBy="user")
	public List<AnnotatedAssay> annotatedAssays;
	
	public User(String email, String password) {
		this.email = email;
		this.password = password;
		this.annotatedAssays = new ArrayList<>();
	}

	public static User connect(String email, String password) {
		return find("byEmailAndPassword", email, password).first();
	}

	public void addAssay(String label) {
		AnnotatedAssay assay = new AnnotatedAssay(this).save();
		this.annotatedAssays.add(assay);
		this.save();
	}

}
