package cjminecraft.doubleslabs.common.blocks;

import cjminecraft.doubleslabs.api.ContainerSupport;
import cjminecraft.doubleslabs.api.IBlockInfo;
import cjminecraft.doubleslabs.api.IContainerSupport;
import cjminecraft.doubleslabs.api.SlabSupport;
import cjminecraft.doubleslabs.api.support.ISlabSupport;
import cjminecraft.doubleslabs.common.init.DSItems;
import cjminecraft.doubleslabs.common.tileentity.SlabTileEntity;
import cjminecraft.doubleslabs.old.Utils;
import cjminecraft.doubleslabs.old.network.NetworkUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class VerticalSlabBlock extends DynamicSlabBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty DOUBLE = BooleanProperty.create("double");

    public VerticalSlabBlock() {
        super();
        setDefaultState(this.getStateContainer().getBaseState().with(WATERLOGGED, false).with(DOUBLE, false).with(FACING, Direction.NORTH));
    }

    protected static Optional<IBlockInfo> getHalfState(IBlockReader world, BlockPos pos, double x, double z) {
        BlockState state = world.getBlockState(pos);

        return getTile(world, pos).flatMap(tile -> tile.getPositiveBlockInfo().getBlockState() == null && tile.getNegativeBlockInfo().getBlockState() == null ? Optional.empty() :
                ((state.get(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                        (state.get(FACING).getAxis() == Direction.Axis.X ? x : z) > 0.5 :
                        (state.get(FACING).getAxis() == Direction.Axis.X ? x : z) < 0.5)
                        || tile.getNegativeBlockInfo().getBlockState() == null) && tile.getPositiveBlockInfo().getBlockState() != null ?
                        Optional.of(tile.getPositiveBlockInfo()) : Optional.of(tile.getNegativeBlockInfo()));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, DOUBLE);
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        BlockState state = context.getWorld().getBlockState(pos);
        if (state.isIn(this)) {
            // same block as this meaning we are trying to combine slabs
            if (isReplaceable(state, context)) {
                return state.with(DOUBLE, true);
            }
            return state;
        }
        BlockState newState = super.getStateForPlacement(context);
        if (context.getFace().getAxis().isVertical()) {
            boolean positive = context.getPlacementHorizontalFacing().getAxis() == Direction.Axis.X ? context.getHitVec().x - (double)context.getPos().getX() > 0.5d : context.getHitVec().z - (double)context.getPos().getZ() > 0.5d;
            return newState.with(FACING, positive ? context.getPlacementHorizontalFacing() : context.getPlacementHorizontalFacing().getOpposite());
        }
        double value = context.getPlacementHorizontalFacing().getAxis() == Direction.Axis.Z ? context.getHitVec().x - (double)context.getPos().getX() : context.getHitVec().z - (double)context.getPos().getZ();
        if (value > 0.25d && value < 0.75d)
            return newState.with(FACING, context.getPlacementHorizontalFacing());
        boolean positive = context.getPlacementHorizontalFacing().getAxisDirection() == Direction.AxisDirection.POSITIVE ? value > 0.5d : value < 0.5d;
        return newState.with(FACING, positive ? context.getPlacementHorizontalFacing().rotateY() : context.getPlacementHorizontalFacing().rotateYCCW());
    }

    @Override
    public Item asItem() {
        return DSItems.VERTICAL_SLAB.get();
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        ItemStack stack = useContext.getItem();
        if (!state.get(DOUBLE) && stack.getItem() == this.asItem()) {
            if (useContext.replacingClickedOnBlock()) {
                Direction direction = state.get(FACING);
                return getTile(useContext.getWorld(), useContext.getPos()).map(tile -> {
//                    boolean positiveX = useContext.getHitVec().x - (double) useContext.getPos().getX() > 0.5d;
//                    boolean positiveZ = useContext.getHitVec().z - (double) useContext.getPos().getZ() > 0.5d;
                    boolean positive = tile.getPositiveBlockInfo().getBlockState() != null;
                    return (useContext.getFace() == direction.getOpposite() && positive) || (useContext.getFace() == direction && !positive);
                }).orElse(false);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (state.get(DOUBLE))
            return VoxelShapes.fullCube();

        double min = 0;
        double max = 8;
        if (state.get(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            min = 8;
            max = 16;
        }

        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity instanceof SlabTileEntity) {
            SlabTileEntity tile = (SlabTileEntity) tileEntity;

            boolean positive = tile.getPositiveBlockInfo().getBlockState() != null;
            boolean negative = tile.getNegativeBlockInfo().getBlockState() != null;

            if ((state.get(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE && positive) || (state.get(FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE && negative)) {
                min = 8;
                max = 16;
            } else {
                min = 0;
                max = 8;
            }
        }

        if (state.get(FACING).getAxis() == Direction.Axis.X)
            return Block.makeCuboidShape(min, 0, 0, max, 16, 16);
        else
            return Block.makeCuboidShape(0, 0, min, 16, 16, max);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return getTile(world, pos).map(tile -> tile.getPositiveBlockInfo().getBlockState() == null || tile.getPositiveBlockInfo().getBlockState().propagatesSkylightDown(tile.getPositiveBlockInfo().getWorld(), pos) || tile.getNegativeBlockInfo().getBlockState() == null || tile.getNegativeBlockInfo().getBlockState().propagatesSkylightDown(tile.getNegativeBlockInfo().getWorld(), pos)).orElse(false);
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        RayTraceResult rayTraceResult = Utils.rayTrace(player);
        Vector3d hitVec = rayTraceResult.getType() == RayTraceResult.Type.BLOCK ? rayTraceResult.getHitVec() : null;
        if (hitVec == null)
            return minFloat(world, pos, i -> i.getBlockState().getPlayerRelativeBlockHardness(player, i.getWorld(), pos));
        return getHalfState(world, pos, hitVec.x - pos.getX(), hitVec.z - pos.getZ())
                .map(i -> i.getBlockState().getPlayerRelativeBlockHardness(player, i.getWorld(), pos))
                .orElse(super.getPlayerRelativeBlockHardness(state, player, world, pos));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getHalfState(world, pos, target.getHitVec().x - pos.getX(), target.getHitVec().z - pos.getZ()).map(i -> i.getBlockState().getPickBlock(target, i.getWorld(), pos, player)).orElse(ItemStack.EMPTY);
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        RayTraceResult rayTraceResult = Utils.rayTrace(player);
        Vector3d hitVec = rayTraceResult.getType() == RayTraceResult.Type.BLOCK ? rayTraceResult.getHitVec() : null;
        if (hitVec == null || te == null) {
            super.harvestBlock(world, player, pos, state, te, stack);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            world.removeTileEntity(pos);
        } else {
            if (state.get(DOUBLE)) {
                SlabTileEntity tile = (SlabTileEntity) te;

                double distance = state.get(FACING).getAxis() == Direction.Axis.X ? hitVec.x - (double) pos.getX() : hitVec.z - (double) pos.getZ();

                boolean positive = state.get(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? distance > 0.5 : distance < 0.5;

                IBlockInfo blockToRemove = positive ? tile.getPositiveBlockInfo() : tile.getNegativeBlockInfo();
//                BlockState stateToRemove = positive ? tile.getPositiveBlockInfo().getBlockState() : tile.getNegativeBlockInfo().getBlockState();

                player.addStat(Stats.BLOCK_MINED.get(blockToRemove.getBlockState().getBlock()));
                world.playEvent(2001, pos, Block.getStateId(blockToRemove.getBlockState()));
                player.addExhaustion(0.005F);

                if (!player.abilities.isCreativeMode)
                    spawnDrops(blockToRemove.getBlockState(), world, pos, null, player, stack);

                blockToRemove.getBlockState().onReplaced(blockToRemove.getWorld(), pos, Blocks.AIR.getDefaultState(), false);

                blockToRemove.setBlockState(null);
//                if (positive)
//                    tile.getPositiveBlockInfo().setBlockState(null);
//                else
//                    tile.getNegativeBlockInfo().setBlockState(null);

                world.setBlockState(pos, state.with(DOUBLE, false), Constants.BlockFlags.DEFAULT);
            } else {
                SlabTileEntity tile = (SlabTileEntity) te;
                boolean positive = tile.getPositiveBlockInfo().getBlockState() != null;
                IBlockInfo blockToRemove = positive ? tile.getPositiveBlockInfo() : tile.getNegativeBlockInfo();
//                BlockState remainingState = positive ? tile.getPositiveBlockInfo().getBlockState() : tile.getNegativeBlockInfo().getBlockState();
                player.addStat(Stats.BLOCK_MINED.get(blockToRemove.getBlockState().getBlock()));
                world.playEvent(2001, pos, Block.getStateId(blockToRemove.getBlockState()));
                player.addExhaustion(0.005F);

                if (!player.abilities.isCreativeMode)
                    spawnDrops(blockToRemove.getBlockState(), world, pos, null, player, stack);

                blockToRemove.getBlockState().onReplaced(blockToRemove.getWorld(), pos, Blocks.AIR.getDefaultState(), false);

                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                world.removeTileEntity(pos);
            }
        }
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        float f = (float) MathHelper.ceil(entity.fallDistance - 3.0F);
        double d0 = Math.min((0.2F + f / 15.0F), 2.5D);
        final int numOfParticles = state1.get(DOUBLE) ? (int) (75.0D * d0) : (int) (150.0D * d0);
        runIfAvailable(worldserver, pos, i -> worldserver.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, i.getBlockState()), entity.getPosX(), entity.getPosY(), entity.getPosZ(), numOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D));
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isRemote) {
            runIfAvailable(world, pos, i -> world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, i.getBlockState()),
                    entity.getPosX() + ((double) world.rand.nextFloat() - 0.5D) * (double) entity.getWidth(),
                    entity.getBoundingBox().minY + 0.1D,
                    entity.getPosZ() + ((double) world.rand.nextFloat() - 0.5D) * (double) entity.getWidth(),
                    -entity.getMotion().x * 4.0D, 1.5D, -entity.getMotion().z * 4.0D));
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        if (target.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult result = (BlockRayTraceResult) target;
            return getHalfState(world, result.getPos(), target.getHitVec().x, target.getHitVec().z).map(info -> {
                BlockPos pos = result.getPos();
                Direction side = result.getFace();

                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();

                AxisAlignedBB axisalignedbb = state.getCollisionShape(world, pos).getBoundingBox();
                double d0 = (double) i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
                double d1 = (double) j + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
                double d2 = (double) k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

                switch (side) {
                    case DOWN:
                        d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
                        break;
                    case UP:
                        d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
                        break;
                    case NORTH:
                        d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
                        break;
                    case SOUTH:
                        d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
                        break;
                    case WEST:
                        d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
                        break;
                    case EAST:
                        d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
                }

                DiggingParticle.Factory factory = new DiggingParticle.Factory();

                Particle particle = factory.makeParticle(new BlockParticleData(ParticleTypes.BLOCK, info.getBlockState()), (ClientWorld) world, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                if (particle != null) {
                    ((DiggingParticle) particle).setBlockPos(pos);
                    particle = particle.multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F);
                    manager.addEffect(particle);
                    return true;
                }

                return false;
            }).orElse(false);
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return getTile(world, pos).map(tile -> {
            DiggingParticle.Factory factory = new DiggingParticle.Factory();
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < 4; l++) {
                        double d0 = ((double) j + 0.5D) / 4.0D + pos.getX();
                        double d1 = ((double) k + 0.5D) / 4.0D + pos.getY();
                        double d2 = ((double) l + 0.5D) / 4.0D + pos.getZ();

                        runIfAvailable(world, pos, i -> {
                            Particle particle = factory.makeParticle(new BlockParticleData(ParticleTypes.BLOCK, i.getBlockState()), (ClientWorld) world, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                            if (particle != null)
                                manager.addEffect(particle);
                        });
                    }
                }
            }
            return true;
        }).orElse(false);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (state.getBlock() != this)
            return ActionResultType.PASS;
        return getHalfState(world, pos, hit.getHitVec().x - pos.getX(), hit.getHitVec().z - pos.getZ()).map(i -> {
            IContainerSupport support = ContainerSupport.getSupport(i.getWorld(), pos, i.getBlockState());
            if (support == null) {
                ActionResultType result;
                ISlabSupport slabSupport = SlabSupport.getSlabSupport(world, pos, i.getBlockState());
//                ISlabSupport slabSupport = SlabSupportOld.getHorizontalSlabSupport(world, pos, i.getBlockState());
//                if (slabSupport == null)
//                    slabSupport = SlabSupportOld.getVerticalSlabSupport(world, pos, i.getBlockState());
                try {
                    result = slabSupport == null ? i.getBlockState().onBlockActivated(i.getWorld(), player, hand, hit) : slabSupport.onBlockActivated(i.getBlockState(), i.getWorld(), pos, player, hand, hit);
                } catch (Exception e) {
                    result = ActionResultType.PASS;
                }
                return result;
            } else {
                if (!world.isRemote) {
                    NetworkUtils.openGui((ServerPlayerEntity) player, support.getNamedContainerProvider(i.getWorld(), pos, i.getBlockState(), player, hand, hit), pos, i.isPositive());
                    support.onClicked(i.getWorld(), pos, i.getBlockState(), player, hand, hit);
                }
                return ActionResultType.SUCCESS;
            }
        }).orElse(ActionResultType.PASS);
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        BlockRayTraceResult result = Utils.rayTrace(player);
        if (result.getHitVec() != null)
            getHalfState(world, pos, result.getHitVec().x - pos.getX(), result.getHitVec().z - pos.getZ())
                    .ifPresent(i -> i.getBlockState().onBlockClicked(i.getWorld(), pos, player));
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (!getHalfState(world, pos, entity.getPosX() - pos.getX(), entity.getPosZ() - pos.getZ()).map(i -> {
            i.getBlockState().getBlock().onFallenUpon(i.getWorld(), pos, entity, fallDistance);
            return true;
        }).orElse(false))
            super.onFallenUpon(world, pos, entity, fallDistance);
    }

    @Override
    public void onLanded(IBlockReader world, Entity entity) {
        BlockPos pos = new BlockPos(entity.getPositionVec()).down();
        if (world.getBlockState(pos).getBlock() == this)
            if (!getHalfState(world, pos, entity.getPosX() - pos.getX(), entity.getPosZ() - pos.getZ()).map(i -> {
                i.getBlockState().getBlock().onLanded(i.getWorld(), entity);
                return true;
            }).orElse(false))
                super.onLanded(world, entity);
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!getHalfState(world, pos, entity.getPosX() - pos.getX(), entity.getPosZ() - pos.getZ()).map(i -> {
            i.getBlockState().getBlock().onEntityWalk(i.getWorld(), pos, entity);
            return true;
        }).orElse(false))
            super.onEntityWalk(world, pos, entity);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
        return state.get(DOUBLE);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        getHalfState(world, pos, entity.getPosX() - pos.getX(), entity.getPosZ() - pos.getZ()).ifPresent(i -> i.getBlockState().onEntityCollision(i.getWorld(), pos, entity));
    }

    @Override
    public void onProjectileCollision(World world, BlockState state, BlockRayTraceResult hit, ProjectileEntity projectile) {
        getHalfState(world, hit.getPos(), hit.getHitVec().x, hit.getHitVec().z).ifPresent(i -> i.getBlockState().onProjectileCollision(i.getWorld(), i.getBlockState(), hit, projectile));
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        if (entity != null)
            return getHalfState(world, pos, entity.getPosX() - pos.getX(), entity.getPosZ() - pos.getZ()).map(i -> i.getBlockState().getSoundType(i.getWorld(), pos, entity)).orElse(super.getSoundType(state, world, pos, entity));
        return getAvailable(world, pos).map(i -> i.getBlockState().getSoundType(i.getWorld(), pos, entity)).orElse(super.getSoundType(state, world, pos, null));
    }
}
