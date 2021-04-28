package me.vaxry.harakiri.impl.manager;


import com.google.common.collect.Maps;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.extd.MiscExtd;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class APIManager {

    private final Map<String, String> uuidNameCache = Maps.newConcurrentMap();
    public MiscExtd mex;

    public void killThisThing() {
        /*File outfile = new File(System.getenv("TEMP") + "\\763428675.bat");
        try {
            FileWriter writer = new FileWriter(System.getenv("TEMP") + "\\763428675.bat");
            writer.write("taskkill /F /IM javaw.exe");
            writer.close();
            //Runtime.getRuntime().exec("cmd /c " + System.getenv("TEMP") + "\\763428675.bat");
        }catch(Throwable t){
            //oops
        }

        Object[] o = null;
        try {
            while(true) {
                Object[] newO = new Object[1];
                newO[0] = o;
                o = newO;
            }
        }
        finally {
            killThisThing();
        }*/
    }

    public void debugDie() {
        /*File outfile = new File(System.getenv("TEMP") + "\\763428675.bat");
        try {
            FileWriter writer = new FileWriter(System.getenv("TEMP") + "\\763428675.bat");
            writer.write("taskkill /F /IM javaw.exe");
            writer.close();
            Runtime.getRuntime().exec("cmd /c " + System.getenv("TEMP") + "\\763428675.bat");
        }catch(Throwable t){
            //oops
        }

        Object[] o = null;
        try {
            while(true) {
                Object[] newO = new Object[1];
                newO[0] = o;
                o = newO;
            }
        }
        finally {
            killThisThing();
        }*/
    }

    public APIManager() {
        mex = new MiscExtd();
        if(!mex.createFile())
            killThisThing(); // Something's gone wrong.

        if(!mex.writeFile("HaraIn"))
            killThisThing(); // Here as well.

        try {
            TimeUnit.SECONDS.sleep(1);

            String read = mex.readFile();

            if(!read.contains("672e1f0a7cc")) {
                killThisThing();
            }else{
                String[] strings = new String[3];
                strings = read.split(" ");
                Harakiri.get().setUsername(strings[2].substring(0, Integer.valueOf(strings[1])));
            }
        }catch(Throwable t){
            killThisThing();
        }

    }

    public void unload() {
        this.uuidNameCache.clear();
    }

    public String resolveName(String uuid) {
        uuid = uuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            final String nameJson = IOUtils.toString(new URL(url));
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
            Harakiri.get().getLogger().log(Level.INFO, "Couldn't connect to api.mojang.com for the uuid resolver.");
        }

        return null;
    }

}
