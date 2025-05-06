package com.example.financetracker.adapter

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.model.Transaction

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val context: Context,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val ivTypeIcon: ImageView = itemView.findViewById(R.id.ivTypeIcon)
        val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)

        init {
            cardContainer.setOnLongClickListener {
                showPopupMenu(adapterPosition, it)
                true
            }
        }

        private fun showPopupMenu(position: Int, anchor: View) {
            val popup = PopupMenu(context, anchor)
            popup.menuInflater.inflate(R.menu.transaction_popup_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit -> {
                        onEdit(transactions[position])
                        true
                    }
                    R.id.menu_delete -> {
                        onDelete(transactions[position])
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val txn = transactions[position]
        holder.tvTitle.text = txn.title
        holder.tvTime.text = txn.timestamp
        holder.tvAmount.text = "${txn.currency} %.2f".format(txn.amount)
        holder.tvAmount.setTextColor(
            if (txn.type == "Expense") context.getColor(R.color.red)
            else context.getColor(R.color.teal_200)
        )
        holder.ivTypeIcon.setImageResource(
            if (txn.type == "Expense") R.drawable.ic_arrow_down
            else R.drawable.ic_arrow_up
        )
    }

    override fun getItemCount(): Int = transactions.size

    fun filterList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}

