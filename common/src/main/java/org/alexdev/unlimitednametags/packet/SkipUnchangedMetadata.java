package org.alexdev.unlimitednametags.packet;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Quaternion4f;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.Metadata;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * EntityLib {@link Metadata} that ignores a write whose value equals the one already stored.
 * <p>
 * Stock {@link Metadata#setIndex} replaces the stored entry and marks the index as pending on every call, even when
 * the value did not change. A nametag whose layout is re-applied on each refresh (translation, rotation, scale...)
 * was therefore resent in full to every nearby viewer, up to 20 times a second. With this class an unchanged value is
 * a no-op: nothing is stored and the index never becomes pending, so no packet is produced for it. Nametags whose
 * values change (animations) are still sent on every update.
 * <p>
 * An index that was never written is always written, whatever the value. packetevents' {@link Quaternion4f} has no
 * {@code equals}, so rotations are compared component by component.
 */
final class SkipUnchangedMetadata extends Metadata {

    SkipUnchangedMetadata(final int entityId) {
        super(entityId);
    }

    /**
     * Creates a {@link WrapperEntity} of the given type whose metadata is a {@link SkipUnchangedMetadata}
     * (the equivalent of {@code new WrapperEntity(entityId, uuid, type)}).
     */
    @NotNull
    static WrapperEntity createEntity(final int entityId, @NotNull final UUID uuid, @NotNull final EntityType type) {
        final EntityMeta meta = EntityMeta.getConverter(type).apply(entityId, new SkipUnchangedMetadata(entityId));
        return new WrapperEntity(entityId, uuid, type, meta);
    }

    @Override
    public <T> void setIndex(final byte index, @NotNull final EntityDataType<T> dataType, final T value) {
        final Object stored = this.<Object>getIndex(index, null);
        if (stored != null && sameValue(stored, value)) {
            return;
        }
        super.setIndex(index, dataType, value);
    }

    static boolean sameValue(@Nullable final Object stored, @Nullable final Object value) {
        if (stored instanceof Quaternion4f a && value instanceof Quaternion4f b) {
            return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ() && a.getW() == b.getW();
        }
        return Objects.equals(stored, value);
    }
}
