package es.adri.pfc.rdfs;

import java.io.File;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.adri.pfc.data.Profile;

/**
 * Clase que se encarga de contruir los ficheros RDF utilizados por la herramienta SemMf para 
 * llevar a cabo su calculo de similitud semantica.
 * 
 * @author Adriano Jose Martin Gutierrez
 * @version 1.0
 */
public class RDFConstructor {
	private Logger log = LoggerFactory.getLogger(RDFConstructor.class);
	
	/**
	 * Se encarga de crear el RDF correspondiente a las consulta de empleo que se esta realizando en el servicio.
	 * El fichero contendra tanto el nombre de la consulta como las competencias que posee el cliente en formato XML.
	 * 
	 * @param query .- Fichero en el cual se va a escribir la informacion exigida en la busqueda.
	 * @param prof .- Objeto que contiene el perfil del candidato que busca empleo.
	 */
	public void rdfOffer(File query, Profile prof){
		log.info("Creando archivo auxiliar con que contiene la consulta ...");
		try {
			query.createNewFile();
			FileWriter out = new FileWriter(query);
			out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			out.write("<rdf:RDF\n");
			out.write("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
			out.write("xmlns:cr=\"http://example.org/CategoriesRequired.rdfs#\"\n");
			out.write("xmlns:skill=\"http://kmm.lboro.ac.uk/ecos/1.0#\"\n");
			out.write("xml:base=\"http://example.org/CategoriesRequired.rdfs#\">\n\r");
			out.write("<cr:CategoriesRequired rdf:ID=\""+prof.getId()+"\">\n\r"); // Nombre de la busqueda/identificador
			out.write("<cr:hasCategorieDetails>\n");
			out.write("<cr:CategorieDetails>\n\r");
			if (prof.getSkills().size() == prof.getCompetence().size()) {
				for (int i = 0; i < prof.getSkills().size(); i++) {
					 out.write("<cr:requiredCompetence>\n");
					 out.write("<skill:"+prof.getSkills().get(i).replace(" ", "_")+">\n"); 
					 out.write("<skill:competenceLevel rdf:resource=\"http://kmm.lboro.ac.uk/ecos/1.0#"+prof.getCompetence().get(i)+"\"/>\n");
					 out.write("</skill:"+prof.getSkills().get(i).replace(" ", "_")+">\n");
					 out.write("</cr:requiredCompetence>\n\r"); 
				}
			}
			out.write("</cr:CategorieDetails>\n\r");
			out.write("</cr:hasCategorieDetails>\n\r");
			out.write("</cr:CategoriesRequired>\n");   
			out.write("</rdf:RDF>");
			out.close();  
		} catch (Exception e) {
			log.error("Error a crear el archivo auxiliar de oferta", e);
        }
        log.info("Creado archivo auxiliar de la oferta");
	}
}
