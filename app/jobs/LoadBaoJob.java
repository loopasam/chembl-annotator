package jobs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import models.BaoTerm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Logger;
import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.HttpResponse;

//Loads BAO from Bioportal API
public class LoadBaoJob extends Job {

	String rootClass = "http://www.bioassayontology.org/bao#BAO_0000015";
	//Obtained from Bioportal
	String apikey = (String) play.Play.configuration.get("apikey");

	public void doJob() throws UnsupportedEncodingException {
		Logger.info("Loading BAO...");
		//Initialisation, then goes down the hierarchy
		saveTerm(rootClass);
		Logger.info("Job done.");
	}

	private void saveTerm(String termUrl) throws UnsupportedEncodingException {
		Logger.info(termUrl);

		//Populate the term
		String encoded = URLEncoder.encode(termUrl, "UTF-8");

		//Call API for term
		HttpResponse res = WS.url("http://data.bioontology.org/ontologies/BAO/classes/" + encoded 
				+ "/?apikey=" + apikey).setHeader("Authorization", "apikey token=" + apikey).get();

		JsonObject json = res.getJson().getAsJsonObject();
		boolean obsolete = json.get("obsolete").getAsBoolean();
		
		if(!obsolete){

			String label = json.get("prefLabel").getAsString();
			String definition;
			try{
				definition = json.get("definition").getAsString();
			}catch(IllegalStateException e){
				definition = null;
			}
			String url = json.get("@id").getAsString();
			BaoTerm currentTerm = BaoTerm.createOrRetrieveTerm(url, label, definition);

			//Get the children
			HttpResponse childrenRes = WS.url("http://data.bioontology.org/ontologies/BAO/classes/" + encoded 
					+ "/children/?apikey=" + apikey).setHeader("Authorization", "apikey token=" + apikey).get();

			JsonArray childrenJson = childrenRes.getJson().getAsJsonObject().get("collection").getAsJsonArray();
			for (JsonElement jsonElement : childrenJson) {

				boolean obsoleteChild = json.get("obsolete").getAsBoolean();
				if(!obsoleteChild){
					
					String childLabel = jsonElement.getAsJsonObject().get("prefLabel").getAsString();

					String childDefinition = null;
					try{
						childDefinition = jsonElement.getAsJsonObject().get("definition").getAsString();
					}catch(IllegalStateException e){
						childDefinition = null;
					}

					String childUrl = jsonElement.getAsJsonObject().get("@id").getAsString();

					//Add the child to the current term
					currentTerm.addChild(childUrl, childLabel, childDefinition);

					//Calls the method recursively on children
					saveTerm(childUrl);
				}
			}
		}else{
			Logger.info("Obsolete term.");
		}

	}

}
