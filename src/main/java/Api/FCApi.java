package Api;

public class FCApi {
    private final String taskId;
    private final byte[] data;
    private double defactoScore;

    public FCApi(String taskId, byte[] data) {
        this.taskId = taskId;
        this.data = data;
        this.defactoScore = 0;
    }

    public void setDefactoScore(double defactoScore) {
        this.defactoScore = defactoScore;
    }

    public String getTaskId() {
        return taskId;
    }

    public byte[] getData() {
        return data;
    }

    public double getDefactoScore() {
        return defactoScore;
    }
}
