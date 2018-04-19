package Api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import Wrapper.FactCheckBytes;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.*;


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

}
