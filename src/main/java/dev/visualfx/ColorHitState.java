package dev.visualfx;

/** Client render data for a single player's hurt animation. */
public interface ColorHitState {
    interface Hurt {
        boolean visualfx$wasHitByLocalPlayer();
    }

    interface Render {
        int visualfx$hitTint();
        void visualfx$setHitTint(int tint);
    }
}
