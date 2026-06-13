package pro.datawiki.igaming.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.datawiki.igaming.notifications.entity.TelegramChannel;

import java.util.List;

@Repository
public interface TelegramChannelRepository extends JpaRepository<TelegramChannel, Long> {
    List<TelegramChannel> findByEnabledTrue();
    boolean existsByChatId(String chatId);
}
