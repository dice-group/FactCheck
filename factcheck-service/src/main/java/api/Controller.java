package api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.apache.jena.rdf.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import rdf.RDFResource;
import rdf.TripleExtractor;
import wrapper.ModelTransform;
import wrapper.preprocessing.FCpreprocessor;


@RestController
@RequestMapping("/api")
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final Set<String> validPredicates = new HashSet<String>();

    public Controller() {
        validPredicates.add("http://dbpedia.org/ontology/award");
        validPredicates.add("http://dbpedia.org/ontology/birthPlace");
        validPredicates.add("http://dbpedia.org/ontology/deathPlace");
        validPredicates.add("http://dbpedia.org/ontology/foundationPlace");
        validPredicates.add("http://dbpedia.org/ontology/leader");
        validPredicates.add("http://dbpedia.org/ontology/publicationDate");
        validPredicates.add("http://dbpedia.org/ontology/spouse");
        validPredicates.add("http://dbpedia.org/ontology/starring");
        validPredicates.add("http://dbpedia.org/ontology/subsidiary");
    }

    // To verify status of server
    @RequestMapping("/default")
    public String defaultpage() {
        return "ok!";
    }

    @GetMapping("/factBechTest")
    public void fbt() throws FileNotFoundException {
        String factBenchPath = "/home/umair/Desktop/factcheck/datasets/factbench/factbench";

        factBenchTest d = new factBenchTest();
        try {
            d.checkFacts(factBenchPath);
        }catch (Exception exp){
            System.out.println(exp);
        }
    }

    @PostMapping("/factBechTestOneFile")
    public void factBechTestOneFile(@RequestBody String singlefile) throws FileNotFoundException {
//        String singleFile = "/home/farshad/Downloads/subsidiary_00007.ttl";
//        String singleFile = "/home/farshad/repos/factBench/factbench/train/wrong/date/subsidiary/subsidiary_00002.ttl";
        factBenchTest d = new factBenchTest();
        try {
            d.checkOneFile(singlefile);
        }catch (Exception exp){
            System.out.println(exp);
        }
    }

    @GetMapping("/checkfact")
    public ResponseEntity<FactcheckResponse> checkFact (@RequestParam(value = "subject", required = true) String subject,
                                                        @RequestParam(value = "predicate", required = true) String predicate,
                                                        @RequestParam(value = "object", required = true) String object,
                                                        @RequestParam(value = "subjectLabel", defaultValue = "") String subjectLabel,
                                                        @RequestParam(value = "objectLabel", defaultValue = "") String objectLabel){



        FactcheckResponse response = new FactcheckResponse();
        Integer taskId = generateRandomIntInRange(1,1000);
        response.taskid = taskId.toString();

        if(!isValidPredicate(predicate)){
            response.filedata="The Predicate is Not Acceptable ==> "+ predicate;
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        RDFResource subjectResource;
        Property predicatePropery;
        RDFResource objectResource;

        if(subjectLabel.equals("")){
            String[] tmp = subject.split("/");
            subjectLabel = tmp[tmp.length-1].replace("_"," ").replace("-"," ");
        }

        if(objectLabel.equals("")){
            String[] tmp = object.split("/");
            objectLabel = tmp[tmp.length-1].replace("_"," ").replace("-"," ");
        }


        FCpreprocessor fCpreprocessor = new FCpreprocessor(setSimplifiedData(subject,predicate,object,subjectLabel,objectLabel), taskId.toString());
        Evidence evidence = getEvidence(fCpreprocessor, taskId.toString());

        // Setting defacto score received from response
        double defactoScore = evidence.getDeFactoScore();
        logger.info("Score {} returned for task {}", defactoScore, taskId);
        response.setDefactoScore(defactoScore);

        //Setting proof sentences
        response.setComplexProofs(setProofSentences(evidence));

        // returning input subject, predicate and object
        response.subject = subject;
        response.object = object;
        response.predicate = predicate;

        return ResponseEntity.ok(response);
    }

    private boolean isValidPredicate(String predicate) {
        if(validPredicates.contains(predicate)){
            return true;
        }
        return true;
    }




    public static Integer generateRandomIntInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private String setSimplifiedData(String subject,String predicate, String object ,  String subjectLabel , String objectLabel) {

        String simplifiedData = String.format("<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n" +
                        "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <%s> .\n" +
                        "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <%s> .\n" +
                        "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> "
                , subject
                , predicate);

            simplifiedData = simplifiedData + String.format("<%s> .\n", object);

            String[] ss = subject.split("/");
            String[] oo = object.split("/");


        String labels = String.format("<%s> <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@en .\n" +
                        "<%s> <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@en .\n"
                , subject, subjectLabel
                , object, objectLabel);
        return simplifiedData + labels;
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
            complexProofs.add(new ComplexProofs(p.getWebSite().getUrl(), p.getProofPhrase(),p.getWebSite().getScore()));
        });
        return complexProofs;
    }
}