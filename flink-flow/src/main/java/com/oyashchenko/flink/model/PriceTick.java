package com.oyashchenko.flink.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PriceTick  implements Serializable {
    private static final long serialVersionUID = 1L;
    @QuerySqlField(name = "ric")
    private final String ric;
    @QuerySqlField(name = "secId", index = true)
    private final Integer secId;
    @QuerySqlField(name = "price")
    private final Double price;
    @QuerySqlField(name = "ccy")
    private final String ccy;

    @QuerySqlField(name = "eventTime")
    private final LocalDateTime eventTime;

    public PriceTick(Integer secId, String ric, Double price, String ccy) {
        this.secId = secId;
        this.ric = ric;
        this.price = price;
        this.ccy = ccy;
        this.eventTime = LocalDateTime.now();
    }

    public Integer getSecId() {
        return secId;
    }

    public Double getPrice() {
        return price;
    }


    public String getCcy() {
        return ccy;
    }

    public String getRic() {
        return ric;
    }

    @Override
    public String toString() {
        return "PriceTick{" +
                "ric='" + ric + '\'' +
                ", secId=" + secId +
                ", price=" + price +
                ", ccy='" + ccy + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
