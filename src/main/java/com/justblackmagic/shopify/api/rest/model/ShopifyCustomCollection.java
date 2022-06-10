package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyCustomCollection {

	private String id;
	private String title;
	private String handle;
	private boolean published;

	@JsonProperty("body_html")
	private String bodyHtml;

	@JsonProperty("published_scope")
	private String publishedScope;

	@JsonProperty("sort_order")
	private String sortOrder;

	@JsonProperty("template_suffix")
	private String templateSuffix;

	@JsonProperty("published_at")
	private DateTime publishedAt;

	@JsonProperty("updated_at")
	private DateTime updatedAt;

	@JsonProperty("admin_graphql_api_id")
	private String adminGraphqlApiId;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}

}
