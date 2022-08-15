package com.oyashchenko.flink.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class PositionDeleteEvent {

    private Integer legalEntityId;
    private LocalDateTime eventTime;

    public PositionDeleteEvent() {
        this.eventTime = LocalDateTime.now();
    }
    public PositionDeleteEvent(Integer legalEntityId) {
        this.legalEntityId = legalEntityId;
        this.eventTime = LocalDateTime.now();
    }

    public Integer getLegalEntityId() {
        return legalEntityId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setLegalEntityId(Integer legalEntityId) {
        this.legalEntityId = legalEntityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionDeleteEvent that = (PositionDeleteEvent) o;
        return Objects.equals(legalEntityId, that.legalEntityId) && Objects.equals(eventTime, that.eventTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(legalEntityId, eventTime);
    }

    @Override
    public String toString() {
        return "PositionDeleteEvent{" +
                "legalEntityId=" + legalEntityId +
                ", eventTime=" + eventTime +
                '}';
    }
}
