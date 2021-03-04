package me.vaxry.harakiri.impl.gui.menu.account;

import com.google.common.collect.Lists;
import me.vaxry.harakiri.impl.gui.menu.HaraAccountManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.LanServerInfo;

import javax.swing.*;
import java.util.List;

public class AltSelectionList extends GuiListExtended {
    private final HaraAccountManager owner;
    private final List<AccountListEntryNormal> accountListEntries = Lists.newArrayList();
    private final IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
    private int selectedSlotIndex = -1;

    public AltSelectionList(HaraAccountManager ownerIn, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.owner = ownerIn;
    }

    public IGuiListEntry getListEntry(int index) {
        if (index < this.accountListEntries.size()) {
            return (IGuiListEntry)this.accountListEntries.get(index);
        } else {
            return null;
        }
    }

    public int getSize() {
        return this.accountListEntries.size(); }

    public void setSelectedSlotIndex(int selectedSlotIndexIn) {
        this.selectedSlotIndex = selectedSlotIndexIn;
    }

    protected boolean isSelected(int slotIndex) {
        return slotIndex == this.selectedSlotIndex;
    }

    public int getSelected() {
        return this.selectedSlotIndex;
    }

    public void updateOnlineServers(AltList p_148195_1_) {
        this.accountListEntries.clear();

        for(int i = 0; i < p_148195_1_.countServers(); ++i) {
            this.accountListEntries.add(new AccountListEntryNormal(this.owner, p_148195_1_.getServerData(i)));
        }

    }

    public void updateNetworkServers(List<LanServerInfo> p_148194_1_) {


    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 30;
    }

    public int getListWidth() {
        return super.getListWidth() + 85;
    }
}
