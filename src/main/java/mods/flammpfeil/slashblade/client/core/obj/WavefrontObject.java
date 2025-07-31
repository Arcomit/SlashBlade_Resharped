package mods.flammpfeil.slashblade.client.core.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wavefront OBJ模型加载器
 * 基于Wavefront .obj文件规范实现：http://en.wikipedia.org/wiki/Wavefront_.obj_file
 */
public class WavefrontObject {

    // 正则表达式
    // 顶点，法线，UV
    private static Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
    private static Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
    private static Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
    private static Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

    // 正则匹配器
    private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
    private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
    private static Matcher groupObjectMatcher;

    // 模型数据存储
    public ArrayList<Vertex> vertices = new ArrayList<Vertex>();          // 顶点列表
    public ArrayList<Vertex> vertexNormals = new ArrayList<Vertex>();     // 法线列表
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>(); // 纹理坐标列表
    public ArrayList<GroupObject> groupObjects = new ArrayList<GroupObject>(); // 组对象列表
    private GroupObject currentGroupObject; // 当前处理的组对象
    private String fileName; // 模型文件名

    /**
     * 通过资源位置构造OBJ模型
     * @param resource 模型资源位置
     */
    public WavefrontObject(ResourceLocation resource) throws ModelFormatException {
        this.fileName = resource.toString();
        try {
            loadObjModel(Minecraft.getInstance().getResourceManager().open(resource));
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }
    }

    /**
     * 通过输入流构造OBJ模型
     * @param filename 模型文件名（仅用于错误信息）
     * @param inputStream 模型数据输入流
     */
    public WavefrontObject(String filename, InputStream inputStream) throws ModelFormatException {
        this.fileName = filename;
        loadObjModel(inputStream);
    }

