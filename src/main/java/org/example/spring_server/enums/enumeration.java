package org.example.spring_server.enums;

public class enumeration {

    public enum SongArtistRole { main, featured, producer, remixer }

    public enum UserRole { USER, ARTIST, ADMIN }

    public enum AccountType {
        NORMAL, PRO, PREMIUM;

        public boolean canAccess(AccountType required) {
            return this.ordinal() >= required.ordinal();
        }
    }

    public enum SongStatus { LIVE, PENDING, DELETED }

    public enum DeviceType { DESKTOP, MOBILE, WEB }

    public enum PlanType         { PRO, PREMIUM }

    public enum SubscriptionStatus { ACTIVE, EXPIRED, CANCELLED }
}
