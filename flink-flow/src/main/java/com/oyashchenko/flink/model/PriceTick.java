package com.oyashchenko.flink.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PriceTick  implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String ric;
    private final Integer secId;
    private final Double price;
    private final String ccy;

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