    /**
     * 加载并解析OBJ模型
     * @param inputStream 模型数据输入流
     */
    private void loadObjModel(InputStream inputStream) throws ModelFormatException {
        BufferedReader reader = null;
        String currentLine;
        int lineCount = 0;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                // 标准化行格式：移除多余空格
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

                // 跳过注释和空行
                if (currentLine.startsWith("#") || currentLine.length() == 0) {
                    continue;
                }
                // 顶点解析
                else if (currentLine.startsWith("v ")) {
                    Vertex vertex = parseVertex(currentLine, lineCount);
                    if (vertex != null) vertices.add(vertex);
                }
                // 法线解析
                else if (currentLine.startsWith("vn ")) {
                    Vertex vertex = parseVertexNormal(currentLine, lineCount);
                    if (vertex != null) vertexNormals.add(vertex);
                }
                // 纹理坐标解析
                else if (currentLine.startsWith("vt ")) {
                    TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                    if (textureCoordinate != null) textureCoordinates.add(textureCoordinate);
                }
                // 面解析
                else if (currentLine.startsWith("f ")) {
                    // 确保存在当前组对象
                    if (currentGroupObject == null) {
                        currentGroupObject = new GroupObject("Default");
                    }
                    Face face = parseFace(currentLine, lineCount);
                    if (face != null){
                        currentGroupObject.faces.add(face);
                        currentGroupObject.vertexCount += face.vertices.length;
                    }
                }
                // 组/对象解析
                else if (currentLine.startsWith("g ") || currentLine.startsWith("o ")) {
                    GroupObject group = parseGroupObject(currentLine, lineCount);
                    // 保存前一个组对象
                    if (currentGroupObject != null) {
                        groupObjects.add(currentGroupObject);
                    }
                    currentGroupObject = group;
                }
            }
            // 保存最后一个组对象
            if (currentGroupObject != null) {
                groupObjects.add(currentGroupObject);
            }
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        } finally {
            // 资源清理
            try {
                if (reader != null) reader.close();
            } catch (IOException ignored) {}
            try {
                inputStream.close();
            } catch (IOException ignored) {}
        }
    }

    // ================ 渲染方法 ================ //

    @OnlyIn(Dist.CLIENT)
    public void initAll(){
        for (GroupObject groupObject : groupObjects) {
            groupObject.init();
        }
    }

    /** 渲染全部组对象 */
    @OnlyIn(Dist.CLIENT)
    public void renderAll(RenderStateShard renderType) {
        for (GroupObject groupObject : groupObjects) {
            groupObject.render(renderType);
        }
    }

    /** 仅渲染指定名称的组 */
    @OnlyIn(Dist.CLIENT)
    public void renderOnly(RenderStateShard renderType, String... groupNames) {
        for (GroupObject groupObject : groupObjects) {
            for (String groupName : groupNames) {
                if (groupName.equalsIgnoreCase(groupObject.name)) {
                    groupObject.render(renderType);
                }
            }
        }
    }

    /** 渲染特定名称的组 */
    @OnlyIn(Dist.CLIENT)
    public void renderPart(RenderStateShard renderType, String partName) {
        for (GroupObject groupObject : groupObjects) {
            if (partName.equalsIgnoreCase(groupObject.name)) {
                groupObject.render(renderType);
            }
        }
    }

    /** 渲染除指定名称外的所有组 */
    @OnlyIn(Dist.CLIENT)
    public void renderAllExcept(RenderStateShard renderType, String... excludedGroupNames) {
        for (GroupObject groupObject : groupObjects) {
            boolean exclude = false;
            for (String excludedGroupName : excludedGroupNames) {
                if (excludedGroupName.equalsIgnoreCase(groupObject.name)) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                groupObject.render(renderType);
            }
        }
    }

    // ================ 解析方法 ================ //

    /** 解析顶点数据 */
    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        if (!isValidVertexLine(line)) {
            throw formatException(line, lineCount);
        }

        line = line.substring(line.indexOf(" ") + 1);
        String[] tokens = line.split(" ");

        try {
            switch (tokens.length) {
                case 2: return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
                case 3: return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
                default: throw new ModelFormatException("Invalid vertex format at line " + lineCount);
            }
        } catch (NumberFormatException e) {
            throw new ModelFormatException("Number formatting error at line " + lineCount, e);
        }
    }

    /** 解析顶点法线数据 */
    private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
        if (!isValidVertexNormalLine(line)) {
            throw formatException(line, lineCount);
        }

        line = line.substring(line.indexOf(" ") + 1);
        String[] tokens = line.split(" ");

        try {
            if (tokens.length == 3) {
                return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } else {
                throw new ModelFormatException("Invalid vertex normal format at line " + lineCount);
            }
        } catch (NumberFormatException e) {
            throw new ModelFormatException("Number formatting error at line " + lineCount, e);
        }
    }

    /** 解析纹理坐标数据（自动翻转V坐标） */
    private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
        if (!isValidTextureCoordinateLine(line)) {
            throw formatException(line, lineCount);
        }

        line = line.substring(line.indexOf(" ") + 1);
        String[] tokens = line.split(" ");

        try {
            if (tokens.length == 2) {
                return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
            } else if (tokens.length == 3) {
                return new TextureCoordinate(
                        Float.parseFloat(tokens[0]),
                        1 - Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2])
                );
            } else {
                throw new ModelFormatException("Invalid texture coordinate format at line " + lineCount);
            }
        } catch (NumberFormatException e) {
            throw new ModelFormatException("Number formatting error at line " + lineCount, e);
        }
    }

    /** 解析面数据（支持多种格式） */
    private Face parseFace(String line, int lineCount) throws ModelFormatException {
        if (!isValidFaceLine(line)) {
            throw formatException(line, lineCount);
        }

        Face face = new Face();
        String trimmedLine = line.substring(line.indexOf(" ") + 1);
        String[] tokens = trimmedLine.split(" ");

        // 确定绘图模式（三角形/四边形）
        if (tokens.length == 3) {
            if (currentGroupObject.glDrawingMode == -1) {
                currentGroupObject.glDrawingMode = GL11.GL_TRIANGLES;
            } else if (currentGroupObject.glDrawingMode != GL11.GL_TRIANGLES) {
                throw new ModelFormatException("Mixed face types in group at line " + lineCount);
            }
        } else if (tokens.length == 4) {
            if (currentGroupObject.glDrawingMode == -1) {
                currentGroupObject.glDrawingMode = GL11.GL_QUADS;
            } else if (currentGroupObject.glDrawingMode != GL11.GL_QUADS) {
                throw new ModelFormatException("Mixed face types in group at line " + lineCount);
            }
        } else {
            throw new ModelFormatException("Unsupported face vertex count at line " + lineCount);
        }

        // 根据格式解析面数据
        if (isValidFace_V_VT_VN_Line(line)) {
            parseFace_V_VT_VN(face, tokens);
        } else if (isValidFace_V_VT_Line(line)) {
            parseFace_V_VT(face, tokens);
        } else if (isValidFace_V_VN_Line(line)) {
            parseFace_V_VN(face, tokens);
        } else if (isValidFace_V_Line(line)) {
            parseFace_V(face, tokens);
        } else {
            throw formatException(line, lineCount);
        }

        // 计算面法线
        face.faceNormal = face.calculateFaceNormal();
        return face;
    }

    // 面解析辅助方法
    private void parseFace_V_VT_VN(Face face, String[] tokens) {
        face.vertices = new Vertex[tokens.length];
        face.textureCoordinates = new TextureCoordinate[tokens.length];
        face.vertexNormals = new Vertex[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String[] subTokens = tokens[i].split("/");
            int vIdx = Integer.parseInt(subTokens[0]) - 1;
            int vtIdx = Integer.parseInt(subTokens[1]) - 1;
            int vnIdx = Integer.parseInt(subTokens[2]) - 1;

            face.vertices[i] = vertices.get(vIdx);
            face.textureCoordinates[i] = textureCoordinates.get(vtIdx);
            face.vertexNormals[i] = vertexNormals.get(vnIdx);
        }
    }

    private void parseFace_V_VT(Face face, String[] tokens) {
        face.vertices = new Vertex[tokens.length];
        face.textureCoordinates = new TextureCoordinate[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String[] subTokens = tokens[i].split("/");
            face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
            face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
        }
    }

    private void parseFace_V_VN(Face face, String[] tokens) {
        face.vertices = new Vertex[tokens.length];
        face.vertexNormals = new Vertex[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            String[] subTokens = tokens[i].split("//");
            face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
            face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
        }
    }

    private void parseFace_V(Face face, String[] tokens) {
        face.vertices = new Vertex[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
        }
    }

    /** 解析组/对象 */
    private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        if (!isValidGroupObjectLine(line)) {
            throw formatException(line, lineCount);
        }

        String name = line.substring(line.indexOf(" ") + 1).trim();
        if (!name.isEmpty()) {
            return new GroupObject(name);
        }
        return null;
    }

    // ================ 格式验证方法 ================ //
    // 以下方法使用预编译的正则表达式验证各类型行

    private static boolean isValidVertexLine(String line) {
        return getMatcher(vertexPattern, vertexMatcher, line).matches();
    }

    private static boolean isValidVertexNormalLine(String line) {
        return getMatcher(vertexNormalPattern, vertexNormalMatcher, line).matches();
    }

    private static boolean isValidTextureCoordinateLine(String line) {
        return getMatcher(textureCoordinatePattern, textureCoordinateMatcher, line).matches();
    }

    private static boolean isValidFace_V_VT_VN_Line(String line) {
        return getMatcher(face_V_VT_VN_Pattern, face_V_VT_VN_Matcher, line).matches();
    }

    private static boolean isValidFace_V_VT_Line(String line) {
        return getMatcher(face_V_VT_Pattern, face_V_VT_Matcher, line).matches();
    }

    private static boolean isValidFace_V_VN_Line(String line) {
        return getMatcher(face_V_VN_Pattern, face_V_VN_Matcher, line).matches();
    }

    private static boolean isValidFace_V_Line(String line) {
        return getMatcher(face_V_Pattern, face_V_Matcher, line).matches();
    }

    private static boolean isValidFaceLine(String line) {
        return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) ||
                isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
    }

    private static boolean isValidGroupObjectLine(String line) {
        return getMatcher(groupObjectPattern, groupObjectMatcher, line).matches();
    }

    /** 辅助方法：获取/重置匹配器 */
    private static Matcher getMatcher(Pattern pattern, Matcher matcher, String line) {
        if (matcher == null) {
            matcher = pattern.matcher(line);
        } else {
            matcher.reset(line);
        }
        return matcher;
    }

    /** 生成格式异常 */
    private ModelFormatException formatException(String line, int lineCount) {
        return new ModelFormatException(
                String.format("Error parsing line %d ('%s') in file '%s' - Invalid format",
                        lineCount, line, fileName)
        );
    }

    /** 返回模型类型标识 */
    public String getType() {
        return "obj";
    }
}