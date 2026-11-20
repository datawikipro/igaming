package pro.datawiki.igaming.analyst.repository.clickhouse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import pro.datawiki.igaming.analyst.ml.vector.ClickHouseDdsClient;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Declarative Repository Factory for ClickHouse.
 * Emulates Spring Data Repository style (Method 2) backed by the high-speed,
 * in-memory cached HTTP client (ClickHouseDdsClient).
 *
 * Automatically:
 * 1. Binds named parameters (:param) to positional arguments with ?
 * 2. Caches responses using query-specific TTL
 * 3. Maps ClickHouse snake_case columns directly to Java Record / DTO fields
 * 4. Supports Optional<T>, List<T>, and scalar/single-record return types.
 */
@Slf4j
@Component
public class ClickHouseRepositoryFactory {

    private final ClickHouseDdsClient clickHouseClient;
    private final ObjectMapper mapper;
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+)");

    public ClickHouseRepositoryFactory(ClickHouseDdsClient clickHouseClient, ObjectMapper objectMapper) {
        this.clickHouseClient = clickHouseClient;
        this.mapper = objectMapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T createRepository(Class<T> repositoryInterface) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                (proxy, method, args) -> handleMethodCall(repositoryInterface, method, args)
        );
    }

    private Object handleMethodCall(Class<?> repositoryInterface, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        Query queryAnnotation = method.getAnnotation(Query.class);
        if (queryAnnotation == null) {
            throw new UnsupportedOperationException("Method " + method.getName() + " in " + repositoryInterface.getSimpleName() + " must be annotated with @Query");
        }

        String rawSql = queryAnnotation.value().trim();
        Map<String, Object> paramValues = extractParamValues(method, args);

        // Convert :namedParam to ? and collect values in order of appearance
        List<Object> orderedArgs = new ArrayList<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(rawSql);
        StringBuilder sqlBuilder = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (!paramValues.containsKey(paramName)) {
                throw new IllegalArgumentException("Missing parameter value for :" + paramName + " in " + method.getName());
            }
            orderedArgs.add(paramValues.get(paramName));
            matcher.appendReplacement(sqlBuilder, "?");
        }
        matcher.appendTail(sqlBuilder);

        String executableSql = sqlBuilder.toString();
        for (Object arg : orderedArgs) {
            if (arg instanceof Collection<?> c && c.isEmpty()) {
                return mapResult(method, Collections.emptyList());
            }
        }
        String cacheKey = repositoryInterface.getSimpleName() + ":" + method.getName() + ":" + Arrays.deepHashCode(args);

        List<Map<String, Object>> rows = clickHouseClient.queryJsonCached(cacheKey, executableSql, orderedArgs.toArray());
        return mapResult(method, rows);
    }

    private Map<String, Object> extractParamValues(Method method, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Object val = (args != null && i < args.length) ? args[i] : null;

            Param paramAnno = p.getAnnotation(Param.class);
            if (paramAnno != null && !paramAnno.value().isBlank()) {
                map.put(paramAnno.value(), val);
            }
            map.put(p.getName(), val);
        }
        return map;
    }

    private Object mapResult(Method method, List<Map<String, Object>> rows) {
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();

        if (Optional.class.isAssignableFrom(returnType)) {
            if (rows == null || rows.isEmpty()) {
                return Optional.empty();
            }
            Class<?> targetClass = extractGenericType(genericReturnType);
            Object obj = mapper.convertValue(rows.get(0), targetClass);
            return Optional.ofNullable(obj);
        }

        if (List.class.isAssignableFrom(returnType)) {
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            Class<?> targetClass = extractGenericType(genericReturnType);
            List<Object> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                list.add(mapper.convertValue(row, targetClass));
            }
            return list;
        }

        // Single object return
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return mapper.convertValue(rows.get(0), returnType);
    }

    private Class<?> extractGenericType(Type genericType) {
        if (genericType instanceof ParameterizedType pt) {
            Type[] actualArgs = pt.getActualTypeArguments();
            if (actualArgs.length > 0 && actualArgs[0] instanceof Class<?> c) {
                return c;
            }
        }
        return Map.class;
    }
}
