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
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class NullableCodec<T> implements Codec<T> {

    private final Codec<T> delegate;

    public NullableCodec(Codec<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable T decode(@NonNull BsonValue bsonValue, ExtraInfo extraInfo) {
        if (bsonValue.isNull()) return null;
        return delegate.decode(bsonValue, extraInfo);
    }

    @Override
    public @NonNull BsonValue encode(@Nullable T value, ExtraInfo extraInfo) {
        if (value == null) return BsonNull.VALUE;
        return delegate.encode(value, extraInfo);
    }

    @Override
    public @Nullable T decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        if (reader.peekFor('n')) {
            if (!reader.tryConsume("null")) {
                throw new IllegalArgumentException("Invalid null value");
            }
            return null;
        }
        return delegate.decodeJson(reader, extraInfo);
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext context) {
        return Schema.anyOf(delegate.toSchema(context), new NullSchema());
    }
}
