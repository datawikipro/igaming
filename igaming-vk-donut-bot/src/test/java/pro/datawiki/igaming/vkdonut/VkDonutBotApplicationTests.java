package pro.datawiki.igaming.vkdonut;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test — verifies Spring context loads without errors.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "vk.community.token=test",
        "vk.community.id=1",
        "vk.callback.secret=test",
        "vk.callback.confirmation=test123"
})
class VkDonutBotApplicationTests {

    @Test
    void contextLoads() {
        // Spring context must start without errors
    }
}
