package Api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class FCApi implements Serializable {
    public String taskid;
    public String filedata; //byte[]
    public double defactoScore;

    public FCApi() {
        super();
    }

    public FCApi(String taskid, String filedata) {//byte[]

        this.taskid = taskid;
        this.filedata = filedata;
        this.defactoScore = 0;
    }

//    @JsonCreator
//    public FCApi(@JsonProperty("taskId") String taskId, @JsonProperty("data") byte[] data,
//                 @JsonProperty("defactoScore")  double defactoScore) {
//        this.taskId = taskId;
//        this.data = data;
//        this.defactoScore = defactoScore;
//    }


    public void setDefactoScore(double defactoScore) {
        this.defactoScore = defactoScore;
    }

    public String getTaskid() { return taskid; }

    public String getFile() {
        return filedata;
    } //byte[]

    public double getDefactoScore() {
        return defactoScore;
    }
}
