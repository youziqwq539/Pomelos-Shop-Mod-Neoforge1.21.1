package shop.pomelo.mod.shop;

import java.util.UUID;

public class ShopCategory {
    private final String id;
    private String displayName;
    private final UUID creatorUUID;
    private final String creatorName;
    private int orderIndex;
    private String parentId; // 父分类ID，为空表示一级分类

    public ShopCategory(String id, String displayName, UUID creatorUUID, String creatorName) {
        this(id, displayName, creatorUUID, creatorName, 0, null);
    }

    public ShopCategory(String id, String displayName, UUID creatorUUID, String creatorName, int orderIndex) {
        this(id, displayName, creatorUUID, creatorName, orderIndex, null);
    }

    public ShopCategory(String id, String displayName, UUID creatorUUID, String creatorName, int orderIndex, String parentId) {
        this.id = id;
        this.displayName = displayName;
        this.creatorUUID = creatorUUID;
        this.creatorName = creatorName;
        this.orderIndex = orderIndex;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getCreatorUUID() {
        return creatorUUID;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isSubCategory() {
        return parentId != null && !parentId.isEmpty();
    }
}
