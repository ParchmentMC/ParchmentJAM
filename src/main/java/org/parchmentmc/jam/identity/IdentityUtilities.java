package org.parchmentmc.jam.identity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.jam.spi.asm.ParameterData;
import org.parchmentmc.feather.io.gson.MDCGsonAdapterFactory;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.mapping.MappingDataBuilder;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableClassData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableFieldData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutablePackageData;
import org.parchmentmc.feather.mapping.MappingDataBuilder.MutableParameterData;
import org.parchmentmc.feather.util.SimpleVersion;

import java.lang.reflect.Modifier;
import java.util.stream.Collectors;

final class IdentityUtilities {
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory(false))
            .create();

    private IdentityUtilities() {
    }

    // What Jammer call the parameter's index is just a counting index -- one which starts at 0 for the first parameter,
    // and increments by 1 for each additional parameter
    //
    // However, the Parchment mapping data and the structures in this class assume that a parameter index means the
    // JVM slot index -- starts at 1 for instance methods or 0 for static methods, and increments by 1 for each parameter,
    // except it increments by 2 for parameters that are (non-array) doubles or longs
    //
    // So we have to convert the counting index from Jammer's data structures (in this case, ParameterData) to a JVM
    // slot index. Thankfully, we have access to the MethodNode in all cases where we need to convert a parameter index,
    // so we can pull the method descriptor and the method access from that.

    public static int calculateJVMSlot(ParameterData data) {
        return calculateJVMSlot((byte) data.index(), data.owner().node().desc, Modifier.isStatic(data.owner().node().access));
    }

    // Converts a counting index to a JVM slot
    public static int calculateJVMSlot(byte targetIndex, String descriptor, boolean isStatic) {
        // Assume descriptor is well-formed
        String parameters = descriptor.substring(1, descriptor.indexOf(')'));

        byte index = 0;
        byte slot = (byte) (isStatic ? 0 : 1);
        int cursor = -1;
        StringBuilder currentParam = new StringBuilder();
        boolean parsingLType = false;
        while (++cursor < parameters.length()) {
            char c = parameters.charAt(cursor);
            currentParam.append(c);

            if (c == '[') {
                // Arrays are attached to other components, so skip to the next one (the type contained in the array)
                continue;
            }

            if (parsingLType) { // Currently parsing an L-type
                if (c == ';') {
                    // End of an L-type; continue to the visit section
                    parsingLType = false;
                } else {
                    // Still parsing an L-type, so continue going down the descriptor we reach its end
                    continue;
                }
            }

            if (c == 'L') {
                // Loop until we reach the end of the L-type
                parsingLType = true;
                continue;
            }

            if (index == targetIndex) { // Reached our target index, so quit out to return the calculated slot
                break;
            }
            if (currentParam.length() == 1 && (c == 'D' || c == 'J')) {
                // If a double or long, increment the slot twice
                slot++;
            }
            slot++;

            index++;
            currentParam.setLength(0);
        }
        return slot;
    }

    // Copied from Compass -- MappingUtil#removeUndocumented
    public static void removeUndocumented(MappingDataBuilder builder) {
        builder.getPackages().stream()
                .filter(s -> s.getJavadoc().isEmpty())
                .map(MutablePackageData::getName)
                .collect(Collectors.toSet())
                .forEach(builder::removePackage);

        //noinspection Convert2MethodRef damn you javac
        builder.getClasses().stream()
                .peek(cls -> cls.getFields().stream()
                        .filter(field -> field.getJavadoc().isEmpty())
                        .map(MutableFieldData::getName)
                        .collect(Collectors.toSet())
                        .forEach(field -> cls.removeField(field)))
                .peek(cls -> cls.getMethods().stream()
                        .peek(method -> method.getParameters().stream()
                                .filter(param -> param.getName() == null && param.getJavadoc() == null)
                                .map(MutableParameterData::getIndex)
                                .collect(Collectors.toSet())
                                .forEach(method::removeParameter))
                        .filter(method -> method.getJavadoc().isEmpty() && method.getParameters().isEmpty())
                        .collect(Collectors.toSet())
                        .forEach(method -> cls.removeMethod(method.getName(), method.getDescriptor())))
                .filter(cls -> cls.getJavadoc().isEmpty() && cls.getFields().isEmpty() && cls.getMethods().isEmpty())
                .map(MutableClassData::getName)
                .collect(Collectors.toSet())
                .forEach(builder::removeClass);
    }
}
