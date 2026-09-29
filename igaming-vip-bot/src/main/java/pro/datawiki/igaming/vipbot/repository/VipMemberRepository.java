package pro.datawiki.igaming.vipbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.datawiki.igaming.vipbot.model.VipMember;
import pro.datawiki.igaming.vipbot.model.VipStatus;

import java.util.List;
import java.util.Optional;

public interface VipMemberRepository extends JpaRepository<VipMember, Long> {

    Optional<VipMember> findByTelegramUserId(Long telegramUserId);

    List<VipMember> findAllByStatus(VipStatus status);
}
