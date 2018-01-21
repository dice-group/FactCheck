package Api;

import java.util.Map;

import FCWrapper.FactCheckBytes;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FCApiController {
    private final double threshold = 0.7;

    /*@RequestMapping(value="/execTask/{taskId}", method= RequestMethod.POST)
    public String execT(@PathVariable(value = "taskId") String taskId,
                           @RequestParam(value = "data", required = true) byte[] data) {
        System.out.println(new String(data));
        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskId, data);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        // do threshold validation here
        if (defactoScore >= threshold)
            return defactoScore + " true";
        return defactoScore + " false";
    }*/

    @RequestMapping(value="/execTask/{taskId}", method= RequestMethod.POST)
    public FCApi execT(@PathVariable(value = "taskId") String taskId,
                        @RequestParam(value = "data", required = true) byte[] data) {
        System.out.println(new String(data));
        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskId, data);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        FCApi fcApi = new FCApi(taskId, data);
        fcApi.setDefactoScore(defactoScore);

        return fcApi;

        // do threshold validation here
//        if (defactoScore >= threshold)
//            return defactoScore + " true";
//        return defactoScore + " false";
    }

    /*@RequestMapping(value="/task/{taskId}", method= RequestMethod.POST)
    public FCApi task(@PathVariable("taskId") String taskId, @RequestBody FCApi FCApi) {
        System.out.println(new String(FCApi.getData()));
        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskId, FCApi.getData());

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        FCApi.setDefactoScore(defactoScore);

        // do threshold validation here
        if (defactoScore >= threshold)
            return defactoScore + " true";
        return defactoScore + " false";
        return FCApi;
    }*/
}
