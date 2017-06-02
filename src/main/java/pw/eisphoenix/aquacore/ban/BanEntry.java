package pw.eisphoenix.aquacore.ban;

import pw.eisphoenix.aquacore.CPlayer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(value = "banEntries", noClassnameStored = true)
public final class BanEntry {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uuid = UUID.randomUUID();
    private UUID source;
    @Reference
    private CPlayer cPlayer;
    private String address;
    private long timeStamp;
    private long until;
    private BanReason banReason;
    private int often;
    private boolean validBan = true;
    private boolean unbanned = false;
    private String notes = "";

    public BanEntry() {

    }

    public BanEntry(final UUID source, final CPlayer cPlayer, final BanReason banReason, final int often) {
        this.source = source;
        this.cPlayer = cPlayer;
        this.timeStamp = System.currentTimeMillis();
        this.until = banReason.getDurations(often) == Integer.MAX_VALUE ? -1 : System.currentTimeMillis() + (long) banReason.getDurations(often) * 1000;
        this.often = often;
        this.banReason = banReason;
    }

    public BanEntry(final UUID source, final String address, final BanReason banReason, final int often) {
        this.source = source;
        this.address = address;
        this.timeStamp = System.currentTimeMillis();
        this.until = banReason.getDurations(often) == Integer.MAX_VALUE ? -1 : System.currentTimeMillis() + (long) banReason.getDurations(often) * 1000;
        this.often = often;
        this.banReason = banReason;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final UUID getSource() {
        return source;
    }

    public final void setSource(final UUID source) {
        this.source = source;
    }

    public final CPlayer getPlayer() {
        return cPlayer;
    }

    public final void setPlayer(final CPlayer cPlayer) {
        this.cPlayer = cPlayer;
    }

    public final String getAddress() {
        return address;
    }

    public final void setAddress(final String address) {
        this.address = address;
    }

    public final long getTimeStamp() {
        return timeStamp;
    }

    public final void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public final long getUntil() {
        return until;
    }

    public final void setUntil(final long until) {
        this.until = until;
    }

    public final BanReason getBanReason() {
        return banReason;
    }

    public final void setBanReason(final BanReason banReason) {
        this.banReason = banReason;
    }

    public final int getOften() {
        return often;
    }

    public final void setOften(final int often) {
        this.often = often;
    }

    public final boolean getValidBan() {
        return validBan;
    }

    public final void setValidBan(final boolean validBan) {
        this.validBan = validBan;
    }

    public final boolean getUnbanned() {
        return unbanned;
    }

    public final void setUnbanned(boolean unbanned) {
        this.unbanned = unbanned;
    }

    public final String getNotes() {
        return notes;
    }

    public final void setNotes(final String notes) {
        this.notes = notes;
    }

    public final boolean isCurrent() {
        return validBan && !unbanned && (until == -1 || until > System.currentTimeMillis());
    }

    /*@Embedded
    public enum BanReasonTemp {
        SPAMMING(3*60*60*1000), CHEATING(0), SPECIAL(0);
        private final long duration;

        BanReasonTemp(final long duration) {
            this.duration = duration;
        }

        public final long getDuration() {
            return duration;
        }
    }*/
}
