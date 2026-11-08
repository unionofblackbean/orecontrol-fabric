package fi.dy.masa.orecontrol.reset;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

/** Edits only closed region files. The caller must hold the world's session lock. */
public final class RegionReset {
    private static final Gson GSON = new Gson();

    public record Selection(int minX, int minZ, int maxX, int maxZ) {
        public Selection {
            if (minX > maxX || minZ > maxZ) throw new IllegalArgumentException("起点区块必须小于或等于终点区块");
            if (Math.max(Math.abs((long) minX), Math.abs((long) maxX)) > 1874999
                    || Math.max(Math.abs((long) minZ), Math.abs((long) maxZ)) > 1874999) {
                throw new IllegalArgumentException("区块坐标超出世界边界");
            }
            if ((long) (maxX - minX + 1) * (maxZ - minZ + 1) > 1024) {
                throw new IllegalArgumentException("一次最多选择 1024 个区块");
            }
        }
        public int count() { return (maxX - minX + 1) * (maxZ - minZ + 1); }
        public boolean contains(int x, int z) { return x >= minX && x <= maxX && z >= minZ && z <= maxZ; }
    }

    public record Result(Path backup, int chunks) {}
    private record Journal(String state, List<String> files, String dimension, Selection selection) {}

    private RegionReset() {}

    public static Result reset(Path world, Path dimension, Selection selection) throws IOException {
        world = world.toAbsolutePath().normalize();
        dimension = dimension.toAbsolutePath().normalize();
        if (!dimension.startsWith(world)) throw new IOException("Dimension is outside this world");
        var files = new LinkedHashSet<Path>();
        int chunks = 0;
        for (int x = selection.minX; x <= selection.maxX; x++) {
            for (int z = selection.minZ; z <= selection.maxZ; z++) {
                for (String folder : List.of("region", "entities", "poi")) {
                    Path region = dimension.resolve(folder).resolve("r." + Math.floorDiv(x, 32) + "." + Math.floorDiv(z, 32) + ".mca");
                    if (hasChunk(region, x, z)) {
                        files.add(region);
                        if (folder.equals("region")) chunks++;
                    }
                    Path external = region.getParent().resolve("c." + x + "." + z + ".mcc");
                    if (Files.isRegularFile(external)) files.add(external);
                }
            }
        }
        if (files.isEmpty()) return new Result(null, 0);
        Path backup = world.resolve("orecontrol-backups").resolve(System.currentTimeMillis() + "-" + UUID.randomUUID());
        Files.createDirectories(backup);
        List<String> relative = new ArrayList<>();
        for (Path file : files) {
            String name = world.relativize(file).toString().replace('\\', '/');
            Path destination = checkedPath(backup, name);
            Files.createDirectories(destination.getParent());
            Files.copy(file, destination, StandardCopyOption.COPY_ATTRIBUTES);
            relative.add(name);
        }
        Journal journal = new Journal("PREPARED", relative, world.relativize(dimension).toString(), selection);
        writeJournal(backup, journal);
        try {
            for (Path file : files) {
                if (file.toString().endsWith(".mcc")) {
                    Files.delete(file);
                    continue;
                }
                try (RandomAccessFile region = new RandomAccessFile(file.toFile(), "rw")) {
                    String[] parts = file.getFileName().toString().split("\\.");
                    int rx = Integer.parseInt(parts[1]);
                    int rz = Integer.parseInt(parts[2]);
                    for (int x = Math.max(selection.minX, rx * 32); x <= Math.min(selection.maxX, rx * 32 + 31); x++) {
                        for (int z = Math.max(selection.minZ, rz * 32); z <= Math.min(selection.maxZ, rz * 32 + 31); z++) {
                            long offset = 4L * (Math.floorMod(x, 32) + Math.floorMod(z, 32) * 32);
                            region.seek(offset);
                            region.writeInt(0);
                            region.seek(4096 + offset);
                            region.writeInt(0);
                        }
                    }
                    region.getFD().sync();
                }
            }
            writeJournal(backup, new Journal("COMPLETE", relative, journal.dimension, selection));
        } catch (Exception error) {
            try { restore(world, backup, journal); }
            catch (IOException rollbackError) { error.addSuppressed(rollbackError); }
            throw new IOException("区块重建准备失败，备份位于 " + backup, error);
        }
        return new Result(backup, chunks);
    }

    public static boolean hasChunk(Path region, int x, int z) throws IOException {
        if (!Files.isRegularFile(region)) return false;
        try (RandomAccessFile file = new RandomAccessFile(region.toFile(), "r")) {
            if (file.length() == 0) return false;
            if (file.length() < 8192) throw new IOException("Truncated region file " + region);
            file.seek(4L * (Math.floorMod(x, 32) + Math.floorMod(z, 32) * 32));
            return file.readInt() != 0;
        }
    }

    public static void recoverInterrupted(Path world) throws IOException {
        Path backups = world.resolve("orecontrol-backups");
        if (!Files.isDirectory(backups)) return;
        try (var entries = Files.list(backups)) {
            for (Path backup : entries.filter(Files::isDirectory).sorted().toList()) {
                Path manifest = backup.resolve("manifest.json");
                if (!Files.isRegularFile(manifest)) continue;
                Journal journal = GSON.fromJson(Files.readString(manifest), Journal.class);
                if (journal != null && journal.state.equals("PREPARED")) restore(world, backup, journal);
            }
        }
    }

    public static Map<String, Selection> latestSelections(Path world) throws IOException {
        Map<String, Selection> selections = new LinkedHashMap<>();
        Path backups = world.resolve("orecontrol-backups");
        if (!Files.isDirectory(backups)) return Map.of();
        try (var entries = Files.list(backups)) {
            for (Path backup : entries.filter(Files::isDirectory).sorted().toList()) {
                Path manifest = backup.resolve("manifest.json");
                if (!Files.isRegularFile(manifest)) continue;
                Journal journal = GSON.fromJson(Files.readString(manifest), Journal.class);
                if (journal == null || !journal.state.equals("COMPLETE")) continue;
                String directory = journal.dimension.replace('\\', '/');
                if (directory.startsWith("dimensions/")) {
                    String name = directory.substring("dimensions/".length());
                    int slash = name.indexOf('/');
                    if (slash > 0) selections.put(name.substring(0, slash) + ":" + name.substring(slash + 1), journal.selection);
                }
            }
        }
        return Map.copyOf(selections);
    }

    private static void restore(Path world, Path backup, Journal journal) throws IOException {
        for (String name : journal.files) {
            Path target = checkedPath(world, name);
            Files.createDirectories(target.getParent());
            Files.copy(checkedPath(backup, name), target, StandardCopyOption.REPLACE_EXISTING);
        }
        writeJournal(backup, new Journal("ROLLED_BACK", journal.files, journal.dimension, journal.selection));
    }

    private static Path checkedPath(Path root, String relative) throws IOException {
        Path base = root.toAbsolutePath().normalize();
        Path path = base.resolve(relative).normalize();
        if (!path.startsWith(base) || !(relative.endsWith(".mca") || relative.endsWith(".mcc"))) {
            throw new IOException("Invalid backup entry");
        }
        return path;
    }

    private static void writeJournal(Path backup, Journal journal) throws IOException {
        Path temporary = backup.resolve("manifest.tmp");
        Files.writeString(temporary, GSON.toJson(journal));
        try (var channel = java.nio.channels.FileChannel.open(temporary, java.nio.file.StandardOpenOption.WRITE)) { channel.force(true); }
        Files.move(temporary, backup.resolve("manifest.json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
