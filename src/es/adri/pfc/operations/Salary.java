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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Salary {
	private Logger log;
	private JSONArray responseSalary;
	private String urlServerLmf;
	private String baseUrl;
	private JSONArray inf;
	
	/**
	 * Constructor de la clase Salary.
	 */
	public Salary(String urlServerLmf, String baseUrl, JSONArray inf){
		log = LoggerFactory.getLogger(Salary.class);
		this.responseSalary = new JSONArray();
		this.urlServerLmf=urlServerLmf;
		this.baseUrl=baseUrl;
		this.inf=inf;
		calcSimSal();
	}
	
	private void calcSimSal(){
		String querySalmax = readSparql(baseUrl + "/resources/salarymax.sparql");
//		String querySalmin = readSparql(baseUrl + "/resources/salarymin.sparql");
		String query = readSparql(baseUrl + "/resources/salary.sparql");
		try {
			querySalmax = URLEncoder.encode(querySalmax, "UTF-8");
			querySalmax = urlServerLmf + "sparql/select?query="+querySalmax+"&output=json";
			String salMaxString = getJson(querySalmax).getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("salary").getString("value");
			double salMax = Double.parseDouble(salMaxString);
//			querySalmin = URLEncoder.encode(querySalmin, "UTF-8");
//			querySalmin = urlServerLmf + "sparql/select?query="+querySalmin+"&output=json";
//			String salMinString = getJson(querySalmin).getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("salary").getString("value");
//			double salMin = Double.parseDouble(salMinString);
			for (int i = 0; i < inf.length(); i++) {
				JSONObject offer = inf.getJSONObject(i);
				String url = offer.getString("url");
				double sim = Double.parseDouble(offer.getString("sim"));
				String salQuery = query.replace("Resource", url);
				salQuery = URLEncoder.encode(salQuery, "UTF-8");
				salQuery = urlServerLmf + "sparql/select?query="+salQuery+"&output=json";
				String salString = getJson(salQuery).getJSONObject("results").getJSONArray("bindings").getJSONObject(0).getJSONObject("salary").getString("value");
				double sal = Double.parseDouble(salString);
				JSONObject jo = new JSONObject();
				jo.put("sim", sim + sal/salMax);
				jo.put("url", url);
				responseSalary.put(jo);
			}			
		} catch (UnsupportedEncodingException e) {
			log.error("No se soporta la codificacion de la query para consultar sueldo");
			e.printStackTrace();
		} catch (JSONException e1) {
			log.error("Fallo al crear objeto JSON en consulta salario");
			e1.printStackTrace();
		}
	}
	
	/**
	 * Devulve la respuesta tras haber tenido en cuenta el sueldo ofrecido por cada oferta de empleo.
	 * 
	 * @return response .- JSONArray de respuesta para el servidor.
	 */
	public JSONArray getResponseSalary(){
		return this.responseSalary;
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
