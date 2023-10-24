package config;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class VendorConfig {
    @Getter
    @Setter
    @JsonProperty("vendor")
    private String name;

    @Getter
    @Setter
    @JsonProperty("contains")
    private List<String> contains;

    @Getter
    @Setter
    @JsonProperty("notContain")
    private List<String> notContain;

    @Getter
    @Setter
    @JsonProperty("tags")
    private List<String> tags;

    @Getter
    @Setter
    @JsonProperty("vendorType")
    private String vendorType;

}
