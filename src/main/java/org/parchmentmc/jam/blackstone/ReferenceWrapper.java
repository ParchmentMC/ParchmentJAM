package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataMethodReference;
import org.parchmentmc.feather.metadata.Reference;
import org.parchmentmc.feather.named.Named;

import java.util.Objects;
import java.util.function.Function;

class ReferenceWrapper implements IMetadataMethodReference {
    private final String owner;
    private final String name;
    private final String descriptor;

    ReferenceWrapper(Reference reference, Function<Named, String> nameExtractor) {
        this.owner = Objects.requireNonNull(nameExtractor.apply(reference.getOwner()));
        this.name = Objects.requireNonNull(nameExtractor.apply(reference.getName()));
        this.descriptor = Objects.requireNonNull(nameExtractor.apply(reference.getDescriptor()));
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDesc() {
        return descriptor;
    }
}
