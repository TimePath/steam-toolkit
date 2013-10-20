package com.timepath.steam.io;

import com.timepath.DataUtils;
import com.timepath.DateUtils;
import com.timepath.Utils;
import com.timepath.io.utils.Savable;
import com.timepath.swing.TreeUtils;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * https://github.com/harvimt/steam_launcher/blob/master/binvdf.py
 * https://github.com/barneygale/bvdf/blob/master/bvdf.py
 * https://github.com/DHager/hl2parse
 * http://cs.rin.ru/forum/viewtopic.php?f=20&t=61506&hilit=appinfo
 * http://cs.rin.ru/forum/viewtopic.php?f=20&t=62438&hilit=packageinfo
 * http://media.steampowered.com/steamcommunity/public/images/apps/[appID]/[sha].[ext]
 * http://cdr.xpaw.ru/app/5/#section_info
 * http://hlssmod.net/he_code/public/tier1/KeyValues.h
 * http://hpmod.googlecode.com/svn/trunk/tier1/KeyValues.cpp
 *
 * @author TimePath
 */
public class BVDF implements Savable {

    private static final Logger LOG = Logger.getLogger(BVDF.class.getName());

    private static int binaryFailurePosition, binaryFailureByte;

    private DataNode root;

    public DataNode getRoot() {
        return root;
    }

    public BVDF() {
        root = new DataNode("BVDF");
    }

