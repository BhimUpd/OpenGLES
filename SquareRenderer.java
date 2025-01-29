package np.nicolai.rubikscube;

import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareRenderer implements GLSurfaceView.Renderer {
    private int vao[] = new int[1];
    private int vbo[] = new int[1];
    private int ebo[] = new int[1];//Index Buffer Object (IBO), is an OpenGL buffer that stores indices (references) to vertices. It is used with glDrawElements() to reuse vertices instead of duplicating them in a vertex buffer.
    private int program;

    private float[] squareVertices = new float[]{

        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f,  // Bottom-left corner  vertex_0
         0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f,  // Bottom-right corner vertex_1
        -0.5f,  0.5f, 0.0f, 0.0f, 1.0f, 0.0f,  // Top-left corner vertex_2
         0.5f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f,  // Top-right corner vertex_3
        // 2->0->1   and  3->2->1   is two triangle both taken in same anticlockWise order

};
private short[] squareIndices=new short[]{
        2,0,1,
        3,2,1
};




    private final String vertex_shader_code = String.join("\n",
            "#version 300 es\n",
            "layout (location = 0) in vec3 position;\n",
            "layout (location = 1) in vec3 vertexColors;\n",
            "out vec3 v_vertexColors;\n",
            "void main() {\n",
            "    v_vertexColors=vertexColors;\n",
            "    gl_Position = vec4(position, 1.0);\n",
            "}"
    );

    private final String fragment_shader_code = String.join("\n",
            "#version 300 es\n",
            "precision mediump float;\n",
            "in vec3 v_vertexColors;",
            "out vec4 outColor;",
            "void main() {\n",
            "    outColor = vec4(v_vertexColors.r, v_vertexColors.g, v_vertexColors.b,  1.0);\n",
            "}"
    );


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glGenBuffers(1, ebo, 0);

        GLES32.glBindVertexArray(vao[0]);//This makes vao[0] the active VAO.

        FloatBuffer vertexBuffer = ByteBuffer
                .allocateDirect(squareVertices.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(squareVertices).position(0);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, squareVertices.length * Float.BYTES, vertexBuffer, GLES32.GL_STATIC_DRAW);
        //glBufferData() is used to upload data to the currently bound buffer (GL_ARRAY_BUFFER in this case).

        ShortBuffer indexBuffer = ByteBuffer
                .allocateDirect(squareIndices.length * Short.BYTES)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indexBuffer.put(squareIndices).position(0);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, squareIndices.length * Short.BYTES, indexBuffer, GLES32.GL_STATIC_DRAW);

        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 6 * Float.BYTES, 0);
        GLES32.glEnableVertexAttribArray(0);

        GLES32.glVertexAttribPointer(1, 3, GLES32.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        GLES32.glEnableVertexAttribArray(1);

        GLES32.glBindVertexArray(0);//unbinds the current Vertex Array Object (VAO) by setting the active VAO to 0.

        int vertexShader=loadShader(GLES32.GL_VERTEX_SHADER,vertex_shader_code);
        int fragmentShader=loadShader(GLES32.GL_FRAGMENT_SHADER,fragment_shader_code);

        program=GLES32.glCreateProgram();
        GLES32.glAttachShader(program,vertexShader);
        GLES32.glAttachShader(program,fragmentShader);
        GLES32.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            throw new RuntimeException("Program link error: " + GLES32.glGetProgramInfoLog(program));
        }

        GLES32.glUseProgram(program);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES32.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT | GLES32.GL_STENCIL_BUFFER_BIT);

        GLES32.glBindVertexArray(vao[0]);  // Bind VAO before drawing


        //GLES32.glDrawArrays(GLES32.GL_TRIANGLES , 0, 6);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,6,GLES32.GL_UNSIGNED_SHORT ,0);//6 indices to give a square

        GLES32.glBindVertexArray(0); // Unbind VAO

    }


    private int loadShader(int type, String shaderCode){
        int shader=GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader,shaderCode);
        GLES32.glCompileShader(shader);

        int[] compiled=new int[1];
        GLES32.glGetShaderiv(shader,GLES32.GL_COMPILE_STATUS,compiled,0);
        if (compiled[0] == 0) {
            GLES32.glDeleteShader(shader);
            throw new RuntimeException("Error compiling shader: " + GLES32.glGetShaderInfoLog(shader));
        }
        return shader;
    }


}