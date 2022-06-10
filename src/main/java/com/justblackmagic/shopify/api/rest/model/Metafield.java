package com.justblackmagic.shopify.api.rest.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.justblackmagic.shopify.api.rest.model.serializer.MetafieldValueTypeDeserializer;
import com.justblackmagic.shopify.api.rest.model.serializer.MetafieldValueTypeSerializer;
import org.joda.time.DateTime;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Metafield {
	private String id;
	private String key;
	private String value;
	@JsonProperty("value_type")
	@JsonSerialize(using = MetafieldValueTypeSerializer.class)
	@JsonDeserialize(using = MetafieldValueTypeDeserializer.class)
	private MetafieldValueType valueType;
	private String namespace;
	@JsonProperty("owner_id")
	private String ownerId;
	@JsonProperty("owner_resource")
	private String ownerResource;
	@JsonProperty("created_at")
	private DateTime createdAt;
	@JsonProperty("updated_at")
	private DateTime updatedAt;


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
