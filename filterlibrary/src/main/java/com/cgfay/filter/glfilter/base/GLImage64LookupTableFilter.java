package com.cgfay.filter.glfilter.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 应用查找表(3D LUT)滤镜(64 x 64)
 * Created by cain.huang on 2018/3/8.
 */
public class GLImage64LookupTableFilter extends GLImageFilter {

    private float mStrength;
    private int mStrengthHandle;
    private int mLookupTableTextureHandle;

    private int mCurveTexture = OpenGLUtils.GL_NOT_TEXTURE;
    private Bitmap mBitmap;

    public GLImage64LookupTableFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/base/fragment_lookup_table_64.glsl"));
    }

    public GLImage64LookupTableFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        mLookupTableTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "lookupTableTexture");
        setStrength(1.0f);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        OpenGLUtils.bindTexture(mLookupTableTextureHandle, mCurveTexture, 1);
        GLES30.glUniform1f(mStrengthHandle, mStrength);
    }

    public void setBitmap(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }
        mBitmap = bitmap;
        if (mBitmap == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (mCurveTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES30.glActiveTexture(GLES20.GL_TEXTURE3);
                    mCurveTexture = OpenGLUtils.createTexture(bitmap, OpenGLUtils.GL_NOT_TEXTURE);
                }
            }
        });
    }

    public void recycleBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public void release() {
        GLES30.glDeleteTextures(1, new int[]{ mCurveTexture }, 0);
        mCurveTexture = OpenGLUtils.GL_NOT_TEXTURE;
        super.release();
        recycleBitmap();
    }

    /**
     *  设置lut滤镜强度，0.0f ~ 1.0f
     * @param value
     */
    public void setStrength(float value) {
        float opacity;
        if (value <= 0) {
            opacity = 0.0f;
        } else if (value > 1.0f) {
            opacity = 1.0f;
        } else {
            opacity = value;
        }
        mStrength = opacity;
        setFloat(mStrengthHandle, mStrength);
    }

}
