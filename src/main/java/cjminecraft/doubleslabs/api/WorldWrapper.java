package cjminecraft.doubleslabs.api;

import cjminecraft.doubleslabs.common.tileentity.SlabTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WorldWrapper extends World {

    private World world;
    
    private SlabTileEntity tile;
    private boolean positive;

    public WorldWrapper(World world) {
        super((ISpawnWorldInfo) world.getWorldInfo(), world.func_234923_W_(), world.func_234922_V_(), world.func_230315_m_(), world.func_234924_Y_(), world.isRemote, world.func_234925_Z_(), 0L);
        this.world = world;
    }

    public boolean isPositive() {
        return this.positive;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setTile(SlabTileEntity tile, boolean positive) {
        this.tile = tile;
        this.positive = positive;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return pos.equals(this.tile.getPos()) ? (this.positive ? this.tile.getPositiveBlockInfo().getBlockState() : this.tile.getNegativeBlockInfo().getBlockState()) : super.getBlockState(pos);
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        this.world.playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {
        this.world.playMovingSound(playerIn, entityIn, eventIn, categoryIn, volume, pitch);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.world.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return pos.equals(this.tile.getPos()) ? (this.positive ? this.tile.getPositiveBlockInfo().getTileEntity() : this.tile.getNegativeBlockInfo().getTileEntity()) : super.getTileEntity(pos);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        if (pos.equals(this.tile.getPos())) {
            if (this.positive)
                this.tile.getPositiveBlockInfo().setTileEntity(tileEntityIn);
            else
                this.tile.getNegativeBlockInfo().setTileEntity(tileEntityIn);
        } else {
            super.setTileEntity(pos, tileEntityIn);
        }
    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return this.world.getEntityByID(id);
    }

    @Nullable
    @Override
    public MapData getMapData(String mapName) {
        return this.world.getMapData(mapName);
    }

    @Override
    public void registerMapData(MapData mapDataIn) {
        this.world.registerMapData(mapDataIn);
    }

    @Override
    public int getNextMapId() {
        return this.world.getNextMapId();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        this.world.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.world.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.world.getRecipeManager();
    }

    @Override
    public NetworkTagManager getTags() {
        return this.world.getTags();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return this.world.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return this.world.getPendingFluidTicks();
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
        this.world.playEvent(player, type, pos, data);
    }

    @Override
    public int func_234938_ad_() {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return this.world.getPlayers();
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return this.world.getNoiseBiomeRaw(x, y, z);
    }

    @Override
    public boolean isRemote() {
        return this.world.isRemote();
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.world.getServer();
    }

    @Override
    public Chunk getChunkAt(BlockPos pos) {
        return this.world.getChunkAt(pos);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return this.world.getChunk(chunkX, chunkZ);
    }

    @Override
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return this.world.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState newState, int flags, int p_241211_4_) {
        this.world.markAndNotifyBlock(pos, chunk, blockstate, newState, flags, p_241211_4_);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState blockStateIn, BlockState newState) {
        this.world.onBlockStateChange(pos, blockStateIn, newState);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return this.world.removeBlock(pos, isMoving);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity destroyer) {
        return this.world.destroyBlock(pos, dropBlock, destroyer);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state) {
        if (pos.equals(this.tile.getPos())) {
            if (this.positive)
                this.tile.getPositiveBlockInfo().setBlockState(state);
            else
                this.tile.getNegativeBlockInfo().setBlockState(state);
            return true;
        }
        return this.world.setBlockState(pos, state);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos blockPosIn, BlockState oldState, BlockState newState) {
        this.world.markBlockRangeForRenderUpdate(blockPosIn, oldState, newState);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockIn) {
        this.world.notifyNeighborsOfStateChange(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, Direction skipSide) {
        this.world.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.world.neighborChanged(pos, blockIn, fromPos);
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return this.world.getHeight(heightmapType, x, z);
    }

    @Override
    public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
        return 0;
    }

    @Override
    public WorldLightManager getLightManager() {
        return this.world.getLightManager();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.world.getFluidState(pos);
    }

    @Override
    public boolean isDaytime() {
        return this.world.isDaytime();
    }

    @Override
    public boolean isNightTime() {
        return this.world.isNightTime();
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        this.world.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        this.world.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        this.world.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        this.world.addParticle(particleData, forceAlwaysRender, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        this.world.addOptionalParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addOptionalParticle(IParticleData particleData, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        this.world.addOptionalParticle(particleData, ignoreRange, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return this.world.getCelestialAngleRadians(partialTicks);
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return this.world.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        this.world.addTileEntities(tileEntityCollection);
    }

    @Override
    public void tickBlockEntities() {
        this.world.tickBlockEntities();
    }

    @Override
    public void guardEntityTick(Consumer<Entity> consumerEntity, Entity entityIn) {
        this.world.guardEntityTick(consumerEntity, entityIn);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, Explosion.Mode modeIn) {
        return this.world.createExplosion(entityIn, xIn, yIn, zIn, explosionRadius, modeIn);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entityIn, double xIn, double yIn, double zIn, float explosionRadius, boolean causesFire, Explosion.Mode modeIn) {
        return this.world.createExplosion(entityIn, xIn, yIn, zIn, explosionRadius, causesFire, modeIn);
    }

    @Override
    public String getProviderName() {
        return this.world.getProviderName();
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (pos.equals(this.tile.getPos()))
            if (this.positive)
                this.tile.getPositiveBlockInfo().setTileEntity(null);
            else
                this.tile.getNegativeBlockInfo().setTileEntity(null);
        else
            this.world.removeTileEntity(pos);
    }

    @Override
    public boolean isBlockPresent(BlockPos pos) {
        return this.world.isBlockPresent(pos);
    }

    @Override
    public boolean isTopSolid(BlockPos pos, Entity entityIn) {
        return this.world.isTopSolid(pos, entityIn);
    }

    @Override
    public void calculateInitialSkylight() {
        this.world.calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        this.world.setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Nullable
    @Override
    public IBlockReader getBlockReader(int chunkX, int chunkZ) {
        return super.getBlockReader(chunkX, chunkZ);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        return this.world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(@Nullable EntityType<T> type, AxisAlignedBB boundingBox, Predicate<? super T> predicate) {
        return this.world.getEntitiesWithinAABB(type, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        return this.world.getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
        return this.world.getLoadedEntitiesWithinAABB(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        this.world.markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public World getWorld() {
        return this.world.getWorld();
    }

    @Override
    public int getStrongPower(BlockPos pos) {
        return this.world.getStrongPower(pos);
    }

    @Override
    public boolean isSidePowered(BlockPos pos, Direction side) {
        return this.world.isSidePowered(pos, side);
    }

    @Override
    public int getRedstonePower(BlockPos pos, Direction facing) {
        return this.world.getRedstonePower(pos, facing);
    }

    @Override
    public boolean isBlockPowered(BlockPos pos) {
        return this.world.isBlockPowered(pos);
    }

    @Override
    public int getRedstonePowerFromNeighbors(BlockPos pos) {
        return this.world.getRedstonePowerFromNeighbors(pos);
    }

    @Override
    public void sendQuittingDisconnectingPacket() {
        this.world.sendQuittingDisconnectingPacket();
    }

    @Override
    public long getGameTime() {
        return this.world.getGameTime();
    }

    @Override
    public long getDayTime() {
        return this.world.getDayTime();
    }

    @Override
    public boolean isBlockModifiable(PlayerEntity player, BlockPos pos) {
        return this.world.isBlockModifiable(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        this.world.setEntityState(entityIn, state);
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return this.world.getChunkProvider();
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        this.world.addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public IWorldInfo getWorldInfo() {
        return this.worldInfo;
    }

    @Override
    public GameRules getGameRules() {
        return this.world.getGameRules();
    }

    @Override
    public float getThunderStrength(float delta) {
        return this.world.getThunderStrength(delta);
    }

    @Override
    public void setThunderStrength(float strength) {
        this.world.setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        return this.world.getRainStrength(delta);
    }

    @Override
    public void setRainStrength(float strength) {
        this.world.setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return this.world.isThundering();
    }

    @Override
    public boolean isRaining() {
        return this.world.isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos position) {
        return this.world.isRainingAt(position);
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos) {
        return this.world.isBlockinHighHumidity(pos);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        this.world.playBroadcastSound(id, pos, data);
    }

    @Override
    public CrashReportCategory fillCrashReport(CrashReport report) {
        return this.world.fillCrashReport(report);
    }

    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable CompoundNBT compound) {
        this.world.makeFireworks(x, y, z, motionX, motionY, motionZ, compound);
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        this.world.updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return this.world.getDifficultyForLocation(pos);
    }

    @Override
    public int getSkylightSubtracted() {
        return this.world.getSkylightSubtracted();
    }

    @Override
    public void setTimeLightningFlash(int timeFlashIn) {
        this.world.setTimeLightningFlash(timeFlashIn);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public void sendPacketToServer(IPacket<?> packetIn) {
        this.world.sendPacketToServer(packetIn);
    }

    @Override
    public Random getRandom() {
        return this.world.getRandom();
    }

    @Override
    public void func_230547_a_(BlockPos p_230547_1_, Block p_230547_2_) {

    }

    @Override
    public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return this.world.hasBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public BlockPos getBlockRandomPos(int p_217383_1_, int p_217383_2_, int p_217383_3_, int p_217383_4_) {
        return this.world.getBlockRandomPos(p_217383_1_, p_217383_2_, p_217383_3_, p_217383_4_);
    }

    @Override
    public boolean isSaveDisabled() {
        return this.world.isSaveDisabled();
    }

    @Override
    public IProfiler getProfiler() {
        return this.world.getProfiler();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.world.getBiomeManager();
    }

    @Override
    public double getMaxEntityRadius() {
        return this.world.getMaxEntityRadius();
    }

    @Override
    public double increaseMaxEntityRadius(double value) {
        return this.world.increaseMaxEntityRadius(value);
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return this.world.getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        return this.world.getCelestialAngle(partialTicks);
    }

    @Override
    public int getMoonPhase() {
        return this.world.getMoonPhase();
    }

    @Override
    public Difficulty getDifficulty() {
        return this.world.getDifficulty();
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return this.world.chunkExists(chunkX, chunkZ);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        this.world.playEvent(type, pos, data);
    }

    @Override
    public Stream<VoxelShape> func_230318_c_(@Nullable Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_) {
        return this.world.func_230318_c_(p_230318_1_, p_230318_2_, p_230318_3_);
    }

    @Override
    public Stream<VoxelShape> func_234867_d_(@Nullable Entity p_234867_1_, AxisAlignedBB p_234867_2_, Predicate<Entity> p_234867_3_) {
        return this.world.func_234867_d_(p_234867_1_, p_234867_2_, p_234867_3_);
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return this.world.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
        return this.getHeight(heightmapType, pos);
    }

    @Override
    public boolean func_226663_a_(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
        return this.world.func_226663_a_(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean checkNoEntityCollision(Entity p_226668_1_) {
        return this.world.checkNoEntityCollision(p_226668_1_);
    }

    @Override
    public boolean hasNoCollisions(AxisAlignedBB p_226664_1_) {
        return this.world.hasNoCollisions(p_226664_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226669_1_) {
        return this.world.hasNoCollisions(p_226669_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
        return this.world.hasNoCollisions(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean func_234865_b_(@Nullable Entity p_234865_1_, AxisAlignedBB p_234865_2_, Predicate<Entity> p_234865_3_) {
        return this.world.func_234865_b_(p_234865_1_, p_234865_2_, p_234865_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(@Nullable Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
        return this.world.getCollisionShapes(p_226666_1_, p_226666_2_);
    }

    @Override
    public Stream<VoxelShape> func_241457_a_(@Nullable Entity p_241457_1_, AxisAlignedBB p_241457_2_, BiPredicate<BlockState, BlockPos> p_241457_3_) {
        return this.world.func_241457_a_(p_241457_1_, p_241457_2_, p_241457_3_);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return this.world.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return this.world.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return this.world.getHeight();
    }

    @Override
    public Stream<BlockState> func_234853_a_(AxisAlignedBB p_234853_1_) {
        return null;
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return this.world.rayTraceBlocks(context);
    }

    @Nullable
    @Override
    public BlockRayTraceResult rayTraceBlocks(Vector3d p_217296_1_, Vector3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return this.world.rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        return this.world.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
        return this.world.getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
        return this.world.getLoadedEntitiesWithinAABB(p_225317_1_, p_225317_2_);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, @Nullable Predicate<Entity> predicate) {
        return this.world.getClosestPlayer(x, y, z, distance, predicate);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(Entity entityIn, double distance) {
        return this.world.getClosestPlayer(entityIn, distance);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return this.world.getClosestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    public boolean isPlayerWithin(double x, double y, double z, double distance) {
        return this.world.isPlayerWithin(x, y, z, distance);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target) {
        return this.world.getClosestPlayer(predicate, target);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return this.world.getClosestPlayer(predicate, target, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, double x, double y, double z) {
        return this.world.getClosestPlayer(predicate, x, y, z);
    }

    @Nullable
    @Override
    public <T extends LivingEntity> T getClosestEntityWithinAABB(Class<? extends T> entityClazz, EntityPredicate p_217360_2_, @Nullable LivingEntity target, double x, double y, double z, AxisAlignedBB boundingBox) {
        return this.world.getClosestEntityWithinAABB(entityClazz, p_217360_2_, target, x, y, z, boundingBox);
    }

    @Nullable
    @Override
    public <T extends LivingEntity> T func_225318_b(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
        return this.world.func_225318_b(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Nullable
    @Override
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, EntityPredicate predicate, @Nullable LivingEntity target, double x, double y, double z) {
        return this.world.getClosestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate predicate, LivingEntity target, AxisAlignedBB box) {
        return this.world.getTargettablePlayersWithinAABB(predicate, target, box);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
        return this.world.getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Nullable
    @Override
    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
        return this.world.getPlayerByUuid(uniqueIdIn);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.world.getBiome(pos);
    }

    @Override
    public Stream<BlockState> func_234939_c_(AxisAlignedBB p_234939_1_) {
        return null;
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return this.world.getBlockColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return this.world.getNoiseBiome(x, y, z);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.world.isAirBlock(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return this.world.canSeeSky(pos);
    }

    @Override
    public float getBrightness(BlockPos pos) {
        return this.world.getBrightness(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return this.world.getStrongPower(pos, direction);
    }

    @Override
    public IChunk getChunk(BlockPos pos) {
        return this.world.getChunk(pos);
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return this.world.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    public boolean hasWater(BlockPos pos) {
        return this.world.hasWater(pos);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return this.world.containsAnyLiquid(bb);
    }

    @Override
    public int getLight(BlockPos pos) {
        return this.world.getLight(pos);
    }

    @Override
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        return this.world.getNeighborAwareLightSubtracted(pos, amount);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return this.world.isBlockLoaded(pos);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return this.world.isAreaLoaded(center, range);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return this.world.isAreaLoaded(from, to);
    }

    @Override
    public boolean isAreaLoaded(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return this.world.isAreaLoaded(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
        return this.world.getLightFor(lightTypeIn, blockPosIn);
    }

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount) {
        return this.world.getLightSubtracted(blockPosIn, amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn) {
        return this.world.canSeeSky(blockPosIn);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return this.world.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean addEntity(Entity entityIn) {
        return this.world.addEntity(entityIn);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return this.world.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return this.world.getCapability(cap);
    }

    @Override
    public double func_234936_m_(BlockPos p_234936_1_) {
        return this.world.func_234936_m_(p_234936_1_);
    }

    @Override
    public double func_234932_c_(BlockPos p_234932_1_, Predicate<BlockState> p_234932_2_) {
        return this.world.func_234932_c_(p_234932_1_, p_234932_2_);
    }

    @Override
    public double func_234928_a_(BlockPos p_234928_1_, double p_234928_2_) {
        return this.world.func_234928_a_(p_234928_1_, p_234928_2_);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return this.setBlockState(pos, newState, flags, 512);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int p_241211_4_) {
        if (pos.equals(this.tile.getPos())) {
            if (this.positive)
                this.tile.getPositiveBlockInfo().setBlockState(state);
            else
                this.tile.getNegativeBlockInfo().setBlockState(state);
            return true;
        } else {
            return super.setBlockState(pos, state, flags, p_241211_4_);
        }
    }

    @Override
    public boolean destroyBlock(BlockPos p_241212_1_, boolean p_241212_2_, @Nullable Entity p_241212_3_, int p_241212_4_) {
        return this.world.destroyBlock(p_241212_1_, p_241212_2_, p_241212_3_, p_241212_4_);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity p_230546_1_, @Nullable DamageSource p_230546_2_, @Nullable IExplosionContext p_230546_3_, double p_230546_4_, double p_230546_6_, double p_230546_8_, float p_230546_10_, boolean p_230546_11_, Explosion.Mode p_230546_12_) {
        return this.world.createExplosion(p_230546_1_, p_230546_2_, p_230546_3_, p_230546_4_, p_230546_6_, p_230546_8_, p_230546_10_, p_230546_11_, p_230546_12_);
    }

    @Override
    public boolean func_234929_a_(BlockPos p_234929_1_, Entity p_234929_2_, Direction p_234929_3_) {
        return this.world.func_234929_a_(p_234929_1_, p_234929_2_, p_234929_3_);
    }

    @Override
    protected void calculateInitialWeather() {
        super.calculateInitialWeather();
    }

    @Override
    public DimensionType func_230315_m_() {
        return super.func_230315_m_();
    }

    @Override
    public RegistryKey<DimensionType> func_234922_V_() {
        return super.func_234922_V_();
    }

    @Override
    public RegistryKey<World> func_234923_W_() {
        return super.func_234923_W_();
    }

    @Override
    public Supplier<IProfiler> func_234924_Y_() {
        return super.func_234924_Y_();
    }
}