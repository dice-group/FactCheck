package api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
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

    /*@PostMapping("/hobbitTask/")
    public FactCheckHobbitResponse execHobbitTask(
            @RequestBody FCpreprocessor fCpreprocessor,
            @RequestBody String taskId) {
        System.out.println("Inside hobbit task");
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace());
    }*/

    @PostMapping("/hobbitTask/")
    public FactCheckHobbitResponse execHobbitTask(
            @RequestParam("fCpreprocessor") FCpreprocessor fCpreprocessor,
            @RequestParam("taskId") String taskId) {
//        FCpreprocessor fCpreprocessor = SerializationUtils.deserialize(data);
        System.out.println("Inside hobbit task");
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace());
    }

    @PostMapping("/hobbitTask/{taskId}")
    public FactCheckHobbitResponse execHobbitTask(
            @PathVariable(value = "taskId") String taskId,
            @RequestParam("data") byte[] data) {
        FCpreprocessor fCpreprocessor = SerializationUtils.deserialize(data);
        System.out.println("Inside hobbit task");
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace());
    }

    @RequestMapping(value = "/benchmarkTask/{taskId}", method = RequestMethod.POST)
    @ResponseBody
    public FactCheckHobbitResponse benchmarkTask(
            @PathVariable("taskId") String taskId,
            @RequestBody FCpreprocessor fCpreprocessor) {
        System.out.println("Inside hobbit task");
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace());
    }

    @RequestMapping(value="/execTask/{taskId}", method= RequestMethod.POST)
    public FactcheckResponse execT(@PathVariable(value = "taskId") String taskId,
                                   @RequestParam(value = "data", required = true) byte[] data) {
        System.out.println("inside api function");
        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskId, data);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        FactcheckResponse fcApi = new FactcheckResponse(taskId, new String(data));
        fcApi.setDefactoScore(defactoScore);

        return fcApi;
    }

}
