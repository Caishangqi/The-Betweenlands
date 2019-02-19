package thebetweenlands.common.entity.mobs;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import thebetweenlands.api.entity.IEntityBL;
import thebetweenlands.common.entity.ai.EntityAIFlyingWander;
import thebetweenlands.common.entity.ai.EntityAISeekRainShelter;
import thebetweenlands.common.entity.movement.FlightMoveHelper;
import thebetweenlands.common.registries.EntityRegistry;
import thebetweenlands.common.registries.LootTableRegistry;
import thebetweenlands.common.world.storage.BetweenlandsWorldStorage;

public class EntityFirefly extends EntityFlyingCreature implements IEntityBL {
	public static final IAttribute GLOW_STRENGTH_ATTRIB = (new RangedAttribute(null, "bl.fireflyGlowStrength", 1, 0, 8)).setDescription("Firefly glow strength").setShouldWatch(true);
	public static final IAttribute GLOW_START_CHANCE = (new RangedAttribute(null, "bl.fireflyGlowStartChance", 0.0025D, 0, 1)).setDescription("Firefly glow start chance per tick");
	public static final IAttribute GLOW_STOP_CHANCE = (new RangedAttribute(null, "bl.fireflyGlowStopChance", 0.00083D, 0, 1)).setDescription("Firefly glow stop chance per tick");

	private static final DataParameter<Float> GLOW_STRENGTH = EntityDataManager.<Float>createKey(EntityFirefly.class, DataSerializers.FLOAT);

	protected int glowTicks = 0;
	protected int prevGlowTicks = 0;

	public EntityFirefly(World world) {
		super(EntityRegistry.FIREFLY, world);
		this.setSize(0.6F, 0.6F);
		this.ignoreFrustumCheck = true;
		this.moveHelper = new FlightMoveHelper(this);
		setPathPriority(PathNodeType.WATER, -8F);
		setPathPriority(PathNodeType.BLOCKED, -8.0F);
		setPathPriority(PathNodeType.OPEN, 8.0F);
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		return this.world.isRainingAt(pos) ? -1.0F : 0.0F;
	}
	
	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISeekRainShelter(this, 0.8D));
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIFlyingWander(this, 0.5D));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.035D);
		this.getAttributeMap().registerAttribute(GLOW_STRENGTH_ATTRIB);
		this.getAttributeMap().registerAttribute(GLOW_START_CHANCE);
		this.getAttributeMap().registerAttribute(GLOW_STOP_CHANCE);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(GLOW_STRENGTH, (float)GLOW_STRENGTH_ATTRIB.getDefaultValue());
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LootTableRegistry.FIREFLY;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}

	@Override
	public void tick() {
		super.tick();

		if(getEntityWorld().getBlockState(getPosition().down()).isSideSolid(getEntityWorld(), getPosition().down(), EnumFacing.UP))
			getMoveHelper().setMoveTo(this.posX, this.posY + 1, this.posZ, 0.32D);

		this.prevGlowTicks = this.glowTicks;

		if(this.isAlive()) {
			if(!this.world.isRemote()) {
				if(!this.isGlowActive() && this.rand.nextDouble() < this.getAttribute(GLOW_START_CHANCE).getValue()) {
					this.setGlowStrength(this.getAttribute(GLOW_STRENGTH_ATTRIB).getValue());
				} else if(this.isGlowActive() && this.rand.nextDouble() < this.getAttribute(GLOW_STOP_CHANCE).getValue()) {
					this.setGlowStrength(0);
				}
			}

			if(this.isGlowActive() && this.glowTicks < 20) {
				this.glowTicks++;
			} else if(!this.isGlowActive() && this.glowTicks > 0) {
				this.glowTicks--;
			}
		} else {
			this.setGlowStrength(0);
			this.glowTicks = 0;
		}
	}

	@Override
	public boolean canDespawn() {
		return true;
	}
	
	@Override
	public boolean canSpawn(IWorld world, boolean spawner) {
		float brightness = this.world.dimension.getSunBrightnessFactor(1);
		return (brightness <= 0.3F || BetweenlandsWorldStorage.forWorld(this.world).getEnvironmentEventRegistry().bloodSky.isActive()) && super.canSpawn(world, spawner);
	}

	@Override
	public void writeAdditional(NBTTagCompound nbt) {
		super.writeAdditional(nbt);
		nbt.setDouble("glowStrength", this.getGlowStrength());
	}

	@Override
	public void readAdditional(NBTTagCompound nbt) {
		super.readAdditional(nbt);
		this.setGlowStrength(nbt.getDouble("glowStrength"));
	}

	/**
	 * Returns whether the glow is active
	 * @return
	 */
	public boolean isGlowActive() {
		return this.getGlowStrength() > 0;
	}

	/**
	 * Returns the glow strength
	 * @return
	 */
	public double getGlowStrength() {
		return this.getDataManager().get(GLOW_STRENGTH);
	}

	/**
	 * Sets the glow strength
	 * @param strength
	 */
	public void setGlowStrength(double strength) {
		this.getDataManager().set(GLOW_STRENGTH, (float)strength);
	}

	/**
	 * Returns the interpolated glow ticks
	 * @param partialTicks
	 * @return
	 */
	public float getGlowTicks(float partialTicks) {
		return this.prevGlowTicks + (this.glowTicks - this.prevGlowTicks) * partialTicks;
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0 || pass == 1;
	}
}