    @Override
    public void readExternal(InputStream in) {
        try {
            byte[] buf = new byte[in.available()];
            readExternal(ByteBuffer.wrap(buf));
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void readExternal(ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int magic = buf.getInt();
        switch(magic) {
            case 0x07564426:
                //<editor-fold defaultstate="collapsed" desc="AppInfo">
                int appUniverse = buf.getInt();
                root.add(new DataNode("universe", Universe.getName(appUniverse)));
                for(;;) {
                    int appID = buf.getInt();
                    if(appID == 0) {
                        break;
                    }
                    DataNode c = new DataNode(appID);
                    root.add(c);

                    int size = buf.getInt(); // skip this many bytes to reach the next entry
//                    c.add(new DataNode("size", size));

                    int appPosition = buf.position();

                    ByteBuffer entrySlice = DataUtils.getSlice(buf, size);

                    int appInfoState = entrySlice.getInt();
                    c.add(new DataNode("state", AppInfoState.getName(appInfoState)));

                    long lastUpdated = entrySlice.getInt();
                    String formattedDate = DateUtils.parse(lastUpdated);
                    DataNode dateNode = new DataNode("lastUpdated", formattedDate);
                    c.add(dateNode);

                    long token = entrySlice.getLong();
                    c.add(new DataNode("token", token));

                    byte[] sha = new byte[20];
                    entrySlice.get(sha);
                    c.add(new DataNode("sha", Arrays.toString(sha)));

                    int changeNumber = entrySlice.getInt();
                    c.add(new DataNode("changeNumber", changeNumber));

                    DataNode sections = new DataNode("Sections");
                    c.add(sections);
                    for(;;) {
                        byte section = entrySlice.get();
                        if(section == 0) {
                            break;
                        }
                        DataNode sectionNode = new DataNode(Section.get(section));
                        sections.add(sectionNode);
                        int sectionPosition = entrySlice.position();
                        DataNode binarySlice = parseBinaryData(entrySlice);
                        if(binarySlice != null) {
                            TreeUtils.moveChildren(binarySlice, sectionNode);
//                        c.removeFromParent(); // TEMP
                        } else {
                            Object[] vars = new Object[] {appID, Integer.toHexString(
                                binaryFailureByte), Section.get(section),
                                                          appPosition + sectionPosition,
                                                          appPosition + binaryFailurePosition};
                            LOG.log(Level.WARNING,
                                    "app: {0} byte: {1}, sec: {2}, secoff: {3} totaloff: {4}", vars);
                            break;
                        }
                    }
                }
                //</editor-fold>
                break;
            case 0x06565527:
                //<editor-fold defaultstate="collapsed" desc="PackageInfo">
                int packageUniverse = buf.getInt();
                root.add(new DataNode("universe", Universe.getName(packageUniverse)));
                for(;;) {
                    int appID = buf.getInt();
                    if(appID == -1) {
                        break;
                    }
                    DataNode c = new DataNode("#", appID);
                    root.add(c);

                    byte[] sha = new byte[20];
                    buf.get(sha);
                    c.add(new DataNode("sha", Arrays.toString(sha)));

                    int changeNumber = buf.getInt();
                    c.add(new DataNode("changeNumber", changeNumber));

                    DataNode bin = new DataNode();
                    bin.name = "Binary Data";
                    TreeUtils.moveChildren(parseBinaryData(buf), bin);
                    c.add(bin);
                }
                //</editor-fold>
                break;
            default:
                //<editor-fold defaultstate="collapsed" desc="Generic .bin">
                buf.position(0);
                DataNode bvdf = parseBinaryData(buf);
                if(bvdf != null) {
                    TreeUtils.moveChildren(bvdf, root);
                } else {
                    Object[] vars = new Object[] {Integer.toHexString(binaryFailureByte),
                                                  binaryFailurePosition};
                    LOG.log(Level.WARNING, "err: {0}, off: {1}", vars);
                }
                //</editor-fold>
                break;
        }
    }

    @Override
    public void writeExternal(OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static DataNode parseBinaryData(ByteBuffer buffer) {
        DataNode parent = new DataNode();
        parent.name = "<joiner>";
        for(;;) {
            int typeNum = buffer.get();
            ValueType type = ValueType.get(typeNum);
            LOG.log(Level.FINE, "Type : {0}", type);

            if(type == null) { // parsing error
                binaryFailureByte = typeNum;
                binaryFailurePosition = buffer.position() - 1;
                return null;
            }

            if(type == ValueType.TYPE_NUMTYPES) {
                LOG.log(Level.FINE, "No more peers");
                break;
            }

            DataNode dat = new DataNode();
            dat.type = type;
            String token = getString(buffer);
            dat.name = token;

            //<editor-fold defaultstate="collapsed" desc="Different cases">
            switch(type) {
                case TYPE_NONE:
                    LOG.log(Level.FINE, "Node has children");
                    DataNode recur = parseBinaryData(buffer);
                    if(recur == null) {
                        return null;
                    }
                    TreeUtils.moveChildren(recur, dat);
                    break;

                case TYPE_STRING:
                    String stringValue = getString(buffer);
                    dat.value = (stringValue);
                    LOG.log(Level.FINE, "String value: {0}", Arrays.toString(stringValue.getBytes()));
                    break;

                case TYPE_WSTRING:
                    LOG.log(Level.SEVERE, "Detected {0}, this should never happen", type);
                    break;

                case TYPE_INT:
                    int intValue = buffer.getInt();
                    dat.value = (intValue);
                    LOG.log(Level.FINE, "Int value: {0}", intValue);
                    break;

                case TYPE_UINT64:
                    long longValue = buffer.getLong();
                    dat.value = (longValue);
                    LOG.log(Level.FINE, "Long value: {0}", longValue);
                    break;

                case TYPE_FLOAT:
                    float floatValue = buffer.getFloat();
                    dat.value = (floatValue);
                    LOG.log(Level.FINE, "Float value: {0}", floatValue);
                    break;

                case TYPE_COLOR:
                    Color colorValue = new Color(buffer.get(), buffer.get(), buffer.get(),
                                                 buffer.get());
                    dat.value = (colorValue);
                    LOG.log(Level.FINE, "Color value: {0}", colorValue);
                    break;

                case TYPE_PTR:
                    long pointerValue = buffer.getInt();
                    dat.value = (pointerValue);
                    LOG.log(Level.FINE, "Pointer value: {0}", pointerValue);
                    break;

                default:
                    LOG.log(Level.SEVERE, "Unhandled data type {0}", type);
                    break;
            }
            //</editor-fold>

            parent.add(dat);
        }
        return parent;
    }

    private static String getString(ByteBuffer buffer) {
        int originalPosition = buffer.position();
        int size = buffer.remaining(); // Source's buffer isn't big enough for some CDR entries
        ByteBuffer textBuffer = DataUtils.getTextBuffer(DataUtils.getSafeSlice(buffer, size), true)[0];
        int length = textBuffer.remaining();
        buffer.position(originalPosition + length);
        textBuffer.limit(length - 1);
        String token = Charset.forName("UTF-8").decode(textBuffer.duplicate()).toString();
        LOG.log(Level.FINER, "Token {0} = {1}", new Object[] {token, Utils.hex(token.getBytes())});
        return token;
    }

    public static class DataNode extends DefaultMutableTreeNode {

        DataNode(Object obj) {
            this.name = obj.toString();
        }

        DataNode(String name, Object obj) {
            this.name = name;
            this.value = obj;
        }

        private String name;

        public Object value;

        private ValueType type;

        @Override
        public String toString() {
            String splitComp = "";
            if(name != null && value != null) {
                splitComp = ": ";
            }
            return (name == null ? "" : name) + splitComp + (value == null ? "" : value + " [" + value.getClass().getSimpleName() + "]");
        }

        DataNode() {
        }

        public DataNode get(String key) {
            DataNode node;
            for(Object o : this.children) {
                if(!(o instanceof DataNode)) {
                    continue;
                }
                node = (DataNode) o;
                if(node.name.equals(key)) {
                    return node;
                }
            }
            return null;
        }

    }

    //<editor-fold defaultstate="collapsed" desc="Enums">
    private enum Universe {

        INVALID(0),
        PUBLIC(1),
        BETA(2),
        INTERNAL(3),
        DEV(4);

        Universe(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static String getName(int i) {
            Universe[] search = Universe.values();
            for(Universe search1 : search) {
                if(search1.ID() == i) {
                    return search1.name();
                }
            }
            return "UNKNOWN (" + i + ")";
        }

    }

    private enum AppInfoState {

        UNAVAILBALE(1),
        AVAILABLE(2);

        AppInfoState(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static String getName(int i) {
            AppInfoState[] search = AppInfoState.values();
            for(AppInfoState search1 : search) {
                if(search1.ID() == i) {
                    return search1.name();
                }
            }
            LOG.log(Level.WARNING, "Unknown {0}: {1}", new Object[] {
                AppInfoState.class.getSimpleName(), i});
            return "UNKNOWN (" + i + ")";
        }

    }

    /**
     * Can be found in steamclient native library, EAppInfoSection
     */
    private enum Section {

        UNKNOWN(0),
        ALL(1),
        COMMON(2),
        EXTENDED(3),
        CONFIG(4),
        STATS(5),
        INSTALL(6),
        DEPOTS(7),
        VAC(8),
        DRM(9),
        UFS(10),
        OGG(11),
        ITEMS(12),
        POLICIES(13),
        SYSREQS(14),
        COMMUNITY(15),
        SERVERONLY(16),
        SERVERANDWGONLY(17);

        Section(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static String get(int i) {
            Section[] search = Section.values();
            for(Section search1 : search) {
                if(search1.ID() == i) {
                    return search1.name();
                }
            }
            return "UNKNOWN (" + i + ")";
        }

    }

    private enum SteamAppState {

        Invalid(0x00000000),
        Uninstalled(0x00000001),
        UpdateRequired(0x00000002),
        FullyInstalled(0x00000004),
        Encrypted(0x00000008),
        Locked(0x00000010),
        FilesMissing(0x00000020),
        AppRunning(0x00000040),
        FilesCorrupt(0x00000080),
        UpdateRunning(0x00000100),
        UpdatePaused(0x00000200),
        UpdateStarting(0x00000400),
        Uninstalling(0x00000800),
        Reconfiguring(0x00001000),
        Preallocating(0x00002000),
        Downloading(0x00004000),
        Staging(0x00008000),
        Comitting(0x00010000),
        Validating(0x00020000),
        UpdateStopping(0x00040000);

        SteamAppState(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static String get(int i) {
            for(SteamAppState s : SteamAppState.values()) {
                if(s.ID() == i) {
                    return s.name();
                }
            }
            return "UNKNOWN (" + i + ")";
        }

    };

    private enum AppInfoSectionPropagationType {

        Invalid(0),
        Public(1),
        OwnersOnly(2),
        ServerOnly(3),
        ClientOnly(4),
        ServerAndWGOnly(5);

        AppInfoSectionPropagationType(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static String get(int i) {
            for(AppInfoSectionPropagationType s : AppInfoSectionPropagationType.values()) {
                if(s.ID() == i) {
                    return s.name();
                }
            }
            return "UNKNOWN (" + i + ")";
        }

    };

    private enum ValueType {

        TYPE_NONE(0),
        TYPE_STRING(1),
        TYPE_INT(2),
        TYPE_FLOAT(3),
        TYPE_PTR(4),
        TYPE_WSTRING(5),
        TYPE_COLOR(6),
        TYPE_UINT64(7),
        TYPE_NUMTYPES(8);

        ValueType(int i) {
            this.id = i;
        }

        private final int id;

        public int ID() {
            return id;
        }

        public static ValueType get(int i) {
            for(ValueType s : ValueType.values()) {
                if(s.ID() == i) {
                    return s;
                }
            }
            return null;
        }

        public static String getName(int i) {
            for(ValueType s : ValueType.values()) {
                if(s.ID() == i) {
                    return s.name();
                }
            }
            return "UNKNOWN (" + i + ")";
        }

    };
    //</editor-fold>
}