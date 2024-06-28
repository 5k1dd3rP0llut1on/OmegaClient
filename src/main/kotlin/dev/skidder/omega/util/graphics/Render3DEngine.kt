package dev.skidder.omega.util.graphics

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.systems.RenderSystem.disableBlend
import dev.skidder.omega.event.impl.Render3DEvent
import dev.skidder.omega.event.safeEventListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import java.awt.Color


object Render3DEngine {

    private val outlineQueue = mutableListOf<OutlineAction>()
    private val filledQueue = mutableListOf<FilledAction>()

    val lastProjMat = Matrix4f()
    val lastModMat = Matrix4f()
    val lastWorldSpaceMatrix = Matrix4f()

    init {
        safeEventListener<Render3DEvent>(-114514, true) {

            val depthFlag = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)

            GL11.glDisable(GL11.GL_DEPTH_TEST)

            if (filledQueue.isNotEmpty()) {
                val tess = Tessellator.getInstance()
                val bufferBuilder = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()
                RenderSystem.setShader { GameRenderer.getPositionColorProgram() }

                filledQueue.forEach { action ->
                    setFilledBoxVertexes(
                        bufferBuilder,
                        it.matrices.peek().positionMatrix,
                        action.box,
                        action.color
                    )
                }
                disableBlend()
                filledQueue.clear()
            }

            if (outlineQueue.isNotEmpty()) {
                setup()
                val tess = Tessellator.getInstance()
                val buffer = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
                RenderSystem.disableCull()
                RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }

                RenderSystem.lineWidth(2f)
                outlineQueue.forEach { action ->
                    RenderSystem.lineWidth(action.lineWidth)
                    setOutlinePoints(
                        action.box,
                        matrixFrom(action.box.minX, action.box.minY, action.box.minZ),
                        buffer,
                        action.color
                    )
                }
                RenderSystem.lineWidth(2f)
                RenderSystem.enableCull()
                cleanup()
                outlineQueue.clear()
            }

