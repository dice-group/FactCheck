package wrapper.preprocessing;

//import net.didion.jwnl.data.Exc;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;

import java.io.ByteArrayInputStream;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class FCpreprocessor {
    private String data;
    private DefactoModel defactoModel;
    private Model modelFC;
    private Model modelISWC;

    public FCpreprocessor(String data, String taskId) {
        this.data = data;
        Model modelISWC = createModel(data);
        setModelISWC(modelISWC);
        init(modelISWC, taskId);
    }

    public String getData() {
        return this.data;
    }

    private void setModelISWC(Model model) {
        this.modelISWC = model;
    }

    private void setModelFC(Model model) {
        this.modelFC = model;
    }

    private Model createModel(String data) {
        try {
            System.out.println("Create Model");
            Model model = ModelFactory.createDefaultModel();
            System.out.println("Creating Model Done");
            model.read(new ByteArrayInputStream(data.getBytes()), null, "TTL");
            return model;
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    private Resource getResource(Model model, String propertyUri) {
        Property property = ResourceFactory.createProperty(propertyUri);
        NodeIterator nodeIterator = model.listObjectsOfProperty(property);
        if (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            return  rdfNode.asResource();
        }
        return null;
    }

    private Literal getLabel(Model model, Resource resource) {
        Property labelProperty = ResourceFactory.createProperty(Constants.RDF_SCHEMA_NAMESPACE + "label");
        NodeIterator nodeIterator = model.listObjectsOfProperty(resource, labelProperty);
        if (nodeIterator.hasNext()) {
            RDFNode rdfNode = nodeIterator.nextNode();
            return rdfNode.asLiteral();
        }
        return null;
    }

    private void setDefactoModel(Model model, DefactoResource subject, DefactoResource object, RDFNode pred, String taskId) {
        Property predicate = ResourceFactory.createProperty(pred.toString());
        this.defactoModel = new DefactoModel(model, subject, object, predicate, taskId);
    }

    public DefactoModel getDefactoModel() {
        return this.defactoModel;
    }

    public Model getModelISWC() {
        return this.modelISWC;
    }

    public Model getModelFC() {
        return this.modelFC;
    }

    private DefactoResource setDefactoResource (Model modelISWC, Model modelFC, Resource resourceNode) {
        DefactoResource defactoResource = new DefactoResource(resourceNode, modelFC, resourceNode.getURI());
        Literal literal = getLabel(modelISWC, resourceNode);
        defactoResource.labels.put(literal.getLanguage(), literal.getLexicalForm());

        return defactoResource;
    }

    private void init(Model modelISWC, String taskId) {
        Resource subNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "subject");
        RDFNode predNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "predicate");
        Resource objNode = getResource(modelISWC, Constants.RDF_SYNTAX_NAMESPACE + "object");

        String dataFC = String.format("<%s> <%s> <%s> .", subNode, predNode, objNode);
        Model model = createModel(dataFC);
        setModelFC(model);

        DefactoResource subject = setDefactoResource(modelISWC, model, subNode);
        DefactoResource object = setDefactoResource(modelISWC, model, objNode);

        setDefactoModel(model, subject, object, predNode, taskId);
    }
}
