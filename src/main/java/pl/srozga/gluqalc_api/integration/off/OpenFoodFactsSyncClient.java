package pl.srozga.gluqalc_api.integration.off;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pl.srozga.gluqalc_api.entity.Product;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenFoodFactsSyncClient {
    private final RestClient restClient;

    @Value("${app.integration.open-food-facts.url}")
    private String baseUrl;
    @Value("${app.integration.open-food-facts.user-agent}")
    private String userAgent;
    @Value("${app.integration.open-food-facts.contact-email}")
    private String contactEmail;
    @Value("${app.integration.open-food-facts.write.user-id}")
    private String userId;
    @Value("${app.integration.open-food-facts.write.password}")
    private String password;
    @Value("${app.integration.open-food-facts.write.app-uuid}")
    private String serverAppUuid;

    @Async
    public void syncProductToOff(Product product) {
        if (product.getBarcode() == null || product.getBarcode().isBlank())
            return;

        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", product.getBarcode());
            body.add("user_id", userId);
            body.add("password", password);
            body.add("app_name", "GluQalc");
            body.add("app_version", "1.0");
            body.add("app_uuid", serverAppUuid);
            body.add("lang", "pl");

            if (product.getName() != null) body.add("product_name", product.getName());
            if (product.getBrand() != null) body.add("brands", product.getBrand());

            putNutrient(body, "energy-kcal", product.getEnergyKcal());
            putNutrient(body, "carbohydrates", product.getCarbohydrates());
            putNutrient(body, "sugars", product.getSugars());
            putNutrient(body, "fat", product.getFat());
            putNutrient(body, "saturated-fat", product.getSaturatedFat());
            putNutrient(body, "proteins", product.getProtein());
            putNutrient(body, "salt", product.getSalt());

            String effectiveUserAgent = (userAgent != null && !userAgent.isBlank())
                    ? userAgent
                    : String.format("GluQalc/1.0 (%s)", contactEmail);

            String response = restClient.post()
                    .uri(baseUrl + "/cgi/product_jqm2.pl")
                    .header("User-Agent", effectiveUserAgent)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (response != null && (response.contains("name=\"password\"") || response.contains("Connexion"))) {
                log.warn("OFF write auth failed for barcode {}", product.getBarcode());
            } else {
                log.info("Synced product barcode {} to OFF successfully.", product.getBarcode());
            }
        } catch (Exception e) {
            log.warn("Failed to sync product barcode {} to OFF: {}", product.getBarcode(), e.getMessage());
        }
    }

    private void putNutrient(MultiValueMap<String, String> body, String key, BigDecimal val) {
        if (val != null) {
            body.add("nutriment_" + key + "_100g", val.toPlainString());
        }
    }
}