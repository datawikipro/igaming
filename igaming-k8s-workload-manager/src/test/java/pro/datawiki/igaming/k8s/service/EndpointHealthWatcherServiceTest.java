package pro.datawiki.igaming.k8s.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pro.datawiki.igaming.k8s.config.HealthWatcherProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EndpointHealthWatcherServiceTest {

    private KubernetesClient kubernetesClient;
    private HealthWatcherProperties properties;
    private EndpointHealthWatcherService service;

    @BeforeEach
    void setUp() {
        kubernetesClient = Mockito.mock(KubernetesClient.class);
        properties = new HealthWatcherProperties();
        service = new EndpointHealthWatcherService(properties, kubernetesClient);
    }

    @Test
    void testDefaultTargetsHaveUpdatedEndpoints() {
        List<HealthWatcherProperties.TargetService> defaults = EndpointHealthWatcherService.getDefaultTargets();
        assertNotNull(defaults);
        assertEquals(4, defaults.size());

        // 1. Airflow: /api/v2/monitor/health
        HealthWatcherProperties.TargetService airflow = defaults.stream()
                .filter(t -> "airflow-api".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(airflow, "airflow-api target must exist");
        assertEquals("http://airflow-api-server.airflow.svc.cluster.local:8080/api/v2/monitor/health", airflow.getUrl());
        assertEquals("airflow", airflow.getNamespace());
        assertEquals("airflow-api-server", airflow.getDeploymentName());
        assertTrue(airflow.isEnabled());
        assertTrue(airflow.isAutoRestart());

        // 2. LLM Admin: /actuator/health
        HealthWatcherProperties.TargetService llmAdmin = defaults.stream()
                .filter(t -> "llm-admin".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(llmAdmin, "llm-admin target must exist");
        assertEquals("http://llm-admin.llm.svc.cluster.local:80/actuator/health", llmAdmin.getUrl());
        assertEquals("llm", llmAdmin.getNamespace());
        assertEquals("llm-admin", llmAdmin.getDeploymentName());
        assertTrue(llmAdmin.isEnabled());
        assertTrue(llmAdmin.isAutoRestart());

        // 3. Data Catalog: /api/sources
        HealthWatcherProperties.TargetService dataCatalog = defaults.stream()
                .filter(t -> "data-catalog".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(dataCatalog, "data-catalog target must exist");
        assertEquals("http://data-catalog-backend.data-catalog.svc.cluster.local:8080/api/sources", dataCatalog.getUrl());
        assertEquals("data-catalog", dataCatalog.getNamespace());
        assertEquals("data-catalog-backend", dataCatalog.getDeploymentName());
        assertTrue(dataCatalog.isEnabled());
        assertTrue(dataCatalog.isAutoRestart());

        // 4. Superset: disabled
        HealthWatcherProperties.TargetService superset = defaults.stream()
                .filter(t -> "superset".equals(t.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(superset, "superset target must exist");
        assertFalse(superset.isEnabled());
        assertFalse(superset.isAutoRestart());
    }

    @Test
    void testTargetHealthStateBuilder() {
        EndpointHealthWatcherService.TargetHealthState state = EndpointHealthWatcherService.TargetHealthState.builder()
                .name("test-svc")
                .url("http://test:8080/health")
                .namespace("test-ns")
                .deploymentName("test-dep")
                .status("HEALTHY")
                .consecutiveFailures(0)
                .lastResponseTimeMs(45)
                .lastStatusCode(200)
                .build();

        assertEquals("test-svc", state.getName());
        assertEquals("HEALTHY", state.getStatus());
        assertEquals(0, state.getConsecutiveFailures());
        assertEquals(200, state.getLastStatusCode());
    }
}
