/*
 * Copyright 2019 Aletheia Ware LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aletheiaware.joy.android.sample;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.aletheiaware.joy.JoyProto.Mesh;
import com.aletheiaware.joy.JoyProto.Shader;
import com.aletheiaware.joy.android.scene.GLCameraNode;
import com.aletheiaware.joy.android.scene.GLColourAttribute;
import com.aletheiaware.joy.android.scene.GLProgram;
import com.aletheiaware.joy.android.scene.GLProgramNode;
import com.aletheiaware.joy.android.scene.GLScene;
import com.aletheiaware.joy.android.scene.GLVertexMesh;
import com.aletheiaware.joy.android.scene.GLVertexMeshNode;
import com.aletheiaware.joy.scene.Animation;
import com.aletheiaware.joy.scene.AttributeNode;
import com.aletheiaware.joy.scene.Matrix;
import com.aletheiaware.joy.scene.MatrixTransformationNode;
import com.aletheiaware.joy.scene.Vector;
import com.aletheiaware.joy.utils.JoyUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final String SHADER_NAME = "line";
    private static final String VERTEX_SOURCE =
            "#if __VERSION__ >= 130\n" +
            "  #define attribute in\n" +
            "  #define varying out\n" +
            "#endif\n" +
            "uniform mat4 u_MVPMatrix;\n" +
            "attribute vec4 a_Position;\n" +
            "void main() {\n" +
            "    gl_Position = u_MVPMatrix * a_Position;\n" +
            "}";
    private static final String FRAGMENT_SOURCE =
            "#if __VERSION__ >= 130\n" +
            "  #define varying in\n" +
            "  out vec4 mgl_FragColour;\n" +
            "#else\n" +
            "  #define mgl_FragColour gl_FragColor   \n" +
            "#endif\n" +
            "#ifdef GL_ES\n" +
            "  #define MEDIUMP mediump\n" +
            "#else\n" +
            "  #define MEDIUMP\n" +
            "#endif\n" +
            "uniform MEDIUMP vec4 u_Colour;\n" +
            "void main() {\n" +
            "    mgl_FragColour = u_Colour;\n" +
            "}";
    private static final List<String> ATTRIBUTES = Collections.singletonList("a_Position");
    private static final List<String> UNIFORMS = Arrays.asList("u_MVPMatrix", "u_Colour");
    private static final String MESH_NAME = "cube";
    private static final int MESH_VERTEX_COUNT = 28;
    private static final List<Double> MESH_VERTICES = Arrays.asList(
            0.5, -0.5, -0.5,
            0.5, -0.5, 0.5,
            0.5, -0.5, -0.5,
            0.5, 0.5, -0.5,
            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            0.5, -0.5, 0.5,
            -0.5, -0.5, 0.5,
            0.5, -0.5, 0.5,
            0.5, 0.5, 0.5,
            -0.5, -0.5, 0.5,
            -0.5, -0.5, -0.5,
            -0.5, 0.5, 0.5,
            -0.5, 0.5, -0.5,
            -0.5, -0.5, -0.5,
            -0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            0.5, 0.5, -0.5,
            0.5, -0.5, -0.5,
            -0.5, -0.5, -0.5,
            -0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,
            -0.5, -0.5, 0.5,
            -0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,
            0.5, 0.5, -0.5,
            0.5, 0.5, 0.5
    );

    Matrix model = new Matrix();
    Matrix view = new Matrix();
    Matrix projection = new Matrix();
    Matrix mv = new Matrix();
    Matrix mvp = new Matrix();
    Matrix rotation = new Matrix();
    Vector cameraEye = new Vector();
    Vector cameraLookAt = new Vector();
    Vector cameraUp = new Vector();
    GLScene scene;
    GLSurfaceView surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a scene
        scene = new GLScene();
        // Set scene colours
        scene.putFloatArray("blue", new float[] {
                0.0f, // Red
                0.0f, // Green
                1.0f, // Blue
                1.0f, // Alpha
        });
        // Set scene frustum
        scene.putFloatArray("frustum", new float[]{
                0.5f, // Near Plane
                2.5f, // Far Plane
        });
        // Set scene camera
        scene.putVector("camera-eye", cameraEye.set(0.0f, 0.0f, 1.5f));
        scene.putVector("camera-look-at", cameraLookAt.set(0.0f, 0.0f, 0.0f));
        scene.putVector("camera-up", cameraUp.set(0.0f, 1.0f, 0.0f));
        // Set scene matrices
        scene.putMatrix("model", model.makeIdentity());
        scene.putMatrix("view", view.makeIdentity());
        scene.putMatrix("projection", projection.makeIdentity());
        scene.putMatrix("model-view", mv.makeIdentity());
        scene.putMatrix("model-view-projection", mvp.makeIdentity());
        scene.putMatrix("rotation", rotation.makeIdentity());

        // Create mesh, or load from assets, resources, or network
        Mesh mesh = Mesh.newBuilder()
                .setName(MESH_NAME)
                .setVertices(MESH_VERTEX_COUNT)
                .addAllVertex(MESH_VERTICES)
                .build();
        try {
            scene.putVertexMesh(mesh.getName(), new GLVertexMesh(mesh));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create shader, or load from assets, resources, or network
        Shader shader = Shader.newBuilder()
                .setName(SHADER_NAME)
                .setVertexSource(VERTEX_SOURCE)
                .setFragmentSource(FRAGMENT_SOURCE)
                .addAllAttributes(ATTRIBUTES)
                .addAllUniforms(UNIFORMS)
                .build();

        // Create program node
        GLProgramNode programNode = new GLProgramNode(new GLProgram(shader));
        scene.putProgramNode(SHADER_NAME, programNode);

        // Create camera node
        GLCameraNode cameraNode = new GLCameraNode();
        programNode.addChild(cameraNode);

        // Create transformation nodes (translate, rotate, scale)
        MatrixTransformationNode rotationNode = new MatrixTransformationNode("rotation");
        cameraNode.addChild(rotationNode);

        // Create attribute node (colour)
        AttributeNode attributeNode = new AttributeNode(new GLColourAttribute(SHADER_NAME, "blue"));
        rotationNode.addChild(attributeNode);

        // Create mesh node
        attributeNode.addChild(new GLVertexMeshNode(SHADER_NAME, mesh.getName()));

        // Create animation to rotate cube
        rotationNode.setAnimation(new Animation() {
            float angle = 0.0f;
            float increment = 0.01f;
            @Override
            public boolean tick() {
                rotation.makeRotationAxis(angle+=increment, Vector.YV);
                return false;
            }
        });

        // Create surface on which to render
        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        surface.setRenderer(scene);

        // Set activity content view
        setContentView(surface);
    }
}
