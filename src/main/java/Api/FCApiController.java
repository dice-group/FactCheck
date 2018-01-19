package Api;

import java.util.Map;

import FCWrapper.FactCheckBytes;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FCApiController {
    private final double threshold = 0.7;

    @RequestMapping(value="/execTask", method= RequestMethod.POST)
    public String taskData(@RequestParam(value = "taskId", required = true) String taskId,
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
    }
}
