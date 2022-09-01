package com.oyashchenko.cache.model;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.time.LocalDateTime;

@Portable
public class PriceTick  implements Serializable {
    private static final long serialVersionUID = 1L;
    @PortableProperty(value = 0)
    @QuerySqlField(name = "ric")
    private String ric;
    @PortableProperty(value = 1)
    @QuerySqlField(name = "secId", index = true)
    private Integer secId;
    @PortableProperty(value = 2)
    @QuerySqlField(name = "price")
    private Double price;
    @PortableProperty(value = 3)
    @QuerySqlField(name = "ccy")
    private String ccy;

    @PortableProperty(value = 4)
    @QuerySqlField(name = "eventTime")
    private LocalDateTime eventTime;

    public PriceTick(Integer secId, String ric, Double price, String ccy) {
        this.secId = secId;
        this.ric = ric;
        this.price = price;
        this.ccy = ccy;
        this.eventTime = LocalDateTime.now();
    }

    public PriceTick() {

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
