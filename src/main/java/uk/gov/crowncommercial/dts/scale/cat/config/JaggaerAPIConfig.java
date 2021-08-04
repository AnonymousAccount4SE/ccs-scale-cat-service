package uk.gov.crowncommercial.dts.scale.cat.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 *
 */
@Configuration
@ConfigurationProperties(prefix = "config.external.jaggaer", ignoreUnknownFields = true)
@Data
public class JaggaerAPIConfig {

  private String baseUrl;
  private String headerValueWWWAuthenticate;
  private String headerValueInvalidContentType;
  private Integer timeoutDuration;
  private Boolean addDivisionToProjectTeam;
  private Long tokenExpirySeconds;
  private Map<String, String> createProject;
  private Map<String, String> createEvent;
  private Map<String, String> getBuyerCompanyProfile;

}
