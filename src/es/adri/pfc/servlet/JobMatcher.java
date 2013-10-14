package es.adri.pfc.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.adri.pfc.config.Configuration;
import es.adri.pfc.connections.JSONTreatment;
import es.adri.pfc.data.Profile;
import es.adri.pfc.operations.PublicationDate;
import es.adri.pfc.operations.Salary;
import es.adri.pfc.operations.SemanticSemMF;
import es.adri.pfc.rdfs.RDFConstructor;

public class JobMatcher extends HttpServlet{
	private Logger log;
	private String urlServerLmf;
	private String urlFileConfiguration;
	private Configuration conf;
	private static final long serialVersionUID = 1L;
	
	/**
	 * Inicializacion del servlet.
	 */
    public void init() throws ServletException {
    	log =  LoggerFactory.getLogger(JobMatcher.class);
		urlFileConfiguration = getServletContext().getRealPath("/config/configuration.properties");
		conf = new Configuration(urlFileConfiguration);
		urlServerLmf = conf.getProperty("serverLmf");
		log.info("Se ha leído correctamente el archivo de configuracion");
    	super.init();
    }
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JobMatcher() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doProcess(request, response); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doProcess(request, response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo al que se redirigen tanto los post como los get realizados al servlet. Se encarga de recoger la peticion realizada 
	 * por el usuario, producir la comunicacion con los motores de recomendacion y devolver la respuesta al cliente.
	 *  
	 * @param request Peticion realizada
	 * @param response Respuesta del servlet. Sera la recomendacion semantica o social que se ha solicitado.
	 * @throws ServletException
	 * @throws IOException
	 * @throws JSONException
	 */
	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		// Declaracion de objetos
		String baseUrl = getServletContext().getRealPath("/");
		RDFConstructor rdc = new RDFConstructor();
		SemanticSemMF sSem = new SemanticSemMF();
		JSONTreatment jt = new JSONTreatment(urlServerLmf);
		log.info("Declarados los objetos de las distintas clases");
				
		// Extraccion de los parametros que contiene la "query"
		Profile prof = jt.getProfile(new JSONObject(request.getParameter("q")));
		
		// Construción de  ficheros y definicion de sus rutas 
		File fileQuery = new File(baseUrl + "/resources/query.rdf");
		rdc.rdfOffer(fileQuery, prof);
		String pathOffers = baseUrl + "/resources/offers.rdf";
		log.info("Creación/lectura archivos auxiliares correcta");
		
				
		// Ejecutando coincidencia semantica (usando la herramienta SemMF)
		JSONArray req = sSem.calMatching(baseUrl, pathOffers, fileQuery.getAbsolutePath(), prof.getId());
		log.info("El JSON del resultado semántico es: "+req);
		
		// Ejecutando similitud de contenido
		Salary salary = new Salary(urlServerLmf, baseUrl, req);
		PublicationDate pd = new PublicationDate(baseUrl, urlServerLmf, salary.getResponseSalary());
		req = pd.getResponse();
		
		// Devolviendo la salida
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin","*");
		PrintWriter pw = new PrintWriter(response.getOutputStream());
		pw.println(req);
		pw.close();
	}
}
