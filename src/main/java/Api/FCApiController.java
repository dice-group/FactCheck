package Api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import FCWrapper.FactCheckBytes;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class FCApiController {

    // To verify status of server
    @RequestMapping("/hi")
    public String hi() {
        return "Hello World from Restful API";
    }

    // Receives the POST request with /api/execTask/ as extension
    @PostMapping("/execTask/")
    public FCApi execT(@RequestBody FCApi fcApi) throws IOException {

       System.out.println(fcApi.getTaskid());
        String taskid =fcApi.getTaskid();
        System.out.println(fcApi.getFile());

        String filedata =fcApi.getFile();
        // Conversion of data in string format to byte array
        byte[] databyte = filedata.getBytes(StandardCharsets.UTF_8);

        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskid, databyte);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        // Setting defacto score received from response
        fcApi.setDefactoScore(defactoScore);

        return fcApi;

    }

}
