package com.skydude.spellrigins;

import io.github.edwinmindcraft.origins.common.OriginsEventHandler;
import io.github.edwinmindcraft.origins.common.condition.OriginCondition;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;


import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.SummonedVex;
import io.redspace.ironsspellbooks.network.ServerboundLearnSpell;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;


@Mod.EventBusSubscriber
public class ModEvents {


    @SubscribeEvent
    public static void spelldmgevent(SpellDamageEvent event) {
        if (!(event.getSpellDamageSource().getEntity() instanceof LivingEntity attacker)) return;

        if (attacker instanceof ServerPlayer player) {

            // ommand source
            CommandSourceStack silentSource = player.createCommandSourceStack()
                    .withSuppressedOutput() // suppress success/failure feedback
                    .withPermission(4);     // make sure it can run admin commands


            // command run
            if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:fire/spellflame") == 1) {
                event.getEntity().setSecondsOnFire(10);

            } else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:spellfreeze") == 1) {
                event.getEntity().setTicksFrozen(10);

            } else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:nature/naturesblight") == 1) {
                event.getEntity().addEffect(new MobEffectInstance(MobEffectRegistry.BLIGHT.get(), 20000, 2));
                System.out.println("de");

            } else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:holy/smite") == 1) {
                if (event.getEntity().getMobType() == MobType.UNDEAD) {
                    event.getEntity().hurt(event.getEntity().damageSources().magic(), (float) (event.getAmount() * 0.1));
                }
            } else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:lightning/lightningsummon") == 1) {
              // 30% chance * lightning power
                if (Math.random() <= 0.3 * attacker.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get())) {


                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + (event.getEntity().getX() + 5) + " " + event.getEntity().getY() + " " + event.getEntity().getZ());
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + (event.getEntity().getX() + 5) + " " + event.getEntity().getY() + " " + event.getEntity().getZ());
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + event.getEntity().getX() + " " + event.getEntity().getY() + " " + (event.getEntity().getZ() + 5));
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + event.getEntity().getX() + " " + event.getEntity().getY() + " " + (event.getEntity().getZ() - 5));
                } else {
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + player.getX() + " " + player.getY() + " " + player.getZ());
                }
            } else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:blood/devour") == 1) {
                if (Math.random() <= 0.1 * attacker.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER.get())) {
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "cast " +  player.getName().getString() + " devour " + 3);

                }
            }  else if (player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:evocation/evocationsummon") == 1) {

                if (Math.random() <= 0.1 * attacker.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER.get())) {
                    // if the cooldown is above ~9 minutes, cancel the summon to not have 10 billion summons
                    if (player.getEffect(MobEffectRegistry.VEX_TIMER.get()) != null && player.getEffect(MobEffectRegistry.VEX_TIMER.get()).getDuration() >= 11700) {
                        return;
                    }
                    int summonTime = 20 * 60 * 10;
                    // summon 2 vex
                    for (int i = 0; i < 2; i++) {
                        SummonedVex vex = new SummonedVex(player.level(), player);
                        vex.moveTo(player.getEyePosition().add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
                        vex.finalizeSpawn((ServerLevel) player.level(), player.level().getCurrentDifficultyAt(vex.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
                        vex.addEffect(new MobEffectInstance(MobEffectRegistry.VEX_TIMER.get(), 12000, 0, false, false, false));
                        player.level().addFreshEntity(vex);
                    }

                    int effectAmplifier = 2 - 1;
                    if (player.hasEffect(MobEffectRegistry.VEX_TIMER.get()))
                        effectAmplifier += player.getEffect(MobEffectRegistry.VEX_TIMER.get()).getAmplifier() + 1;
                    player.addEffect(new MobEffectInstance(MobEffectRegistry.VEX_TIMER.get(), summonTime, effectAmplifier, false, false, true));

                }
            }
        }
    }
  @SubscribeEvent
    public static void spellcast(SpellPreCastEvent event){
      if(event.getEntity().getServer().getCommands().performPrefixedCommand(event.getEntity().createCommandSourceStack().withSuppressedOutput().withPermission(4), "power has " + event.getEntity().getName().getString() + " spellrigins:holy/bowonlytogod") == 1) {

          if (event.getSchoolType() == SchoolRegistry.BLOOD.get() | event.getSchoolType() == SchoolRegistry.ELDRITCH.get()) {
              event.setCanceled(true);
              event.getEntity().sendSystemMessage(Component.literal("Bow only to god"));
          }
      }
      if(event.getEntity().getServer().getCommands().performPrefixedCommand(event.getEntity().createCommandSourceStack().withSuppressedOutput().withPermission(4), "power has " + event.getEntity().getName().getString() + " spellrigins:nospells") == 1) {
          if (!Objects.equals(event.getSpellId(), "irons_spellbooks:counterspell")) {
              event.setCanceled(true);
              System.out.println(event.getSpellId());
          }
      }
  }
    @SubscribeEvent
    public static void takelightningdamage(LivingDamageEvent event) {
        if (event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
            // server only
            if (event.getEntity() instanceof ServerPlayer player) {

                if (player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput() .withPermission(4) , "power has " + player.getName().getString() + " spellrigins:lightning/lightningsummon") == 1) {

                    event.setAmount(event.getAmount() / 3);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
            // server only
            if (event.getEntity() instanceof ServerPlayer player) {
                if (player.getServer().getCommands().performPrefixedCommand(event.getEntity().createCommandSourceStack().withSuppressedOutput().withPermission(4), "power has " + player.getName().getString() + " spellrigins:ender/enderbility") == 1) {

                    player.addEffect(new MobEffectInstance(MobEffectRegistry.EVASION.get(), (int) (300 * player.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER.get())), 1, false, false, true));
                }

            }
    }
    @SubscribeEvent
    public static void onEndermanAnger(EnderManAngerEvent event) {
        // server only
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (player.getServer().getCommands().performPrefixedCommand(event.getEntity().createCommandSourceStack().withSuppressedOutput().withPermission(4), "power has " + player.getName().getString() + " spellrigins:ender/enderspellresist") == 1) {
            event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public static void onRecieveEffect(MobEffectEvent.Applicable event) {
        // server only
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getServer().getCommands().performPrefixedCommand(event.getEntity().createCommandSourceStack().withSuppressedOutput().withPermission(4), "power has " + player.getName().getString() + " spellrigins:icespellresist") == 1) {

                if (event.getEffectInstance().getEffect() == MobEffectRegistry.CHILLED.get()) {
                    event.setCanceled(true);
                }
            }
        }
    }

}