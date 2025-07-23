package com.example.chorki_task

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        Log.d("MainActivity", "Registering NativeVideoViewFactory")
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "native-video-view",
                NativeVideoViewFactory(flutterEngine.dartExecutor.binaryMessenger)
            )
    }
}

class NativeVideoViewFactory(private val messenger: BinaryMessenger)
    : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        Log.d("NativeVideoViewFactory", "create() called with id: $viewId")
        return NativeVideoView(context, messenger, viewId)
    }
}

class NativeVideoView(context: Context, messenger: BinaryMessenger, id: Int) : PlatformView {
    private val playerView: PlayerView = PlayerView(context)
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    init {
        Log.d("NativeVideoView", "Initializing PlayerView and ExoPlayer")

        playerView.player = player
        playerView.useController = true

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        player.setAudioAttributes(audioAttributes, true)

        val mediaItem = MediaItem.fromUri(
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
        )
        player.setMediaItem(mediaItem)
        player.prepare()

        val channelName = "video_player_channel_$id"
        val channel = MethodChannel(messenger, channelName)
        Log.d("NativeVideoView", "Setting MethodChannel handler on $channelName")

        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "play" -> {
                    Log.d("NativeVideoView", "Play called")
                    player.play()
                    result.success(null)
                }
                "pause" -> {
                    Log.d("NativeVideoView", "Pause called")
                    player.pause()
                    result.success(null)
                }
                else -> {
                    Log.d("NativeVideoView", "Method not implemented: ${call.method}")
                    result.notImplemented()
                }
            }
        }
    }

    override fun getView(): View {
        Log.d("NativeVideoView", "getView called")
        return playerView
    }

    override fun dispose() {
        Log.d("NativeVideoView", "dispose called")
        player.pause()
        player.release()
    }
}
