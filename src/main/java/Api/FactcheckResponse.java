package Api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class FactcheckResponse implements Serializable {
    public String taskid;
    public String filedata;
    public double defactoScore;

    public FactcheckResponse() {
        super();
    }

    public FactcheckResponse(String taskid, String filedata) {

        this.taskid = taskid;
        this.filedata = filedata;
        this.defactoScore = 0;
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
}
