package pro.datawiki.igaming.mdm.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnMdmResolutionCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String roleStr = context.getEnvironment().getProperty("app.mdm.role", "ALL");
        MdmRole role = MdmRole.fromString(roleStr);
        return role == MdmRole.ALL || role == MdmRole.RESOLUTION;
    }
}
