package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PresenterScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + CoroutineName("CatsCoroutine") + Job()
}

class CatsPresenter(
    private val catsRepository: CatsRepository
) {
    private val presenterScope: PresenterScope = PresenterScope()

    private var _catsView: ICatsView? = null
    private var job: Job? = null

    fun onInitComplete() {
        job = presenterScope.launch {
            try {
                coroutineScope {
                    val fact = async { catsRepository.getCatFact() }
                    val imageLink = async { catsRepository.getImageLink() }

                    _catsView?.populate(Cat(fact.await(), imageLink.await()))
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }


    private fun onFailure(e: Exception) {
        val exceptionMessage: String? =
            if (e is java.net.SocketTimeoutException) {
                "Не удалось получить ответ от сервером"
            } else {
                CrashMonitor.trackWarning(e)
                e.message
            }
        exceptionMessage?.let { _catsView?.showError(it) }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        job?.cancel()
    }
}

//private operator fun <T> Lazy<T>.getValue(
//    catsPresenter: CatsPresenter,
//    property: KProperty<T?>
//): CoroutineScope {
//
//}
