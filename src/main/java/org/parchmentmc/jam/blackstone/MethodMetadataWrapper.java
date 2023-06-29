package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataBounce;
import com.ldtteam.jam.spi.ast.metadata.IMetadataMethod;
import com.ldtteam.jam.spi.ast.metadata.IMetadataMethodReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.parchmentmc.feather.metadata.MethodMetadata;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.CollectorUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

class MethodMetadataWrapper extends WithSecurityWrapper implements IMetadataMethod {
    private final @Nullable String signature;
    private final @Nullable BouncingTargetMetadataWrapper bouncer;
    private final @Nullable LinkedHashSet<ReferenceWrapper> overrides;
    private final @Nullable ReferenceWrapper parent;

    protected MethodMetadataWrapper(MethodMetadata metadata, Function<Named, String> nameExtractor) {
        super(metadata);
        this.signature = nameExtractor.apply(metadata.getSignature());
        this.bouncer = metadata.getBouncingTarget()
                .filter(b -> {
                    // TODO: somehow, bouncing metadata with null values are getting through
                    // filter them out in the meantime
                    //noinspection OptionalAssignedToNull
                    return b.getTarget() != null && b.getOwner() != null && b.getTarget().isPresent() && b.getOwner().isPresent();
                })
                .map(b -> new BouncingTargetMetadataWrapper(b, nameExtractor))
                .orElse(null);
        this.overrides = WrapperHelper.emptyToNull(metadata.getOverrides().stream()
                .map(r -> new ReferenceWrapper(r, nameExtractor))
                .collect(CollectorUtils.toLinkedSet()));
        this.parent = metadata.getParent()
                .map(r -> new ReferenceWrapper(r, nameExtractor))
                .orElse(null);
    }

    @Override
    public @Nullable String getSignature() {
        return signature;
    }

    @Override
    public @Nullable IMetadataBounce getBouncer() {
        return bouncer;
    }

    @Override
    public @Nullable String getForce() {
        // We don't have an equivalent here in Blackstone
        return null;
    }

    @Override
    public @Nullable Set<? extends IMetadataMethodReference> getOverrides() {
        return overrides;
    }

    @Override
    public @Nullable IMetadataMethodReference getParent() {
        return parent;
    }
}
