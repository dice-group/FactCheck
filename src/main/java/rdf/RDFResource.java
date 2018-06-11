package rdf;

import com.hp.hpl.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DANISH AHMED on 3/22/2018
 */
public class RDFResource {

    protected Resource resource;
    protected Model model;
    protected String label;
    public String uri;

    private Property owlSameAsProperty = ResourceFactory.createProperty(Constants.OWL_NAMESPACE + "sameAs");

    public List<Resource> owlSameAsList = new ArrayList<Resource>();     // only considering english for now
    public Map<String, String> langLabelsMap = new HashMap<String, String>();

    /**
     * give me FactCheck resource model
     * @param resource given jena resource
     * @param model jena model
     */
    RDFResource(Resource resource, Model model) {
        this.resource = resource;
        this.uri = resource.getURI();
        this.model = model;

        // set labels w.r.t language
        Property labelProperty = ResourceFactory.createProperty(Constants.RDF_SCHEMA_NAMESPACE + "label");
        NodeIterator labelNodeIterator = this.model.listObjectsOfProperty(this.resource, labelProperty);

        // set sameAs resource list
        getResourceLabel(labelNodeIterator);
        setOwlSameAsList();
    }

    private void getResourceLabel(NodeIterator nodeIterator) {
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            String lang = rdfNode.asLiteral().getLanguage();
            String label = rdfNode.asLiteral().getLexicalForm();

            langLabelsMap.put(lang, label);
            this.label = label;
        }
    }

    /**
     * get sameAs owl property
     */
    private void setOwlSameAsList() {
        NodeIterator nodeIterator = model.listObjectsOfProperty(this.resource, this.owlSameAsProperty);
        while (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            owlSameAsList.add(rdfNode.asResource());
        }
    }

    /**
     * filter out only dbpedia resource that is not inter-lang
     * @param resource RDFResource
     * @return uri of resource
     */
    public static String getDBpediaUri(RDFResource resource) {
        String uri = resource.uri;
        if (uri.contains(Constants.DBPEDIA_URI))
            return uri;

        List<Resource> subjectSameAsList = resource.owlSameAsList;
        String subjectUri = "";

        for (Resource rsc : subjectSameAsList) {
            if (rsc.toString().contains(Constants.DBPEDIA_URI)) {
                subjectUri = rsc.toString();
            }
        }
        return subjectUri;
    }
}
