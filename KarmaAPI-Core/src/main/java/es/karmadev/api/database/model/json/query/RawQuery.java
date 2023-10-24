package es.karmadev.api.database.model.json.query;

import es.karmadev.api.strings.StringUtils;
import lombok.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Raw query
 */
@Getter @RequiredArgsConstructor
@ToString @EqualsAndHashCode
public class RawQuery implements QueryPart {

    private final static String DEFAULT_NULL_VALUE = StringUtils.generateString();

    private final String content;
    private final boolean and;
    private final boolean or;
    private final boolean single;

    private Matcher storedMatcher;

    /**
     * Test the part
     *
     * @param tests the values to test with
     * @return if the test was successful
     */
    @Override
    public boolean test(final Map<String, GlobalTypeValue<?>> tests) {
        String key = getKey();
        GlobalTypeValue<?> existingValue = tests.get(key);
        String comparator = getOperator();
        String value = getValue();

        if (existingValue.getType().equals(String.class)) {
            String stringValue = (String) existingValue.getValue();
            return checkString(value, stringValue, comparator);
        } else if (existingValue.getType().equals(Boolean.class)) {
            Boolean booleanValue = (Boolean) existingValue.getValue();
            boolean match = booleanValue != null &&
                    booleanValue.equals(Boolean.parseBoolean(value.replace("1", "true")));

            switch (comparator) {
                case "=":
                    if (booleanValue == null) {
                        return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                    }

                    return match;
                case "<>":
                    if (booleanValue == null) {
                        return !value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                    }

                    return !match;
                case ">":
                case "<":
                case ">=":
                case "<=":
                    return false;
            }
        } else if (existingValue.getType().equals(Byte.class)) {
            Byte byteValue = (Byte) existingValue.getValue();
            return checkNumber(value, byteValue.longValue(), comparator);
        } else if (existingValue.getType().equals(Short.class)) {
            Short shortValue = (Short) existingValue.getValue();
            return checkNumber(value, shortValue.longValue(), comparator);
        } else if (existingValue.getType().equals(Integer.class)) {
            Integer intValue = (Integer) existingValue.getValue();
            return checkNumber(value, intValue.longValue(), comparator);
        } else if (existingValue.getType().equals(Long.class)) {
            Long longValue = (Long) existingValue.getValue();
            return checkNumber(value, longValue, comparator);
        } else if (existingValue.getType().equals(Float.class)) {
            Float floatValue = (Float) existingValue.getValue();
            return checkNumber(value, floatValue.doubleValue(), comparator);
        } else {
            Double doubleValue = (Double) existingValue.getValue();
            return checkNumber(value, doubleValue, comparator);
        }

        return false;
    }

    private boolean checkString(final String value, final String string, final String comparator) {
        switch (comparator) {
            case "=":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.equals(value);
            case "~=":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.equalsIgnoreCase(value);
            case "<>":
                if (string == null) {
                    return !value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !string.equals(value);
            case "!>":
                if (string == null) {
                    return !value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !string.equalsIgnoreCase(value);
            case ">":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.contains(value);
            case "<":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !string.contains(value);
            case "~>":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.toLowerCase().contains(value.toLowerCase());
            case "~<":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !string.toLowerCase().contains(value.toLowerCase());
            case ">=":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.startsWith(value);
            case ">~":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.toLowerCase().startsWith(value.toLowerCase());
            case "<=":
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.endsWith(value);
            case "<~":
            default:
                if (string == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return string.toLowerCase().endsWith(value.toLowerCase());
        }
    }

    private boolean checkNumber(final String value, final Double number, final String comparator) {
        switch (comparator) {
            case "=":
                if (number == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return String.valueOf(number).equals(value.replace(",", "."));
            case "<>":
                if (number == null) {
                    return !value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !String.valueOf(number).equals(value.replace(",", "."));
            case ">":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                double lv = Double.parseDouble(value);
                return number > lv;
            }
            case "<":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                double lv = Double.parseDouble(value.replace(",", "."));
                return number < lv;
            }
            case ">=":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                double lv = Double.parseDouble(value.replace(",", "."));
                return number >= lv;
            }
            case "<=":
            default:{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                double lv = Double.parseDouble(value.replace(",", "."));
                return number <= lv;
            }
        }
    }

    private boolean checkNumber(final String value, final Long number, final String comparator) {
        switch (comparator) {
            case "=":
                if (number == null) {
                    return value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return String.valueOf(number).equals(value);
            case "<>":
                if (number == null) {
                    return !value.equalsIgnoreCase(DEFAULT_NULL_VALUE);
                }

                return !String.valueOf(number).equals(value);
            case ">":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                long lv = Long.parseLong(value);
                return number > lv;
            }
            case "<":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                long lv = Long.parseLong(value);
                return number < lv;
            }
            case ">=":{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                long lv = Long.parseLong(value);
                return number >= lv;
            }
            case "<=":
            default:{
                if (value.equalsIgnoreCase(DEFAULT_NULL_VALUE)) return false;
                long lv = Long.parseLong(value);
                return number <= lv;
            }
        }
    }

    private String getKey() {
        setupMatcher();
        return storedMatcher.group(1);
    }

    private String getOperator() {
        setupMatcher();
        return storedMatcher.group(2);
    }

    private String getValue() {
        setupMatcher();

        String value = storedMatcher.group(3);
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.equalsIgnoreCase("null")) return DEFAULT_NULL_VALUE;
        return value;
    }

    private void setupMatcher() {
        if (storedMatcher != null) return;
        Pattern keyValuePattern = Pattern.compile("\\s*'([^']*)'\\s*(=|~=|!>|~>|~<|>=|<=|>~|<~|<>|>|<)\\s*('[^']*'|null|true|false|[+-]?\\d+[,.]?e?\\d*)", Pattern.CASE_INSENSITIVE);
        String useContent = content;
        if (content.toLowerCase().startsWith("and")) {
            useContent = content.substring(3);
        }
        if (content.toLowerCase().startsWith("or")) {
            useContent = content.substring(2);
        }

        storedMatcher = keyValuePattern.matcher(useContent);
        if (!storedMatcher.matches()) {
            throw new IllegalStateException("Invalid query syntax at: " + content);
        }
    }
}