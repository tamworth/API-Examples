/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.beautyapi.bytedance

import android.content.Context
import android.view.View
import com.bytedance.labcv.effectsdk.RenderManager
import io.agora.base.VideoFrame
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine

/**
 * Version
 */
const val VERSION = "1.0.3"

/**
 * Capture mode
 *
 * @constructor Create empty Capture mode
 */
enum class CaptureMode{
    /**
     * Agora
     *
     * @constructor Create empty Agora
     */
    Agora, // 使用声网内部的祼数据接口进行处理

    /**
     * Custom
     *
     * @constructor Create empty Custom
     */
    Custom // 自定义模式，需要自己调用onFrame接口将原始视频帧传给BeautyAPI做处理
}

/**
 * Event callback
 *
 * @property onBeautyStats
 * @property onEffectInitialized
 * @property onEffectDestroyed
 * @constructor Create empty Event callback
 */
data class EventCallback(
    /**
     * 统计数据回调，每处理完一帧后会回调一次
     *
     * @param stats 美颜统计数据
     */
    val onBeautyStats: ((stats: BeautyStats)->Unit)? = null,

    /**
     * effectManager在GL线程里初始化完成后回调
     */
    val onEffectInitialized: (()->Unit)? = null,

    /**
     * effectManager在GL线程里销毁完成后回调
     */
    val onEffectDestroyed: (()->Unit)? = null
)

/**
 * Beauty stats
 *
 * @property minCostMs
 * @property maxCostMs
 * @property averageCostMs
 * @constructor Create empty Beauty stats
 */
data class BeautyStats(
    val minCostMs:Long, // 统计区间内的最小值
    val maxCostMs: Long, // 统计区间内的最大值
    val averageCostMs: Long // 统计区间内的平均值
)

/**
 * Mirror mode
 *
 * @constructor Create empty Mirror mode
 */
enum class MirrorMode {

    // 没有镜像正常画面的定义：前置拍到画面和手机看到画面是左右不一致的，后置拍到画面和手机看到画面是左右一致的

    /**
     * Mirror Local Remote
     *
     * @constructor Create empty Mirror Local Remote
     */
    MIRROR_LOCAL_REMOTE, //本地远端都镜像，前置默认，本地和远端贴纸都正常

    /**
     * Mirror Local Only
     *
     * @constructor Create empty Mirror Local Only
     */
    MIRROR_LOCAL_ONLY, // 仅本地镜像，远端不镜像，，远端贴纸正常，本地贴纸镜像。用于打电话场景，电商直播场景(保证电商直播后面的告示牌文字是正的)；这种模式因为本地远端是反的，所以肯定有一边的文字贴纸方向会是反的

    /**
     * Mirror Remote Only
     *
     * @constructor Create empty Mirror Remote Only
     */
    MIRROR_REMOTE_ONLY, // 仅远端镜像，本地不镜像，远端贴纸正常，本地贴纸镜像

    /**
     * Mirror None
     *
     * @constructor Create empty Mirror None
     */
    MIRROR_NONE // 本地远端都不镜像，后置默认，本地和远端贴纸都正常
}

/**
 * Camera config
 *
 * @property frontMirror
 * @property backMirror
 * @constructor Create empty Camera config
 */
data class CameraConfig(
    val frontMirror: MirrorMode = MirrorMode.MIRROR_LOCAL_REMOTE, // 前置默认镜像：本地远端都镜像
    val backMirror: MirrorMode = MirrorMode.MIRROR_NONE // 后置默认镜像：本地远端都不镜像
)

/**
 * Config
 *
 * @property context
 * @property rtcEngine
 * @property renderManager
 * @property eventCallback
 * @property captureMode
 * @property statsDuration
 * @property statsEnable
 * @property cameraConfig
 * @constructor Create empty Config
 */
data class Config(
    val context: Context, // Android Context上下文
    val rtcEngine: RtcEngine, // 声网Rtc引擎
    val renderManager: RenderManager, // 美颜SDK处理句柄
    val eventCallback: EventCallback? = null, // 事件回调
    val captureMode: CaptureMode = CaptureMode.Agora, // 处理模式
    val statsDuration: Long = 1000, // 统计区间
    val statsEnable: Boolean = false, // 是否开启统计
    val cameraConfig: CameraConfig = CameraConfig() // 摄像头镜像配置
)

