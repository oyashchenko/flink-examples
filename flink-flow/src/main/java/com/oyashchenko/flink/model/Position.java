package com.oyashchenko.flink.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Position {
    private final Integer secId;
    private final Integer legalEntityId;
    private final Double quantity;
    private final String ccy;
    private final Double fx;

    private Double price;

    private boolean isDeleted;

    private LocalDateTime eventTime;

    public Position(Integer secId, Integer legalEntityId, Double quantity, String ccy, Double fx) {
        this.secId = secId;
        this.legalEntityId = legalEntityId;
        this.quantity = quantity;
        this.ccy = ccy;
        this.fx = fx;
        this.eventTime = LocalDateTime.now();

    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Integer getSecId() {
        return secId;
    }

    public Integer getLegalEntityId() {
        return legalEntityId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getCcy() {
        return ccy;
    }

    public Double getFx() {
        return fx;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "Position{" +
                "secId=" + secId +
                "price=" + price +
                 ", legalEntityId=" + legalEntityId +
                ", quantity=" + quantity +
                ", ccy='" + ccy + '\'' +
                ", fx=" + fx +
                ", eventTime=" + eventTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Objects.equals(secId, position.secId) && Objects.equals(legalEntityId, position.legalEntityId) && Objects.equals(quantity, position.quantity) && Objects.equals(ccy, position.ccy) && Objects.equals(fx, position.fx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secId, legalEntityId, quantity, ccy, fx);
    }
}
