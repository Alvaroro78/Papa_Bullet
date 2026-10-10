package model;

import structures.HashTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lector de JSON mínimo (Java no trae uno). Convierte el texto en:
 * objeto -> HashTable&lt;String, Object&gt; · arreglo -> List&lt;Object&gt; · texto -> String
 * número -> Double · true/false -> Boolean · null -> null.
 * Si el JSON está mal escrito lanza IllegalArgumentException diciendo en qué posición.
 */
public final class JsonReader {
    private final String s;
    private int pos;

    private JsonReader(String text) {
        this.s = text;
    }

    public static Object parse(String text) {
        JsonReader r = new JsonReader(text);
        Object value = r.value();
        r.skipSpaces();
        if (r.pos != r.s.length()) throw r.error("sobra texto después del JSON");
        return value;
    }

    private Object value() {
        skipSpaces();
        if (pos >= s.length()) throw error("el JSON terminó antes de tiempo");
        char c = s.charAt(pos);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (s.startsWith("null", pos)) { pos += 4; return null; }
        return number();
    }

    private HashTable<String, Object> object() {
        HashTable<String, Object> out = new HashTable<>();
        expect('{');
        skipSpaces();
        if (peek() == '}') { pos++; return out; }
        while (true) {
            skipSpaces();
            String key = string();
            skipSpaces();
            expect(':');
            out.put(key, value());
            skipSpaces();
            if (peek() == ',') { pos++; continue; }
            expect('}');
            return out;
        }
    }

    private List<Object> array() {
        List<Object> out = new ArrayList<>();
        expect('[');
        skipSpaces();
        if (peek() == ']') { pos++; return out; }
        while (true) {
            out.add(value());
            skipSpaces();
            if (peek() == ',') { pos++; continue; }
            expect(']');
            return out;
        }
    }

    private String string() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= s.length()) throw error("falta cerrar las comillas");
            char c = s.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            char e = s.charAt(pos++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> { sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16)); pos += 4; }
                default -> sb.append(e); // \" \\ \/
            }
        }
    }

    private Double number() {
        int start = pos;
        while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error("valor inesperado '" + s.charAt(pos) + "'");
        return Double.parseDouble(s.substring(start, pos));
    }

    private void skipSpaces() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }

    private char peek() {
        if (pos >= s.length()) throw error("el JSON terminó antes de tiempo");
        return s.charAt(pos);
    }

    private void expect(char c) {
        if (peek() != c) throw error("se esperaba '" + c + "' y hay '" + s.charAt(pos) + "'");
        pos++;
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException("JSON inválido en la posición " + pos + ": " + msg);
    }
}