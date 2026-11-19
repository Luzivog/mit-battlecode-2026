package bcx.subst;

import java.util.Locale;

/** Patch of java.util.Locale: the default locale is the reference JVM's here (LANG=C.UTF-8: user.language=en). */
public final class Locale_p {
    private static Locale initDefault() { return Locale.ENGLISH; }
    private static Locale initDefault(Locale.Category category) { return Locale.ENGLISH; }
}
