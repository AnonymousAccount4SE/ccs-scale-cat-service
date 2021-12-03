package uk.gov.crowncommercial.dts.scale.cat.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.crowncommercial.dts.scale.cat.config.ApplicationFlagsConfig;
import uk.gov.crowncommercial.dts.scale.cat.config.JaggaerAPIConfig;
import uk.gov.crowncommercial.dts.scale.cat.model.generated.GetUserResponse.RolesEnum;
import uk.gov.crowncommercial.dts.scale.cat.service.ProfileManagementService;
import uk.gov.crowncommercial.dts.scale.cat.utils.TendersAPIModelUtils;

/**
 * Web (mock MVC) Tenders controller tests. Security aware.
 */
@WebMvcTest(TendersController.class)
@Import({TendersAPIModelUtils.class, JaggaerAPIConfig.class, GlobalErrorHandler.class,
    ApplicationFlagsConfig.class})
@ActiveProfiles("test")
class TendersControllerTest {

  private static final String PRINCIPAL = "jsmith@ccs.org.uk";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProfileManagementService profileManagementService;
  private JwtRequestPostProcessor validCATJwtReqPostProcessor;
  private JwtRequestPostProcessor validLDJwtReqPostProcessor;

  @BeforeEach
  void beforeEach() {
    validCATJwtReqPostProcessor = jwt().authorities(new SimpleGrantedAuthority("CAT_USER"))
        .jwt(jwt -> jwt.subject(PRINCIPAL));

    validLDJwtReqPostProcessor = jwt().authorities(new SimpleGrantedAuthority("JAEGGER_BUYER"))
        .jwt(jwt -> jwt.subject(PRINCIPAL));
  }

  @Test
  void listProcurementEventTypes_200_OK() throws Exception {
    mockMvc
        .perform(
            get("/tenders/event-types").with(validCATJwtReqPostProcessor).accept(APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON)).andExpect(jsonPath("$.size()").value(6))
        .andExpect(jsonPath("$[*]", contains("EOI", "RFI", "CA", "DA", "FC", "TBD")));
  }

  /*
   * CON-1680-AC3
   */
  @Test
  void listProcurementEventTypes_403_Forbidden() throws Exception {
    var invalidJwtReqPostProcessor =
        jwt().authorities(new SimpleGrantedAuthority("OTHER")).jwt(jwt -> jwt.subject(PRINCIPAL));

    mockMvc
        .perform(
            get("/tenders/event-types").with(invalidJwtReqPostProcessor).accept(APPLICATION_JSON))
        .andDo(print()).andExpect(status().isForbidden())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].status", is("403 FORBIDDEN"))).andExpect(
            jsonPath("$.errors[0].title", is("Access to the requested resource is forbidden")));
  }

  @Test
  void listProcurementEventTypes_401_Unauthorised() throws Exception {
    mockMvc.perform(get("/tenders/event-types").accept(APPLICATION_JSON)).andDo(print())
        .andExpect(status().isUnauthorized()).andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].status", is("401 UNAUTHORIZED")))
        .andExpect(jsonPath("$.errors[0].title", is("Missing, expired or invalid access token")));
  }

  @Test
  void getUser_200_OK() throws Exception {
    when(profileManagementService.getUserRoles(PRINCIPAL)).thenReturn(List.of(RolesEnum.BUYER));
    mockMvc
        .perform(get("/tenders/users/{user-id}", PRINCIPAL).with(validLDJwtReqPostProcessor)
            .accept(APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.roles.size()").value(1))
        .andExpect(jsonPath("$.roles[0]", is("buyer")));
  }

  @Test
  void getUser_403_Forbidden() throws Exception {
    mockMvc
        .perform(get("/tenders/users/{user-id}", "ted.crilly@craggyisland.com")
            .with(validLDJwtReqPostProcessor).accept(APPLICATION_JSON))
        .andDo(print()).andExpect(status().isForbidden())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.errors", hasSize(1)))
        .andExpect(jsonPath("$.errors[0].status", is("403 FORBIDDEN"))).andExpect(jsonPath(
            "$.errors[0].title", is("Authenticated user does not match requested user-id")));
  }

}
