package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CategoryConfig {

    @Getter
    @Setter
    @JsonProperty("name")
    private String name;

    @Getter
    @Setter
    @JsonProperty("vendorTypes")
    private List<String> vendorTypes;

    @Getter
    @Setter
    @JsonProperty("vendors")
    private List<String> vendors;
}
