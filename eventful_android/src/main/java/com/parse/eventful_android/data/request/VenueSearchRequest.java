package com.parse.eventful_android.data.request;

public class VenueSearchRequest extends SearchRequest {
    private SortOrder sortOrder;

    public VenueSearchRequest() {
        this.sortOrder = SortOrder.POPULARITY;
    }

    public String getSortOrder() {
        return this.sortOrder.toString().toLowerCase();
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public enum SortOrder {
        RELEVANCE,
        DATE,
        EVENT_COUNT,
        POPULARITY;

        SortOrder() {
        }
    }
}
