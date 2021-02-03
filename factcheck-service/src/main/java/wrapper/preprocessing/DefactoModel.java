package wrapper.preprocessing;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author DANISH AHMED on 4/15/2018
 */
public class DefactoModel {
    public Model model;
    public String name;
    public boolean correct;
    public DefactoResource subject;
    public Property predicate;
    private String predicateUri;
    public DefactoResource object;
    public List<String> languages = new ArrayList<String>();

    public DefactoModel(Model model, DefactoResource subject, DefactoResource object, Property predicate, String name) {
        this.model = model;
        this.subject = subject;
        this.object = object;
        this.name = name;
        this.predicate = predicate;

        this.predicateUri = predicate.getURI();
        this.correct = false;
        languages.add("en");
    }

    public Resource getSubjectResource() {
        return this.subject.getResource();
    }

    public Resource getObjectResource() {
        return this.object.getResource();
    }

    public Map<String, String> getSubjectLabels() {
        return this.subject.getLabels();
    }

    public Map<String, String> getObjectLabels() {
        return this.object.getLabels();
    }
}
