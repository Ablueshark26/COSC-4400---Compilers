package Symbol;

public class Symbol {
    private final String name;
    private Symbol(String n) { name = n; }
    private static final java.util.HashMap<String, Symbol> dict = new java.util.HashMap<String, Symbol>();
    public String toString() { return name; }
    public static Symbol symbol(String n) {
        Symbol s = dict.get(n);
        if (s == null) { s = new Symbol(n); dict.put(n, s); }
        return s;
    }
}
