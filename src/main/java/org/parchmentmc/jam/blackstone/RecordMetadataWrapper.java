package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataRecordComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.parchmentmc.feather.metadata.RecordMetadata;
import org.parchmentmc.feather.named.Named;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

class RecordMetadataWrapper implements IMetadataRecordComponent {
    private final String fieldName;
    private final String descriptor;
    private final String getter;

    RecordMetadataWrapper(RecordMetadata metadata, Function<Named, String> nameExtractor) {
        this.fieldName = Objects.requireNonNull(nameExtractor.apply(metadata.getField().getName()));
        this.descriptor = Objects.requireNonNull(nameExtractor.apply(metadata.getField().getDescriptor()));
        this.getter = Objects.requireNonNull(nameExtractor.apply(metadata.getGetter().getName()));
    }

    @Override
    public String getField() {
        return fieldName;
    }

    @Override
    public String getDesc() {
        return descriptor;
    }

    @Override
    public @Nullable List<String> getMethods() {
        return List.of(getter); // There's always a getter, so this will never be null
    }
}
