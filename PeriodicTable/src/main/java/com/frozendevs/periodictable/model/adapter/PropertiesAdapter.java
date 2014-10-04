package com.frozendevs.periodictable.model.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.TableItem;

public class PropertiesAdapter extends BaseExpandableListAdapter implements
        ExpandableListView.OnGroupClickListener {

    private static enum ViewType {
        HEADER,
        ITEM,
        SUMMARY,
        EMISSION_SPECTRUM
    }

    private Context mContext;
    private Typeface mTypeface;
    private TableAdapter mTableAdapter;
    private Property[] mProperties = new Property[0];
    private StateListDrawable mGroupIndicator;
    private AlertDialog mLegendDialog;

    private class Property<T> {
        String mName = "";
        T mValue;
        ViewType mType = ViewType.ITEM;

        Property(int name, T value) {
            mName = mContext.getString(name);

            if (value == null) {
                mType = ViewType.HEADER;
            } else if (value instanceof String) {
                mValue = (T) parseString((String) value);
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                String[] mValues = new String[values.length];

                for (int i = 0; i < values.length; i++) {
                    mValues[i] = parseString(values[i]);
                }

                mValue = (T) mValues;
            } else {
                mValue = value;
            }
        }

        Property(int name, T value, ViewType type) {
            this(name, value);

            mType = type;
        }

        String parseString(String value) {
            if (value.equals("")) {
                return mContext.getString(R.string.property_value_unknown);
            } else if (value.equals("-")) {
                return mContext.getString(R.string.property_value_none);
            }

            return value;
        }

        String getName() {
            return mName;
        }

        T getValue() {
            return mValue;
        }

        ViewType getType() {
            return mType;
        }
    }

    private class ViewHolder {
        ImageView indicator;
        TextView name, value;
    }

    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        Resources.Theme theme = mContext.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.expandableListViewStyle, typedValue, true);
        TypedArray typedArray = theme.obtainStyledAttributes(typedValue.resourceId,
                new int[]{android.R.attr.groupIndicator});
        mGroupIndicator = (StateListDrawable) typedArray.getDrawable(0);
        typedArray.recycle();

        mTableAdapter = new TableAdapter(context);
        mTableAdapter.setItems((TableItem) properties);

        int category = R.string.category_unknown;
        switch (properties.getCategory()) {
            case 0:
                category = R.string.category_diatomic_nonmetals;
                break;

            case 1:
                category = R.string.category_noble_gases;
                break;

            case 2:
                category = R.string.category_alkali_metals;
                break;

            case 3:
                category = R.string.category_alkaline_earth_metals;
                break;

            case 4:
                category = R.string.category_metalloids;
                break;

            case 5:
                category = R.string.category_polyatomic_nonmetals;
                break;

            case 6:
                category = R.string.category_other_metals;
                break;

            case 7:
                category = R.string.category_transition_metals;
                break;

            case 9:
                category = R.string.category_lanthanides;
                break;

            case 10:
                category = R.string.category_actinides;
                break;
        }

        mProperties = new Property[]{
                new Property<String>(R.string.properties_header_summary, null),
                new Property<String[]>(R.string.properties_header_summary, new String[]{
                        properties.getElectronConfiguration(), properties.getElectronsPerShell(),
                        properties.getElectronegativity(), properties.getOxidationStates()
                }, ViewType.SUMMARY),
                new Property<String>(R.string.properties_header_general, null),
                new Property<String>(R.string.property_symbol, properties.getSymbol()),
                new Property<String>(R.string.property_atomic_number,
                        String.valueOf(properties.getNumber())),
                new Property<String>(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property<String>(R.string.property_group, String.valueOf(properties.getGroup())),
                new Property<String>(R.string.property_period,
                        String.valueOf(properties.getPeriod())),
                new Property<String>(R.string.property_block, properties.getBlock()),
                new Property<String>(R.string.property_category, mContext.getString(category)),
                new Property<String>(R.string.property_electron_configuration,
                        properties.getElectronConfiguration()),
                new Property<String>(R.string.property_electrons_per_shell,
                        properties.getElectronsPerShell()),
                new Property<String>(R.string.properties_header_physical, null),
                new Property<String>(R.string.property_appearance, properties.getAppearance()),
                new Property<String>(R.string.property_phase, properties.getPhase()),
                new Property<String>(R.string.property_density, properties.getDensity()),
                new Property<String>(R.string.property_liquid_density_at_mp,
                        properties.getLiquidDensityAtMeltingPoint()),
                new Property<String>(R.string.property_liquid_density_at_bp,
                        properties.getLiquidDensityAtBoilingPoint()),
                new Property<String>(R.string.property_melting_point, properties.getMeltingPoint()),
                new Property<String>(R.string.property_sublimation_point,
                        properties.getSublimationPoint()),
                new Property<String>(R.string.property_boiling_point, properties.getBoilingPoint()),
                new Property<String>(R.string.property_triple_point, properties.getTriplePoint()),
                new Property<String>(R.string.property_critical_point, properties.getCriticalPoint()),
                new Property<String>(R.string.property_heat_of_fusion, properties.getHeatOfFusion()),
                new Property<String>(R.string.property_heat_of_vaporization,
                        properties.getHeatOfVaporization()),
                new Property<String>(R.string.property_molar_heat_capacity,
                        properties.getMolarHeatCapacity()),
                new Property<String>(R.string.properties_header_atomic, null),
                new Property<String>(R.string.property_oxidation_states,
                        properties.getOxidationStates()),
                new Property<String>(R.string.property_electronegativity,
                        properties.getElectronegativity()),
                new Property<String>(R.string.property_molar_ionization_energies,
                        properties.getMolarIonizationEnergies()),
                new Property<Integer>(R.string.property_emission_spectrum, properties.getNumber(),
                        ViewType.EMISSION_SPECTRUM),
                new Property<String>(R.string.property_atomic_radius, properties.getAtomicRadius()),
                new Property<String>(R.string.property_covalent_radius,
                        properties.getCovalentRadius()),
                new Property<String>(R.string.property_van_der_waals_radius,
                        properties.getVanDerWaalsRadius()),
                new Property<String>(R.string.properties_header_miscellanea, null),
                new Property<String>(R.string.property_crystal_structure,
                        properties.getCrystalStructure()),
                new Property<String>(R.string.property_magnetic_ordering,
                        properties.getMagneticOrdering()),
                new Property<String>(R.string.property_thermal_conductivity,
                        properties.getThermalConductivity()),
                new Property<String>(R.string.property_thermal_expansion,
                        properties.getThermalExpansion()),
                new Property<String>(R.string.property_thermal_diffusivity,
                        properties.getThermalDiffusivity()),
                new Property<String>(R.string.property_electrical_resistivity,
                        properties.getElectricalResistivity()),
                new Property<String>(R.string.property_band_gap, properties.getBandGap()),
                new Property<String>(R.string.property_curie_point, properties.getCuriePoint()),
                new Property<String>(R.string.property_tensile_strength,
                        properties.getTensileStrength()),
                new Property<String>(R.string.property_speed_of_sound, properties.getSpeedOfSound()),
                new Property<String>(R.string.property_poisson_ratio, properties.getPoissonRatio()),
                new Property<String>(R.string.property_youngs_modulus, properties.getYoungsModulus()),
                new Property<String>(R.string.property_shear_modulus, properties.getShearModulus()),
                new Property<String>(R.string.property_bulk_modulus, properties.getBulkModulus()),
                new Property<String>(R.string.property_mohs_hardness, properties.getMohsHardness()),
                new Property<String>(R.string.property_vickers_hardness,
                        properties.getVickersHardness()),
                new Property<String>(R.string.property_brinell_hardness,
                        properties.getBrinellHardness()),
                new Property<String>(R.string.property_cas_number, properties.getCasNumber())
        };
    }

    @Override
    public int getGroupCount() {
        return mProperties.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Property getGroup(int groupPosition) {
        return mProperties[groupPosition];
    }

    @Override
    public Property getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             final ViewGroup parent) {
        Property property = getGroup(groupPosition);

        switch (property.getType()) {
            case EMISSION_SPECTRUM:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_spectrum_item, parent, false);

                    TextView name = (TextView) convertView.findViewById(R.id.property_name);
                    name.setText(property.getName());
                }
                break;

            case SUMMARY:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_summary_item, parent, false);

                    View tileView = convertView.findViewById(R.id.tile_view);

                    for (TableItem item : mTableAdapter.getAllItems()) {
                        if (item != null) {
                            mTableAdapter.getView(mTableAdapter.getItemPosition(item), tileView,
                                    (ViewGroup) convertView);
                            break;
                        }
                    }

                    tileView.setClickable(false);
                    tileView.setDuplicateParentStateEnabled(true);

                    String[] properties = (String[]) property.getValue();

                    TextView configuration =
                            (TextView) convertView.findViewById(R.id.element_electron_configuration);
                    configuration.setText(properties[0]);
                    configuration.setTypeface(mTypeface);

                    TextView shells =
                            (TextView) convertView.findViewById(R.id.element_electrons_per_shell);
                    shells.setText(properties[1]);
                    shells.setTypeface(mTypeface);

                    TextView electronegativity =
                            (TextView) convertView.findViewById(R.id.element_electronegativity);
                    electronegativity.setText(properties[2]);
                    electronegativity.setTypeface(mTypeface);

                    TextView oxidationStates =
                            (TextView) convertView.findViewById(R.id.element_oxidation_states);
                    oxidationStates.setText(properties[3]);
                    oxidationStates.setTypeface(mTypeface);
                }
                break;

            case ITEM:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_list_item, parent, false);
                }

                ViewHolder viewHolder = (ViewHolder) convertView.getTag();

                if (viewHolder == null) {
                    viewHolder = new ViewHolder();

                    viewHolder.indicator = (ImageView) convertView.findViewById(R.id.group_indicator);
                    viewHolder.name = (TextView) convertView.findViewById(R.id.property_name);
                    viewHolder.value = (TextView) convertView.findViewById(R.id.property_value);
                    viewHolder.value.setTypeface(mTypeface);

                    convertView.setTag(viewHolder);
                }

                viewHolder.name.setText(property.getName());

                String[] lines = ((String) property.getValue()).split("\\n");
                if (lines.length > 3) {
                    mGroupIndicator.setState(isExpanded ?
                            new int[]{android.R.attr.state_expanded} : null);
                    viewHolder.indicator.setImageDrawable(mGroupIndicator.getCurrent());
                    viewHolder.indicator.setVisibility(View.VISIBLE);

                    viewHolder.value.setText((String) property.getValue());
                    if (isExpanded) viewHolder.value.setMaxLines(Integer.MAX_VALUE);
                    else viewHolder.value.setMaxLines(1);
                } else {
                    viewHolder.indicator.setVisibility(View.GONE);

                    viewHolder.value.setText((String) property.getValue());
                }
                break;

            case HEADER:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_list_header, parent, false);
                }

                ((TextView) convertView).setText(property.getName());

                convertView.setOnClickListener(null);
                break;
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        switch (getGroup(groupPosition).getType()) {
            case SUMMARY:
                if (mLegendDialog == null) {
                    mLegendDialog = new AlertDialog.Builder(mContext).create();
                    mLegendDialog.setTitle(R.string.context_title_legend);

                    View view = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_summary_item, parent, false);

                    View tileView = view.findViewById(R.id.tile_view);

                    for (TableItem item : mTableAdapter.getAllItems()) {
                        if (item != null) {
                            mTableAdapter.getView(mTableAdapter.getItemPosition(item), tileView,
                                    (ViewGroup) view);
                            break;
                        }
                    }

                    tileView.setClickable(false);

                    ((TextView) tileView.findViewById(R.id.element_symbol)).setText(
                            R.string.property_atom_symbol);
                    ((TextView) tileView.findViewById(R.id.element_number)).setText(
                            R.string.property_atomic_number_symbol);
                    ((TextView) tileView.findViewById(R.id.element_name)).setText(
                            R.string.property_name);
                    ((TextView) tileView.findViewById(R.id.element_weight)).setText(
                            R.string.property_relative_atomic_mass_symbol);

                    TextView configuration =
                            (TextView) view.findViewById(R.id.element_electron_configuration);
                    configuration.setText(R.string.property_electron_configuration);
                    configuration.setTypeface(mTypeface);

                    TextView shells = (TextView) view.findViewById(R.id.element_electrons_per_shell);
                    shells.setText(R.string.property_electrons_per_shell);
                    shells.setTypeface(mTypeface);

                    TextView electronegativity =
                            (TextView) view.findViewById(R.id.element_electronegativity);
                    electronegativity.setText(
                            mContext.getString(R.string.property_electronegativity));
                    electronegativity.setTypeface(mTypeface);

                    TextView oxidationStates =
                            (TextView) view.findViewById(R.id.element_oxidation_states);
                    oxidationStates.setText(R.string.property_oxidation_states);
                    oxidationStates.setTypeface(mTypeface);

                    mLegendDialog.setView(view);
                }

                if (!mLegendDialog.isShowing()) {
                    mLegendDialog.show();
                }
                break;
        }

        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getGroupType(int groupPosition) {
        return getGroup(groupPosition).getType().ordinal();
    }

    @Override
    public int getGroupTypeCount() {
        return ViewType.values().length;
    }
}