            if (depthFlag)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
            else
                GL11.glDisable(GL11.GL_DEPTH_TEST)

        }


    }

    fun drawBoxOutline(box: Box, color: Color, lineWidth: Float) {
        outlineQueue.add(OutlineAction(box, color, lineWidth))
    }
    fun drawLine(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, color: Color, width: Float) {
        setup()
        val matrices = matrixFrom(x1, y1, z1)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        // Line
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
        RenderSystem.lineWidth(width)
        vertexLine(
            matrices, buffer, 0f, 0f, 0f, (x2 - x1).toFloat(), (y2 - y1).toFloat(), (z2 - z1).toFloat(),
            color
        )
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        cleanup()
    }

    fun drawLine(vec1: Vec3d, vec2: Vec3d, color: Color, width: Float) {
        setup()
        val matrices = matrixFrom(vec1.getX(), vec1.getY(), vec1.getZ())
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        // Line
        RenderSystem.disableDepthTest()
        RenderSystem.disableCull()
        RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
        RenderSystem.lineWidth(width)
        vertexLine(
            matrices,
            buffer,
            0f,
            0f,
            0f,
            (vec2.getX() - vec1.getX()).toFloat(),
            (vec2.getY() - vec1.getY()).toFloat(),
            (vec2.getZ() - vec1.getZ()).toFloat(),
            color
        )

        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        cleanup()
    }
    fun drawHoleOutline(box0: Box, color: Color, lineWidth: Float) {
        var box = box0
        setup()
        val matrices = matrixFrom(box.minX, box.minY, box.minZ)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        RenderSystem.disableCull()
        RenderSystem.setShader { GameRenderer.getRenderTypeLinesProgram() }
        RenderSystem.lineWidth(lineWidth)
        box = box.offset(Vec3d(box.minX, box.minY, box.minZ).negate())
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val y2 = box.maxY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val z2 = box.maxZ.toFloat()
        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color)
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color)
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color)
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color)
        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color)
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color)
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color)
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color)
        RenderSystem.enableCull()
        cleanup()
    }

    fun drawFilledBox(box: Box, c: Color) {
        filledQueue.add(FilledAction(box, c))
    }

    fun setOutlinePoints(box: Box, matrices: MatrixStack, buffer: BufferBuilder, color: Color) {
        var box = box
        box = box.offset(Vec3d(box.minX, box.minY, box.minZ).negate())
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val y2 = box.maxY.toFloat()
        val z2 = box.maxZ.toFloat()
        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color)
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color)
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color)
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color)
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color)
        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color)
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color)
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color)
        vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color)
        vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color)
        vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color)
        vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color)
    }

    fun setFilledBoxVertexes(bufferBuilder: BufferBuilder, m: Matrix4f, box: Box, c: Color) {
        val mc = MinecraftClient.getInstance()

        val minX = (box.minX - mc.entityRenderDispatcher.camera.pos.getX()).toFloat()
        val minY = (box.minY - mc.entityRenderDispatcher.camera.pos.getY()).toFloat()
        val minZ = (box.minZ - mc.entityRenderDispatcher.camera.pos.getZ()).toFloat()
        val maxX = (box.maxX - mc.entityRenderDispatcher.camera.pos.getX()).toFloat()
        val maxY = (box.maxY - mc.entityRenderDispatcher.camera.pos.getY()).toFloat()
        val maxZ = (box.maxZ - mc.entityRenderDispatcher.camera.pos.getZ()).toFloat()
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.rgb)
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.rgb)
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.rgb)
    }


    fun vertexLine(
        matrices: MatrixStack,
        buffer: VertexConsumer,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float,
        lineColor: Color
    ) {
        val model = matrices.peek().positionMatrix
        val entry = matrices.peek()
        val normalVec = getNormal(x1, y1, z1, x2, y2, z2)

        buffer.vertex(model, x1, y1, z1).color(lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha)
            .normal(entry, normalVec.x(), normalVec.y(), normalVec.z())
        buffer.vertex(model, x2, y2, z2).color(lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha)
            .normal(entry, normalVec.x(), normalVec.y(), normalVec.z())
    }

    fun worldSpaceToScreenSpace(pos: Vec3d): Vec3d {
        val mc = MinecraftClient.getInstance()

        val camera = mc.entityRenderDispatcher.camera
        val displayHeight = mc.window.height
        val viewport = IntArray(4)
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport)
        val target = Vector3f()
        val deltaX = pos.x - camera.pos.x
        val deltaY = pos.y - camera.pos.y
        val deltaZ = pos.z - camera.pos.z
        val transformedCoordinates: Vector4f =
            Vector4f(deltaX.toFloat(), deltaY.toFloat(), deltaZ.toFloat(), 1f).mul(lastWorldSpaceMatrix)
        val matrixProj = Matrix4f(lastProjMat)
        val matrixModel = Matrix4f(lastModMat)
        matrixProj.mul(matrixModel).project(
            transformedCoordinates.x(),
            transformedCoordinates.y(),
            transformedCoordinates.z(),
            viewport,
            target
        )
        return Vec3d(
            target.x / mc.window.scaleFactor,
            (displayHeight - target.y) / mc.window.scaleFactor,
            target.z.toDouble()
        )
    }


    fun getNormal(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Vector3f {
        val xNormal = x2 - x1
        val yNormal = y2 - y1
        val zNormal = z2 - z1
        val normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal)
        return Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt)
    }

    fun matrixFrom(x: Double, y: Double, z: Double): MatrixStack {
        val matrices = MatrixStack()
        val camera: Camera = MinecraftClient.getInstance().gameRenderer.camera
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.pitch))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.yaw + 180.0f))
        matrices.translate(x - camera.pos.x, y - camera.pos.y, z - camera.pos.z)
        return matrices
    }

    fun setup() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
    }

    fun cleanup() {
        disableBlend()
    }

    data class OutlineAction(val box: Box, val color: Color, val lineWidth: Float)

    data class FilledAction(val box: Box, val color: Color)

}