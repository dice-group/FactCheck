package api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import wrapper.FactCheckBytes;
import wrapper.ModelTransform;
import org.springframework.web.bind.annotation.*;
import wrapper.preprocessing.FCpreprocessor;


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

        //Setting proof sentences
        factcheckResponse.setComplexProofs(setProofSentences(evidence));

        // Setting defacto score received from response
        factcheckResponse.setDefactoScore(defactoScore);

        return factcheckResponse;
    }



    @RequestMapping(value="/hobbitTask/{taskId}", method= RequestMethod.POST)
    public FactCheckHobbitResponse execT(@PathVariable(value = "taskId") String taskId,
                                         @RequestParam(value = "dataISWC", required = true) String dataISWC,
                                         @RequestParam(value = "fileTrace", required = true) String fileTrace) {

        System.out.println("called by hobbit");

        FCpreprocessor fCpreprocessor = new FCpreprocessor(dataISWC, taskId, fileTrace);
        DefactoModel defactoModel = new ModelTransform(fCpreprocessor, taskId).getDefactoModel();
        Evidence factEvidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        double defactoScore = factEvidence.getDeFactoScore();

        return new FactCheckHobbitResponse(taskId, defactoScore, fCpreprocessor.getFileTrace(), setProofSentences(factEvidence));
    }



    private ArrayList<ComplexProofs> setProofSentences(Evidence evidence) {
        ArrayList<ComplexProofs> complexProofs = new ArrayList<>();
        evidence.getComplexProofs().forEach( p -> {
            complexProofs.add(new ComplexProofs(p.getWebSite().getUrl(), p.getProofPhrase()));
        });
        return complexProofs;
    }
}
