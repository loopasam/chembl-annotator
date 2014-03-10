package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {
	
	//Job to check if the BAO terms needs updating
	//if so, triggers the job synchronoously

    public static void index() {
        render();
    }

}