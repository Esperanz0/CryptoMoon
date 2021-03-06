package com.rmnivnv.cryptomoon.ui.coins

import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rmnivnv.cryptomoon.R
import com.rmnivnv.cryptomoon.model.DisplayCoin
import com.rmnivnv.cryptomoon.model.HoldingsHandler
import com.rmnivnv.cryptomoon.model.MultiSelector
import com.rmnivnv.cryptomoon.utils.ResourceProvider
import com.rmnivnv.cryptomoon.utils.doubleFromString
import com.rmnivnv.cryptomoon.utils.getChangeColor
import com.rmnivnv.cryptomoon.utils.getStringWithTwoDecimalsFromDouble
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.coins_list_item_refactor.view.*

/**
 * Created by rmnivnv on 02/07/2017.
 */

class CoinsListAdapter(private val coins: ArrayList<DisplayCoin>,
                       private val resProvider: ResourceProvider,
                       private val multiSelector: MultiSelector,
                       private val holdingsHandler: HoldingsHandler,
                       val clickListener: (DisplayCoin) -> Unit) : RecyclerView.Adapter<CoinsListAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.coins_list_item_refactor, parent, false))

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItems(coins[position], clickListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(coin: DisplayCoin, listener: (DisplayCoin) -> Unit) = with(itemView) {
            setOnClickListener {
                if (multiSelector.atLeastOneIsSelected) {
                    multiSelector.onClick(coin, main_item_card, coins)
                } else {
                    listener(coin)
                }
            }
            setOnLongClickListener {
                multiSelector.onClick(coin, main_item_card, coins)
            }
            if (coin.selected) main_item_card.setBackgroundColor(resProvider.getColor(R.color.colorAccent))
            main_item_from.text = coin.from
            main_item_to.text = """ / ${coin.to}"""
            main_item_full_name.text = coin.fullName
            main_item_last_price.text = coin.PRICE
            main_item_change_in_24.text = """${coin.CHANGEPCT24HOUR}%"""
            main_item_change_in_24.setTextColor(resProvider.getColor(getChangeColor(doubleFromString(coin.CHANGEPCT24HOUR))))
            main_item_price_arrow.setImageDrawable(resProvider.getDrawable(getChangeArrowDrawable(doubleFromString(coin.CHANGEPCT24HOUR))))
            DrawableCompat.setTint(main_item_price_arrow.drawable, resProvider.getColor(getChangeColor(doubleFromString(coin.CHANGEPCT24HOUR))))
            if (coin.imgUrl.isNotEmpty()) {
                Picasso.with(context)
                        .load(coin.imgUrl)
                        .into(main_item_market_logo)
            }

            val holding = holdingsHandler.isThereSuchHolding(coin.from, coin.to)
            if (holding != null) {
                main_item_holding_qty.text = getStringWithTwoDecimalsFromDouble(holding.quantity)
                main_item_holding_value.text = "$${getStringWithTwoDecimalsFromDouble(holdingsHandler.getTotalValueWithCurrentPriceByHoldingData(holding))}"
                main_item_holding_qty.visibility = View.VISIBLE
                main_item_holding_value.visibility = View.VISIBLE
            } else {
                main_item_holding_qty.visibility = View.GONE
                main_item_holding_value.visibility = View.GONE
            }
        }
    }

    private fun getChangeArrowDrawable(change: Double) = when {
        change > 0 -> R.drawable.ic_arrow_drop_up
        change == 0.0 -> R.drawable.ic_remove
        else -> R.drawable.ic_arrow_drop_down
    }

    override fun getItemCount() = coins.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position
}