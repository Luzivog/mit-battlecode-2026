package bcx.subst;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** A parameterized type from a class's Signature attribute (see JClass.getGenericInterfaces). */
final class PType implements ParameterizedType {
    private final Type raw;
    private final Type[] args;
    PType(Type raw, Type[] args) { this.raw = raw; this.args = args; }
    public Type[] getActualTypeArguments() { return args == null ? null : args.clone(); }
    public Type getRawType() { return raw; }
    public Type getOwnerType() { return null; }
}
