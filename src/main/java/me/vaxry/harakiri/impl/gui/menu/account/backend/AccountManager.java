package me.vaxry.harakiri.impl.gui.menu.account.backend;

import com.google.common.collect.Maps;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AccountManager {
    private static final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public AccountManager(final String name, final String password, boolean online) {
        if (online) {
            if (name != null && password != null) {
                (new Thread() {
                    public void run() {
                        AccountManager.loginPassword(name, password);
                    }
                }).start();
            } else {
                //OOps
            }
        } else {
            loginPasswordOffline(name);
        }
    }

    public static String resolveName(String uuid) {
        uuid = uuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            final String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (nameJson != null && nameJson.length() > 0) {
                final JSONArray jsonArray = (JSONArray) JSONValue.parseWithException(nameJson);
                if (jsonArray != null) {
                    final JSONObject latestName = (JSONObject) jsonArray.get(jsonArray.size() - 1);
                    if (latestName != null) {
                        return latestName.get("name").toString();
                    }
                }
            }
        } catch (IOException | ParseException e) {
            //e.printStackTrace();
        }

        return null;
    }

    public static void loginPasswordOffline(String username) {
        Minecraft.getMinecraft().session = new Session(username, username, username, "MOJANG");
    }

    public static String loginPassword(String username, String password) {
        if (username == null || username.length() <= 0 || password == null || password.length() <= 0)
            return "Error-1";
        YggdrasilAuthenticationService a = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication b = (YggdrasilUserAuthentication)a.createUserAuthentication(Agent.MINECRAFT);
        b.setUsername(username);
        b.setPassword(password);
        try {
            b.logIn();
            Minecraft.getMinecraft()
                    .session = new Session(b.getSelectedProfile().getName(), b.getSelectedProfile().getId().toString(), b.getAuthenticatedToken(), "MOJANG");
        } catch (AuthenticationException e) {
            return e.getMessage();
        }
        return b.getSelectedProfile().getName();
    }
}
