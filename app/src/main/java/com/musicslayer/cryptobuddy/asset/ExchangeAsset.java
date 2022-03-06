package com.musicslayer.cryptobuddy.asset;

// Instances represent an asset stored on an exchange, with subclasses existing for each supported exchange.
// For example, 500 MATIC on an exchange may not exist on a specific network until it is withdrawn.
// Therefore, we cannot say whether it is a Coin or a Token in the traditional sense.
// Also, assets on an exchange do not have fiat values given by aggregate price data, such as what CoinGecko gives.
// The value of the asset in Fiat must be calculated based on the particular exchange's spot price.

abstract public class ExchangeAsset extends Asset {
}
