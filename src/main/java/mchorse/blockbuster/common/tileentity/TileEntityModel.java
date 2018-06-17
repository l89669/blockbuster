package mchorse.blockbuster.common.tileentity;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.network.common.PacketModifyModelBlock;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Model tile entity
 * 
 * This little guy is responsible for storing visual data about model's 
 * rendering.
 */
public class TileEntityModel extends TileEntityFlowerPot implements ITickable
{
    public AbstractMorph morph;
    public EntityLivingBase entity;
    public ItemStack[] slots = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    /* Entity rotations */
    public RotationOrder order = RotationOrder.ZYX;
    public float rotateYawHead;
    public float rotatePitch;
    public float rotateBody;

    /* Translation */
    public float x;
    public float y;
    public float z;

    /* Rotation */
    public float rx;
    public float ry;
    public float rz;

    /* Scale */
    public boolean one = false;
    public float sx = 1;
    public float sy = 1;
    public float sz = 1;

    public boolean shadow;

    public TileEntityModel()
    {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("Name", "blockbuster.fred");

        this.morph = MorphManager.INSTANCE.morphFromNBT(tag);
    }

    public TileEntityModel(float yaw)
    {
        this();
        this.ry = yaw;
    }

    public void setMorph(AbstractMorph morph)
    {
        this.morph = morph;
        this.markDirty();
    }

    @SideOnly(Side.CLIENT)
    public void createEntity()
    {
        this.entity = new EntityActor(Minecraft.getMinecraft().world);
        this.entity.onGround = true;
        this.updateEntity();
    }

    public void updateEntity()
    {
        if (this.entity == null)
        {
            return;
        }

        for (int i = 0; i < this.slots.length; i++)
        {
            this.entity.setItemStackToSlot(EntityEquipmentSlot.values()[i], this.slots[i]);
        }
    }

    @Override
    public void update()
    {
        if (this.entity != null)
        {
            this.entity.ticksExisted++;

            if (this.morph != null)
            {
                this.morph.update(this.entity, null);
            }
        }
    }

    /**
     * Infinite extend AABB allows to avoid frustum culling which can be 
     * used for some interesting things (like placing a whole OBJ level 
     * in the game)
     */
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared()
    {
        float range = Blockbuster.proxy.config.actor_rendering_range;

        return range * range;
    }

    public void copyData(PacketModifyModelBlock message)
    {
        this.order = message.order;
        this.rotateYawHead = message.yaw;
        this.rotatePitch = message.pitch;
        this.rotateBody = message.body;
        this.x = message.x;
        this.y = message.y;
        this.z = message.z;
        this.rx = message.rx;
        this.ry = message.ry;
        this.rz = message.rz;
        this.one = message.one;
        this.sx = message.sx;
        this.sy = message.sy;
        this.sz = message.sz;

        for (int i = 0; i < message.slots.length; i++)
        {
            ItemStack stack = message.slots[i];

            this.slots[i] = stack.copy();
        }

        this.setMorph(message.morph);
        this.updateEntity();
    }

    public void copyData(TileEntityModel model)
    {
        this.order = model.order;
        this.rotateYawHead = model.rotateYawHead;
        this.rotatePitch = model.rotatePitch;
        this.rotateBody = model.rotateBody;
        this.x = model.x;
        this.y = model.y;
        this.z = model.z;
        this.rx = model.rx;
        this.ry = model.ry;
        this.rz = model.rz;
        this.one = model.one;
        this.sx = model.sx;
        this.sy = model.sy;
        this.sz = model.sz;
        this.morph = model.morph;

        for (int i = 0; i < model.slots.length; i++)
        {
            ItemStack stack = model.slots[i];

            this.slots[i] = stack.copy();
        }

        this.updateEntity();
    }

