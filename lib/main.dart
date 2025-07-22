import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(VideoApp());

class VideoApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: VideoScreen(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class VideoScreen extends StatefulWidget {
  @override
  State<VideoScreen> createState() => _VideoScreenState();
}

class _VideoScreenState extends State<VideoScreen> {
  MethodChannel? _channel;
  bool isPlaying = false;
  bool _channelReady = false;

  void _togglePlayPause() {
    if (_channel == null) {
      print('⚠️ MethodChannel not ready! Cannot invoke native method.');
      return;
    }
    final method = isPlaying ? 'pause' : 'play';
    print('Flutter: Calling $method');
    _channel!.invokeMethod(method).catchError((error) {
      print('Flutter: Error calling $method: $error');
    });
    setState(() {
      isPlaying = !isPlaying;
    });
  }

  @override
  Widget build(BuildContext context) {
    final screenHeight = MediaQuery.of(context).size.height;
    final screenWidth = MediaQuery.of(context).size.width;

    // Uncomment this line to force enable button during debugging
    // _channelReady = true;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Native Video Player"),
        backgroundColor: Colors.black87,
      ),
      backgroundColor: Colors.grey[200],
      body: Column(
        children: [
          Container(
            height: screenHeight / 2.2,
            width: screenWidth,
            margin: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              color: Colors.black,
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: AndroidView(
                viewType: 'native-video-view',
                onPlatformViewCreated: (id) {
                  print('Flutter: Platform view created with id: $id');
                  _channel = MethodChannel('video_player_channel_$id');
                  print('Flutter: MethodChannel initialized: $_channel');
                  setState(() {
                    _channelReady = true;
                  });
                },
                creationParamsCodec: const StandardMessageCodec(),
              ),
            ),
          ),
          const Spacer(),
          Opacity(
            opacity: _channelReady ? 1 : 0.5,
            child: GestureDetector(
              onTap: _channelReady ? _togglePlayPause : null,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                decoration: BoxDecoration(
                  color: Colors.blueAccent,
                  borderRadius: BorderRadius.circular(30),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      isPlaying ? Icons.pause : Icons.play_arrow,
                      size: 32,
                      color: Colors.white,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isPlaying ? 'Pause' : 'Play',
                      style: const TextStyle(fontSize: 18, color: Colors.white),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }
}
