package net.flowersauce.zombie_horse_spawning.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathMixin
{

    @Inject(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V",
                    shift = At.Shift.AFTER // 插入到调用 setPose 之后
            )
    )
    public void onDeathInject(DamageSource damageSource, CallbackInfo info)
    {
        // 判断是否是马并且是否因溺水死亡
        if ((Object) this instanceof HorseEntity && damageSource.isOf(DamageTypes.DROWN))
        {
            double spawnChance = 0.95; // 僵尸马的生成概率
            if (new Random().nextDouble() < spawnChance)
            {
                // 创建僵尸马实体
                HorseEntity horse = (HorseEntity) (Object) this;
                ZombieHorseEntity zombieHorse = new ZombieHorseEntity(EntityType.ZOMBIE_HORSE, horse.getWorld());
                // 继承原马的位置、大小、驯服状态、驯服者ID、自定义名称
                zombieHorse.setPosition(horse.getX(), horse.getY(), horse.getZ());
                zombieHorse.setBaby(horse.isBaby());
                zombieHorse.setTame(horse.isTame());
                zombieHorse.setOwnerUuid(horse.getOwnerUuid());
                zombieHorse.setCustomName(horse.getCustomName());

                // 调整驯服的僵尸马的属性
                if (zombieHorse.isTame())
                {
                    // 设置生命值为 20.0
                    zombieHorse.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0); // 最大生命值为 20
                    zombieHorse.setHealth(20.0f); // 当前生命值同步到最大生命值

                    // 设置速度为普通马的最大值
                    zombieHorse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3375);

                    // 设置跳跃高度为普通马的最大值
                    zombieHorse.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(1.0);
                }

                horse.getWorld().spawnEntity(zombieHorse);

                horse.getWorld().playSound(
                        null, // null 表示对所有玩家播放
                        horse.getBlockPos(), // 播放音效的位置（马的死亡位置）
                        SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT, // 音效
                        SoundCategory.NEUTRAL, // 音效类别
                        2f, // 音量
                        0.6f  // 音调
                );
            }
        }
    }
}
