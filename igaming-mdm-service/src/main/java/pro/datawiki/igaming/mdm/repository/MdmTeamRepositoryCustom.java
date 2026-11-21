package pro.datawiki.igaming.mdm.repository;

import java.util.List;
import java.util.Map;

public interface MdmTeamRepositoryCustom {
    List<Map<String, Object>> searchTeams(String query, Integer sportId, int limit);
    List<Map<String, Object>> findDuplicates(Integer sportId, int limit);
    void mergeTeams(Long survivingTeamId, Long duplicateTeamId);
}
