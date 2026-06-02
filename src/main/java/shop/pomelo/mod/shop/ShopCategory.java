package shop.pomelo.mod.shop;

import java.util.UUID;

public class ShopCategory {
    private final String id;
    private final String displayName;
    private final UUID creatorUUID;
    private final String creatorName;

    public ShopCategory(String id, String displayName, UUID creatorUUID, String creatorName) {
        this.id = id;
        this.displayName = displayName;
        this.creatorUUID = creatorUUID;
        this.creatorName = creatorName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getCreatorUUID() {
        return creatorUUID;
    }

    public String getCreatorName() {
        return creatorName;
    }
}
