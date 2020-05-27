package ru.gdcn.polytorrent

import org.apache.log4j.Level

object TorrentConfig {

    const val USE_DEFAULT_TRACKERS = true
    const val MAX_PEER_CONNECTIONS = 20
    val LOGGING_LEVEL = Level.OFF
    const val TRACKER_ASKING_THREADS = 8
    val DEFAULT_TORRENTS = listOf(
        "http://tracker.thepiratebay.org/announce",
        "http://denis.stalker.h3q.com:6969/announce",
        "http://tracker.openbittorrent.com/announce",
        "http://tracker.torrentbay.to:6969/announce",
        "http://tracker.istole.it:80/announce",
        "http://tracker.torrent.to:2710/announce",
        "http://papaja.v2v.cc:6970/announce",
        "http://i.bandito.org/announce.php?uk=aFnt7k16j6&",
        "http://tracker.publicbt.com:80/announce",
        "http://tracker.tfile.me/announce.php?uk=aFnt7k16j6&",
        "http://tracker.tfile.co/announce.php?uk=aFnt7k16j6&",
        "http://retracker.home/announce",
        "http://tracker3.torrentino.com/announce?passkey=00000000000000000000000000000000",
        "http://bt.nnm-club.ru:2710/announce",
        "http://bt.nnm-club.info:2710/announce",
        "http://www.filebase.ws:5678/announce",
        "http://exodus.desync.com/announce",
        "http://www.progressivetorrents.com/announce.php",
        "http://retracker.bashtel.ru/announce.php",
        "http://radioarchive.cc/announce.php",
        "http://medbit.ru/announce.php",
        "http://piratbit.net/bt/announce.php",
        "http://sound-park.ru/announce.php",
        "http://retracker.local/announce",
        "http://tracker.filetracker.pl:8089/announce",
        "http://tracker2.wasabii.com.tw:6969/announce",
        "http://tracker.grepler.com:6969/announce",
        "http://80.246.243.18:6969/announce",
        "http://125.227.35.196:6969/announce",
        "http://tracker.tiny-vps.com:6969/announce",
        "http://87.248.186.252:8080/announce",
        "http://www.torrentheaven.de/announce.php",
        "http://tracker.mp3-es.com/announce.php",
        "http://tracker.calculate.ru:6969/announce",
        "http://210.244.71.25:6969/announce",
        "http://46.4.109.148:6969/announce",
        "http://tracker.dler.org:6969/announce)"
    )


//    val LOGGING_LEVEL = Level.INFO


}