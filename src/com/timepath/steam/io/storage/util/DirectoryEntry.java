package com.timepath.steam.io.storage.util;

import com.timepath.io.utils.ViewableData;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 *
 * @author timepath
 */
public abstract class DirectoryEntry implements ViewableData {

    public abstract int getItemSize();

    public abstract Object getAttributes();

    public abstract boolean isDirectory();

    public abstract String getName();

    public String getPath() {
        String path = (isDirectory() ? getName() : "").replaceAll("/", "");
        if(parent != null) {
            path = parent.getPath() + "/" + path;
        }
        return path;
    }

    public String getAbsoluteName() {
        return getPath() + getName();
    }

    public abstract Archive getArchive();

    public abstract void extract(File dir) throws IOException;

    public abstract boolean isComplete();

    public long getChecksum() {
        return -1;
    }

    public long calculateChecksum() {
        return -1;
    }

    public Icon getIcon() {
        if(this == getArchive().getRoot()) {
            return getArchive().getIcon();
        }
        if(isDirectory()) {
            return UIManager.getIcon("FileView.directoryIcon");
        } else if(!isComplete()) {
            return UIManager.getIcon("html.missingImage");
        } else {
            return UIManager.getIcon("FileView.fileIcon");
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private DirectoryEntry parent;

    private ArrayList<DirectoryEntry> children = new ArrayList<DirectoryEntry>();
    
    public ArrayList<DirectoryEntry> children() {
        return children;
    }

    public void add(DirectoryEntry c) {
        if(c == null || c == this) {
            return;
        }
        if(children.contains(c)) {
            return;
        }
        children.add(c);
        c.setParent(this);
    }

    public void addAll(Collection<? extends DirectoryEntry> all) {
        for(DirectoryEntry d : all) {
            add(d);
        }
    }

    public void remove(DirectoryEntry c) {
        if(c == null || c == this) {
            return;
        }
        if(!children.contains(c)) {
            return;
        }
        children.remove(c);
        c.removeFromParent();
    }

    public void removeAll(Collection<? extends DirectoryEntry> all) {
        for(DirectoryEntry d : all) {
            remove(d);
        }
    }

    public void setParent(DirectoryEntry newParent) {
        if(parent == newParent) {
            return;
        }
        if(parent != null) {
            parent.remove(this);
        }
        if(newParent != null) {
            newParent.add(this);
        }
        parent = newParent;
    }

    public void removeFromParent() {
        setParent(null);
    }

}