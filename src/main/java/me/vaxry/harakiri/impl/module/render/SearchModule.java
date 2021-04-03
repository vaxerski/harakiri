package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventDestroyBlock;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockModel;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

public final class SearchModule extends Module {

    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"opacity"}, "Alpha value for the tracer.", 185, 1, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"size"}, "Line width of the tracer.", 1.0f, 0.1f, 2.0f, 0.1f);

    private final int MAX_BLOCKS = 512;
    private List<Integer> ids = new ArrayList<>();
    private final List<Vec3d> blocks = new ArrayList<>();

    public SearchModule() {
        super("Search", new String[]{"srch", "find", "search"}, "Search for selected blocks.", "NONE", -1, ModuleType.RENDER);

        if (Harakiri.get().getConfigManager().isFirstLaunch()) {
            this.add("furnace");
            this.add("crafting_table");
            this.add("enchanting_table");
            this.add("chest");
            this.add("trapped_chest");
            this.add("bed");
            this.add("hopper");
            this.add("dispenser");
            this.add("dropper");
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.blocks.clear();
        }
    }

    @Listener
    public void onDestroyBlock(EventDestroyBlock event) {
        if (event.getPos() != null) {
            if (this.isPosCached(event.getPos())) {
                this.removeBlock(event.getPos());
            }
        }
    }

    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null)
            return;

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            final Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock == null)
                continue;

            final BlockPos blockPos = new BlockPos(searchBlock.x, searchBlock.y, searchBlock.z);
            final Block block = mc.world.getBlockState(blockPos).getBlock();

            if (block instanceof BlockAir)
                continue;

            final int color = ColorUtil.changeAlpha(this.getColor(blockPos, block), this.alpha.getValue());
            final Vec3d pos = new Vec3d(searchBlock.x, searchBlock.y, searchBlock.z).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
            //final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
            //RenderUtil.drawLine3D(forward.x, forward.y + mc.player.getEyeHeight(), forward.z, pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f, this.width.getValue(), color);
            Coordinate coord2D = conv3Dto2DSpace(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f);
            RenderUtil.drawLine(res.getScaledWidth()/2.f,res.getScaledHeight()/2.f, (float)coord2D.x, (float)coord2D.y, this.width.getValue(), color);
        }

        for(TileEntity te : mc.world.loadedTileEntityList){
            if(te instanceof TileEntityChest && this.ids.contains(54) ||
                    te instanceof TileEntityEnderChest && this.ids.contains(130) ||
                        te instanceof TileEntityBed && this.ids.contains(355)) {
                Coordinate coord2D = conv3Dto2DSpace(te.getPos().getX() + 0.5f - mc.getRenderManager().renderPosX, te.getPos().getY() + 0.5f - mc.getRenderManager().renderPosY, te.getPos().getZ() + 0.5f - mc.getRenderManager().renderPosZ);
                final int color = ColorUtil.changeAlpha(this.getTileColor(te), this.alpha.getValue());
                RenderUtil.drawLine(res.getScaledWidth()/2.f,res.getScaledHeight()/2.f, (float)coord2D.x, (float)coord2D.y, this.width.getValue(), color);
            }
        }
    }

    @Listener
    public void onRenderBlock(EventRenderBlockModel event) {
        final BlockPos pos = event.getBlockPos();
        final IBlockState blockState = event.getBlockState();
        if (this.contains(Block.getIdFromBlock(blockState.getBlock())) && !this.isPosCached(pos.getX(), pos.getY(), pos.getZ()) && this.blocks.size() < MAX_BLOCKS) {
            this.blocks.add(new Vec3d(pos));
        }
    }

    @Override
    public void onEnable() {
        this.blocks.clear();
        super.onEnable();
    }

    @Override
    public void onToggle() {
        super.onToggle();
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    private void removeBlock(BlockPos pos) {
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            final Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == pos.getX() && searchBlock.y == pos.getY() && searchBlock.z == pos.getZ()) {
                this.blocks.remove(i);
            }
        }
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    private boolean isPosCached(int x, int y, int z) {
        boolean temp = false;
        for (int i = this.blocks.size() - 1; i >= 0; i--) {
            Vec3d searchBlock = this.blocks.get(i);
            if (searchBlock.x == x && searchBlock.y == y && searchBlock.z == z)
                temp = true;
        }
        return temp;
    }

    private boolean isPosCached(BlockPos pos) {
        return this.isPosCached(pos.getX(), pos.getY(), pos.getZ());
    }

    private AxisAlignedBB boundingBoxForBlock(BlockPos blockPos) {
        final Minecraft mc = Minecraft.getMinecraft();
        return new AxisAlignedBB(
                blockPos.getX() - mc.getRenderManager().viewerPosX,
                blockPos.getY() - mc.getRenderManager().viewerPosY,
                blockPos.getZ() - mc.getRenderManager().viewerPosZ,

                blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    }

    public void updateRenders() {
        //mc.renderGlobal.loadRenderers();
        final Minecraft mc = Minecraft.getMinecraft();
        mc.renderGlobal.markBlockRangeForRenderUpdate(
                (int) mc.player.posX - 256,
                (int) mc.player.posY - 256,
                (int) mc.player.posZ - 256,
                (int) mc.player.posX + 256,
                (int) mc.player.posY + 256,
                (int) mc.player.posZ + 256);
    }

    public boolean contains(int id) {
        return this.ids.contains(id);
    }

    public boolean contains(String localizedName) {
        final Block blockFromName = Block.getBlockFromName(localizedName);
        if (blockFromName != null) {
            return contains(Block.getIdFromBlock(blockFromName));
        }
        return false;
    }

    public void add(int id) {
        if (!contains(id)) {
            this.ids.add(id);
        }
    }

    public void add(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if(name.equalsIgnoreCase("bed")){
            this.ids.add(355);
            return;
        }
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (!contains(id)) {
                this.ids.add(id);
            }
        }
    }

    public void remove(int id) {
        for (Integer i : this.ids) {
            if (id == i) {
                this.ids.remove(i);
                break;
            }
        }
    }

    public void remove(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if(name.equalsIgnoreCase("bed")){
            final Integer id = 355;
            this.ids.remove(id);
            return;
        }
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (contains(id)) {
                this.ids.remove(id);
            }
        }
    }

    public int clear() {
        final int count = this.ids.size();
        this.ids.clear();
        return count;
    }

    private int getTileColor(TileEntity te) {
        if (te instanceof TileEntityChest) {
            return 0xFFF0F000;
        }
        if (te instanceof TileEntityDropper) {
            return 0xFFA6A6A6;
        }
        if (te instanceof TileEntityDispenser) {
            return 0xFF4E4E4E;
        }
        if (te instanceof TileEntityHopper) {
            return 0xFF4E4E4E;
        }
        if (te instanceof TileEntityFurnace) {
            return 0xFF2D2D2D;
        }
        if (te instanceof TileEntityBrewingStand) {
            return 0xFF17B9D2;
        }
        if (te instanceof TileEntityEnderChest) {
            return 0xFFCC00FF;
        }
        if (te instanceof TileEntityShulkerBox) {
            //final TileEntityShulkerBox shulkerBox = (TileEntityShulkerBox) te;
            //return (255 << 24) | shulkerBox.getColor().getColorValue();
            return 0xFFFF0066;
        }
        if (te instanceof TileEntityBed){
            return 0xFFFF2222;
        }
        return 0xFFFFFFFF;
    }

    private int getColor(final BlockPos pos, final Block block) {
        if (block instanceof BlockEnderChest)
            return 0xFF624FFF;
        else if (block == Blocks.CRAFTING_TABLE)
            return 0xFFFFC853;
        else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE)
            return 0xFF76A7B4;
        else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
            return 0xFFFF8639;
        else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
            return 0xFF4CD1FF;
        else if (block == Blocks.ANVIL)
            return 0xFFB6BA9E;
        else if (block == Blocks.DISPENSER || block == Blocks.DROPPER)
            return 0xFFD3E2CA;
        else if (block == Blocks.CAULDRON)
            return 0xFF9E97BF;
        else if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block instanceof BlockChest)
            return 0xFFCCA300;
        else if (block == Blocks.BED || block instanceof BlockBed)
            return 0xFFCC0000;
        else if (block == Blocks.OBSIDIAN || block == Blocks.PORTAL)
            return 0xFF800080;

        final int mapColor = Minecraft.getMinecraft().world.getBlockState(pos).getMaterial().getMaterialMapColor().colorValue;
        if (mapColor > 0)
            return mapColor;

        return 0xFFBFBFBF;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    private Coordinate conv3Dto2DSpace(double x, double y, double z) {
        final GLUProjection.Projection projection = GLUProjection.getInstance().project(x, y, z, GLUProjection.ClampMode.NONE, false);

        final Coordinate returns = new Coordinate(projection.getX(), projection.getY());

        return returns;
    }
}