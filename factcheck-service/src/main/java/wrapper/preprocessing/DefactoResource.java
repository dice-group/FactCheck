package wrapper.preprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DANISH AHMED on 4/15/2018
 */
class DefactoResource {
    private Resource resource;
    private Model model;
    public Map<String, String> labels = new HashMap<String, String>();
    private String uri;

    DefactoResource(Resource resource, Model model, String uri){
        this.resource = resource;
        this.model = model;
        this.uri = uri;
    }

    Resource getResource() {
        return this.resource;
    }

    Map<String, String> getLabels() {
        return this.labels;
    }
}
