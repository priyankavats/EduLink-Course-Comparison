package servlets;


import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class SearchServlet
 */
public class SearchServlet extends HttpServlet {
	Model _model = null;
	Model schema = null;
	private static final long serialVersionUID = 1L;

	String[] url = {"http://cdhekne.github.io/khan_acad.ttl", "http://cdhekne.github.io/tuts_video.ttl", "http://cdhekne.github.io/tuts_tutorials.ttl", "http://cdhekne.github.io/ocw.ttl"};

	public void loadData(String url){
		_model = ModelFactory.createOntologyModel();
		try{
			_model.read(url,"TURTLE");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String searchParameter,searchBy;

		searchBy = request.getParameter("searchBy");
		if(searchBy.equals("price")==true)
			searchParameter="free";
		else
			searchParameter = request.getParameter("searchBox");


		if(searchParameter==null) {
			// Send back to home page
			try {
				response.sendRedirect("index.html");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		else{
			String jsonOutput="";
			JsonObject json = new JsonObject();
			FileWriter fw= new FileWriter("C:\\Users\\Chinmay\\git\\semWebProj\\Semantic_Project\\Files\\outputJson.json");
			for(int i=0;i<url.length;i++){
				loadData(url[i]);
				if(searchBy.equals("price")==true){
					fw.write(runQueryForFreeCourses(searchBy,searchParameter,_model));
					jsonOutput= runQueryForFreeCourses(searchBy,searchParameter,_model);
				}
				else{
					fw.write(basicRun(searchBy,searchParameter,_model));
					jsonOutput = basicRun(searchBy,searchParameter,_model);
				}
			}
			fw.close();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(new Gson().toJson(jsonOutput)); 
			System.out.println("Done");
			
		}
		/*		else {
			FileWriter fw= new FileWriter("C:\\Users\\Chinmay\\git\\semWebProj\\Semantic_Project\\Files\\outputJson.json");

			for(int i=0;i<url.length;i++){
				String db = "http://demo.openlinksw.com/sparql";
				OntModel m = ModelFactory.createOntologyModel();
//				m.removeAll();
				m.read(url[i], "TURTLE");

				String queryString = "PREFIX edu: <http://www.semanticweb.org/cdhekne/ontologies/2015/10/untitled-ontology-8#>\n" +
						"SELECT DISTINCT ?name ?courseProvider ?courseLink ?desc\n" +
						"WHERE {"+
						"?course edu:courseName ?name ; edu:courseProvider ?courseProvider ; edu:courseLink ?courseLink ; "
						+ "edu:courseDescription ?desc.\n" +
						"FILTER regex(?name , \""+searchParameter+"\", \"i\")\n" +
						"}";
				//				System.out.println(queryString);
				Query query = QueryFactory.create(queryString) ;
				QueryExecution qexec = QueryExecutionFactory.sparqlService(db, query);
				ResultSet rs = qexec.execSelect();
				while(rs.hasNext()){
					HashMap<String, String> j = new HashMap<String, String>();
					Gson gson = new Gson();
					QuerySolution qs = rs.nextSolution();
					Literal name = qs.getLiteral("name");
					Literal courseProvider = qs.getLiteral("courseProvider");
					Literal courseLink = qs.getLiteral("courseLink");
					Literal desc = qs.getLiteral("desc");
					j.put("name", name.toString());
					j.put("courseProvider", courseProvider.toString());
					j.put("courseLink", courseLink.toString());
					j.put("desc", desc.toString());
					String json = gson.toJson(j);
					System.out.println(json+"\n");
					try {
						fw.write(json+"\n");

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					System.out.println(name + "\n" + courseLink + "\n" + courseProvider + "\n" + desc +"\n\n");
					//					System.out.println(qs.get("courseProvider"));
					//					System.out.println(qs.get("courseLink"));
					//					System.out.println(qs.get("desc"));
				}
				db=null;
				rs=null;
				qexec.close();
				m.close();
			}
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done");
			return;
		}*/
	}

	private String basicRun(String searchBy,String searchParameter,Model _model) {
		if(searchBy.equals("price")==true)
			return runQueryForFreeCourses(searchBy,searchParameter,_model);
		else
			return runQuery(searchBy,searchParameter,_model);
	}
	private String runQueryForFreeCourses(String searchBy, String searchParameter, Model _model2) {
		String queryString="PREFIX edu: <http://www.semanticweb.org/cdhekne/ontologies/2015/10/untitled-ontology-8#>"+

"SELECT ?name ?courseProvider ?courseLink ?desc ?duration ?price ?type ?tname"+

"WHERE {"+
"?course edu:coursePricing ?price."+
"?course edu:courseName ?name."+

  "?course edu:courseProvider ?courseProvider."+

  "?course edu:courseLink ?courseLink."+

  "?course edu:courseDescription ?desc."+

  "OPTIONAL{"+
  "?course edu:courseDuration ?duration."+

    "?course edu:courseType ?type."+
    "?course edu:teacherName ?tname."+
    "}"+

  "FILTER regex(?"+searchBy+" , \""+searchParameter+"\", \"i\")"+
  "}";
		String json="";
		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExecution = QueryExecutionFactory.create(query,_model2);
		try{
			ResultSet response = queryExecution.execSelect();

			while( response.hasNext())
			{
				HashMap<String, String> j = new HashMap<String, String>();
				Gson gson = new Gson();
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				RDFNode courseProvider = soln.get("?courseProvider");
				RDFNode courseLink = soln.get("?courseLink");
				RDFNode desc = soln.get("?desc");
				j.put("name", name.toString());
				j.put("courseProvider", courseProvider.toString());
				j.put("courseLink", courseLink.toString());
				j.put("desc", desc.toString());
				json += "\n"+gson.toJson(j);
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		queryExecution.close();
		return json;

	}
	private String runQuery(String searchBy,String searchParameter, Model _model2) {
		String queryString = "PREFIX edu: <http://www.semanticweb.org/cdhekne/ontologies/2015/10/untitled-ontology-8#>\n" +
				"SELECT ?name ?courseProvider ?courseLink ?desc ?price ?type ?teacherName ?categoryName ?topicName ?duration \n" +
				"WHERE {"+
				"?course edu:courseName ?name ; edu:courseProvider ?courseProvider ; edu:courseLink ?courseLink ; \n"
				+ "edu:courseDescription ?desc.\n" +
				"OPTIONAL{?course edu:coursePricing ?price.}"+
				"OPTIONAL{  ?teacher edu:teaches ?course ; edu:teacherName ?teacherName .}"+
				"OPTIONAL{  ?category edu:includesCourse ?course ; edu:categoryName ?categoryName. }"+
				"OPTIONAL{  ?syllabus edu:belongsToCourse ?course ; edu:topics ?topicName. }  "+
				"OPTIONAL{ ?course edu:courseType ?type.}"+
				"OPTIONAL{ ?course edu:courseDuration ?duration.}"+
				"FILTER regex(?"+searchBy+" ,\""+searchParameter+"\", \"i\")\n" +
				"}";
		String json="";
		Query query = QueryFactory.create(queryString.toString());
		QueryExecution queryExecution = QueryExecutionFactory.create(query,_model2);
		try{
			ResultSet response = queryExecution.execSelect();

			while( response.hasNext())
			{
				HashMap<String, String> j = new HashMap<String, String>();
				Gson gson = new Gson();
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				RDFNode courseProvider = soln.get("?courseProvider");
				RDFNode courseLink = soln.get("?courseLink");
				RDFNode desc = soln.get("?desc");
				
				RDFNode courseDuration = soln.get("?duration");
				RDFNode courseType = soln.get("?type");
				RDFNode teacherName = soln.get("?tname");
				RDFNode price = soln.get("?price");
				RDFNode topicName = soln.get("?topicName");
				RDFNode categoryName = soln.get("?categoryName");
				
				j.put("name", name.toString());
				j.put("courseProvider", courseProvider.toString());
				j.put("courseLink", courseLink.toString());
				j.put("desc", desc.toString());
				
				
				if(courseDuration==null)
					j.put("duration", "-");
				else
					j.put("duration", courseDuration.toString());
				if(courseType==null)
					j.put("type", "-");
				else
					j.put("type", courseType.toString());
				if(teacherName==null)
					j.put("tname", "-");
				else
					j.put("tname", teacherName.toString());
				if(price==null)
					j.put("price", "-");
				else
					j.put("price", price.toString());
				if(topicName==null)
					j.put("topicName", "-");
				else
					j.put("topicName", topicName.toString());
				if(categoryName==null)
					j.put("categoryName", "-");
				else
					j.put("categoryName", categoryName.toString());

				json += "\n"+gson.toJson(j);
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		queryExecution.close();
		return json;
	}
}
