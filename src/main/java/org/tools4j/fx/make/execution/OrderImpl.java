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
package org.tools4j.fx.make.execution;

import static org.tools4j.fx.make.util.StringUtil.formatQuantityAndPrice;

import java.util.Objects;

import org.tools4j.fx.make.asset.AssetPair;

/**
 * An immutable {@link Order}.
 */
public class OrderImpl implements Order {

	private final long id = ID_GENERATOR.incrementAndGet();
	private final AssetPair<?, ?> assetPair;
	private final String party;
	private final Side side;
	private final double price;
	private final long quantity;

	public OrderImpl(AssetPair<?, ?> assetPair, String party, Side side, double price, long quantity) {
		this.assetPair = Objects.requireNonNull(assetPair, "assetPair is null");
		this.party = Objects.requireNonNull(party, "party is null");
		this.side = Objects.requireNonNull(side, "side is null");
		if (price < 0 | Double.isNaN(price)) {
			throw new IllegalArgumentException("illegal price: " + price);
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("illegal quantity: " + quantity);
		}
		this.price = price;
		this.quantity = quantity;
	}

	public OrderImpl(Order order, long remainingQuantity) {
		this(order.getAssetPair(), order.getParty(), order.getSide(), order.getPrice(),
				validateRemainingQuantity(order, remainingQuantity));
	}

	private static long validateRemainingQuantity(Order order, long remainingQuantity) {
		if (remainingQuantity > order.getQuantity()) {
			throw new IllegalArgumentException(
					"remaining quantity exceeds order quantity: " + remainingQuantity + " > " + order);
		}
		return remainingQuantity;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public AssetPair<?, ?> getAssetPair() {
		return assetPair;
	}

	@Override
	public String getParty() {
		return party;
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public long getQuantity() {
		return quantity;
	}

	public String toShortString() {
		return getSide() + ":" + getAssetPair() + "[" + formatQuantityAndPrice(getQuantity(), getPrice()) + "]";
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id=" + id + ", assetPair=" + assetPair + ", party=" + party + ", side="
				+ side + ", price=" + price + ", quantity=" + quantity + "}";
	}

}
