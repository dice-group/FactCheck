package Wrapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoResource;
import preprocessing.FCpreprocessor;

/**
 * @author DANISH AHMED on 4/20/2018
 */
public class ModelTransform {
    private DefactoModel defactoModel;
    public ModelTransform(FCpreprocessor fCpreprocessor, String taskId) {
        this.defactoModel = setDefactoModel(fCpreprocessor, taskId);
    }

    private DefactoModel setDefactoModel(FCpreprocessor fCpreprocessor, String taskId) {
        preprocessing.DefactoModel preProcessingDM = fCpreprocessor.getDefactoModel();

        DefactoResource defactoResourceSubject = new DefactoResource((Resource) preProcessingDM.getSubjectResource(), (Model) fCpreprocessor.getModelFC());
        defactoResourceSubject.labels = preProcessingDM.getSubjectLabels();
        DefactoResource defactoResourceObject = new DefactoResource((Resource) preProcessingDM.getObjectResource(), (Model) fCpreprocessor.getModelFC());
        defactoResourceObject.labels = preProcessingDM.getObjectLabels();

        DefactoModel defactoModel = new DefactoModel((Model) fCpreprocessor.getModelFC(), taskId, true, preProcessingDM.languages);
        defactoModel.object = defactoResourceObject;
        defactoModel.subject = defactoResourceSubject;
        defactoModel.predicate = (Property) preProcessingDM.predicate;

        return defactoModel;
    }

    public DefactoModel getDefactoModel() {
        return defactoModel;
    }
}
