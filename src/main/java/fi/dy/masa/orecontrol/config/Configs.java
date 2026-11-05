package fi.dy.masa.orecontrol.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Reads the original Forge boolean configuration format without a Forge dependency. */
public final class Configs {
    public static final Map<String, String> OPTIONS;
    private static final Pattern CATEGORY = Pattern.compile("\\\"?([A-Za-z]+)\\\"?\\s*\\{");
    private static final Pattern BOOLEAN = Pattern.compile("B:([A-Za-z0-9_]+)\\s*=\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static volatile Configs master = new Configs(Map.of());
    private static volatile Map<String, Configs> dimensions = Map.of();
    private final Map<String, Boolean> values;

    static {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("Generic", "enablePerDimensionConfigs");
        options.put("Minable", "disableAllVanillaGeneration disableCOAL disableDIAMOND disableDIRT disableGOLD disableGRAVEL disableIRON disableLAPIS disableREDSTONE disableQUARTZ disableDIORITE disableGRANITE disableANDESITE disableEMERALD disableSILVERFISH disableCUSTOM disableCOPPER disableANCIENT_DEBRIS");
        options.put("Decorate", "disableBIG_SHROOM disableCACTUS disableCLAY disableDEAD_BUSH disableDESERT_WELL disableLILYPAD disableFLOWERS disableFOSSIL disableGRASS disableICE disableLAKE_WATER disableLAKE_LAVA disablePUMPKIN disableREED disableROCK disableSAND disableSAND_PASS2 disableSHROOM disableTREE disableCUSTOM");
        options.put("Populate", "disableDUNGEON disableFIRE disableGLOWSTONE disableICE disableLAKE disableLAVA disableNETHER_LAVA disableNETHER_LAVA2 disableNETHER_MAGMA disableANIMALS disableCUSTOM");
        options.put("Custom", "disableSurfaceLavaLakes");
        OPTIONS = Collections.unmodifiableMap(options);
    }

    private Configs(Map<String, Boolean> values) {
        this.values = Map.copyOf(values);
    }

    public boolean enabled(String category, String key) {
        return values.getOrDefault(category + "." + key, false);
    }

    public boolean disabled(String category, String type) {
        return enabled(category, "disable" + type);
    }

    public static Configs get(String dimension) {
        return dimensions.getOrDefault(dimension, master);
    }

    public static synchronized void load(Path directory) throws IOException {
        Files.createDirectories(directory);
        Configs nextMaster = read(directory.resolve("orecontrol.cfg"));
        Map<String, Configs> nextDimensions = new LinkedHashMap<>();
        if (nextMaster.enabled("Generic", "enablePerDimensionConfigs")) {
            String[] ids = {"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};
            String[] legacy = {"0", "-1", "1"};
            for (int i = 0; i < ids.length; i++) {
                Path file = directory.resolve("orecontrol_dim" + legacy[i] + ".cfg");
                if (Files.isRegularFile(file)) nextDimensions.put(ids[i], read(file));
            }
            Path root = directory.resolve("dimensions");
            if (Files.isDirectory(root)) {
                try (var paths = Files.walk(root)) {
                    for (Path file : paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".cfg")).toList()) {
                        String relative = root.relativize(file).toString().replace('\\', '/');
                        int slash = relative.indexOf('/');
                        if (slash > 0) {
                            String id = relative.substring(0, slash) + ":" + relative.substring(slash + 1, relative.length() - 4);
                            nextDimensions.put(id, read(file));
                        }
                    }
                }
            }
        }
        dimensions = Map.copyOf(nextDimensions);
        master = nextMaster;
    }

    public static Configs read(Path file) throws IOException {
        Map<String, Boolean> values = new LinkedHashMap<>();
        if (Files.exists(file)) {
            String category = "";
            for (String raw : Files.readAllLines(file)) {
                String line = raw.split("#", 2)[0].strip();
                if (line.isEmpty()) continue;
                var section = CATEGORY.matcher(line);
                var property = BOOLEAN.matcher(line);
                if (section.matches()) category = section.group(1);
                else if (line.equals("}")) category = "";
                else if (property.matches() && !category.isEmpty()) {
                    values.put(category + "." + property.group(1), Boolean.parseBoolean(property.group(2)));
                } else if (line.startsWith("B:")) {
                    throw new IOException("Invalid boolean in " + file + ": " + line);
                }
            }
        }
        boolean missing = OPTIONS.entrySet().stream().anyMatch(entry ->
                java.util.Arrays.stream(entry.getValue().split(" ")).anyMatch(key -> !values.containsKey(entry.getKey() + "." + key)));
        if (missing) {
            StringBuilder text = new StringBuilder("# Ore Control Fabric 26.1.2. true disables the corresponding generation.\n# Per-dimension files fully override the master settings, as in the original mod.\n\n");
            for (var entry : OPTIONS.entrySet()) {
                text.append(entry.getKey()).append(" {\n");
                for (String key : entry.getValue().split(" ")) {
                    text.append("    B:").append(key).append('=').append(values.getOrDefault(entry.getKey() + "." + key, false)).append('\n');
                }
                text.append("}\n\n");
            }
            Files.writeString(file, text);
        }
        return new Configs(values);
    }
}
