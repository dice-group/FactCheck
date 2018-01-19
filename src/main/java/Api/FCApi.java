package Api;

public class FCApi {
    private final String taskId;
    private final byte[] data;

    public FCApi(String taskId, byte[] data) {
        this.taskId = taskId;
        this.data = data;
    }

    public String getTaskId() {
        return taskId;
    }

    public byte[] getData() {
        return data;
    }
}
