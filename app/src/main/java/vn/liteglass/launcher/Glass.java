package vn.liteglass.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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

    static int text(boolean day) { return day ? 0xFF141E36 : 0xFFF2F5FF; }
    static int muted(boolean day) { return day ? 0xFF3D4A66 : 0xFFB9C3E6; }
    static int background(boolean day) { return day ? R.drawable.bg_day : R.drawable.bg_night; }

    static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Mat kinh: chuyen sac trong suot + vien sang + vet sang phan chieu o goc. */
    private static Drawable glassLayer(Context c, int top, int bottom, int stroke, int strokeW, float r) {
        GradientDrawable body = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{top, bottom});
        body.setCornerRadius(r);
        body.setStroke(strokeW, stroke);
        GradientDrawable shine = new GradientDrawable();
        shine.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        shine.setColors(new int[]{0x4DFFFFFF, 0x00FFFFFF});
        shine.setGradientCenter(0.12f, 0.0f);
        shine.setGradientRadius(dp(c, 150));
        shine.setCornerRadius(r);
        return new LayerDrawable(new Drawable[]{body, shine});
    }

    private static Drawable normal(Context c, boolean day, float r) {
        return day
                ? glassLayer(c, 0x8CFFFFFF, 0x24FFFFFF, 0xD9FFFFFF, dp(c, 1), r)
                : glassLayer(c, 0x29FFFFFF, 0x08FFFFFF, 0x47FFFFFF, dp(c, 1), r);
    }

    /** O kinh co trang thai duoc chon (focus bang remote). */
    static Drawable panel(Context c, boolean day, float radiusDp) {
        float r = dp(c, radiusDp);
        Drawable focused = day
                ? glassLayer(c, 0xBFFFFFFF, 0x33FFFFFF, 0xF2FFFFFF, dp(c, 2), r)
                : glassLayer(c, 0x5CFFFFFF, 0x17FFFFFF, 0xF2FFFFFF, dp(c, 2), r);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, focused);
        s.addState(new int[]{android.R.attr.state_pressed}, focused);
        s.addState(new int[]{android.R.attr.state_selected}, focused);
        s.addState(new int[]{}, normal(c, day, r));
        return s;
    }

    /** O kinh tinh (khong chon duoc), vd. khung thoi tiet. */
    static Drawable staticPanel(Context c, boolean day, float radiusDp) {
        return normal(c, day, dp(c, radiusDp));
    }

    /** Vang sang mau phia sau o duoc chon. */
    static Drawable glow(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setColors(new int[]{color, color & 0x00FFFFFF});
        g.setGradientCenter(0.5f, 0.5f);
        g.setGradientRadius(dp(c, radiusDp));
        return g;
    }

    /** Phong to nhe khi duoc chon. Chi dung scale (re tren GPU). */
    static final View.OnFocusChangeListener ZOOM = (v, has) ->
            v.animate().scaleX(has ? 1.06f : 1f).scaleY(has ? 1.06f : 1f)
                    .setDuration(110).withLayer().start();

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
