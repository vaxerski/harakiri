package me.vaxry.harakiri.impl.gui.menu;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.gui.menu.account.*;
import me.vaxry.harakiri.impl.gui.menu.account.backend.AccountManager;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class HaraAccountManager extends GuiScreen {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerPinger oldServerPinger = new ServerPinger();
    private final GuiScreen parentScreen;
    private AltSelectionList altListSelector;
    private AltList altList;
    private GuiButton btnEditServer;
    private GuiButton btnSelectServer;
    private GuiButton btnDeleteServer;
    private boolean deletingServer;
    private boolean addingServer;
    private boolean editingServer;
    private boolean directConnect;
    private String hoveringText;
    private Account selectedAccount;
    private boolean initialized;

    private String lastStatus = "";
    private String lastUsernameLoggedIn = "";

    public HaraAccountManager(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        if (this.initialized) {
            this.altListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
        } else {
            this.initialized = true;
            this.altList = new AltList(this.mc);

            try {

            } catch (Exception var2) {
                LOGGER.warn("Unable to start LAN server detection: {}", var2.getMessage());
            }
        }

        this.altListSelector = new AltSelectionList(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
        this.altListSelector.updateOnlineServers(this.altList);
        this.createButtons();
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.altListSelector.handleMouseInput();
    }

    public void createButtons() {
        this.btnEditServer = this.addButton(new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, I18n.format("selectServer.edit", new Object[0])));
        this.btnDeleteServer = this.addButton(new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, I18n.format("selectServer.delete", new Object[0])));
        this.btnSelectServer = this.addButton(new GuiButton(1, this.width / 2 - 154, this.height - 52, 100, 20, "Select"));
        //this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 52, 100, 20, "Direct login"));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, "Add Account"));
        this.buttonList.add(new GuiButton(8, this.width / 2 + 4, this.height - 28, 70, 20, "nhentai"));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, "Return"));
        this.selectServer(this.altListSelector.getSelected());
    }

    public void updateScreen() {
        super.updateScreen();

        this.oldServerPinger.pingPendingNetworks();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        this.oldServerPinger.clearPendingNetworks();
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.altListSelector.getSelected() < 0 ? null : this.altListSelector.getListEntry(this.altListSelector.getSelected());
            if (button.id == 2 && guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                String s4 = ((AccountListEntryNormal)guilistextended$iguilistentry).getServerData().name;
                if (s4 != null) {
                    this.deletingServer = true;
                    String s = "Are you sure?";
                    String s1 = "";
                    String s2 = "Delete";
                    String s3 = "Cancel";
                    GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, this.altListSelector.getSelected());
                    this.mc.displayGuiScreen(guiyesno);
                }
            } else if (button.id == 1) {
                // Login to the account

                if(this.selectedAccount != null) {
                    if(this.selectedAccount.name.equalsIgnoreCase(lastUsernameLoggedIn) && !this.selectedAccount.name.equalsIgnoreCase("")){
                        lastStatus = "Status: " + ChatFormatting.GREEN + "Success " + ChatFormatting.GRAY + "(Already logged in as " + this.selectedAccount.name + ")";
                    }else {
                        String accountLoginName = AccountManager.loginPassword(this.selectedAccount.email, this.selectedAccount.pass);
                        if (accountLoginName != null) {
                            if (!accountLoginName.equalsIgnoreCase("Error-1") && !accountLoginName.contains("Invalid user") && !accountLoginName.contains("Forbidden")) {
                                this.selectedAccount.name = accountLoginName;
                                this.lastUsernameLoggedIn = accountLoginName;
                                this.altList.saveAltList();
                                lastStatus = "Status: " + ChatFormatting.GREEN + "Success " + ChatFormatting.GRAY + "(Logged in as " + this.selectedAccount.name + ")";

                                Harakiri.get().setLoggedAccount(this.selectedAccount.name);
                            } else {
                                lastStatus = "Status: " + ChatFormatting.RED + "Failed to login." + ChatFormatting.RESET + " (" + accountLoginName + ")";
                            }
                        } else {
                            lastStatus = "Status: " + ChatFormatting.RED + "Failed to login. (ALN null)";
                        }
                    }
                } else {
                    lastStatus = "Status: " + ChatFormatting.RED + "Failed to login. (SA null)";
                }
            } else if (button.id == 4) {
                //this.directConnect = true;
                //this.selectedAccount = new Account("", "", "");
                //this.mc.displayGuiScreen(new GuiScreenServerList(this, this.selectedAccount));
            } else if (button.id == 3) {
                this.addingServer = true;
                this.selectedAccount = new Account("", "", "");
                this.mc.displayGuiScreen(new GuiScreenAddAlt(this, this.selectedAccount));
            } else if (button.id == 7 && guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                //this.editingServer = true;
                //ServerData serverdata = ((ServerListEntryNormal)guilistextended$iguilistentry).getServerData();
                //this.selectedAccount = new ServerData(serverdata.serverName, serverdata.serverIP, false);
                //this.selectedAccount.copyFrom(serverdata);
                //this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedAccount));
            } else if (button.id == 0) {
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (button.id == 8) {
                this.openweb(new URL("https://nhentai.net"));
            }
        }

    }

    private void refreshServerList() {
        this.mc.displayGuiScreen(new GuiMultiplayer(this.parentScreen));
    }

    public void confirmClicked(boolean result, int id) {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.altListSelector.getSelected() < 0 ? null : this.altListSelector.getListEntry(this.altListSelector.getSelected());
        if (this.deletingServer) {
            this.deletingServer = false;
            if (result && guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                this.altList.removeServerData(this.altListSelector.getSelected());
                this.altList.saveAltList();
                this.altListSelector.setSelectedSlotIndex(-1);
                this.altListSelector.updateOnlineServers(this.altList);
            }

            this.mc.displayGuiScreen(this);
        } else if (this.addingServer) {
            this.addingServer = false;
            if (result) {
                this.altList.addServerData(this.selectedAccount);
                this.altList.saveAltList();
                this.altListSelector.setSelectedSlotIndex(-1);
                this.altListSelector.updateOnlineServers(this.altList);
            }

            this.mc.displayGuiScreen(this);
        } else if (this.editingServer) {
            this.editingServer = false;
            if (result && guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                //ServerData serverdata = ((ServerListEntryNormal)guilistextended$iguilistentry).getServerData();
                //serverdata.serverName = this.selectedAccount.serverName;
                //serverdata.serverIP = this.selectedAccount.serverIP;
                //serverdata.copyFrom(this.selectedAccount);
                //this.altList.saveServerList();
                //this.serverListSelector.updateOnlineServers(this.altList);
            }

            this.mc.displayGuiScreen(this);
        }

    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        int i = this.altListSelector.getSelected();
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = i < 0 ? null : this.altListSelector.getListEntry(i);
        if (keyCode == 63) {
            this.refreshServerList();
        } else if (i >= 0) {
            if (keyCode == 200) {
                if (isShiftKeyDown()) {
                    if (i > 0 && guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                        this.altList.swapServers(i, i - 1);
                        this.selectServer(this.altListSelector.getSelected() - 1);
                        this.altListSelector.scrollBy(-this.altListSelector.getSlotHeight());
                        this.altListSelector.updateOnlineServers(this.altList);
                    }
                } else if (i > 0) {
                    this.selectServer(this.altListSelector.getSelected() - 1);
                    this.altListSelector.scrollBy(-this.altListSelector.getSlotHeight());
                } else {
                    this.selectServer(-1);
                }
            } else if (keyCode == 208) {
                if (isShiftKeyDown()) {
                    if (i < this.altList.countServers() - 1) {
                        this.altList.swapServers(i, i + 1);
                        this.selectServer(i + 1);
                        this.altListSelector.scrollBy(this.altListSelector.getSlotHeight());
                        this.altListSelector.updateOnlineServers(this.altList);
                    }
                } else if (i < this.altListSelector.getSize()) {
                    this.selectServer(this.altListSelector.getSelected() + 1);
                    this.altListSelector.scrollBy(this.altListSelector.getSlotHeight());
                } else {
                    this.selectServer(-1);
                }
            } else if (keyCode != 28 && keyCode != 156) {
                super.keyTyped(typedChar, keyCode);
            } else {
                this.actionPerformed((GuiButton)this.buttonList.get(2));
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveringText = null;
        this.drawDefaultBackground();
        this.altListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "Harakiri Account Manager", this.width / 2, 20, 16777215);
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(this.lastStatus, 0, 0, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.hoveringText != null) {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringText)), mouseX, mouseY);
        }

    }

    public void connectToSelected() {
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.altListSelector.getSelected() < 0 ? null : this.altListSelector.getListEntry(this.altListSelector.getSelected());
        if (guilistextended$iguilistentry instanceof ServerListEntryNormal) {
            this.connectToServer(((ServerListEntryNormal)guilistextended$iguilistentry).getServerData());
        } else if (guilistextended$iguilistentry instanceof ServerListEntryLanDetected) {
            LanServerInfo lanserverinfo = ((ServerListEntryLanDetected)guilistextended$iguilistentry).getServerData();
            this.connectToServer(new ServerData(lanserverinfo.getServerMotd(), lanserverinfo.getServerIpPort(), true));
        }

    }

    private void connectToServer(ServerData server) {
        FMLClientHandler.instance().connectToServer(this, server);
    }

    public void selectServer(int index) {
        this.altListSelector.setSelectedSlotIndex(index);
        GuiListExtended.IGuiListEntry guilistextended$iguilistentry = index < 0 ? null : this.altListSelector.getListEntry(index);
        this.btnSelectServer.enabled = false;
        this.btnEditServer.enabled = false;
        this.btnDeleteServer.enabled = false;
        if (guilistextended$iguilistentry != null && !(guilistextended$iguilistentry instanceof ServerListEntryLanScan)) {
            this.selectedAccount = this.altList.getServerData(index);
            this.btnSelectServer.enabled = true;
            if (guilistextended$iguilistentry instanceof AccountListEntryNormal) {
                this.btnEditServer.enabled = true;
                this.btnDeleteServer.enabled = true;
            }
        }

    }

    public ServerPinger getOldServerPinger() {
        return this.oldServerPinger;
    }

    public void setHoveringText(String p_146793_1_) {
        this.hoveringText = p_146793_1_;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.altListSelector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.altListSelector.mouseReleased(mouseX, mouseY, state);
    }

    public AltList getServerList() {
        return this.altList;
    }

    public boolean canMoveUp(AccountListEntryNormal p_175392_1_, int p_175392_2_) {
        return p_175392_2_ > 0;
    }

    public boolean canMoveDown(AccountListEntryNormal p_175394_1_, int p_175394_2_) {
        return p_175394_2_ < this.altList.countServers() - 1;
    }

    public void moveServerUp(AccountListEntryNormal p_175391_1_, int p_175391_2_, boolean p_175391_3_) {
        int i = p_175391_3_ ? 0 : p_175391_2_ - 1;
        this.altList.swapServers(p_175391_2_, i);
        if (this.altListSelector.getSelected() == p_175391_2_) {
            this.selectServer(i);
        }

        this.altListSelector.updateOnlineServers(this.altList);
    }

    public void moveServerDown(AccountListEntryNormal p_175393_1_, int p_175393_2_, boolean p_175393_3_) {
        int i = p_175393_3_ ? this.altList.countServers() - 1 : p_175393_2_ + 1;
        this.altList.swapServers(p_175393_2_, i);
        if (this.altListSelector.getSelected() == p_175393_2_) {
            this.selectServer(i);
        }

        this.altListSelector.updateOnlineServers(this.altList);
    }

    public boolean openlink(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public boolean openweb(URL url) {
        try {
            return openlink(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
