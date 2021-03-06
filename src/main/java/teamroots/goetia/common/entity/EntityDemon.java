package teamroots.goetia.common.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import teamroots.goetia.MainRegistry;
import teamroots.goetia.capability.capabilites.GoetiaProvider;
import teamroots.goetia.common.util.Utils;

public class EntityDemon extends EntityMob implements IDemonic {
    public static final DataParameter<Boolean> trapped = EntityDataManager.<Boolean>createKey(EntityDemon.class, DataSerializers.BOOLEAN);

	public EntityDemon(World worldIn) {
		super(worldIn);
        this.setSize(1.4F, 2.8F);
		this.experienceValue = 20;
	}
	
	@Override
    protected void entityInit(){
    	super.entityInit();
        this.getDataManager().register(trapped, Boolean.valueOf(false));
        this.isImmuneToFire = true;
    }

	protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 0.46D, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.46D));
        this.tasks.addTask(7, new EntityAIWander(this, 0.46D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }
	
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(92.0D);
    }

    protected void applyEntityAI()
    {
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }
    
    @Override
    public void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source){
    	super.dropLoot(wasRecentlyHit,lootingModifier,source);
    	if (!getEntityWorld().isRemote){
    		getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(Items.BONE,1)));
    		for (int i = 0; i < 3+lootingModifier; i ++){
	    		if (rand.nextInt(2) == 0){
	    			getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(Items.BONE,1)));
	    		}
	    	}
    		getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(MainRegistry.demonHide,1)));
    		for (int i = 0; i < 3+lootingModifier; i ++){
	    		if (rand.nextInt(2) == 0){
	    			getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(MainRegistry.demonHide,1)));
	    		}
	    	}
    		for (int i = 0; i < 3+lootingModifier; i ++){
	    		if (rand.nextInt(2) == 0){
	    			getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(Items.IRON_INGOT,1)));
	    		}
	    	}
	    	for (int i = 0; i < 1; i ++){
	    		if (rand.nextInt(2) == 0){
	    			getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(),posX,posY+0.5,posZ,new ItemStack(MainRegistry.demonHorn,1)));
	    		}
	    	}
    	}
    }
    
    @Override
    public void onUpdate(){
    	super.onUpdate();
    	if (this.getHealth() > 0 && this.ticksExisted % 80 == 0 && this.rand.nextBoolean() && this.getAttackTarget() != null && !getDataManager().get(trapped).booleanValue()){
    		Vec3d targetVector = (new Vec3d(getAttackTarget().posX-posX,getAttackTarget().posY-posY, getAttackTarget().posZ-posZ)).normalize().scale(0.01);
    		EntityLargeFireball fireball = new EntityLargeFireball(getEntityWorld(), posX+this.getLookVec().xCoord*2.0f, posY+1.8+this.getLookVec().yCoord*2.0f, posZ+getLookVec().zCoord*2.0f, targetVector.xCoord, targetVector.yCoord, targetVector.zCoord);
    		fireball.explosionPower = 1;
        	getEntityWorld().spawnEntityInWorld(fireball);
    	}
    	if (this.getEntityBoundingBox() != null){
    		for (int i = 0; i < 4; i ++){
    			Vec3d particle = Utils.randomPointInAABB(this.getEntityBoundingBox());
    			getEntityWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particle.xCoord, particle.yCoord, particle.zCoord, 0, 0, 0, 0);
    		}
    	}
    	if(!this.isBurning() && !this.isImmuneToFire){
    		this.isImmuneToFire = true;
    	}
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount){
    	float initHealth = getHealth();
    	super.attackEntityFrom(source, amount);
    	if (getHealth() <= 0 && initHealth > 0){
    		if (!getEntityWorld().isRemote && source.getEntity() instanceof EntityPlayer){
    			if (((EntityPlayer)source.getEntity()).hasCapability(GoetiaProvider.goetiaCapability, null)){
    				((EntityPlayer)source.getEntity()).getCapability(GoetiaProvider.goetiaCapability, null).addImpurity((EntityPlayer)source.getEntity(), rand.nextInt(6)+11);
    			}
    		}
    	}
    	return true;
    }
    
    @Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		getDataManager().set(trapped, compound.getBoolean("trapped"));
		getDataManager().setDirty(trapped);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("trapped", getDataManager().get(trapped));
	}

	@Override
	public void setTrapped() {
		getDataManager().set(trapped,true);
		getDataManager().setDirty(trapped);
	}

	@Override
	public void onHolyWaterContact() {
		this.isImmuneToFire = false;
		this.setFire(3);
	}
}
