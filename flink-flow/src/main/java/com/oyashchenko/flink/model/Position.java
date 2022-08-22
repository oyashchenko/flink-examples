package com.oyashchenko.flink.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.time.LocalDateTime;
import java.util.Objects;

public class Position {

    @QuerySqlField(name = "secId")
    private final Integer secId;
    @QuerySqlField(name = "legalEntityId")
    private final Integer legalEntityId;
    @QuerySqlField(name = "quantity")
    private final Double quantity;
    @QuerySqlField(name = "ccy")
    private final String ccy;
    @QuerySqlField(name = "fx")
    private final Double fx;
    @QuerySqlField(name = "price")
    private Double price;

    @QuerySqlField(name = "pnl")
    private Double pnl;

    @QuerySqlField(name = "isDeleted")
    private boolean isDeleted;

    @QuerySqlField(name = "eventTime")
    private final LocalDateTime eventTime;


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

    public String getKey(){
        return this.legalEntityId + ":" + this.secId;
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

    public void setPnl(Double pnl) {
        this.pnl = pnl;
    }

    public Double getPnl() {
        return pnl;
    }
}
