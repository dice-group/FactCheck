/**
 * 
 */
package org.aksw.defacto.evaluation;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class DBpediaQuery {

	public void main(String[] args) {
		Dataset dataset = TDBFactory.createDataset("/Users/gerb/Development/workspaces/experimental/dbpedia/store");
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
		
		String queryString =
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  " +
				"select count(distinct ?s) { ?s rdfs:label ?label . FILTER(lang(?label) = \"fr\") }";
		
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		
		
		ResultSet result = QueryExecutionFactory.create(query, dbpedia).execSelect();

		while ( result.hasNext() ) {
			
			QuerySolution solution = result.next();
			
			System.out.println(solution);
		}
	}
}
