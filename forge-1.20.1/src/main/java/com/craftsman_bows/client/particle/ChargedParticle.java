package com.craftsman_bows.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChargedParticle extends SimpleAnimatedParticle {

    protected ChargedParticle(ClientLevel clientWorld, double d, double e, double f, SpriteSet spriteProvider) {
        super(clientWorld, d, e, f, spriteProvider, 0.0125F);
        this.alpha = 0.7F;
        this.lifetime = 3;
        this.quadSize = 0.3F;
        this.setSpriteFromAge(spriteProvider);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Provider(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double d, double e, double f,
                                       double g, double h, double i) {
            return new ChargedParticle(world, d, e, f, this.spriteProvider);
        }
    }
}
