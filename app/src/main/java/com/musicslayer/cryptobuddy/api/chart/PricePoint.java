package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.transaction.Timestamp;

import java.io.IOException;
import java.math.BigDecimal;

// A price point represents a price at a specific point in time.
public class PricePoint implements DataBridge.SerializableToJSON {
    public Timestamp timestamp;
    public BigDecimal price;

    public PricePoint(Timestamp timestamp, BigDecimal price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "1", String.class)
                .serialize("timestamp", timestamp, Timestamp.class)
                .serialize("price", price, BigDecimal.class)
                .endObject();
    }

    public static PricePoint deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        PricePoint pricePoint;

        if("1".equals(version)) {
            Timestamp timestamp = o.deserialize("timestamp", Timestamp.class);
            BigDecimal price = o.deserialize("price", BigDecimal.class);
            o.endObject();

            pricePoint = new PricePoint(timestamp, price);
        }
        else {
            throw new IllegalStateException();
        }

        return pricePoint;
    }
}
