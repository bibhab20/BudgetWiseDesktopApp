package config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VendorTypeConfig {
    String name;
    List<VendorConfig> vendorConfigs;
}
