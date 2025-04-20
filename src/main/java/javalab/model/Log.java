package javalab.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Log {
    public enum Status {
        IN_PROGRESS,
        SUCCESS,
        FAILURE,
    }

    private Long id;
    private Status status;
    private String date;
    private String body;

    public Log(Long id, Status status, String date) {
        this.id = id;
        this.status = status;
        this.date = date;
    }
}
