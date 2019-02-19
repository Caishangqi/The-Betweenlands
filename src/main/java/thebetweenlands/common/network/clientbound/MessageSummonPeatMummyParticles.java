package thebetweenlands.common.network.clientbound;

import javax.xml.ws.handler.MessageContext;

import net.minecraft.entity.Entity;
import net.minecraft.init.Particles;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thebetweenlands.common.network.MessageEntity;
import thebetweenlands.common.registries.BlockRegistry;

public class MessageSummonPeatMummyParticles extends MessageEntity {
	public MessageSummonPeatMummyParticles() { }

	public MessageSummonPeatMummyParticles(Entity entity) {
		this.addEntity(entity);
	}

	@Override
	public IMessage process(MessageContext ctx) {
		super.process(ctx);

		if(ctx.side == Dist.CLIENT) {
			this.handle();
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	private void handle() {
		Entity entity = this.getEntity(0);
		if(entity != null) {
			for (int i = 0; i < 250; i++) {
				double px = entity.posX - 0.75F + entity.world.rand.nextFloat() * 1.5F;
				double py = entity.posY - 2.0F + entity.world.rand.nextFloat() * 4.0F;
				double pz = entity.posZ - 0.75F + entity.world.rand.nextFloat() * 1.5F;
				Vec3d vec = new Vec3d(px, py, pz).subtract(new Vec3d(entity.posX + 0.35F, entity.posY + 1.1F, entity.posZ + 0.35F)).normalize();
				entity.world.spawnParticle(new BlockParticleData(Particles.BLOCK, BlockRegistry.MUD.getDefaultState()), px, py, pz, vec.x * 0.25F, vec.y * 0.25F, vec.z * 0.25F);
			}
		}
	}
}
