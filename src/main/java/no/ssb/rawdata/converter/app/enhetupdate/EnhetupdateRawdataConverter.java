package no.ssb.rawdata.converter.app.enhetupdate;

import lombok.extern.slf4j.Slf4j;
import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.converter.core.convert.ConversionResult;
import no.ssb.rawdata.converter.core.convert.ConversionResult.ConversionResultBuilder;
import no.ssb.rawdata.converter.core.convert.RawdataConverter;
import no.ssb.rawdata.converter.core.convert.ValueInterceptorChain;
import no.ssb.rawdata.converter.core.exception.RawdataConverterException;
import no.ssb.rawdata.converter.core.schema.AggregateSchemaBuilder;
import no.ssb.rawdata.converter.core.schema.DcManifestSchemaAdapter;
import no.ssb.rawdata.converter.util.RawdataMessageAdapter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import java.util.Collection;

import static no.ssb.rawdata.converter.util.AvroSchemaUtil.readAvroSchema;

@Slf4j
public class EnhetupdateRawdataConverter implements RawdataConverter {

    private static final String RAWDATA_ITEMNAME_ENTRY = "entry";
    private static final String FIELDNAME_MANIFEST = "manifest";
    private static final String FIELDNAME_COLLECTOR = "collector";
    private static final String FIELDNAME_DATA = "data";

    private final EnhetupdateRawdataConverterConfig converterConfig;
    private final ValueInterceptorChain valueInterceptorChain;

    private DcManifestSchemaAdapter dcManifestSchemaAdapter;
    private Schema manifestSchema;
    private Schema targetAvroSchema;
    private Schema dataSchema;

    public EnhetupdateRawdataConverter(EnhetupdateRawdataConverterConfig converterConfig, ValueInterceptorChain valueInterceptorChain) {
        this.converterConfig = converterConfig;
        this.valueInterceptorChain = valueInterceptorChain;
        this.dataSchema = readAvroSchema("schema/enhetsreg_update_v1.0.avsc");
    }

    @Override
    public void init(Collection<RawdataMessage> sampleRawdataMessages) {
        log.info("Determine target avro schema from {}", sampleRawdataMessages);
        RawdataMessage sample = sampleRawdataMessages.stream()
          .findFirst()
          .orElseThrow(() ->
            new EnhetupdateRawdataConverterException("Unable to determine target avro schema since no sample rawdata messages were supplied. Make sure to configure `converter-settings.rawdata-samples`")
          );

        RawdataMessageAdapter msg = new RawdataMessageAdapter(sample);
        dcManifestSchemaAdapter = DcManifestSchemaAdapter.of(sample);

        manifestSchema = new AggregateSchemaBuilder("dapla.rawdata.manifest")
          .schema(FIELDNAME_COLLECTOR, dcManifestSchemaAdapter.getDcManifestSchema())
          .build();

        String targetNamespace = "dapla.rawdata.enhetupdate." + msg.getTopic().orElse("dataset");
        targetAvroSchema = new AggregateSchemaBuilder(targetNamespace)
          .schema(FIELDNAME_MANIFEST, manifestSchema)
          .schema(FIELDNAME_DATA, dataSchema)
          .build();
    }

    public DcManifestSchemaAdapter dcManifestSchemaAdapter() {
        if (dcManifestSchemaAdapter == null) {
            throw new IllegalStateException("dcManifestSchemaAdapter is null. Make sure RawdataConverter#init() was invoked in advance.");
        }

        return dcManifestSchemaAdapter;
    }

    @Override
    public Schema targetAvroSchema() {
        if (targetAvroSchema == null) {
            throw new IllegalStateException("targetAvroSchema is null. Make sure RawdataConverter#init() was invoked in advance.");
        }

        return targetAvroSchema;
    }

    @Override
    public boolean isConvertible(RawdataMessage rawdataMessage) {
        return true;
    }

    @Override
    public ConversionResult convert(RawdataMessage rawdataMessage) {
        ConversionResultBuilder resultBuilder = ConversionResult.builder(targetAvroSchema, rawdataMessage);

        RawdataMessageAdapter.print(rawdataMessage); // TODO: Remove this ;-)

        addManifest(rawdataMessage, resultBuilder);
        // TODO: add converted data

        return resultBuilder.build();
    }

    void addManifest(RawdataMessage rawdataMessage, ConversionResultBuilder resultBuilder) {
        GenericRecord manifest = new GenericRecordBuilder(manifestSchema)
          .set(FIELDNAME_COLLECTOR, dcManifestSchemaAdapter.newRecord(rawdataMessage, valueInterceptorChain))
          .build();

        resultBuilder.withRecord(FIELDNAME_MANIFEST, manifest);
    }

    public static class EnhetupdateRawdataConverterException extends RawdataConverterException {
        public EnhetupdateRawdataConverterException(String msg) {
            super(msg);
        }
        public EnhetupdateRawdataConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}