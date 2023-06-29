package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataField;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.parchmentmc.feather.metadata.FieldMetadata;
import org.parchmentmc.feather.named.Named;

import java.util.function.Function;

class FieldMetadataWrapper extends WithSecurityWrapper implements IMetadataField {
    private final @Nullable String descriptor;
    private final @Nullable String signature;

    FieldMetadataWrapper(FieldMetadata metadata, Function<Named, String> nameExtractor) {
        super(metadata);

        this.descriptor = nameExtractor.apply(metadata.getDescriptor());
        this.signature = nameExtractor.apply(metadata.getSignature());
    }

    @Override
    public @Nullable String getDesc() {
        return descriptor;
    }

    @Override
    public @Nullable String getSignature() {
        return signature;
    }

    @Override
    public @Nullable String getForce() {
        // We don't have an equivalent here in Blackstone
        return null;
    }
}
