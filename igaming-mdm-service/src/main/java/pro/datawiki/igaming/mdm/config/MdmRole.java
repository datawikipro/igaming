package pro.datawiki.igaming.mdm.config;

public enum MdmRole {
    ALL,
    RESOLUTION,
    CATALOG;

    public static MdmRole fromString(String val) {
        if (val == null || val.isBlank()) {
            return ALL;
        }
        try {
            return MdmRole.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
