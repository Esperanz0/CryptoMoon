package com.rmnivnv.cryptomoon.view.coins

import android.content.Intent
import android.util.Log
import com.rmnivnv.cryptomoon.MainApp
import com.rmnivnv.cryptomoon.R
import com.rmnivnv.cryptomoon.model.*
import com.rmnivnv.cryptomoon.model.db.CMDatabase
import com.rmnivnv.cryptomoon.model.rxbus.CoinsLoadingEvent
import com.rmnivnv.cryptomoon.model.rxbus.MainCoinsListUpdatedEvent
import com.rmnivnv.cryptomoon.model.rxbus.OnDeleteCoinsMenuItemClickedEvent
import com.rmnivnv.cryptomoon.model.rxbus.RxBus
import com.rmnivnv.cryptomoon.network.NetworkRequests
import com.rmnivnv.cryptomoon.utils.ResourceProvider
import com.rmnivnv.cryptomoon.utils.createCoinsMapWithCurrencies
import com.rmnivnv.cryptomoon.utils.toastShort
import com.rmnivnv.cryptomoon.view.coins.coinInfo.CoinInfoActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by rmnivnv on 11/07/2017.
 */
class CoinsPresenter : ICoins.Presenter {

    @Inject lateinit var app: MainApp
    @Inject lateinit var view: ICoins.View
    @Inject lateinit var networkRequests: NetworkRequests
    @Inject lateinit var coinsController: CoinsController
    @Inject lateinit var db: CMDatabase
    @Inject lateinit var resProvider: ResourceProvider
    @Inject lateinit var pageController: PageController
    @Inject lateinit var multiSelector: MultiSelector

    private val disposable = CompositeDisposable()
    private var coins: ArrayList<DisplayCoin> = ArrayList()
    private var isRefreshing = false
    private var isFirstStart = true

    override fun onCreate(component: CoinsComponent, coins: ArrayList<DisplayCoin>) {
        component.inject(this)
        this.coins = coins
        subscribeToObservables()
        getAllCoinsInfo()
    }

    private fun subscribeToObservables() {
        addCoinsChangesObservable()
        setupRxBusEventsListeners()
        addOnPageChangedObservable()
    }

    private fun addCoinsChangesObservable() {
        disposable.add(db.displayCoinsDao().getAllCoins()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onCoinsFromDbUpdates(it) }))
    }

    private fun onCoinsFromDbUpdates(list: List<DisplayCoin>) {
        if (list.isNotEmpty()) {
            coins.clear()
            coins.addAll(list)
            coins.sortBy { it.from }
            view.updateRecyclerView()
            if (isFirstStart) {
                isFirstStart = false
                updatePrices()
            }
        }
    }

    private fun setupRxBusEventsListeners() {
        disposable.add(RxBus.listen(OnDeleteCoinsMenuItemClickedEvent::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onDeleteClicked() })
    }

    private fun onDeleteClicked() {
        val coinsToDelete = coins.filter { it.selected }
        if (coinsToDelete.isNotEmpty()) {
            val toast = if (coinsToDelete.size > 1) resProvider.getString(R.string.coins_deleted)
            else resProvider.getString(R.string.coin_deleted)
            coinsController.deleteDisplayCoins(coinsToDelete)
            app.toastShort(toast)
            multiSelector.atLeastOneIsSelected = false
            RxBus.publish(MainCoinsListUpdatedEvent())
        }
    }

    private fun addOnPageChangedObservable() {
        disposable.add(pageController.getPageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onPageChanged(it) })
    }

    private fun onPageChanged(position: Int) {
        if (position != COINS_FRAGMENT_PAGE_POSITION) {
            disableSelected()
        }
    }

    private fun disableSelected() {
        if (multiSelector.atLeastOneIsSelected) {
            coins.forEach { if (it.selected) it.selected = false }
            view.updateRecyclerView()
            multiSelector.atLeastOneIsSelected = false
        }
    }

    private fun getAllCoinsInfo() {
        disposable.add(networkRequests.getAllCoins(object : GetAllCoinsCallback {
            override fun onSuccess(allCoins: ArrayList<InfoCoin>) {
                if (allCoins.isNotEmpty()) {
                    coinsController.saveAllCoinsInfo(allCoins)
                }
            }

            override fun onError(t: Throwable) {
                Log.d("onError", t.message)
            }
        }))
    }

    override fun onViewCreated() {

    }

    override fun onStart() {
        if (coins.isNotEmpty()) updatePrices()
    }

    private fun updatePrices() {
        val queryMap = createCoinsMapWithCurrencies(coins)
        if (queryMap.isNotEmpty()) {
            RxBus.publish(CoinsLoadingEvent(true))
            disposable.add(networkRequests.getPrice(queryMap, object : GetPriceCallback {
                override fun onSuccess(coinsInfoList: ArrayList<DisplayCoin>?) {
                    if (coinsInfoList != null && coinsInfoList.isNotEmpty()) {
                        coinsController.saveDisplayCoinList(filterList(coinsInfoList))
                    }
                    afterRefreshing()
                }

                override fun onError(t: Throwable) {
                    afterRefreshing()
                }
            }))
        }
    }

    private fun filterList(coinsInfoList: ArrayList<DisplayCoin>): ArrayList<DisplayCoin> {
        val result: ArrayList<DisplayCoin> = ArrayList()
        coins.forEach {
            val coin = it
            val find = coinsInfoList.find { it.from == coin.from && it.to == coin.to }
            if (find != null) result.add(find)
        }
        return result
    }

    private fun afterRefreshing() {
        RxBus.publish(CoinsLoadingEvent(false))
        if (isRefreshing) {
            view.hideRefreshing()
            isRefreshing = false
            view.enableSwipeToRefresh()
        }
    }

    override fun onDestroy() {
        disposable.clear()
    }

    override fun onStop() {
        disableSelected()
    }

    override fun onSwipeUpdate() {
        disableSelected()
        isRefreshing = true
        updatePrices()
    }

    override fun onCoinClicked(coin: DisplayCoin) {
        val intent = Intent(app, CoinInfoActivity::class.java)
        intent.putExtra(NAME, coin.from)
        intent.putExtra(TO, coin.to)
        view.startActivityByIntent(intent)
    }
}