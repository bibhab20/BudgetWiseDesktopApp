package model;

import lombok.Getter;

@Getter
public class CliSummary {
    String message;
    Status status;

    public CliSummary(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public enum Status {
        PASS,
        FAIL
    }

    public void appendMessage(String message) {
        this.message = this.message +"\n" + message;
    }

}
