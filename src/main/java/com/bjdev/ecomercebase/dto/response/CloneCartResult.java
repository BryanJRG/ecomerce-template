package com.bjdev.ecomercebase.dto.response;

import java.util.List;

public record CloneCartResult(CartResponse newCart, List<PriceChangeNotice> priceChanges) {
}
