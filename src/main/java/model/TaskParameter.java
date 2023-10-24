package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskParameter {
    String name;
    String value;
    String defaultValue;

    public TaskParameter(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
}
