package com.frozendevs.periodictable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class EmissionSpectrumView extends View {

    private static final double START_WAVELENGTH = 4000;
    private static final double END_WAVELENGTH = 7000;
    private static final double LINE_WIDTH = 1;
    private static final double CONTRAST = 10;
    private static final double CONTINUUM = 0.5;
    private static final double[][] COLORS = {
            {0, 0, 0, 8},
            {0.188235294, 0, 0, 255},
            {0.376470588, 0, 255, 255},
            {0.501960784, 0, 255, 0},
            {0.62745098, 255, 255, 0},
            {0.811764706, 255, 0, 0},
            {1, 8, 0, 0}
    };

    private static final double AVOGADRO_CONSTANT = 6.02214129E+23;
    private static final double PLANCK_CONSTANT = 6.62606957E-34;
    private static final double RYDBERG_CONSTANT = 1.0973731568539E+7;
    private static final int SPEED_OF_LIGHT = 299792458;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Double[] mWavelengths;
    private Double[] mStrengths;

    public EmissionSpectrumView(Context context) {
        super(context);

        initEmissionSpectrumView(context);
    }

    public EmissionSpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initEmissionSpectrumView(context);
    }

    public EmissionSpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initEmissionSpectrumView(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initEmissionSpectrumView(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (!isInEditMode()) {
                setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = width / 10;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double dwave = (END_WAVELENGTH - START_WAVELENGTH) / getWidth();

        double maxs = -7715;
        for (int i = 0; i < getWidth(); i++) {
            double wave = i * dwave + START_WAVELENGTH;
            double sum = 0;
            if(mWavelengths != null && mStrengths != null) {
                for (int j = 0; j < mWavelengths.length && j < mStrengths.length; j++) {
                    double delta = mWavelengths[j] - wave;
                    sum += mStrengths[j] * Math.exp((-delta * delta) / (LINE_WIDTH * LINE_WIDTH));
                }
            }
            maxs = Math.max(maxs, sum);
        }

        double scale = (1 - CONTINUUM) * CONTRAST / maxs != 0 ? maxs : 1;

        int RED = 1;
        int GREEN = 2;
        int BLUE = 3;

        for (int i = 0; i < getWidth(); i++) {
            double wave = i * dwave + START_WAVELENGTH;
            double dw = Math.max(Math.min((wave - START_WAVELENGTH) /
                    (END_WAVELENGTH - START_WAVELENGTH), 1), 0);

            double intensity = 0;
            if(mWavelengths != null && mStrengths != null) {
                for (int j = 0; j < mWavelengths.length && j < mStrengths.length; j++) {
                    double delta = mWavelengths[j] - wave;
                    intensity += mStrengths[j] * Math.exp((-delta * delta) /
                            (LINE_WIDTH * LINE_WIDTH));
                }
            }

            for (int k = 1; k < COLORS.length; k++) {
                if (dw <= COLORS[k][0]) {
                    double fraction = (dw - COLORS[k - 1][0]) / (COLORS[k][0] - COLORS[k - 1][0]);

                    double wavRed = fraction * (COLORS[k][RED] - COLORS[k - 1][RED]) +
                            COLORS[k - 1][RED];
                    double wavGreen = fraction * (COLORS[k][GREEN] - COLORS[k - 1][GREEN]) +
                            COLORS[k - 1][GREEN];
                    double wavBlue = fraction * (COLORS[k][BLUE] - COLORS[k - 1][BLUE]) +
                            COLORS[k - 1][BLUE];

                    double plot = Math.min(CONTINUUM + scale * intensity, 1);

                    mPaint.setColor(Color.rgb((int) (plot * wavRed), (int) (plot * wavGreen),
                            (int) (plot * wavBlue)));
                    canvas.drawLine(i, 0, i, getHeight() - 1, mPaint);

                    break;
                }
            }
        }
    }
}