package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.math.BigDecimal;

// TODO Do these Chart classes need to be Parcelable?

// A candle represents an OHLC quartet at a specific point in time.
// (Open, High, Low, Close)
public class Candle implements DataBridge.SerializableToJSON{
    public Timestamp timestamp;
    public BigDecimal openPrice;
    public BigDecimal highPrice;
    public BigDecimal lowPrice;
    public BigDecimal closePrice;

    public Candle(Timestamp timestamp, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice) {
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("openPrice", openPrice, BigDecimal.class)
                .serialize("highPrice", highPrice, BigDecimal.class)
                .serialize("lowPrice", lowPrice, BigDecimal.class)
                .serialize("closePrice", closePrice, BigDecimal.class)
                .endObject();
    }

    public static Candle deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Candle candle;

        if("1".equals(version)) {
            Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
            BigDecimal openPrice = o.deserialize("openPrice", BigDecimal.class);
            BigDecimal highPrice = o.deserialize("highPrice", BigDecimal.class);
            BigDecimal lowPrice = o.deserialize("lowPrice", BigDecimal.class);
            BigDecimal closePrice = o.deserialize("closePrice", BigDecimal.class);
            o.endObject();

            candle = new Candle(timestamp, openPrice, highPrice, lowPrice, closePrice);
        }
        else {
            throw new IllegalStateException();
        }

        return candle;
    }
}
