package pl.srozga.gluqalc_api.integration.off.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffProduct(
        String _id,
        String code,
        @JsonAlias({"product_name", "product_name_en", "product_name_pl"})
        @JsonProperty("product_name")
        String productName,
        String brands,
        OffNutriments nutriments,
        @JsonProperty("countries_tags")
        List<String> countriesTags
) {
}
