package vn.liteglass.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllAppsActivity extends Activity {

    private static final class App {
        final String pkg;
        final String label;
        Drawable icon;
        final ResolveInfo info;

        App(String pkg, String label, ResolveInfo info) {
            this.pkg = pkg;
            this.label = label;
            this.info = info;
        }
    }

    private final List<App> apps = new ArrayList<>();
    private boolean day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_apps);
        day = Glass.isDay();

        ((ImageView) findViewById(R.id.bg)).setImageResource(Glass.background(day));
        ((TextView) findViewById(R.id.title)).setTextColor(Glass.text(day));

        loadApps();

        GridView grid = findViewById(R.id.grid);
        grid.setSelector(Glass.panel(this, day, 18));
        grid.setDrawSelectorOnTop(false);
        grid.setAdapter(new Adapter());
        grid.setOnItemClickListener((parent, view, position, id) -> {
            App a = apps.get(position);
            if (!Glass.launchPackage(this, a.pkg)) Glass.toast(this, "Không mở được " + a.label);
        });
        grid.requestFocus();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Map<String, App> map = new LinkedHashMap<>();
        for (String cat : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat), 0)) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName()) || map.containsKey(pkg)) continue;
                CharSequence l = ri.loadLabel(pm);
                map.put(pkg, new App(pkg, l == null ? pkg : l.toString(), ri));
            }
        }
        apps.addAll(map.values());
        Collator col = Collator.getInstance(new Locale("vi"));
        java.util.Collections.sort(apps, (a, b) -> col.compare(a.label, b.label));
    }

    private final class Adapter extends BaseAdapter {
        @Override public int getCount() { return apps.size(); }
        @Override public Object getItem(int p) { return apps.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convert, ViewGroup parent) {
            LinearLayout box;
            ImageView icon;
            TextView label;
            if (convert == null) {
                box = new LinearLayout(AllAppsActivity.this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setGravity(Gravity.CENTER_HORIZONTAL);
                int pad = Glass.dp(AllAppsActivity.this, 12);
                box.setPadding(pad, pad, pad, pad);
                box.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Glass.dp(AllAppsActivity.this, 120)));
                box.setBackground(Glass.staticPanel(AllAppsActivity.this, day, 18));

                icon = new ImageView(AllAppsActivity.this);
                int s = Glass.dp(AllAppsActivity.this, 52);
                icon.setLayoutParams(new LinearLayout.LayoutParams(s, s));
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                box.addView(icon);

                label = new TextView(AllAppsActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = Glass.dp(AllAppsActivity.this, 8);
                label.setLayoutParams(lp);
                label.setGravity(Gravity.CENTER_HORIZONTAL);
                label.setTextSize(14);
                label.setMaxLines(2);
                label.setEllipsize(TextUtils.TruncateAt.END);
                label.setTextColor(Glass.text(day));
                box.addView(label);
            } else {
                box = (LinearLayout) convert;
                icon = (ImageView) box.getChildAt(0);
                label = (TextView) box.getChildAt(1);
            }
            App a = apps.get(position);
            if (a.icon == null) a.icon = a.info.loadIcon(getPackageManager());
            icon.setImageDrawable(a.icon);
            label.setText(a.label);
            return box;
        }
    }
}
