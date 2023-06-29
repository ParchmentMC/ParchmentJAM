package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataClass;
import com.ldtteam.jam.spi.ast.metadata.IMetadataField;
import com.ldtteam.jam.spi.ast.metadata.IMetadataMethod;
import com.ldtteam.jam.spi.ast.metadata.IMetadataRecordComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.parchmentmc.feather.metadata.ClassMetadata;
import org.parchmentmc.feather.named.Named;
import org.parchmentmc.feather.util.CollectorUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.parchmentmc.jam.blackstone.WrapperHelper.emptyToNull;

class ClassMetadataWrapper extends WithSecurityWrapper implements IMetadataClass {
    private final String superName;
    private final @Nullable List<String> interfaces;
    private final String signature;
    private final @Nullable Map<String, FieldMetadataWrapper> fieldsByName;
    private final @Nullable Map<String, MethodMetadataWrapper> methodsByName;
    private final @Nullable List<RecordMetadataWrapper> records;

    ClassMetadataWrapper(ClassMetadata metadata, Function<Named, String> nameExtractor) {
        super(metadata);
        this.superName = nameExtractor.apply(metadata.getSuperName());
        this.interfaces = metadata.getInterfaces().isEmpty() ? null :
                metadata.getInterfaces().stream().map(nameExtractor).toList();
        this.signature = nameExtractor.apply(metadata.getSignature());

        this.fieldsByName = emptyToNull(metadata.getFields().stream().collect(CollectorUtils.toLinkedMap(
                f -> nameExtractor.apply(f.getName()),
                f -> new FieldMetadataWrapper(f, nameExtractor))));
        this.methodsByName = emptyToNull(metadata.getMethods().stream().collect(CollectorUtils.toLinkedMap(
                m -> nameExtractor.apply(m.getName()) + nameExtractor.apply(m.getDescriptor()),
                m -> new MethodMetadataWrapper(m, nameExtractor))));
        this.records = emptyToNull(metadata.getRecords().stream()
                .map(r -> new RecordMetadataWrapper(r, nameExtractor))
                .toList());
    }

    @Override
    public @Nullable String getSuperName() {
        return superName;
    }

    @Override
    public @Nullable Collection<String> getInterfaces() {
        return interfaces;
    }

    @Override
    public @Nullable String getSignature() {
        return signature;
    }

    @Override
    public @Nullable Map<String, ? extends IMetadataField> getFieldsByName() {
        return fieldsByName;
    }

    @Override
    public @Nullable Map<String, ? extends IMetadataMethod> getMethodsByName() {
        return methodsByName;
    }

    @Override
    public @Nullable List<? extends IMetadataRecordComponent> getRecords() {
        return records;
    }
}
