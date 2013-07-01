package org.synnefo.jclouds;

/**
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public interface Proc<T> {
    public void apply(T value);

    public static final class Helpers {
        public static <T> Proc<T> safe(Proc<T> proc) {
            return null != proc
                ? proc
                : new Proc<T>() { @Override public void apply(T value) {} };
        }
    }
}
