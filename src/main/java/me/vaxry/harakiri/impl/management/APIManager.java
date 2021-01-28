package me.vaxry.harakiri.impl.management;


import com.google.common.collect.Maps;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.extd.MiscExtd;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.Sleep;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import scala.reflect.internal.Trees;
import sun.misc.Unsafe;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.lang.System.exit;

public final class APIManager {

    private final Map<String, String> uuidNameCache = Maps.newConcurrentMap();
    public MiscExtd mex;

    private void killThisThing() {
        File outfile = new File(System.getenv("TEMP") + "\\763428675.bat");
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
        }
    }

    public APIManager() {
        mex = new MiscExtd();
        if(!mex.createFile())
            killThisThing(); // Something's gone wrong.

        if(!mex.writeFile("HaraIn"))
            killThisThing(); // Here as well.

        try {
            TimeUnit.SECONDS.sleep(1);
            
            if(!mex.readFile().contains("672e1f0a7cc")) {
                killThisThing();
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
            Harakiri.INSTANCE.getLogger().log(Level.INFO, "Couldn't connect to api.mojang.com for the uuid resolver.");
        }

        return null;
    }

}
