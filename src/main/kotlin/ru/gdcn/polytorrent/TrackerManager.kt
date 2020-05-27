package ru.gdcn.polytorrent

import com.dampcake.bencode.Bencode
import com.dampcake.bencode.BencodeException
import com.dampcake.bencode.Type
import khttp.*
import khttp.responses.Response
import org.apache.log4j.LogManager
import ru.gdcn.polytorrent.Utilities.byteArrayToURLString
import java.io.Closeable
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.schedule
import kotlin.streams.toList

class TrackerManager(private val metafile: Metadata, private val peerId: ByteArray) : Closeable {
    private val trackerList: MutableList<String> = mutableListOf()
    private var validTracker: String? = null
    private var timer: Timer? = null
    private val logger = LogManager.getLogger(this::class.java)

    init {
        if (metafile.announceList.isEmpty()) {
            logger.info("Добавление единственного трекера в очередь опроса")
            trackerList.add(metafile.announce)
        } else {
            logger.info("Добавление трекеров из аннонс-листа в очередь опроса")
            trackerList.addAll(metafile.announceList)
        }
        if (TorrentConfig.USE_DEFAULT_TRACKERS){
            trackerList.addAll(TorrentConfig.DEFAULT_TORRENTS)
        }
        trackerList.shuffle()
    }

    fun getAnnounceInfo(): AnnounceInfo {
        val futures = mutableListOf<Future<Pair<Optional<Response>, String>>>()
        logger.info("Создание пула экзекьюторов (аеее)")
        val executorService = Executors.newFixedThreadPool(TorrentConfig.TRACKER_ASKING_THREADS)

        logger.info("Идём по списку трекеров")
        for (tracker in trackerList) {
            if (!tracker.startsWith("http")) {
                logger.warn("Неподдерживаемый формат адреса: $tracker")
                continue
            }

            val future: Future<Pair<Optional<Response>, String>> =
                executorService.submit<Pair<Optional<Response>, String>> {
                    logger.info("Опрашиваю $tracker")
                    return@submit Pair(askTracker(tracker), tracker)
                }
            futures.add(future)
        }

        //Ждём пока все фучуры выполнятся
        logger.info("Ожидание окончания опроса (должно быть не больше 3 секунд)")
        for (future in futures) {
            future.get()
        }

        logger.info("Выключаю сервис исполнителей")
        //Вырубаем работяг
        executorService.shutdown()

//        val dictionaries = mutableListOf<MutableMap<String, Any>>()
        //А теперь точно всё выполнилось и проверяем чё получили
        logger.info("Проходимся по полученным ответам")
        var resultAnnounceInfo : AnnounceInfo? = null
        for (future in futures) {
            if (future.get().first.isEmpty) {
                continue
            } else {
                try {
                    logger.info("Парсим ответ трекера")
                    val responseDictionary = Bencode()
                        .decode(future.get().first.get().content, Type.DICTIONARY)
                    if (responseDictionary.containsKey("failure reason")) {
                        logger.error("Трекер прислал ошибку: ${responseDictionary["failure reason"].toString()}")
                        continue
                    } else {
                        logger.info("Получили корректный ответ от одного из трекеров")
                        validTracker = future.get().second
                        val announceInfo = AnnounceInfo(responseDictionary)
                        try {
                            announceInfo.peers
                        } catch (e: IllegalStateException){
                            logger.error("Не смог распарсить ответ от трекера")
                            continue
                        }
                        if (announceInfo.peers.isEmpty()){
                            continue
                        }
                        if (resultAnnounceInfo == null){
                            resultAnnounceInfo = announceInfo
                        } else {
                            resultAnnounceInfo.peers.addAll(announceInfo.peers)
                        }
//                        setTrackerAskingTimer(announceInfo.interval)
                    }
                } catch (e: BencodeException) {
                    logger.error("Не удалось распарсить ответ от трекера")
                    //Не удалось распарсить ответ
                    continue
                } catch (e: java.lang.IllegalStateException) {
                    logger.error("Неправильный формат сообщения от трекера")
                    continue
                }
            }
        }

        if (resultAnnounceInfo == null){
            throw IllegalStateException("Не удалось получить информацию об анонсах")
        } else {
            return resultAnnounceInfo
        }
    }

    private fun setTrackerAskingTimer(interval: Int) {
        logger.info("Запускаем таймер на опрос трекера с интервалом $interval секунд")
        close()
        logger.info("Создаю новый экземпляр таймера")
        timer = Timer()
        timer!!.schedule(delay = interval.toLong() * 1000L, period = interval * 1000L, action = {
            if (validTracker == null) {
                logger.error("Валидный трекер не назначен")
            } else {
                logger.info("Опрашиваю трекер")
                askTracker(validTracker!!)
            }
        })
    }

    private fun askTracker(urlString: String): Optional<Response> {
        logger.info("Формирую запрос")
        val parameters = mutableMapOf<String, String>()
        parameters["info_hash"] = byteArrayToURLString(metafile.infoHash.toByteArray())
        parameters["peer_id"] = byteArrayToURLString(peerId)
        parameters["port"] = Utils.PORT
        parameters["uploaded"] = "0"
        parameters["downloaded"] = "0"
        parameters["left"] = metafile.info.fileDatas.map { it.length }.sum().toString()
//        parameters["compact"] = "1"

        val encodedUrl = urlString + "?" + parameters.entries.stream()
            .map { it.key + "=" + it.value }
            .toList()
            .joinToString("&")

//        println(parameters.entries.joinToString())

        return try {
            logger.info("Отправляю запрос")
            val response = get(encodedUrl, timeout = Utils.TRACKER_TIMEOUT)
            Optional.of(response)
        } catch (e: SocketTimeoutException) {
            logger.warn("Трекер не ответил")
            Optional.empty()
        } catch (e: UnknownHostException) {
            logger.error("Не удалось узнать адрес хоста")
            Optional.empty()
        } catch (e: ConnectException) {
            logger.error("Ошибка подключения")
            Optional.empty()
        } catch (e: SocketException) {
            logger.error("Сеть трекера недоступна")
            Optional.empty()
        }
    }

    override fun close() {
        logger.info("Отменяю таймер, если он существует")
        if (timer != null) {
            logger.info("Отменяю существующий таймер")
            timer!!.cancel()
        }
        timer = null
    }

}