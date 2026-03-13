package com.craftai4u.lightning_tnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

@Mod("lightning_tnt")
public class LightningTNTMod {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "lightning_tnt");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "lightning_tnt");

    public static final RegistryObject<Block> LIGHTNING_TNT = BLOCKS.register("lightning_tnt",
        () -> new TntBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak()) {
            @Override
            public void onCaughtFire(BlockState state, Level level, BlockPos pos, boolean isPlayer) {
                if (!level.isClientSide) {
                    PrimedTnt primedTnt = new LightningTntEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null);
                    level.addFreshEntity(primedTnt);
                    level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        });

    public static final RegistryObject<Item> LIGHTNING_TNT_ITEM = ITEMS.register("lightning_tnt",
        () -> new BlockItem(LIGHTNING_TNT.get(), new Item.Properties()));

    public LightningTNTMod() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void addCreativeTabItems(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(LIGHTNING_TNT_ITEM);
        }
    }

    public static class LightningTntEntity extends PrimedTnt {
        private static final int EXPLOSION_RADIUS = 10;
        private static final int LIGHTNING_DURATION_TICKS = 600; // 30 seconds
        private int lightningTicks = LIGHTNING_DURATION_TICKS;

        public LightningTntEntity(Level level, double x, double y, double z, Entity igniter) {
            super(level, x, y, z, igniter);
        }

        @Override
        public void tick() {
            super.tick();
            if (this.lightningTicks > 0) {
                this.lightningTicks--;
                if (!this.level.isClientSide) {
                    Random random = new Random();
                    for (int i = 0; i < 5; i++) { // Multiple lightning strikes per tick
                        double offsetX = random.nextDouble() * 100 - 50;
                        double offsetZ = random.nextDouble() * 100 - 50;
                        BlockPos strikePos = this.blockPosition().offset((int)offsetX, 0, (int)offsetZ);
                        this.level.strikeLightning(strikePos);
                    }
                }
            }
        }

        @Override
        protected void explode() {
            this.level.explode(this, this.getX(), this.getY(0.0625D), this.getZ(), EXPLOSION_RADIUS, Explosion.BlockInteraction.DESTROY);
        }
    }
}