package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TaskParameter {
    String name;
    String value;
    String defaultValue;
    List<String> options;

    public TaskParameter(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.options = new ArrayList<>();
    }

    public TaskParameter(String name, String defaultValue, List<String> options) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.options = options;
    }

    public void setValueToDefault() {
        this.value = this.defaultValue;
    }
    public void readInput(String input) {
        if(input == null || input.isBlank()) {
            setValueToDefault();
            return;
        }
        if (options.size() != 0 ) {
            try {
                this.value = this.options.get(Math.min(Integer.parseInt(input) - 1, options.size()-1));
                return;
            } catch (Exception e) {
                this.setValueToDefault();
            }
        }
        this.value = input;
    }

    public enum DataType {
        DATE,
        STRING,
        NUM,
        DOUBLE,
        STRING_LIST,
        NUM_LIST
    }

}
