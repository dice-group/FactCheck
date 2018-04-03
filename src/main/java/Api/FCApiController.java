package Api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import FCWrapper.FactCheckBytes;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.springframework.web.bind.annotation.*;

import static org.aksw.defacto.DefactoDemo.fileToBytes;

@RestController
@RequestMapping("/api")
public class FCApiController {
    private final double threshold = 0.7;

    @RequestMapping("/api/hi")
    public String hi() {
        return "Hello World from Restful API";
    }

    @PostMapping("/execTask/")

   // @RequestMapping(value="/execTask/", method= RequestMethod.POST)
  //  public FCApi execT(@PathVariable(value = "taskId") String taskId,
   //                    @RequestParam(value = "data", required = true) byte[] data) {

    public FCApi execT(@RequestBody FCApi fcApi) throws IOException {

       System.out.println(fcApi.getTaskid());
        String taskid =fcApi.getTaskid();
        System.out.println(fcApi.getFile());
       // byte[]


        String filedata =fcApi.getFile();

        byte[] databyte = filedata.getBytes(StandardCharsets.UTF_8);

//byte[] myDataBytes = filedata.getBytes(Charset.forName("UTF-8"));
        //fileToBytes("resources/Einstein.ttl");// data.


       // System.out.println(new String(data));
        Map<DefactoModel, Evidence> defactoModelEvidenceMap = FactCheckBytes.FactCheckFromBytes(taskid, databyte);

        Map.Entry<DefactoModel, Evidence> entryIterator = defactoModelEvidenceMap.entrySet().iterator().next();
        Evidence evidence = entryIterator.getValue();
        double defactoScore = evidence.getDeFactoScore();

        //FCApi fcApi = new FCApi(taskId, data);
        fcApi.setDefactoScore(defactoScore);

        return fcApi;

        // do threshold validation here
//        if (defactoScore >= threshold)
//            return defactoScore + " true";
//        return defactoScore + " false";
    }


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
