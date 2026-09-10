package com.bjdev.ecomercebase.exception;

import com.bjdev.ecomercebase.models.catalog.Item;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

/** Catalog domain (Item/Category/Brand) errors — see AuthException for the errorCode/args + messages.properties pattern. */
@Getter
public class CatalogException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;
    /** Blocking-item ids/names for *_IN_USE errors — structured data, not translated prose. */
    private final List<String> details;

    private CatalogException(HttpStatus status, String errorCode, List<String> details, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
        this.args = args;
    }

    public static CatalogException categoryNotFound() {
        return new CatalogException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", null);
    }

    public static CatalogException brandNotFound() {
        return new CatalogException(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND", null);
    }

    public static CatalogException itemNotFound() {
        return new CatalogException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", null);
    }

    public static CatalogException variantNotFound() {
        return new CatalogException(HttpStatus.NOT_FOUND, "VARIANT_NOT_FOUND", null);
    }

    public static CatalogException categoryInactive() {
        return new CatalogException(HttpStatus.CONFLICT, "CATEGORY_INACTIVE", null);
    }

    public static CatalogException brandInactive() {
        return new CatalogException(HttpStatus.CONFLICT, "BRAND_INACTIVE", null);
    }

    public static CatalogException priceOutOfRange(String variantLabel, BigDecimal min, BigDecimal max) {
        return new CatalogException(HttpStatus.BAD_REQUEST, "PRICE_OUT_OF_RANGE", null,
                variantLabel, orUnbounded(min), orUnbounded(max));
    }

    public static CatalogException categoryInUse(String categoryName, List<Item> blockingItems) {
        return new CatalogException(HttpStatus.CONFLICT, "CATEGORY_IN_USE", toDetails(blockingItems), categoryName);
    }

    public static CatalogException brandInUse(String brandName, List<Item> blockingItems) {
        return new CatalogException(HttpStatus.CONFLICT, "BRAND_IN_USE", toDetails(blockingItems), brandName);
    }

    private static List<String> toDetails(List<Item> items) {
        return items.stream().map(i -> i.getId() + ": " + i.getName()).toList();
    }

    private static String orUnbounded(BigDecimal value) {
        return value != null ? value.toPlainString() : "sin límite";
    }
}
