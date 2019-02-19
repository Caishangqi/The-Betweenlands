package thebetweenlands.common.block.terrain;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Particles;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thebetweenlands.client.handler.ItemTooltipHandler;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.common.block.BlockStateContainerHelper;

public class BlockHearthgroveLog extends BlockLogBetweenlands {
	public static final BooleanProperty TARRED = BooleanProperty.create("tarred");

	public BlockHearthgroveLog() {
		this.setDefaultState(this.blockState.getBaseState().with(TARRED, false));
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		List<String> strings = ItemTooltipHandler.splitTooltip(I18n.format("tooltip.hearthgrove_log"), 0);
		if (stack.getMetadata() == 5 || stack.getMetadata() == 7)
			strings.remove(strings.size() - 1);
		tooltip.addAll(strings);
	}

	@Override
	public void animateTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		try(PooledMutableBlockPos checkPos = PooledMutableBlockPos.retain()) {
			boolean hasWater = false;
			for(EnumFacing offset : EnumFacing.Plane.HORIZONTAL) {
				IBlockState offsetState = worldIn.getBlockState(checkPos.setPos(pos.getX() + offset.getXOffset(), pos.getY(), pos.getZ() + offset.getZOffset()));
				IBlockState offsetStateDown = worldIn.getBlockState(checkPos.setPos(pos.getX() + offset.getXOffset(), pos.getY() - 1, pos.getZ() + offset.getZOffset()));
	
				if(offsetStateDown.getMaterial() == Material.WATER && offsetState.getMaterial() != Material.WATER) {
					if(rand.nextInt(8) == 0) {
						for(int i = 0; i < 5; i++) {
							float x = pos.getX() + (offset.getXOffset() > 0 ? 1.05F : offset.getXOffset() == 0 ? rand.nextFloat() : -0.05F);
							float y = pos.getY() - 0.1F;
							float z = pos.getZ() + (offset.getZOffset() > 0 ? 1.05F : offset.getZOffset() == 0 ? rand.nextFloat() : -0.05F);
	
							BLParticles.PURIFIER_STEAM.spawn(worldIn, x, y, z);
						}
					}
				}
	
				if(offsetState.getMaterial() == Material.WATER) {
					if(rand.nextInt(8) == 0) {
						for(int i = 0; i < 5; i++) {
							float x = pos.getX() + (offset.getXOffset() > 0 ? 1.1F : offset.getXOffset() == 0 ? rand.nextFloat() : -0.1F);
							float y = pos.getY() + rand.nextFloat();
							float z = pos.getZ() + (offset.getZOffset() > 0 ? 1.1F : offset.getZOffset() == 0 ? rand.nextFloat() : -0.1F);
	
							worldIn.spawnParticle(Particles.BUBBLE, x, y, z, 0, 0, 0);
						}
					}
					hasWater = true;
				}
			}
			if(!hasWater) {
				for(EnumFacing offset : EnumFacing.Plane.HORIZONTAL) {
					if(rand.nextFloat() < 0.04F) {
						checkPos.setPos(pos.getX() + offset.getXOffset(), pos.getY(), pos.getZ() + offset.getZOffset());
						IBlockState offsetState = worldIn.getBlockState(checkPos);
						if(!offsetState.isSideSolid(worldIn, checkPos, offset.getOpposite())) {
							float x = pos.getX() + (offset.getXOffset() > 0 ? 1.05F : offset.getXOffset() == 0 ? rand.nextFloat() : -0.05F);
							float y = pos.getY() + rand.nextFloat();
							float z = pos.getZ() + (offset.getZOffset() > 0 ? 1.05F : offset.getZOffset() == 0 ? rand.nextFloat() : -0.05F);
	
							switch(rand.nextInt(3)) {
							default:
							case 0:
								BLParticles.EMBER_1.spawn(worldIn, x, y, z);
								break;
							case 1:
								BLParticles.EMBER_2.spawn(worldIn, x, y, z);
								break;
							case 2:
								BLParticles.EMBER_3.spawn(worldIn, x, y, z);
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = this.getStateFromMeta(meta);
		if(state.get(LOG_AXIS) != BlockLog.EnumAxis.NONE) {
			return state.with(LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
		}
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		BlockLog.EnumAxis axis;

		int axisIndex = meta & 3;
		switch(axisIndex) {
		default:
			case 0:
				axis = BlockLog.EnumAxis.X;
				break;
			case 1:
				axis = BlockLog.EnumAxis.Y;
				break;
			case 2:
				axis = BlockLog.EnumAxis.Z;
				break;
			case 3:
				axis = BlockLog.EnumAxis.NONE;
				break;
		}

		boolean tarred = (meta >> 2) != 0;

		return this.getDefaultState().with(LOG_AXIS, axis).with(TARRED, tarred);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = 0;

		switch(state.get(LOG_AXIS)) {
			case X:
				meta = 0;
				break;
			case Y:
				meta = 1;
				break;
			case Z:
				meta = 2;
				break;
			case NONE:
				meta = 3;
				break;
		}

		meta |= state.get(TARRED) ? (1 << 2) : 0;

		return meta;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return BlockStateContainerHelper.extendBlockstateContainer(super.createBlockState(), new IProperty[] {TARRED});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.Y).with(TARRED, false))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.Y).with(TARRED, true))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.NONE).with(TARRED, false))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.NONE).with(TARRED, true))));
	}

	@Override
	public int damageDropped(IBlockState state) {
		BlockLog.EnumAxis axis = state.get(LOG_AXIS);
		if(axis == BlockLog.EnumAxis.X || axis == BlockLog.EnumAxis.Z) {
			state = state.with(LOG_AXIS, BlockLog.EnumAxis.Y);
		}
		return this.getMetaFromState(state);
	}

	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, IBlockState state) {
		return getSilkTouchDrop(state);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getSilkTouchDrop(state);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
	}

	@Override
	public int getSubtypeNumber() {
		return 4;
	}

	@Override
	public int getSubtypeMeta(int subtype) {
		switch(subtype) {
		default:
		case 0:
			return this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.Y).with(TARRED, false));
		case 1:
			return this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.Y).with(TARRED, true));
		case 2:
			return this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.NONE).with(TARRED, false));
		case 3:
			return this.getMetaFromState(this.getDefaultState().with(LOG_AXIS, BlockLog.EnumAxis.NONE).with(TARRED, true));
		}
	}

	@Override
	public String getSubtypeName(int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		String name = "%s";
		if(state.get(LOG_AXIS) == BlockLog.EnumAxis.NONE) {
			name = name + "_full";
		}
		if(state.get(TARRED)) {
			name = name + "_tarred";
		}
		return name;
	}

	@Override
	public ItemBlock getItemBlock() {
		ItemBlock item = new ItemBlock(this) {
			@Override
			public String getTranslationKey(ItemStack stack) {
				IBlockState state = this.block.getStateFromMeta(this.getMetadata(stack.getItemDamage()));
				return this.block.getTranslationKey() + (state.get(TARRED) ? "_tarred" : "");
			}

			@Override
			public int getMetadata(int damage) {
				return damage;
			}
		};
		item.setHasSubtypes(true);
		return item;
	}
}
