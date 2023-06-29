package org.parchmentmc.jam.blackstone;

import com.ldtteam.jam.spi.ast.metadata.IMetadataAST;
import com.ldtteam.jam.spi.ast.metadata.IMetadataClass;
import org.parchmentmc.feather.metadata.ClassMetadata;
import org.parchmentmc.feather.metadata.SourceMetadata;
import org.parchmentmc.feather.named.Named;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class SourceMetadataWrapper implements IMetadataAST {
    private final Map<String, ClassMetadataWrapper> classesByName;

    SourceMetadataWrapper(SourceMetadata metadata, Function<Named, String> nameExtractor) {
        // Because only outer classes are in the set given by SourceMetadata#getClasses, we have to traverse
        // each class and collect their inner classes
        // We use recursion here instead of a set, in order to preserve ordering of inner classes being after 
        // their parent/enclosing class
        Map<String, ClassMetadataWrapper> classesByName = new LinkedHashMap<>(metadata.getClasses().size());
        for (ClassMetadata classMetadata : metadata.getClasses()) {
            traverse(classesByName, nameExtractor, classMetadata);
        }

        this.classesByName = classesByName;
    }

    @Override
    public Map<String, ? extends IMetadataClass> getClassesByName() {
        return classesByName;
    }

    static void traverse(Map<String, ClassMetadataWrapper> classesByName, Function<Named, String> nameExtractor,
                         ClassMetadata current) {
        final String name = Objects.requireNonNull(nameExtractor.apply(current.getName()));
        classesByName.put(name, new ClassMetadataWrapper(current, nameExtractor));
        for (ClassMetadata innerClass : current.getInnerClasses()) {
            traverse(classesByName, nameExtractor, innerClass);
        }
    }
}