    /* NBT methods */

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 5, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound.setByte("Order", (byte) this.order.ordinal());
        if (compound.hasKey("Shadow") || !this.shadow) compound.setBoolean("Shadow", this.shadow);
        if (compound.hasKey("Yaw") || this.rotateYawHead != 0) compound.setFloat("Yaw", this.rotateYawHead);
        if (compound.hasKey("Pitch") || this.rotatePitch != 0) compound.setFloat("Pitch", this.rotatePitch);
        if (compound.hasKey("Body") || this.rotateBody != 0) compound.setFloat("Body", this.rotateBody);
        if (compound.hasKey("ShiftX") || this.x != 0) compound.setFloat("ShiftX", this.x);
        if (compound.hasKey("ShiftY") || this.y != 0) compound.setFloat("ShiftY", this.y);
        if (compound.hasKey("ShiftZ") || this.z != 0) compound.setFloat("ShiftZ", this.z);
        if (compound.hasKey("RotateX") || this.rx != 0) compound.setFloat("RotateX", this.rx);
        if (compound.hasKey("RotateY") || this.ry != 0) compound.setFloat("RotateY", this.ry);
        if (compound.hasKey("RotateZ") || this.rz != 0) compound.setFloat("RotateZ", this.rz);
        if (compound.hasKey("Scale") || this.one == true) compound.setBoolean("Scale", this.one);

        if (compound.hasKey("ScaleX") || this.sx != 1) compound.setFloat("ScaleX", this.sx);
        if (compound.hasKey("ScaleY") || this.sy != 1) compound.setFloat("ScaleY", this.sy);
        if (compound.hasKey("ScaleZ") || this.sz != 1) compound.setFloat("ScaleZ", this.sz);

        NBTTagList list = new NBTTagList();

        for (int i = 0; i < this.slots.length; i++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            ItemStack stack = this.slots[i];

            if (!stack.isEmpty())
            {
                stack.writeToNBT(tag);
            }

            list.appendTag(tag);
        }

        compound.setTag("Items", list);

        if (this.morph != null)
        {
            NBTTagCompound morph = new NBTTagCompound();
            this.morph.toNBT(morph);

            compound.setTag("Morph", morph);
        }

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("Order"))
        {
            this.order = RotationOrder.values()[compound.getByte("Order")];
        }

        if (compound.hasKey("Shadow")) this.shadow = compound.getBoolean("Shadow");
        if (compound.hasKey("Yaw")) this.rotateYawHead = compound.getFloat("Yaw");
        if (compound.hasKey("Pitch")) this.rotatePitch = compound.getFloat("Pitch");
        if (compound.hasKey("Body")) this.rotateBody = compound.getFloat("Body");
        if (compound.hasKey("ShiftX")) this.x = compound.getFloat("ShiftX");
        if (compound.hasKey("ShiftY")) this.y = compound.getFloat("ShiftY");
        if (compound.hasKey("ShiftZ")) this.z = compound.getFloat("ShiftZ");
        if (compound.hasKey("RotateX")) this.rx = compound.getFloat("RotateX");
        if (compound.hasKey("RotateY")) this.ry = compound.getFloat("RotateY");
        if (compound.hasKey("RotateZ")) this.rz = compound.getFloat("RotateZ");
        if (compound.hasKey("Scale")) this.one = compound.getBoolean("Scale");

        if (compound.hasKey("ScaleX")) this.sx = compound.getFloat("ScaleX");
        if (compound.hasKey("ScaleY")) this.sy = compound.getFloat("ScaleY");
        if (compound.hasKey("ScaleZ")) this.sz = compound.getFloat("ScaleZ");

        if (compound.hasKey("Items", 9))
        {
            NBTTagList items = compound.getTagList("Items", 10);

            for (int i = 0, c = items.tagCount(); i < c; i++)
            {
                NBTTagCompound tag = items.getCompoundTagAt(i);
                ItemStack stack = new ItemStack(tag);

                this.slots[i] = stack;
            }
        }

        if (compound.hasKey("Morph", 10))
        {
            this.morph = MorphManager.INSTANCE.morphFromNBT(compound.getCompoundTag("Morph"));
        }
    }

    /**
     * Rotation order
     */
    public static enum RotationOrder
    {
        ZYX, XYZ;
    }
}