package wrapper;

import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoResource;
import wrapper.preprocessing.FCpreprocessor;

/**
 * @author DANISH AHMED on 4/20/2018
 */
public class ModelTransform {
    private DefactoModel defactoModel;
    public ModelTransform(FCpreprocessor fCpreprocessor, String taskId) {
        this.defactoModel = setDefactoModel(fCpreprocessor, taskId);
    }

    private DefactoModel setDefactoModel(FCpreprocessor fCpreprocessor, String taskId) {
        wrapper.preprocessing.DefactoModel preProcessingDM = fCpreprocessor.getDefactoModel();

        DefactoResource defactoResourceSubject = new DefactoResource(preProcessingDM.getSubjectResource(), fCpreprocessor.getModelFC());
        defactoResourceSubject.labels = preProcessingDM.getSubjectLabels();
        DefactoResource defactoResourceObject = new DefactoResource(preProcessingDM.getObjectResource(), fCpreprocessor.getModelFC());
        defactoResourceObject.labels = preProcessingDM.getObjectLabels();

        DefactoModel defactoModel = new DefactoModel(fCpreprocessor.getModelFC(), taskId, true, preProcessingDM.languages);
        defactoModel.object = defactoResourceObject;
        defactoModel.subject = defactoResourceSubject;
        defactoModel.predicate = preProcessingDM.predicate;

        return defactoModel;
    }

    public DefactoModel getDefactoModel() {
        return this.defactoModel;
    }
}
