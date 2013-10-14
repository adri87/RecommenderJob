package es.adri.pfc.operations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Esta clase calcula la diferencia en días entre las fecha actual y la fecha de publicacion 
 * de las vacantes de empleo.
 * 
 * @author Adriano Jose Martin Gutierrez
 * @version 1.0
 */
public class PublicationDate {
	private Logger log;
	private String urlServerLmf;
	private JSONArray inf;
	private JSONArray responsePublicationDate;
	private Date currentDate;
	private DateFormat df;
	private String query;
	
	/**
	 * Constructor de la clase PublicationDate.
	 * 
	 * @param baseUrl .- URL en la que se encuentra desplegado el servicio.
	 * @param urlServerLmf .- URL del servidor LMF.
	 * @param inf .- JSONArray que contiene el identificador de cada oferta de empleo presente en el sistema.
	 */
	public PublicationDate (String baseUrl, String urlServerLmf, JSONArray inf){
		this.log = LoggerFactory.getLogger(PublicationDate.class);
		this.urlServerLmf=urlServerLmf;
		this.inf=inf;
		responsePublicationDate = new JSONArray();
		currentDate = new Date();
		query = readSparql(baseUrl + "/resources/publication.sparql");
		diffTemp();
	}
	
	/**
	 * Se encarga de llevar a cabo las consultas y calcular la diferencia temporal de la ofertas respecto de la 
	 * fecha actual. 
	 */
	private void diffTemp (){
		for (int i = 0; i < inf.length(); i++) {
			try {
				JSONObject offer = inf.getJSONObject(i);
				String url = offer.getString("url");
				double sim = Double.parseDouble(offer.getString("sim"));
				String dateQuery = query.replace("Resource", url);
				dateQuery = URLEncoder.encode(dateQuery, "UTF-8");
				dateQuery = urlServerLmf + "sparql/select?query="+dateQuery+"&output=json";
				JSONObject result = getJson(dateQuery);
				String publicationDate = result.getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("date").getString("value");
				df = DateFormat.getDateInstance(DateFormat.SHORT);
				Date publ = df.parse(publicationDate);
			    double diffDays = (currentDate.getTime() - publ.getTime()) / (60 * 60 * 1000 * 24);
			    JSONObject jo = new JSONObject();
			    System.out.println(sim * Math.exp(-(diffDays/10)));
			    jo.put("sim", sim * Math.exp(-(diffDays/10)));
			    jo.put("url", url);
			    this.responsePublicationDate.put(jo);
			    log.info("Se ha calculado correctamente la diferencia de dias");
			} catch (JSONException e) {
				log.error("Fallo al crear objeto JSON en calculo de fecha");
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				log.error("No se soporta la codificacion de la query para consultar fechas");
				e1.printStackTrace();
			} catch (ParseException e2) {
				log.error("Error al parsear fecha");
				e2.printStackTrace();
			}	
		}
	}
	
	
	/**
	 * Devuelve la respuesta tras haber aplicado la variable temporal.
	 * 
	 * @return response .- JSONArray de respuesta para el servidor.
	 */
	public JSONArray getResponse () {
		return this.responsePublicationDate;
	}
	
	/**
	 * Este metodo se encarga de leer y devolver el contenido del fichero que se le solicita.
	 * 
	 * @param fileName .- Nombre del fichero a leer.
	 * @return Un String con el contenido del fichero.
	 */
	private String readSparql(String fileName){
		BufferedReader br = null;
		String content;
		try {
			br = new BufferedReader(new FileReader(fileName));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	        	sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
	        }
	        content = sb.toString();
		} catch (FileNotFoundException e) {
			log.error("Archivo no encontrado");
			throw new RuntimeException("File not found");
		} catch (IOException e) {
			log.error("Erron en ejecucion");
			throw new RuntimeException("IO Error occured");
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	
	/**
	 * Hace una consulta al servidor donde se encuentran los datos para obtener la respuesta deseada.
	 * 
	 * @param query .- consulta a realizar al servidor.
	 * @return JSONObject con la información solicitada.
	 */
	private JSONObject getJson(String query){
		URL url;
	    HttpURLConnection conn;
	    BufferedReader rd;
	    JSONObject json = new JSONObject();
	    String line;
	    String result = "";
	    try {
	    	url = new URL(query);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        while ((line = rd.readLine()) != null) {
	        	result += line;
	        }
	        rd.close();
	        json = new JSONObject(result);
	    } catch (Exception e) {
	    	log.info("Error al obtener JSON");
	    	e.printStackTrace();
		}
		return json;
	}

}
