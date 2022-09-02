package com.oyashchenko.cache.model;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.time.LocalDateTime;
import java.util.Objects;
@Portable
public class Position {

    @PortableProperty(value = 0)
    @QuerySqlField(name = "secId")
    private Integer secId;

    @PortableProperty(value = 1)
    @QuerySqlField(name = "legalEntityId")
    private Integer legalEntityId;
    @PortableProperty(value = 2)
    @QuerySqlField(name = "quantity")
    private Double quantity;
    @PortableProperty(value = 3)
    @QuerySqlField(name = "ccy")
    private String ccy;
    @PortableProperty(value = 4)
    @QuerySqlField(name = "fx")
    private Double fx;

    @PortableProperty(value = 5)
    @QuerySqlField(name = "price")
    private Double price;

    @PortableProperty(value = 6)
    @QuerySqlField(name = "pnl")
    private Double pnl;

    @PortableProperty(value = 7)
    @QuerySqlField(name = "isDeleted")
    private boolean isDeleted;

    @PortableProperty(value = 8)
    @QuerySqlField(name = "eventTime")
    private LocalDateTime eventTime;

    @PortableProperty(value = 8)
    @QuerySqlField(name = "modificationTime")
    private LocalDateTime modificationTime;




    public Position(Integer secId, Integer legalEntityId, Double quantity, String ccy, Double fx) {
        this.secId = secId;
        this.legalEntityId = legalEntityId;
        this.quantity = quantity;
        this.ccy = ccy;
        this.fx = fx;
        this.eventTime = LocalDateTime.now();

    }

    public Position() {}

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
        modificationTime  = LocalDateTime.now();
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
                ", isDeleted=" + isDeleted +
                ", modificationTime=" + modificationTime +
                "}";
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
