package cn.nukkit.customblock.comparator;

import com.nukkitx.nbt.NbtMap;

import java.util.Comparator;

public class AlphabetPaletteComparator implements Comparator<NbtMap> {
    public static final AlphabetPaletteComparator INSTANCE = new AlphabetPaletteComparator();

    @Override
    public int compare(NbtMap o1, NbtMap o2) {
        return getIdentifier(o1).compareToIgnoreCase(getIdentifier(o2));
    }

    private String getIdentifier(NbtMap state) {
        return state.getString("name");
    }
}
