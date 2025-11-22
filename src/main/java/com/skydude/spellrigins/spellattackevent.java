package com.skydude.spellrigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.edwinmindcraft.apoli.api.ApoliAPI;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.apace100.origins.command.OriginCommand;
import io.github.apace100.origins.command.OriginArgumentType;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.github.apace100.origins.command.OriginArgumentType.getOrigin;


@Mod.EventBusSubscriber
public class spellattackevent {


    @SubscribeEvent
    public static void spelldmgevent(SpellDamageEvent event) {
        if (!(event.getSpellDamageSource().getEntity() instanceof LivingEntity attacker)) return;

        if (attacker instanceof ServerPlayer player) {

            // ommand source
            CommandSourceStack silentSource = player.createCommandSourceStack()
                    .withSuppressedOutput() // suppress success/failure feedback
                    .withPermission(4);     // make sure it can run admin commands



            // command run
            if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:fire/spellflame") == 1){
                event.getEntity().setSecondsOnFire(10);

            }  if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:spellfreeze") == 1){
                event.getEntity().setTicksFrozen(10);

            }
            if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:nature/naturesblight") == 1){
                event.getEntity().addEffect(new MobEffectInstance(MobEffectRegistry.BLIGHT.get(), 20000, 2));
                System.out.println("de");

            }
            if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:holy/smite") == 1) {
                if(event.getEntity().getMobType() == MobType.UNDEAD) {
                    event.getEntity().hurt(event.getEntity().damageSources().magic(), (float) (event.getAmount() * 0.1));
                }
            }
            if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:lightning/lightningsummon") == 1) {
                if (Math.random() < 0.3 * attacker.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get())) {


                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + (event.getEntity().getX() + 5) + " " + event.getEntity().getY() + " " + event.getEntity().getZ());
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + (event.getEntity().getX() + 5) + " " + event.getEntity().getY() + " " + event.getEntity().getZ());
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + event.getEntity().getX() + " " + event.getEntity().getY() + " " + (event.getEntity().getZ() + 5));
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + event.getEntity().getX() + " " + event.getEntity().getY() + " " + (event.getEntity().getZ() - 5));
                } else {
                    player.getServer().getCommands().performPrefixedCommand(silentSource, "summon minecraft:lightning_bolt " + player.getX() + " " + player.getY() + " " + player.getZ());
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
}