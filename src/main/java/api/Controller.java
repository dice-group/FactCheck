package api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import wrapper.FactCheckBytes;
import wrapper.ModelTransform;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.*;
import preprocessing.FCpreprocessor;


@RestController
@RequestMapping("/api")
public class Controller {

    // To verify status of server
    @RequestMapping("/default")
    public String defaultpage() {
        return "Welcome to FactCheck";
    }

    // Receives the POST request with /api/execTask/ as extension
    @PostMapping("/execTask/")
    public FactcheckResponse execT(@RequestBody FactcheckResponse factcheckResponse) throws IOException {

       System.out.println(factcheckResponse.getTaskid());
        String taskid =factcheckResponse.getTaskid();
        System.out.println(factcheckResponse.getFile());

        String filedata =factcheckResponse.getFile();
        // Conversion of data in string format to byte array
        byte[] databyte = filedata.getBytes(StandardCharsets.UTF_8);

        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskid, databyte);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        // Setting defacto score received from response
        factcheckResponse.setDefactoScore(defactoScore);

        return factcheckResponse;
    }

    @PostMapping("/hobbitTask/")
    public FactCheckHobbitResponse execHobbitTask(@RequestBody FCpreprocessor fCpreprocessor, String taskId) {
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace());
    }

}
