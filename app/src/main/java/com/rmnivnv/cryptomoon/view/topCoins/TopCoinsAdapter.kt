package com.rmnivnv.cryptomoon.view.topCoins

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rmnivnv.cryptomoon.R
import com.rmnivnv.cryptomoon.model.CoinsController
import com.rmnivnv.cryptomoon.model.TopCoinData
import com.rmnivnv.cryptomoon.utils.ResourceProvider
import com.rmnivnv.cryptomoon.utils.addCommasToStringNumber
import com.rmnivnv.cryptomoon.utils.doubleFromString
import com.rmnivnv.cryptomoon.utils.getChangeColor
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.top_coin_item.view.*
import javax.inject.Inject

/**
 * Created by rmnivnv on 02/09/2017.
 */
class TopCoinsAdapter(private val coins: ArrayList<TopCoinData>,
                      component: TopCoinsComponent,
                      val clickListener: (TopCoinData) -> Unit) : RecyclerView.Adapter<TopCoinsAdapter.ViewHolder>() {
    init {
        component.inject(this)
    }

    @Inject lateinit var resProvider: ResourceProvider
    @Inject lateinit var presenter: ITopCoins.Presenter
    @Inject lateinit var coinsController: CoinsController

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.top_coin_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItems(coins[position], clickListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(coin: TopCoinData, listener: (TopCoinData) -> Unit) = with(itemView) {
            setOnClickListener { listener(coin) }
            top_coin_rank.text = coin.rank.toString()
            top_coin_name.text = coin.name
            top_coin_price.text = coin.price_usd
            top_coin_24h_pct.text = coin.percent_change_24h
            top_coin_24h_pct.setTextColor(resProvider.getColor(getChangeColor(doubleFromString(coin.percent_change_24h!!))))
            top_coin_pct_sign.setTextColor(resProvider.getColor(getChangeColor(doubleFromString(coin.percent_change_24h!!))))
            top_coin_market_cap.text = addCommasToStringNumber(coin.market_cap_usd)
            top_coin_supply.text = addCommasToStringNumber(coin.total_supply)
            top_coin_volume_24h.text = addCommasToStringNumber(coin.vol24Usd)
            if (!coin.imgUrl.isNullOrEmpty()) {
                Picasso.with(context)
                        .load(coin.imgUrl)
                        .into(top_coin_logo)
            }
            if (coinsController.coinIsAdded(coin)) {
                top_coin_add_icon.setImageDrawable(resProvider.getDrawable(R.drawable.ic_done))
            } else {
                top_coin_add_icon.setImageDrawable(resProvider.getDrawable(R.drawable.ic_add_circle))
                top_coin_add_layout.setOnClickListener {
                    presenter.onAddCoinClicked(coin, itemView)
                }
            }
        }
    }

    override fun getItemCount() = coins.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position
}