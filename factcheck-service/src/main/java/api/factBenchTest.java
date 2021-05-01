package api;

import io.reactivex.rxjava3.core.Single;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.io.FilenameUtils;
import rdf.TripleExtractor;
import wrapper.ModelTransform;
import wrapper.preprocessing.FCpreprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class factBenchTest {

    String ProgressFileName = "/home/umair/Desktop/factcheck/datasets/factbench/factbench/prf.txt";
    String ProgressTextResults = "/home/umair/Desktop/factcheck/datasets/factbench/factbench/textResults.txt";
    String factBenchPath = "/home/umair/Desktop/factcheck/datasets/factbench/factbench";
    HashMap<String,String> progress = new HashMap<>();
    int TaskID = 0;

    public factBenchTest() throws FileNotFoundException {
        //if there is pr file
        File prf = new File(ProgressFileName);
        if(prf.exists()){
            try {
                FileInputStream fileIn = new FileInputStream(ProgressFileName);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                progress = (HashMap) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
            } catch (ClassNotFoundException c) {
                System.out.println("Employee class not found");
                c.printStackTrace();
            }
        }
    }

/*    public static void main(String[] args) {


        factBechTest d = new factBechTest();
        try {
            d.checkFacts(factBenchPath);
        }catch (Exception exp){
            System.out.println(exp);
        }
    }*/

    public void checkFacts(String path) throws IOException {

        File dir = new File(path);
        File[] files = dir.listFiles();

        for (File file : files) {
            if(file.isFile()){
                System.out.println(FilenameUtils.getExtension(String.valueOf(file)));
                if(FilenameUtils.getExtension(String.valueOf(file)).equals("ttl")){
                    System.out.println("Check This File"+file.getPath());

                    // check if it is checked before 
                    if(progress.containsKey(file.getPath())){
                        continue;
                    }
                    
                    //check fact
                    String result = checkFactFromFile(file, Integer.toString(TaskID));
                    TaskID = TaskID+1;
                   // add Progress
                    progress.put(file.getPath(),result);
                   // write to file
                    String FileName = "result_"+file.getName();
                    try (PrintWriter out = new PrintWriter(file.getParent()+"/"+FileName+"r")) {
                        out.println(result);
                    }
                    // update progress
                    updateProgress();

                    try {


                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(ProgressTextResults, true)));
                        out.println(TaskID);
                        out.println(file.toPath());
                        out.println(result);
                        out.println("-=-=-=-=-=-=-==-=-==-=-=-=-==-=-=-=-=-=-=-==-=-=-=-");
                        out.close();
                    }catch (IOException e) {
                        //exception handling left as an exercise for the reader
                    }


                }
            }
            if(file.isDirectory()){
                String newPath = path +"/"+file.getName();
                checkFacts(newPath);
            }
        }
    }

    private void updateProgress() {
        FileOutputStream fileOut = null;
        try {
            fileOut =
                     new FileOutputStream(ProgressFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            System.out.println("updatePr");
            out.writeObject(progress);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in"+ ProgressFileName+ "progress size is :"+progress.size());
        } catch (IOException i) {
            i.printStackTrace();
        }finally {
            System.out.println("Any body here ...");
            try {
                if (fileOut != null) {
                    System.out.println("CloseConn");
                    fileOut.close();
                    System.out.println("CloseConnDone");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private String checkFactFromFile(File input,String taskId) throws IOException {
        
        String fileData = ReadFile(input);
        
        TripleExtractor tripleExtractor = new TripleExtractor(fileData, false);
        FCpreprocessor fCpreprocessor = new FCpreprocessor(tripleExtractor.getSimplifiedData(), taskId);
        Evidence evidence = getEvidence(fCpreprocessor, taskId);

        // Setting defacto score received from response
        double defactoScore = evidence.getDeFactoScore();

        String Result ="";
        Result+= " defactoScore: " +defactoScore;

        //Setting proof sentences
        Result+= " setProofSentences : "+ setProofSentences(evidence);

        // returning input subject, predicate and object
        Result+= " subject : "+  tripleExtractor.getSubject();
        Result+= " object : "+ tripleExtractor.getObject();
        String predicate = tripleExtractor.getPredicateUri();
        String[] p = predicate.split("/");
        Result+= " predicate "+ p[p.length - 1];
        
        return Result;
    }

    private String ReadFile(File input) throws FileNotFoundException {
        Scanner myReader = new Scanner(input);
        StringBuilder sb = new StringBuilder();
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            sb.append(data);
        }
        myReader.close();
        return sb.toString();
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
