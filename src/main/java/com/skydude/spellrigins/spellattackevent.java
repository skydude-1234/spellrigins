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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
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
            if(player.getServer().getCommands().performPrefixedCommand(silentSource, "power has " + player.getName().getString() + " spellrigins:spellflame") == 1){
                event.getEntity().setSecondsOnFire(10);
            }





        }
    }


}