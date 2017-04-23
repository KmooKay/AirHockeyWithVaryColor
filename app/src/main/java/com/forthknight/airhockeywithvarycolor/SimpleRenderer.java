package com.forthknight.airhockeywithvarycolor;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.forthknight.airhockeywithvarycolor.util.ShaderHelper;
import com.forthknight.airhockeywithvarycolor.util.TextResouceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

/**
 * Created by xiongyikai on 2017/4/22.
 */

public class SimpleRenderer implements GLSurfaceView.Renderer{

    private static final int BYTE_PRE_FLOAT = 4;

    private static final int POSITION_COMPONENT_COUNT = 2;

//    private static final String U_COLOR = "u_Color";
//    private int mUColorLocation;

    private static final String A_POSITION = "a_Position";
    private int mAPositionLocation;

    private static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTE_PRE_FLOAT;

    private static final String U_MATRIX = "u_Matrix";
    private int mUMatrixLocation;
    private float[] mProjectionMatrix = new float[16];

    private int mAColorLocation;

    private Context mContext;

    private int mProgram;

    private final float[] mData = new float[]{
            // Order of coordinates: X, Y, R, G, B

            // Triangle Fan
            0f,    0f,   1f,   1f,   1f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
            0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f,  0.8f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

            //线
            -0.5f, 0f, 1f, 0f, 0f,
            0.5f, 0f, 1f, 0f, 0f,

            //点
            0f, -0.4f, 0f, 0f, 1f,
            0f,  0.4f, 1f, 0f, 0f
    };

    private final FloatBuffer mVertexData;

    public SimpleRenderer(Context context){
        mContext = context;

        mVertexData = ByteBuffer
                .allocateDirect(mData.length * BYTE_PRE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(mData);
    }



    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0f ,0f , 0f, 0f);
        String vertexShaderCode = TextResouceReader.readTextFileFromResource(mContext , R.raw.sample_vertex_shader);
        String fragmentShaderCode = TextResouceReader.readTextFileFromResource(mContext , R.raw.simple_fragment_shader);

        int vertextShaderId = ShaderHelper.compileVertexShader(vertexShaderCode);
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShaderCode);

        mProgram = ShaderHelper.linkProgram(vertextShaderId , fragmentShaderId);
        ShaderHelper.vaildProgram(mProgram);

        glUseProgram(mProgram);

//        mUColorLocation = glGetUniformLocation(mProgram , U_COLOR);
        mAPositionLocation = glGetAttribLocation(mProgram , A_POSITION);

        mAColorLocation = glGetAttribLocation(mProgram , A_COLOR);

        mUMatrixLocation = glGetUniformLocation(mProgram , U_MATRIX);

        mVertexData.position(0);
        glVertexAttribPointer(mAPositionLocation , POSITION_COMPONENT_COUNT, GL_FLOAT , false , STRIDE , mVertexData);
        glEnableVertexAttribArray(mAPositionLocation);

        mVertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(mAColorLocation , COLOR_COMPONENT_COUNT , GL_FLOAT , false , STRIDE , mVertexData);
        glEnableVertexAttribArray(mAColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0 , 0 , width , height);
        final float aspectRatio = width > height ?
                (float)width / (float) height : (float) height/ (float) width;
        if (width > height){
            //横屏
            orthoM(mProjectionMatrix , 0 , -aspectRatio , aspectRatio , -1 , 1 , -1 , 1);
        }else {
            //竖屏
            orthoM(mProjectionMatrix , 0 , -1 , 1 , -aspectRatio , aspectRatio , -1 , 1);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);

        glUniformMatrix4fv(mUMatrixLocation , 1 , false , mProjectionMatrix , 0);

        //绘制两个三角形
//        glUniform4f(mUColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        //绘制线
//        glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);

        //绘制点
//        glUniform4f(mUColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);
//        glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 9, 1);

    }
}
