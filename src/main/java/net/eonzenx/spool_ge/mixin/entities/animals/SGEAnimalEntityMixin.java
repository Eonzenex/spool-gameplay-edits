package net.eonzenx.spool_ge.mixin.entities.animals;

import com.mojang.datafixers.util.Either;
import net.eonzenx.spool_ge.config.Config;
import net.eonzenx.spool_ge.utils.mixin.animals.IBreedableGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IEdibleGoalEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.IHappinessEntity;
import net.eonzenx.spool_ge.utils.mixin.animals.PickupItemGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(AnimalEntity.class)
public abstract class SGEAnimalEntityMixin extends MobEntity implements IBreedableGoalEntity, IEdibleGoalEntity, IHappinessEntity
{
    protected SGEAnimalEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }


    @Shadow private int loveTicks;

    @Shadow public abstract boolean isBreedingItem(ItemStack stack);
    @Shadow public abstract void setLoveTicks(int loveTicks);
    @Shadow public abstract void lovePlayer(@Nullable PlayerEntity player);


    protected Either<Tag<Item>, Item> BREEDABLE_ITEMS = Either.right(Items.WHEAT);
    protected Either<Tag<Item>, Item> EDIBLE_ITEMS = Either.right(Items.WHEAT);
    protected int BREEDABLE_ITEM_GOAL_PRIORITY = 3;
    protected int EDIBLE_ITEM_GOAL_PRIORITY = 3;
    protected float happiness = Config.Animals.Happiness.calcBaseHappiness(this);
    protected int eatTimeout;


    // <editor-folds desc="BreedableGoalEntity">
    @Override
    public Either<Tag<Item>, Item> getBreedableItemTags() { return BREEDABLE_ITEMS; }

    @Override
    public void setBreedableItems(Either<Tag<Item>, Item> newItems) { BREEDABLE_ITEMS = newItems; }

    @Override
    public int getBreedableItemGoalPriority()  { return BREEDABLE_ITEM_GOAL_PRIORITY; }

    @Override
    public void setBreedableItemGoalPriority(int newPriority) { BREEDABLE_ITEM_GOAL_PRIORITY = newPriority; }

    @Override
    public boolean canEatBreedable() { return loveTicks <= 0 && !isBaby(); }

    @Override
    public boolean isBreedableItem(ItemStack stack) {
        var result = new AtomicBoolean(false);
        BREEDABLE_ITEMS.ifLeft(itemTag -> result.set(result.get() || stack.isIn(itemTag)));
        BREEDABLE_ITEMS.ifRight(itemValue -> result.set(result.get() || stack.isOf(itemValue)));
        return result.get();
    }

    @Override
    public void completeBreedableGoalSetup() {
        goalSelector.add(BREEDABLE_ITEM_GOAL_PRIORITY, new PickupItemGoal((AnimalEntity) (Object) this, BREEDABLE_ITEMS, 0));
        BREEDABLE_ITEMS.ifLeft(itemTag -> goalSelector.add(BREEDABLE_ITEM_GOAL_PRIORITY, new TemptGoal((AnimalEntity) (Object) this, Config.Animals.BREEDABLE_FOLLOW_SPEED, Ingredient.fromTag(itemTag), false)));
        BREEDABLE_ITEMS.ifRight(item -> goalSelector.add(BREEDABLE_ITEM_GOAL_PRIORITY, new TemptGoal((AnimalEntity) (Object) this, Config.Animals.BREEDABLE_FOLLOW_SPEED, Ingredient.ofItems(item), false)));
    }
    // </editor-folds>

    // <editor-folds desc="EdibleGoalEntity">
    @Override
    public Either<Tag<Item>, Item> getEdibleItemTags() { return EDIBLE_ITEMS; }

    @Override
    public void setEdibleItems(Either<Tag<Item>, Item> newItems) { EDIBLE_ITEMS = newItems; }

    @Override
    public int getEdibleItemGoalPriority() { return EDIBLE_ITEM_GOAL_PRIORITY; }

    @Override
    public void setEdibleItemGoalPriority(int newPriority) { EDIBLE_ITEM_GOAL_PRIORITY = newPriority; }

    @Override
    public boolean canEatEdible() {
        return happiness < Config.Animals.Happiness.MAX;
    }

    @Override
    public boolean isEdibleItem(ItemStack stack) {
        var result = new AtomicBoolean(false);
        EDIBLE_ITEMS.ifLeft(itemTag -> result.set(result.get() || stack.isIn(itemTag)));
        EDIBLE_ITEMS.ifRight(itemValue -> result.set(result.get() || stack.isOf(itemValue)));
        return result.get();
    }

    @Override
    public void completeEdibleGoalSetup() {
        goalSelector.add(EDIBLE_ITEM_GOAL_PRIORITY, new PickupItemGoal((AnimalEntity) (Object) this, EDIBLE_ITEMS, 1));
        EDIBLE_ITEMS.ifLeft(itemTag -> goalSelector.add(EDIBLE_ITEM_GOAL_PRIORITY, new TemptGoal((AnimalEntity) (Object) this, Config.Animals.EDIBLE_FOLLOW_SPEED, Ingredient.fromTag(itemTag), false)));
        EDIBLE_ITEMS.ifRight(item -> goalSelector.add(EDIBLE_ITEM_GOAL_PRIORITY, new TemptGoal((AnimalEntity) (Object) this, Config.Animals.EDIBLE_FOLLOW_SPEED, Ingredient.ofItems(item), false)));
    }

    @Override
    public boolean canEat() {
        return loveTicks <= 0 && canEatEdible() && eatTimeout <= 0;
    }
    // </editor-folds>

    // <editor-folds desc="HappinessEntity">
    @Override
    public float getHappiness() { return happiness; }

    @Override
    public void setHappiness(float newHappiness) { happiness = newHappiness; }

    @Override
    public float addHappiness(float amount) {
        if (happiness + amount > Config.Animals.Happiness.MAX) {
            happiness = Config.Animals.Happiness.MAX;
        } else {
            happiness += amount;
        }

        System.out.println(happiness);
        return happiness;
    }

    @Override
    public int getEatTimeout() { return eatTimeout; }

    @Override
    public void setEatTimeout(int newEatTimeout) { eatTimeout = newEatTimeout; }

    @Override
    public void eat(ItemStack stack) {
        var didEat = new AtomicBoolean(false);
        if (isBreedableItem(stack) && canEatBreedable()) {
            BREEDABLE_ITEMS.ifLeft(itemTag -> {
                eatUsingTag(stack, itemTag, Config.Animals.Happiness.BREEDABLE_HAPPINESS);
                didEat.set(true);
            });
            BREEDABLE_ITEMS.ifRight(item -> {
                eatUsingStack(item, stack, Config.Animals.Happiness.BREEDABLE_HAPPINESS);
                didEat.set(true);
            });
        } else if (isEdibleItem(stack) && canEatEdible()) {
            EDIBLE_ITEMS.ifLeft(itemTag -> {
                eatUsingTag(stack, itemTag, Config.Animals.Happiness.EDIBLE_HAPPINESS);
                didEat.set(true);
            });
            EDIBLE_ITEMS.ifRight(item -> {
                eatUsingStack(item, stack, Config.Animals.Happiness.EDIBLE_HAPPINESS);
                didEat.set(true);
            });
        }

        if (didEat.get()) postEat(stack);
    }

    @Override
    public void postEat(ItemStack stack) {
        var timer = Config.Animals.Happiness.calcEatTimer(this);
        setEatTimeout(timer);

        playEatSound(stack);
        var bite = getStatusByte(getEdibleType(stack));
        world.sendEntityStatus(this, bite);

        if (isBreedableItem(stack) && !isBaby()) {
            lovePlayer(null);
        }

        // This eats the stack and reduces stack to air, must be placed last
        eatFood(world, stack);
    }

    @Override
    public boolean onEatTimeout() {
        return getEatTimeout() > 0;
    }
    // </editor-folds>


    public int getEdibleType(ItemStack stack) {
        if (isBreedableItem(stack)) return 0;
        if (isEdibleItem(stack)) return 1;

        return -1;
    }

    public byte getStatusByte(int edible) {
        return (byte) switch (edible) {
            case 0 -> 110;
            case 1 -> 111;
            default -> 110;
        };
    }

    private void eatUsingTag(ItemStack stack, Tag<Item> itemTag, float happiness) {
        if (stack.isIn(itemTag)) {
            addHappiness(happiness);
        }
    }

    private void eatUsingStack(Item item, ItemStack stack, float happiness) {
        if (item.getDefaultStack() == stack) {
            addHappiness(happiness);
        }
    }


    @Inject(method = "isBreedingItem", at = @At("TAIL"), cancellable = true)
    private void isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var found = new AtomicBoolean(false);

        var either = this.getBreedableItemTags();

        either.ifLeft(itemTag -> found.set(found.get() || stack.isIn(itemTag)));
        either.ifRight(item -> found.set(found.get() || stack == item.getDefaultStack()));

        cir.setReturnValue(found.get());
    }

    @Inject(method = "interactMob", at = @At("HEAD"))
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            var stack = player.getStackInHand(hand);

            var isEdibleItem = isEdibleItem(stack);
            var isBreedableItem = isBreedableItem(stack);

            if (!(isEdibleItem || isBreedableItem)) return;
            if (isEdibleItem && !canEatEdible()) return;
            if (isBreedableItem && !canEatBreedable()) return;

            eat(stack);
        }
    }


    public void eatParticles(ParticleEffect effect) {
        for(var i = 0; i < 7; ++i) {
            var d = random.nextGaussian() * 0.02D;
            var e = random.nextGaussian() * 0.02D;
            var f = random.nextGaussian() * 0.02D;
            world.addParticle(effect, getParticleX(1.0D), getRandomBodyY() + 0.5D, getParticleZ(1.0D), d, e, f);
        }
    }

    public ParticleEffect getParticleEffect(int edible) {
        return switch (edible) {
            case 0 -> ParticleTypes.COMPOSTER;
            case 1 -> ParticleTypes.CRIT;
            default -> ParticleTypes.CRIT;
        };
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    public void handleStatus(byte status, CallbackInfo ci) {
        ParticleEffect particleEffect;
        if (status == 110) {
            particleEffect = getParticleEffect(0);
        } else if (status == 111) {
            particleEffect = getParticleEffect(1);
        } else {
            return;
        }

        eatParticles(particleEffect);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(EntityType<? extends AnimalEntity> entityType, World world, CallbackInfo ci) {
        setCanPickUpLoot(true);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    public void tickMovement(CallbackInfo ci) {
        if (eatTimeout > 0) eatTimeout--;
    }


    private void dropItem(ItemStack stack) {
        var itemEntity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), stack);
        world.spawnEntity(itemEntity);
    }

    @Override
    protected void loot(ItemEntity item) {
        if (!canEat()) return;

        var itemStack = item.getStack();

        var isEdibleItem = isEdibleItem(itemStack);
        var isBreedableItem = isBreedableItem(itemStack);
        if (!(isEdibleItem || isBreedableItem)) return;

        var i = itemStack.getCount();
        if (i > 1) dropItem(itemStack.split(i - 1));
        itemStack = itemStack.split(1);

        if (tryEquip(itemStack)) {
            triggerItemPickedUpByEntityCriteria(item);
            sendPickup(item, itemStack.getCount());
            item.discard();
        }
    }

    private void playEatSound(ItemStack stack) {
        var soundEvent = stack.getEatSound();
        playSound(soundEvent, 1.0F, 1.0F);
    }


    @Override
    protected void onEquipStack(ItemStack stack) {
        if (!world.isClient) {
            var isEdibleItem = isEdibleItem(stack);
            var isBreedableItem = isBreedableItem(stack);

            if (!(isEdibleItem || isBreedableItem)) {
                super.onEquipStack(stack);
                return;
            }

            eat(stack);
        }
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat(Config.Nbt.HAPPINESS_KEY, happiness);
        nbt.putInt(Config.Nbt.EAT_TIMEOUT_KEY, eatTimeout);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        happiness = nbt.getFloat(Config.Nbt.HAPPINESS_KEY);
        eatTimeout = nbt.getInt(Config.Nbt.EAT_TIMEOUT_KEY);
    }
}













