package fi.dy.masa.orecontrol.reset;

import fi.dy.masa.orecontrol.config.Configs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;

public final class WorldReset {
    private record Request(Path dimension, RegionReset.Selection selection, CompletableFuture<RegionReset.Result> result,
                           java.util.concurrent.atomic.AtomicBoolean closeFailed) {}
    private static final ConcurrentHashMap<MinecraftServer, Request> PENDING = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<MinecraftServer, Map<String, RegionReset.Selection>> BOUNDARIES = new ConcurrentHashMap<>();

    private WorldReset() {}

    public static CompletableFuture<RegionReset.Result> queue(MinecraftServer server, ResourceKey<Level> dimension, RegionReset.Selection selection) {
        if (!server.isSameThread()) throw new IllegalStateException("Must queue on the server thread");
        if (!Configs.master().enabled("Generic", "allowExistingChunkRebuild")) throw new IllegalStateException("请先开启允许重建旧区域");
        if (server.getLevel(dimension) == null) throw new IllegalArgumentException("Dimension is not loaded");
        var result = new CompletableFuture<RegionReset.Result>();
        Path directory = DimensionType.getStorageFolder(dimension, server.getWorldPath(LevelResource.ROOT));
        if (PENDING.putIfAbsent(server, new Request(directory, selection, result, new java.util.concurrent.atomic.AtomicBoolean())) != null) {
            throw new IllegalStateException("已有待执行的区块重建任务");
        }
        return result;
    }

    public static void finishOnShutdown(MinecraftServer server) {
        BOUNDARIES.remove(server);
        Request request = PENDING.remove(server);
        if (request == null) return;
        try {
            if (request.closeFailed.get()) throw new java.io.IOException("世界文件未能正常关闭，已取消重建");
            request.result.complete(RegionReset.reset(server.getWorldPath(LevelResource.ROOT), request.dimension, request.selection));
        } catch (Exception e) {
            request.result.completeExceptionally(e);
            System.getLogger("orecontrol").log(System.Logger.Level.ERROR, "Chunk reset failed", e);
        }
    }

    public static void loadBoundaries(MinecraftServer server) throws java.io.IOException {
        BOUNDARIES.put(server, RegionReset.latestSelections(server.getWorldPath(LevelResource.ROOT)));
    }

    public static boolean canWrite(WorldGenRegion region, BlockPos position) {
        Map<String, RegionReset.Selection> selections = BOUNDARIES.get(region.getServer());
        if (selections == null) return true;
        var selected = selections.get(region.getLevel().dimension().identifier().toString());
        if (selected == null || !selected.contains(region.getCenter().x(), region.getCenter().z())) return true;
        return selected.contains(Math.floorDiv(position.getX(), 16), Math.floorDiv(position.getZ(), 16));
    }

    public static void markCloseFailed(MinecraftServer server) {
        Request request = PENDING.get(server);
        if (request != null) request.closeFailed.set(true);
    }

    public static void cancelIncomplete(MinecraftServer server) {
        BOUNDARIES.remove(server);
        Request request = PENDING.remove(server);
        if (request != null) request.result.completeExceptionally(new IllegalStateException("服务器关闭未完成区块重建，请检查日志"));
    }
}