/**
 * Error code
 *
 * @property value
 * @constructor Create empty Error code
 */
enum class ErrorCode(val value: Int) {
    /**
     * Error Ok
     *
     * @constructor Create empty Error Ok
     */
    ERROR_OK(0), // 一切正常

    /**
     * Error Has Not Initialized
     *
     * @constructor Create empty Error Has Not Initialized
     */
    ERROR_HAS_NOT_INITIALIZED(101), // 没有调用Initialize或调用失败情况下调用了其他API

    /**
     * Error Has Initialized
     *
     * @constructor Create empty Error Has Initialized
     */
    ERROR_HAS_INITIALIZED(102), // 已经Initialize成功后再次调用报错

    /**
     * Error Has Released
     *
     * @constructor Create empty Error Has Released
     */
    ERROR_HAS_RELEASED(103), // 已经调用release销毁后还调用其他API

    /**
     * Error Process Not Custom
     *
     * @constructor Create empty Error Process Not Custom
     */
    ERROR_PROCESS_NOT_CUSTOM(104), // 非Custom处理模式下调用onFrame接口从外部传入视频帧

    /**
     * Error Process Disable
     *
     * @constructor Create empty Error Process Disable
     */
    ERROR_PROCESS_DISABLE(105), // 当调用enable(false)禁用美颜后调用onFrame接口返回

    /**
     * Error View Type Error
     *
     * @constructor Create empty Error View Type Error
     */
    ERROR_VIEW_TYPE_ERROR(106), // 当调用setupLocalVideo时view类型错误时返回

    /**
     * Error Frame Skipped
     *
     * @constructor Create empty Error Frame Skipped
     */
    ERROR_FRAME_SKIPPED(107), // 当处理帧忽略时在onFrame返回
}

/**
 * Beauty preset
 *
 * @constructor Create empty Beauty preset
 */
enum class BeautyPreset {
    /**
     * Custom
     *
     * @constructor Create empty Custom
     */
    CUSTOM, // 不使用推荐的美颜参数

    /**
     * Default
     *
     * @constructor Create empty Default
     */
    DEFAULT // 默认的
}

/**
 * Create byte dance beauty a p i
 *
 * @return
 */
fun createByteDanceBeautyAPI(): ByteDanceBeautyAPI = ByteDanceBeautyAPIImpl()

/**
 * Byte dance beauty a p i
 *
 * @constructor Create empty Byte dance beauty a p i
 */
interface ByteDanceBeautyAPI {

    /**
     * 初始化API
     *
     * @param config 配置参数
     * @return 见ErrorCode
     */
    fun initialize(config: Config): Int

    /**
     * 开启/关闭美颜
     *
     * @param enable true：开启; false: 关闭
     * @return 见ErrorCode
     */
    fun enable(enable: Boolean): Int

    /**
     * 本地视图渲染，由内部来处理镜像问题
     *
     * @param view SurfaceView或TextureView
     * @param renderMode 渲染缩放模式
     * @return 见ErrorCode
     */
    fun setupLocalVideo(view: View, renderMode: Int = Constants.RENDER_MODE_HIDDEN): Int

    /**
     * 当ProcessMode==Custom时由外部传入原始视频帧
     *
     * @param videoFrame 原始视频帧
     * @return 见ErrorCode
     */
    fun onFrame(videoFrame: VideoFrame): Int

    /**
     * 声网提供的美颜最佳默认参数
     *
     * @return 见ErrorCode
     */
    fun setBeautyPreset(
        preset: BeautyPreset = BeautyPreset.DEFAULT,
        beautyNodePath: String,
        beauty4ItemNodePath: String,
        reSharpNodePath: String
    ): Int

    /**
     * 更新摄像头配置
     */
    fun updateCameraConfig(config: CameraConfig): Int

    /**
     * 是否是前置摄像头
     * PS：只在美颜处理中才能知道准确的值，否则会一直是true
     */
    fun isFrontCamera(): Boolean

    /**
     * Get mirror applied
     *
     * @return
     */
    fun getMirrorApplied(): Boolean

    /**
     * 私参配置，用于不对外api的调用，多用于测试
     */
    fun setParameters(key: String, value: String)

    /**
     * 释放资源，一旦释放后这个实例将无法使用
     *
     * @return 见ErrorCode
     */
    fun release(): Int

}