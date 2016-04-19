package com.parse.eventful_android.data.request;

public class PerformerSearchRequest extends SearchRequest {
    private SortOrder sortOrder;

    public PerformerSearchRequest() {
        this.sortOrder = SortOrder.RELEVANCE;
    }

    public String getSortOrder() {
        return this.sortOrder.toString().toLowerCase();
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public enum SortOrder {
        RELEVANCE,
        NAME,
        CATEGORY,
        MEMBER_COUNT,
        PERFORMER,
        CREATED;

        SortOrder() {
        }
    }
}
