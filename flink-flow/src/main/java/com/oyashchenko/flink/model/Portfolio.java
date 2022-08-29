package com.oyashchenko.flink.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.time.LocalDateTime;

public class Portfolio {
    @QuerySqlField(name = "legalEntityId")
    private Integer legalEntityId;

    @QuerySqlField(name = "pnl")
    private Double pnl = 0d;
    @QuerySqlField(name = "gmv")
    private Double gmv = 0d;
    @QuerySqlField(name = "lmv")
    private Double lmv = 0d;
    @QuerySqlField(name = "smv")
    private Double smv =0d;
    @QuerySqlField(name = "modificationTime")
    private LocalDateTime modificationTime;
    @QuerySqlField(name = "createdOnTime")
    private LocalDateTime createdOnTime;
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

    public void updateModificationDate() {
        modificationTime = LocalDateTime.now();
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }
}
