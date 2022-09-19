package deso.future_bot.bot.binance.model.trade;

import deso.future_bot.bot.binance.constant.BinanceApiConstants;
import deso.future_bot.bot.data.TradeType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class Order {

    public Order() {
    }

    public Order(long price, double amount, Long refId, long time, String explanation) {
        this.price = price;
        this.origQty = amount;
        this.refOrderId = refId;
        this.time = time;
        this.side = TradeType.BUY;
        this.explanation = explanation;
    }

    public Order(TradeType side, double price, double qty, long time, String explanation) {
        this.side = side;
        this.price = price;
        this.origQty = qty;
        this.explanation = explanation;
        this.time = time;
    }

    private String clientOrderId;

    private BigDecimal cumQuote;

    private Double executedQty;

    private Long orderId;

    private Long refOrderId;

    private double origQty;

    protected double price;

    private Boolean reduceOnly;

    private String positionSide;

    private String status;

    private BigDecimal stopPrice;

    private String symbol;

    private String timeInForce;

    private TradeType side;

    private Long updateTime;

    private String workingType;

    private String explanation;

    private Long time = System.currentTimeMillis();

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public BigDecimal getCumQuote() {
        return cumQuote;
    }

    public void setCumQuote(BigDecimal cumQuote) {
        this.cumQuote = cumQuote;
    }

    public Double getExecutedQty() {
        if (executedQty == null) {
            return 0.0;
        }
        return executedQty;
    }

    public void setExecutedQty(double executedQty) {
        this.executedQty = executedQty;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getOrigQty() {
        return origQty;
    }

    public void setOrigQty(double origQty) {
        this.origQty = origQty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Boolean getReduceOnly() {
        return reduceOnly;
    }

    public void setReduceOnly(Boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    public String getPositionSide() {
        return positionSide;
    }

    public void setPositionSide(String positionSide) {
        this.positionSide = positionSide;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public TradeType getSide() {
        return side;
    }

    public void setSide(TradeType side) {
        this.side = side;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getWorkingType() {
        return workingType;
    }

    public void setWorkingType(String workingType) {
        this.workingType = workingType;
    }

    public Long getRefOrderId() {
        return refOrderId;
    }

    public void setRefOrderId(Long refOrderId) {
        this.refOrderId = refOrderId;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setExecutedQty(Double executedQty) {
        this.executedQty = executedQty;
    }

    public String getExplanation() {
        return explanation;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("clientOrderId", clientOrderId).append("cumQuote", cumQuote).append("executedQty", executedQty)
                .append("orderId", orderId).append("origQty", origQty).append("price", price)
                .append("reduceOnly", reduceOnly).append("positionSide", positionSide).append("status", status)
                .append("stopPrice", stopPrice).append("symbol", symbol).append("timeInForce", timeInForce)
                .append("type", side).append("updateTime", updateTime).append("workingType", workingType).toString();
    }

}
