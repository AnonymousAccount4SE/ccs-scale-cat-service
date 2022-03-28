package uk.gov.crowncommercial.dts.scale.cat.repo;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.crowncommercial.dts.scale.cat.model.entity.ProjectUserMapping;

/**
 *
 */
@Repository
public interface ProjectUserMappingRepo extends JpaRepository<ProjectUserMapping, Integer> {

  Set<ProjectUserMapping> findByProjectId(Integer projectId);

  Set<ProjectUserMapping> findByUserId(String userId);

  List<ProjectUserMapping> findByUserId(String userId, Pageable pageable);
}
