CastDemo
========

Demo of ChromCast function use CCL(ChromCast library)

Current I find the below m3u8 link can be cast:
http://download.clockworkmod.com/cast/bitrate-500000-bps.m3u8

many local and internet stream still have issue with below kind of message:

XMLHttpRequest cannot load http://137.116.170.232:3000/record/20012/stream.m3u8. No 'Access-Control-Allow-Origin' header is present on the requested resource. Origin 'https://www.gstatic.com' is therefore not allowed access. player.html:1
1.  [ 3.587s] [cast.receiver.MediaManager] Load metadata error

Also I setup an Apache/2.0.65 (Win32) Server in my note book, and also find some m3u8 can be cast(seemd low bit rate) and some with same error. The stream is from Google sample ( http://commondatastorage.googleapis.com/cast-media-server-samples%2Fmedia.zip)
