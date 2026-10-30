// Projector Home v0.3.0
package com.builtbyfood.projectorhome;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Roku remote passthrough.
 *
 * While the projector's HDMI player (HiTvPlayer) is in front and Projector Home is in Roku
 * mode, the projector remote's navigation keys are sent to the Roku over ECP instead of to
 * the projector:
 *
 *   arrows → Up/Down/Left/Right, OK → Select, Back → Back,
 *   play/pause → Play, rewind → Rev, fast-forward → Fwd, info → Info
 *
 * Never intercepted: Home (returns to Projector Home), volume, mute, power.
 * Menu toggles: first press hands the remote back to the projector (its own menu opens),
 * the next press gives it back to the Roku. Switching away from the player and back
 * resets to "remote → Roku".
 *
 * The service reads only the package and class name of the window in front. It does not
 * read screen content, and it forwards keys only to the Roku address the user entered.
 */
public class RokuRemoteService extends AccessibilityService {
    private static final String TAG = "ProjectorHome";
    /** Minimum gap between forwarded repeats while a key is held, in ms. */
    private static final long REPEAT_MS = 140;
    /** Drop held-key repeats if this many requests are already waiting. */
    private static final int MAX_PENDING = 2;

    private static final SparseArray<String> KEYS = new SparseArray<>();
    static {
        KEYS.put(KeyEvent.KEYCODE_DPAD_UP, "Up");
        KEYS.put(KeyEvent.KEYCODE_DPAD_DOWN, "Down");
        KEYS.put(KeyEvent.KEYCODE_DPAD_LEFT, "Left");
        KEYS.put(KeyEvent.KEYCODE_DPAD_RIGHT, "Right");
        KEYS.put(KeyEvent.KEYCODE_DPAD_CENTER, "Select");
        KEYS.put(KeyEvent.KEYCODE_ENTER, "Select");
        KEYS.put(KeyEvent.KEYCODE_NUMPAD_ENTER, "Select");
        KEYS.put(KeyEvent.KEYCODE_BACK, "Back");
        KEYS.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "Play");
        KEYS.put(KeyEvent.KEYCODE_MEDIA_PLAY, "Play");
        KEYS.put(KeyEvent.KEYCODE_MEDIA_PAUSE, "Play");
        KEYS.put(KeyEvent.KEYCODE_MEDIA_REWIND, "Rev");
        KEYS.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, "Fwd");
        KEYS.put(KeyEvent.KEYCODE_INFO, "Info");
    }

    private final ExecutorService net = Executors.newSingleThreadExecutor();
    private final AtomicInteger pending = new AtomicInteger();

    private Config cfg;
    private volatile boolean playerInFront;
    private volatile boolean paused;
    private String lastPackage = "";
    private long lastSent;
    private boolean menuConsumed;

    /** True if the user has enabled this service in Accessibility settings. */
    static boolean isEnabled(Context ctx) {
        String list = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (list == null) return false;
        String me = new ComponentName(ctx, RokuRemoteService.class).flattenToString();
        for (String s : list.split(":")) if (s.equalsIgnoreCase(me)) return true;
        return false;
    }

    @Override
    protected void onServiceConnected() {
        cfg = new Config(this);
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            setServiceInfo(info);
        }
        Log.i(TAG, "Roku remote passthrough connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = e.getPackageName();
        if (pkgCs == null) return;
        String pkg = pkgCs.toString();
        boolean player = HdmiInput.PLAYER_PKG.equals(pkg);
        // Coming to the player from somewhere else: the remote goes to the Roku again.
        if (player && !HdmiInput.PLAYER_PKG.equals(lastPackage)) paused = false;
        playerInFront = player;
        lastPackage = pkg;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent ev) {
        if (cfg == null || !playerInFront || !cfg.isRoku() || !cfg.hasRokuIp()
                || !cfg.remotePassthrough()) {
            return false;
        }
        int code = ev.getKeyCode();

        if (code == KeyEvent.KEYCODE_MENU) return handleMenu(ev);
        if (paused) return false;

        String rokuKey = KEYS.get(code);
        if (rokuKey == null) return false;

        if (ev.getAction() == KeyEvent.ACTION_DOWN) {
            long now = SystemClock.uptimeMillis();
            boolean first = ev.getRepeatCount() == 0;
            if (first || (now - lastSent >= REPEAT_MS && pending.get() < MAX_PENDING)) {
                lastSent = now;
                send(rokuKey);
            }
        }
        return true; // consume both DOWN and UP so the projector doesn't act on it too
    }

    /** Menu: hand the remote to the projector (and let Menu through), or take it back. */
    private boolean handleMenu(KeyEvent ev) {
        if (ev.getAction() == KeyEvent.ACTION_DOWN) {
            if (ev.getRepeatCount() > 0) return menuConsumed;
            if (!paused) {
                paused = true;
                menuConsumed = false;
                toast("Remote → projector  (Menu again for Roku)");
            } else {
                paused = false;
                menuConsumed = true;
                toast("Remote → Roku");
            }
        }
        return menuConsumed;
    }

    private void send(String key) {
        final String ip = cfg.rokuIp();
        pending.incrementAndGet();
        net.execute(() -> {
            try {
                new RokuClient(ip, new File(getFilesDir(), "roku")).keypress(key);
            } catch (IOException e) {
                Log.w(TAG, "Roku keypress " + key + " failed: " + e.getMessage());
            } finally {
                pending.decrementAndGet();
            }
        });
    }

    private void toast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterrupt() {
        // Nothing to interrupt: no feedback is produced.
    }

    @Override
    public void onDestroy() {
        net.shutdownNow();
        super.onDestroy();
    }
}
