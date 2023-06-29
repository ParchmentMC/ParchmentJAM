package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataBounce;
import com.ldtteam.jam.spi.ast.metadata.IMetadataMethodReference;
import org.parchmentmc.feather.metadata.BouncingTargetMetadata;
import org.parchmentmc.feather.named.Named;

import java.util.function.Function;

class BouncingTargetMetadataWrapper implements IMetadataBounce {
    private final ReferenceWrapper target;
    private final ReferenceWrapper owner;

    BouncingTargetMetadataWrapper(BouncingTargetMetadata metadata, Function<Named, String> nameExtractor) {
        if (metadata.getTarget().isEmpty()) {
            System.out.println("u wot mate");
        }
        this.target = new ReferenceWrapper(metadata.getTarget().orElseThrow(), nameExtractor);
        this.owner = new ReferenceWrapper(metadata.getOwner().orElseThrow(), nameExtractor);
    }

    @Override
    public IMetadataMethodReference getTarget() {
        return target;
    }

    @Override
    public IMetadataMethodReference getOwner() {
        return owner;
    }
}
