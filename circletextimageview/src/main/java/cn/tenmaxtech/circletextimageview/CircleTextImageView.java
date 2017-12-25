package cn.tenmaxtech.circletextimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

import cn.tenmaxtech.circletextimageview.R.styleable;

/**
 * Created by DamonQu on 2017/12/23.
 * File: CircleTextImageView.java
 * Description: Circle Image View with bottom and center Text
 */
@SuppressWarnings("unused")
public class CircleTextImageView extends AppCompatImageView {
    private static final ScaleType SCALE_TYPE;
    private static final Bitmap.Config BITMAP_CONFIG;

    private final RectF mDrawableRect;
    private final RectF mBorderRect;
    private final Matrix mShaderMatrix;
    private final Paint mBitmapPaint;
    private final Paint mBorderPaint;
    private final Paint mCircleBackgroundPaint;
    private final TextPaint mCenterTextPaint;
    private final Paint mBottomTextPaint;
    private final Paint mBottomBackgroundPaint;

    private int mBorderColor;
    private int mBorderWidth;
    private int mCircleBackgroundColor;
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private float mDrawableRadius;
    private float mBorderRadius;
    private ColorFilter mColorFilter;
    private boolean mReady;
    private boolean mSetupPending;
    private boolean mBorderOverlay;
    private boolean mDisableCircularTransformation;

    private String mCenterText;
    private int mCenterTextColor;
    private int mCenterTextSize;
    private String mBottomText;
    private int mBottomTextColor;
    private int mBottomTextSize;
    private int mBottomBackgroundColor;
    private int mCenterTextOffset;

    private Rect mCenterTextRect;
    private Rect mBottomTextRect;
    private RectF mBottomBackgroundRectF;

    private int mBottomTextPaddingTopBottom;

    public CircleTextImageView(Context context) {
        super(context);

        this.mDrawableRect = new RectF();
        this.mBorderRect = new RectF();
        this.mShaderMatrix = new Matrix();
        this.mBitmapPaint = new Paint();
        this.mBorderPaint = new Paint();
        this.mCenterTextPaint = new TextPaint();
        this.mBottomTextPaint = new Paint();
        this.mBottomBackgroundPaint = new Paint();
        this.mCircleBackgroundPaint = new Paint();
        this.mBorderColor = -16777216;
        this.mBorderWidth = 0;
        this.mCircleBackgroundColor = 0;

        this.mCenterText = null;
        this.mCenterTextColor = Color.BLACK;
        this.mCenterTextSize = sp2px(13, context);

        this.mBottomText = null;
        this.mBottomTextColor = Color.WHITE;
        this.mBottomTextSize = sp2px(13, context);
        this.mBottomBackgroundColor = Color.GRAY;
        this.mCenterTextOffset = 0;
        this.mBottomTextPaddingTopBottom = 0;

        this.init();
    }

    public CircleTextImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleTextImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDrawableRect = new RectF();
        this.mBorderRect = new RectF();
        this.mShaderMatrix = new Matrix();
        this.mBitmapPaint = new Paint();
        this.mBorderPaint = new Paint();
        this.mCircleBackgroundPaint = new Paint();

        this.mCenterTextPaint = new TextPaint();
        this.mBottomTextPaint = new Paint();
        this.mBottomBackgroundPaint = new Paint();

