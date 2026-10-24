package vn.liteglass.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Giao dien "kinh gia lap": nen da lam mo san (anh tinh), cac o chi la lop
 * trong suot co vien sang. Khong dung blur thoi gian thuc de chip yeu khong bi lag.
 */
final class Glass {
    private Glass() {}

    /** Ban ngay tu 6h00 den truoc 18h00. */
    static boolean isDay() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= 6 && h < 18;
    }

    static int text(boolean day) { return day ? 0xFF10202F : 0xFFEEF3FA; }
    static int muted(boolean day) { return day ? 0xFF34495E : 0xFFB4C1D3; }
    static int background(boolean day) { return day ? R.drawable.bg_day : R.drawable.bg_night; }

    static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable shape(int fill, int stroke, int strokeW, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(radius);
        g.setColor(fill);
        g.setStroke(strokeW, stroke);
        return g;
    }

    /** O kinh co trang thai duoc chon (focus bang remote). */
    static Drawable panel(Context c, boolean day, float radiusDp) {
        float r = dp(c, radiusDp);
        GradientDrawable focused = shape(
                day ? 0x57FFFFFF : 0x1FFFFFFF,
                day ? 0xFFFFFFFF : 0xD9FFFFFF,
                dp(c, 3), r);
        GradientDrawable normal = shape(
                day ? 0x38FFFFFF : 0x0AFFFFFF,
                day ? 0xCCFFFFFF : 0x2EFFFFFF,
                dp(c, 1), r);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, focused);
        s.addState(new int[]{android.R.attr.state_pressed}, focused);
        s.addState(new int[]{android.R.attr.state_selected}, focused);
        s.addState(new int[]{}, normal);
        return s;
    }

    /** O kinh tinh (khong chon duoc), vd. khung thoi tiet. */
    static Drawable staticPanel(Context c, boolean day, float radiusDp) {
        return shape(day ? 0x38FFFFFF : 0x0AFFFFFF,
                day ? 0xCCFFFFFF : 0x2EFFFFFF,
                dp(c, 1), dp(c, radiusDp));
    }

    /** Phong to nhe khi duoc chon. Chi dung scale (re tren GPU). */
    static final View.OnFocusChangeListener ZOOM = (v, has) ->
            v.animate().scaleX(has ? 1.05f : 1f).scaleY(has ? 1.05f : 1f)
                    .setDuration(120).start();

    static boolean launchPackage(Context c, String pkg) {
        PackageManager pm = c.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        return start(c, i);
    }

    static boolean start(Context c, Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }
}
