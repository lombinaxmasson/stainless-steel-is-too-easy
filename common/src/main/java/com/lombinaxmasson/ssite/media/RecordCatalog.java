package com.lombinaxmasson.ssite.media;

import net.minecraft.resources.ResourceLocation;

/** Stable media item identities; individual tracks are stored in the item data. */
public final class RecordCatalog {
    public static final int COMPARATOR_OUTPUT = 15;

    public static final String VINYL_ITEM = "vinyl_record";
    public static final String CD_ITEM = "cd_record";
    public static final String TEMPLATE_ITEM = "record_template_trackone";
    public static final String DATA_KEY = "ssite.record";

    public static final String TEMPLATE_PREFIX = "recordTemplate";
    // Preserve the generated item's registry id when the template becomes reusable for any track.
    public static final String TEMPLATE_MATERIAL = "TrackOne";
    public static final String STYLUS_PREFIX = "recordStylus";
    public static final String STYLUS_MATERIAL = "BlueSapphire";

    private RecordCatalog() {}

    public static ResourceLocation itemId(String path) {
        return ResourceLocation.tryBuild("ssite", path);
    }
}
