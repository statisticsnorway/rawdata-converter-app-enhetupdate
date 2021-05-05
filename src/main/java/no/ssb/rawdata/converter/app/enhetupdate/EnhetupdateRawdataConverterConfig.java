package no.ssb.rawdata.converter.app.enhetupdate;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("rawdata.converter.enhetupdate")
@Data
public class EnhetupdateRawdataConverterConfig {

    /**
     * <p>Some config param</p>
     *
     * <p>Defaults to "default value"</p>
     *
     * TODO: Remove this
     */
    private String someParam = "default value";

}