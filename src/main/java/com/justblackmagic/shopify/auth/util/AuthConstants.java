package com.justblackmagic.shopify.auth.util;

public abstract class AuthConstants {

    /**
     * Session attribute name for storing shop name.
     */
    public static final String SHOP_ATTRIBUTE_NAME = "shop";

    /**
     * @deprecated Use {@link #SHOP_ATTRIBUTE_NAME} instead. This constant has a typo and is kept for backwards compatibility.
     */
    @Deprecated
    public static final String SHOP_ATTRIBUE_NAME = SHOP_ATTRIBUTE_NAME;


}
