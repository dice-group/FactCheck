package api;

import java.io.IOException;
import java.util.ArrayList;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rdf.TripleExtractor;
import wrapper.ModelTransform;
import wrapper.preprocessing.FCpreprocessor;


@RestController
@RequestMapping("/api")
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    // To verify status of server
    @RequestMapping("/default")
    public String defaultpage() {
        return "Welcome to FactCheck";
    }

    // Receives the POST request with /api/execTask/ as extension
    @PostMapping("/execTask/")
    public FactcheckResponse execT(@RequestBody FactcheckResponse factcheckResponse) throws IOException {

        logger.info("Task {} received with data: {}", factcheckResponse.getTaskid(), factcheckResponse.getFile());

        String taskId = factcheckResponse.getTaskid();
        String fileData = factcheckResponse.getFile();

        logger.info("Extracting data using TripleExtractor");
        TripleExtractor tripleExtractor = new TripleExtractor(fileData, false);

        FCpreprocessor fCpreprocessor = new FCpreprocessor(tripleExtractor.getSimplifiedData(), taskId);
        Evidence evidence = getEvidence(fCpreprocessor, taskId);

        // Setting defacto score received from response
        double defactoScore = evidence.getDeFactoScore();
        logger.info("Score {} returned for task {}", defactoScore, taskId);
        factcheckResponse.setDefactoScore(defactoScore);

        //Setting proof sentences
        factcheckResponse.setComplexProofs(setProofSentences(evidence));

        // returning input subject, predicate and object
        factcheckResponse.subject = tripleExtractor.getSubject();
        factcheckResponse.object = tripleExtractor.getObject();
        String predicate = tripleExtractor.getPredicateUri();
        String[] p = predicate.split("/");
        factcheckResponse.predicate = p[p.length - 1];

        return factcheckResponse;
    }

    @RequestMapping(value = "/hobbitTask/", method = RequestMethod.POST)
    public FactCheckHobbitResponse execT(@RequestParam(value = "taskId") String taskId,
                                         @RequestParam(value = "dataISWC", required = true) String dataISWC) {

        logger.info("Received HOBBIT Task {}", taskId);

        FCpreprocessor fCpreprocessor = new FCpreprocessor(dataISWC, taskId);
        Evidence factEvidence = getEvidence(fCpreprocessor, taskId);

        logger.info("Score {} returned for task {}", factEvidence.getDeFactoScore(), taskId);

        return new FactCheckHobbitResponse(taskId, factEvidence.getDeFactoScore());
    }

    //Return evidence object for task specified
    private Evidence getEvidence(FCpreprocessor preprocessor, String taskId) {

        DefactoModel defactoModel = new ModelTransform(preprocessor, taskId).getDefactoModel();
        defactoModel.setCorenlpClient(ApplicationStartup.corenlpClient);

        Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        return evidence;
    }

    private ArrayList<ComplexProofs> setProofSentences(Evidence evidence) {
        ArrayList<ComplexProofs> complexProofs = new ArrayList<>();
        evidence.getComplexProofs().forEach(p -> {
            complexProofs.add(new ComplexProofs(p.getWebSite().getUrl(), p.getProofPhrase()));
        });
        return complexProofs;
    }
}