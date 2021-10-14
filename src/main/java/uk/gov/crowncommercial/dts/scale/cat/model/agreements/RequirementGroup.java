package uk.gov.crowncommercial.dts.scale.cat.model.agreements;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.crowncommercial.dts.scale.cat.model.generated.Question;

/**
 *
 */
@Value
@Builder
@Jacksonized
public class RequirementGroup {

  @Value
  @Builder
  @Jacksonized
  public static class NonOCDS {

    String prompt;
    String task;
    Boolean mandatory;
  }

  @Value
  @Builder
  @Jacksonized
  public static class OCDS {

    String id;
    String description;
    Set<Question> requirements; // Should match Tenders API Question type
  }

  NonOCDS nonOCDS;

  @JsonProperty("OCDS")
  OCDS ocds;

}
