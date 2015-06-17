package com.frozendevs.periodictable.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.PagesAdapter;
import com.frozendevs.periodictable.model.adapter.PropertiesAdapter;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class PropertiesActivity extends AppCompatActivity {

    public static final String EXTRA_ATOMIC_NUMBER = "com.frozendevs.periodictable.AtomicNumber";

    public static final String ARGUMENT_PROPERTIES = "properties";

    private String mWikipediaUrl;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.properties_activity);

        final TableFragment tableFragment = TableFragment.getInstance();

        if (tableFragment != null) {
            setEnterSharedElementCallback(tableFragment.mSharedElementCallback);

            /*
             * Work around shared view alpha state not being restored on exit transition finished.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().getDecorView().addOnAttachStateChangeListener(
                        new View.OnAttachStateChangeListener() {
                            @Override
                            public void onViewAttachedToWindow(View v) {
                            }

                            @Override
                            public void onViewDetachedFromWindow(View v) {
                                if (PropertiesActivity.this.isFinishing()) {
                                    tableFragment.onExitTransitionFinished();
                                }
                            }
                        });
            }
        }

        final ElementProperties elementProperties = Database.getInstance(this).getElementProperties(
                getIntent().getIntExtra(EXTRA_ATOMIC_NUMBER, 1));
        mWikipediaUrl = elementProperties.getWikipediaLink();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(
                R.id.collapsing_toolbar);

        final AppBarLayout appBar = (AppBarLayout) findViewById(R.id.app_bar);
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            String title = "";

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (-verticalOffset >= appBar.getTotalScrollRange() - toolbar.getHeight() &&
                        title.equals("")) {
                    title = elementProperties.getName();

                    collapsingToolbar.setTitle(title);
                } else if (!title.equals("")) {
                    title = "";

                    collapsingToolbar.setTitle(title);
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_PROPERTIES, elementProperties);

        PagesAdapter pagesAdapter = new PagesAdapter(this);
        pagesAdapter.addPage(R.string.fragment_title_properties, PropertiesFragment.class, bundle);
        pagesAdapter.addPage(R.string.fragment_title_isotopes, IsotopesFragment.class, bundle);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagesAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf");

        TableAdapter tableAdapter = new TableAdapter(this);

        View tileView = findViewById(R.id.tile_view);
        tableAdapter.getView(elementProperties, tileView, (ViewGroup) tileView.getParent());
        tileView.setClickable(false);

        TextView configuration = (TextView) findViewById(R.id.element_electron_configuration);
        configuration.setText(PropertiesAdapter.formatProperty(this,
                elementProperties.getElectronConfiguration()));
        configuration.setTypeface(typeface);

        TextView shells = (TextView) findViewById(R.id.element_electrons_per_shell);
        shells.setText(PropertiesAdapter.formatProperty(this,
                elementProperties.getElectronsPerShell()));
        shells.setTypeface(typeface);

        TextView electronegativity = (TextView) findViewById(R.id.element_electronegativity);
        electronegativity.setText(PropertiesAdapter.formatProperty(this,
                elementProperties.getElectronegativity()));
        electronegativity.setTypeface(typeface);

        TextView oxidationStates = (TextView) findViewById(R.id.element_oxidation_states);
        oxidationStates.setText(PropertiesAdapter.formatProperty(this,
                elementProperties.getOxidationStates()));
        oxidationStates.setTypeface(typeface);

        String imageUrl = elementProperties.getImageUrl();

        if (!imageUrl.equals("")) {
            final ImageView backdrop = (ImageView) findViewById(R.id.backdrop);

            final View backdropProgressBar = findViewById(R.id.backdrop_progressbar);
            backdropProgressBar.setVisibility(View.VISIBLE);

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State mobileNetwork = connectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).getState();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            String downloadWifiOnlyKey = getString(R.string.preferences_download_wifi_only_key);

            RequestCreator requestCreator = Picasso.with(this).load(imageUrl).placeholder(
                    R.drawable.banner);
            if ((mobileNetwork == NetworkInfo.State.CONNECTED ||
                    mobileNetwork == NetworkInfo.State.CONNECTING) &&
                    preferences.getBoolean(downloadWifiOnlyKey, false)) {
                requestCreator.networkPolicy(NetworkPolicy.OFFLINE);
            }
            requestCreator.into(backdrop, new Callback() {
                @Override
                public void onSuccess() {
                    backdropProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    onSuccess();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.properties_action_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wiki:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mWikipediaUrl)));
                return true;

            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.properties_context_menu, menu);
        menu.setHeaderTitle(R.string.context_title_options);

        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String propertyName, propertyValue;

        View view = ((RecyclerView.RecyclerContextMenuInfo) item.getMenuInfo()).targetView;

        TextView symbol = (TextView) view.findViewById(R.id.property_symbol);

        if (symbol != null) {
            propertyName = getString(R.string.property_symbol);
            propertyValue = (String) symbol.getText();
        } else {
            propertyName = (String) ((TextView) view.findViewById(R.id.property_name)).getText();
            propertyValue = (String) ((TextView) view.findViewById(R.id.property_value)).getText();
        }

        switch (item.getItemId()) {
            case R.id.context_copy:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ((android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                            setText(propertyValue);
                } else {
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                            setPrimaryClip(ClipData.newPlainText(propertyName, propertyValue));
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }
}
