package com.skydude.spellrigins

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent
import io.redspace.ironsspellbooks.api.magic.MagicData
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry
import io.redspace.ironsspellbooks.api.spells.AbstractSpell
import io.redspace.ironsspellbooks.api.util.Utils
import io.redspace.ironsspellbooks.entity.mobs.SummonedVex
import io.redspace.ironsspellbooks.registries.MobEffectRegistry
import io.redspace.ironsspellbooks.spells.fire.HeatSurgeSpell
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.MobType
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.phys.Vec3
import net.minecraftforge.event.entity.EntityTeleportEvent
import net.minecraftforge.event.entity.living.EnderManAngerEvent
import net.minecraftforge.event.entity.living.LivingDamageEvent
import net.minecraftforge.event.entity.living.MobEffectEvent.Applicable
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import org.jetbrains.annotations.NotNull

@EventBusSubscriber
object ModEvents {
    @SubscribeEvent
    fun spelldmgevent(event: SpellDamageEvent) {
        if (event.spellDamageSource.entity !is LivingEntity) return
        if (event.spellDamageSource.entity is ServerPlayer) {
            val attacker = event.spellDamageSource.entity as ServerPlayer
            // ommand source

            val silentSource: CommandSourceStack = attacker.createCommandSourceStack()
                .withSuppressedOutput() // suppress success/failure feedback
                .withPermission(4) // make sure it can run admin commands


            // command run
            if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:fire/spellflame"
                ) == 1
            ) {
                event.entity
                    .setSecondsOnFire((10 * attacker.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER.get())).toInt())
                if (Math.random() <= 0.3 * attacker.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER.get())) {
                    val spell: AbstractSpell = (HeatSurgeSpell())
                    spell.castSpell(
                        attacker.level(),
                        3,
                        attacker,
                        MagicData.getPlayerMagicData(attacker).getCastSource(),
                        false
                    )
                }
            } else if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:spellfreeze"
                ) == 1
            ) {
                event.entity.ticksFrozen = 10
            } else if (attacker.getServer()!!.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:nature/naturesblight"
                ) == 1
            ) {
                event.entity.addEffect(MobEffectInstance(MobEffectRegistry.BLIGHT.get(), 20000, 2))
                println("de")
            } else if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:holy/smite"
                ) == 1
            ) {
                if (event.entity.mobType === MobType.UNDEAD) {
                    event.setAmount((event.amount * 2))
                }
            } else if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:lightning/lightningsummon"
                ) == 1
            ) {
                // 30% chance * lightning power
                if (Math.random() <= 0.3 * attacker.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get())) {
                    attacker.server.commands.performPrefixedCommand(
                        silentSource,
                        "summon minecraft:lightning_bolt " + (event.entity.x + 5) + " " + event.entity
                            .y + " " + event.entity.z
                    )
                    attacker.server.commands.performPrefixedCommand(
                        silentSource,
                        "summon minecraft:lightning_bolt " + (event.entity.x + 5) + " " + event.entity
                            .y + " " + event.entity.z
                    )
                    attacker.server.commands.performPrefixedCommand(
                        silentSource,
                        "summon minecraft:lightning_bolt " + event.entity.x + " " + event.entity
                            .y + " " + (event.entity.z + 5)
                    )
                    attacker.server.commands.performPrefixedCommand(
                        silentSource,
                        "summon minecraft:lightning_bolt " + event.entity.x + " " + event.entity
                            .y + " " + (event.entity.z - 5)
                    )
                } else {
                    attacker.server.commands.performPrefixedCommand(
                        silentSource,
                        "summon minecraft:lightning_bolt " + attacker.x + " " + attacker.y + " " + attacker.z
                    )
                }
            } else if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:blood/devour"
                ) == 1
            ) {
                if (Math.random() <= 0.1 * attacker.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER.get())) {
                    attacker.server.commands
                        .performPrefixedCommand(silentSource, "cast " + attacker.name.string + " devour " + 3)
                }
            } else if (attacker.server.commands.performPrefixedCommand(silentSource, "power has " + attacker.name.string + " spellrigins:evocation/evocationsummon") == 1) {
                if (Math.random() <= 0.1 * attacker.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER.get())) {
                    // if the cooldown is above ~9 minutes, cancel the summon to not have 10 billion summons
                    val effect = attacker.getEffect(MobEffectRegistry.VEX_TIMER.get())
                    if (effect != null && effect.duration >= 11700) {
                       return
                    }
                    val summonTime = 20 * 60 * 10
                    // summon 2 vex
                    for (i in 0..1) {
                        val vex: SummonedVex = SummonedVex(attacker.level(), attacker)
                        vex.moveTo(
                            attacker.eyePosition
                                .add(Vec3(Utils.getRandomScaled(2.0), 1.0, Utils.getRandomScaled(2.0)))
                        )
                        vex.finalizeSpawn(
                            attacker.level() as ServerLevel,
                            attacker.level().getCurrentDifficultyAt(vex.onPos),
                            MobSpawnType.MOB_SUMMONED,
                            null,
                            null
                        )
                        vex.addEffect(MobEffectInstance(MobEffectRegistry.VEX_TIMER.get(), 12000, 0, false, false, false
                            )
                        )
                        attacker.level().addFreshEntity(vex)
                    }

                    var effectAmplifier = 2 - 1
                    if (attacker.hasEffect(MobEffectRegistry.VEX_TIMER.get())) {
                        effectAmplifier += (attacker.getEffect(MobEffectRegistry.VEX_TIMER.get())!!.amplifier) + 1
                    }
                    attacker.addEffect(
                        MobEffectInstance(
                            MobEffectRegistry.VEX_TIMER.get(),
                            summonTime,
                            effectAmplifier,
                            false,
                            false,
                            true
                        )
                    )
                }
            } else if (attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "power has " + attacker.name.string + " spellrigins:aqua/wet"
                ) == 1
            ) {
                // cast command to only apply if mod loaded, + no dependency needed
                attacker.server.commands.performPrefixedCommand(
                    silentSource,
                    "effect give " + event.entity.getStringUUID() + " traveloptics:wet" + " 200" + " 2"
                )
            }
        } else if (event.entity is ServerPlayer) {
            val player = event.entity as ServerPlayer
            if (player.server.commands.performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput(),
                    "power has " + player.name.string + " spellrigins:geo/tremor"
                ) == 1
            ) {
                player.server.commands.performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput(),
                    "effect give " + player.name
                        .string + " gtbcs_geomancy_plus:tremor_step_effect" + " 100" + " 2"
                )
            }
        }
    }

    @SubscribeEvent
    fun spellcast(event: SpellPreCastEvent) {
        if (event.entity.server!!.commands.performPrefixedCommand(
                event.entity.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                "power has " + event.entity.name.string + " spellrigins:holy/bowonlytogod"
            ) == 1
        ) {
            if ((event.schoolType === SchoolRegistry.BLOOD.get()) or (event.schoolType === SchoolRegistry.ELDRITCH.get())) {
                event.setCanceled(true)
                event.entity.sendSystemMessage(Component.literal("Bow only to god"))
            }
        }
        if (event.entity.server!!.commands.performPrefixedCommand(
                event.entity.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                "power has " + event.entity.name.string + " spellrigins:nospells"
            ) == 1
        ) {
            if (event.spellId != "irons_spellbooks:counterspell") {
                event.setCanceled(true)
                println(event.spellId)
            }
        }
    }

    @SubscribeEvent
    fun takelightningdamage(event: LivingDamageEvent) {
        if (event.source.`is`(DamageTypes.LIGHTNING_BOLT)) {
            // server only
            if (event.entity is ServerPlayer) {
                val player = event.entity as ServerPlayer
                if (player.server.commands.performPrefixedCommand(
                        player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                        "power has " + player.name.string + " spellrigins:lightning/lightningsummon"
                    ) == 1
                ) {
                    event.amount /= 3
                }
            }
        }
    }

    @SubscribeEvent
    fun onTeleport(event: EntityTeleportEvent) {
        // server only
        if (event.entity is ServerPlayer) {
            val player = event.entity as ServerPlayer

            if (player.server.commands.performPrefixedCommand(
                    event.entity.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    "power has " + player.getName().getString() + " spellrigins:ender/enderbility"
                ) == 1
            ) {
                player.addEffect(
                    MobEffectInstance(
                        MobEffectRegistry.EVASION.get(),
                        (300 * player.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER.get())).toInt(),
                        1,
                        false,
                        false,
                        true
                    )
                )
            }
        }
    }

    @SubscribeEvent
    fun onEndermanAnger(event: EnderManAngerEvent) {
        // server only
        if (event.player is ServerPlayer) {
            val player = event.player as ServerPlayer
            if (player.server.commands.performPrefixedCommand(
                    event.entity.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    "power has " + player.name.string + " spellrigins:ender/enderspellresist"
                ) == 1
            ) {
                event.setCanceled(true)
            }
        }
    }


    @SubscribeEvent
    fun onRecieveEffect(event: Applicable) {
        // server only

        if (event.entity is ServerPlayer) {
            val player = event.entity as ServerPlayer
            if (player.server.commands.performPrefixedCommand(
                    event.entity.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    "power has " + player.name.string + " spellrigins:icespellresist"
                ) == 1
            ) {
                if (event.getEffectInstance().effect === MobEffectRegistry.CHILLED.get()) {
                    event.setCanceled(true)
                }
            }
        }
    }
}