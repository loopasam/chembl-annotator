package models;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class AnnotatedAssay extends Model {

	@ManyToOne
	public User user;
	public String label;
	
	public AnnotatedAssay(User user) {
		this.user = user;
		this.label = "foo label";
	}

}
