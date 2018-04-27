package wrapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FactCheckBytes {
    public static Map<DefactoModel, Evidence> FactCheckFromBytes(String taskId, byte[] data) {
        String lang = "en";

        List<DefactoModel> models = new ArrayList<>();
        models.add(bytesToModel(data, taskId, lang));   // taskId = modelName

        return Defacto.checkFacts(models, Defacto.TIME_DISTRIBUTION_ONLY.NO);
    }

    private static DefactoModel bytesToModel(byte[] data, String modelName, String lang) {
        final Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(data), null, "TURTLE");

        return new DefactoModel(model, modelName, true, Arrays.asList(lang));
    }
}
