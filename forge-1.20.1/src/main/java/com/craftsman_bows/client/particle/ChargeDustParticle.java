package com.craftsman_bows.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChargeDustParticle extends SimpleAnimatedParticle {
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    protected ChargeDustParticle(ClientLevel clientWorld,
                                 double startX, double startY, double startZ,
                                 double targetX, double targetY, double targetZ,
                                 SpriteSet spriteProvider) {
        super(clientWorld, startX, startY, startZ, spriteProvider, 0.0125F);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.lifetime = 5;
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            double directionX = targetX - this.x;
            double directionY = targetY - this.y;
            double directionZ = targetZ - this.z;

            double distance = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);

            if (distance > 0.1) {
                directionX /= distance;
                directionY /= distance;
                directionZ /= distance;

                // 収束する速度
                double speed = 0.2;
                this.x += directionX * speed;
                this.y += directionY * speed;
                this.z += directionZ * speed;
            } else {
                this.x = targetX;
                this.y = targetY;
                this.z = targetZ;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Provider(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double startX, double startY, double startZ,
                                       double targetX, double targetY, double targetZ) {
            ChargeDustParticle particle =
                    new ChargeDustParticle(world, startX, startY, startZ, targetX, targetY, targetZ, spriteProvider);
            particle.pickSprite(this.spriteProvider);
            return particle;
        }
    }
}
