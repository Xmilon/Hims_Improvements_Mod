package net.xmilon.himproveme.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class StasisChamberBlock extends Block {

    public StasisChamberBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.isClient()) {
            if (random.nextFloat() < 0.14f) {
                world.playSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        SoundEvents.BLOCK_BEACON_AMBIENT,
                        SoundCategory.BLOCKS,
                        0.17f,
                        0.62f + random.nextFloat() * 0.06f,
                        false
                );
            }

            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
                double y = pos.getY() + 0.3 + random.nextDouble() * 0.4;
                double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
                double dx = (random.nextDouble() - 0.5) * 0.05;
                double dy = random.nextDouble() * 0.05;
                double dz = (random.nextDouble() - 0.5) * 0.05;
                world.addParticle(ParticleTypes.PORTAL, x, y, z, dx, dy, dz);
            }
        }
    }
}
