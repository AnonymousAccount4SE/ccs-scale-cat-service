package uk.gov.crowncommercial.dts.scale.cat.model.entity;

import java.util.Set;
import javax.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 *
 */
@Entity
@Table(name = "document_templates")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "document_template_id")
  Integer id;

  @Column(name = "template_url")
  String templateUrl;

  @Column(name = "event_type")
  String eventType;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "document_template_id")
  Set<DocumentTemplateSource> documentTemplateSources;

}