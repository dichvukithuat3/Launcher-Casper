package vn.liteglass.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    // Ha Noi
    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=21.0285&longitude=105.8542"
                    + "&current=temperature_2m,weather_code&timezone=Asia%2FBangkok";
    private static final String CITY = "Hà Nội";
    private static final long WEATHER_EVERY_MS = 30L * 60 * 1000;

    private static final String[] DAYS = {
            "Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ImageView bg, weatherIcon;
    private TextView clock, date, temp, weatherDesc;
    private LinearLayout weatherBox;
    private View[] tiles;
    private TextView[] tileLabels;
    private TextView[] pills;

    private Boolean currentDay = null;
    private volatile long lastWeather = 0;
    private Double temperature = null;
    private int weatherCode = -1;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateDate();
            applyTheme(false);
            if (System.currentTimeMillis() - lastWeather > WEATHER_EVERY_MS) fetchWeather();
            handler.postDelayed(this, 30_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bg = findViewById(R.id.bg);
        clock = findViewById(R.id.clock);
        date = findViewById(R.id.date);
        temp = findViewById(R.id.temp);
        weatherDesc = findViewById(R.id.weatherDesc);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherBox = findViewById(R.id.weatherBox);

        tiles = new View[]{findViewById(R.id.tileYoutube), findViewById(R.id.tileVtv), findViewById(R.id.tileFpt)};
        tileLabels = new TextView[]{findViewById(R.id.labelYoutube), findViewById(R.id.labelVtv), findViewById(R.id.labelFpt)};
        pills = new TextView[]{findViewById(R.id.btnHdmi), findViewById(R.id.btnSettings), findViewById(R.id.btnAllApps)};

        for (View t : tiles) t.setOnFocusChangeListener(Glass.ZOOM);
        for (View p : pills) p.setOnFocusChangeListener(Glass.ZOOM);

        tiles[0].setOnClickListener(v -> openApp("YouTube",
                new String[]{"com.google.android.youtube.tv", "com.google.android.youtube"},
                new String[]{"youtube"}));
        tiles[1].setOnClickListener(v -> openApp("VTV Go",
                new String[]{"com.vtvgo.androidtv", "com.vtvgo.android", "vn.vtv.vtvgo"},
                new String[]{"vtv go", "vtvgo"}));
        tiles[2].setOnClickListener(v -> openApp("FPT Play",
                new String[]{"net.fptplay.ottbox", "com.fptplay.ottbox", "com.fplay.activity"},
                new String[]{"fpt play", "fptplay"}));

        pills[0].setOnClickListener(v -> openInputs());
        pills[1].setOnClickListener(v -> {
            if (!Glass.start(this, new Intent(Settings.ACTION_SETTINGS)))
                Glass.toast(this, "Không mở được Cài đặt");
        });
        pills[2].setOnClickListener(v -> Glass.start(this, new Intent(this, AllAppsActivity.class)));

        applyTheme(true);
        updateDate();
        updateWeatherViews();
        tiles[0].requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        if (getCurrentFocus() == null) tiles[0].requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    @Override
    public void onBackPressed() {
        // Day la man hinh chinh: nut Back khong lam gi.
    }

    // ---------- Giao dien sang / toi ----------

    private void applyTheme(boolean force) {
        boolean day = Glass.isDay();
        if (!force && currentDay != null && currentDay == day) return;
        currentDay = day;

        bg.setImageResource(Glass.background(day));
        int text = Glass.text(day);
        int muted = Glass.muted(day);

        clock.setTextColor(text);
        date.setTextColor(muted);
        temp.setTextColor(text);
        weatherDesc.setTextColor(muted);
        weatherBox.setBackground(Glass.staticPanel(this, day, 20));

        for (View t : tiles) t.setBackground(Glass.panel(this, day, 27));
        for (TextView l : tileLabels) l.setTextColor(text);
        for (TextView p : pills) {
            p.setBackground(Glass.panel(this, day, 999));
            p.setTextColor(text);
            Drawable d = p.getCompoundDrawables()[0];
            if (d != null) {
                d = d.mutate();
                d.setColorFilter(text, PorterDuff.Mode.SRC_IN);
                int s = Glass.dp(this, 21);
                d.setBounds(0, 0, s, s);
                p.setCompoundDrawables(d, null, null, null);
            }
        }
        updateWeatherViews();
    }

    private void updateDate() {
        Calendar c = Calendar.getInstance();
        date.setText(DAYS[c.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                + c.get(Calendar.DAY_OF_MONTH) + " tháng " + (c.get(Calendar.MONTH) + 1));
    }

    // ---------- Thoi tiet (Open-Meteo, khong can khoa API) ----------

    private void fetchWeather() {
        lastWeather = System.currentTimeMillis();
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(WEATHER_URL).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                StringBuilder sb = new StringBuilder();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                }
                JSONObject cur = new JSONObject(sb.toString()).getJSONObject("current");
                final double t = cur.getDouble("temperature_2m");
                final int code = cur.getInt("weather_code");
                handler.post(() -> {
                    temperature = t;
                    weatherCode = code;
                    updateWeatherViews();
                });
            } catch (Exception e) {
                // Loi mang: thu lai sau khoang 5 phut
                lastWeather = System.currentTimeMillis() - WEATHER_EVERY_MS + 5L * 60 * 1000;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void updateWeatherViews() {
        boolean day = currentDay == null || currentDay;
        temp.setText(temperature == null ? "--°C"
                : String.format(Locale.US, "%d°C", Math.round(temperature)));
        weatherDesc.setText(CITY + " · " + describe(weatherCode));

        int icon;
        int color;
        if (weatherCode >= 0 && weatherCode <= 1) {
            icon = day ? R.drawable.ic_sun : R.drawable.ic_moon;
            color = day ? 0xFFC26A12 : 0xFFF4D27A;
        } else if (weatherCode >= 51) {
            icon = R.drawable.ic_rain;
            color = Glass.text(day);
        } else if (weatherCode >= 2) {
            icon = R.drawable.ic_cloud;
            color = Glass.text(day);
        } else {
            icon = day ? R.drawable.ic_sun : R.drawable.ic_moon;
            color = Glass.muted(day);
        }
        weatherIcon.setImageResource(icon);
        weatherIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private static String describe(int code) {
        if (code < 0) return "Đang cập nhật";
        if (code == 0) return "Trời quang";
        if (code <= 2) return "Ít mây";
        if (code == 3) return "Nhiều mây";
        if (code <= 48) return "Sương mù";
        if (code <= 57) return "Mưa phùn";
        if (code <= 67) return "Mưa";
        if (code <= 77) return "Tuyết";
        if (code <= 82) return "Mưa rào";
        return "Dông";
    }

    // ---------- Mo ung dung ----------

    private void openApp(String name, String[] packages, String[] keywords) {
        for (String p : packages) {
            if (Glass.launchPackage(this, p)) return;
        }
        String pkg = findPackageByLabel(keywords);
        if (pkg != null && Glass.launchPackage(this, pkg)) return;
        Glass.toast(this, "Chưa cài " + name + " trên TV. Hãy cài từ Google Play.");
    }

    /** Tim app theo ten hien thi (khong phan biet hoa thuong). */
    private String findPackageByLabel(String[] keywords) {
        PackageManager pm = getPackageManager();
        for (String cat : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            List<ResolveInfo> list = pm.queryIntentActivities(
                    new Intent(Intent.ACTION_MAIN).addCategory(cat), 0);
            for (ResolveInfo ri : list) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue;
                CharSequence l = ri.loadLabel(pm);
                if (l == null) continue;
                String label = l.toString().toLowerCase(Locale.ROOT);
                for (String k : keywords) {
                    if (label.contains(k)) return pkg;
                }
            }
        }
        return null;
    }

    /** Nguon vao HDMI: thu cac cach pho bien, neu khong duoc thi mo Cai dat. */
    private void openInputs() {
        String[] actions = {
                "com.android.tv.action.VIEW_INPUTS",
                "android.settings.TV_INPUT_SETTINGS",
        };
        PackageManager pm = getPackageManager();
        for (String a : actions) {
            Intent i = new Intent(a);
            if (i.resolveActivity(pm) != null && Glass.start(this, i)) return;
        }
        String pkg = findPackageByLabel(new String[]{"nguồn", "đầu vào", "source", "input", "hdmi"});
        if (pkg != null && Glass.launchPackage(this, pkg)) return;
        for (String p : new String[]{"com.android.tv", "com.tcl.tv", "com.google.android.tv"}) {
            if (Glass.launchPackage(this, p)) return;
        }
        Glass.toast(this, "Hãy dùng nút Nguồn/Input trên remote để chọn HDMI");
        Glass.start(this, new Intent(Settings.ACTION_SETTINGS));
    }
}
