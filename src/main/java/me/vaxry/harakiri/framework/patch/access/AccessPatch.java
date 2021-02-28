package me.vaxry.harakiri.framework.patch.access;

/**
 * Author Seth
 * 6/27/2019 @ 2:22 AM.
 */
public class AccessPatch {

    private String file;

    public AccessPatch(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
