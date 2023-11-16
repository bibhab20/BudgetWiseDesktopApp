package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ParameterBatch {
    String name;
    List<TaskParameter> parameters;

    public ParameterBatch(String name) {
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public ParameterBatch(String name, List<TaskParameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }
}
