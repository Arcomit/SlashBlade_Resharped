package mods.flammpfeil.slashblade.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class PlayerAttackHelper {
    public static void attack(Player attacker, Entity target){
        // 触发Forge事件，以兼容其他模组
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(attacker, target)) return;
        // 判断攻击目标是否可以被攻击
        if (target.isAttackable()) {
            if (!target.skipAttackInteraction(attacker)) {
                // 获取攻击者的攻击伤害属性
                float baseDamage = (float)attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

                //伤害>0时不造成伤害
                if (baseDamage > 0.0F) {

                    // 获取攻击者的击退属性
                    float knockback = (float)attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK); // Forge: Initialize attacker value to the attack knockback attribute of the player, which is by default 0
                    // 加上当前击退附魔加成
                    knockback  += EnchantmentHelper.getKnockbackBonus(attacker);
                    if (attacker.isSprinting()) {
                        attacker.level().playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, attacker.getSoundSource(), 1.0F, 1.0F);
                        ++knockback ;
                    }

                    // 暴击
                    boolean isCritical = attacker.fallDistance > 0.0F && !attacker.onGround() &&
                            !attacker.onClimbable() && !attacker.isInWater() &&
                            !attacker.hasEffect(MobEffects.BLINDNESS) &&
                            !attacker.isPassenger() && target instanceof LivingEntity && !attacker.isSprinting();
                    net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(attacker, target, isCritical, isCritical ? 1.5F : 1.0F);
                    isCritical = hitResult != null;
                    if (isCritical) {
                        baseDamage *= hitResult.getDamageModifier();
                    }


                    //火焰附加
                    float preAttackHealth = 0.0F;
                    boolean shouldSetFire = false;
                    int fireAspectLevel = EnchantmentHelper.getFireAspect(attacker);
                    if (target instanceof LivingEntity) {
                        preAttackHealth  = ((LivingEntity)target).getHealth();
                        if (fireAspectLevel > 0 && !target.isOnFire()) {
                            shouldSetFire  = true;
                            target.setSecondsOnFire(1);
                        }
                    }

                    Vec3 vec3 = target.getDeltaMovement();
                    boolean damageSuccess = target.hurt(attacker.damageSources().playerAttack(attacker), baseDamage);
                    if (damageSuccess) {
                        //击退
                        if (knockback  > 0) {
                            if (target instanceof LivingEntity) {
                                ((LivingEntity)target).knockback((double)((float)knockback  * 0.5F), (double) Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F))));
                            } else {
                                target.push((double)(-Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)) * (float)knockback  * 0.5F), 0.1D, (double)(Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F)) * (float)knockback  * 0.5F));
                            }

                            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            attacker.setSprinting(false);
                        }

                        if (target instanceof ServerPlayer && target.hurtMarked) {
                            ((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
                            target.hurtMarked = false;
                            target.setDeltaMovement(vec3);
                        }

                        //音效
                        if (isCritical) {
                            attacker.level().playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, attacker.getSoundSource(), 1.0F, 1.0F);
                            attacker.crit(target);
                        }else {
                            attacker.level().playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, attacker.getSoundSource(), 1.0F, 1.0F);
                        }

                        //触发攻击目标的附魔效果
                        attacker.setLastHurtMob(target);
                        if (target instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity)target, attacker);
                        }

                        EnchantmentHelper.doPostDamageEffects(attacker, target);
                        ItemStack itemstack1 = attacker.getMainHandItem();
                        Entity entity = target;
                        if (target instanceof net.minecraftforge.entity.PartEntity) {
                            entity = ((net.minecraftforge.entity.PartEntity<?>) target).getParent();
                        }

                        // 减少耐久
                        if (!attacker.level().isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            ItemStack copy = itemstack1.copy();
                            itemstack1.hurtEnemy((LivingEntity)entity, attacker);
                            if (itemstack1.isEmpty()) {
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(attacker, copy, InteractionHand.MAIN_HAND);
                                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float damageDealt = preAttackHealth  - ((LivingEntity)target).getHealth();
                            //伤害统计
                            attacker.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                            //应用完整的火焰附加效果(每级4秒)
                            if (fireAspectLevel > 0) {
                                target.setSecondsOnFire(fireAspectLevel * 4);
                            }


                            if (attacker.level() instanceof ServerLevel && damageDealt > 2.0F) {
                                int k = (int)((double)damageDealt  * 0.5D);
                                ((ServerLevel)attacker.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        attacker.causeFoodExhaustion(0.1F);// 消耗饱食度
                    } else {//伤害未成功应用
                        attacker.level().playSound((Player)null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, attacker.getSoundSource(), 1.0F, 1.0F);
                        if (shouldSetFire ) {
                            //取消预火焰附加效果
                            target.clearFire();
                        }
                    }
                }
            }
        }
    }
}
