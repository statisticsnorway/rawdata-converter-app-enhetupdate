package no.ssb.rawdata.converter.app.enhetupdate;

import no.ssb.rawdata.converter.core.convert.ConversionResult;
import no.ssb.rawdata.converter.core.convert.ValueInterceptorChain;
import no.ssb.rawdata.converter.test.message.RawdataMessageFixtures;
import no.ssb.rawdata.converter.test.message.RawdataMessages;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Disabled
public class EnhetupdateRawdataConverterTest {

    static RawdataMessageFixtures fixtures;

    @BeforeAll
    static void loadFixtures() {
        fixtures = RawdataMessageFixtures.init("sometopic");
    }

    @Disabled
    @Test
    void shouldConvertRawdataMessages() {
        RawdataMessages messages = fixtures.rawdataMessages("sometopic"); // TODO: replace with topicname
        EnhetupdateRawdataConverterConfig config = new EnhetupdateRawdataConverterConfig();
        // TODO: Set app config

        EnhetupdateRawdataConverter converter = new EnhetupdateRawdataConverter(config, new ValueInterceptorChain());
        converter.init(messages.index().values());
        ConversionResult res = converter.convert(messages.index().get("123456")); // TODO: replace with message position
    }

}
