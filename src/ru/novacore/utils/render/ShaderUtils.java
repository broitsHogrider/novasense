package ru.novacore.utils.render;

import org.lwjgl.opengl.GL11;
import ru.novacore.utils.FileUtils;
import ru.novacore.utils.client.IMinecraft;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils implements IMinecraft {

    public static ShaderUtils ROUND,MAINMENUSHADER,textShader,bloom, GRADIENT_ROUND,ROUND_SHADER_OUTLINE, VECTOR_ROUND, GLOW_ROUND,out, GRADIENT_GLOW_ROUND, TEXTURE_ROUND, FACE_ROUND, TEXT_MASK, GRADIENT, OUTLINE, GAUSSIAN, KAWASE_UP, KAWASE_DOWN, KAWASE_BLOOM_UP, KAWASE_BLOOM_DOWN;
    private final int programID;

    public ShaderUtils(String fragmentShaderLoc) {
        programID = glCreateProgramObjectARB();

        int fragmentShaderID = switch (fragmentShaderLoc) {
            case "round" -> createShader(new ByteArrayInputStream(round.getBytes()), GL_FRAGMENT_SHADER);
            case "gradientRound" -> createShader(new ByteArrayInputStream(gradientRound.getBytes()), GL_FRAGMENT_SHADER);
            case "vectorRound" -> createShader(new ByteArrayInputStream(vectorRound.getBytes()), GL_FRAGMENT_SHADER);
            case "glowRound" -> createShader(new ByteArrayInputStream(glowRound.getBytes()), GL_FRAGMENT_SHADER);
            case "gradientGlowRound" -> createShader(new ByteArrayInputStream(gradientGlowRound.getBytes()), GL_FRAGMENT_SHADER);
            case "textureRound" -> createShader(new ByteArrayInputStream(textureRound.getBytes()), GL_FRAGMENT_SHADER);
            case "faceRound" -> createShader(new ByteArrayInputStream(faceRound.getBytes()), GL_FRAGMENT_SHADER);
            case "textMask" -> createShader(new ByteArrayInputStream(textMask.getBytes()), GL_FRAGMENT_SHADER);
            case "gradient" -> createShader(new ByteArrayInputStream(gradient.getBytes()), GL_FRAGMENT_SHADER);
            case "outline" -> createShader(new ByteArrayInputStream(outline.getBytes()), GL_FRAGMENT_SHADER);
            case "out" -> createShader(new ByteArrayInputStream(outOneColor.getBytes()), GL_FRAGMENT_SHADER);
            case "gaussian" -> createShader(new ByteArrayInputStream(gaussian.getBytes()), GL_FRAGMENT_SHADER);
            case "kawaseUp" -> createShader(new ByteArrayInputStream(kawaseUp.getBytes()), GL_FRAGMENT_SHADER);
            case "kawaseDown" -> createShader(new ByteArrayInputStream(kawaseDown.getBytes()), GL_FRAGMENT_SHADER);
            case "kawaseBloomUp" -> createShader(new ByteArrayInputStream(kawaseUpBloom.getBytes()), GL_FRAGMENT_SHADER);
            case "kawaseBloomDown" -> createShader(new ByteArrayInputStream(kawaseDownBloom.getBytes()), GL_FRAGMENT_SHADER);
            case "roundedOutline" -> createShader(new ByteArrayInputStream(roundedOutline.getBytes()), GL_FRAGMENT_SHADER);
            case "mainmenu" -> createShader(new ByteArrayInputStream(main.getBytes()), GL_FRAGMENT_SHADER);
            case "textShader" -> createShader(new ByteArrayInputStream(text.getBytes()), GL_FRAGMENT_SHADER);
            case "bloom" -> createShader(new ByteArrayInputStream(kawaseBloom.getBytes()), GL_FRAGMENT_SHADER);
            default -> throw new IllegalStateException("Unexpected value: " + fragmentShaderLoc);
        };
        glAttachObjectARB(programID, fragmentShaderID);
        glAttachObjectARB(programID, createShader(new ByteArrayInputStream(vertex.getBytes()), GL_VERTEX_SHADER));
        glLinkProgramARB(programID);
    }

    public static void init() {
        ROUND = new ShaderUtils("round");
        GRADIENT_ROUND = new ShaderUtils("gradientRound");
        VECTOR_ROUND = new ShaderUtils("vectorRound");
        GLOW_ROUND = new ShaderUtils("glowRound");
        GRADIENT_GLOW_ROUND = new ShaderUtils("gradientGlowRound");
        TEXTURE_ROUND = new ShaderUtils("textureRound");
        FACE_ROUND = new ShaderUtils("faceRound");
        TEXT_MASK = new ShaderUtils("textMask");
        GRADIENT = new ShaderUtils("gradient");
        OUTLINE = new ShaderUtils("outline");
        GAUSSIAN = new ShaderUtils("gaussian");
        KAWASE_UP = new ShaderUtils("kawaseUp");
        KAWASE_DOWN = new ShaderUtils("kawaseDown");
        KAWASE_BLOOM_UP = new ShaderUtils("kawaseBloomUp");
        KAWASE_BLOOM_DOWN = new ShaderUtils("kawaseBloomDown");
        ROUND_SHADER_OUTLINE = new ShaderUtils("roundedOutline");
        out = new ShaderUtils("out");
        MAINMENUSHADER = new ShaderUtils("mainmenu");
        textShader = new ShaderUtils("textShader");
        bloom = new ShaderUtils("bloom");
    }

    public int getUniform(String name) {
        return glGetUniformLocationARB(programID, name);
    }

    public void attach() {
        glUseProgramObjectARB(programID);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void setUniform(String name, float... args) {
        int loc = glGetUniformLocationARB(programID, name);
        switch (args.length) {
            case 1: {
                glUniform1fARB(loc, args[0]);
                break;
            }
            case 2: {
                glUniform2fARB(loc, args[0], args[1]);
                break;
            }
            case 3: {
                glUniform3fARB(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                glUniform4fARB(loc, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }

    public void setUniform(String name, int... args) {
        int loc = glGetUniformLocationARB(programID, name);
        switch (args.length) {
            case 1: {
                glUniform1iARB(loc, args[0]);
                break;
            }
            case 2: {
                glUniform2iARB(loc, args[0], args[1]);
            }
            case 3: {
                glUniform3iARB(loc, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                glUniform4iARB(loc, args[0], args[1], args[2], args[3]);
            }
        }
    }

    public void setUniformf(String var1, float... args) {
        int var3 = glGetUniformLocationARB(this.programID, var1);
        switch (args.length) {
            case 1: {
                glUniform1fARB(var3, args[0]);
                break;
            }
            case 2: {
                glUniform2fARB(var3, args[0], args[1]);
                break;
            }
            case 3: {
                glUniform3fARB(var3, args[0], args[1], args[2]);
                break;
            }
            case 4: {
                glUniform4fARB(var3, args[0], args[1], args[2], args[3]);
                break;
            }
        }
    }

    public static void drawQuads() {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, Math.max(window.getScaledHeight(), 1));
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(Math.max(window.getScaledWidth(), 1), Math.max(window.getScaledHeight(), 1));
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(Math.max(window.getScaledWidth(), 1), 0);
        GL11.glEnd();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShaderObjectARB(shaderType);
        glShaderSourceARB(shader, FileUtils.readInputStream(inputStream));
        glCompileShaderARB(shader);
        if (glGetShaderi(shader, 35713) == 0) {
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }
        return shader;
    }

    String vertex = """
            #version 120
                        
            void main() {
                gl_TexCoord[0] = gl_MultiTexCoord0;
                gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
            }
            """;

    String main = """
            #extension GL_OES_standard_derivatives : enable
            
            #ifdef GL_ES
            precision mediump float;
            #endif
            
            #ifndef REDUCER
            #define _GLF_ZERO(X, Y)          (Y)
            #define _GLF_ONE(X, Y)           (Y)
            #define _GLF_FALSE(X, Y)         (Y)
            #define _GLF_TRUE(X, Y)          (Y)
            #define _GLF_IDENTITY(X, Y)      (Y)
            #define _GLF_DEAD(X)             (X)
            #define _GLF_FUZZED(X)           (X)
            #define _GLF_WRAPPED_LOOP(X)     X
            #define _GLF_WRAPPED_IF_TRUE(X)  X
            #define _GLF_WRAPPED_IF_FALSE(X) X
            #endif
            
            // END OF GENERATED HEADER
            
            vec2 injectionSwitch = vec2(0.0, 1.0);
            
            uniform float time;
            
            uniform vec2 resolution;
            
            void main(void)
            {
                if (injectionSwitch.x > injectionSwitch.y)
                {
                    if (injectionSwitch.x > injectionSwitch.y)
                        return;
                    int donor_replacementfrom ;
                }
                vec2 uv = gl_FragCoord.xy / resolution.xy - .5;
                uv.y *= resolution.y / resolution.x;
                vec3 dir = vec3(uv * 1.4, 1.);
                float a2 = time * 20. + .5;
                float a1 = 0.0;
                mat2 rot1 = mat2(cos(a1), sin(a1), - sin(a1), cos(a1));
                mat2 rot2 = rot1;
                dir.xz *= rot1;
                dir.xy *= rot2;
                vec3 from = vec3(0., 0., 0.);
                from += vec3(1.25 * sin(time),  -1.03 * time, - 2.);
                from.xz *= rot1;
                from.xy *= rot2;
                float s = .1, fade = .07;
                vec3 v = vec3(0.4);
                for(
                    int r = 0;
                    r < 12;
                    r ++
                )
                    {
                        vec3 p = from + s * dir * 1.5;
                        p = abs(vec3(0.750) - mod(p, vec3(0.750 * 2.)));
                        p.x += float(r * r) * 0.01;
                        p.y += float(r) * 0.02;
                        float pa, a = pa = 0.;
                        for(
                            int i = 0;
                            i < 15;
                            i ++
                        )
                            {
                                p = abs(p) / dot(p, p) - 0.340;
                                a += abs(length(p) - pa * 0.2);
                                if (injectionSwitch.x > injectionSwitch.y)
                                    discard;
                                pa = length(p);
                            }
                        a *= a * a * 2.;
                        v += vec3(s, s * s, s * s * s * s) * a * 0.0017 * fade;
                        fade *= 0.960;
                        s += 0.110;
                    }
                v = mix(vec3(length(v)), v, 0.8);
                gl_FragColor = vec4(v * .01, 1.);
            }""";

    String roundedOutline = """
                       #version 120
                       \s
            uniform vec2 location, rectSize;
            uniform vec4 color, outlineColor1,outlineColor2,outlineColor3,outlineColor4;
            uniform float radius, outlineThickness;
            #define NOISE .5/255.0
            
            float roundedSDF(vec2 centerPos, vec2 size, float radius) {
                return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
            }
            
            vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4)
            {
                vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                //Dithering the color
                // from https://shader-tutorial.dev/advanced/color-banding-dithering/
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                return color;
            }
            
            void main() {
                float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness * 0.5) - 1.0, radius);
            
                float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * 0.5));
                vec4 outlineColor = vec4(createGradient(gl_TexCoord[0].st, outlineColor1.rgb, outlineColor2.rgb, outlineColor3.rgb, outlineColor4.rgb), outlineColor1.a);
                vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);
                gl_FragColor = mix(outlineColor, insideColor, blendAmount);
            }
            """;

    String outOneColor = """
            #version 120
         
            // ���������� ����������
            uniform vec2 size; // ������ ��������������
            uniform vec4 round; // ������������ ���������� �����
            uniform vec2 smoothness; // ��������� �������� �� ����� � ������������
            uniform float value; // ��������, ������������ ��� ������� ���������� �� �������
            uniform vec4 color; // ���� ��������������
            uniform float outlineSize; // ������ �������
            uniform vec4 outlineColor; // ���� �������
            
            // ������� ��� ������� ���������� �� �������
            float test(vec2 vec_1, vec2 vec_2, vec4 vec_4) {
                vec_4.xy = (vec_1.x > 0.0) ? vec_4.xy : vec_4.zw;
                vec_4.x = (vec_1.y > 0.0) ? vec_4.x : vec_4.y;
                vec2 coords = abs(vec_1) - vec_2 + vec_4.x;
                return min(max(coords.x, coords.y), 0.0) + length(max(coords, vec2(0.0f))) - vec_4.x;
            }
            
            void main() {
                vec2 st = gl_TexCoord[0].st * size; // ���������� �������� �������
                vec2 halfSize = 0.5 * size; // �������� ������� ��������������
                float sa = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value, round));
                // ������������ ������������ � ����������� �� ���������� �� �������
                gl_FragColor = mix(vec4(color.rgb, 0.0), vec4(color.rgb, color.a), sa); // ������������� ���� �������������� � ������������� sa
              \s
                // ��������� �������
                vec2 outlineSizeVec = size + vec2(outlineSize);
                float outlineDist = test(halfSize - st, halfSize - value, round);
                float outline = smoothstep(smoothness.x, smoothness.y, outlineDist) - smoothstep(smoothness.x, smoothness.y, outlineDist - outlineSize);
                if (outlineDist < outlineSize)
                    gl_FragColor = mix(gl_FragColor, outlineColor, outline);
            }""";
    String round = """
            #version 120

            uniform vec2 size;
            uniform vec4 color;
            uniform float radius;

            float calcLength(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b , 0)) - r;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                vec2 halfSize = size * 0.5;
                float distance = calcLength(halfSize - (st * size), halfSize - radius - 1, radius);
                float smoothedAlpha = (1 - smoothstep(0, 2, distance)) * color.a;
                gl_FragColor = vec4(color.rgb, smoothedAlpha);
            }
            """;

    String gradientRound = """
            #version 120
            
            uniform vec2 location, rectSize;
            uniform vec4 color1, color2, color3, color4;
            uniform vec4 cornerRadii; // (topLeft, topRight, bottomLeft, bottomRight)
            
            #define NOISE 0.5 / 255.0
            
            float calc_length(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b, 0.0)) - r;
            }
            
            float getCornerRadius(vec2 st) {
                return (st.x < 0.5) ? ((st.y > 0.5) ? cornerRadii.z : cornerRadii.x) // bottomLeft : topLeft
                                    : ((st.y > 0.5) ? cornerRadii.w : cornerRadii.y); // bottomRight : topRight
            }
            
            vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                return color;
            }
            
            void main() {
                vec2 halfSize = rectSize * 0.5;
                vec2 st = gl_TexCoord[0].st;
                float cornerRadius = getCornerRadius(st);
                float dist = calc_length(halfSize - (st * rectSize), halfSize - cornerRadius - 1.0, cornerRadius);
                float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, dist)) * color1.a;
                gl_FragColor = vec4(createGradient(st, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);
            }
            """;

    String vectorRound = """
            #version 120
                        
            uniform vec2 size;
            uniform vec4 color;
            uniform vec4 radius;

            float calcLength(vec2 p, vec2 b, vec4 r) {
                r.xy = (p.x > 0) ? r.xy : r.zw;
                r.x = (p.y > 0) ? r.x : r.y;
                vec2 coords = abs(p) - b + r.x;
                return min(max(coords.x, coords.y), 0) + length(max(coords, vec2(0))) - r.x;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                vec2 halfSize = size * 0.5;
                float distance = calcLength(halfSize - (st * size), halfSize - 1, radius);
                float smoothedAlpha = (1 - smoothstep(0, 1, distance)) * color.a;
                gl_FragColor = vec4(color.rgb, smoothedAlpha);
            }
            """;

    String glowRound = """
            #version 120
                        
            uniform vec2 size;
            uniform vec4 color;
            uniform float radius, glowRadius;
            
            float calcLength(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b , 0)) - r;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                vec2 halfSize = size * 0.5;
                float distance = calcLength(halfSize - (st * size), halfSize - radius - glowRadius, radius);
                float smoothedAlpha = (1 - smoothstep(-glowRadius, glowRadius, distance)) * color.a;
                gl_FragColor = vec4(color.rgb, smoothedAlpha);
            }
            """;

    String gradientGlowRound = """
            #version 120
                        
            uniform vec2 size;
            uniform vec4 color1, color2, color3, color4;
            uniform float radius, glowRadius;
            
            float calcLength(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b , 0)) - r;
            }
            
            vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
                vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                return color;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                vec2 halfSize = size * 0.5;
                float distance = calcLength(halfSize - (st * size), halfSize - radius - glowRadius, radius);
                float smoothedAlpha = (1 - smoothstep(-glowRadius, glowRadius, distance)) * color1.a;
                gl_FragColor = vec4(createGradient(st, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);
            }
            """;

    String textureRound = """
            #version 120

            uniform vec2 size;
            uniform sampler2D textureIn;
            uniform float radius, alpha;

            float calcLength(vec2 centerPos, vec2 size, float radius) {
                return length(max(abs(centerPos) - size, 0)) - radius;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                float distance = calcLength((size * 0.5) - (st * size), (size * 0.5) - radius - 1, radius);
                float smoothedAlpha = (1 - smoothstep(0, 2, distance)) * alpha;
                gl_FragColor = vec4(texture2D(textureIn, st).rgb, smoothedAlpha);
            }
            """;

    String faceRound = """
            #version 120
            
            uniform vec2 location, size;
            uniform sampler2D texture;
            uniform float radius, alpha;
            uniform float u, v, w, h;
            uniform float hurtFactor; // ����� �������� ��� ���������� ������� ������
            
            float calcLength(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b, 0)) - r;
            }
            
            void main() {
                vec2 halfSize = size * 0.5;
                vec2 st = gl_TexCoord[0].st;
                st.x = u + st.x * w;
                st.y = v + st.y * h;
               \s
                float distance = calcLength(halfSize - (gl_TexCoord[0].st * size), halfSize - radius - 1, radius);
                float smoothedAlpha = (1 - smoothstep(0, 2, distance)) * alpha;
                vec4 color = texture2D(texture, st);
               \s
                // ��������� ������ �������� �����
                vec3 redTint = mix(color.rgb, vec3(0.5, 0.0, 0.0), hurtFactor);
               \s
                gl_FragColor = vec4(redTint, smoothedAlpha);
            }
            """;

    String textMask = """
            #version 120

            uniform sampler2D font;
            uniform vec4 inColor;
            uniform float width;
            uniform float maxWidth;

            void main() {
                float f = clamp(smoothstep(0.5, 1, 1 - (gl_FragCoord.x - maxWidth) / width), 0, 1);
                vec2 pos = gl_TexCoord[0].xy;
                vec4 color = texture2D(font, pos);
                
                if (color.a > 0) color.a = color.a * f;
                
                gl_FragColor = color * inColor;
            }
            """;

    String gradient = """
            #version 120
                                         
            uniform vec2 location, size;
            uniform sampler2D texture;
            uniform vec4 color1, color2, color3, color4;
            
            vec3 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
                 vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                 return color;
            }
            
            void main() {
                 vec2 coords = (gl_FragCoord.xy - location) / size;
                 float textureAlpha = texture2D(texture, gl_TexCoord[0].st).a;
                 gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4).rgb, textureAlpha);
            }
            """;

    String outline = """
           #version 120
                                     
           uniform vec4 color;
           uniform sampler2D textureIn, textureToCheck;
           uniform vec2 texelSize, direction;
           uniform float size;
           
           #define offset direction * texelSize
           
           void main() {
               if (direction.y == 1) {
                   if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0) discard;
               }
               vec4 innerAlpha = texture2D(textureIn, gl_TexCoord[0].st);
               innerAlpha *= innerAlpha.a;
               for (float r = 1; r <= size; r ++) {
                   vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
                   vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);
                   colorCurrent1.rgb *= colorCurrent1.a;
                   colorCurrent2.rgb *= colorCurrent2.a;
                   innerAlpha += (colorCurrent1 + colorCurrent2) * r;
               }
               gl_FragColor = vec4(innerAlpha.rgb / innerAlpha.a, mix(innerAlpha.a, 1 - exp(-innerAlpha.a), step(0, direction.y)));
            }
            """;

    String gaussian = """
            #version 120
            
            uniform sampler2D textureIn;
            uniform vec2 texelSize, direction;
            uniform float radius, weights[256];
            
            #define offset texelSize * direction
            
            void main() {
                vec3 color = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];
                float totalWeight = weights[0];
                for (float f = 1; f <= radius; f++) {
                    color += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * (weights[int(abs(f))]);
                    color += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * (weights[int(abs(f))]);
                    totalWeight += (weights[int(abs(f))]) * 2;
                }
                gl_FragColor = vec4(color / totalWeight, 1);
            }
            """;

    String text = """
            #version 120
                           
                            
                uniform sampler2D Sampler;
                uniform vec2 TextureSize;
                uniform float Range; // distance field range of the msdf font texture
                uniform float EdgeStrength;
                uniform float Thickness;
                uniform vec4 color;
                uniform bool Outline; // if false, outline computation will be ignored (and its uniforms)
                uniform float OutlineThickness;
                uniform vec4 OutlineColor;
                           
                            
                float median(float red, float green, float blue) {
                  return max(min(red, green), min(max(red, green), blue));
                }
                            
                void main() {
                    vec4 texColor = texture2D(Sampler, gl_TexCoord[0].st);
                            
                    float dx = dFdx(gl_TexCoord[0].x) * TextureSize.x;
                    float dy = dFdy(gl_TexCoord[0].y) * TextureSize.y;
                    float toPixels = Range * inversesqrt(dx * dx + dy * dy);
                            
                    float sigDist = median(texColor.r, texColor.g, texColor.b) - 0.5 + Thickness;
                   

                    float alpha = smoothstep(-EdgeStrength, EdgeStrength, sigDist * toPixels);
                    if (Outline) {
                        float outlineAlpha = smoothstep(-EdgeStrength, EdgeStrength, (sigDist + OutlineThickness) * toPixels) - alpha;
                        float finalAlpha = alpha * color.a + outlineAlpha * color.a;
                     
                        gl_FragColor = vec4(mix(OutlineColor.rgb, color.rgb, alpha), finalAlpha);
                        return;
                    }
                    gl_FragColor = vec4(color.rgb, color.a * alpha);
                }""";

    String kawaseUp = """
                 #version 120
                uniform sampler2D image;
                uniform float offset;
                uniform vec2 resolution;
                
                void main()
                {
                    vec2 uv = gl_TexCoord[0].xy / 2.0;
                    vec2 halfpixel = resolution / 2.0;
                    vec3 sum = texture2D(image, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset).rgb;
                    sum += texture2D(image, uv + vec2(-halfpixel.x, halfpixel.y) * offset).rgb * 2.0;
                    sum += texture2D(image, uv + vec2(0.0, halfpixel.y * 2.0) * offset).rgb;   
                    sum += texture2D(image, uv + vec2(halfpixel.x, halfpixel.y) * offset).rgb * 2.0;
                    sum += texture2D(image, uv + vec2(halfpixel.x * 2.0, 0.0) * offset).rgb;
                    sum += texture2D(image, uv + vec2(halfpixel.x, -halfpixel.y) * offset).rgb * 2.0; 
                    sum += texture2D(image, uv + vec2(0.0, -halfpixel.y * 2.0) * offset).rgb;  
                    sum += texture2D(image, uv + vec2(-halfpixel.x, -halfpixel.y) * offset).rgb * 2.0; 
                    gl_FragColor = vec4(sum / 12.0, 1);
                }""";

    String kawaseDown = "#version 120\n" +
            "\n" +
            "uniform sampler2D image;\n" +
            "uniform float offset;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec2 uv = gl_TexCoord[0].xy * 2.0;\n" +
            "    vec2 halfpixel = resolution * 2.0;\n" +
            "    vec3 sum = texture2D(image, uv).rgb * 4.0;\n" +
            "    sum += texture2D(image, uv - halfpixel.xy * offset).rgb;\n" +
            "    sum += texture2D(image, uv + halfpixel.xy * offset).rgb;\n" +
            "    sum += texture2D(image, uv + vec2(halfpixel.x, -halfpixel.y) * offset).rgb;\n" +
            "    sum += texture2D(image, uv - vec2(halfpixel.x, -halfpixel.y) * offset).rgb;\n" +
            "    gl_FragColor = vec4(sum / 8.0, 1);\n" +
            "}";

    String kawaseUpBloom = """
            #version 120

            uniform sampler2D inTexture, textureToCheck;
            uniform vec2 halfPixel, offset, resolution;
            uniform int check;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / resolution);
                vec4 sum = texture2D(inTexture, uv + vec2(-halfPixel.x * 2, 0) * offset);
                sum.rgb *= sum.a;
                vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfPixel.x, halfPixel.y) * offset);
                smpl1.rgb *= smpl1.a;
                sum += smpl1 * 2;
                vec4 smp2 = texture2D(inTexture, uv + vec2(0, halfPixel.y * 2) * offset);
                smp2.rgb *= smp2.a;
                sum += smp2;
                vec4 smp3 = texture2D(inTexture, uv + vec2(halfPixel.x, halfPixel.y) * offset);
                smp3.rgb *= smp3.a;
                sum += smp3 * 2;
                vec4 smp4 = texture2D(inTexture, uv + vec2(halfPixel.x * 2, 0) * offset);
                smp4.rgb *= smp4.a;
                sum += smp4;
                vec4 smp5 = texture2D(inTexture, uv + vec2(halfPixel.x, -halfPixel.y) * offset);
                smp5.rgb *= smp5.a;
                sum += smp5 * 2;
                vec4 smp6 = texture2D(inTexture, uv + vec2(0, -halfPixel.y * 2) * offset);
                smp6.rgb *= smp6.a;
                sum += smp6;
                vec4 smp7 = texture2D(inTexture, uv + vec2(-halfPixel.x, -halfPixel.y) * offset);
                smp7.rgb *= smp7.a;
                sum += smp7 * 2;
                vec4 result = sum / 12;
                gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1 - texture2D(textureToCheck, gl_TexCoord[0].st).a), check));
            }
            """;

    String kawaseDownBloom = """
            #version 120

            uniform sampler2D inTexture;
            uniform vec2 offset, halfPixel, resolution;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / resolution);
                vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);
                sum.rgb *= sum.a;
                sum *= 4;
                vec4 smp1 = texture2D(inTexture, uv - halfPixel.xy * offset);
                smp1.rgb *= smp1.a;
                sum += smp1;
                vec4 smp2 = texture2D(inTexture, uv + halfPixel.xy * offset);
                smp2.rgb *= smp2.a;
                sum += smp2;
                vec4 smp3 = texture2D(inTexture, uv + vec2(halfPixel.x, -halfPixel.y) * offset);
                smp3.rgb *= smp3.a;
                sum += smp3;
                vec4 smp4 = texture2D(inTexture, uv - vec2(halfPixel.x, -halfPixel.y) * offset);
                smp4.rgb *= smp4.a;
                sum += smp4;
                vec4 result = sum / 8;
                gl_FragColor = vec4(result.rgb / result.a, result.a);
            }
            """;
    String kawaseBloom = """
            #version 120

                uniform sampler2D textureIn, textureToCheck;
                uniform vec2 texelSize, direction;
                uniform float exposure, radius;
                uniform float weights[128];
                uniform bool avoidTexture;

                #define offset direction * texelSize

                void main() {
                    if (direction.y >= 1 && avoidTexture) {
                        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
                    }
                    vec4 innerAlpha = texture2D(textureIn, gl_TexCoord[0].st);
                    innerAlpha *= innerAlpha.a;
                    innerAlpha *= weights[0];



                    for (float r = 1.0; r <= radius; r ++) {
                        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
                        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);

                        colorCurrent1.rgb *= colorCurrent1.a;
                        colorCurrent2.rgb *= colorCurrent2.a;

                        innerAlpha += (colorCurrent1 + colorCurrent2) * weights[int(r)];

                    }

                    gl_FragColor = vec4(innerAlpha.rgb / innerAlpha.a, mix(innerAlpha.a, 1.0 - exp(-innerAlpha.a * exposure), step(0.0, direction.y)));
                }""";
}