package es.karmadev.api.time;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time formatter
 */
@Builder
@FieldNameConstants
class TimeFormatter {

    @Getter
    private final int yyyy;

    @Getter
    private final int mm;

    @Getter
    private final int ww;

    @Getter
    private final int dd;

    @Getter
    private final long hh;

    @Getter
    private final long MM;

    @Getter
    private final long ss;

    @Getter
    private final long msms;

    @Getter
    private final String _yyyy_;

    @Getter
    private final String _mm_;

    @Getter
    private final int _ww_;

    @Getter
    private final String _dd_;

    @Getter
    private final String _hh_;

    @Getter
    private final String _MM_;

    @Getter
    private final String _ss_;

    @Getter
    private final UnitName name;

    /**
     * Parse the raw format
     *
     * @param raw the raw format
     * @return the parsed format
     */
    public String parse(final String raw) {
        Map<String, String> fieldTranslator = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().equals(Integer.TYPE) || f.getType().equals(Long.TYPE) || f.getType().equals(String.class)) {
                String fieldName = f.getName();
                if (fieldName.startsWith("_")) {
                    fieldName = "{" + fieldName.substring(1, fieldName.length() - 1) + "}";
                }

                try {
                    fieldTranslator.put(fieldName, String.valueOf(f.get(this)));
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Pattern pattern = Pattern.compile("(\\{ss}|\\{MM}|\\{hh}|\\{dd}|\\{ww}|\\{mm}|\\{yyyy}|msms|ss|MM|hh|dd|ww|mm|yyyy|%.?_?milli.?%|%.?_?second.?%|%.?_?minute.?%|%.?_?hour.?%|%.?_?day.?%|%.?_?week.?%|%.?_?month.?%|%.?_?year.?%)");
        Matcher matcher = pattern.matcher(raw);

        Map<String, String> replaces = new HashMap<>();
        Map<String, String> dateReplaces = new HashMap<>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String part = raw.substring(start, end);
            if (part.startsWith("%") && part.endsWith("%")) {
                long value = -1;
                String rawPart = part.replace("%", "").replace("s_", "").replace("p_", "");
                if (rawPart.endsWith("s")) rawPart = rawPart.substring(0, rawPart.length() - 1);

                switch (rawPart) {
                    case "milli":
                        value = msms;
                        break;
                    case "second":
                        value = ss;
                        break;
                    case "minute":
                        value = MM;
                        break;
                    case "hour":
                        value = hh;
                        break;
                    case "day":
                        value = dd;
                        break;
                    case "week":
                        value = ww;
                        break;
                    case "month":
                        value = mm;
                        break;
                    case "year":
                        value = yyyy;
                        break;
                }

                TemporalUnit unit = TemporalUnit.fromAlias(part.replace("%", ""));
                if (value > -1) {
                    unit = TemporalUnit.fromAlias(rawPart, value);
                }

                if (unit != null) {
                    replaces.put(part, name.get(unit));
                }
                continue;
            }

            String replace = fieldTranslator.getOrDefault(part, null);
            if (replace != null) {
                if (part.startsWith("{") && part.endsWith("}")) {
                    dateReplaces.put(part, replace);
                } else {
                    replaces.put(part, replace);
                }
            }
        }


        String modified = raw;
        for (String replace : dateReplaces.keySet()) { //If we use the same map to store replaces, {dd} for example, gets replaced with {<dd replace>} instead of <{dd} replace>
            modified = modified.replace(replace, dateReplaces.get(replace));
        }

        for (String replace : replaces.keySet()) {
            modified = modified.replace(replace, replaces.get(replace));
        }

        return modified;
    }
}
