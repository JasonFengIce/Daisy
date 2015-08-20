package tv.ismar.daisy.qiyimediaplayer;

import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake video data source.
 */
public class FakeVideoFactory {
    // fake video data:                             album id        tv id           vid
    private static final String[] FAKE_VIDEO_1 = {  "300266900",    "300266900",    "a53ce6122c860a4bd9f15f107b265de5"  };  // 纯纯欲动
    private static final String[] FAKE_VIDEO_2 = {  "202153901",    "308529000",    "8d301d7723586e7a0e1ecb778ada0cb5"  };  // 势不两立 第一集
    private static final String[] FAKE_VIDEO_3 = {  "201175901",    "325443000",    "233765f4e7d9846612941bccb2937c88"  };  // 地球创世纪(4:3)
    private static final String[] FAKE_VIDEO_4 = {  "299926600",    "299926600",    "1c70d29855cf572c9153767ddfb68f75"  };  // X站警：逆转未来
    
    private static final List<String[]> FAKE_VIDEOS;
    static {
        FAKE_VIDEOS = new ArrayList<String[]>();
        FAKE_VIDEOS.add(FAKE_VIDEO_1);
        FAKE_VIDEOS.add(FAKE_VIDEO_2);
        FAKE_VIDEOS.add(FAKE_VIDEO_3);
        FAKE_VIDEOS.add(FAKE_VIDEO_4);
    }
    
    private FakeVideoFactory() { }
    
    public static IPlaybackInfo getFakeVideoAt(int index, Definition definition) {
        int count = getFakeVideoCount();
        return (index < 0 || index >= count) ? null : new SdkVideo(FAKE_VIDEOS.get(index)[0], FAKE_VIDEOS.get(index)[1], FAKE_VIDEOS.get(index)[2],definition);
    }
    
    public static IPlaybackInfo getFakeVideoAt(int index) {
        return getFakeVideoAt(index, Definition.DEFINITON_1080P);
    }
    
    public static int getFakeVideoCount() {
        return FAKE_VIDEOS.size();
    }
}
