package fr.tropimon.catchpreview;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Method;

/** Optional official 1.8 API; 1.7 keeps the existing preview without invented values. */
final class PokemonDetails {
    private static final Method ALPHA = optional("isAlpha");
    private static final Method SIZE = optional("getSizeCategory");

    private PokemonDetails() {}

    static boolean alpha(Pokemon pokemon) { return Boolean.TRUE.equals(read(ALPHA, pokemon)); }

    static String size(Pokemon pokemon) {
        Object value = read(SIZE, pokemon);
        return value instanceof Enum<?> category ? category.name() : "";
    }

    private static Method optional(String name) {
        try { return Pokemon.class.getMethod(name); }
        catch (NoSuchMethodException absentOnOlderVersion) { return null; }
    }

    private static Object read(Method method, Pokemon pokemon) {
        if (method == null) return null;
        try { return method.invoke(pokemon); }
        catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot read official Cobblemon preview details", exception);
        }
    }
}
