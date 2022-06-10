package com.justblackmagic.shopify.event.events;

import java.util.Locale;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.annotation.Async;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The AppInstallEvent class is triggered when a Shopify Shop installs our application. This event might drive data load or push actions, welcome
 * emails, and more.
 */
@Async
@Data
@EqualsAndHashCode(callSuper = false)
public class AppInstallEvent extends ApplicationEvent {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7313587378037149313L;

    /** The app url. */
    private final String shopName;

    /** The locale. */
    private final Locale locale;

    private String appUrl;

    /**
     * Instantiates a new on registration complete event.
     *
     * @param user the user
     * @param locale the locale
     * @param appUrl the app url
     * @return
     */
    public AppInstallEvent(final String shopName, final Locale locale, final String appUrl) {
        super(shopName);
        this.shopName = shopName;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
