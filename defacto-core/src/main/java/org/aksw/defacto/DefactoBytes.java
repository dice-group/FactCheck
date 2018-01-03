package org.aksw.defacto;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.defacto.model.DefactoModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefactoBytes {
    public static byte[] fileToBytes(String filePath) throws IOException {
        Path fileLocation = Paths.get(filePath);
        return Files.readAllBytes(fileLocation);
    }

    public static void FactCheckFromBytes(String taskId, byte[] data) {
        String modelName = taskId;
        String lang = "en";

        List<DefactoModel> models = new ArrayList<>();
        models.add(bytesToModel(data, modelName, lang));

        Defacto.checkFacts(models, Defacto.TIME_DISTRIBUTION_ONLY.NO);
    }

    public static DefactoModel bytesToModel(byte[] data, String modelName, String lang) {
        final Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(data), null, "TURTLE");

        return new DefactoModel(model, modelName, true, Arrays.asList(lang));
    }
}
