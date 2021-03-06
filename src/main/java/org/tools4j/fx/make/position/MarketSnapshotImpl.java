/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 fx-market-making (tools4j), Marco Terzer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.fx.make.position;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;
import org.tools4j.fx.make.util.StringUtil;

/**
 * Immutable implementation of {@link MarketSnapshot}. A {@link Builder} can be used to
 * construct a market snaphot.
 * <p>
 * This class thread safe.
 */
public class MarketSnapshotImpl implements MarketSnapshot {

	private final Map<Asset, Map<Asset, Double>> marketRates = new HashMap<>();

	public MarketSnapshotImpl(Map<? extends AssetPair<?, ?>, Double> marketRates) {
		for (final Map.Entry<? extends AssetPair<?, ?>, Double> e : marketRates.entrySet()) {
			final Asset base = Objects.requireNonNull(e.getKey().getBase(), "assetPair.base is null for " + e.getKey());
			final Asset terms = Objects.requireNonNull(e.getKey().getTerms(),
					"assetPair.terms is null for " + e.getKey());
			final Double rate = Objects.requireNonNull(e.getValue(), "rate value is null for " + e.getKey());
			Map<Asset, Double> rates = this.marketRates.get(base);
			if (rates == null) {
				rates = new HashMap<>();
				this.marketRates.put(base, rates);
			}
			rates.put(terms, rate);
		}
	}

	public double getRate(Asset from, Asset to) {
		Objects.requireNonNull(from, "from is null");
		Objects.requireNonNull(to, "to is null");
		if (from.equals(to)) {
			return 1;
		}
		final Double direct = getRateOrNull(from, to);
		if (direct != null) {
			return direct.doubleValue();
		}
		final Double indirect = getRateOrNull(to, from);
		if (indirect != null) {
			return 1 / indirect.doubleValue();
		}
		throw new IllegalArgumentException("no market rate present for: " + from + "/" + to);
	}

	private Double getRateOrNull(Asset from, Asset to) {
		final Map<Asset, Double> rates = marketRates.get(from);
		return rates != null ? rates.get(to) : null;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder implements MarketSnapshot.Builder {
		private final Map<AssetPair<?, ?>, Double> marketRates = new LinkedHashMap<>();

		public Builder withRate(AssetPair<?, ?> pair, double rate) {
			marketRates.put(pair, rate);
			return this;
		}

		public Builder withRate(Currency base, Currency terms, double rate) {
			return withRate(new CurrencyPair(base, terms), rate);
		}

		public MarketSnapshotImpl build() {
			return new MarketSnapshotImpl(marketRates);
		}

		@Override
		public String toString() {
			return "Builder@" + build();
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('{');
		boolean first = true;
		for (final Entry<Asset, Map<Asset, Double>> e1 : marketRates.entrySet()) {
			for (final Entry<Asset, Double> e2 : e1.getValue().entrySet()) {
				sb.append(first ? "" : ", ");
				sb.append(e1.getKey()).append('/').append(e2.getKey());
				sb.append('=').append(StringUtil.formatPrice(e2.getValue()));
			}
		}
		sb.append('}');
		return sb.toString();
	}
}
