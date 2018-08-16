package api;

import java.io.Serializable;
import java.util.ArrayList;

public class FactcheckResponse implements Serializable {
    public String taskid;
    public String filedata;
    public double defactoScore;
    public ArrayList<ComplexProofs> complexProofs;
    public String subject;
    public String predicate;
    public String object;
    public FactcheckResponse() {
        super();
    }

    public FactcheckResponse(String taskid, String filedata) {

        this.taskid = taskid;
        this.filedata = filedata;
        this.defactoScore = 0;
        this.complexProofs = new ArrayList<>();
    }


    public void setDefactoScore(double defactoScore) {
        this.defactoScore = defactoScore;
    }

    public String getTaskid() { return taskid; }

    public String getFile() {
        return filedata;
    }

    public double getDefactoScore() {
        return defactoScore;
    }

    public ArrayList<ComplexProofs> getComplexProofs() {
        return complexProofs;
    }

    public void setComplexProofs(ArrayList<ComplexProofs> complexProofs) {
        this.complexProofs = complexProofs;
    }
}
