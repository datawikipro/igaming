package pro.datawiki.igaming.analyst.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.datawiki.igaming.analyst.repository.AiPolicyInterventionRepository;
import pro.datawiki.igaming.dto.sport.Sports;
import pro.datawiki.igaming.dto.sport.Sport;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstitutionalRiskPolicyManagerTest {

    @Mock
    private AiPolicyInterventionRepository interventionRepository;

    @InjectMocks
    private InstitutionalRiskPolicyManager policyManager;

    @Test
    @DisplayName("Should cap draw ML_90 to ML_70 for risk prudence")
    void testDrawHighOddsPrudenceCap() {
        Sport football = Sports.get("FOOTBALL");
        InstitutionalRiskPolicyManager.PolicyCapResult res = policyManager.evaluatePrudenceCaps(football, "DRAW", 3.40, "ML_90");
        assertTrue(res.isCapped());
        assertEquals("ML_70", res.getAppliedTier());
        assertEquals("DRAW_HIGH_ODDS_PRUDENCE_CAP", res.getRuleCode());
    }

    @Test
    @DisplayName("Should cap ML_90 to ML_70 when market odds >= 2.00")
    void testOddsGe200TierCap() {
        Sport football = Sports.get("FOOTBALL");
        InstitutionalRiskPolicyManager.PolicyCapResult res = policyManager.evaluatePrudenceCaps(football, "WIN1", 2.15, "ML_90");
        assertTrue(res.isCapped());
        assertEquals("ML_70", res.getAppliedTier());
        assertEquals("ODDS_GE_2_00_TIER_CAP", res.getRuleCode());
    }

    @Test
    @DisplayName("Should cap ML_90 to ML_80 when market odds in [1.75, 2.00)")
    void testOddsGe175TierCap() {
        Sport football = Sports.get("FOOTBALL");
        InstitutionalRiskPolicyManager.PolicyCapResult res = policyManager.evaluatePrudenceCaps(football, "WIN1", 1.85, "ML_90");
        assertTrue(res.isCapped());
        assertEquals("ML_80", res.getAppliedTier());
        assertEquals("ODDS_GE_1_75_TIER_CAP", res.getRuleCode());
    }

    @Test
    @DisplayName("Should cap ML_80 to ML_70 when market odds >= 2.50")
    void testOddsGe250TierCap() {
        Sport football = Sports.get("FOOTBALL");
        InstitutionalRiskPolicyManager.PolicyCapResult res = policyManager.evaluatePrudenceCaps(football, "WIN2", 2.60, "ML_80");
        assertTrue(res.isCapped());
        assertEquals("ML_70", res.getAppliedTier());
        assertEquals("ODDS_GE_2_50_TIER_CAP", res.getRuleCode());
    }

    @Test
    @DisplayName("Kelly stake percentage calculation produces fractional scaled stake")
    void testKellyStakeCalculation() {
        // odd = 2.0 (b=1), prob = 60% (p=0.6, q=0.4), full kelly = (0.6 - 0.4)/1 = 0.2
        double stake = policyManager.calculateKellyStakePct(2.0, 60.0, "ML_70");
        assertTrue(stake >= 1.0 && stake <= 10.0);
    }

    @Test
    @DisplayName("Stake amounts match institutional tier values")
    void testStakeAmounts() {
        assertEquals(BigDecimal.valueOf(1000.0), policyManager.getStakeForTier("ML_90"));
        assertEquals(BigDecimal.valueOf(500.0), policyManager.getStakeForTier("ML_80"));
        assertEquals(BigDecimal.valueOf(250.0), policyManager.getStakeForTier("ML_70"));
        assertEquals(BigDecimal.valueOf(100.0), policyManager.getStakeForTier("ML_60"));
        assertEquals(BigDecimal.ZERO, policyManager.getStakeForTier("PASS"));
    }
}
