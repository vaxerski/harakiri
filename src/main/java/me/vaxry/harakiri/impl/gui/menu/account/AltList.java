package me.vaxry.harakiri.impl.gui.menu.account;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AltList {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<Account> accounts = Lists.newArrayList();

    public AltList(Minecraft mcIn) {
        this.mc = mcIn;
        this.loadAltList();
    }

    public static String encrypt(final String secret, final String data) {


        byte[] decodedKey = Base64.getDecoder().decode(secret);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);
            byte[] cipherText = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while encrypting data", e);
        }

    }

    public static String decrypt(final String secret,
                                 final String encryptedString) {


        byte[] decodedKey = Base64.getDecoder().decode(secret);

        try {
            Cipher cipher = Cipher.getInstance("AES");
            // rebuild key using SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            return new String(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error occured while decrypting data", e);
        }
    }


    public void loadAltList() {
        try {

            JsonArray accountJson = null;

            final JsonElement accSettings = this.convertJsonObjectFromFile().get("accounts");
            if (accSettings != null)
                accountJson = accSettings.getAsJsonArray();

            if (accountJson != null) {
                for (int i = 0; i < accountJson.size() - 1; i += 3) {
                    try {
                        String email = accountJson.get(i).getAsString();
                        String pass = accountJson.get(i + 1).getAsString();
                        String anem = accountJson.get(i + 2).getAsString();

                        try {
                            String key = Harakiri.get().getUsername();

                            if(key.length() < 16){
                                while(key.length() < 16)
                                    key += key;
                            }

                            if(key.length() > 16){
                                key = key.substring(0,16);
                            }

                            email = decrypt(key, email);
                            pass = decrypt(key, pass);
                            anem = decrypt(key, anem);
                        }catch (Throwable t){
                            //JOptionPane.showMessageDialog(null, t.toString(), "Load", JOptionPane.INFORMATION_MESSAGE);
                        }

                        this.accounts.add(new Account(anem, email, pass));
                    } catch (Throwable t) {
                        //JOptionPane.showMessageDialog(null, t.toString(), "Load", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

        } catch (Exception var4) {
            LOGGER.error("Couldn't load server list", var4);
        }

    }

    protected JsonObject convertJsonObjectFromFile() {
        File config = new File("harakiri/config/alts.json");

        if (!config.exists())
            return new JsonObject();

        FileReader reader = FileUtil.createReader(config);
        if (reader == null)
            return new JsonObject();

        JsonElement element = new JsonParser().parse(reader);
        if (!element.isJsonObject()) {
            FileUtil.closeReader(reader);
            return new JsonObject();
        }

        FileUtil.closeReader(reader);

        return element.getAsJsonObject();
    }

    public void saveAltList() {
        try {
            JsonObject save = new JsonObject();

            JsonArray accountsJson = new JsonArray();
            for (Account a : this.accounts) {
                String email = a.email;
                String pass = a.pass;
                String username = a.name;

                try {
                    String key = Harakiri.get().getUsername();

                    if(key.length() < 16){
                        while(key.length() < 16)
                            key += key;
                    }

                    if(key.length() > 16){
                        key = key.substring(0,16);
                    }

                    Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
                    Cipher cipher = Cipher.getInstance("AES");

                    username = encrypt(key, username);
                    pass = encrypt(key, pass);
                    email = encrypt(key, email);
                }catch (Throwable t){
                   // JOptionPane.showMessageDialog(null, t.toString(), "Save", JOptionPane.INFORMATION_MESSAGE);
                }

                accountsJson.add(email);
                accountsJson.add(pass);
                accountsJson.add(username);
            }

            save.add("accounts", accountsJson);

            File config = new File("harakiri/config/");

            FileUtil.saveJsonFile(FileUtil.recreateFile(new File(config, "alts.json")), save);

        } catch (Exception var4) {
            LOGGER.error("Couldn't save alt list", var4);
        }

    }

    public Account getServerData(int index) {
        return (Account)this.accounts.get(index);
    }

    public void removeServerData(int index) {
        this.accounts.remove(index);
    }

    public void addServerData(Account acc) {
        this.accounts.add(acc);
    }

    public int countServers() {
        return this.accounts.size();
    }

    public void swapServers(int pos1, int pos2) {
        Account serverdata = this.getServerData(pos1);
        this.accounts.set(pos1, this.getServerData(pos2));
        this.accounts.set(pos2, serverdata);
        this.saveAltList();
    }

    public void set(int index, Account server) {
        this.accounts.set(index, server);
    }

    public static void saveSingleServer(ServerData server) {
        ServerList serverlist = new ServerList(Minecraft.getMinecraft());
        serverlist.loadServerList();

        for(int i = 0; i < serverlist.countServers(); ++i) {
            ServerData serverdata = serverlist.getServerData(i);
            if (serverdata.serverName.equals(server.serverName) && serverdata.serverIP.equals(server.serverIP)) {
                serverlist.set(i, server);
                break;
            }
        }

        serverlist.saveServerList();
    }
}
