package com.oyashchenko.cache.model;

import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.time.LocalDateTime;
import java.util.Objects;

@Portable
public class Portfolio {
    @PortableProperty(value = 0)

    @QuerySqlField(name = "legalEntityId")
    private Integer legalEntityId;

    @PortableProperty(value = 1)
    @QuerySqlField(name = "pnl")
    private Double pnl = 0d;

    @PortableProperty(value = 2)
    @QuerySqlField(name = "gmv")
    private Double gmv = 0d;

    @PortableProperty(value = 3)
    @QuerySqlField(name = "lmv")
    private Double lmv = 0d;

    @PortableProperty(value = 4)
    @QuerySqlField(name = "smv")
    private Double smv =0d;

    @PortableProperty(value = 6)
    @QuerySqlField(name = "modificationTime")
    private LocalDateTime modificationTime;

    @PortableProperty(value = 5)
    @QuerySqlField(name = "createdOnTime")
    private LocalDateTime createdOnTime;

    @PortableProperty(value = 7)
    @QuerySqlField(name = "cobLoadTime")
    private LocalDateTime cobLoadTime;

    public Portfolio(Integer legalEntityId) {
        this.legalEntityId = legalEntityId;
        this.createdOnTime = LocalDateTime.now();
    }

    public Portfolio() {
        this.createdOnTime = LocalDateTime.now();
    }

    public Integer getLegalEntityId() {
        return legalEntityId;
    }

    public void addPnl(Double pnl) {
        this.pnl = this.pnl +  pnl;
    }

    public void removePnl(Double pnl) {
        this.pnl = this.pnl - pnl;
    }

    public void gmv(Double quantity) {
        if (quantity > 0) {
            this.lmv = this.lmv + quantity;
        } else {
            this.smv = this.smv + quantity;
        }
        this.gmv = this.gmv + Math.abs(quantity);
    }

    public void removeGmv(Double quantity) {
        if (quantity > 0) {
            this.lmv = this.lmv - quantity;
        } else {
            this.smv = this.smv + quantity;
        }
        this.gmv = this.gmv - Math.abs(quantity);
    }

    public Double getPnl() {
        return pnl;
    }

    public Double getGmv() {
        return gmv;
    }

    public void updateModificationDate() {
        modificationTime = LocalDateTime.now();
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return Objects.equals(legalEntityId, portfolio.legalEntityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(legalEntityId);
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "legalEntityId=" + legalEntityId +
                ", pnl=" + pnl +
                ", gmv=" + gmv +
                ", lmv=" + lmv +
                ", smv=" + smv +
                ", modificationTime=" + modificationTime +
                ", createdOnTime=" + createdOnTime +
                ", cobLoadTime=" + cobLoadTime +
                '}';
    }
}
