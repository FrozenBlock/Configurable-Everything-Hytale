package net.frozenblock.configurableeverything.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.NullSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonNull;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Optional;

/**
 * Codec for {@code Optional<T>} fields.
 *
 * Unlike {@code NullableCodec}, this wraps the value in {@code Optional} so the field
 * is always non-null in Java. That satisfies BuilderField's null-skip check, meaning
 * {@code Optional.empty()} is written as JSON {@code null} and non-empty optionals are
 * written as their normal value.
 */
public class OptionalCodec<T> implements Codec<Optional<T>> {
    public static final OptionalCodec<Boolean> BOOLEAN = new OptionalCodec<>(Codec.BOOLEAN);
    public static final OptionalCodec<Byte> BYTE = new OptionalCodec<>(Codec.BYTE);
    public static final OptionalCodec<Short> SHORT = new OptionalCodec<>(Codec.SHORT);
    public static final OptionalCodec<Integer> INTEGER = new OptionalCodec<>(Codec.INTEGER);
    public static final OptionalCodec<Long> LONG = new OptionalCodec<>(Codec.LONG);
    public static final OptionalCodec<Float> FLOAT = new OptionalCodec<>(Codec.FLOAT);
    public static final OptionalCodec<Double> DOUBLE = new OptionalCodec<>(Codec.DOUBLE);
    public static final OptionalCodec<String> STRING = new OptionalCodec<>(Codec.STRING);

    private final Codec<T> delegate;

    public OptionalCodec(Codec<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NonNull Optional<T> decode(@NonNull BsonValue bsonValue, ExtraInfo extraInfo) {
        if (bsonValue.isNull()) return Optional.empty();
        return Optional.of(delegate.decode(bsonValue, extraInfo));
    }

    @Override
    public @NonNull BsonValue encode(@NonNull Optional<T> value, ExtraInfo extraInfo) {
        return value.isEmpty() ? BsonNull.VALUE : delegate.encode(value.get(), extraInfo);
    }

    @Override
    public @NonNull Optional<T> decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        if (reader.peekFor('n')) {
            if (!reader.tryConsume("null")) {
                throw new IllegalArgumentException("Invalid null value");
            }
            return Optional.empty();
        }
        return Optional.of(delegate.decodeJson(reader, extraInfo));
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext context) {
        return Schema.anyOf(delegate.toSchema(context), new NullSchema());
    }
}
