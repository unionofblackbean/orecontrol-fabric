package fi.dy.masa.orecontrol.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Reads the original Forge boolean configuration format without a Forge dependency. */
public final class Configs {
    public static final Map<String, String> OPTIONS;
    private static final Pattern CATEGORY = Pattern.compile("\\\"?([A-Za-z]+)\\\"?\\s*\\{");
    private static final Pattern BOOLEAN = Pattern.compile("B:([A-Za-z0-9_]+)\\s*=\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private record Snapshot(Configs master, Map<String, Configs> dimensions) {}
    private static volatile Snapshot snapshot = new Snapshot(new Configs(Map.of()), Map.of());
    private final Map<String, Boolean> values;
    private final boolean disabledDecorations;

    static {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("Generic", "enablePerDimensionConfigs allowExistingChunkRebuild");
        options.put("Minable", "disableAllVanillaGeneration disableCOAL disableDIAMOND disableDIRT disableGOLD disableGRAVEL disableIRON disableLAPIS disableREDSTONE disableQUARTZ disableDIORITE disableGRANITE disableANDESITE disableEMERALD disableSILVERFISH disableCUSTOM disableCOPPER disableANCIENT_DEBRIS disableTUFF disableCLAY disableBLACKSTONE disableSOUL_SAND");
        options.put("Decorate", "disableBIG_SHROOM disableCACTUS disableCLAY disableDEAD_BUSH disableDESERT_WELL disableLILYPAD disableFLOWERS disableFOSSIL disableGRASS disableICE disableLAKE_WATER disableLAKE_LAVA disablePUMPKIN disableREED disableROCK disableSAND disableSAND_PASS2 disableSHROOM disableTREE disableCUSTOM disableBAMBOO disableSEAGRASS disableKELP disableSEA_PICKLE disableCORAL disableVINES disableCAVE_VINES disableNETHER_VINES disableBERRIES disableBUSH disableFIREFLY_BUSH disableLEAF_LITTER disableMELON disableMOSS disableDRIPLEAF disableCAVE_CLAY disableSPORE_BLOSSOM disableROOTS disableNETHER_VEGETATION disableCHORUS disableLICHEN disableSCULK disableDRIPSTONE disableGEODE disableBASALT disableBLACKSTONE_BLOBS disableDELTA disableICEBERG disableBLUE_ICE disableUNDERWATER_MAGMA disableEND_ISLAND disableEND_SPIKE disableEND_GATEWAY disableEND_PLATFORM disableBLOCK_PILES disableDIRT_DISK");
        options.put("Populate", "disableDUNGEON disableFIRE disableGLOWSTONE disableICE disableLAKE disableLAVA disableNETHER_LAVA disableNETHER_LAVA2 disableNETHER_MAGMA disableANIMALS disableCUSTOM");
        options.put("Custom", "disableSurfaceLavaLakes");
        options.put("Stages", "disableALL disableRAW_GENERATION disableLAKES disableLOCAL_MODIFICATIONS disableUNDERGROUND_STRUCTURES disableSURFACE_STRUCTURES disableSTRONGHOLDS disableUNDERGROUND_ORES disableUNDERGROUND_DECORATION disableFLUID_SPRINGS disableVEGETAL_DECORATION disableTOP_LAYER_MODIFICATION");
        options.put("Surface", "disableDIRT disableFROZEN_OCEAN disableBADLANDS_PILLARS");
        options.put("Structures", "disableALL disableMINESHAFT disableTRIAL_CHAMBERS disableVILLAGE disableANCIENT_CITY disableTRAIL_RUINS disableSTRONGHOLD disableDESERT_PYRAMID disableJUNGLE_PYRAMID disableIGLOO disableSWAMP_HUT disableMANSION disablePILLAGER_OUTPOST disableMONUMENT disableOCEAN_RUIN disableSHIPWRECK disableBURIED_TREASURE disableRUINED_PORTAL disableFORTRESS disableBASTION_REMNANT disableNETHER_FOSSIL disableEND_CITY disableCUSTOM");
        OPTIONS = Collections.unmodifiableMap(options);
    }

    private Configs(Map<String, Boolean> values) {
        this.values = Map.copyOf(values);
        this.disabledDecorations = values.entrySet().stream().anyMatch(e -> e.getKey().startsWith("Decorate.disable") && e.getValue());
    }

    public boolean hasDisabledDecorations() { return disabledDecorations; }

    public boolean enabled(String category, String key) {
        return values.getOrDefault(category + "." + key, false);
    }

    public boolean disabled(String category, String type) {
        return enabled(category, "disable" + type);
    }

    public static Configs get(String dimension) {
        Snapshot current = snapshot;
        return current.dimensions.getOrDefault(dimension, current.master);
    }

    public static Configs master() { return snapshot.master; }

    public Map<String, Boolean> settings() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        OPTIONS.forEach((category, keys) -> {
            for (String key : keys.split(" ")) result.put(category + "." + key, enabled(category, key));
        });
        return result;
    }

    public static void write(Path file, Map<String, Boolean> values) throws IOException {
        StringBuilder text = new StringBuilder("# Ore Control. true disables generation except Generic options.\n\n");
        for (var entry : OPTIONS.entrySet()) {
            text.append(entry.getKey()).append(" {\n");
            for (String key : entry.getValue().split(" ")) {
                text.append("    B:").append(key).append('=').append(values.getOrDefault(entry.getKey() + "." + key, false)).append('\n');
            }
            text.append("}\n\n");
        }
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "orecontrol-", ".tmp");
        try {
            Files.writeString(temporary, text);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static synchronized void saveSettings(Path directory, Map<String, Boolean> masterValues,
                                                 String dimension, Map<String, Boolean> dimensionValues) throws IOException {
        Map<String, Boolean> nextMaster = new LinkedHashMap<>(masterValues);
        Map<Path, byte[]> originals = new LinkedHashMap<>();
        Path main = directory.resolve("orecontrol.cfg");
        originals.put(main, Files.exists(main) ? Files.readAllBytes(main) : null);
        Path perDimension = null;
        if (dimension != null) {
            if (!dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IOException("Invalid dimension id");
            String[] id = dimension.split(":", 2);
            Path root = directory.resolve("dimensions").toAbsolutePath().normalize();
            perDimension = root.resolve(id[0]).resolve(id[1] + ".cfg").normalize();
            if (!perDimension.startsWith(root)) throw new IOException("Invalid dimension path");
            originals.put(perDimension, Files.exists(perDimension) ? Files.readAllBytes(perDimension) : null);
            nextMaster.put("Generic.enablePerDimensionConfigs", true);
        }
        try {
            write(main, nextMaster);
            if (perDimension != null) write(perDimension, dimensionValues);
            load(directory);
        } catch (IOException error) {
            for (var original : originals.entrySet()) {
                try {
                    if (original.getValue() == null) Files.deleteIfExists(original.getKey());
                    else Files.write(original.getKey(), original.getValue());
                } catch (IOException restoreError) { error.addSuppressed(restoreError); }
            }
            throw error;
        }
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
        snapshot = new Snapshot(nextMaster, Map.copyOf(nextDimensions));
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
            write(file, values);
        }
        return new Configs(values);
    }
}
