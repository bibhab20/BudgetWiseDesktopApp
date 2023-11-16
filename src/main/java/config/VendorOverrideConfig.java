package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VendorOverrideConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("vendorType")
    private String vendorType;
}
