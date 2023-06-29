package org.parchmentmc.jam.ast;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.ldtteam.jam.spi.asm.ClassData;
import com.ldtteam.jam.spi.asm.FieldData;
import com.ldtteam.jam.spi.ast.metadata.IMetadataClass;
import com.ldtteam.jam.spi.ast.named.INamedField;
import com.ldtteam.jam.spi.ast.named.builder.INamedFieldBuilder;

public class ExpandedNamedFieldBuilder implements INamedFieldBuilder {
    private final INamedFieldBuilder delegate;

    public ExpandedNamedFieldBuilder(INamedFieldBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public INamedField build(ClassData classData,
                             FieldData fieldData,
                             IMetadataClass classMetadata,
                             Multimap<ClassData, ClassData> inheritanceVolumes,
                             BiMap<FieldData, FieldData> fieldMappings,
                             BiMap<FieldData, Integer> fieldIds) {

        final INamedField parent = delegate.build(classData, fieldData, classMetadata, inheritanceVolumes, fieldMappings, fieldIds);
        return new ExpandedNamedField(parent, fieldData.node().desc);
    }

    public record ExpandedNamedField(int id, String originalName, String identifiedName,
                                     String remappedDescriptor) implements INamedField {

        public ExpandedNamedField(INamedField field, String remappedDescriptor) {
            this(field.id(), field.originalName(), field.identifiedName(), remappedDescriptor);
        }
    }
}
