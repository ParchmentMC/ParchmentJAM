package org.parchmentmc.jam.ast;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.ldtteam.jam.spi.asm.ClassData;
import com.ldtteam.jam.spi.asm.MethodData;
import com.ldtteam.jam.spi.asm.ParameterData;
import com.ldtteam.jam.spi.ast.metadata.IMetadataClass;
import com.ldtteam.jam.spi.ast.named.INamedMethod;
import com.ldtteam.jam.spi.ast.named.INamedParameter;
import com.ldtteam.jam.spi.ast.named.builder.INamedMethodBuilder;

import java.util.Collection;
import java.util.Map;

public class ExpandedNamedMethodBuilder implements INamedMethodBuilder {
    private final INamedMethodBuilder delegate;

    public ExpandedNamedMethodBuilder(INamedMethodBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public INamedMethod build(ClassData classData,
                              MethodData methodData,
                              IMetadataClass classMetadata,
                              Map<String, ClassData> classDatasByAstName,
                              Multimap<ClassData, ClassData> inheritanceVolumes,
                              Map<MethodData, MethodData> rootMethodsByOverride,
                              Multimap<MethodData, MethodData> overrideTree,
                              Map<String, String> identifiedFieldNamesByASTName,
                              BiMap<MethodData, MethodData> methodMappings,
                              BiMap<ParameterData, ParameterData> parameterMappings,
                              BiMap<MethodData, Integer> methodIds,
                              BiMap<ParameterData, Integer> parameterIds) {

        final INamedMethod parent = delegate.build(classData, methodData, classMetadata, classDatasByAstName,
                inheritanceVolumes, rootMethodsByOverride, overrideTree, identifiedFieldNamesByASTName, methodMappings,
                parameterMappings, methodIds, parameterIds);

        return new ExpandedNamedMethod(parent, methodData.node().desc);
    }

    public record ExpandedNamedMethod(int id, String originalName, String identifiedName, String originalDescriptor,
                                      String remappedDescriptor, boolean isStatic, boolean isLambda,
                                      Collection<? extends INamedParameter> parameters) implements INamedMethod {
        public ExpandedNamedMethod(INamedMethod method, String remappedDescriptor) {
            this(method.id(), method.originalName(), method.identifiedName(), method.originalDescriptor(), remappedDescriptor,
                    method.isStatic(), method.isLambda(), method.parameters());
        }
    }
}
