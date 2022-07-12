package uk.gov.crowncommercial.dts.scale.cat.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.crowncommercial.dts.scale.cat.model.DocumentAttachment;
import uk.gov.crowncommercial.dts.scale.cat.model.DocumentsKey;
import uk.gov.crowncommercial.dts.scale.cat.model.generated.Award2AllOf;
import uk.gov.crowncommercial.dts.scale.cat.model.generated.AwardState;
import uk.gov.crowncommercial.dts.scale.cat.model.generated.DocumentSummary;
import uk.gov.crowncommercial.dts.scale.cat.service.AwardService;

/**
 * Award Controller which can do award events
 *
 */
@RestController
@RequestMapping(path = "/tenders/projects/{proc-id}/events/{event-id}",
    produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Validated
public class AwardController extends AbstractRestController {

  private final AwardService awardService;
  private static final String CONTENT_TYPE = "application/zip";
  private static final String FILE_NAME = "attachment; filename= %s.zip";

  @PostMapping("/state/{award-state}/awards")
  public ResponseEntity<String> createAward(@PathVariable("proc-id") final Integer procId,
      @PathVariable("event-id") final String eventId,
      @PathVariable("award-state") final AwardState awardState,
      @Valid @RequestBody final Award2AllOf awardRequest,
      final JwtAuthenticationToken authentication) {
    var principal = getPrincipalFromJwt(authentication);
    var conclaveOrgId = getCiiOrgIdFromJwt(authentication);
    log.info("createOrComplete award process on behalf of principal: {}", principal, conclaveOrgId);
    return ResponseEntity.ok(awardService.createOrUpdateAwardRfx(principal, procId, eventId,
        awardState, awardRequest, null));
  }

  @PutMapping("/state/{award-state}/awards/{award-id}")
  public ResponseEntity<String> updateAward(@PathVariable("proc-id") final Integer procId,
      @PathVariable("event-id") final String eventId,
      @PathVariable("award-state") final AwardState awardState,
      @PathVariable("award-id") final Integer awardId,
      @Valid @RequestBody final Award2AllOf awardRequest,
      final JwtAuthenticationToken authentication) {
    var principal = getPrincipalFromJwt(authentication);
    var conclaveOrgId = getCiiOrgIdFromJwt(authentication);
    log.info("updateOrComplete award process invoked on behalf of principal: {}", principal,
        conclaveOrgId);
    return ResponseEntity.ok(awardService.createOrUpdateAwardRfx(principal, procId, eventId,
        awardState, awardRequest, awardId));
  }

  @GetMapping("/awards/templates")
  public Collection<DocumentSummary> getAwardTemplates(
      @PathVariable("proc-id") final Integer procId, @PathVariable("event-id") final String eventId,
      final JwtAuthenticationToken authentication) {
    var principal = getPrincipalFromJwt(authentication);
    log.debug("getAwardTemplates invoked on behalf of principal: {}", principal);
    return awardService.getAwardTemplates(procId, eventId);
  }

  @GetMapping(value = "/awards/templates/{templateId}")
  public ResponseEntity<StreamingResponseBody> getAwardTemplate(
      @PathVariable("proc-id") final Integer procId, @PathVariable("event-id") final String eventId,
      @PathVariable("templateId") final String templateId, HttpServletResponse response,
      final JwtAuthenticationToken authentication) {

    var principal = getPrincipalFromJwt(authentication);
    log.debug("getAwardTemplate invoked on behalf of principal: {}", principal);

    var documentKey = DocumentsKey.fromString(templateId);
    var exportDocuments = awardService.getAwardTemplate(procId, eventId, documentKey);

    StreamingResponseBody streamResponseBody = out -> {
      final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
      ZipEntry zipEntry = null;
      for (DocumentAttachment documentAttachment : exportDocuments) {
        zipEntry = new ZipEntry(documentAttachment.getFileName());
        zipOutputStream.putNextEntry(zipEntry);
        try (InputStream is = new ByteArrayInputStream(documentAttachment.getData())) {
          IOUtils.copy(is, zipOutputStream);
        }
      }
      // set zip size in response
      response.setContentLength((int) (zipEntry != null ? zipEntry.getSize() : 0));
      if (zipOutputStream != null) {
        zipOutputStream.close();
      }
    };
    response.setContentType(CONTENT_TYPE);
    response.setHeader("Content-Disposition", String.format(FILE_NAME, documentKey.getFileName()));
    response.addHeader("Pragma", "no-cache");
    response.addHeader("Expires", "0");
    return ResponseEntity.ok(streamResponseBody);
  }
}