        this.mBorderColor = -16777216;
        this.mBorderWidth = 0;
        this.mCircleBackgroundColor = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, styleable.CircleTextImageView, defStyle, 0);
        this.mBorderWidth = a.getDimensionPixelSize(styleable.CircleTextImageView_ctiv_border_width, 0);
        this.mBorderColor = a.getColor(styleable.CircleTextImageView_ctiv_border_color, 0);
        this.mBorderOverlay = a.getBoolean(styleable.CircleTextImageView_ctiv_border_overlay, false);
        this.mCircleBackgroundColor = a.getColor(styleable.CircleTextImageView_ctiv_circle_background_color, 0);

        this.mCenterText = a.getString(styleable.CircleTextImageView_ctiv_center_text);
        this.mCenterTextColor = a.getColor(styleable.CircleTextImageView_ctiv_center_text_color, Color.BLACK);
        // sp to px
        this.mCenterTextSize = a.getDimensionPixelSize(styleable.CircleTextImageView_ctiv_center_text_size, 13);

        this.mBottomText = a.getString(styleable.CircleTextImageView_ctiv_bottom_text);
        this.mBottomTextColor = a.getColor(styleable.CircleTextImageView_ctiv_bottom_text_color, Color.WHITE);
        // sp to px
        this.mBottomTextSize = a.getDimensionPixelSize(styleable.CircleTextImageView_ctiv_bottom_text_size, 13);
        this.mBottomBackgroundColor = a.getColor(styleable.CircleTextImageView_ctiv_bottom_text_background, Color.GRAY);

        this.mCenterTextOffset = a.getDimensionPixelOffset(styleable.CircleTextImageView_ctiv_center_text_offset, 0);
        this.mBottomTextPaddingTopBottom = a.getDimensionPixelOffset(styleable.CircleTextImageView_ctiv_bottom_text_paddingTopBottom, 0);
        a.recycle();

        this.init();
    }

    private void init() {
        super.setScaleType(SCALE_TYPE);
        this.mReady = true;
        if (Build.VERSION.SDK_INT >= 21) {
            this.setOutlineProvider(new OutlineProvider());
        }

        if (this.mSetupPending) {
            this.setup();
            this.mSetupPending = false;
        }
    }

    protected void onDraw(Canvas canvas) {
        if(this.mDisableCircularTransformation) {
            super.onDraw(canvas);

        } else if(this.mBitmap != null) {
            if(this.mCircleBackgroundColor != 0) {
                canvas.drawCircle(this.mDrawableRect.centerX(), this.mDrawableRect.centerY(), this.mDrawableRadius, this.mCircleBackgroundPaint);
            }

            canvas.drawCircle(this.mDrawableRect.centerX(), this.mDrawableRect.centerY(), this.mDrawableRadius, this.mBitmapPaint);
            if(this.mBorderWidth > 0) {
                canvas.drawCircle(this.mBorderRect.centerX(), this.mBorderRect.centerY(), this.mBorderRadius, this.mBorderPaint);
            }

            if (mCenterText != null) {
                mCenterTextPaint.getTextBounds(mCenterText, 0, mCenterText.length(), mCenterTextRect);
                canvas.drawText(mCenterText, getMeasuredWidth()/2 - mCenterTextRect.width()/2, getMeasuredHeight()/2 + mCenterTextRect.height()/2 + mCenterTextOffset, mCenterTextPaint);
            }

            if (mBottomText != null) {
                mBottomTextPaint.getTextBounds(mBottomText, 0, mBottomText.length(), mBottomTextRect);

                double r = getMeasuredHeight()/2;
                double backgroundHeight = mBottomTextRect.height() + mBottomTextPaddingTopBottom * 2;
                double a = r - backgroundHeight;
                double angle = Math.toDegrees(Math.asin(a/r));
                mBottomBackgroundRectF.right = getMeasuredWidth();
                mBottomBackgroundRectF.bottom = getMeasuredHeight();
                canvas.drawArc(mBottomBackgroundRectF, (float) angle, (float) (180 - 2 * angle), false, mBottomBackgroundPaint);

                double textWidth = Math.sqrt(Math.pow(r, 2) - Math.pow(r - mBottomTextPaddingTopBottom, 2)) * 2;
                Log.d(getClass().getSimpleName(), "onDraw: r:" + r + " padding:" + mBottomTextPaddingTopBottom + " text width:" + textWidth);


                canvas.drawText(mBottomText, getMeasuredWidth()/2 - mBottomTextRect.width()/2, getMeasuredHeight() - mBottomTextPaddingTopBottom, mBottomTextPaint);
            }
        }
    }

    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", new Object[]{scaleType}));
        }
    }

    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if(adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.setup();
    }

    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        this.setup();
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        this.setup();
    }

    public int getBorderColor() {
        return this.mBorderColor;
    }

    public void setBorderColor(@ColorInt int borderColor) {
        if(borderColor != this.mBorderColor) {
            this.mBorderColor = borderColor;
            this.mBorderPaint.setColor(this.mBorderColor);
            this.invalidate();
        }
    }

    public int getCircleBackgroundColor() {
        return this.mCircleBackgroundColor;
    }

    public void setCircleBackgroundColor(@ColorInt int circleBackgroundColor) {
        if(circleBackgroundColor != this.mCircleBackgroundColor) {
            this.mCircleBackgroundColor = circleBackgroundColor;
            this.mCircleBackgroundPaint.setColor(circleBackgroundColor);
            this.invalidate();
        }
    }

    @SuppressWarnings("deprecation")
    public void setCircleBackgroundColorResource(@ColorRes int circleBackgroundRes) {
        this.setCircleBackgroundColor(this.getContext().getResources().getColor(circleBackgroundRes));
    }

    public int getBorderWidth() {
        return this.mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if(borderWidth != this.mBorderWidth) {
            this.mBorderWidth = borderWidth;
            this.setup();
        }
    }

    public boolean isBorderOverlay() {
        return this.mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        if(borderOverlay != this.mBorderOverlay) {
            this.mBorderOverlay = borderOverlay;
            this.setup();
        }
    }

    public String getCenterText() {
        return mCenterText;
    }

    public void setCenterText(String centerText) {
        if (centerText != null && ! centerText.equals(mCenterText)) {
            this.mCenterText = centerText;
            this.setup();
        }
    }

    public int getCenterTextColor() {
        return mCenterTextColor;
    }

    public void setCenterTextColor(@ColorInt int textColor) {
        if (textColor == mCenterTextColor) return;

        this.mCenterTextColor = textColor;
        mCenterTextPaint.setColor(textColor);
        this.invalidate();
    }

    public int getCenterTextSize() {
        return mCenterTextSize;
    }

    public void setCenterTextSize(int sp) {
        int textSize = sp2px(sp, getContext());

        if (textSize == mCenterTextSize) return;
        mCenterTextPaint.setTextSize(textSize);
        this.setup();
    }


    public String getBottomText() {
        return mBottomText;
    }

    public void setBottomText(String bottomText) {
        if (bottomText != null && ! bottomText.equals(mBottomText)) {
            this.mBottomText = bottomText;
            this.setup();
        }
    }

    public int getBottomTextColor() {
        return mBottomTextColor;
    }

    public void setBottomTextColor(int textColor) {
        if (textColor == mBottomTextColor) return;

        this.mBottomTextColor = textColor;
        mBottomTextPaint.setColor(textColor);
        this.invalidate();
    }

    public int getBottomTextSize() {
        return mBottomTextSize;
    }

    public void setBottomTextSize(int sp) {
        int textSize = sp2px(sp, getContext());
        if (textSize == mBottomTextSize) return;
        mBottomTextPaint.setTextSize(textSize);
        this.setup();
    }

    public int getBottomBackgroundColor() {
        return mBottomBackgroundColor;
    }

    public void setBottomBackgroundColor(int backgroundColor) {
        if (backgroundColor == mBottomBackgroundColor) return;

        mBottomBackgroundPaint.setColor(backgroundColor);
        this.invalidate();
    }

    public boolean isDisableCircularTransformation() {
        return this.mDisableCircularTransformation;
    }

    public void setDisableCircularTransformation(boolean disableCircularTransformation) {
        if(this.mDisableCircularTransformation != disableCircularTransformation) {
            this.mDisableCircularTransformation = disableCircularTransformation;
            this.initializeBitmap();
        }
    }

    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.initializeBitmap();
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        this.initializeBitmap();
    }

    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        this.initializeBitmap();
    }

    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        this.initializeBitmap();
    }

    public void setColorFilter(ColorFilter cf) {
        if(cf != this.mColorFilter) {
            this.mColorFilter = cf;
            this.applyColorFilter();
            this.invalidate();
        }
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    private void applyColorFilter() {
        if(this.mBitmapPaint != null) {
            this.mBitmapPaint.setColorFilter(this.mColorFilter);
        }

    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) return null;

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap e;
        if (drawable instanceof ColorDrawable) {
            e = Bitmap.createBitmap(2, 2, BITMAP_CONFIG);

        } else {
            e = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);

        }

        Canvas canvas = new Canvas(e);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return e;
    }

    private void initializeBitmap() {
        if (this.mDisableCircularTransformation) {
            this.mBitmap = null;

        } else {
            this.mBitmap = this.getBitmapFromDrawable(this.getDrawable());

        }

        this.setup();
    }

    private void setup() {
        if(!this.mReady) {
            this.mSetupPending = true;

        } else if(this.getWidth() != 0 || this.getHeight() != 0) {
            if(this.mBitmap == null) {
                this.invalidate();

            } else {
                this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                this.mBitmapPaint.setAntiAlias(true);
                this.mBitmapPaint.setShader(this.mBitmapShader);
                this.mBorderPaint.setStyle(Paint.Style.STROKE);
                this.mBorderPaint.setAntiAlias(true);
                this.mBorderPaint.setColor(this.mBorderColor);
                this.mBorderPaint.setStrokeWidth((float)this.mBorderWidth);
                this.mCircleBackgroundPaint.setStyle(Paint.Style.FILL);
                this.mCircleBackgroundPaint.setAntiAlias(true);
                this.mCircleBackgroundPaint.setColor(this.mCircleBackgroundColor);
                this.mBitmapHeight = this.mBitmap.getHeight();
                this.mBitmapWidth = this.mBitmap.getWidth();
                this.mBorderRect.set(this.calculateBounds());
                this.mBorderRadius = Math.min((this.mBorderRect.height() - (float)this.mBorderWidth) / 2.0F, (this.mBorderRect.width() - (float)this.mBorderWidth) / 2.0F);
                this.mDrawableRect.set(this.mBorderRect);
                if(!this.mBorderOverlay && this.mBorderWidth > 0) {
                    this.mDrawableRect.inset((float)this.mBorderWidth - 1.0F, (float)this.mBorderWidth - 1.0F);
                }

                this.mDrawableRadius = Math.min(this.mDrawableRect.height() / 2.0F, this.mDrawableRect.width() / 2.0F);
                this.applyColorFilter();
                this.updateShaderMatrix();
                this.invalidate();

                mCenterTextPaint.setColor(mCenterTextColor);
                mCenterTextPaint.setTextSize(mCenterTextSize);

                mCenterTextRect = new Rect();
                mBottomTextRect = new Rect();
                mBottomBackgroundRectF = new RectF(0,0,0,0);

                mBottomTextPaint.setColor(mBottomTextColor);
                mBottomTextPaint.setTextSize(mBottomTextSize);

                mBottomBackgroundPaint.setAntiAlias(true);
                mBottomBackgroundPaint.setColor(mBottomBackgroundColor);

            }
        }
    }

    private RectF calculateBounds() {
        int availableWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        int availableHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
        int sideLength = Math.min(availableWidth, availableHeight);
        float left = (float)this.getPaddingLeft() + (float)(availableWidth - sideLength) / 2.0F;
        float top = (float)this.getPaddingTop() + (float)(availableHeight - sideLength) / 2.0F;
        return new RectF(left, top, left + (float)sideLength, top + (float)sideLength);
    }

    private void updateShaderMatrix() {
        float dx = 0.0F;
        float dy = 0.0F;
        this.mShaderMatrix.set(null);
        float scale;
        if((float)this.mBitmapWidth * this.mDrawableRect.height() > this.mDrawableRect.width() * (float)this.mBitmapHeight) {
            scale = this.mDrawableRect.height() / (float)this.mBitmapHeight;
            dx = (this.mDrawableRect.width() - (float)this.mBitmapWidth * scale) * 0.5F;
        } else {
            scale = this.mDrawableRect.width() / (float)this.mBitmapWidth;
            dy = (this.mDrawableRect.height() - (float)this.mBitmapHeight * scale) * 0.5F;
        }

        this.mShaderMatrix.setScale(scale, scale);
        this.mShaderMatrix.postTranslate((float)((int)(dx + 0.5F)) + this.mDrawableRect.left, (float)((int)(dy + 0.5F)) + this.mDrawableRect.top);
        this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);
    }

    static {
        SCALE_TYPE = ScaleType.CENTER_CROP;
        BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    }

    @RequiresApi(api = 21)
    private class OutlineProvider extends ViewOutlineProvider {
        private OutlineProvider() {
        }

        public void getOutline(View view, Outline outline) {
            Rect bounds = new Rect();
            CircleTextImageView.this.mBorderRect.roundOut(bounds);
            outline.setRoundRect(bounds, (float)bounds.width() / 2.0F);
        }
    }

    public int sp2px(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
